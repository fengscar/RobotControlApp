package com.feng.UserManage;

import com.feng.Constant.I_Parameters;

/**
 * Created by fengscar on 2016/5/31.
 */
public class Programmer extends User{
    private static final String PROGRAMMER_PASSWORD_KEY="programmer_password_key";
    private static final String PROGRAMMER_DEFAULT_PASSWORD="041225";


    @Override
    protected String getPasswordKey() {
        return PROGRAMMER_PASSWORD_KEY;
    }

    @Override
    protected String getDefaultPassword() {
        return PROGRAMMER_DEFAULT_PASSWORD;
    }

    @Override
    protected String getUserName() {
        return I_Parameters.USER_PROGRAMMER;
    }
}
