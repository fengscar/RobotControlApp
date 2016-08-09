package com.feng.Usb.ArmModel;

import com.feng.Usb.ArmHead;

/**
 * Created by fengscar on 2016/7/19.
 */
public enum ArmUltrasound implements ArmHead {
    SetWarningDistance(1), SetStartSelfCheck(2), UltraSoundWarning(3);
    private final int index;

    ArmUltrasound(int b) {
        index = b;
    }


    @Override
    public ArmHead get(byte commandIndex) {
        for (ArmUltrasound p : ArmUltrasound.values()) {
            if (p.index == commandIndex) {
                return p;
            }
        }
        return null;
    }

    @Override
    public byte[] getHead() {
        return new byte[]{ROBOT, ULTRASOUND, (byte) this.index};
    }

    @Override
    public byte getModule() {
        return ULTRASOUND;
    }

    @Override
    public byte getCommand() {
        return (byte) index;
    }
}
