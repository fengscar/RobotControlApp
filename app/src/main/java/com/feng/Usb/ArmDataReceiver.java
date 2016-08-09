package com.feng.Usb;

/**
 * Created by fengscar on 2016/7/30.
 */
public interface ArmDataReceiver {

    void onReceive(byte[] fromUsbData);

}
