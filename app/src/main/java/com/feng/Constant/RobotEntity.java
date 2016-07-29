package com.feng.Constant;

/**
 * Created by fengscar on 2016/5/28.
 * 用来描述当前系统所连接的机器人
 * 包含各类属性
 */
public class RobotEntity {
    public enum RobotState {
        MOVING, WAITING, FREE, ERROR;

        @Override
        public String toString() {
            switch (this) {
                case MOVING:
                    return "Moving";
                case WAITING:
                    return "Waiting";
                case FREE:
                    return "Free";
                case ERROR:
                    return "Error";
            }
            return null;
        }

        public int toInt() {
            switch (this) {
                case MOVING:
                    return 1;
                case WAITING:
                    return 2;
                case FREE:
                    return 3;
                case ERROR:
                    return 4;
            }
            return 0;
        }
    }

    private final String mDeviceName = "ROBOT";

    private String mRobotID; //机器人编号
    private String mRobotName; //机器人名称

    private boolean mIsLogin;

    private int mLocation; //当前位置
    private RobotState mState;  //当前状态
    private int mSpeed;  //当前速度
    private String[] mWarnings;  // 报警信息
    private int[] mTasks; // 当前已选任务
    private int[] mPaths; // 当前路径(前往下个节点的)


    public boolean isLogin() {
        return mIsLogin;
    }

    public void setLogin(boolean login) {
        mIsLogin = login;
    }

    public String getRobotID() {
        return mRobotID;
    }

    public void setRobotID(String robotID) {
        mRobotID = robotID;
    }

    public String getDeviceName() {
        return mDeviceName;
    }

    public String getRobotName() {
        return mRobotName;
    }

    public void setRobotName(String robotName) {
        mRobotName = robotName;
    }


    public int getLocation() {
        return mLocation;
    }

    public void setLocation(int location) {
        mLocation = location;
    }

    public RobotState getState() {
        return mState;
    }

    public void setState(RobotState state) {
        mState = state;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public void setSpeed(int speed) {
        mSpeed = speed;
    }

    public String[] getWarnings() {
        return mWarnings;
    }

    public void setWarnings(String[] warnings) {
        mWarnings = warnings;
    }

    public int[] getTasks() {
        return mTasks;
    }

    public void setTasks(int[] tasks) {
        mTasks = tasks;
    }

    public int[] getPaths() {
        return mPaths;
    }

    public void setPaths(int[] paths) {
        mPaths = paths;
    }

    private static RobotEntity instance = null;

    private RobotEntity() {
        mState = RobotState.FREE;
    }

    public static RobotEntity getInstance() {
        if (instance == null) {
            synchronized (RobotEntity.class) {
                instance = new RobotEntity();
            }
        }
        return instance;
    }
}
