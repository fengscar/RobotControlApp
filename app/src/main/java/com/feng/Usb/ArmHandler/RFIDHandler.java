package com.feng.Usb.ArmHandler;

import com.feng.Usb.ArmModel.ArmRFID;
import com.feng.Usb.ArmHead;

/**
 * Created by fengscar on 2016/8/1.
 */
public class RFIDHandler extends BaseHandler {
    private static final String TAG = "RFIDHandler";

    //region Singleton
    public static RFIDHandler getInstance() {
        // 这里必须是全局路径 否则无法找到
        return (RFIDHandler) instance(RFIDHandler.class.getName());
    }
    //endregion

    //region Members
    private int mCurrentCard;
    private int[] mMissingCards;
    private boolean isWrongCard; //是否读到错误卡

    public int getCurrentCard() {
        return mCurrentCard;
    }

    public int[] getMissingCards() {
        return mMissingCards;
    }

    public boolean isWrongCard() {
        return isWrongCard;
    }
    //endregion

    @Override
    public boolean startSelfCheck() {
        isSelfCheck = send(ArmRFID.SetStartSelfCheck, null).getSendState();
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
                mCurrentCard = mTransfer.twoByteToInt(body);
                reply(fromUsbData);
            case 0x03:
                //TODO 与协议不符
                mMissingCards = queryTaskResult(body);
                reply(fromUsbData);
                break;
            case 0x04:
                isWrongCard = true;
                break;
        }
    }


    public int[] queryTaskResult(byte[] fromUsbData) {
        //无任务时
        if (fromUsbData == null) {
            return null;
        }
        int[] result = new int[fromUsbData[0]];
        byte[] setOrDelData = mTransfer.getBody(fromUsbData, 1);
        if (setOrDelData == null) {
            return null;
        }
        for (int i = 0; i < setOrDelData.length / 2; i++) {
            int nodeID = mTransfer.twoByteToInt(new byte[]{setOrDelData[i * 2], setOrDelData[i * 2 + 1]});
            result[i] = nodeID;
        }
        return result;
    }


}
