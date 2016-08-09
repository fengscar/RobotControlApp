package com.feng.Schedule;

import com.feng.Utils.Transfer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fengscar on 2016/5/28.
 * 用来描述当前系统所连接的机器人 (用于与调度系统通信)
 * 包含各类属性
 */
public class ScheduleRobot implements IUpdateStatus, ScheduleProtocal {
    public interface IRobotStatusChangeListener {
        void onStatusChange(IUpdateStatus robot);
    }

    private IRobotStatusChangeListener mIRobotStatusChangeListener;

    public void setIRobotStatusChangeListener(IRobotStatusChangeListener IRobotStatusChangeListener) {
        mIRobotStatusChangeListener = IRobotStatusChangeListener;
    }

    //region 提供方法来监听是否状态发生改变
    private static boolean isChanged = false;

    public void beginNotifyChange() {
        isChanged = false;
    }

    public void endNotifyChange() {
        if (isChanged && mIRobotStatusChangeListener != null) {
            mIRobotStatusChangeListener.onStatusChange(this);
        }
    }
    //endregion

    public final static String DEVICE_ROBOT = "ROBOT";


    //region Singleton
    private static ScheduleRobot instance = null;

    private ScheduleRobot() {
        mLocation = -1;
        mMotionState = MotionState.STOP;
        mSpeed = 0;
        mWarnings = new ArrayList<>();
        mPower = 0;
        mScheduleMove = true;
    }

    public static ScheduleRobot getInstance() {
        if (instance == null) {
            synchronized (ScheduleRobot.class) {
                instance = new ScheduleRobot();
            }
        }
        return instance;
    }

    //endregion
    public enum MotionState {
        STOP(0x00), STRAIGHT(0x01), TURN_ORI(0x02), TURN_ARC(0x03), ERROR(0x04);
        private int mIndex;

        MotionState(int b) {
            mIndex = b;
        }

        public int getIndex() {
            return mIndex;
        }

        public static MotionState getState(int b) {
            for (MotionState p : MotionState.values()) {
                if (p.mIndex == b) {
                    return p;
                }
            }
            return STOP;
        }
    }

    //region 调度相关的状态
    private String mRobotID; //机器人编号
    private String mRobotName; //机器人名称
    private boolean mIsLogin;
    //endregion

    //region Robot由Arm获取的状态
    private int mLocation; //当前位置
    private MotionState mMotionState;  //当前状态
    private int mSpeed;  //当前速度
    private List<String> mWarnings;  // 报警信息
    private int[] mTasks; // 当前已选任务
    private int[] mPaths; // 当前路径(前往下个节点的)
    private int mPower; //电量百分比
    private boolean mScheduleMove; // 调度控制状态
    //endregion

    //region Getter
    public boolean isLogin() {
        return mIsLogin;
    }

    public String getRobotID() {
        return mRobotID;
    }


    public String getRobotName() {
        return mRobotName;
    }

    public void setLogin(boolean login) {
        mIsLogin = login;
    }

    public void setRobotID(String robotID) {
        mRobotID = robotID;
    }

    public void setRobotName(String robotName) {
        mRobotName = robotName;
    }


    public int getLocation() {
        return mLocation;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public List<String> getWarnings() {
        return mWarnings;
    }

    public int[] getTasks() {
        return mTasks;
    }

    public int[] getPaths() {

        return mPaths;
    }

    public int getPower() {
        return mPower;
    }

    public boolean isScheduleMove() {
        return mScheduleMove;
    }

    public MotionState getMotionState() {
        return mMotionState;
    }

    //endregion

    //region Setter
    public void setLocation(int location) {
        isChanged = location != mLocation;
        mLocation = location;
    }

    public void setMotionState(MotionState motionState) {
        isChanged = mMotionState != motionState;
        mMotionState = motionState;
    }


    public void setSpeed(int speed) {
        isChanged = speed != mSpeed;
        mSpeed = speed;
    }


    public void addWarning(String warning) {
        if (!mWarnings.contains(warning)) {
            mWarnings.add(warning);
            isChanged = true;
        }
    }

    public void setTasks(int[] tasks) {
        isChanged = !compare2Int(tasks, mTasks);
        mTasks = tasks;
    }


    public void setPaths(int[] paths) {
        isChanged = !compare2Int(paths, mPaths);
        mPaths = paths;
    }


    public void setPower(int power) {
        isChanged = power != mPower;
        mPower = power;
    }

    public void setScheduleMove(boolean scheduleMove) {
        isChanged = scheduleMove != mScheduleMove;
        mScheduleMove = scheduleMove;
    }
    //endregion

    private boolean compare2Int(int[] b1, int[] b2) {
        if (b1 == null && b2 == null) {
            return true;
        }
        if (b1 == null || b2 == null || b1.length != b2.length) {
            return false;
        } else {
            for (int i = 0; i < b1.length; i++) {
                if (b1[i] != b2[i]) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public JSONObject getRobotStatusJSON() {
        try {
            /**
             * 生成JSON 对象
             */
            JSONObject result = new JSONObject();
            result.put(LOCATION, mLocation);
            result.put(MOTION_STATE, mMotionState.getIndex());
            result.put(SCHEDULE_MOVE, mScheduleMove);
            result.put(SPEED, mSpeed);
            result.put(POWER, mPower);

            if (mTasks == null) {
                result.put(TASKS, null);
            } else {
                JSONArray pathsJsonArray = new JSONArray();
                for (int taskID : mTasks) {
                    pathsJsonArray.put(taskID);
                }
                result.put(TASKS, pathsJsonArray);
            }

            if (mPaths == null) {
                result.put(PATHS, null);
            } else {
                JSONArray tasksJsonArray = new JSONArray();
                for (int id : mPaths) {
                    tasksJsonArray.put(id);
                }
                result.put(PATHS, tasksJsonArray);
            }

            if (mWarnings == null || mWarnings.size() == 0) {
                result.put(WARNINGS, null);
            } else {
                JSONArray warningJsonArray = new JSONArray();
                for (String warning : mWarnings) {
                    warningJsonArray.put(warning);
                }
                result.put(WARNINGS, warningJsonArray);
            }
            return result;
        } catch (NullPointerException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateMotionStatusFromArm(byte[] body) {
        if (body != null && body.length == 8) {
            Transfer mTransfer = new Transfer();
            setMotionState(MotionState.getState(body[0]));
            setSpeed(mTransfer.twoByteToInt(new byte[]{body[1], body[2]}));
            setScheduleMove(body[7] == 0x01);
        }
    }


}
