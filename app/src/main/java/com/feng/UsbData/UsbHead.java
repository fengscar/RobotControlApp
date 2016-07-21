package com.feng.UsbData;

/**
 * Created by fengscar on 2016/7/19.
 */
public interface UsbHead {
    /**
     * 协议的 格式
     * [机器人编号]+[模块编号]+[命令]+[数据长度]+[数据]+[校验位]
     */
    int DEVICE = 0;
    int MODULE = 1;
    int COMMAND = 2;
    int DATA_LENGTH = 3;
    int DATA = 4;
    /**
     * 协议的纵向分类
     */
    // Robot
    byte ROBOT = 0x01;
    // Model
    byte MOTION = 0x01;
    byte PATH = 0x02;
    byte BARRIER = 0x03;
    byte RFID = 0x04;
    byte MAGNETIC = 0x05;
    byte ULTRA = 0x06;
    byte POWER = 0x07;
    byte INFRARED = 0x08;
    //    private static final byte CALL = 0x09;
    byte PAD = 0x0A;
    byte BUTTON = 0x0b;
    byte SYSTEM = 0x50;


    // 获取 协议头( 前三个字节)
    byte[] value();

    UsbHead get(byte commandIndex);
}