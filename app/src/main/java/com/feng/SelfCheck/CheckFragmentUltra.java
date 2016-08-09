/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2016-3-22 上午9:57:37
 */
package com.feng.SelfCheck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindString;
import com.feng.RSS.R;
import com.feng.Usb.ArmHandler.UltrasoundHandler;
import com.feng.Usb.ArmProtocol;
import com.feng.Usb.UsbData;
import com.feng.Usb.UsbEvent;
import com.feng.Utils.Verifier;

public class CheckFragmentUltra extends CheckFragment {
    @BindString(R.string.selfCheck_ultra_distance)
    String ultraDistance;


    public static CheckFragment newInstance(int expRes, int imgRes) {
        CheckFragment f = new CheckFragmentUltra();
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
        return UltrasoundHandler.getInstance().startSelfCheck();
    }

    @Override
    public void onReceiveArmData(UsbData usbData) {
        if (usbData.getEvent() == UsbEvent.UsbReceive) {
            if (new Verifier().compare2Byte(usbData.getDataReceive(), ArmProtocol.UltrasonicWarning)) {
                byte[] body = usbData.getReceiveBody();
                if (body == null) {
                    return;
                }
                if ((int) (body[0] & 0xff) <= 10) {
                    btn1.setText("< 10 cm");
                    btn2.setText("< 10 cm");
                } else {
                    // &0xff 是为了将 byte从有符号位转换为无符号位
                    btn1.setText((int) (body[0] & 0xff) + "cm");
                    btn2.setText((int) (body[0] & 0xff) + "cm");
                }
            }
        }
    }

    private void initValue() {
        setKeyStr("左前方超声波传感器:", "右前方超声波传感器:", null);
        btn1.setChecked(true);
        btn2.setChecked(true);
        btn1.setText(" 0 cm");
        btn2.setText(" 0 cm");
        btn3.setVisibility(View.GONE);
    }
}

