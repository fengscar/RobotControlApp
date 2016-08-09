package com.feng.Usb.ArmHandler;

import com.feng.Usb.ArmModel.ArmBarrier;
import com.feng.Usb.ArmHead;
import com.feng.Utils.L;

/**
 * Created by fengscar on 2016/8/1.
 */
public class BarrierHandler extends BaseHandler {
    private static final String TAG = "BarrierHandler";

    //region Singleton
    public static BarrierHandler getInstance() {
        return (BarrierHandler) instance(BarrierHandler.class.getName());
    }
    //endregion

    @Override
    public boolean startSelfCheck() {
        isSelfCheck = send(ArmBarrier.SetStartSelfCheck, null).getSendState();
        return isSelfCheck;
    }

    //region Members

    //正常情况下 是否有报警
    private boolean isWarning;

    //自检时
    private boolean mFrontWarning;
    private boolean mSideWarning;
    private boolean mBackWarning;

    public boolean isWarning() {
        return isWarning;
    }

    public boolean isFrontWarning() {
        return mFrontWarning;
    }

    public boolean isSideWarning() {
        return mSideWarning;
    }

    public boolean isBackWarning() {
        return mBackWarning;
    }
    //endregion

    @Override
    public void onReceive(byte[] fromUsbData) {
        byte[] body = mTransfer.getBody(fromUsbData);
        if (body == null) {
            return;
        }
        switch (fromUsbData[ArmHead.COMMAND]) {
            case 0x02:
                if (!isSelfCheck) {
                    isWarning = body[0] == 0x01;
                } else {
                    mFrontWarning = getBooleanArray(body[0], 0);
                    mSideWarning = getBooleanArray(body[0], 1);
                    mBackWarning = getBooleanArray(body[0], 2);
                }
                reply(fromUsbData);
                break;
        }
    }

    // 得到 字节b 每位 & 的结果 ( 具体看协议中的红外避障)
    private boolean getBooleanArray(byte b, int x) {
        if (x < 0 && x > 2) {
            L.e("错误的输入" + "getBooleanArray");
            return false;
        }
        b = (byte) (b >> x);
        return (b & 1) != 0;
    }

}
