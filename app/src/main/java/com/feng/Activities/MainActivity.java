
package com.feng.Activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import android.widget.ImageView.ScaleType;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.feng.Adapter.ExpanderAdapter;
import com.feng.Adapter.UserControlFragmentListener;
import com.feng.Adapter.UserControlTaskListListener;
import com.feng.Base.BaseActivity;
import com.feng.Constant.I_Parameters;
import com.feng.CustomView.IconButton;
import com.feng.CustomView.WarningDialogCallback;
import com.feng.Database.Map.MapDatabaseHelper;
import com.feng.Database.Map.Node;
import com.feng.Fragments.IFragmentControl;
import com.feng.Fragments.ListingFragment;
import com.feng.Fragments.WorkspaceFragment;
import com.feng.MapModule.MapFragment;
import com.feng.RSS.R;
import com.feng.RobotApplication;
import com.feng.Schedule.Robot;
import com.feng.Schedule.ScheduleClient;
import com.feng.Schedule.ScheduleProtocal;
import com.feng.SpeechRecognize.IatService;
import com.feng.Usb.ArmHandler.*;
import com.feng.Usb.ArmProtocol;
import com.feng.Usb.ArmUsbManager;
import com.feng.Usb.UsbData;
import com.feng.Usb.UsbEvent;
import com.feng.UserManage.PasswordManager;
import com.feng.Utils.*;
import com.sdsmdg.tastytoast.TastyToast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends BaseActivity implements I_Parameters, ArmProtocol, ScheduleProtocal, UserControlFragmentListener, UserControlTaskListListener {
    private final static String TAG = MainActivity.class.getSimpleName();

    //region FindViewByID
    @BindView(R.id.ibHome)
    ImageButton mIbHome;
    @BindView(R.id.taskListView)
    ExpandableListView mTaskListView;
    @BindView(R.id.btnClearTask)
    Button mBtnClearTask;
    @BindView(R.id.btnRefreshTask)
    Button mBtnRefreshTask;
    @BindView(R.id.mainFragment)
    FrameLayout mMainFragment;
    @BindView(R.id.tbExecute)
    ToggleButton mTbExecute;
    @BindView(R.id.tbWash)
    ToggleButton mTbWash;
    @BindView(R.id.btnSetting)
    IconButton mBtnSetting;
    @BindView(R.id.btnQuit)
    IconButton mBtnQuit;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.iconBtnRobot)
    IconButton mIconBtnRobot;
    @BindView(R.id.iconBtnBack)
    IconButton mIconBtnBack;
    @BindView(R.id.iconBtnUsb)
    IconButton mIconBtnUsb;
    @BindView(R.id.iconBtnSchedule)
    IconButton mIconBtnSchedule;
    @BindView(R.id.iconBtnPower)
    IconButton mIconBtnPower;
    @BindView(R.id.iconBtnWarning)
    IconButton mIconBtnWarning;

    //endregion

    //region OnCLickListener
    @OnClick({R.id.ibHome, R.id.btnClearTask, R.id.btnRefreshTask,
            R.id.tbExecute, R.id.tbWash, R.id.btnSetting, R.id.btnQuit,
            R.id.iconBtnRobot, R.id.iconBtnBack, R.id.iconBtnUsb,
            R.id.iconBtnSchedule, R.id.iconBtnPower, R.id.iconBtnWarning})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.ibHome:
                //				changeFragment(false, null);
                if (!IS_WORKSPACE_FRAGMENT) {
                    changeToMenuFragment();
                }
                break;


            case R.id.btnClearTask:
                //清空机器人界面的任务
                for (Node node : mExpanderAdapter.getTasks()) {
                    if (!sRobot.hasTask(node.getId())) {
                        mExpanderAdapter.delTarget(node);
                        if (mIFragmentController != null) {
                            mIFragmentController.delTask(node);
                        }
                    }
                }
                // 清空ARM的任务
                PathHandler.getInstance().delTargets(mExpanderAdapter.getTasks());
                break;

            case R.id.btnRefreshTask:
                PathHandler.getInstance().queryTarget();
                break;


            case R.id.tbExecute:
                if (mTbExecute.isChecked()) {
                    //点击后先不改变状态
                    mTbExecute.setChecked(false);
                    // 发送还未设置的任务
                    final List<Node> newTask = new ArrayList<>();
                    for (Node n : mExpanderAdapter.getTasks()) {
                        if (!sRobot.hasTask()) {
                            newTask.add(n);
                        }
                    }
                    if (newTask.size() == 0) {
                        // 如果没有新任务...只发送执行按键
                        MotionHandler.getInstance().setExecuteMove(true);
                        return;
                    }
                    // 如果有新任务,根据发送任务的结果来进行不同的操作
                    ExecutorService executorService = Executors.newSingleThreadExecutor();
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            //设置任务到ARM成功, 将继续发送 [设置执行]
                            if (PathHandler.getInstance().addTargetsForResult(newTask)) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new IntentDealer(new Transfer()).sendTtsIntent(TTS_START_SPEAK, "任务设置成功");
                                        TastyToast.makeText(RobotApplication.getContext(), "开始执行任务", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS);
                                    }
                                });
                                for (Node n : newTask) {
                                    sRobot.addTask(n.getId());
                                }
                                // 让USB发送 , 收到回复后, 在onReceive中会改变 按键的状态
                                MotionHandler.getInstance().setExecuteMove(true);
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new IntentDealer(new Transfer()).sendTtsIntent(TTS_START_SPEAK, "任务设置失败");
                                        TastyToast.makeText(RobotApplication.getContext(), "任务设置失败", TastyToast.LENGTH_SHORT, TastyToast.ERROR);
                                    }
                                });
                            }
                        }
                    });

                } else {
                    mTbExecute.setChecked(true);
                    MotionHandler.getInstance().setExecuteMove(false);
                }
                break;

            case R.id.tbWash:
                // 按下撤台后, 查找清洗区的NODE
                //1.如果找到,添加到列表,
                //2 如果没找到, 提示出错
                //TODO 工作区ID如何获取
                //点击后先不改变状态 ( 点击后,获取getChecked是已经改变过的,所以设置为!相反)
                mTbWash.setChecked(!mTbWash.isChecked());
                List<Node> nodeList = mDatabaseHelper.getNodeByType(NODE_TYPE.WASH);
                if (nodeList == null || nodeList.size() == 0) {
                    T.show("未找到清洗区,请前往地图编辑!");
                    break;
                }
                //有多个清洗区时
                if (nodeList.size() > 1) {
                    // TODO 弹出POPUP让用户选择具体的清洗区
                } else {
                    //只有一个清洗区时
                    Node node = nodeList.get(0);
                    onUserAdd(node);
                    break;
                }
                break;

            case R.id.btnSetting:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                PasswordManager.login(MainActivity.this);
                break;

            case R.id.btnQuit:
                MainActivity.this.finish();


                break;


            /**
             * 以下是 左滑菜单的点击事件
             */
            case R.id.iconBtnRobot:
                TastyToast.makeText(RobotApplication.getContext(), "连接成功", TastyToast.LENGTH_LONG, TastyToast.SUCCESS);
                // TODO提供修改机器人名称的对话框
//                    CustomDialog.Builder builder = new CustomDialog.Builder(MainActivity.this);
//                    builder.setTitle()
                break;

            case R.id.iconBtnBack:
                mDrawerLayout.closeDrawer(GravityCompat.START);
                break;

            case R.id.iconBtnUsb:
                SystemHandler.getInstance().queryUsbConnectStatus();
                break;

            case R.id.iconBtnSchedule:
                if (mScheduleClient == null) {
                    mScheduleClient = ScheduleClient.getInstance();
                } else {
                    // 如果未连接 socket, 重新连接
                    if (!mScheduleClient.isConnect()) {
                        mScheduleClient.reconnect();
                    } else {
                        // 如果是已连接未登录,重新登录
                        if (!mScheduleClient.getLoginState()) {
                            mScheduleClient.login();
                        }
                    }
                }
                break;

            case R.id.iconBtnPower:
                PowerHandler.getInstance().queryCurrentPower();
                break;

            case R.id.iconBtnWarning:
                if (warningDialog.getWarningCount() != 0) {
                    warningDialog.showDialog();
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }
                break;
        }
    }
    //endregion

    private ArmUsbManager mArmUsbManager = ArmUsbManager.getInstance();
    private MapDatabaseHelper mDatabaseHelper;

    private ScheduleClient mScheduleClient;

    private ExpandableListView taskListView;
    private ExpanderAdapter mExpanderAdapter;

    private IFragmentControl mIFragmentController;
    private WorkspaceFragment workspaceFragment;
    private boolean IS_WORKSPACE_FRAGMENT = false;

    private boolean isLocked; // 是否处于 表情界面
    private boolean prepareExit; // 是否已经在3 秒内按下过 back键,再按则退出

    private Robot sRobot;

    private View currentView;

    // activity内部的handler
    private InnerHandler handler = new InnerHandler(this);

    private static class InnerHandler extends Handler {
        private WeakReference<MainActivity> activityWeakReference;

        public InnerHandler(MainActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                if (msg.what == REFRESH) {
                    //刷新ListView
                    activity.taskListView.setAdapter(activity.mExpanderAdapter);
                } else if (msg.what == PREPARE_EXIT) {
                    activity.prepareExit = false;
                }
            }
        }
    }

    //处理 usb接收的信息
    private class MainUsbHandler extends BaseUsbHandler {
        private WeakReference<MainActivity> activityWeakReference;

        public MainUsbHandler(MainActivity activity) {
            super(activity);
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            //先做超类BaseHandler的方法. 刷新ROBOT状态


            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                if (msg.obj == null) {
                    return;
                }
                UsbData data = (UsbData) msg.obj;
                UsbEvent event = data.getEvent();
                byte[] dataReceive = data.getDataReceive();
                byte[] dataSend = data.getDataToSend();

                switch (event) {
                    case UsbConnect:
                        updateUsbState(true);
                        initArmState();
                        break;

                    case UsbConnectFailed:
                    case UsbDisconnect:
                        updateUsbState(false);
                        // 不return,继续给 basehandelr处理,让其更新警告框
                        break;

                    case UsbReceive:
                        // 如果处理成功,返回true,退出处理.否则会调用super.handleMessage
                        if (handleReceive(dataReceive)) {
                            return;
                        }
                        break;

                    case UsbSendFailed:
                        if (handleSendFailed(dataReceive, dataSend)) {
                            return;
                        }
                        break;

                    case UsbSendSuccess:
                        if (handleSendSuccess(dataReceive, dataSend)) {
                            return;
                        }
                        break;
                }
                super.handleMessage(msg);
            }
        }

        /**
         * 当发送失败时,进行的处理
         *
         * @param dataReceive 发送失败时,接收的信息
         * @param dataSend    发送失败是时,发送的信息
         * @return
         */
        private boolean handleSendFailed(byte[] dataReceive, byte[] dataSend) {
            String failedAction = transfer.getAction(dataSend);
            if (failedAction == null) {
                return false;
            }
            switch (failedAction) {
                case QUERY_USB_STATE:
                    updateUsbState(false);
                    break;
                default:
                    return false;
            }
            return true;
        }

        /**
         * 当接收数据时, 对数据进行的处理
         *
         * @param dataReceive 消息中的数据
         * @return 是否处理了该信息
         */
        private boolean handleReceive(byte[] dataReceive) {
            String action = transfer.getAction(dataReceive);
            if (action == null) {
                return false;
            }
            sRobot.beginNotifyChange();
            switch (action) {
                case CURRENT_RFID:
                    int nodeID = transfer.twoByteToInt(transfer.getBody(dataReceive));
                    Node currentNode = mDatabaseHelper.getNodeByID(nodeID);
                    if (currentNode == null) {
                        L.e(TAG, "[OnReceiveRFID]读到错误的RFID点...");
                        break;
                    }
                    sRobot.setLocation(currentNode.getId());
                    //if  true: 是任务点 ; 返回 false : 非任务点.
                    if (sRobot.delTask(currentNode.getId())) {
                        mExpanderAdapter.delTarget(currentNode);
                        // 同时更新 fragment...
                        if (mIFragmentController != null) {
                            mIFragmentController.delTask(currentNode);
                        }
                        L.i(TAG, "到达目的点 : " + currentNode.getName());
                        T.show(" 已到达 : " + currentNode.getName());
//            intentDealer.sendTtsIntent(TTS_START_SPEAK, " 已到达 : " + currentNode.getName());
                        lockContentView(false);
                    } else {
                        L.i(TAG, "经过点 : " + currentNode.getName());
                    }
                    break;

                case CURRENT_PATH:
                    // 首位: 总路径数,  剩余: 节点ID
                    try {
                        byte[] currentPathByte = transfer.getBody(dataReceive);
                        int pathNum = currentPathByte[0];
                        List<Integer> paths = new ArrayList<>();
                        for (int i = 0; i < pathNum; i++) {
                            paths.add(transfer.twoByteToInt(new byte[]{currentPathByte[i * 2 + 1], currentPathByte[i * 2 + 2]}));
                        }
                        sRobot.setPaths(paths);
                    } catch (IndexOutOfBoundsException e) {
                        Log.e(TAG, "handleReceive: [CurrentPath] 传来的数据有误");
                    }
                    break;


                case MACHINE_START_BTN:
                    // 不发送数据,只切换状态!
                    // 如果是 接收到的是执行. 切换按键为 true

//                    if (transfer.getFlag(dataReceive)) {
                    mTbExecute.setChecked(ButtonHandler.getInstance().isExecute());
//                    } else {
//                        // 如果接收到 暂停. 切换按键为 false( 显示的字体是 [执行])
//                        //  但此时不可用( 交给外设按键操作)
//                        mTbExecute.setChecked(false);
//                    }
                    break;

                case MACHINE_STOP_BTN:
                    mTbExecute.setChecked(!ButtonHandler.getInstance().isScram());
                    break;

                case ROBOT_REBOOT:
                    initArmState();
                    break;

                default:
                    sRobot.endNotifyChange();
                    return false;
            }
            sRobot.endNotifyChange();
            return true;
        }

        /**
         * 当发送成功时,进行相应的处理
         */
        private boolean handleSendSuccess(byte[] dataReceive, byte[] dataSend) {
            String action = transfer.getAction(dataSend);
            if (action == null) {
                return false;
            }
            L.i(TAG, "发送成功!" + action);
            sRobot.beginNotifyChange();
            switch (action) {
                case QUERY_STATE:
                    byte[] body = transfer.getBody(dataReceive);
                    sRobot.beginNotifyChange();
                    sRobot.updateMotionStatusFromArm(body);
                    sRobot.endNotifyChange();
//                    byte[] detailData = transfer.getBody(dataReceive);
//                    if (detailData[0] == (byte) 0x00) {
//                        //停止中
//                        tbExecute.setChecked(false);
//                        sRobot.setState(RobotEntity.MotionState.FREE);
//                    } else if (detailData[0] == (byte) 0x01 ||
//                            detailData[0] == (byte) 0x03) {
//                        //形式中  , 转向中
//                        tbExecute.setChecked(true);
//                        sRobot.setState(RobotEntity.MotionState.MOVING);
//                    } else if (detailData[0] == (byte) 0x02) {
//                        // 原地转向
//                        tbExecute.setChecked(false);
//                        sRobot.setState(RobotEntity.MotionState.WAITING);
//                    } else if (detailData[0] == (byte) 0x04) {
//                        //故障
//                        tbExecute.setChecked(false);
//                        sRobot.setState(RobotEntity.MotionState.ERROR);
//                    }
//
//                    if (mScheduleClient != null && mScheduleClient.isConnect()) {
//                        mScheduleClient.updateStatus(sRobot);
//                    }

                    break;

                case SET_MOVING:
                    //改变 toggle状态
                    // 如果接收到的是 发送成功 ( 0x01,改变按键状态, 如果是 0x00,不操作 )
                    boolean setSuccess = transfer.getFlag(dataReceive);
                    if (!setSuccess) {
                        break;
                    } else {
                        // 如果是设置运动成功...则ToggleBtn按下..
                        boolean isSetMove = transfer.getFlag(dataSend);
                        mTbExecute.setChecked(isSetMove);
                    }
                    //进入锁屏界面
                    lockContentView(false);
                    break;
                case QUERY_TARGET:
                    List<Node> currentTargets = PathHandler.getInstance().getNodesFromByte(dataReceive);
                    mExpanderAdapter.clearTask();
                    mExpanderAdapter.addTarget(currentTargets);
                    // 具体的工作区界面可能还未初始化
                    if (mIFragmentController != null) {
                        mIFragmentController.clearTask();
                        mIFragmentController.addTask(currentTargets);
                    }
                    updateWashBtn();

                    // 更新调度
                    sRobot.clearTask();
                    for (Node current : currentTargets) {
                        sRobot.addTask(current.getId());
                    }
                    break;


                //添加任务成功
                case ADD_TARGETS:
                    //成功添加的节点s
                    List<Node> addTargets = PathHandler.getInstance().getNodesFromByte(dataSend);
                    //向任务列表 添加任务
                    mExpanderAdapter.addTarget(addTargets);
                    // 向Fragment添加任务..(fragment有判断,如果是撤台Node,不添加)
                    if (mIFragmentController != null) {
                        mIFragmentController.addTask(addTargets);
                    }
                    //更新撤台按键状态
                    updateWashBtn();

                    for (Node add : addTargets) {
                        sRobot.addTask(add.getId());
                    }
                    break;

                case DEL_TARGETS:
                    // 成功删除的节点s
                    List<Node> delTargets = PathHandler.getInstance().getNodesFromByte(dataSend);
                    mExpanderAdapter.delTarget(delTargets);
                    if (mIFragmentController != null) {
                        mIFragmentController.delTask(delTargets);
                    }
                    updateWashBtn();

                    for (Node del : delTargets) {
                        sRobot.delTask(del.getId());
                    }
                    break;

                case QUERY_POWER:
                    byte[] p = transfer.getBody(dataReceive);
                    int percent = p[0] & 0xff;
                    L.e(TAG, "更新电量为 :" + percent);
                    updatePowerState(percent);
                    break;

                case QUERY_USB_STATE:
                    // 收到ARM回复,表示连接正常
                    updateUsbState(true);
                    break;

                default:
                    sRobot.endNotifyChange();
                    return false;
            }
            sRobot.endNotifyChange();
            return true;
        }
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDatabaseHelper = MapDatabaseHelper.getInstance();

        isLocked = false;
        // 这里是用 Inflater的原因是 将对操作界面的修改 保存到currentView中
        //  当Activity从 lockContentView(?) 中切换回来时,可以不需要重新初始化各个控件
        LayoutInflater inflater = LayoutInflater.from(this);
        currentView = inflater.inflate(R.layout.activity_main, null);


//        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

        setContentView(currentView);

        ButterKnife.bind(this);

        //初始化应用级对象 (机器人实体,USB通信,,调度通信)
        sRobot = Robot.getInstance();
        //初始化USB对象
        mArmUsbManager = RobotApplication.getArmUsbManager();
        ArmUsbManager.getInstance().addObserver(TAG, new MainUsbHandler(this));
        // 调度系统客户端对象
        mScheduleClient = RobotApplication.getScheduleClient();
        //初始化 处理调度系统命令的handler
        this.initScheduleHandler();

//        initIAT();
        initView();
        initFragment();

        //初始化任务列表
        initExpanderListView();
        // 初始化ARM状态
        initArmState();
        // 初始化调度系统链接状态
        updateScheduleState(mScheduleClient.isConnect(), mScheduleClient.getLoginState());

    }

    private void initScheduleHandler() {

        mScheduleClient.putHandler(PUSH_COMMAND, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                try {
                    JSONObject json = (JSONObject) msg.obj;
                    /**
                     获取命令
                     */
                    boolean command = json.getBoolean(ScheduleProtocal.MOVE);
                    L.i(TAG, "接收到调度服务端指令: " + command);
                    MotionHandler.getInstance().setScheduleControl(command);
                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });

        mScheduleClient.putHandler(PUSH_TASK, new Handler() {
            @Override
            public void handleMessage(Message msg) {
                try {
                    JSONObject json = (JSONObject) msg.obj;
                    /**
                     * 添加任务
                     */
                    if (json.has(ADD_TASKS) && !json.isNull(ADD_TASKS)) {
                        JSONArray addTaskArray = json.getJSONArray(ADD_TASKS);
                        if (addTaskArray != null && addTaskArray.length() > 0 && mArmUsbManager != null) {
                            List<Node> nodeList = new ArrayList<>();
                            for (int i = 0; i < addTaskArray.length(); i++) {
                                Node node = mDatabaseHelper.getNodeByID(addTaskArray.getInt(i));
                                if (node != null) {
                                    nodeList.add(node);
                                }
                            }
                            PathHandler.getInstance().addTargets(nodeList);
                        }
                    }

                    /**
                     * 删除任务
                     */
                    if (json.has(DEL_TASKS) && !json.isNull(DEL_TASKS)) {
                        JSONArray delTaskArray = json.getJSONArray(DEL_TASKS);
                        if (delTaskArray != null && delTaskArray.length() > 0 && mArmUsbManager != null) {
                            List<Node> nodeList = new ArrayList<>();
                            for (int i = 0; i < delTaskArray.length(); i++) {
                                Node node = mDatabaseHelper.getNodeByID(delTaskArray.getInt(i));
                                if (node != null) {
                                    nodeList.add(node);
                                }
                            }
                            PathHandler.getInstance().delTargets(nodeList);
                        }
                    }
                    /**
                     * 进行回复
                     */
                    JSONObject reply = new JSONObject();
                    reply.put(PARAM, null);
                    mScheduleClient.sendJson(PUSH_COMMAND, reply);

                } catch (JSONException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initIAT() {
        startService(new Intent().setClass(this, IatService.class));
    }


    /**
     * 获取 当前任务列表 ( 包括了 路线总数)
     * 用于发送 给ARM
     *
     * @return 转换为byte形式的 任务列表
     */
    private byte[] getByteTaskList() {
        List<Node> nodeList = mExpanderAdapter.getTasks();
        int dataLength = nodeList.size() * 2 + 1;
        byte[] res = new byte[dataLength];
        //节点数量 n
        res[0] = (byte) (nodeList.size());
        int i = 1;
        // 将任务列表中的 每个节点 ID 转成 2 BYTE ...
        for (Node node : nodeList) {
            System.arraycopy(transfer.intTo2Byte(node.getId()), 0, res, i, 2);
            i += 2;
        }
        return res;
    }

    private int[] getIntTaskList() {
        List<Node> nodeList = mExpanderAdapter.getTasks();
        int[] taskIds = new int[nodeList.size()];
        for (int i = 0; i < taskIds.length; i++) {
            taskIds[i] = nodeList.get(i).getId();
        }
        return taskIds;
    }

    /**
     * @return List<node>格式的已选任务
     */
    public List<Node> getNodeTaskList() {
        return mExpanderAdapter.getTasks();
    }

    /**
     * App退出时: 关闭掉服务,关闭掉
     */
    @Override
    protected void onDestroy() {
        stopService(new Intent(MainActivity.this, IatService.class));

        ((RobotApplication) getApplication()).quit();
        mDatabaseHelper = null;

        super.onDestroy();
    }

    /**
     * 重新开始监听
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        // TODO 这里只刷新了 taskList , 需要改成刷新 Fragment,
        //是否需要在 onStop中保存Fragment的状态?
        handler.sendEmptyMessage(REFRESH);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 将当前工作区设置为空, 以便初始化
        if (mScheduleClient != null) {
            mScheduleClient.putNotifier(MainActivity.class.getSimpleName(), new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case ScheduleClient.SOCKET_CONNECT:
                            updateScheduleState(true, false);
                            break;

                        case ScheduleClient.SOCKET_DISCONNECT:
                            updateScheduleState(false, false);
                            break;

                        case ScheduleClient.LOGIN_SUCCESS:
                            updateScheduleState(true, true);
                            break;

                        case ScheduleClient.LOGIN_FAILED:
                            updateScheduleState(true, false);
                            break;

                        case ScheduleClient.MAP_UPDATE:
                            mIFragmentController = null;
                            workspaceFragment = null;
                            changeToMenuFragment();
                            break;
                    }
                    super.handleMessage(msg);
                }
            });
        }
        if (mIFragmentController != null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.hide((Fragment) mIFragmentController);
            fragmentTransaction.commit();
            mIFragmentController = null;
        }
        workspaceFragment = null;
        changeToMenuFragment();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mScheduleClient != null) {
            mScheduleClient.removeNotifier(MainActivity.class.getSimpleName());
        }

    }

    // 初始化 fragment
    private void initFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        workspaceFragment = new WorkspaceFragment();
        fragmentTransaction.replace(R.id.mainFragment, workspaceFragment);
        fragmentTransaction.commit();

        IS_WORKSPACE_FRAGMENT = true;
    }

    /**
     * 切换到具体工作区...
     *
     * @param wspId 工作区ID
     */
    public void changeToDetailFragment(int wspId) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        /**
         * 初始化fragment
         */
        //获取 显示模式,默认为 列表模式
        boolean isListingMode = !(boolean) SP.get(this, IS_MAP_MODEL, true);
        boolean isMatching = isListingMode ?
                mIFragmentController instanceof ListingFragment : mIFragmentController instanceof MapFragment;

        if (mIFragmentController != null) {
            //如果不为空,但是 模式不匹配,则销毁当前fragment并新建
            // 如果 工作区切换了, 也销毁并新建 TODO 应该改成修改data内容
            if (!isMatching || mIFragmentController.getWorkspaceID() != wspId) {
                fragmentTransaction.remove((Fragment) mIFragmentController);
                mIFragmentController = isListingMode ?
                        (IFragmentControl) ListingFragment.getInstance(wspId, getIntTaskList())
                        : new MapFragment();
                fragmentTransaction.add(R.id.mainFragment, (Fragment) mIFragmentController);
            }
        } else {
            // 初始化 新的 fragment
            mIFragmentController = isListingMode ?
                    (IFragmentControl) ListingFragment.getInstance(wspId, getIntTaskList())
                    : new MapFragment();

            fragmentTransaction.add(R.id.mainFragment, (Fragment) mIFragmentController);
        }

        /**
         * 配置 fragment参数, 此时fragment的onCreateView还未调用...
         */
        // 设置回调...当用户点击 fragment中的元素时调用
        mIFragmentController.setCallback(this);
        fragmentTransaction.show((Fragment) mIFragmentController);
        fragmentTransaction.hide(workspaceFragment);
        fragmentTransaction.commit();

        IS_WORKSPACE_FRAGMENT = false;
    }

    /**
     * 切换到 工作区选择界面
     */
    private void changeToMenuFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (mIFragmentController != null) {
            fragmentTransaction.hide((Fragment) mIFragmentController);
        }
        if (workspaceFragment == null) {
            workspaceFragment = new WorkspaceFragment();
            fragmentTransaction.replace(R.id.mainFragment, workspaceFragment);
        }
        fragmentTransaction.show(workspaceFragment);
        fragmentTransaction.commit();
        IS_WORKSPACE_FRAGMENT = true;
    }

    //初始化 按键
    private void initView() {
        warningDialog.setOnWarningChangeListenner(new WarningDialogCallback() {
            public void onWarningChange(int warningNum) {
                if (warningNum == 0) {
                    updateHomeMenuBtn(true);
                } else {
                    updateHomeMenuBtn(false);
                }
                updateWarningState(warningNum);
            }

            public void onAddWarning(String ttsStr) {
                intentDealer.sendTtsIntent(TTS_START_SPEAK, ttsStr);
            }
        });
    }

    //初始化 任务列表
    private void initExpanderListView() {
        taskListView = (ExpandableListView) findViewById(R.id.taskListView);

        taskListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // 屏蔽点击组选项, 防止列表收起...
                return true;
            }
        });

        mExpanderAdapter = new ExpanderAdapter(this, this);
        // 展开该Group...
//        taskListView.expandGroup(groupPosition);
//        //TODO 滚动到新增点的位置
//        taskListView.smoothScrollByOffset(groupPosition);


        taskListView.setAdapter(mExpanderAdapter);
        // 设置group的 伸缩图标
        //		taskListView.setGroupIndicator(getResources().getDrawable(R.drawable.route));
        taskListView.setGroupIndicator(null);
    }


    // 如果有 清洗去被选中... 将撤台按键设为选定状态
    private void updateWashBtn() {
        List<Node> washNodes = mDatabaseHelper.getNodeByType(NODE_TYPE.WASH);
        if (washNodes == null) {
            return;
        }
        //遍历所有已选任务
        for (Node node : mExpanderAdapter.getTasks()) {
            //遍历所有 清洗区
            for (Node washNode : washNodes) {
                if (node.equals(washNode)) {
                    mTbWash.setChecked(true);
                    return;
                }
            }
        }
        mTbWash.setChecked(false);
    }


    /**
     * @param lock true: 切换到表情模式  false : 切换到操作模式
     */
    private void lockContentView(boolean lock) {
        if (isLocked == lock) {
            return;
        }
        isLocked = lock;
        if (lock) {
            ImageView ivFace = new ImageView(this);
            ivFace.setImageResource(R.drawable.face_smile);
            ivFace.setScaleType(ScaleType.FIT_CENTER);
            setContentView(ivFace);
        } else {
            //TODO 如果在操作界面下 执行以下代码, 报错?
            setContentView(currentView);
        }
    }

    /**
     * 通过USB发送查询指令,初始化各个状态
     * 1. USB连接状态
     * 2. 电量状态
     * 3. 当前已选任务
     * 4. 当前运动状态
     */
    private void initArmState() {
        resetActivity();

        // 初始化 USB链接状态
        SystemHandler.getInstance().queryUsbConnectStatus();

        //查询当前任务
        PathHandler.getInstance().queryTarget();

        // 初始化电量信息 ( 默认为无法获取)
        PowerHandler.getInstance().queryCurrentPower();

        //查询运动状态
        MotionHandler.getInstance().queryMotionState();
    }


    public void updateHomeMenuBtn(boolean isNormal) {
        if (isNormal) {
            mIbHome.setBackgroundResource(R.drawable.uniform_blue_button);
        } else {
            mIbHome.setBackgroundResource(R.drawable.uniform_red_button);
        }
    }

    private void resetActivity() {
        mExpanderAdapter.clearTask();
        if (mIFragmentController != null) {
            mIFragmentController.clearTask();
        }
        warningDialog.clearWarning();
        mTbExecute.setChecked(false);
        mTbWash.setChecked(false);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (prepareExit) {
                this.finish();
            } else {
                prepareExit = true;
                T.show("再按一次 退出系统");
                handler.sendEmptyMessageDelayed(PREPARE_EXIT, 3000);
            }
            return true;
        }
        //TODO 方便测试使用 记得删除!
//        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//            lockContentView(false);
//            return true;
//        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//            lockContentView(true);
//            return true;
//        }
        return super.onKeyDown(keyCode, event);
    }

    //region 更新 左滑菜单的图标

    private void updatePowerState(int percent) {
        if (percent <= 100 && percent > 80) {
            mIconBtnPower.setIconDrawableID(R.mipmap.drawer_menu_power100);
        } else if (percent <= 80 && percent > 60) {
            mIconBtnPower.setIconDrawableID(R.mipmap.drawer_menu_power80);
        } else if (percent <= 60 && percent > 40) {
            mIconBtnPower.setIconDrawableID(R.mipmap.drawer_menu_power60);
        } else if (percent <= 40 && percent > 20) {
            mIconBtnPower.setIconDrawableID(R.mipmap.drawer_menu_power40);
        } else if (percent <= 20 && percent >= 0) {
            mIconBtnPower.setIconDrawableID(R.mipmap.drawer_menu_power20);
        } else {
            //无法获取电源信息
            mIconBtnPower.setIconDrawableID(R.mipmap.drawer_menu_power100);
            mIconBtnPower.setText(R.string.powerUnknown);
            return;
        }
        mIconBtnPower.setText(" " + percent + "%");
    }

    private void updateUsbState(boolean isConnected) {
        mIconBtnUsb.setIconDrawableID(isConnected ? R.mipmap.drawer_menu_usb_connect : R.mipmap.drawer_menu_usb_disconnect);
        mIconBtnUsb.setText(isConnected ? R.string.usbConnect : R.string.usbDisconnect);
    }

    private void updateWarningState(int warningCount) {
        if (warningCount == 0) {
            mIconBtnWarning.setIconDrawableID(R.mipmap.drawer_menu_warn);
            mIconBtnWarning.setVisibility(View.GONE);
        } else {
            mIconBtnWarning.setIconDrawableID(R.mipmap.drawer_menu_warn);
            mIconBtnWarning.setText(warningCount + " 条报警信息");
            mIconBtnWarning.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 更新连接调度系统的状态
     * 3种状态... 未连接Socket, 已连接socket, 已成功Login
     *
     * @param isSocketConnect 是否成功链接到调度系统
     * @param isLogined       是否已登录
     */
    private void updateScheduleState(boolean isSocketConnect, boolean isLogined) {
        if (!isSocketConnect) {
            mIconBtnSchedule.setIconDrawableID(R.mipmap.drawer_menu_schedule_disconnect);
            mIconBtnSchedule.setText(R.string.ScheduleDisconnect);
        } else {
            if (isLogined) {
                mIconBtnSchedule.setIconDrawableID(R.mipmap.drawer_menu_schedule_logined);
                mIconBtnSchedule.setText(R.string.ScheduleLogin);
            } else {
                mIconBtnSchedule.setIconDrawableID(R.mipmap.drawer_menu_schedule_connect);
                mIconBtnSchedule.setText(R.string.ScheduleConnect);
            }
        }
    }
    //endregion

    /**
     * 用户准备 添加任务,分3种情况,
     * 最开始: 如果是在停靠点...只能到厨房任务
     * 第一种是:机器人处于空闲状态(在厨房,且没有任务,且不在运动中),直接将所选任务显示到界面上,当按下执行后才统一发送所添加的任务..;
     * 另一种是:当机器人处于运行状态时,直接发送添加任务的指令,不更新界面,在回调中更新界面;
     *
     * @param node 用户点击的节点
     */
    @Override
    public void onUserAdd(Node node) {
        // 如果是在停靠点
        Node locationNode = mDatabaseHelper.getNodeByID(sRobot.getLocation());
        if (locationNode == null) {
            final List<Node> stationList = mDatabaseHelper.getNodeByType(NODE_TYPE.STATION);
            if (stationList == null || stationList.size() == 0) {
                T.show("无法获取停靠点位置");
            } else {
                // 弹出对话框让用户选择当前机器人的停靠点
                final View dialogView = View.inflate(MainActivity.this, R.layout.dialog_node_simple, null);

                final Spinner spStationNode = (Spinner) dialogView.findViewById(R.id.spStationNode);
                String[] selections = new String[stationList.size()];
                for (int i = 0; i < selections.length; i++) {
                    selections[i] = stationList.get(i).getName();
                }
                ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, selections);
                spStationNode.setAdapter(adapter);

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("请给机器人选择一个停靠点")
                        .setIcon(R.mipmap.fab_btn_map_add_station)
                        .setView(dialogView)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Node node = stationList.get(spStationNode.getSelectedItemPosition());
                                PathHandler.getInstance().setStationPoints(node.getId());
                                sRobot.setLocation(node.getId());
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
            return;
        }

        if (NODE_TYPE.STATION.equals(locationNode.getType()))

        {
            // 只能前往厨房
            if (!NODE_TYPE.KITCHEN.equals(node.getType())) {
                T.show("当前在停靠点，请先前往厨房。");
                return;
            } else {
                // 前往厨房
                mExpanderAdapter.addTarget(node);
                if (mIFragmentController != null) {
                    mIFragmentController.addTask(node);
                }
                return;
            }
        }
        //第一种情况 ( 只操作界面)
        if (!sRobot.hasTask() && NODE_TYPE.KITCHEN.equals(locationNode.getType())
                && !ButtonHandler.getInstance().

                isExecute()

                )

        {
            mExpanderAdapter.addTarget(node);
            if (mIFragmentController != null) {
                mIFragmentController.addTask(node);
            }
        }
        // 第二种情况 (只发送USB请求)
        else

        {
            PathHandler.getInstance().addTargets(node.getId());
        }

    }

    /**
     * 用户准备删除 任务.
     * 如果该任务已经 设置到ARM，不能直接取消，发送取消指令给ARM, 通过回调 handlerReceive来取消;
     * 如果该任务未设置到ARM,更新左边的任务列表和中间的GridView
     *
     * @param node 用户点击的节点
     */
    @Override
    public void onUserDel(@NonNull Node node) {
        if (sRobot.hasTask(node.getId())) {
            PathHandler.getInstance().delTargets(node.getId());
        } else {
            mExpanderAdapter.delTarget(node);
            if (mIFragmentController != null) {
                mIFragmentController.delTask(node);
            }
        }
    }

    @Override
    public void onUserAddToExpander(int groupPosition) {
        mTaskListView.expandGroup(groupPosition);

        mTaskListView.smoothScrollToPosition(groupPosition);
    }

    @Override
    public void onUserDelFromExpander(Node node) {
        // 和 Fragment中操作一样
        this.onUserDel(node);
    }
}
