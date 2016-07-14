package com.feng.Constant;

import java.util.HashMap;

public interface ArmProtocol {
    /**
     * 协议的 格式
     * [机器人编号]+[模块编号]+[命令]+[数据长度]+[数据]+[校验位]
     */
    int ROBOT = 0;
    int MODULE = 1;
    int COMMAND = 2;
    int DATA_LENGTH = 3;
    int DATA = 4;
    byte ROBOT_ID=0x01;
    /**
     * intent 包含的 头byte 的key
     * intent.putExtra( UNIFORM_HEAD,  {0x01,0x02...} )
     */
    String UNIFORM_SEND = "Uniform_SEND";
    String UNIFORM_RECEIVE = "UNIFORM_RECEIVE";
    //作为服务器...指定发送的客户端ID
    String UNIFORM_CLIENT_ID = "cor_client_id";
    /**
     * 	 byte [] 为: 机器人 <-> 控制板的 传输协议
     *   机器人 R ->  控制板 C 的  命令 以  set / query / update 为开头
     *   控制板 C ->  机器人 R 的 命令 无前缀
     *
     *   String 为 平板中广播的传输 协议
     * @ARMClientService 监听 action为该值的广播,
     * 当接收到(用户按下后发出的)广播后,执行对应的操作(发送命令给ARM)
     */
	/*   运动模块    */
    /**
     * 查询运动状态  null
     * replyID:11
     **/
    byte[] QueryState = {ROBOT_ID, 0x01, 0x01};
    String QUERY_STATE = "查询当前状态";
    /**
     * 设置执行运动   0x00 不运动 0x01 运动
     * *  replyID:12
     **/
    byte[] SetMoving = {ROBOT_ID, 0x01, 0x02};
    String SET_MOVING = "设置运动";

    /**
     * 设置运动  0x00  pause  0x01 forward 0x02 back
     * replyID:13
     **/
    byte[] SetWalk = {0x01, 0x01, 0x03};
    String SET_WALK = "设置前进后退停止";

    /**
     * 设置目的地是否转向
     */
    byte[] SetTurn = {0x01, 0x01, 0x04};
    String SET_TURN = "设置目的地是否转向";

    /**
     * 以下是 设置ARM参数的 MOTION
     */
    byte[] ArmMotionOriginTurnSpeed = {0x01, 0x01, 0x05};
    byte[] ArmMotionStartSpeed = {0x01, 0x01, 0x06};
    byte[] ArmMotionIncrementSpeed = {0x01, 0x01, 0x07};
    byte[] ArmMotionMaxSpeed = {0x01, 0x01, 0x08};
    byte[] ArmMotionTurnSpeed = {0x01, 0x01, 0x09};
    byte[] ArmMotionUltraSpeed = {0x01, 0x01, 0x0A};
    byte[] ArmMotionReadCardSpeed = {0x01, 0x01, 0x0B};
    String ARM_MOTION_ORIGIN_TURN_SPEED = "ARM_MOTION_ORIGIN_TURN_SPEED";
    String ARM_MOTION_START_SPEED = "ARM_MOTION_START_SPEED";
    String ARM_MOTION_INCREMENT_SPEED = "ARM_MOTION_INCREMENT_SPEED";
    String ARM_MOTION_MAX_SPEED = "ARM_MOTION_MAX_SPEED";
    String ARM_MOTION_TURN_SPEED = "ARM_MOTION_TURN_SPEED";
    String ARM_MOTION_ULTRA_SPEED = "ARM_MOTION_ULTRA_SPEED";
    String ARM_MOTION_READ_CARD_SPEED = "ARM_MOTION_READ_CARD_SPEED";

    /**
     * 开始自检
     */
    byte[] SelfCheckMotion90 = {0x01, 0x01, 0x0C};
    String SELF_CHECK_MOTION90 = "运动模块开始90度自检";

    byte[] SelfCheckMotion180 = {0x01, 0x01, 0x0D};
    String SELF_CHECK_MOTION180 = "运动模块开始180度自检";
    /**
     * 上报 移动距离 溢出
     */
    byte[] MoveDistanceOverflow = {0x01, 0x01, 0x0E};
    String MOVE_DISTANCE_OVERFLOW = "移动距离溢出警报";
    /**
     * 上报 长时间无人操作
     */
    byte[] LongTimeNotOperate = {0x01, 0x01, 0x0F};
    String LONG_TIME_NOT_OPERATE = "长时间无人操作";


    /*   路径算法    */
    // 查询路线版本号
    byte[] ArmRouteQueryVersion = {0x01, 0x02, 0x01};
    String ARM_ROUTE_QUERY_VERSION = "ARM_ROUTE_QUERY_VERSION";
    // 查询路线/节点/目的最大数量

    byte[] ArmRouteQueryAll = {0x01, 0x02, 0x02};
    String ARM_ROUTE_QUERY_ALL = "ARM_ROUTE_QUERY_ALL";
    /**
     * 查询卡信息  0xID低字节 0xID高字节
     **/
    byte[] QueryCard = {0x01, 0x02, 0x03};
    String QUERY_CARD = "查询卡信息";
    /**
     * 查询目的卡信息 null
     **/
    byte[] QueryTarget = {0x01, 0x02, 0x04};
    String QUERY_TARGET = "刷新任务列表";
    /**
     * 修改卡信息  节点结构体 ( 12byte 0x0c )
     */
    byte[] UpdateCard = {0x01, 0x02, 0x05};
    String UPDATE_CARD = "修改卡信息";
    /**
     * 设置目的卡  卡数量n + 0x卡1低位 0x卡1高位
     */
    byte[] AddTargets = {0x01, 0x02, 0x06};
    String ADD_TARGETS = "添加目的";
    byte[] DelTargets = {0x01, 0x02, 0x07};
    String DEL_TARGETS = "删除目的";

    byte[] SetPriority = {0x01, 0x02, 0x08};
    String SET_PRIORITY = "设置路线优先级";
    /**
     * 下发路径信息
     * 1、0x01 路线，0x02节点
     * 2、总包数
     * 3、包序号
     * 其他：节点结构体1 节点结构体2
     * replyID:26 27
     */
    byte[] SendMap = {0x01, 0x02, 0x09};
    byte[] SendRouteReply = {0x26};
    byte[] SendNodeReply = {0x27};
    String SEND_MAP = "下发地图信息";
    /**
     * 上传路径信息 null
     * replyID:28
     */
    byte[] UploadMap = {0x01, 0x02, 0x0A};
    String UPLORD_MAP = "上传地图信息";
    /**
     * 回复上传信息
     * 1、0x01 路线，0x02节点
     * 2、包序号；
     * 3、0x00:失败 0x01:成功"
     * replyID:28
     */
    byte[] ReplyUploadMap = {0x01, 0x02, 0x0B};
    String REPLY_UPLORD_MAP = "回复上传地图";


    // 红外避障
    byte[] SelfCheckBarrier = {0x01, 0x03, 0x01};
    String SELF_CHECK_BARRIER = "红外模块自检开始";
    // 查询障碍物情况 障碍物方位 0x01-0x07
    byte[] BarrierWarning = {0x01, 0x03, 0x02};
    String BARRIER_WARNING = "红外避障警报";

    // RFID检测
    /**
     * 查询当前读到的RFID卡
     * replyID:41
     */
    byte[] SelfCheckRFID = {0x01, 0x04, 0x01};
    String SELF_CHECK_RFID = "读卡模块开始自检";

    byte[] CurrentRFID = {0x01, 0x04, 0x02};
    String CURRENT_RFID = "读到RFID卡";

    /**
     * 当前漏 读 卡信息
     */
    byte[] MissingRFID = {0x01, 0x04, 0x03};
    String MISSING_RFID = "漏读RFID卡";

    /**
     * 读到 不该读到的RFID
     */
    byte[] WrongRFID = {0x01, 0x04, 0x04};
    String WRONG_RFID = "读到错误的RFID卡";


	/* * 磁传感器    */
    /**
     * 查询磁传感器位置
     */
    byte[] QueryMagnetic = {0x01, 0x05, 0x01};
    String QUERY_MAGNETIC = "查询磁感应位置";
    // 设置感应数
    byte[] ArmMagMax = {0x01, 0x05, 0x02};
    String ARM_MAG_MAX = "ARM_MAG_MAX";

    byte[] SelfCheckMag = {0x01, 0x05, 0x03};
    String SELF_CHECK_MAG = "磁传感器开始自检";

    /**
     * 为读到 磁条 警报
     */
    byte[] MissingMagnetic = {0x01, 0x05, 0x04};
    String MISSING_MAGNETIC = "未读到磁条警报";

    byte[] SelfCheckMagPosition = {0x01, 0x05, 0x05};
    String SELF_CHECK_MAG_POSITION = "Mag_Position";

    // 超声波检测
    // ARM setting distance of ultra..
    byte[] ArmUltraWarningDistance = {0x01, 0x06, 0x01};
    String ARM_ULTRA_WARNING_DISTANCE = "ARM_ULTRA_WARNING_DISTANCE";

    byte[] SelfCheckUltra = {0x01, 0x06, 0x02};
    String SELF_CHECK_ULTRA = "超声模块开始自检";

    /**
     * 上传超声波报警
     */
    byte[] UltrasonicWarning = {0x01, 0x06, 0x03};
    String ULTRASONIC_WARNING = "超声波警报";

	/* * 电源模块  */
    /**
     * 查询电量  null
     * replyID:71
     */
    byte[] QueryPower = {0x01, 0x07, 0x01};
    String QUERY_POWER = "查询电量"; // 本机发送

    byte[] QueryPowerNode = {0x01, 0x07, 0x02};
    String QUERY_POWER_NOTE = "查询电量记录"; // 本机发送


    /**
     * 充电状态
     */
    byte[] PowerState = {0x01, 0x07, 0x03};
    String POWER_STATE = "电量状态";  // ARM 回复
    String POWER_NOT_CHARGING = "耗电中";
    String POWER_CHARGING = "充电中";
    String POWER_NOT_ENOUGH = "电池电量不足";
    String POWER_EMPTY = "电池电量耗尽";

    //人体红外模块
    byte[] SelfCheckInfrared = {0x01, 0x08, 0x01};
    String SELF_CHECK_INFRARED = "人体红外开始自检";

    byte[] InfraredWarning = {0x01, 0x08, 0x02};
    String INFRARED_WARNING = "人体红外警报";

    //外设按键模块
    byte[] SelfCheckMachineBtn = {0x01, 0x0B, 0x01};
    String SELF_CHECK_MACHINE_BTN = "自检外设按键模块";

    byte[] MachineStartBtn = {0x01, 0x0B, 0x02};
    String MACHINE_START_BTN = "点击执行按键";

    byte[] MachineStopBtn = {0x01, 0x0B, 0x03};
    String MACHINE_STOP_BTN = "点击制动按键";

    // 系统信息 模块
    byte[] QuerySoftVersion = {0x01, 0x50, 0x01};
    String QUERY_SOFT_VERSION = "查询软件版本";

    byte[] QueryUsbState = {0x01, 0x50, 0x02};
    String QUERY_USB_STATE = "查询联网状态";
    String CONNECT_STATE = "网络连接状态";

    byte[] ArmSysRobotID = {0x01, 0x50, 0x03};
    String ARM_SYS_ROBOT_ID = "ARM_SYS_ROBOT_ID";

    byte[] ArmSysSendPeriod = {0x01, 0x50, 0x04};
    String ARM_SYS_SEND_PERIOD = "ARM_SYS_SEND_PERIOD";

    byte[] ArmSysSendCount = {0x01, 0x50, 0x05};
    String ARM_SYS_SEND_COUNT = "ARM_SYS_SEND_COUNT";

    byte[] ArmSysRest = {0x01, 0x50, 0x06};
    String ARM_SYS_RESET = "Arm_SYS_RESET";

    byte[] SelfCheckStartStop = {0x01, 0x50, 0x07};
    String SELF_CHECK_START_STOP = "开启/退出自检";

    byte[] SetRobotReboot = {0x01, 0x50, 0x08};
    String SET_ROBOT_REBOOT = "设置下位机重启";

    //来自ARM的错误代码
    byte[] ErrorFromArm = {0x01, 0x50, 0x10};
    String ERROR_FROM_ARM = "ARM错误命令";
    //机器人重启
    byte[] RobotReboot = {0x01, 0x50, 0x50};
    String ROBOT_REBOOT = "机器人重启";

    String UNKNOWN = "未知的ACTION";
    /**
     * 本机产生的ACTIONS
     */
    String SOCKET_CONNECT_SUCCESS = "SOCKET连接成功";
    String SOCKET_CONNECT_FAILED = "SOCKET连接失败";
    String SOCKET_RECONNECT = "重新连接";
    String SOCKET_BROKEN = "SOCKET已断开";
    String WIFI_DISABLED = "WIFI没有打开";
    String WIFI_DISCONNECTED = "WIFI没有连接";
    String FINISH_SERVICE = "结束WIFI服务";
    String SEND_SUCCESS = "USB发送成功";
    String SEND_FAILED = "USB发送失败";
    String USB_CONNECT_SUCCESS = "USB连接成功";
    String USB_CONNECT_FAILED = "USB连接失败";
    /**
     * 操作USB的 ACTIONS
     */
    String USB_CONNECT = "连接USB";
    String USB_DISCONNECT = "断开USB";
    String USB_RECONNECT = "重连USB";
    String USB_SEND = "USB发送数据";
    String USB_RECEIVE = "USB接收数据";
    String USB_REPLY = "USB回复数据";
    /**
     * 本机的 错误信息
     */
    String[] LOCAL_ACTIONS = new String[]{
            SOCKET_CONNECT_SUCCESS,
            SOCKET_CONNECT_FAILED,
            SOCKET_RECONNECT,
            SOCKET_BROKEN,
            WIFI_DISABLED,
            WIFI_DISCONNECTED,
            FINISH_SERVICE,
            SEND_SUCCESS,
            SEND_FAILED,
            USB_CONNECT_SUCCESS,
            USB_CONNECT_FAILED
    };
    String[] USB_ACTIONS = new String[]{
            USB_SEND, USB_RECEIVE, USB_REPLY,
            USB_CONNECT, USB_RECONNECT, USB_DISCONNECT
    };

    //ARM 参数设置
    HashMap<String, byte[]> ARM_SETTINGS = new HashMap<String, byte[]>() {
        {
            put(ARM_MAG_MAX, ArmMagMax);

            put(ARM_MOTION_INCREMENT_SPEED, ArmMotionIncrementSpeed);
            put(ARM_MOTION_MAX_SPEED, ArmMotionMaxSpeed);
            put(ARM_MOTION_ORIGIN_TURN_SPEED, ArmMotionOriginTurnSpeed);
            put(ARM_MOTION_READ_CARD_SPEED, ArmMotionReadCardSpeed);
            put(ARM_MOTION_START_SPEED, ArmMotionStartSpeed);
            put(ARM_MOTION_TURN_SPEED, ArmMotionTurnSpeed);
            put(ARM_MOTION_ULTRA_SPEED, ArmMotionUltraSpeed);

            put(ARM_ROUTE_QUERY_ALL, ArmRouteQueryAll);

            put(ARM_ULTRA_WARNING_DISTANCE, ArmUltraWarningDistance);

            put(ARM_SYS_ROBOT_ID, ArmSysRobotID);
            put(ARM_SYS_SEND_COUNT, ArmSysSendCount);
            put(ARM_SYS_SEND_PERIOD, ArmSysSendPeriod);
            put(ARM_SYS_RESET, ArmSysRest);
        }
    };
    /**
     * 需要 监听的 本机action
     */
    HashMap<String, byte[]> SEND_ACTIONS = new HashMap<String, byte[]>() {
        {
            // 所有的 设置 都是发送!..
            putAll(ARM_SETTINGS);

            //运动模块
            put(QUERY_STATE, QueryState);
            put(SET_MOVING, SetMoving);
            put(SET_WALK, SetWalk);
            put(SET_TURN, SetTurn);
            //路径算法
            put(QUERY_CARD, QueryCard);
            put(QUERY_TARGET, QueryTarget);
            put(UPDATE_CARD, UpdateCard);
            put(ADD_TARGETS, AddTargets);
            put(DEL_TARGETS, DelTargets);
            put(SET_PRIORITY, SetPriority);
            put(SEND_MAP, SendMap);
            put(UPLORD_MAP, UploadMap);
            put(REPLY_UPLORD_MAP, ReplyUploadMap);
            //磁传感器
            put(QUERY_MAGNETIC, QueryMagnetic);
            //电源
            put(QUERY_POWER, QueryPower);
            // 系统信息
            put(QUERY_SOFT_VERSION, QuerySoftVersion);
            //			put(QUERY_MAP_VERSION,QueryMapVersion);
            put(QUERY_USB_STATE, QueryUsbState);

            put(ERROR_FROM_ARM, ErrorFromArm);
        }
    };
    /**
     * 来自 ARM 主动发送的信息
     */
    HashMap<String, byte[]> RECEIVE_ACTIONS = new HashMap<String, byte[]>() {
        {
            //运动模块
            put(MOVE_DISTANCE_OVERFLOW, MoveDistanceOverflow);
            put(LONG_TIME_NOT_OPERATE, LongTimeNotOperate);
            //红外避障
            put(BARRIER_WARNING, BarrierWarning);
            //RFID检测
            put(CURRENT_RFID, CurrentRFID);
            put(MISSING_RFID, MissingRFID);
            put(WRONG_RFID, WrongRFID);
            //磁传感器
            put(MISSING_MAGNETIC, MissingMagnetic);
            put(SELF_CHECK_MAG_POSITION, SelfCheckMagPosition);
            //超声波检测
            put(ULTRASONIC_WARNING, UltrasonicWarning);
            //电源模块
            put(POWER_STATE, PowerState);
            //人体红外
            put(INFRARED_WARNING, InfraredWarning);
            //按键模块
            put(MACHINE_START_BTN, MachineStartBtn);
            put(MACHINE_STOP_BTN, MachineStopBtn);
            //系统模块
            put(ROBOT_REBOOT, RobotReboot);
        }
    };
    /**
     * 自检 模块 信息
     */
    HashMap<String, byte[]> SELF_CHECK_ACTIONS = new HashMap<String, byte[]>() {
        {
            putAll(RECEIVE_ACTIONS);
            put(SELF_CHECK_START_STOP, SelfCheckStartStop);
            put(SELF_CHECK_MOTION90, SelfCheckMotion90);
            put(SELF_CHECK_MOTION90, SelfCheckMotion180);
            put(SELF_CHECK_MAG_POSITION, SelfCheckMagPosition);
        }
    };

    HashMap<String, Object> WarningCode =
            new HashMap<String, Object>() {
                {
                    put(MOVE_DISTANCE_OVERFLOW, 0x15);
                    put(BARRIER_WARNING, 0x31);
                    put(MISSING_RFID, 0x41);
                    put(WRONG_RFID, 0x42);
                    put(MISSING_MAGNETIC, 0x51);
                    put(ULTRASONIC_WARNING, 0x61);
                    put(POWER_NOT_ENOUGH, 0x72);
                    put(POWER_EMPTY, 0x73);
                    put(INFRARED_WARNING, 0x81);
                    put(SOCKET_CONNECT_FAILED, 0x01);
                }
            };
    /**
     * 报警的语音
     */
    HashMap<String, String> WarningTTS = new HashMap<String, String>() {
        {
            put(MOVE_DISTANCE_OVERFLOW, "警报!警报!超出移动距离");
            put(LONG_TIME_NOT_OPERATE, "请按执行键,谢谢");
            put(BARRIER_WARNING, ",让一下啦!我正在工作!");
            put(MISSING_RFID, "好像走丢了!");
            put(WRONG_RFID, " 好像走错路了");
            put(MISSING_MAGNETIC, " 我找不到路了!");
            put(ULTRASONIC_WARNING, ",让一下啦!我正在工作!");
            put(POWER_NOT_ENOUGH, " 电量不足!");
            put(POWER_EMPTY, "电池耗尽,即将停机");
            put(INFRARED_WARNING, "让一下啦!我正在工作!");


            put(SOCKET_CONNECT_FAILED, " 网络连接好像出错了");
        }
    };
}

