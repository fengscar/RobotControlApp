package com.feng.UsbData;

/**
 * Created by fengscar on 2016/7/19.
 */
public enum ModelMotion implements UsbHead {
    QueryMotionState(1), SetMovingState(2), SetMoving(3), SetTurn(4),

    ArmMotionOriginTurnSpeed(5), ArmMotionStartSpeed(6), ArmMotionIncrementSpeed(7), ArmMotionMaxSpeed(8),
    ArmMotionTurnSpeed(9), ArmMotionUltraSpeed(10), ArmMotionReadCardSpeed(11),

    SelfCheckMotion90(12), SelfCheckMotion180(13), MoveDistanceOverflow(14), LongTimeNotOperate(15);

    private int index;

    ModelMotion(int b) {
        index = b;
    }

    @Override
    public byte[] value() {
        return new byte[]{ROBOT, MOTION, (byte) this.index};
    }

    @Override
    public UsbHead get(byte index) {
        for (ModelMotion m : ModelMotion.values()) {
            if ((byte) m.index == index) {
                return m;
            }
        }
        return null;
    }
}
