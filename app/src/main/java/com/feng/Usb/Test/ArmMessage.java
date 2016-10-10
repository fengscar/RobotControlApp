package com.feng.Usb.Test;

/**
 * Created by fengscar on 2016/9/29.
 */

public class ArmMessage {
    final byte mRobot = 0x01;
    byte mModule;
    byte mCommand;
    byte mLength;
    byte mData;
    byte mParity;
}
