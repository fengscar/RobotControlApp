package com.feng.Usb;

import android.os.*;
import android.util.Log;
import com.feng.Constant.I_Parameters;
import com.feng.Usb.ArmHandler.MotionHandler;
import com.feng.Usb.ArmHandler.PowerHandler;
import com.feng.Usb.UsbSerial.UsbSerialConnector;
import com.feng.Usb.UsbSerial.UsbSerial_CP2102;
import com.feng.Usb.UsbSerial.UsbSerial_PL2303;
import com.feng.Utils.L;
import com.feng.Utils.T;
import com.feng.Utils.Transfer;
import com.feng.Utils.Verifier;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2016-2-29 下午4:46:11
 * 功能: 管理USB的连接,重连,断开..发送/接收
 */

public class ArmUsbManager implements ArmProtocol, I_Parameters {
    private final static String TAG = ArmUsbManager.class.getSimpleName();

    private static UsbSerialDriver sUsbSerialDriver;

    //region 单例模式的实现

    private static ArmUsbManager instance = null;

    protected ArmUsbManager() {
    }

    public static ArmUsbManager getInstance() {
        if (instance == null) {
            synchronized (ArmUsbManager.class) {
                if (instance == null) {
                    instance = new ArmUsbManager();
                }
            }
        }
        return instance;
    }
    //endregion

    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService sendExecutor = Executors.newFixedThreadPool(5);

    // Runnable 对象.  监听usb的接收
    private SerialInputOutputManager mSerialIoManager;

    // 保存接收到的数据
    private byte[] mRecData = new byte[1024];
    private int mRecCount = 0; //当前接收到的数据
    private final SerialInputOutputManager.Listener mListener = new SerialInputOutputManager.Listener() {
        @Override
        public void onRunError(Exception e) {
            e.printStackTrace();
        }

        // 由于串口模块输出时,数据可能出现截断或者粘包,所以要加入一个缓冲区,来循环判断接收的数据是否正确
        @Override
        public void onNewData(final byte[] data) {
            // 将接收到的数据存放到 缓存中
            System.arraycopy(data, 0, mRecData, mRecCount, data.length);
            mRecCount += data.length;

            byte[] debugCache = new byte[mRecCount]; //用来输出调试信息
            System.arraycopy(mRecData, 0, debugCache, 0, mRecCount);
            Log.w(TAG, "receive: " + Arrays.toString(data) + ", cache: " + Arrays.toString(debugCache));

            //判断是否是正确的数据 (过滤调试信息)
            int mIndex = 0;
            while (mRecData[mIndex] != 0x01 && mRecCount > 0) {
                mIndex++;
                mRecCount--;
            }
            // 如果当前缓存区有数据(>5), 并且已经完整接收到数据
            while (mRecCount >= 5 && mRecData[mIndex + DATA_LENGTH] + 5 <= mRecCount) {
                // 判断数据长度是否正确
                int commandLength = mRecData[mIndex + DATA_LENGTH] + 5;
                if (commandLength < 0) {
                    mIndex++;
                    mRecCount--;
                    continue;
                }
                // 处理一条data
                byte[] command = new byte[commandLength];
                System.arraycopy(mRecData, mIndex, command, 0, commandLength);
                if (mVerifier.verify(command)) {
                    /**
                     * 识别出正确的一条命令,开始处理
                     */
                    receive(command);
                    // 累计已处理的数据数
                    mIndex += commandLength;
                    mRecCount -= commandLength;
                } else {
                    // 无法通过校验, mIndex++
                    mIndex++;
                    mRecCount--;
                }
            }
            if (mIndex == 0) {
                return;
            } else {
                // 清理缓存 ( 数据前移,计数重置)
                System.arraycopy(mRecData, mIndex, mRecData, 0, mRecCount);
                L.e(TAG, "处理了 " + mIndex + " 字节数据");
            }
        }
    };

    protected Map<String, Handler> activityHandlerMap = new HashMap<>();

    /**
     * 添加观察者, 当系统状态改变时/接受数据时 ,通知该系统的handler,让其发送数据
     */
    public void addObserver(String activityName, Handler activityMessagehandler) {
        if (activityHandlerMap.containsKey(activityName)) {
            return;
        }
        activityHandlerMap.put(activityName, activityMessagehandler);
        L.i(TAG, "添加观察者: " + activityName + ",当前有 " + activityHandlerMap.size() + " 个观察者");
    }

    public void delObserver(String activityKey) {
        activityHandlerMap.remove(activityKey);
        L.i(TAG, "删除观察者: " + activityKey + ",当前有 " + activityHandlerMap.size() + " 个观察者");
    }

    /**
     * 通知Activity(Usb服务端向客户端发送通知)..(具体事件定义查看 {@link com.feng.Usb.UsbEvent }
     */
    public void notifyActivities(UsbData usbData) {
        for (Handler handler : activityHandlerMap.values()) {
            if (handler != null) {
                Message message = handler.obtainMessage();
                // 统一格式的USB数据
                message.obj = usbData;
                //让activity对应的handler 处理该信息
                handler.sendMessage(message);
            }
        }
    }

    private void notifyActivities(UsbEvent event) {
        this.notifyActivities(new UsbData(event));
    }


    // 当前接收的数据
    private byte[] currentReceive;
    private Verifier mVerifier = new Verifier();
    private boolean mConnectFlag = false;

    public boolean isConnect() {
        return mConnectFlag;
    }

    //内部的handler ,用来处理重连等问题
    private final int ACTION_RECONNECT_USB = 122;
    //    private final int ACTION_QUERY_STATE = 123;
//    private final int ACTION_QUERY_POWER = 124;
    private Handler innerHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ACTION_RECONNECT_USB:
                    reconnect();
                    break;

//                case ACTION_QUERY_STATE:
//                    removeMessages(ACTION_QUERY_STATE);
//                    motionHandler.queryMotionState();
//                    sendEmptyMessageDelayed(ACTION_QUERY_STATE, 500);
//                    break;
//
//                case ACTION_QUERY_POWER:
//                    removeMessages(ACTION_QUERY_POWER);
//                    powerHandler.queryCurrentPower();
//                    sendEmptyMessageDelayed(ACTION_QUERY_STATE, 180000);
//
//                    break;

                default:
                    break;
            }
        }
    };

    //region 轮询机器人的方法
    private Timer queryTimer;

    private void startQueryRobot() {
        queryTimer = new Timer();
        queryTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                MotionHandler.getInstance().queryMotionState();
            }
        }, 1000, 1000); //延迟0ms开始查询,间隔1000ms一次

        queryTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                PowerHandler.getInstance().queryCurrentPower();
            }
        }, 1000, 180000); //延迟0ms开始查询,半分钟查询一次
    }

    private void stopQueryRobot() {
        if (queryTimer != null) {
            queryTimer.cancel();
        }
    }

    //endregion的方法的方法


    private void startIoManager() {
        if (sUsbSerialDriver != null) {
            L.i(TAG, "Starting io manager...");
            mSerialIoManager = new SerialInputOutputManager(sUsbSerialDriver, mListener);
            mExecutor.submit(mSerialIoManager);
            mConnectFlag = true;


        }
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            L.e(TAG, "Stopping io manager...");
            mSerialIoManager.stop();
            mSerialIoManager = null;
            mConnectFlag = false;
        }
    }


    public void reply(byte[] data, boolean isSuccess) {
        // 获取 0 1 2 作为头, 并将 data 设置为 成功 , 打包后 发送出去;
        byte[] dataToSend = new Transfer().packingByte(
                new byte[]{data[0], data[1], data[2]},
                isSuccess ? new byte[]{(byte) 0x01} : new byte[]{(byte) 0x00});
        try {
            sUsbSerialDriver.write(dataToSend, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void receive(byte[] data) {
        currentReceive = data;
        // 最后一个参数表示不需要打包
        if (belongAction(data, RECEIVE_ACTIONS)) {
            //自动回复.
            this.reply(data, true);
            L.i(TAG, "[USB接收]: 已自动回复" + Arrays.toString(data));
            notifyActivities(new UsbData(data));
        } else {
            L.w(TAG, "[ARM-USB回复]: " + Arrays.toString(data));
        }
    }

    public void send(byte[] data) {
        if (!mConnectFlag) {
            L.e(TAG, "USB还未连接,发送失败:" + Arrays.toString(data));
            notifyActivities(new UsbData(UsbEvent.UsbConnectFailed));
            return;
        }
        if (mVerifier.confirmSendData(data)) {
            new SendTask().execute(data);
        } else {
            L.e(TAG, "发送失败-数据格式错误 : " + Arrays.toString(data));
        }
    }

    public void send(byte[] head, byte[] body) {
        this.send(new Transfer().packingByte(head, body));
    }

    public void send(ArmHead head, byte[] body) {
        this.send(head.getHead(), body);
    }

    public void send(ArmHead head, byte body) {
        this.send(head, new byte[]{body});
    }

    public void send(ArmHead head, boolean b) {
        this.send(head, (byte) (b ? 0x01 : 0x00));
    }

    //    /**
//     * 通过USB 向ARM发送data
//     * 不能再UI线程使用
//     *
//     * @param data 所发送的data
//     * @return 发送结果: 成功/失败
//     */
    public UsbData sendForResult(byte[] data) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            Log.e(TAG, "send: d当前在UI线程发送");
        }
        if (!mConnectFlag) {
            Log.e(TAG, "send: USB还未连接,发送失败:" + Arrays.toString(data));
            notifyActivities(UsbEvent.UsbDisconnect);
            return new UsbData(UsbEvent.UsbSendFailed, null, null);
        }
        if (mVerifier.confirmSendData(data)) {
//            new SendTask().execute(data);
            try {
                FutureTask<UsbData> sendTask = new FutureTask<>(new SendCallable(data));
                sendExecutor.submit(sendTask);
                while (!sendTask.isDone()) {
                }
                return sendTask.get();
//                return new UsbData(UsbEvent.UsbSendFailed, null, null);
            } catch (Exception e) {
                e.printStackTrace();
                return new UsbData(UsbEvent.UsbSendFailed, null, null);
            }
        } else {
            L.e(TAG, "发送失败-数据格式错误 : " + Arrays.toString(data));
            return new UsbData(UsbEvent.UsbSendFailed, null, null);
        }
    }

    //
//    /**
//     * 创建一个线程来发送byte[],实时监测当前接收值并与之对比HEAD
//     * 返回是否发送成功
//     */
    private class SendCallable implements Callable<UsbData> {
        private UsbData mUsbData;


        public SendCallable(byte[] data) {
            mUsbData = new UsbData();
            mUsbData.setDataToSend(data);
        }

        @Override
        public UsbData call() throws Exception {
            byte[] head = mVerifier.getHead(mUsbData.getDataToSend());
            //当前发送次数
            int sendCount = 0;
            // 重置当前接收的信息
            currentReceive = null;
            try {
                while (sendCount < SEND_REPEAT_COUNT) {
                    sUsbSerialDriver.write(mUsbData.getDataToSend(), 0);
                    sendCount++;
                    Log.i(TAG, "[USB发送]: " + sendCount + "次 ---" + Arrays.toString(mUsbData.getDataToSend()));
                    SystemClock.sleep(SEND_TIMEOUT);
                    // 与当前接收到的 currentReceive比较 头三位( 是否不严谨?)
                    //如果相同 返回发送成功 ,退出 while() 不再发送, 并返回接收到的结果
                    if (mVerifier.compareHead(currentReceive, head)) {
                        mUsbData.setDataReceive(currentReceive);
                        mUsbData.setEvent(UsbEvent.UsbSendSuccess);
                        return mUsbData;
                    }
                    // 如果接收到的是 错误0x50... 退出!
                    if (mVerifier.compareHead(currentReceive, ErrorFromArm)) {
                        mUsbData.setDataReceive(currentReceive);
                        mUsbData.setEvent(UsbEvent.UsbSendFailed);
                        return mUsbData;
                    }
                }
                // 如果没有接收到 回复, 发送失败
                mUsbData.setEvent(UsbEvent.UsbSendFailed);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return mUsbData;
        }
    }


    public void connect() {
        // 先断开连接...
        if (sUsbSerialDriver != null) {
            try {
                sUsbSerialDriver.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!mConnectFlag) {
            Log.i(TAG, "connecting...");
            try {
                sUsbSerialDriver = UsbSerialConnector.getInstance().getCurrentUsbSerialDriver(new UsbSerial_CP2102(), new UsbSerial_PL2303());
            } catch (SecurityException e) {
                T.show("没有USB连接权限!\n请重新插拔USB连接线");
                innerHandler.removeMessages(ACTION_RECONNECT_USB);
                return;
            }

            startIoManager();
            // 开始轮询机器人
            startQueryRobot();
//            innerHandler.sendEmptyMessageDelayed(ACTION_QUERY_STATE, 500);
//            innerHandler.sendEmptyMessageDelayed(ACTION_QUERY_POWER, 1800000);

        }
    }

    public void reconnect() {
        L.i(TAG, "Usb准备重连...");
        mConnectFlag = false;
        stopIoManager();
        connect();
    }

    public void disconnect() {
        instance = null;
        innerHandler.removeMessages(ACTION_RECONNECT_USB);
//        innerHandler.removeMessages(ACTION_QUERY_STATE);
//        innerHandler.removeMessages(ACTION_QUERY_POWER);

        L.e(TAG, "正在断开USB连接");
        stopIoManager();
        stopQueryRobot();
        try {
            if (sUsbSerialDriver != null) {
                sUsbSerialDriver.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        mConnectFlag = false;
        L.i(TAG, "[关闭线程] ARM-USB通信线程");
        //通知 activity
        notifyActivities(UsbEvent.UsbDisconnect);
    }

    class SendTask extends AsyncTask<byte[], Void, byte[]> {
        private byte[] dataToSend;

        protected byte[] doInBackground(byte[]... params) {
            dataToSend = params[0];
            byte[] head = mVerifier.getHead(dataToSend);
            //当前发送次数
            int sendCount = 0;
            // 重置当前接收的信息
            currentReceive = null;
            try {
                while (sendCount < SEND_REPEAT_COUNT) {
                    sUsbSerialDriver.write(dataToSend, 0);
                    sendCount++;
                    L.e(TAG, "[USB发送]: " + sendCount + "次 ---" + Arrays.toString(dataToSend));
                    SystemClock.sleep(SEND_TIMEOUT);
                    // 与当前接收到的 currentReceive比较 头三位( 是否不严谨?)
                    //如果相同 返回发送成功 ,退出 while() 不再发送, 并返回接收到的结果
                    if (mVerifier.compareHead(currentReceive, head)
                            // 如果接收到的是 错误0x50... 退出!
                            || mVerifier.compareHead(currentReceive, ErrorFromArm)) {
                        return currentReceive;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        /**
         * @param receive 接收到的数据
         */
        protected void onPostExecute(byte[] receive) {
            //反馈发送结果,
            //发送失败( 没任何回复就是发送失败),并将 发送的data返回
            if (receive == null || mVerifier.compareHead(currentReceive, ErrorFromArm)) {
                //发送失败( 收到ARM报错的回复)
                notifyActivities(new UsbData(UsbEvent.UsbSendFailed, receive, dataToSend));
            } else {
                //发送成功
                notifyActivities(new UsbData(UsbEvent.UsbSendSuccess, receive, dataToSend));
            }
        }
    }


    /**
     * 判断 buffer是否属于 指定Action的MAP中
     */
    protected boolean belongAction(byte[] buffer, HashMap<String, byte[]> map) {
        for (byte[] head : map.values()) {
            if (mVerifier.compareHead(buffer, head)) {
                return true;
            }
        }
        return false;
    }
}

