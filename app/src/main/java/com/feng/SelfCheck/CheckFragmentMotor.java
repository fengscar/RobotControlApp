/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2016-3-22 上午9:57:37
 */
package com.feng.SelfCheck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.feng.RSS.R;
import com.feng.Usb.ArmHandler.MotionHandler;
import com.feng.Usb.UsbData;

public class CheckFragmentMotor extends CheckFragment {
    public static CheckFragment newInstance(int expRes, int imgRes) {
        CheckFragment f = new CheckFragmentMotor();
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
        btn1.setVisibility(View.GONE);
        btn2.setVisibility(View.GONE);
        btn3.setVisibility(View.GONE);

        setKeyStr(" 转动 90°", "转动180° ", null);
        key1.setBackgroundResource(R.drawable.selector_tb_wash);
        key1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                MotionHandler.getInstance().setSelfCheckMotion90();
            }
        });
        key2.setBackgroundResource(R.drawable.selector_tb_wash);
        key2.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                MotionHandler.getInstance().setSelfCheckMotion180();
            }
        });
    }

    @Override
    public boolean startSelfCheck() {
        return true;
    }

    @Override
    public void onReceiveArmData(UsbData usbData) {

    }
}

