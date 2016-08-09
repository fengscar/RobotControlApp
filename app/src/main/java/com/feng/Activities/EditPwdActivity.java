package com.feng.Activities;


import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import com.feng.Base.BaseActivity;
import com.feng.Usb.ArmProtocol;
import com.feng.Constant.I_Parameters;
import com.feng.CustomView.CustomDialog;
import com.feng.CustomView.CustomDialogCallback;
import com.feng.RSS.R;
import com.feng.UserManage.*;
import com.feng.Utils.T;

import java.util.List;

public class EditPwdActivity extends BaseActivity implements I_Parameters, ArmProtocol {

    private Button btnCancel, btnSave, btnReset;
    private RadioGroup rgUserGroup;
    private RadioButton rbCustomer, rbServicer, rbProgrammer;
    private EditText etOldPwd, etNewPwd1, etNewPwd2;

    private String userGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_pwd);

        userGroup = this.getIntent().getAction();
        initView();
    }

    private void initView() {
        btnCancel = (Button) findViewById(R.id.btnUniformTitleLeft);
        btnCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                EditPwdActivity.this.finish();
            }
        });

        ((TextView) findViewById(R.id.tvUniformTitleCenter)).setText(R.string.menu_edit_password);

        

        rgUserGroup = (RadioGroup) findViewById(R.id.rgUserGroup);
        rbCustomer = (RadioButton) findViewById(R.id.rbCustomer);
        rbServicer = (RadioButton) findViewById(R.id.rbServicer);
        rbProgrammer = (RadioButton) findViewById(R.id.rbProgrammer);

        etOldPwd = (EditText) findViewById(R.id.etOldPwd);
        etNewPwd1 = (EditText) findViewById(R.id.etNewPwd1);
        etNewPwd2 = (EditText) findViewById(R.id.etNewPwd2);

        btnReset = (Button) findViewById(R.id.btnUniformTitleRight);
        btnReset.setText(R.string.setting_edit_password_reset);

        btnReset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String confirmMessage=
                        "重置后密码如下:\n"+
                        new Customer().toString()+"\n"+
                        new Servicer().toString()+"\n"+
                        new Programmer().toString();
                CustomDialog.Builder builder=new CustomDialog.Builder(EditPwdActivity.this);
                builder.setResourceID(R.layout.dialog_confirm);
                builder.setTitle("确认重置密码");
                builder.setButtonText("确定","取消");
                builder.setCancelBtnClick(R.id.dialogCancelBtn,null);
                builder.setOkBtnClick(R.id.dialogOKBtn, new CustomDialogCallback() {
                    @Override
                    public boolean onDialogBtnClick(List<View> viewList) {
                        PasswordManager.resetPassword(new Customer());
                        PasswordManager.resetPassword(new Servicer());
                        PasswordManager.resetPassword(new Programmer());
                        T.show("密码重置成功");
                        return true;
                    }
                });
                builder.create(new int[]{R.id.dialog_confirm_text},
                        new Object[]{confirmMessage});
                builder.show();
            }
        });

        //限制选项
        switch (userGroup) {
            case USER_CUSTOMER:
                rbCustomer.setChecked(true);
                rbServicer.setVisibility(View.INVISIBLE);
                rbProgrammer.setVisibility(View.INVISIBLE);
                btnReset.setVisibility(View.INVISIBLE);
                break;

            case USER_SERVICER:
                rbServicer.setChecked(true);
                rbProgrammer.setVisibility(View.INVISIBLE);
                btnReset.setVisibility(View.VISIBLE);
                break;

            case USER_PROGRAMMER:
                rbProgrammer.setChecked(true);
                btnReset.setVisibility(View.VISIBLE);
                break;

            default:
                break;
        }

        btnSave = (Button) findViewById(R.id.btnSavePwd);
        btnSave.setOnClickListener(new OnClickListener() {
            User user = null;

            public void onClick(View v) {
                switch (rgUserGroup.getCheckedRadioButtonId()) {
                    case R.id.rbCustomer:
                        user = new Customer();
                        break;
                    case R.id.rbServicer:
                        user = new Servicer();
                        break;
                    case R.id.rbProgrammer:
                        user = new Programmer();
                        break;
                }
                if (user == null) {
                    return;
                }
                if (!PasswordManager.modifyPassword(user,
                        etOldPwd.getText().toString(),
                        etNewPwd1.getText().toString(),
                        etNewPwd2.getText().toString())) {
                    etOldPwd.setText("");
                    etNewPwd1.setText("");
                    etNewPwd2.setText("");
                }
            }
        });


        //是否显示 新密码
        ToggleButton tbShowPwd = (ToggleButton) findViewById(R.id.tbShowPwd);
        tbShowPwd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if( !isChecked ){
                    etNewPwd1.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    etNewPwd2.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }else{
                    etNewPwd1.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    etNewPwd2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
        });
    }
}
