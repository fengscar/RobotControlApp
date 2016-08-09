/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2016-3-22 上午9:57:37
 */
package com.feng.SelfCheck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.feng.RSS.R;
import com.feng.Usb.ArmHandler.ButtonHandler;
import com.feng.Usb.ArmProtocol;
import com.feng.Usb.UsbData;
import com.feng.Usb.UsbEvent;
import com.feng.Utils.Verifier;

public class CheckFragmentMachineBtn extends CheckFragment {
    public static CheckFragment newInstance(int expRes, int imgRes) {
        CheckFragment f = new CheckFragmentMachineBtn();
        Bundle args = new Bundle();
        args.putInt(Explain_RES, expRes);
        args.putInt(Image_RES, imgRes);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_self_check_3button, null);
        initView();
        init3Button();
        initValue();
        return view;
    }

    @Override
    public boolean startSelfCheck() {
        return ButtonHandler.getInstance().startSelfCheck();
    }

    @Override
    public void onReceiveArmData(UsbData usbData) {
        if (usbData.getEvent() == UsbEvent.UsbReceive) {
            byte[] body = usbData.getReceiveBody();
            if (body == null) {
                return;
            }
            if( new Verifier().compare2Byte(usbData.getDataReceive(), ArmProtocol.MachineStartBtn)){
                if (body[0] == 0x00) {
                    btn1.setChecked(false);
                } else if (body[0] == 0x01) {
                    btn1.setChecked(true);
                }
            }
            if( new Verifier().compare2Byte(usbData.getDataReceive(), ArmProtocol.MachineStopBtn)){
                if (body[0] == 0x00) {
                    btn2.setChecked(false);
                } else if (body[0] == 0x01) {
                    btn2.setChecked(true);
                }
            }
        }
    }

    private void initValue() {
        setKeyStr("执行按键:", "停止按键", null);
        btn3.setVisibility(View.GONE);
    }

}

