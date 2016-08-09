package com.feng.Usb.UsbSerial;

/**
 * Created by fengscar on 2016/8/1.
 */
public class UsbSerial_PL2303 extends UsbSerialDevice {

    @Override
    int getVID() {
        return 1659;
    }

    @Override
    int getPID() {
        return 8963;
    }
}
