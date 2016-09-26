package com.feng.Usb.ArmHandler;

import android.util.Log;
import com.feng.Usb.*;
import com.feng.Utils.Transfer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by fengscar on 2016/8/1.
 */
public class BaseHandler implements ArmDataReceiver {
    private static final String TAG = "BaseHandler";
    protected Transfer mTransfer;
    protected volatile ArmUsbManager mArmUsbManager;


    protected BaseHandler() {
        mTransfer = new Transfer();
        mArmUsbManager = ArmUsbManager.getInstance();
    }


    public static void releaseUsbManager() {
        instance = null;
    }

    private static Map<String, Object> instance;

    public static Object instance(String objname) {
        if (instance == null) {
            instance = new HashMap<>();
        }
        if (instance.get(objname) == null || !(instance.get(objname) instanceof BaseHandler)) {
            try {
                instance.put(objname, Class.forName(objname).newInstance());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return instance.get(objname);
    }

    //region 自检
    protected boolean isSelfCheck = false; //是否在自检

    public boolean startSelfCheck() {
        Log.e(TAG, "systemStartSelfCheck: 子类未重写");
        return false;
    }
    //endregion

    @Override
    public void onReceive(byte[] fromUsbData) {
        switch (fromUsbData[ArmHead.DATA]) {
        }
    }

    //默认回复->成功
    protected void reply(byte[] receiveData) {
        this.reply(receiveData, true);
    }

    protected void reply(byte[] receiveData, boolean isSuccess) {
        if (receiveData == null || receiveData.length < 5) {
            Log.e(TAG, "reply : error,wrong receiveData");
            return;
        }
        // 获取 0 1 2 作为头, 并将 receiveData 设置为 成功 , 打包后 发送出去;
        byte[] dataToSend = new Transfer().packingByte(
                new byte[]{receiveData[0], receiveData[1], receiveData[2]},
                isSuccess ? new byte[]{(byte) 0x01} : new byte[]{(byte) 0x00});
        mArmUsbManager.send(dataToSend);
    }

    public UsbData sendFullData(byte[] fullData) {
        if (mArmUsbManager == null) {
            return new UsbData(UsbEvent.UsbDisconnect, null, null);
        }
        return mArmUsbManager.sendForResult(fullData);
    }

    public UsbData send(byte[] head, byte[] body) {
        return this.sendFullData(mTransfer.packingByte(head, body));
    }

    public UsbData send(ArmHead head, byte[] body) {
        return this.send(head.getHead(), body);
    }

    public UsbData send(ArmHead head, byte body) {
        return this.send(head, new byte[]{body});
    }

    public UsbData send(ArmHead head, boolean b) {
        return this.send(head, (byte) (b ? 0x01 : 0x00));
    }
}
