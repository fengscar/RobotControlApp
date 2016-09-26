package com.feng.Schedule;

import com.feng.Constant.I_Parameters;
import com.feng.Database.Map.MapDatabaseHelper;
import com.feng.Database.Map.Node;
import com.feng.RobotApplication;
import com.feng.Utils.SP;
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
public class Robot implements IUpdateStatus, ScheduleProtocal {

    private RobotComparer mComparer;

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
    private static Robot instance = null;

    private Robot() {
        mComparer = new RobotComparer();

        mLocation = -1;
        mMotionState = MotionState.STOP;
        mSpeed = 0;
        mTasks = new ArrayList<>();
        mPaths = new ArrayList<>();
        mWarnings = new ArrayList<>();
        mPower = 0;
        mScheduleMove = true;
    }

    public static Robot getInstance() {
        if (instance == null) {
            synchronized (Robot.class) {
                instance = new Robot();
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
    private List<Integer> mTasks; // 当前已选任务
    private List<Integer> mPaths; // 当前路径(前往下个节点的)
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
        // 如果当前位置为-1 (刚初始化) ,尝试从 STATION_NODE_ID 和 CURRENT_NODE_ID中寻找..
        if (mLocation == -1) {
            int spLocation = (int) SP.get(RobotApplication.getContext(), I_Parameters.CURRENT_NODE_ID, -1);
            Node spNode = MapDatabaseHelper.getInstance().getNodeByID(spLocation);
            if (spNode != null) {
                return spNode.getId();
            }

            int spStation = (int) SP.get(RobotApplication.getContext(), I_Parameters.STATION_NODE_ID, -1);
            Node spNodeStation = MapDatabaseHelper.getInstance().getNodeByID(spStation);
            if (spNodeStation != null) {
                return spNodeStation.getId();
            }
        }
        return mLocation;
    }

    public int getSpeed() {
        return mSpeed;
    }

    public List<String> getWarnings() {
        return mWarnings;
    }

    public List<Integer> getTasks() {
        return mTasks;
    }

    public Integer[] getTasksArray() {
        Integer[] taskArray = new Integer[mTasks.size()];
        return mTasks.toArray(taskArray);
    }

    public List<Integer> getPaths() {
        return mPaths;
    }

    public Integer[] getPathsArray() {
        Integer[] pathArray = new Integer[mPaths.size()];
        return mPaths.toArray(pathArray);
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
        // 保存到SP中
        SP.put(RobotApplication.getContext(), I_Parameters.CURRENT_NODE_ID, location);
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

    public void setTasks(List<Integer> tasks) {
        isChanged = !mComparer.compare2List(tasks, mTasks);
        mTasks = tasks;
    }


    public void setPaths(List<Integer> paths) {
        isChanged = !mComparer.compare2List(paths, mPaths);
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

    //region 额外的方法
    public boolean hasTask() {
        return mTasks != null && mTasks.size() != 0;
    }

    public boolean hasTask(int nodeID) {
        return mTasks.contains(nodeID);
    }

    // 返回是否删除任务成功
    public boolean delTask(int nodeID) {
        isChanged = mTasks.remove((Integer.valueOf(nodeID)));
        return isChanged;
    }

    public boolean addTask(int nodeID) {
        if (hasTask(nodeID)) {
            return false;
        }
        mTasks.add(nodeID);
        return isChanged = true;
    }

    public void clearTask() {
        isChanged = true;
        mTasks.clear();
    }
    //endregion


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
