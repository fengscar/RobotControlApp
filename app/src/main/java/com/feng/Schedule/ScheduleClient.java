package com.feng.Schedule;

import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import com.feng.Constant.I_MapData;
import com.feng.Constant.I_Parameters;
import com.feng.Constant.RobotEntity;
import com.feng.Database.FileTransporter;
import com.feng.Database.MapDatabaseHelper;
import com.feng.RobotApplication;
import com.feng.Utils.L;
import com.feng.Utils.SP;
import com.feng.Utils.T;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by fengscar on 2016/5/19.
 * <p/>
 * 该类作用是 : 在后台新建线程 与调度服务端进行交互
 * 后台线程 在Application的onCreate()中 开始 ; 当成员sSocket 为空时结束
 * <p/>
 * -->要主动发送请求给调度系统时, 使用该类对象直接调用public方法 , 比如updateTask等...
 * <p/>
 * <--要处理调度系统主动发送的信息, activity通过putHandler添加处理指定Method的handler,
 * 后台线程接收到调度系统的回复时,会寻找对应的handler进行处理
 */
public class ScheduleClient implements ScheduleProtocal {
    private final static String TAG = ScheduleClient.class.getSimpleName();

    private final static int HEART_BEAT_MESSAGE = 555;
    // 通知的Message的 what 举例
    public final static int SOCKET_CONNECT = 900; // socket连接上
    public final static int SOCKET_DISCONNECT = 901; //socket断开
    public final static int LOGIN_SUCCESS = 910; // 登录成功
    public final static int LOGIN_FAILED = 911; // 登录失败

    public final static int MAP_UPDATE = 1000;//地图已经更新
    Handler innerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HEART_BEAT_MESSAGE:
                    sendJson(HEART_BEAT, null);
                    sendHeartBeatHandlerMessage();
                    break;
            }
        }
    };
    private static Socket sSocket;
    // 存放处理 外部(调度系统)信息的handler
    private Map<String, Handler> handlerMap;
    // 存放当前注册的Activity,当发生内部事件时,通知所有activity
    private Map<String, Handler> notifierMap;

    // socket连接状态
    private boolean SocketConnectState = false;

    public boolean isConnect() {
        return SocketConnectState;
    }

    //登录状态
    private boolean LoginState = false;

    public void setLoginState(boolean state) {
        LoginState = state;
    }

    public boolean getLoginState() {
        return LoginState;
    }


    private volatile static ScheduleClient instance = null;

    private ScheduleClient() {
        this.startThread();
        initHandlerMap();
    }

    //初始化 handlerMap, 加入 接收到 heartbeat ,syncMap, updateMap时的处理
    private void initHandlerMap() {
        handlerMap = new HashMap<String, Handler>() {
            {
                put(HEART_BEAT, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        L.i(TAG, "接收到服务端心跳包");
                    }
                });


                put(ScheduleClient.LOGIN, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        try {
                            JSONObject json = (JSONObject) msg.obj;
                            boolean success = json.getBoolean(SUCCESS);
                            String expStr = json.getString(EXPLAIN);

                            L.i(TAG, "接收到响应" + success + expStr);

                            setLoginState(success);
                            notifyActivity(success ? LOGIN_SUCCESS : LOGIN_FAILED);

                        } catch (JSONException | NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                });

                // 机器人接收到 服务端的UpdateMap Json-Result
                //  替换本地数据库文件;
                put(UPDATE_MAP, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        try {
                            JSONObject json = (JSONObject) msg.obj;

                            String mapData = json.getString(MAP_DATA);
                            // String-> base64解码为 byte[] -> 解压缩为初始文件的byte[]
                            byte[] base64Bytes = Base64.decode(mapData, Base64.DEFAULT);
                            // 传来的数据库文件的二进制流
                            byte[] decompressedBytes = FileTransporter.decompress(base64Bytes);
                            // 数据库文件路径
                            String dbPath = RobotApplication.getContext().getDatabasePath(I_MapData.MAP_DATABASE).getAbsolutePath();
                            // 替换数据库文件
                            FileTransporter.createFile(dbPath, decompressedBytes);
                            // 关闭数据库,同时发送通知给
//                            MapDatabaseHelper.getInstance().closeDatabase();
//                            // 重新打开数据库
                            MapDatabaseHelper.getInstance().reopenDatabase();
                            MapDatabaseHelper.getInstance().autoAdapterScreen();
                            //
                            // 更新当前地图版本
                            int remoteVersion = (json.getInt(MAP_VERSION));
                            SP.put(RobotApplication.getContext(), I_Parameters.MAP_VERSION, remoteVersion);

                            notifyActivity(MAP_UPDATE);
                            T.show("更新地图成功,当前地图版本: " + remoteVersion);
                        } catch (JSONException | NullPointerException | IllegalArgumentException e) {
                            e.printStackTrace();
                            T.show("更新地图失败,地图数据错误,请检查服务端数据完整性");
                        }
                    }
                });

                put(SYNC_MAP, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        syncMap();
                    }
                });

                put(UPDATE_STATUS, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        L.i(TAG, "成功将状态信息同步到调度系统");
                    }
                });
            }
        };
        notifierMap = new HashMap<>();

    }


    public static ScheduleClient getInstance() {
        if (instance == null) {
            synchronized (ScheduleClient.class) {
                if (instance == null) {
                    instance = new ScheduleClient();
                }
            }
        }
        return instance;
    }

    public void putHandler(String methodName, Handler handler) {
        this.removeHandler(methodName);
        this.handlerMap.put(methodName, handler);
    }

    public void removeHandler(String methodName) {
        if (this.handlerMap.containsKey(methodName)) {
            this.handlerMap.remove(methodName);
        }
    }


    public void putNotifier(String activityName, Handler handler) {
        this.removeNotifier(activityName);
        this.notifierMap.put(activityName, handler);
    }

    public void removeNotifier(String activityName) {
        if (this.notifierMap.containsKey(activityName)) {
            this.notifierMap.remove(activityName);
        }
    }

    // 开始后台线程,实时接收服务端的信息
    private void startThread() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    L.i(TAG, "[开启线程] 调度系统客户端后台线程");
                    ScheduleServerInfo serverInfo = ScheduleServerInfo.getInstance();
                    sSocket = new Socket(serverInfo.getIp(), serverInfo.getPort());
                    L.i(TAG, "Socket成功连接到调度系统...");

                    SocketConnectState = true;
                    notifyActivity(SOCKET_CONNECT);

                    sendHeartBeatHandlerMessage();

                    login();

                } catch (IOException e) {
                    L.e(TAG, "连接到调度系统失败" + '\n' + e.toString());

                    close();
                    return;
                }

                while (true) {
                    if (sSocket == null || !sSocket.isConnected()) {
                        break;
                    }
                    BufferedReader reader;
                    try {
                        reader = new BufferedReader(new InputStreamReader(
                                sSocket.getInputStream()));
                        String jsonStr = reader.readLine();
                        if (null == jsonStr) {
                            // 服务端 退出
                            L.e(TAG, "客户端停止接收信息,服务端已退出");
                            close();
                            break;
                        }
                        if (jsonStr.equals("")) {
                            continue;
                        }
                        L.d(TAG, "[Receive Json]" + jsonStr);

                        JSONObject json = new JSONObject(jsonStr);

                        String method = json.getString(METHOD);
                        if (method.equals("") || !handlerMap.containsKey(method)) {
                            L.e(TAG, "HandlerMap has not method : " + method);
                            continue;
                        }

                        // 根据 Method , 获取不同的handler进行响应
                        Handler handler = handlerMap.get(method);
                        if (handler == null) {
                            L.d(TAG, "Handler is null");
                            continue;
                        }
                        // 判断是否有Result字段
                        if (!json.has(RESULT)) {
                            L.d(TAG, "JSON has not Result");
                            continue;
                        }

                        Object resultObj = null;
                        // 判断JSON字段是否为NULL,如果不是NULL,获取json对象
                        if (!json.isNull(RESULT)) {
                            resultObj = json.getJSONObject(RESULT);
                        }

                        Message msg = new Message();
                        msg.obj = resultObj;
                        handler.sendMessage(msg);

                    } catch (IOException | JSONException | NullPointerException e) {
                        L.e(TAG, e.toString());
                    }
                }
            }
        };
        thread.start();
    }

    public void reconnect() {
        try {
            if (sSocket != null) {
                sSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        startThread();
    }

    // 将相应的通知发送给Activity
    private void notifyActivity(int msgWhat) {
        for (Handler notifier : notifierMap.values()) {
            if (notifier != null) {
                Message msg = notifier.obtainMessage();
                msg.what = msgWhat;
                notifier.sendMessage(msg);
            }
        }
    }

    /**
     * 机器人在 调度管理系统上 登录
     */
    public void login() {
        String robotID = (String) SP.get(RobotApplication.getContext(), I_Parameters.ROBOT_ID, "HCWY-A01");
        String robotName = (String) SP.get(RobotApplication.getContext(), I_Parameters.ROBOT_NAME, "PangPang");

        L.i(TAG, robotID + "[ " + robotName + " ]   正在登录......");

        JSONObject param = new JSONObject();
        // 设备类型
        try {
            param.put(DEVICE, "ROBOT");
            param.put(ROBOT_ID, robotID);
            param.put(ROBOT_NAME, robotName);

            sendJson(LOGIN, param);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 机器人 发送地图数据给 调度系统
     * 是否更新调度系统的数据( 当用户主动发送时,将机器人数据更新到调度系统)
     *
     * @throws FileNotFoundException
     * @throws JSONException
     */
    public void syncMap() {
        String dbPath = RobotApplication.getContext().getDatabasePath(I_MapData.MAP_DATABASE).getAbsolutePath();
        JSONObject param = new JSONObject();
        try {
            //获取地图版本信息
            int mapVersion = (int) SP.get(RobotApplication.getContext(), I_Parameters.MAP_VERSION, 1);
            param.put(MAP_VERSION, mapVersion);

            // 获取db文件原始二进制流
            byte[] dbFileBytes = FileTransporter.loadFile(dbPath);
            // 压缩
            byte[] compressBytes = FileTransporter.compress(dbFileBytes);
            // Base64编码
            String localDbFile = Base64.encodeToString(compressBytes, Base64.DEFAULT);
            param.put(MAP_DATA, localDbFile);
            // 发送JSON文件
            sendJson(SYNC_MAP, param);

        } catch (FileNotFoundException | JSONException e) {
            e.printStackTrace();
        }

    }

    public void updateMap() {
        JSONObject param = new JSONObject();
        try {
            int localVersion = (int) SP.get(RobotApplication.getContext(), I_Parameters.MAP_VERSION, 1);
            param.put(MAP_VERSION, localVersion);

            sendJson(UPDATE_MAP, param);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 机器人更新实时状态...(当机器人状态改变时调用)
     */
    public void updateStatus(RobotEntity robotEntity) {
        if (robotEntity == null) {
            L.e(TAG, "向调度系统更新状态失败 :参数为空");
            return;
        }
        try {
            /**
             * 获取机器人状态
             */
            int locationID = robotEntity.getLocation().getId();
            int state = robotEntity.getState().toInt();
            int speed = robotEntity.getSpeed();
            String[] warnings = robotEntity.getWarnings();
            int[] nodeList = robotEntity.getTasks();
            int[] pathList = robotEntity.getPaths();

            /**
             * 生成JSON 对象
             */
            JSONObject jsonParam = new JSONObject();
            jsonParam.put(LOCATION, locationID);
            jsonParam.put(STATE, state);
            jsonParam.put(SPEED, speed);


            if (warnings == null) {
                jsonParam.put(WARNINGS, null);
            } else {
                JSONArray warningJsonArray = new JSONArray();
                for (String warning : warnings) {
                    warningJsonArray.put(warning);
                }
                jsonParam.put(WARNINGS, warningJsonArray);
            }

            if (nodeList == null) {
                jsonParam.put(TASKS, null);
            } else {
                JSONArray tasksJsonArray = new JSONArray();
                for (int node : nodeList) {
                    tasksJsonArray.put(node);
                }
                jsonParam.put(TASKS, tasksJsonArray);
            }

            if (pathList == null) {
                jsonParam.put(PATHS, null);
            } else {
                JSONArray pathsJsonArray = new JSONArray();
                for (int path : pathList) {
                    pathsJsonArray.put(path);
                }
                jsonParam.put(PATHS, pathsJsonArray);
            }

            sendJson(UPDATE_STATUS, jsonParam);
        } catch (NullPointerException | JSONException e) {
            e.printStackTrace();
        }


    }


    // 开启新线程发送心跳包...

    // 开启新线程发送心跳包...
    public void sendHeartBeatHandlerMessage() {
        Message msg = innerHandler.obtainMessage();
        msg.what = HEART_BEAT_MESSAGE;
        innerHandler.sendMessageDelayed(msg, I_Parameters.HEART_BEAT_PERIOD);
    }

    // 机器人发送JSON请求
    public void sendJson(String method, JSONObject param) {
        if (sSocket == null || !sSocket.isConnected()) {
            notifyActivity(SOCKET_DISCONNECT);
            Log.e(TAG, "sendJson: 发送失败,调度系统还未连接");
            return;
        }
        try {
            // 获取 输出流
            OutputStreamWriter osw = new OutputStreamWriter(sSocket.getOutputStream());
            BufferedWriter writer = new BufferedWriter(osw);

            JSONObject json = new JSONObject();
            // 获取JSON
            json.put(METHOD, method);
            json.put(PARAM, param);

            // 输出流发送 JSON字符串
            writer.write(json.toString());
            writer.flush();
            L.d(TAG, "[Send JSON]" + json.toString());
        } catch (JSONException | IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * close后 需要重新获取操作调度通信的实例对象...
     */
    public void close() {
        try {
            if (sSocket != null) {
                sSocket.close();
                sSocket = null;
            }
            this.SocketConnectState = false;
            notifyActivity(SOCKET_DISCONNECT);
            innerHandler.removeMessages(HEART_BEAT_MESSAGE);
            L.i(TAG, "[关闭线程] 调度系统客户端线程");
        } catch (IOException e) {
            e.printStackTrace();
        }
        instance = null;
    }
}
