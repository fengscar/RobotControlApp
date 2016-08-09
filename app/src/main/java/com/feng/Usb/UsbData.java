package com.feng.Usb;

import com.feng.Utils.Transfer;

import java.util.Arrays;

/**
 * Created by fengscar on 2016/5/24.
 */

public class UsbData {
    private byte[] dataToSend;
    private byte[] dataReceive;
    private UsbEvent mEvent;

    //region Getter/Setter
    public byte[] getDataToSend() {
        return dataToSend;
    }

    public void setDataToSend(byte[] dataToSend) {
        this.dataToSend = dataToSend;
    }

    public byte[] getDataReceive() {
        return dataReceive;
    }

    public void setDataReceive(byte[] dataReceive) {
        this.dataReceive = dataReceive;
    }

    public UsbEvent getEvent() {
        return mEvent;
    }

    public void setEvent(UsbEvent event) {
        mEvent = event;
    }
    //endregion


    public UsbData() {
    }

    public UsbData(UsbEvent dataType, byte[] dataReceive, byte[] dataToSend) {
        mEvent = dataType;
        this.dataReceive = dataReceive;
        this.dataToSend = dataToSend;
    }

    //默认: 发出通知
    public UsbData(UsbEvent dataEvent) {
        this(dataEvent, null, null);
    }

    //默认:接收
    public UsbData(byte[] dataReceive) {
        this(UsbEvent.UsbReceive, dataReceive, null);
    }

    //默认:发送成功
    public UsbData(byte[] dataReceive, byte[] dataToSend) {
        this(UsbEvent.UsbSendSuccess, dataReceive, dataToSend);
    }


    @Override
    public String toString() {
        String event = null;
        switch (this.mEvent) {
            case UsbConnect:
                event = "USB连接成功";
                break;
            case UsbConnectFailed:
                event = "USB连接失败";
                break;
            case UsbDisconnect:
                event = "USB断开连接";
                break;

            case UsbReceive:
                event = "USB接收数据";
                break;
            case UsbSendSuccess:
                event = "USB发送成功";
                break;
            case UsbSendFailed:
                event = "USB发送失败";
                break;
        }
        return "  UsbData" +
                "---[事件]:" + event + "--" + new Transfer().getAction(dataReceive) +
                ". [接收]:" + Arrays.toString(this.dataReceive) +
                ",[发送]:" + Arrays.toString(dataToSend);
    }

    public boolean getSendState() {
        return UsbEvent.UsbSendSuccess == this.getEvent();
    }

    public byte[] getReceiveBody() {
        if (dataReceive != null) {
            return new Transfer().getBody(dataReceive);
        } else {
            return null;
        }
    }
}
