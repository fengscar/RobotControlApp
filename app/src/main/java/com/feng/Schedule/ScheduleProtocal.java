package com.feng.Schedule;

public interface ScheduleProtocal {
    String METHOD="Method"; //请求方法
    String PARAM="Param";   //客户端请求
    String RESULT="Result"; //服务端结果

    // 具体的处理方法( json method name) 以及字段名称
    String LOGIN = "Login";
    String DEVICE="Device";
    String ROBOT_ID="RobotID";
    String ROBOT_NAME="RobotName";
    String SUCCESS="Success";
    String EXPLAIN="Explain";

    String HEART_BEAT = "HeartBeat";

    String UPDATE_MAP = "UpdateMap";
    String SYNC_MAP="SyncMap";

    String MAP_VERSION="MapVersion";
    String MAP_DATA="MapData";


    String UPDATE_STATUS = "UpdateStatus";
    String LOCATION = "Location";
    String MOTION_STATE = "MotionState";
    String SCHEDULE_MOVE = "ScheduleMove";
    String SPEED = "Speed";
    String WARNINGS = "Warnings";
    String TASKS = "Tasks";
    String PATHS = "Paths";
    String POWER = "Power";


    String PUSH_COMMAND="PushCommand";
    String MOVE="Move";

    String PUSH_TASK="PushTask";
    String ADD_TASKS="AddTasks";
    String DEL_TASKS="DelTasks";

    /**
     * 调度系统发来的控制运动指令
     */
    interface RobotCommand {
       String COMMAND_MOVE = "Move";
       String COMMAND_STOP = "Stop";
    }
}


