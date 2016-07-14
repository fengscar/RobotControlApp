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




    public UsbEvent getUsbEvent() {
        return mEvent;
    }

    public byte[] getDataToSend() {
        return dataToSend;
    }

    public byte[] getDataReceive() {
        return dataReceive;
    }


    @Override
    public String toString() {
        String event=null;
        switch (this.mEvent){
            case UsbConnect:
                event="USB连接成功";
                break;
            case UsbConnectFailed:
                event="USB连接失败";
                break;
            case UsbDisconnect:
                event="USB断开连接";
                break;

            case UsbReceive:
                event="USB接收数据";
                break;
            case UsbSendSuccess:
                event="USB发送成功";
                break;
            case UsbSendFailed:
                event="USB发送失败";
                break;
        }
        return "  UsbData" +
                "---[事件]:"+event+ "--"+new Transfer().getAction(dataReceive)+
                ". [接收]:"+ Arrays.toString(this.dataReceive)+
                ",[发送]:"+Arrays.toString(dataToSend);
    }
}
