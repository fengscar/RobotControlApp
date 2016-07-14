package com.feng.Usb;

/**
 * Created by fengscar on 2016/5/26.
 */
public enum UsbEvent {
    UsbConnect,  //连接成功
    UsbConnectFailed, //连接失败
    UsbDisconnect,  //主动断开连接

    UsbReceive,   //接收到数据

    UsbSendSuccess,   //发送数据成功
    UsbSendFailed,//发送数据失败
}
