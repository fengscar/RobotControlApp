package com.feng.Usb.ArmHandler;

import com.feng.Usb.ArmModel.ArmButton;
import com.feng.Usb.ArmHead;

/**
 * Created by fengscar on 2016/8/1.
 */
public class ButtonHandler extends BaseHandler {
    private static final String TAG = "ButtonHandler";

    //region Singleton
    public static ButtonHandler getInstance() {
        // 这里必须是全局路径 否则无法找到
        return (ButtonHandler) instance(ButtonHandler.class.getName());
    }
    //endregion

    @Override
    public boolean startSelfCheck() {
        isSelfCheck = send(ArmButton.SetStartSelfCheck, null).getSendState();
        return isSelfCheck;
    }

    //region Members

    //正常情况下 是否有报警
    private boolean isExecute = false; // 执行按键状态 是否按下
    private boolean isScram = false; // 急停按键状态

    public boolean isExecute() {
        return isExecute;
    }

    public boolean isScram() {
        return isScram;
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
                isExecute = body[0] == 0x01;
                reply(fromUsbData);
            case 0x03:
                isScram = body[0] == 0x01;
                reply(fromUsbData);
                break;
        }
    }
}
