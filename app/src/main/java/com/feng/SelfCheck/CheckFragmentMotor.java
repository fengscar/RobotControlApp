/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2016-3-22 上午9:57:37
 */
package com.feng.SelfCheck;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.feng.RSS.R;
import com.feng.RobotApplication;
import com.feng.Usb.ArmHandler.MotionHandler;
import com.feng.Usb.UsbData;
import com.sdsmdg.tastytoast.TastyToast;

public class CheckFragmentMotor extends CheckFragment {
    @BindView(R.id.tvExplain)
    TextView mTvExplain;

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
        view = inflater.inflate(R.layout.fragment_self_check_motor, null);

        ButterKnife.bind(this, view);

        mTvExplain.setText(this.getArguments().getInt(Explain_RES));

        return view;
    }

    @Override
    public boolean startSelfCheck() {
        return true;
    }

    @Override
    public void onReceiveArmData(UsbData usbData) {

    }

    @OnClick({R.id.btn1, R.id.btn2})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn1:
                MotionHandler.getInstance().setSelfCheckMotion90();
                TastyToast.makeText(RobotApplication.getContext(), "开始转动90度", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                break;
            case R.id.btn2:
                MotionHandler.getInstance().setSelfCheckMotion180();
                TastyToast.makeText(RobotApplication.getContext(), "开始转动180度", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                break;
        }
    }
}

