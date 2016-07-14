package com.feng.UserManage;

import com.feng.Utils.L;
import com.feng.RobotApplication;
import com.feng.Utils.SP;

/**
 * Created by fengscar on 2016/5/28.
 */
public class User {
    private static final String LOG = User.class.getSimpleName();

    // 给子类重载
    protected String getUserName() {
        return null;
    }

    protected String getPasswordKey() {
        return null;
    }

    protected String getDefaultPassword() {
        return null;
    }


    public String getPassword() {
        if (getPasswordKey() == null || getDefaultPassword() == null) {
            L.e(LOG, "该用户组没有实现getPasswordKey()和getDefaultPassword()");
            return null;
        }
        // 如果获取到默认值,则表示还未初始化密码 , 开始初始化,并返回初始化密码
        if (SP.get(RobotApplication.getContext(), getPasswordKey(), "").equals("")) {
            SP.put(RobotApplication.getContext(), getPasswordKey(), getDefaultPassword());
            return getDefaultPassword();
        }
        // 返回 已修改过的
        return (String) SP.get(RobotApplication.getContext(), getPasswordKey(), "");
    }

    public void setPassword(String pwd) {
        if (getPasswordKey() == null) {
            L.e(LOG, "该用户组没有实现getPasswordKey()");
        }
        SP.put(RobotApplication.getContext(), getPasswordKey(), pwd);
    }

    @Override
    public String toString() {
        return getUserName()+":"+getDefaultPassword();
    }
}
