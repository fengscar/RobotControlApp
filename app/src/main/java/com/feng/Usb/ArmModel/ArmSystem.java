package com.feng.Usb.ArmModel;

import com.feng.Usb.ArmHead;

/**
 * Created by fengscar on 2016/7/19.
 */
public enum ArmSystem implements ArmHead {
    QuerySoftwareVersion(1), QueryUsbConnectStatus(2),
    SetRobotID(3), SetSendPeriod(4), SetSendCount(5),
    SetReset(6), SetStartSelfCheck(7),
    SetArmReboot(8),// 控制机器人重启
    SysError(16),
    ArmReboot(80); //上报机器人重启..
    private final int index;

    ArmSystem(int b) {
        index = b;
    }

    @Override
    public ArmHead get(byte commandIndex) {
        for (ArmSystem p : ArmSystem.values()) {
            if (p.index == commandIndex) {
                return p;
            }
        }
        return null;
    }

    @Override
    public byte[] getHead() {
        return new byte[]{ROBOT, SYSTEM, (byte) this.index};
    }

    @Override
    public byte getModule() {
        return SYSTEM;
    }

    @Override
    public byte getCommand() {
        return (byte) index;
    }
}
