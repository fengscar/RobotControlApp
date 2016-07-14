package com.feng.CustomView;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;
import com.feng.Constant.I_Parameters;
import com.feng.RSS.R;
import com.feng.UserManage.*;
import com.feng.Utils.L;
import com.feng.RobotApplication;
import com.feng.Utils.SP;

import java.util.ArrayList;
import java.util.List;

public class CustomDialog extends Dialog {

    public CustomDialog(Context context) {
        super(context);
    }

    public CustomDialog(Context context, int theme) {
        super(context, theme);
    }

    /**
     * 使用步骤
     * 0. 构造函数 ,传入context
     * 1. setResourceID( ),
     * 2. setTitle( );
     * 3. setOkBtn ( 确认键ID, 对应操作),setCancelBtn( 取消键ID,对应操作)
     * 4. create( 控件ID, 对应的初始值 )
     */
    public static class Builder {
        private Context context;
        private CustomDialogCallback okBtnCallback;
        private CustomDialogCallback cancelBtnCallback;

        private TextView title;
        private Button okBtn;
        private Button cancelBtn;

        private String titleText;
        private String okBtnText;
        private String cancelBtnText;

        private View convertView;
        private CustomDialog dialog;

        private EditText[] edits;


        public Builder(Context cx) {
            this.context = cx;
            okBtnText = "编辑";
            cancelBtnText = "取消";
        }

        public View getViewByID(int id) {
            return convertView.findViewById(id);
        }

        public View getConvertView() {
            return convertView;
        }

        /**
         * 弹出警告框
         *
         * @param title      警告栏标题
         * @param confirmMsg 显示的提示信息
         * @param dcb        当 按下确定后 触发的 dcb
         */
        public CustomDialog getConfirmDialog(String title, String confirmMsg, CustomDialogCallback dcb) {
            dialog = new CustomDialog(context, R.style.RobotDialog);
            setResourceID(R.layout.dialog_confirm);
            dialog.addContentView(convertView, new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            // 绑定监听器
            setOkBtnClick(R.id.dialogOKBtn, dcb);
            setCancelBtnClick(R.id.dialogCancelBtn, null);
            // 设置 按键文本
            if (okBtn != null) {
                okBtn.setText("确定");
                okBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        // 点击后 触发 传入的 dcb :-> okBtnCallback
                        close(dialog, okBtnCallback.onDialogBtnClick((null)));
                    }
                });
            }
            if (cancelBtn != null) {
                cancelBtn.setText("取消");
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
            //设置标题栏
            setTitle(title);
            // 设置确认信息
            ((TextView) convertView.findViewById(R.id.dialog_confirm_text)).setText(confirmMsg);
            initValue(convertView, new int[]{R.id.dialogTitle, R.id.dialog_confirm_text},
                    new Object[]{title, confirmMsg});
            dialog.setContentView(convertView);
            return dialog;
        }

        public Builder setResourceID(int xmlID) {
            this.convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                    .inflate(xmlID, null);
            return this;
        }

        public Builder setOkBtnClick(int okBtnId, CustomDialogCallback dcb) {
            this.okBtn = (Button) convertView.findViewById(okBtnId);
            this.okBtnCallback = dcb;
            return this;
        }

        public Builder setCancelBtnClick(int cancelBtn, CustomDialogCallback dcb) {
            this.cancelBtn = (Button) convertView.findViewById(cancelBtn);
            this.cancelBtnCallback = dcb;
            return this;
        }

        public Builder setTitle(String str) {
            this.title = (TextView) convertView.findViewById(R.id.dialogTitle);
            if (str != null) {
                this.titleText = str;
            } else {
                this.titleText = "提示";
            }
            return this;
        }

        public Builder setButtonText(String okStr, String cancelStr) {
            if (cancelStr != null) {
                this.cancelBtnText = cancelStr;
            }
            if (okStr != null) {
                this.okBtnText = okStr;
            }
            return this;
        }

        public void show() {
            dialog.show();
        }

        /**
         * 默认的 dialog 创建方法(还未show())
         *
         * @return
         */
        public CustomDialog create(final int[] resID, Object[] initValues) {
            dialog = new CustomDialog(context, R.style.RobotDialog);
            dialog.addContentView(convertView, new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            // 初始化 title 文本
            if (title != null) {
                title.setText(titleText);
            }
            // 初始化 各项值
            initValue(convertView, resID, initValues);
            // 初始化 按键文本、 监听器
            if (okBtn != null) {
                okBtn.setText(okBtnText);
                okBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        List<View> views = new ArrayList<>();
                        if (resID != null) {
                            for (int i = 0; i < resID.length; i++) {
                                views.add(convertView.findViewById(resID[i]));
                            }
                        }
                        // DialogTool 开始调用 functionFromUI
                        close(dialog, okBtnCallback.onDialogBtnClick((views)));
                    }
                });
            }
            if (cancelBtn != null) {
                cancelBtn.setText(cancelBtnText);
                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
            }
            dialog.setContentView(convertView);
            return dialog;
        }

        /**
         * 模仿支付宝的6位密码
         */
        public CustomDialog createPasswordDialog(final Intent it) {
            dialog = new CustomDialog(context, R.style.RobotDialog);
            dialog.addContentView(convertView, new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            final int[] resID = new int[]{R.id.inputPassword1, R.id.inputPassword2, R.id.inputPassword3,
                    R.id.inputPassword4, R.id.inputPassword5, R.id.inputPassword6};
            edits = new EditText[6];

            final Spinner spUserGroup = (Spinner) convertView.findViewById(R.id.spUserGroup);
            // 自动选择上次登录的客户组
            String lastUser = (String) SP.get(context, I_Parameters.LAST_LOGIN_USER, I_Parameters.USER_CUSTOMER);
            if (lastUser.equals(I_Parameters.USER_PROGRAMMER)) {
                spUserGroup.setSelection(2);
            } else if (lastUser.equals(I_Parameters.USER_SERVICER)) {
                spUserGroup.setSelection(1);
            } else {
                spUserGroup.setSelection(0);
            }
            for (int i = 0; i < resID.length; i++) {
                edits[i] = (EditText) convertView.findViewById(resID[i]);
                final int curIndex = i;
                edits[i].addTextChangedListener(new TextWatcher() {
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    public void afterTextChanged(Editable s) {
                        if (edits[curIndex].getText().length() == 0) {
                            return;
                        }
                        int nextIndex = curIndex + 1;
                        if (nextIndex >= resID.length) {
                            StringBuffer sb = new StringBuffer();
                            try {
                                for (EditText edit : edits) {
                                    sb.append(edit.getText().toString());
                                }
                                // 获取当前选中的用户组
                                User user = null;
                                switch (spUserGroup.getSelectedItem().toString()) {
                                    case I_Parameters.USER_CUSTOMER:
                                        user = new Customer();
                                        break;
                                    case I_Parameters.USER_SERVICER:
                                        user = new Servicer();
                                        break;
                                    case I_Parameters.USER_PROGRAMMER:
                                        user = new Programmer();
                                        break;
                                    default:
                                        break;
                                }

                                // 验证 该权限的密码
                                if (user == null) {
                                    L.e("获取当前用户错误");
                                }
                                if (PasswordManager.confirmPwd(user, sb.toString())) { //验证成功
                                    it.putExtra(I_Parameters.LAST_LOGIN_USER,
                                            (String) SP.get(RobotApplication.getContext(), I_Parameters.LAST_LOGIN_USER, ""));
                                    context.startActivity(it);
                                    dialog.dismiss();
                                } else {
                                    for (EditText edit : edits) {
                                        edit.setText("");
                                    }
                                    edits[0].requestFocus();
                                }
                            } catch (Exception e) {
                                L.e("输入密码出错");
                            }
                        } else {
                            edits[nextIndex].requestFocus();
                        }
                    }
                });
                edits[i].setOnKeyListener(new View.OnKeyListener() {
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        //如果按下 删除键( 67)
                        if (keyCode == KeyEvent.KEYCODE_DEL
                                && event.getAction() == KeyEvent.ACTION_DOWN
                                //并且 不是第一个 编辑框
                                && curIndex != 0) {
                            //先清空 上个 密码小框再设置焦点
                            // ( 否则会触发 afterTextChange..)
                            edits[curIndex - 1].setText("");
                            edits[curIndex - 1].requestFocus();
                        }
                        return false;
                    }
                });
            }
            dialog.setContentView(convertView);
            return dialog;
        }


        /**
         * 初始化 dialog的view 的值
         *
         * @param view  初始化的view
         * @param resID 需要初始化的view的R.id
         * @param iv    初始化的值 (不需要初始化则为NULL)
         */
        private void initValue(View view, int[] resID, Object[] iv) {
            if (iv != null && resID != null) {
                for (int i = 0; i < iv.length; i++) {
                    if (iv[i] != null) {
                        try {
                            // 先判断 是不是switch  ( 因为 switch 是 textView子类)
                            if (view.findViewById(resID[i]) instanceof Switch) {
                                ((Switch) view.findViewById(resID[i])).setChecked((boolean) iv[i]);
                            } else if (view.findViewById(resID[i]) instanceof EditText) {
                                ((EditText) view.findViewById(resID[i])).setText(iv[i].toString());
                                // 将光标移动到最后
                                ((EditText) view.findViewById(resID[i])).setSelection(iv[i].toString().length());
                            } else if (view.findViewById(resID[i]) instanceof TextView) {
                                ((TextView) view.findViewById(resID[i])).setText(iv[i].toString());
                            } else if (view.findViewById(resID[i]) instanceof Spinner) {
                                ((Spinner) view.findViewById(resID[i])).setSelection(Integer.valueOf(String.valueOf(iv[i])));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        private void close(DialogInterface dialog, boolean isDismiss) {
            if (isDismiss == true) {
                dialog.dismiss();
            }
        }
    }
}
