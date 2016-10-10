package com.feng.Settings;

import com.feng.RobotApplication;
import com.feng.Utils.SP;

/**
 * Created by fengscar on 2016/9/26.
 * 保存到SP的软件设置 .
 * 要添加新成员变量时 ,使用ssp快捷键来调用Live Template来生成新代码
 * 使用: get变量名 / set变量名( value)
 */
public class AppSettings {
    //region 单例方法 , getSP/setSp
    private static AppSettings ourInstance = new AppSettings();

    public static AppSettings getInstance() {
        return ourInstance;
    }

    private AppSettings() {
    }


    // 方便一点点..
    private static Object getSP(String key, Object defaultValue) {
        return SP.get(RobotApplication.getContext(), key, defaultValue);
    }

    private static void setSP(String key, Object value) {
        SP.put(RobotApplication.getContext(), key, value);
    }
    //endregion

    /**
     * RobotID
     */
    private static final int DEFAULT_RobotID = 1;

    public static void setRobotID(int value) {
        setSP("SP_KEY_RobotID", value);
    }

    public static int getRobotID() {
        return (int) getSP("SP_KEY_RobotID", DEFAULT_RobotID);
    }


    /**
     * 机器人名称
     */
    private static final String DEFAULT_RobotName = "首松机器人";

    public static void setRobotName(String value) {
        setSP("SP_KEY_RobotName", value);
    }

    public static String getRobotName() {
        return (String) getSP("SP_KEY_RobotName", DEFAULT_RobotName);
    }


    /**
     * 主界面任务框默认列数
     */
    private static final int DEFAULT_TaskColumnNum = 3;

    public static void setTaskColumnNum(int value) {
        setSP("SP_KEY_TaskColumnNum", value);
    }

    public static int getTaskColumnNum() {
        return (int) getSP("SP_KEY_TaskColumnNum", DEFAULT_TaskColumnNum);
    }

    /**
     * 主界面任务框 排序方式( 节点类型,节点编号)
     */
    public static final String SORT_BY_TYPE = "NODE_TYPE";
    public static final String SORT_BY_ID = "NODE_ID";
    private static final String DEFAULT_SortType = SORT_BY_TYPE;

    /**
     * 设置排序方式
     *
     * @param value SORT_BY_TYPE 或 SORT_BY_ID
     */
    public static void setSortType(String value) {
        setSP("SP_KEY_SortType", value);
    }

    public static String getSortType() {
        return (String) getSP("SP_KEY_SortType", DEFAULT_SortType);
    }


    /**
     * MapMode
     */
    public static final String MODE_MAP = "MODE_MAP";
    public static final String MODE_LIST = "MODE_LIST";
    private static final String DEFAULT_MapMode = MODE_LIST;

    public static void setMapMode(String value) {
        setSP("SP_KEY_MapMode", value);
    }

    public static String getMapMode() {
        return (String) getSP("SP_KEY_MapMode", DEFAULT_MapMode);
    }

    /**
     * RobotLocation
     */
    private static final int DEFAULT_RobotLocation = -1;

    public static void setRobotLocation(int value) {
        setSP("SP_KEY_RobotLocation", value);
    }

    public static int getRobotLocation() {
        return (int) getSP("SP_KEY_RobotLocation", DEFAULT_RobotLocation);
    }

    /**
     * RobotStation
     */
    private static final int DEFAULT_RobotStation = -1;

    public static void setRobotStation(int value) {
        setSP("SP_KEY_RobotStation", value);
    }

    public static int getRobotStation() {
        return (int) getSP("SP_KEY_RobotStation", DEFAULT_RobotStation);
    }

    /**
     * IpAddress
     */
    private static final String DEFAULT_IpAddress = "192.168.1.125";

    public static void setIpAddress(String value) {
        setSP("SP_KEY_IpAddress", value);
    }

    public static String getIpAddress() {
        return (String) getSP("SP_KEY_IpAddress", DEFAULT_IpAddress);
    }

    /**
     * IpPort
     */
    private static final int DEFAULT_IpPort = 12250;

    public static void setIpPort(int value) {
        setSP("SP_KEY_IpPort", value);
    }

    public static int getIpPort() {
        return (int) getSP("SP_KEY_IpPort", DEFAULT_IpPort);
    }

    /**
     * LoginUser
     */
    public static final String USER_CUSTOMER = "普通用户";
    public static final String USER_SERVICER = "维护人员";
    public static final String USER_PROGRAMMER = "开发人员";
    private static final String DEFAULT_LoginUser = USER_CUSTOMER;

    public static void setLoginUser(String value) {
        setSP("SP_KEY_LoginUser", value);
    }

    public static String getLoginUser() {
        return (String) getSP("SP_KEY_LoginUser", DEFAULT_LoginUser);
    }

    /**
     * LoginTime
     */
    private static final long DEFAULT_LoginTime = 0;

    public static void setLoginTime(long value) {
        setSP("SP_KEY_LoginTime", value);
    }

    public static long getLoginTime() {
        return (long) getSP("SP_KEY_LoginTime", DEFAULT_LoginTime);
    }


    /**
     * MapVersion
     */
    private static final String DEFAULT_MapVersion = "1.0";

    public static void setMapVersion(String value) {
        setSP("SP_KEY_MapVersion", value);
    }

    public static String getMapVersion() {
        return (String) getSP("SP_KEY_MapVersion", DEFAULT_MapVersion);
    }

    /**
     * TaskFinishTTS  : 到点提示
     */
    private static final boolean DEFAULT_TaskFinishTTS = true;

    public static void setTaskFinishTTS(boolean value) {
        setSP("SP_KEY_TaskFinishTTS", value);
    }

    public static boolean getTaskFinishTTS() {
        return (boolean) getSP("SP_KEY_TaskFinishTTS", DEFAULT_TaskFinishTTS);
    }

    /**
     * AlarmTTS : 报警的语音提示
     */
    private static final boolean DEFAULT_AlarmTTS = true;

    public static void setAlarmTTS(boolean value) {
        setSP("SP_KEY_AlarmTTS", value);
    }

    public static boolean getAlarmTTS() {
        return (boolean) getSP("SP_KEY_AlarmTTS", DEFAULT_AlarmTTS);
    }

    /**
     * BackgroundMusic
     */
    private static final boolean DEFAULT_BackgroundMusic = true;

    public static void setBackgroundMusic(boolean value){
        setSP("SP_KEY_BackgroundMusic",value);
    }
    public static boolean getBackgroundMusic(){
        return (boolean)getSP("SP_KEY_BackgroundMusic", DEFAULT_BackgroundMusic);
    }

    /**
     *
     */

}
