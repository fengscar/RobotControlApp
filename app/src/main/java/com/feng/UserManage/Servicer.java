package com.feng.UserManage;

import com.feng.Constant.I_Parameters;

/**
 * Created by fengscar on 2016/5/28.
 */
public class Servicer extends User {
    private static final String SERVICE_PASSWORD_KEY="service_password_key";
    private static final String SERVICE_DEFAULT_PASSWORD="666666";


    @Override
    protected String getUserName() {
        return I_Parameters.USER_SERVICER;
    }

    @Override
    protected String getPasswordKey() {
        return SERVICE_PASSWORD_KEY;
    }

    @Override
    protected String getDefaultPassword() {
        return SERVICE_DEFAULT_PASSWORD;
    }
}
