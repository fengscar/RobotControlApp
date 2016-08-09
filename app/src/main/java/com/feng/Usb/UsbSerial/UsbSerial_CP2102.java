package com.feng.Usb.UsbSerial;

/**
 * Created by fengscar on 2016/8/1.
 */
public class UsbSerial_CP2102 extends UsbSerialDevice {

    @Override
    int getVID() {
        return 4292;
    }

    @Override
    int getPID() {
        return 60000;
    }
}
