/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2016-1-25 下午3:31:23
 */
package com.feng.Base;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.Point;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import com.feng.CustomView.WarningDialog;
import com.feng.RobotApplication;
import com.feng.Schedule.ScheduleProtocal;
import com.feng.Usb.ArmProtocol;
import com.feng.Usb.UsbData;
import com.feng.Usb.UsbEvent;
import com.feng.Utils.IntentDealer;
import com.feng.Utils.L;
import com.feng.Utils.Transfer;
import com.sdsmdg.tastytoast.TastyToast;

import java.lang.ref.WeakReference;

public class BaseActivity extends Activity implements ArmProtocol, ScheduleProtocal {
    private final static String TAG = "BaseActivity";

    private BroadcastReceiver receiver;
    private IntentFilter filter;
    protected WarningDialog warningDialog;

    protected IntentDealer intentDealer;
    protected Transfer transfer;


    public static class BaseUsbHandler extends Handler {
        private WeakReference<BaseActivity> activityWeakReference;

        public BaseUsbHandler(BaseActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseActivity activity = activityWeakReference.get();
            if (activity != null) {
                if (msg.obj == null) {
                    return;
                }
                UsbData data = (UsbData) msg.obj;
                UsbEvent event = data.getEvent();
                byte[] dataReceive = data.getDataReceive();
                byte[] dataSend = data.getDataToSend();

                String action = new Transfer().getAction(dataReceive);
                if (action == null) {
                    return;
                }
                Log.i(TAG, "handleMessage:  " + action);

                switch (action) {
                    case BARRIER_WARNING:
                    case LONG_TIME_NOT_OPERATE:
                    case MOVE_DISTANCE_OVERFLOW:
                    case MISSING_RFID:
                    case WRONG_RFID:
                    case MISSING_MAGNETIC:
                    case ULTRASONIC_WARNING:
                    case INFRARED_WARNING:
                        if (dataReceive[DATA] == 0x01) {
                            activity.warningDialog.addWarning(false, action, action, null);
                        } else {
                            activity.warningDialog.removeWarning(INFRARED_WARNING);
                        }
                        break;
                    case ERROR_FROM_ARM:
                        switch (dataReceive[DATA]) {
                            case 0x01:
                                L.e("接收超时错误");
                                break;
                            case 0x02:
                                L.e("模块编号错误");
                                break;
                            case 0x03:
                                L.e("命令编号错误");
                                break;
                            case 0x04:
                                L.e("数据长度错误");
                                break;
                            case 0x05:
                                L.e("数据错误");
                                break;
                            case 0x06:
                                IntentDealer.sendTtsBroadcast("我还不能这么做");
                                TastyToast.makeText(RobotApplication.getContext(), "我还不能这么做", TastyToast.LENGTH_SHORT, TastyToast.WARNING);
                                break;
                            case 0x07:
                                L.e("校验位错误");
                                break;
                            case 0x08:
                                L.e("进入硬件中断错误");
                                break;
                            default:
                                break;
                        }
                        break;
                }
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        warningDialog = new WarningDialog(RobotApplication.getContext());
        transfer = new Transfer();
        intentDealer = new IntentDealer(transfer);
    }


    /**
     * 如果需要自定义 广播接收器 , 重写该函数就行
     * 在 onstart()之前调用就行
     */
    protected void configureReceiver(BroadcastReceiver rec) {
        if (rec != null) {
            receiver = rec;
        }
    }

    private void init() {
        if (filter == null) {
            filter = new IntentFilter();
        }
        // SEND  -Actions
        for (String action : SEND_ACTIONS.keySet()) {
            filter.addAction(action);
//			L.i("添加了Action :  "+action);
        }
        // USB -actions
        for (String action : USB_ACTIONS) {
            filter.addAction(action);
//			L.i("添加了Action :  "+action);
        }
        // RECEIVE  -Actions
        for (String action : RECEIVE_ACTIONS.keySet()) {
            filter.addAction(action);
//			L.i("添加了Action :  "+action);
        }
        // Local -Actions
        for (String action : ArmProtocol.LOCAL_ACTIONS) {
            filter.addAction(action);
//			L.i("添加了Action :  "+action);
        }
        if (receiver == null) {
            receiver = new BaseReceiver(warningDialog);
        }

        // USB设备接入
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        // USB设备拔出
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);

        registerReceiver(receiver, filter);
        L.e(TAG, "BaseActivity ->" + this + "注册 Receiver");
    }

    @Override
    protected void onStart() {
        super.onStart();
        init();
    }

    @Override
    protected void onStop() {
        super.onStop();
        L.e(TAG, "BaseActivity ->" + this + " 注销 Receiver");
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
    }

    public Point getScreenSize() {
        Display display = getWindowManager().getDefaultDisplay(); //Activity#getWindowManager()
        Point size = new Point();
        display.getSize(size);
        return size;
    }
}

