package com.feng.Usb.ArmHandler;

import com.feng.Usb.ArmModel.ArmPIR;
import com.feng.Usb.ArmHead;

/**
 * Created by fengscar on 2016/8/1.
 */
public class PIRHandler extends BaseHandler {
    private static final String TAG = "PIRHandler";

    //region Singleton
    public static PIRHandler getInstance() {
        // 这里必须是全局路径 否则无法找到
        return (PIRHandler) instance(PIRHandler.class.getName());
    }
    //endregion

    //region Members
    private boolean isPirWarning;

    public boolean isPirWarning() {
        return isPirWarning;
    }
    //endregion

    @Override
    public boolean startSelfCheck() {
        isSelfCheck = send(ArmPIR.SetStartSelfCheck, null).getSendState();
        return isSelfCheck;
    }

    @Override
    public void onReceive(byte[] fromUsbData) {
        byte[] body = mTransfer.getBody(fromUsbData);
        if (body == null) {
            return;
        }
        switch (fromUsbData[ArmHead.COMMAND]) {
            case 0x02:
                isPirWarning = body[0] == 0x01;
                reply(fromUsbData);
                break;
        }
    }
}
