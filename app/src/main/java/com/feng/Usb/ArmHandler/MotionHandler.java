package com.feng.Usb.ArmHandler;

import android.util.Log;
import com.feng.Schedule.ScheduleRobot;
import com.feng.Usb.ArmHead;
import com.feng.Usb.ArmModel.ArmMotion;

/**
 * Created by fengscar on 2016/8/1.
 */
public class MotionHandler extends BaseHandler {
    private static final String TAG = "MotionHandler";

    //region Singleton
    public static MotionHandler getInstance() {
        // 这里必须是全局路径 否则无法找到
        return (MotionHandler) instance(MotionHandler.class.getName());
    }
    //endregion

    //region Members

    private ScheduleRobot.MotionState mMotionState;
    private int mSpeed;
    private int mMaxSpeed;
    private int mDistance;
    private boolean mScheduleMove; // true: 调度控制运动 ; false: 调度命令停止
    private boolean mMoveDistanceOverflow; //true: 移动距离异常-报警!
    private boolean mLongTimeNotOperate; //true: 长时间无人操作!
    //endregion

    //region Getter
    public ScheduleRobot.MotionState getMotionState() {
        return mMotionState;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public int getMaxSpeed() {
        return mMaxSpeed;
    }

    public int getDistance() {
        return mDistance;
    }

    public boolean isScheduleMove() {
        return mScheduleMove;
    }

    public boolean isMoveDistanceOverflow() {
        return mMoveDistanceOverflow;
    }

    public boolean isLongTimeNotOperate() {
        return mLongTimeNotOperate;
    }

    //endregion

    @Override
    public void onReceive(byte[] fromUsbData) {
        byte[] body = mTransfer.getBody(fromUsbData);
        if (body == null) {
            return;
        }
        switch (fromUsbData[ArmHead.COMMAND]) {
            case 0x01:
                queryStateResult(body);
                // 更新RobotEntity状态
                ScheduleRobot robot = ScheduleRobot.getInstance();
                robot.beginNotifyChange();
                robot.setMotionState(mMotionState);
                robot.setSpeed(mSpeed);
                robot.setScheduleMove(mScheduleMove);
                robot.endNotifyChange();

                break;
            case 0x0e:
                mMoveDistanceOverflow = body[0] == 0x01;
                reply(fromUsbData);
                break;
            case 0x0f:
                mLongTimeNotOperate = body[0] == 0x01;
                reply(fromUsbData);
                break;
        }
    }

    //region 主动发送命令的方法

    /**
     * 查询状态
     * 如果发送成功.返回true.
     */
    public void queryMotionState() {
        mArmUsbManager.send(ArmMotion.QueryMotionState, null);
    }

    /**
     * 设置机器人执行运动/停止运动
     *
     * @param isMove true: 设置运动 ;  false: 设置为停止
     */
    public void setExecuteMove(boolean isMove) {
        mArmUsbManager.send(ArmMotion.SetExecuteMove, isMove);
    }

    /**
     * 设置机器人运动
     *
     * @param moveState ( 0x00 停止 , 0x01 前进, 0x02 : 后退)
     */
    public void setMove(int moveState) {
        send(ArmMotion.SetMove, (byte) moveState);
    }

    /**
     * 设置目的地是否转向
     *
     * @param isTurn true则转向
     */
    public boolean setTurn(boolean isTurn) {
        return send(ArmMotion.SetTurn, isTurn).getSendState();
    }

    /**
     * 设置原地转向速度
     *
     * @param speed 80-250..转成2个byte 默认130
     */
    public boolean setArmMotionOriginTurnSpeed(int speed) {
        return send(ArmMotion.ArmMotionOriginTurnSpeed, mTransfer.intTo2Byte(speed)).getSendState();
    }

    /**
     * 设置启动速度
     *
     * @param speed 50-100
     */
    public boolean setArmMotionStartSpeed(int speed) {
        return send(ArmMotion.ArmMotionStartSpeed, mTransfer.intTo2Byte(speed)).getSendState();
    }

    /**
     * 设置速度增量
     *
     * @param speed
     */
    public boolean setArmMotionIncrementSpeed(int speed) {
        return send(ArmMotion.ArmMotionIncrementSpeed, mTransfer.intTo2Byte(speed)).getSendState();
    }

    /**
     * 设置最大速度
     *
     * @param speed
     */
    public boolean setArmMotionMaxSpeed(int speed) {
        return send(ArmMotion.ArmMotionMaxSpeed, mTransfer.intTo2Byte(speed)).getSendState();
    }

    /**
     * 设置转弯速度
     *
     * @param speed
     */
    public boolean setArmMotionTurnSpeed(int speed) {

        return send(ArmMotion.ArmMotionTurnSpeed, mTransfer.intTo2Byte(speed)).getSendState();
    }

    /**
     * 设置超声感应速度
     *
     * @param speed
     */
    public boolean setArmMotionUltraSpeed(int speed) {
        return send(ArmMotion.ArmMotionUltraSpeed, mTransfer.intTo2Byte(speed)).getSendState();
    }

    /**
     * 设置读卡速度
     */
    public boolean setArmMotionReadCardSpeed(int speed) {
        return send(ArmMotion.ArmMotionReadCardSpeed, mTransfer.intTo2Byte(speed)).getSendState();
    }

    /**
     * 开始90度自检
     */
    public void setSelfCheckMotion90() {
        mArmUsbManager.send(ArmMotion.SelfCheckMotion90, null);
    }

    /**
     * 开始180度自检
     */
    public void setSelfCheckMotion180() {
        mArmUsbManager.send(ArmMotion.SelfCheckMotion180, null);
    }

    /**
     * 操作机器人门控
     *
     * @param isClose true:关闭机器人的前门
     */
    public boolean setDoorControl(boolean isClose) {
        return send(ArmMotion.DoorControl, isClose).getSendState();
    }

    /**
     * 设置调度系统控制状态
     *
     * @param isMove true:调度系统命令运动
     */
    public void setScheduleControl(boolean isMove) {
        send(ArmMotion.ScheduleControl, isMove);
    }


    //endregion

    // 更新当前状态
    private void queryStateResult(byte[] body) {
        if (body == null || body.length != 8) {
            Log.e(TAG, "queryState: 接收到的信息错误!");
            return;
        }
        this.mMotionState = ScheduleRobot.MotionState.getState(body[0]);
        this.mSpeed = mTransfer.twoByteToInt(new byte[]{body[1], body[2]});
        this.mDistance = mTransfer.twoByteToInt(new byte[]{body[3], body[4]});
        this.mMaxSpeed = mTransfer.twoByteToInt(new byte[]{body[5], body[6]});
        this.mScheduleMove = body[7] == 0x01;

    }

}
