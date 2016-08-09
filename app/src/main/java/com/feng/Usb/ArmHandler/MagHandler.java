package com.feng.Usb.ArmHandler;

import android.util.Log;
import com.feng.Usb.ArmModel.ArmMagnetic;
import com.feng.Usb.ArmHead;

/**
 * Created by fengscar on 2016/8/1.
 */
public class MagHandler extends BaseHandler {
    private static final String TAG = "MagHandler";

    //region Singleton
    public static MagHandler getInstance() {
        // 这里必须是全局路径 否则无法找到
        return (MagHandler) instance(MagHandler.class.getName());
    }
    //endregion

    //region Members
    private int mCurrentMagPosition; //当前磁传感 位置  [-13,13]
    private boolean isMissingMag; // 是否有 未读到磁条警报
    //TODO 行号,具体感应值?

    public boolean isMissingMag() {
        return isMissingMag;
    }

    public int getCurrentMagPosition() {
        return mCurrentMagPosition;
    }

    //endregion

    public boolean queryMagPosition() {
        return send(ArmMagnetic.QueryMagPosition, null).getSendState();
    }

    @Override
    public boolean startSelfCheck() {
        isSelfCheck = send(ArmMagnetic.SetStartSelfCheck, null).getSendState();
        return isSelfCheck;
    }

    public boolean setMaxMag(int oneToSix) {
        if (oneToSix > 6 || oneToSix < 1) {
            Log.e(TAG, "setMaxMag: 请输入1到6的数字");
            return false;
        }
        return send(ArmMagnetic.SetMaxMag, (byte) oneToSix).getSendState();
    }


    @Override
    public void onReceive(byte[] fromUsbData) {
        byte[] body = mTransfer.getBody(fromUsbData);
        if (body == null) {
            return;
        }
        switch (fromUsbData[ArmHead.COMMAND]) {
            case 0x01:
                mCurrentMagPosition = body[0];
            case 0x04:
                isMissingMag = body[0] == 0x01;
                reply(fromUsbData);
                break;
            case 0x05:
                //TODO 什么行号什么鬼?
                reply(fromUsbData);
                break;
        }
    }


}
