package com.feng.Usb.ArmModel;

import com.feng.Usb.ArmHead;

/**
 * Created by fengscar on 2016/7/19.
 */
public enum ArmMotion implements ArmHead {
    QueryMotionState(1), SetExecuteMove(2), SetMove(3), SetTurn(4),

    ArmMotionOriginTurnSpeed(5), ArmMotionStartSpeed(6), ArmMotionIncrementSpeed(7), ArmMotionMaxSpeed(8),
    ArmMotionTurnSpeed(9), ArmMotionUltraSpeed(10), ArmMotionReadCardSpeed(11),

    SelfCheckMotion90(12), SelfCheckMotion180(13),

    MoveDistanceOverflow(14), LongTimeNotOperate(15),

    DoorControl(17), ScheduleControl(17);

    private int index;

    ArmMotion(int b) {
        index = b;
    }

    @Override
    public byte[] getHead() {
        return new byte[]{ROBOT, MOTION, (byte) this.index};
    }

    @Override
    public byte getModule() {
        return MOTION;
    }

    @Override
    public byte getCommand() {
        return (byte) index;
    }

    @Override
    public ArmHead get(byte index) {
        for (ArmMotion m : ArmMotion.values()) {
            if ((byte) m.index == index) {
                return m;
            }
        }
        return null;
    }

}
