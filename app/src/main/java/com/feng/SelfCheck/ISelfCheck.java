package com.feng.SelfCheck;

import com.feng.Usb.UsbData;

/**
 * Created by fengscar on 2016/8/9.
 */
public interface ISelfCheck {
    void onReceiveArmData(UsbData usbData);

    boolean startSelfCheck();
}
