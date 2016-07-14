package com.feng.UserManage;

import com.feng.Constant.I_Parameters;

/**
 * Created by fengscar on 2016/5/31.
 */
public class Customer extends User {
    private static final String CUSTOMER_PASSWORD_KEY = "customer_password_key";
    private static final String CUSTOMER_DEFAULT_PASSWORD = "000000";


    @Override
    protected String getPasswordKey() {
        return CUSTOMER_PASSWORD_KEY;
    }

    @Override
    protected String getDefaultPassword() {
        return CUSTOMER_DEFAULT_PASSWORD;
    }
    @Override
    protected String getUserName() {
        return I_Parameters.USER_CUSTOMER;
    }

}
