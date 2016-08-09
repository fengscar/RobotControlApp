package com.feng.Usb.ArmModel;

import com.feng.Usb.ArmHead;

/**
 * Created by fengscar on 2016/7/19.
 */
public enum ArmMagnetic implements ArmHead {
    QueryMagPosition(1), SetMaxMag(2), SetStartSelfCheck(3), MissingMag(4), CurrentMagPosition(5);
    private final int index;

    ArmMagnetic(int b) {
        index = b;
    }


    @Override
    public ArmHead get(byte commandIndex) {
        for (ArmMagnetic p : ArmMagnetic.values()) {
            if (p.index == commandIndex) {
                return p;
            }
        }
        return null;
    }

    @Override
    public byte[] getHead() {
        return new byte[]{ROBOT, MAGNETIC, (byte) this.index};
    }

    @Override
    public byte getModule() {
        return MAGNETIC;
    }

    @Override
    public byte getCommand() {
        return (byte) index;
    }
}
