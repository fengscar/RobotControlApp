package com.feng.Activities;


import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.feng.Base.BaseActivity;
import com.feng.Usb.ArmProtocol;
import com.feng.Constant.I_Parameters;
import com.feng.RSS.R;
import com.feng.CustomView.ArmSettingView;
import com.feng.CustomView.ArmSettingView.ArmSettingBtnCallback;
import com.feng.CustomView.CustomDialog;
import com.feng.CustomView.CustomDialogCallback;

import java.util.List;

public class EditArmActivity extends BaseActivity implements I_Parameters, ArmProtocol {

    private Button btnCancel, btnReset;
    private TextView tvMag, tvMotion, tvRoute, tvUltra, tvSys;

    private ArmSettingView asvArmMagMax,
            asvArmMotionOriginTurnSpeed, asvArmMotionStartSpeed, asvArmMotionIncrementSpeed,
            asvArmMotionMaxSpeed, asvArmMotionTurnSpeed, asvArmMotionUltraSpeed, asvArmMotionReadCardSpeed,
            asvArmRouteMaxRoute, asvArmRouteMaxTarget, asvArmRouteMaxNode,
            asvArmUltraWarningDistance,
            asvArmSysRobotID, asvArmSysSendPeriod, asvArmSysSendCount;
    private ArmSettingView[] asvSet;

    //用户 权限等级
    private String userGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_edit_arm);
        // 这里简单的把  用户作为action传递过来, 是否需要改成使用 BUNDLE
        userGroup = this.getIntent().getAction();
        initView();
        /**
         * 部分例外的权限 在这里
         */

        // 获取SP的值 ,初始化 状态 (  要提前在 绑定监听前 )
        initValue();

    }

    private void initView() {
        // 标题栏2个按键( 返回与重置)
        btnCancel = (Button) findViewById(R.id.btnUniformTitleLeft);
        btnReset = (Button) findViewById(R.id.btnUniformTitleRight);
        btnCancel.setOnClickListener(new ButtonListener());
        btnReset.setOnClickListener(new ButtonListener());
        btnReset.setText(R.string.arm_sys_reset);
        // 当前设置选项 的Text
        ((TextView) findViewById(R.id.tvUniformTitleCenter)).setText(R.string.menu_edit_rss);
        // Title
        tvMag = (TextView) findViewById(R.id.arm_mag);
        tvMotion = (TextView) findViewById(R.id.arm_motion);
        tvRoute = (TextView) findViewById(R.id.arm_route);
        tvUltra = (TextView) findViewById(R.id.arm_ultra);
        tvSys = (TextView) findViewById(R.id.arm_sys);
        // ArmSettingsView ..
        asvArmMagMax = (ArmSettingView) findViewById(R.id.arm_mag_max);
        asvArmMotionOriginTurnSpeed = (ArmSettingView) findViewById(R.id.arm_motion_originTurnSpeed);
        asvArmMotionStartSpeed = (ArmSettingView) findViewById(R.id.arm_motion_startSpeed);
        asvArmMotionIncrementSpeed = (ArmSettingView) findViewById(R.id.arm_motion_incrementSpeed);
        asvArmMotionMaxSpeed = (ArmSettingView) findViewById(R.id.arm_motion_maxSpeed);
        asvArmMotionTurnSpeed = (ArmSettingView) findViewById(R.id.arm_motion_turnSpeed);
        asvArmMotionUltraSpeed = (ArmSettingView) findViewById(R.id.arm_motion_ultraSpeed);
        asvArmMotionReadCardSpeed = (ArmSettingView) findViewById(R.id.arm_motion_readCardSpeed);
        asvArmRouteMaxRoute = (ArmSettingView) findViewById(R.id.arm_route_maxRoute);
        asvArmRouteMaxTarget = (ArmSettingView) findViewById(R.id.arm_route_maxTarget);
        asvArmRouteMaxNode = (ArmSettingView) findViewById(R.id.arm_route_maxNode);
        asvArmUltraWarningDistance = (ArmSettingView) findViewById(R.id.arm_ultra_warningDistance);
        asvArmSysRobotID = (ArmSettingView) findViewById(R.id.arm_sys_robot_id);
        asvArmSysSendPeriod = (ArmSettingView) findViewById(R.id.arm_sys_sendPeriod);
        asvArmSysSendCount = (ArmSettingView) findViewById(R.id.arm_sys_sendCount);

        //绑定监听器
        asvArmMagMax.setCallback(new ArmSettingsImp(ArmMagMax, 1));
        asvArmMotionOriginTurnSpeed.setCallback(new ArmSettingsImp(ArmMotionOriginTurnSpeed, 2));
        asvArmMotionStartSpeed.setCallback(new ArmSettingsImp(ArmMotionStartSpeed, 2));
        asvArmMotionIncrementSpeed.setCallback(new ArmSettingsImp(ArmMotionIncrementSpeed, 2));
        asvArmMotionMaxSpeed.setCallback(new ArmSettingsImp(ArmMotionMaxSpeed, 2));
        asvArmMotionTurnSpeed.setCallback(new ArmSettingsImp(ArmMotionTurnSpeed, 2));
        asvArmMotionUltraSpeed.setCallback(new ArmSettingsImp(ArmMotionUltraSpeed, 2));
        asvArmMotionReadCardSpeed.setCallback(new ArmSettingsImp(ArmMotionReadCardSpeed, 2));
        asvArmUltraWarningDistance.setCallback(new ArmSettingsImp(ArmUltraWarningDistance, 1));
        asvArmSysRobotID.setCallback(new ArmSettingsImp(ArmSysRobotID, 1));
        asvArmSysSendPeriod.setCallback(new ArmSettingsImp(ArmSysSendPeriod, 1));
        asvArmSysSendCount.setCallback(new ArmSettingsImp(ArmSysSendCount, 1));

        //方便同一管理
        asvSet = new ArmSettingView[]{
                asvArmMagMax,
                asvArmMotionOriginTurnSpeed, asvArmMotionStartSpeed, asvArmMotionIncrementSpeed,
                asvArmMotionMaxSpeed, asvArmMotionTurnSpeed, asvArmMotionUltraSpeed, asvArmMotionReadCardSpeed,
                asvArmRouteMaxRoute, asvArmRouteMaxTarget, asvArmRouteMaxNode,
                asvArmUltraWarningDistance,
                asvArmSysRobotID, asvArmSysSendPeriod, asvArmSysSendCount
        };
        //同一设置权限
        for (ArmSettingView asv : asvSet) {
            asv.putUserLimit(userGroup);
        }
        //隐藏
        hideTvTitle();
    }

    // 隐藏标题栏( 当前权限下该选项栏中没有子项就隐藏)
    private void hideTvTitle() {
        switch (userGroup) {
            case USER_CUSTOMER:
                tvMag.setVisibility(View.GONE);
                tvRoute.setVisibility(View.GONE);
                tvUltra.setVisibility(View.GONE);
                break;
            case USER_SERVICER:
                // 暂时没有!
                break;
            case USER_PROGRAMMER:
                // 暂时没有!
                break;
            default:
                break;
        }
    }

    private void initValue() {
        Context context = EditArmActivity.this;
    }

    class ButtonListener implements OnClickListener {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnUniformTitleLeft:
                    EditArmActivity.this.finish();
                    break;
                case R.id.btnUniformTitleRight:
                    CustomDialog.Builder builder = new CustomDialog.Builder(EditArmActivity.this);
                    builder.getConfirmDialog("警 告!", "是否确认恢复到默认设置,不可恢复",
                            new CustomDialogCallback() {
                                public boolean onDialogBtnClick(List<View> viewList) {
                                    // 以下操作 恢复出厂
                                    //1 .SP 清空
                                    //2 .当前界面 asv全部重置
                                    for (ArmSettingView asv : asvSet) {
                                        asv.reset();
                                    }
                                    //3. 发送命令( USB_SEND)
                                    intentDealer.sendIntent(USB_SEND, ArmSysRest);
                                    return true;
                                }
                            }).show();
                    break;
                default:
                    break;
            }
        }
    }

    class ArmSettingsImp implements ArmSettingBtnCallback {
        private byte[] head;
        private int byteLength;

        public ArmSettingsImp(byte[] sendHead, int byteLength) {
            head = sendHead;
            this.byteLength = byteLength;
        }

        public void applyConfig(int currentNum) {
            // 沃日, 居然数据格式还有 2位,1位的区别
            byte[] resultByte = null;
            if (this.byteLength == 2) {
                resultByte = transfer.intTo2Byte(currentNum);
            } else if (this.byteLength == 1) {
                resultByte = new byte[]{(byte) currentNum};
            }
            intentDealer.sendIntent(USB_SEND, head, resultByte);
        }

    }
}
