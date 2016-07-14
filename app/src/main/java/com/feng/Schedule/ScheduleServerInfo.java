package com.feng.Schedule;

/**
 * Created by fengscar on 2016/5/19.
 */
public class ScheduleServerInfo {

    private String ip;

    private int port;

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    private static ScheduleServerInfo instance = null;

    public static ScheduleServerInfo getInstance() {
        if (instance == null) {

            synchronized (ScheduleServerInfo.class) {
                if (instance == null) {
                    instance = new ScheduleServerInfo();
                    //TODO 要改成在 设置界面中 获取
                    instance.setIp("192.168.1.125");
                    instance.setPort(12250);
                }
            }
        }
        return instance;
    }

}
