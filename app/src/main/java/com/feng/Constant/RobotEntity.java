package com.feng.Constant;

import com.feng.Database.Node;

import java.util.List;

/**
 * Created by fengscar on 2016/5/28.
 * 用来描述当前系统所连接的机器人
 * 包含各类属性
 */
public class RobotEntity {
    public interface RobotState{
        String MOVING ="Moving";
        String WAITING="Waiting";
        String FREE="Free";
        String ERROR="Error";
    }

    private final String mDeviceName = "ROBOT";

    private String mRobotID; //机器人编号
    private String mRobotName; //机器人名称

    private boolean mIsLogin;

    private Node mLocation; //当前位置
    private String mState;  //当前状态
    private int mSpeed;  //当前速度
    private String[] mWarnings;  // 报警信息
    private List<Node> mTasks; // 当前已选任务
    private List<Node> mPaths; // 当前路径(前往下个节点的)


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

    public List<Node> getTasks() {
        return mTasks;
    }

    public void setTasks(List<Node> tasks) {
        mTasks = tasks;
    }

    public Node getLocation() {
        return mLocation;
    }

    public void setLocation(Node location) {
        mLocation = location;
    }

    public String getState() {
        return mState;
    }

    public void setState(String state) {
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

    public List<Node> getPaths() {
        return mPaths;
    }

    public void setPaths(List<Node> paths) {
        mPaths = paths;
    }

    private static RobotEntity instance = null;

    private RobotEntity() {
        mLocation=null;


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
