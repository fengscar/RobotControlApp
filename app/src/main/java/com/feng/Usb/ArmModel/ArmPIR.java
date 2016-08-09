package com.feng.Usb.ArmModel;

import com.feng.Usb.ArmHead;

/**
 * Created by fengscar on 2016/7/19.
 * 人体红外  (PIR : 被动式红外)
 */
public enum ArmPIR implements ArmHead {
    SetStartSelfCheck(1), PirWarning(2);
    private final int index;

    ArmPIR(int b) {
        index = b;
    }


    @Override
    public ArmHead get(byte commandIndex) {
        for (ArmPIR p : ArmPIR.values()) {
            if (p.index == commandIndex) {
                return p;
            }
        }
        return null;
    }

    @Override
    public byte[] getHead() {
        return new byte[]{ROBOT, PIR, (byte) this.index};
    }

    @Override
    public byte getModule() {
        return PIR;
    }

    @Override
    public byte getCommand() {
        return (byte) index;
    }
}
