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
import com.feng.Usb.ArmHandler.RFIDHandler;
import com.feng.Usb.ArmProtocol;
import com.feng.Usb.UsbData;
import com.feng.Utils.L;
import com.feng.Utils.Verifier;

public class CheckFragmentRFID extends CheckFragment {
    public static CheckFragment newInstance(int expRes, int imgRes) {
        CheckFragment f = new CheckFragmentRFID();
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
        return RFIDHandler.getInstance().startSelfCheck();
    }

    @Override
    public void onReceiveArmData(UsbData usbData) {
        if (new Verifier().compareHead(usbData.getDataReceive(), ArmProtocol.CurrentRFID)) {
            byte[] body = usbData.getReceiveBody();
            if (body == null) {
                return;
            }
            if (body.length != 2) {
                L.e(" 自检卡号 长度错误 !=2..");
            }
            btn1.setVisibility(View.VISIBLE);
            btn1.setText("0x" +
                    (Integer.toHexString((int) (body[1] & 0xff)).toUpperCase()) +
                    (Integer.toHexString((int) (body[0] & 0xff)).toUpperCase()));
        }
    }

    private void initValue() {
        setKeyStr("读到的卡号 :", null, null);
        btn1.setVisibility(View.INVISIBLE);
        btn1.setChecked(true);
        btn2.setVisibility(View.GONE);
        btn3.setVisibility(View.GONE);
    }

}

