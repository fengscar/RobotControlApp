package com.feng.Base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import com.feng.Constant.ArmProtocol;
import com.feng.CustomView.WarningDialog;
import com.feng.RobotApplication;
import com.feng.Utils.*;

import java.io.IOException;
import java.util.Arrays;


public class BaseReceiver extends BroadcastReceiver implements ArmProtocol {
    private final static String LOG = "BaseReceiver";

    private IntentDealer intentDealer;
    private Transfer transfer;
    protected Verifier verifier;

    public BaseReceiver(WarningDialog wd) {
        transfer = new Transfer();
        intentDealer = new IntentDealer(transfer);
        verifier = new Verifier();
        this.warningDialog = wd;
    }

    protected WarningDialog warningDialog;

    /**
     * 接收到广播后 执行相应的操作
     * 1. 接收到LOCAL_ACTIONS 后 : 一般是弹窗+ 刷新控件
     * 2. 接收到FROM_ARM_ACTIONS 后 : 一般是 出错+ 弹窗
     */
    public void onReceive(Context context, Intent intent) {
        byte[] receiveData = null;
        if (intent.hasExtra(UNIFORM_RECEIVE)) {
            receiveData = intent.getByteArrayExtra(UNIFORM_RECEIVE);
            L.e(LOG, "onReceive接收到:" + intent.getAction() + ": " + Arrays.toString(receiveData));
        }
        //确保 接收到 多次广播,只执行一次操作
        switch (intent.getAction()) {
            case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                RobotApplication.getArmUsbUtil().connect();
                break;
            case UsbManager.ACTION_USB_DEVICE_DETACHED:
                RobotApplication.getArmUsbUtil().disconnect();
                break;
            case USB_RECEIVE:
                dealReceive(receiveData);
                break;
            case USB_CONNECT_FAILED:
                warningDialog.addWarning(false, USB_CONNECT_FAILED, USB_CONNECT_FAILED, null);
                break;
            case SEND_FAILED:
                // 根据头3个字节 获取到具体的ACTION
                String detailAction = getSendAction(receiveData);
                if (detailAction == null) {
                    break;
                }
                switch (detailAction) {
                    case ERROR_FROM_ARM:
                        switch (transfer.getData(receiveData)[0]) {
                            case 0x01:
                                L.e("模块编号错误");
                                break;
                            case 0x02:
                                L.e("命令编号错误");
                                break;
                            case 0x03:
                                L.e("数据长度错误");
                                break;
                            case 0x04:
                                L.e("数据错误");
                                break;
                            case 0x05:
                                T.show("我还不能那么做");
                                break;
                            case 0x06:
                                L.e("校验位错误");
                                break;
                            case 0x07:
                                L.e("进入硬件中断错误");
                                break;
                            default:
                                break;
                        }
                        break;
                    default:
                        if (intent.getAction().equals(SEND_FAILED)) {
                            T.show("发送失败:" + detailAction);
                            L.e(" 发送失败 : " + detailAction +
                                    receiveData == null ? " null" : (" " + Arrays.toString(receiveData)));
                        }
                        break;
                }
            default:
                break;
        }
    }

    /**
     * 处理接收到的信息(  不回复)
     * 回复ARM使用的是 replyReceive();
     * 需要回复 返回 false;
     *
     * @return
     * @throws IOException
     */
    public boolean dealReceive(byte[] buffer) {

        switch (buffer[MODULE]) {
            //运动模块
            case 0x01:
                switch (buffer[COMMAND]) {
                    // 查询运动状态
                    case 0x01:
                        try {
                            L.i("运动状态为:" + buffer[4] + ",当前速度为:" + buffer[5] + ",运动距离为:" + buffer[6] + ",最大速度为:" + buffer[7]);
                        } catch (Exception e) {
                            return false;
                        }
                        break;
                    // 移动距离 溢出
                    case 0x0E:
                        if (buffer[DATA] == 0x01) {
                            warningDialog.addWarning(false, MOVE_DISTANCE_OVERFLOW, MOVE_DISTANCE_OVERFLOW, null);
                            L.e("收到移动距离溢出 报警, 已回复确认信息给ARM");
                        } else {
                            removeWarning(MOVE_DISTANCE_OVERFLOW);
                            L.i("收到移动距离溢出 警报取消, 已回复确认信息给ARM");
                        }
                        break;
                    case 0x0F:
                        warningDialog.addWarning(false, LONG_TIME_NOT_OPERATE, LONG_TIME_NOT_OPERATE, null);
                        break;
                    default:
                        break;
                }
                break;
            case 0x02:
                //路径算法
                switch (buffer[COMMAND]) {
                    case 0x01:
                        L.i("收到节点结构体");
                        break;
                    case 0x02:
                        L.i("收到目的卡信息");
                        break;
                    case 0x07:
                        L.i("收到ARM的路径-第一条");
                        //				if( "这里对收到的数据进行判断"!=null){
                        //					transfer.throwGoods(
                        //							transfer.packingByte(ReplyUploadRoute, new byte[]{buffer[4],buffer[6],0x01}));
                        //				}
                        break;
                    case 0x08:
                        L.i("收到ARM的路径-后续");
                        //				if( "这里对收到的数据进行判断"!=null){
                        //					transfer.throwGoods(
                        //							transfer.packingByte(ReplyUploadRoute, new byte[]{buffer[4],buffer[6],0x01}));
                        //				}
                    case 0x09:
                        break;
                    default:
                        break;
                }
                break;
            //红外避障
            case 0x03:
                switch (buffer[COMMAND]) {
                    case 0x02:
                        if (buffer[DATA] == 0x01) {
                            warningDialog.addWarning(false, BARRIER_WARNING, BARRIER_WARNING, null);
                            L.e("收到 红外避障报警,已回复 确认信息给ARM");
                        } else {
                            removeWarning(BARRIER_WARNING);
                            L.e("红外避障报警取消");
                        }
                        break;
                    default:
                        break;
                }
                break;
            //RFID检测
            case 0x04:
                switch (buffer[COMMAND]) {
                    case 0x01:    // ARM 读到卡编号
                        break;
                    case 0x02:    // ARM 漏掉卡号
                        warningDialog.addWarning(false, MISSING_RFID, MISSING_RFID, null);
                        break;
                    case 0x03:    //ARM 读到错误的卡号
                        warningDialog.addWarning(false, WRONG_RFID, WRONG_RFID, null);
                        break;
                    default:
                        break;
                }
                break;
            //磁传感器
            case 0x05:
                switch (buffer[COMMAND]) {
                    case 0x01://查询磁感应位置
                        // TODO 这里动态显示 磁条位置?
                        L.i("收到ARM回复的磁感应距离");
                        break;
                    case 0x02://未读到磁条
                        if (buffer[DATA] == 0x01) {
                            warningDialog.addWarning(false, MISSING_MAGNETIC, MISSING_MAGNETIC, null);
                            L.e("收RM未读到磁条,已回复确认信息给ARM");
                        } else {
                            removeWarning(MISSING_MAGNETIC);
                            L.i("ARM又读到磁条了,已回复确认信息给ARM");
                        }
                        break;
                    default:
                        break;
                }
                break;
            //超声波检测
            case 0x06:
                switch (buffer[COMMAND]) {
                    case 0x03:
                        if (buffer[DATA] == 0x01) {
                            warningDialog.addWarning(false, ULTRASONIC_WARNING, ULTRASONIC_WARNING, null);
                            L.e("收到超声波报警,已回复确认信息给ARM");
                        } else {
                            removeWarning(ULTRASONIC_WARNING);
                            L.i("超声波报警取消");
                        }
                        break;
                    default:
                        break;
                }
                break;
            //电源模块
            case 0x07:
                switch (buffer[COMMAND]) {
                    case 0x01:
                        L.i("剩余电量为 : " + (int) transfer.getData(buffer)[0] + " %");
                        break;
                    case 0x02:
                        switch (buffer[DATA]) {
                            case 0x01:
                                warningDialog.addWarning(false, POWER_NOT_CHARGING, POWER_NOT_CHARGING, null);
                                break;
                            case 0x02:
                                warningDialog.addWarning(false, POWER_CHARGING, POWER_CHARGING, null);
                                break;
                            case 0x03:
                                warningDialog.addWarning(false, POWER_NOT_ENOUGH, POWER_NOT_ENOUGH, null);
                                break;
                            case 0x04:
                                warningDialog.addWarning(false, POWER_EMPTY, POWER_EMPTY, null);
                                break;

                            default:
                                break;
                        }
                        L.i("收到 充电状态变化,已回复确认信息给ARM");
                        break;
                    default:
                        break;
                }
                break;
            //人体红外
            case 0x08:
                switch (buffer[COMMAND]) {
                    case 0x01: // 人体红外报警
                        if (buffer[DATA] == 0x01) {
                            warningDialog.addWarning(false, INFRARED_WARNING, INFRARED_WARNING, null);
                            L.e("收到人体红外报警,已回复确认信息给ARM");
                        } else {
                            removeWarning(INFRARED_WARNING);
                            L.i("人体红外报警取消!");
                        }
                        break;
                    default:
                        break;
                }
                break;
            // 外设 按键
            case 0x0b:
                switch (buffer[COMMAND]) {
                    case 0x01: //执行按键
                        L.i("收到执行按键按下,已回复确认信息给ARM");
                        break;
                    case 0x02:
                        L.i("收到制动按键按下,已回复确认信息给ARM");
                        break;
                    default:
                        break;
                }
                //系统信息
            case 0x50:
                switch (buffer[COMMAND]) {
                    case 0x01: // 主动查询 软件版本号
                        // TODO 预留
                        L.i("收到 ARM的 软件版本号");
                        break;
                    case 0x02: // 主动查询 地图版本号
                        //TODO 这里判断 版本号,并提示是否更新
                        L.i("收到 ARM的 地图版本号");
                        break;
                    case 0x03:
                        L.i("收到 心跳包 ");
                        intentDealer.sendIntent(CONNECT_STATE, buffer);
                        break;
                    default:
                        L.e("未定义的协议格式: " + buffer[0] + " | " + buffer[1] + " | " + buffer[2]);
                        return false;
                }
        }
        return false;
    }

    /**
     * 根据 byte[] 获取Action的String
     *
     * @param receiveData
     * @return
     */
    protected String getReceiveAction(byte[] receiveData) {
        for (String action : RECEIVE_ACTIONS.keySet()) {
            if (verifier.compareHead(receiveData, RECEIVE_ACTIONS.get(action)) == true) {
                return action;
            }
        }
        return null;
    }

    protected String getSendAction(byte[] sendedData) {
        for (String action : SEND_ACTIONS.keySet()) {
            if (verifier.compareHead(sendedData, SEND_ACTIONS.get(action)) == true) {
                return action;
            }
        }
        return null;
    }

    protected void showWarningDialog() {
        if (warningDialog != null) {
            warningDialog.show();
        }
    }

    private void removeWarning(String protocalAction) {
        warningDialog.removeWarning(protocalAction);
    }
}








