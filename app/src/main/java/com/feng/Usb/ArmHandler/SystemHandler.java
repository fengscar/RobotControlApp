package com.feng.Usb.ArmHandler;

import com.feng.Usb.ArmModel.ArmSystem;
import com.feng.Usb.ArmHead;

/**
 * Created by fengscar on 2016/8/1.
 */
public class SystemHandler extends BaseHandler {
    private static final String TAG = "SystemHandler";


    //region Singleton
    public static SystemHandler getInstance() {
        // 这里必须是全局路径 否则无法找到
        return (SystemHandler) instance(SystemHandler.class.getName());
    }

    //endregion
    public enum SystemState {
        NORMAL(0), TIMEOUT(1), WRONG_MODULE(2), WRONG_COMMAND(3), WRONG_DATA_LENGTH(4),
        WRONG_DATA(5), CANT_EXECUTE(6), WRONG_VERIFY(7), HARDWARE_ERROR(8), OTHERS(0xff);
        private int mIndex;

        SystemState(int b) {
            mIndex = b;
        }

        public int getIndex() {
            return mIndex;
        }

        public static SystemState getState(int b) {
            for (SystemState p : SystemState.values()) {
                if (p.mIndex == b) {
                    return p;
                }
            }
            return NORMAL;
        }
    }

    //region Members
    private int mCurrentSoftwareVersion;
    private boolean mUsbConnectStatus;
    private int mRobotID;
    private int mSendPeriod;
    private int mSendCount;
    private SystemState mSystemState;

    public int getCurrentSoftwareVersion() {
        return mCurrentSoftwareVersion;
    }

    public boolean isUsbConnectStatus() {
        return mUsbConnectStatus;
    }

    public int getRobotID() {
        return mRobotID;
    }

    public int getSendPeriod() {
        return mSendPeriod;
    }

    public int getSendCount() {
        return mSendCount;
    }

    public SystemState getSystemState() {
        return mSystemState;
    }
    //endregion

    public void queryUsbConnectStatus() {
        mArmUsbManager.send(ArmSystem.QueryUsbConnectStatus, null);
    }

    public boolean setRobotID(int robotID) {
        return send(ArmSystem.SetRobotID, (byte) robotID).getSendState();
    }

    public boolean setSendPeriod(int sendPeriod) {
        boolean setResult = send(ArmSystem.SetSendPeriod, (byte) sendPeriod).getSendState();
        if (setResult) {
            mSendPeriod = sendPeriod;
        }
        return setResult;
    }

    public boolean setSendCount(int sendCount) {
        boolean setResult = send(ArmSystem.SetSendPeriod, (byte) sendCount).getSendState();
        if (setResult) {
            mSendCount = sendCount;
        }
        return setResult;
    }


    @Override
    public boolean startSelfCheck() {
        mArmUsbManager.send(ArmSystem.SetStartSelfCheck, (byte) 0x01);
        return true;
    }

    public void stopSelfCheck() {
        mArmUsbManager.send(ArmSystem.SetStartSelfCheck, (byte) 0x00);
    }


    @Override
    public void onReceive(byte[] fromUsbData) {
        byte[] body = mTransfer.getBody(fromUsbData);
        if (body == null) {
            return;
        }
        switch (fromUsbData[ArmHead.COMMAND]) {
            case 0x01:
                mCurrentSoftwareVersion = body[0];
                break;
            case 0x10:
                mSystemState = SystemState.getState(body[0]);
                break;
            case 0x50:

                break;
        }
    }


}
