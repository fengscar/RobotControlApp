package com.feng.Usb.ArmModel;

import com.feng.Usb.ArmHead;

/**
 * Created by fengscar on 2016/7/19.
 */
public enum ArmPower implements ArmHead {
    QueryPower(1), QueryPowerRecord(2), ChargeStatus(3);
    private final int index;

    ArmPower(int b) {
        index = b;
    }


    @Override
    public ArmHead get(byte commandIndex) {
        for (ArmPower p : ArmPower.values()) {
            if (p.index == commandIndex) {
                return p;
            }
        }
        return null;
    }

    @Override
    public byte[] getHead() {
        return new byte[]{ROBOT, POWER, (byte) this.index};
    }

    @Override
    public byte getModule() {
        return POWER;
    }

    @Override
    public byte getCommand() {
        return (byte) index;
    }
}
