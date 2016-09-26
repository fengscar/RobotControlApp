package com.feng.Usb;

import com.feng.Usb.ArmHandler.*;

/**
 * Created by fengscar on 2016/7/30.
 */
public class HandlerFinder {
    // 根据byte信息获取具体的handler
    public static ArmDataReceiver getHandler(byte[] fromUsbData) {
        if (fromUsbData == null || fromUsbData.length < 5) {
            return null;
        }
        switch (fromUsbData[ArmProtocol.MODULE]) {
            case 0x01:
                return MotionHandler.getInstance();
            case 0x02:
                return PathHandler.getInstance();
            case 0x03:
                return BarrierHandler.getInstance();
            case 0x04:
                return RFIDHandler.getInstance();
            case 0x05:
                return MagHandler.getInstance();
            case 0x06:
                return UltrasoundHandler.getInstance();
            case 0x07:
                return PowerHandler.getInstance();
            case 0x08:
                return PIRHandler.getInstance();
            case 0x0b:
                return ButtonHandler.getInstance();
            case 0x50:
                return SystemHandler.getInstance();
        }
        return null;
    }
}
