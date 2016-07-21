package com.feng.UsbData;

/**
 * Created by fengscar on 2016/7/19.
 */
public class UsbProtocal implements UsbHead {
    private final static String TAG = UsbProtocal.class.getSimpleName();

    // 使用接口聚合 enum...


    @Override
    public byte[] value() {
        return new byte[0];
    }

    @Override
    public UsbHead get(byte commandIndex) {
        return null;
    }


    //路径算法模块的协议
    public enum PathModel {
        QueryMotionState(1), SetMovingState(2), SetMoving(3), SetTurn(4),

        ArmMotionOriginTurnSpeed(5), ArmMotionStartSpeed(6), ArmMotionIncrementSpeed(7), ArmMotionMaxSpeed(8),
        ArmMotionTurnSpeed(9), ArmMotionUltraSpeed(10), ArmMotionReadCardSpeed(11),

        SelfCheckMotion90(12), SelfCheckMotion180(13), MoveDistanceOverflow(14), LongTimeNotOperate(15);

        private int index;

        PathModel(int b) {
            index = b;
        }

        byte[] value() {
            return new byte[]{ROBOT, PATH, (byte) this.index};
        }

        static PathModel getValue(byte b) {
            for (PathModel p : PathModel.values()) {
                if (p.index == b) {
                    return p;
                }
            }
            return null;
        }
    }

    public void operate() {
        PathModel.ArmMotionIncrementSpeed.value();
        PathModel em = PathModel.ArmMotionIncrementSpeed;
        switch (em) {
            case ArmMotionIncrementSpeed:

                break;
        }
    }

}
