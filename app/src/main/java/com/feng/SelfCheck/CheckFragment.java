/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2016-3-22 下午8:54:22
 */
package com.feng.SelfCheck;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.WorkerThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.feng.RSS.R;
import com.feng.Usb.ArmProtocol;
import com.feng.Usb.UsbData;

public abstract class CheckFragment extends Fragment implements ArmProtocol {
    public final static String Explain_RES = "expRes";
    public final static String Image_RES = "imgRes";


    protected View view;
    protected ImageView ivSignPic;
    protected TextView tvExplain;

    protected TextView key1, key2, key3;
    protected ToggleButton btn1, btn2, btn3;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_self_check_3button, null);
        initView();
        ivSignPic.setImageResource(R.drawable.check_ultra);
        return view;
    }

    protected void initView() {
        key1 = (TextView) view.findViewById(R.id.tvKey1);
        key2 = (TextView) view.findViewById(R.id.tvKey2);
        key3 = (TextView) view.findViewById(R.id.tvKey3);

        ivSignPic = (ImageView) view.findViewById(R.id.ivSignPic);
        tvExplain = (TextView) view.findViewById(R.id.tvExplain);

        tvExplain.setText(this.getArguments().getInt(Explain_RES));
        if (getArguments().getInt(Image_RES) != 0) {
            ivSignPic.setImageResource(this.getArguments().getInt(Image_RES));
        } else {
            ivSignPic.setVisibility(View.GONE);
        }
    }

    protected void init3Button() {
        btn1 = (ToggleButton) view.findViewById(R.id.btn1);
        btn2 = (ToggleButton) view.findViewById(R.id.btn2);
        btn3 = (ToggleButton) view.findViewById(R.id.btn3);
    }

    protected void setKeyStr(String str1, String str2, String str3) {
        if (str1 != null) {
            key1.setText(str1);
        } else {
            key1.setVisibility(View.GONE);
        }
        if (str2 != null) {
            key2.setText(str2);
        } else {
            key2.setVisibility(View.GONE);
        }
        if (str3 != null) {
            key3.setText(str3);
        } else {
            key3.setVisibility(View.GONE);
        }
    }


    @WorkerThread
    public abstract boolean startSelfCheck();


    public abstract void onReceiveArmData(UsbData usbData);
}

