package com.feng.UsbData;

/**
 * Created by fengscar on 2016/7/19.
 */
public enum ModelPath implements UsbHead {
    QueryMotionState(1), SetMovingState(2), SetMoving(3), SetTurn(4),

    ArmMotionOriginTurnSpeed(5), ArmMotionStartSpeed(6), ArmMotionIncrementSpeed(7), ArmMotionMaxSpeed(8),
    ArmMotionTurnSpeed(9), ArmMotionUltraSpeed(10), ArmMotionReadCardSpeed(11),

    SelfCheckMotion90(12), SelfCheckMotion180(13), MoveDistanceOverflow(14), LongTimeNotOperate(15);

    private final int index;

    ModelPath(int b) {
        index = b;
    }

    public byte[] value() {
        return new byte[]{ROBOT, PATH, (byte) this.index};
    }

    public ModelPath get(byte b) {
        for (ModelPath p : ModelPath.values()) {
            if (p.index == b) {
                return p;
            }
        }
        return null;
    }
}
