package com.feng.Usb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.util.Log;
import com.khdz.netpatrol.Usb.ReaderType.WirelessPatrolReader;

/**
 * 当用户 授权USB权限时,将发送广播:Action==GET_USER_PERMISSION
 * 同时监听 USB设备拔出 的广播
 * 尝试重新连接USB
 */
public class GetUsbReceiver extends BroadcastReceiver {
    private static final String TAG = "GetUsbReceiver";

    // 监听的广播 Filter ( USB连接得到用户权限)
    public static final String GET_USER_PERMISSION = "com.khdz.get_user_permission";
    public static final int GET_USB_REQUEST_CODE = 250;


    public GetUsbReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (GET_USER_PERMISSION.equals(intent.getAction())) {
            Log.i(TAG, "Application get user permission.");
            SerialManager.getInstance().connect(new WirelessPatrolReader(), false);
        }
        if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(intent.getAction())) {
            Log.i(TAG, "Usb device detached");
        }
    }
}
