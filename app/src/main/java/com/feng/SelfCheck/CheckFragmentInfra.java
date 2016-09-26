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
import com.feng.Usb.ArmHandler.PIRHandler;
import com.feng.Usb.ArmProtocol;
import com.feng.Usb.UsbData;
import com.feng.Utils.Verifier;

public class CheckFragmentInfra extends CheckFragment {

    public static CheckFragment newInstance(int expRes, int imgRes) {
        CheckFragment f = new CheckFragmentInfra();
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
        return PIRHandler.getInstance().startSelfCheck();
    }

    @Override
    public void onReceiveArmData(UsbData usbData) {
        if (new Verifier().compareHead(usbData.getDataReceive(), ArmProtocol.InfraredWarning)) {
            byte[] body = usbData.getReceiveBody();
            if (body == null) {
                return;
            }
            switch (body[0]) {
                case 0x00:
                    btn1.setChecked(false);
                    break;
                case 0x01:
                    btn1.setChecked(true);
                    break;
            }
        }
    }

    private void initValue() {
        setKeyStr("人体红外传感器:", null, null);
        btn2.setVisibility(View.GONE);
        btn3.setVisibility(View.GONE);
    }
}

