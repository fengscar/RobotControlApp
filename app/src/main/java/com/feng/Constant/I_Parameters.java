package com.feng.Constant;

import com.feng.RSS.R;

public interface I_Parameters {
    /**
     * handler. msg.what
     */
    int REFRESH = 120;
    int REFRESH_ALL = 110;
    int STATE_CHANGE = 119;
    int REFRESH_USB = 122;
    int PREPARE_EXIT = 123;

    /**
     * 节点类型 int
     * 以及mapfragment按键背景
     * 以及ListingFragment ICON图标
     */
    class NODE_TYPE {
        public static final String TABLE = "工作台";
        public static final String CROSS = "交叉点";
        public static final String KITCHEN = "厨房";
        public static final String WASH = "消洗区";
    }

    class PATH_TURN {
        public static final String STRAIGHT = "不转向";
        public static final String LEFT = "左转";
        public static final String RIGHT = "右转";
    }

    /*
     *  3中权限
     */
    String USER_CUSTOMER = "普通用户";
    String USER_SERVICER = "维护人员";
    String USER_PROGRAMMER = "开发人员";

    /**
     * SP 中的 属性名
     */
    String IS_MAP_MODEL = "mapModel";
    String AUDIO_POINT = "pointAudio";
    String AUDIO_ALARM = "alarmAudio";
    String AUDIO_BGM = "backgroundAudio";

    String COLUMN_NUM = "columnNum";
    String SORT_TYPE = "sort_type";
    //保存 上次执行到的位置
    String CURRENT_NODE_ID = "current_node_id";
    // 未完成的任务
    String TASK_LIST = "task_list";
    //直接进入 上次退出的工作区
    String AUTO_ENTER_WORKSPACE = "auto_enter_workspace";
    // IP地址
    String IP_ADDRESS = "ipAddress";
    String IP_PORT = "ipPort";

    String ROBOT_ID = "RobotID";
    String ROBOT_NAME = "RobotName";

    //用户密码管理
    String LAST_LOGIN_USER = "last_login_user";
    String LAST_LOGIN_TIME = "last_login_time";

    String MAP_VERSION = "map_version";

    /**
     * 进度条样式 1 , 2 , 3 , 4 , ...
     */
    class HorizontalNumberSeekerbar {
        public static int ONE = R.drawable.red;
        public static int TWO = R.drawable.blue;
        public static int THREE = R.drawable.yellow;
        public static int FOUR = R.drawable.green;
    }

    // 每个包发3次
    int SEND_REPEAT_COUNT = 3;
    //每个包发送间隔  单位 毫秒
    int SEND_TIMEOUT = 300;
    // 心跳包 间隔周期  单位 毫秒
    int HEART_BEAT_PERIOD = 15000;
    //USB检测间隔
    int USB_CONNECT_TIMEOUT = 10000;
    //召唤系统默认端口
    int COR_PORT = 12250;  // 召唤系统服务器端口的默认值


    /**
     * 语音识别的ACTION
     */
    String UNIFORM_TTS = "UNIFORM_TTS";

    String IAT_START_LISTENNING = "开始识别";
    String IAT_STOP_LISTENNING = "停止识别";
    String TTS_START_SPEAK = "开始语音合成";


}
