package com.feng.Usb.ArmHandler;

import com.feng.Schedule.ScheduleRobot;
import com.feng.Usb.ArmModel.ArmPower;
import com.feng.Usb.ArmModel.ArmRFID;
import com.feng.Usb.ArmHead;

/**
 * Created by fengscar on 2016/8/1.
 */
public class PowerHandler extends BaseHandler {
    private static final String TAG = "PIRHandler";

    //region Singleton
    public static PowerHandler getInstance() {
        // 这里必须是全局路径 否则无法找到
        return (PowerHandler) instance(PowerHandler.class.getName());
    }

    //endregion
    public enum PowerState {
        NOT_CHARGE(0x01), CHARGING(0x02), CHARGE_WARN(0x03), CHARGE_ERROR(0x04);
        private int mIndex;

        PowerState(int b) {
            mIndex = b;
        }

        public int getIndex() {
            return mIndex;
        }

        public static PowerState getState(int b) {
            for (PowerState p : PowerState.values()) {
                if (p.mIndex == b) {
                    return p;
                }
            }
            return NOT_CHARGE;
        }
    }

    //region Members
    private PowerState mPowerState;
    private int mCurrentPower;
    private byte[] mPowerRecord;  //TODO 电量记录

    public PowerState getPowerState() {
        return mPowerState;
    }

    public int getCurrentPower() {
        return mCurrentPower;
    }

    public byte[] getPowerRecord() {
        return mPowerRecord;
    }

    //endregion

    @Override
    public boolean startSelfCheck() {
        isSelfCheck = send(ArmRFID.SetStartSelfCheck, null).getSendState();
        return isSelfCheck;
    }

    public void queryCurrentPower() {
        mArmUsbManager.send(ArmPower.QueryPower, null);
    }

    //TODO
    public boolean queryPowerRecord() {
        return send(ArmPower.QueryPowerRecord, null).getSendState();
    }

    @Override
    public void onReceive(byte[] fromUsbData) {
        byte[] body = mTransfer.getBody(fromUsbData);
        if (body == null) {
            return;
        }
        switch (fromUsbData[ArmHead.COMMAND]) {
            case 0x01:
                mCurrentPower = body[0];

                ScheduleRobot scheduleRobot = ScheduleRobot.getInstance();
                scheduleRobot.beginNotifyChange();
                scheduleRobot.setPower(mCurrentPower);
                scheduleRobot.endNotifyChange();

                break;

            case 0x02:
                mPowerRecord = body;
            case 0x03:
                mPowerState = PowerState.getState(body[0]);
                reply(fromUsbData);
                break;
        }
    }
}
