package com.feng.Schedule;

import android.util.Log;
import com.feng.Constant.I_Parameters;
import com.feng.RobotApplication;
import com.feng.Utils.SP;

/**
 * 该类 的get、set 直接操作SP中的数据
 * Created by fengscar on 2016/5/19.
 */
public class ScheduleServerInfo {
    private final static String TAG = ScheduleServerInfo.class.getSimpleName();

    public String getIp() {
        return (String) SP.get(RobotApplication.getContext(), I_Parameters.IP_ADDRESS, "192.168.1.125");
    }

    public int getPort() {
        return (int) SP.get(RobotApplication.getContext(), I_Parameters.IP_PORT, 12250);
    }

    public void setIp(String ip) {
        if (ip == null) {
            Log.e(TAG, "setIp: IP地址不能为NULL");
            return;
        }
        SP.put(RobotApplication.getContext(), I_Parameters.IP_ADDRESS, ip);
    }

    public void setPort(int port) {
        SP.put(RobotApplication.getContext(), I_Parameters.IP_PORT, port);
    }

    private static ScheduleServerInfo instance = null;

    public static ScheduleServerInfo getInstance() {
        if (instance == null) {
            synchronized (ScheduleServerInfo.class) {
                if (instance == null) {
                    instance = new ScheduleServerInfo();
                }
            }
        }
        return instance;
    }
}
