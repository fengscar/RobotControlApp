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
import com.feng.Usb.ArmHandler.BarrierHandler;
import com.feng.Usb.ArmProtocol;
import com.feng.Usb.UsbData;
import com.feng.Utils.L;
import com.feng.Utils.Verifier;

public class CheckFragmentBarrier extends CheckFragment {
    public static CheckFragment newInstance(int expRes, int imgRes) {
        CheckFragment f = new CheckFragmentBarrier();
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

    private void initValue() {
        setKeyStr("前方红外传感器[5个]:", "侧方红外传感器[4个]:", "后方红外传感器[2个]:");
    }


    @Override
    public boolean startSelfCheck() {
        return BarrierHandler.getInstance().startSelfCheck();
    }

    @Override
    public void onReceiveArmData(UsbData usbData) {
        if (new Verifier().compareHead(usbData.getDataReceive(), ArmProtocol.BarrierWarning)) {
            byte[] body = usbData.getReceiveBody();
            if (body == null) {
                return;
            }
            // data[0]	 的二进制的最低位 表示前方, 倒二位表示侧面, 倒三位表示 后面
            btn1.setChecked(getBooleanArray(body[0], 0));
            btn2.setChecked(getBooleanArray(body[0], 1));
            btn3.setChecked(getBooleanArray(body[0], 2));
        }
    }

    /**
     * @param b
     * @param x >>x , 取值 1~3
     * @return
     */
    private boolean getBooleanArray(byte b, int x) {
        if (x < 0 && x > 2) {
            L.e("错误的输入" + "getBooleanArray");
            return false;
        }
        b = (byte) (b >> x);
        return (b & 1) != 0;
    }
}

