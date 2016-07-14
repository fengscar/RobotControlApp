package com.feng.UserManage;

import android.content.Context;
import android.content.Intent;
import com.feng.Activities.MenuActivity;
import com.feng.Constant.I_Parameters;
import com.feng.RSS.R;
import com.feng.CustomView.CustomDialog;
import com.feng.Utils.L;
import com.feng.RobotApplication;
import com.feng.Utils.SP;
import com.feng.Utils.T;

public class PasswordManager implements I_Parameters {
    private final static String LOG = PasswordManager.class.getSimpleName();


    private PasswordManager() {
        //构造函数 逻辑处理
    }
//    private volatile static PasswordManager instance;

//    public static PasswordManager getInstance() {
//        //第一次判断是否为空
//        if (instance == null) {
//            synchronized (PasswordManager.class) {//锁
//                //第二次判断是否为空 多线程同时走到这里的时候，需要这样优化处理
//                if (instance == null) {
//                    instance = new PasswordManager();
//                }
//            }
//        }
//        return instance;
//    }

    public static boolean confirmPwd(User user, String inputPwd) {
        // 获取SP中保存的密码
        String userPwd = user.getPassword();

        L.i(LOG, "开始验证密码:" + user.getUserName() + ", " + inputPwd);

        //进行 密码验证
        if (!userPwd.equals(inputPwd)) {
            T.show("密码有误,请重新输入");
        } else {
            T.show("密码验证通过");
            // 保存登录时间
            SP.put(RobotApplication.getContext(), LAST_LOGIN_TIME, System.currentTimeMillis());
            // 保存登录用户组
            SP.put(RobotApplication.getContext(), LAST_LOGIN_USER, user.getUserName());
            return true;
        }
        return false;
    }

    public static boolean modifyPassword(User user, String old, String new1, String new2) {
        // 验证 是否完整输入了3个编辑框
        if (old.equals("") || new1.equals("") || new2.equals("")) {
            T.show("请完整输入");
            return false;
        }

        if (new1.equals(new2) == false) {   // 验证2次密码是否相同,不同则弹出Toast提示
            T.show("两次输入的新密码不同");
        } else if (new1.length() != 6) {    // 验证新密码长度是否足够6位
            T.show("请输入正好6位的新密码");
        } else if (new1.equals(old)) {
            T.show("新旧密码相同");
        } else {
            // SP中保存的密码
            String oldpwd = user.getPassword();
            if (oldpwd == null) {
                T.show("获取该用户初始密码失败");
                return false;
            }
            if (!oldpwd.equals(old)) {
                T.show("原始密码错误");
                return false;
            } else {
                user.setPassword(new1);
                T.show("密码修改成功");
                return true; // 正确后进入修改并保存 并关闭dialog
            }
        }
        return false;
    }
    private static boolean needConfirm() {
        long nowTime = System.currentTimeMillis();// 获取当前时间
        // 获取上次通过验证的时间 如果为首次验证,则获取到1225
        Long lastConfirmTime = (Long) SP.get(RobotApplication.getContext(), LAST_LOGIN_TIME, Long.valueOf(1225));
        if (lastConfirmTime != 1225 && nowTime - lastConfirmTime < 18000 * 1000) {
            //并非首次登陆(!=1225),并且时间未超时(<180s)
            //重新 刷新 登录时间
            SP.put(RobotApplication.getContext(), LAST_LOGIN_TIME, nowTime);
            return false;
        } else {
            return true;
        }
    }


    public static void login(Context activityContext){
        Intent it=new Intent();
        it.setClass(activityContext, MenuActivity.class);
        it.putExtra(LAST_LOGIN_USER, (String) SP.get(RobotApplication.getContext(), LAST_LOGIN_USER, ""));
        // 如果不需要验证登录信息,直接跳转到目录
        if( !needConfirm() ){
            activityContext.startActivity(it);
        }else{
            CustomDialog.Builder builder = new CustomDialog.Builder(activityContext);
            builder.setResourceID(R.layout.dialog_input_password);
            builder.createPasswordDialog(it);
            builder.show();
        }
    }

    public static void resetPassword(User user){
        user.setPassword(user.getDefaultPassword());
    }
}
