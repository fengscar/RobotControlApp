package com.feng.Usb.ArmHandler;

import android.util.Log;
import com.feng.Usb.ArmModel.ArmUltrasound;
import com.feng.Usb.ArmHead;

/**
 * Created by fengscar on 2016/8/1.
 */
public class UltrasoundHandler extends BaseHandler {
    private static final String TAG = "UltrasoundHandler";

    //region Singleton
    public static UltrasoundHandler getInstance() {
        // 这里必须是全局路径 否则无法找到
        return (UltrasoundHandler) instance(UltrasoundHandler.class.getName());
    }
    //endregion

    //region Members
    private int mCurrentWarningDistace;
    private boolean isUltrasoundWarning;
    private int mCurrentCheckDistance;// 自检中的距离

    public int getCurrentWarningDistace() {
        return mCurrentWarningDistace;
    }

    public boolean isUltrasoundWarning() {
        return isUltrasoundWarning;
    }

    public int getCurrentCheckDistance() {
        return mCurrentCheckDistance;
    }

    //endregion

    public boolean setWarningDistance(int distance) {
        if (distance > 50 || distance < 10) {
            Log.e(TAG, "setWarningDistance: 数值不再10-50之间");
            return false;
        }
        return send(ArmUltrasound.SetWarningDistance, (byte) distance).getSendState();
    }

    @Override
    public boolean startSelfCheck() {
        isSelfCheck = send(ArmUltrasound.SetStartSelfCheck, null).getSendState();
        return isSelfCheck;
    }


    @Override
    public void onReceive(byte[] fromUsbData) {
        byte[] body = mTransfer.getBody(fromUsbData);
        if (body == null) {
            return;
        }
        switch (fromUsbData[ArmHead.COMMAND]) {
            case 0x03:
                if (!isSelfCheck) {
                    isUltrasoundWarning = body[0] == 0x01;
                } else {
                    mCurrentCheckDistance = (int) body[0];
                }
                reply(fromUsbData);
                break;
        }
    }


}
