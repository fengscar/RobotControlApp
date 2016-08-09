package com.feng.Usb.ArmModel;

import com.feng.Usb.ArmHead;

/**
 * Created by fengscar on 2016/7/19.
 */
public enum ArmButton implements ArmHead {
    SetStartSelfCheck(1), BarrierWarning(2);
    private final int index;

    ArmButton(int b) {
        index = b;
    }


    @Override
    public ArmHead get(byte commandIndex) {
        for (ArmButton p : ArmButton.values()) {
            if (p.index == commandIndex) {
                return p;
            }
        }
        return null;
    }

    @Override
    public byte[] getHead() {
        return new byte[]{ROBOT, BUTTON, (byte) this.index};
    }

    @Override
    public byte getModule() {
        return BUTTON;
    }

    @Override
    public byte getCommand() {
        return (byte) index;
    }
}
