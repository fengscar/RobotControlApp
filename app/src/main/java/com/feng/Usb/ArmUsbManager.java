package com.feng.Usb;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import com.feng.Constant.ArmProtocol;
import com.feng.Constant.I_Parameters;
import com.feng.RobotApplication;
import com.feng.Utils.L;
import com.feng.Utils.Transfer;
import com.feng.Utils.Verifier;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2016-2-29 下午4:46:11
 */

public class ArmUsbManager implements ArmProtocol, I_Parameters {
    private final static String TAG = ArmUsbManager.class.getSimpleName();

    protected ArmUsbManager() {
    }

    // 用来获取 USB设备列表
    private UsbManager mUsbManager;
    // ARM USB驱动对象,用来收发数据
    private static UsbSerialDriver sDriver;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    // 设备实体类
    class DeviceEntry {
        public UsbDevice device;
        public UsbSerialDriver driver;

        DeviceEntry(UsbDevice device, UsbSerialDriver driver) {
            this.device = device;
            this.driver = driver;
        }
    }

    private DeviceEntry mEntry;

    protected Map<String, Handler> activityHandlerMap = new HashMap<>();

    /**
     * 添加观察者, 当系统状态改变时/接受数据时 ,通知该系统的handler,让其发送数据
     */
    public void addObserver(String activityName, Handler activityMessagehandler) {
        activityHandlerMap.put(activityName, activityMessagehandler);
        L.i(TAG, "添加观察者: " + activityName + ",当前有 " + activityHandlerMap.size() + " 个观察者");
    }

    public void delObserver(String activityKey) {
        activityHandlerMap.remove(activityKey);
        L.i(TAG, "删除观察者: " + activityKey + ",当前有 " + activityHandlerMap.size() + " 个观察者");
    }

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
    private boolean CONNECT_FLAG = false;

    public boolean getConnectState() {
        return CONNECT_FLAG;
    }

    //内部的handler ,用来处理重连等问题
    private Handler innerHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REFRESH_USB:
                    reconnect();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 获取 驱动 并开始回调接收
     *
     * @return
     */
    private boolean initDriver() {
        L.i(TAG, "USB initDriver...");
        sDriver = mEntry.driver;
        try {
            sDriver.open();
            sDriver.setParameters(115200, 8, UsbSerialDriver.STOPBITS_1, UsbSerialDriver.PARITY_NONE);
        } catch (IOException e) {
            try {
                sDriver.close();
            } catch (IOException e2) {
                return false;
            }
            sDriver = null;
            return false;
        }
        startIoManager();
        return true;
    }

    private void startIoManager() {
        if (sDriver != null) {
            L.i(TAG, "Starting io manager...");
            mSerialIoManager = new SerialInputOutputManager(sDriver, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            L.e(TAG, "Stopping io manager...");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }


    public void reply(byte[] data, boolean isSuccess) {
        // 获取 0 1 2 作为头, 并将 data 设置为 成功 , 打包后 发送出去;
        byte[] dataToSend = new Transfer().packingByte(
                new byte[]{data[0], data[1], data[2]},
                isSuccess ? new byte[]{(byte) 0x01} : new byte[]{(byte) 0x00});
        try {
            sDriver.write(dataToSend, 0);
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

    public void connect() {
        if (!CONNECT_FLAG) {
            L.i(TAG, "开始连接USB");

            mUsbManager = (UsbManager) RobotApplication.getContext().getSystemService(Context.USB_SERVICE);

            new GetUsbTask().execute((Void) null);
        }
    }

    public void reconnect() {
        L.i(TAG, "Usb准备重连...");
        CONNECT_FLAG = false;
        stopIoManager();
        connect();
    }


    public void disconnect() {
        innerHandler.removeMessages(REFRESH_USB);
        L.e(TAG, "正在断开USB连接");
        stopIoManager();
        try {
            //移除队列中的 重连信息
            if (mEntry == null) {
                L.i(TAG, "mEntry为空");
                return;
            }
            if (mEntry.driver != null) {
                mEntry.driver.close();
            }
            if (mEntry.device != null) {
                mEntry.device = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        CONNECT_FLAG = false;
        L.i(TAG, "[关闭线程] ARM-USB通信线程");
        //通知 activity
        notifyActivities(UsbEvent.UsbDisconnect);
    }

    public void send(byte[] data) {
        if (!CONNECT_FLAG) {
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

    /**
     * 启动一个异步任务,来连接USB.
     * 连接成功会启动一个新线程来接收USB数据,并通过回调接口(SerialInputOutputManager.Listener)来接收数据
     * <p/>
     * --在doInBackground()中
     * 1. 查找USB设备
     * 2. 获取该设备驱动
     * <p/>
     * --在onPostExecute中
     * 3. 配置该USB驱动信息,开始回调 ; (如果驱动配置错误,则通过handler延迟n秒后重连)
     */
    class GetUsbTask extends AsyncTask<Void, Void, DeviceEntry> {
        @Override
        protected DeviceEntry doInBackground(Void... params) {
            //			SystemClock.sleep(1000);
            DeviceEntry result = new DeviceEntry(null, null);
            try {
                L.i(TAG, "当前的USB设备有" + mUsbManager.getDeviceList().size() + "个");
                for (final UsbDevice device : mUsbManager.getDeviceList().values()) {
                    L.i(TAG, "找到USB设备: " + device.toString());
                    final List<UsbSerialDriver> drivers =
                            UsbSerialProber.probeSingleDevice(mUsbManager, device);
                    if (drivers.isEmpty()) {
                        result = new DeviceEntry(device, null);
                    } else {
                        for (UsbSerialDriver driver : drivers) {
                            result = new DeviceEntry(device, driver);
                        }
                    }
                    //如果找到CP2012 后, 停止搜索其他设备...因为找到其他设备后result会被替换..
                    if ((device.getVendorId() == 4292 && device.getProductId() == 60000)
                            || (device.getVendorId() == 1659 && device.getProductId() == 8963)) {
                        L.i(TAG, "找到USB转串口设备及驱动,停止查找并退出...");
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(DeviceEntry result) {
            mEntry = result;
            if (mEntry.device != null && mEntry.driver != null) {
                if (initDriver() == true) {
                    CONNECT_FLAG = true;
                    L.i(TAG, "(＾－＾) USB设备连接成功,驱动配置成功");
                    notifyActivities(new UsbData(UsbEvent.UsbConnect));
                }
            } else {
                CONNECT_FLAG = false;
                L.e(TAG, mEntry.device == null ? "USB设备未找到" : "USB驱动未找到");
                L.e(TAG, USB_CONNECT_TIMEOUT / 1000 + "秒后重连...");
                notifyActivities(new UsbData(UsbEvent.UsbConnectFailed));
                innerHandler.sendEmptyMessageDelayed(REFRESH_USB, USB_CONNECT_TIMEOUT);
            }
        }
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
                    sDriver.write(dataToSend, 0);
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
}

