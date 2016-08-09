package com.feng.Usb.ArmModel;

import com.feng.Usb.ArmHead;

/**
 * Created by fengscar on 2016/7/19.
 */
public enum ArmRFID implements ArmHead {
    SetStartSelfCheck(1), CurrentCard(2), MissingCard(3), WrongCard(4);
    private final int index;

    ArmRFID(int b) {
        index = b;
    }


    @Override
    public ArmHead get(byte commandIndex) {
        for (ArmRFID p : ArmRFID.values()) {
            if (p.index == commandIndex) {
                return p;
            }
        }
        return null;
    }

    @Override
    public byte[] getHead() {
        return new byte[]{ROBOT, RFID, (byte) this.index};
    }

    @Override
    public byte getModule() {
        return RFID;
    }

    @Override
    public byte getCommand() {
        return (byte) index;
    }
}
