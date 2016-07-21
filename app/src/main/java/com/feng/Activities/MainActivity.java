
package com.feng.Activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.ImageView.ScaleType;
import com.feng.Adapter.DrawerMenuAdapter;
import com.feng.Adapter.ExpanderAdapter;
import com.feng.Base.BaseActivity;
import com.feng.Constant.ArmProtocol;
import com.feng.Constant.I_Parameters;
import com.feng.Constant.RobotEntity;
import com.feng.CustomView.WarningDialogCallback;
import com.feng.Database.MapDatabaseHelper;
import com.feng.Database.Node;
import com.feng.Database.Workspace;
import com.feng.Fragments.IFragmentControl;
import com.feng.Fragments.ListingFragment;
import com.feng.Fragments.UserControlFragmentCallback;
import com.feng.Fragments.WorkspaceFragment;
import com.feng.MapModule.MapFragment;
import com.feng.RSS.R;
import com.feng.RobotApplication;
import com.feng.Schedule.ScheduleClient;
import com.feng.Schedule.ScheduleProtocal;
import com.feng.SpeechRecognize.IatService;
import com.feng.Usb.ArmUsbUtil;
import com.feng.Usb.UsbData;
import com.feng.Usb.UsbEvent;
import com.feng.UserManage.PasswordManager;
import com.feng.Utils.L;
import com.feng.Utils.SP;
import com.feng.Utils.T;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseActivity implements I_Parameters, ArmProtocol, ScheduleProtocal {
    private final static String TAG = MainActivity.class.getSimpleName();

    private MapDatabaseHelper mDatabaseHelper;

    private ScheduleClient mScheduleClient;
    private ArmUsbUtil mArmUsbUtil;

    private ToggleButton tbWash, tbExecute;
    //点击LOGO图标返回工作区
    private ImageButton ibHomeMenu;


    private ExpandableListView taskListView;
    private ExpanderAdapter mExpanderAdapter;

    private IFragmentControl mIFragmentControl;
    private WorkspaceFragment workspaceFragment;
    private boolean IS_WORKSPACE_FRAGMENT = false;

    private boolean isLocked; // 是否处于 表情界面
    private boolean prepareExit; // 是否已经在3 秒内按下过 back键,再按则退出
    //当前位置 , 当前目标, 当前清洗区
    private Node currentNode, targetNode, currentWash;

    // 左滑菜单的布局... 用来控制 菜单的 伸和收缩
    private DrawerLayout mDrawerLayout;
    private DrawerMenuAdapter mDrawerMenuAdatper;

    private RobotEntity sRobot;

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
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                if (msg.obj == null) {
                    return;
                }
                UsbData data = (UsbData) msg.obj;
                UsbEvent event = data.getUsbEvent();
                byte[] dataReceive = data.getDataReceive();
                byte[] dataSend = data.getDataToSend();

                switch (event) {
                    case UsbConnect:
                        mDrawerMenuAdatper.updateUsbState(true);
                        initArmState();
                        break;

                    case UsbConnectFailed:
                    case UsbDisconnect:
                        mDrawerMenuAdatper.updateUsbState(false);
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
                    mDrawerMenuAdatper.updateUsbState(false);
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
            switch (action) {
                case CURRENT_RFID:
                    int nodeID = transfer.byteToInt(transfer.getBody(dataReceive));
                    Node currentNode = mDatabaseHelper.getNodeByID(nodeID);
                    if (currentNode == null) {
                        L.e(TAG, "[OnReceiveRFID]读到错误的RFID点...");
                        break;
                    }
                    arriveNode(currentNode);
                    handler.sendEmptyMessage(REFRESH);

                    // 通知调度
                    sRobot.setLocation(currentNode);
                    if (mScheduleClient != null && mScheduleClient.isConnect()) {
                        mScheduleClient.updateStatus(sRobot);
                    }

                    break;

                case CURRENT_PATH:
                    // 首位: 总路径数,  剩余: 节点ID
                    try {
                        byte[] currentPathByte = transfer.getBody(dataReceive);
                        int pathNum = currentPathByte[0];
                        int[] paths = new int[pathNum];
                        for (int i = 0; i < pathNum; i++) {
                            paths[i] = transfer.byteToInt(new byte[]{currentPathByte[i * 2 + 1], currentPathByte[i * 2 + 2]});
                        }
                        sRobot.setPaths(paths);
                        if (mScheduleClient != null && mScheduleClient.isConnect()) {
                            mScheduleClient.updateStatus(sRobot);
                        }

                    } catch (IndexOutOfBoundsException e) {
                        Log.e(TAG, "handleReceive: [CurrentPath] 传来的数据有误");
                    }
                    break;


                case MACHINE_START_BTN:
                    // 不发送数据,只切换状态!
                    // 如果是 接收到的是执行. 切换按键为 true
                    if (transfer.getFlag(dataReceive)) {
                        tbExecute.setChecked(true);
                    } else {
                        // 如果接收到 暂停. 切换按键为 false( 显示的字体是 [执行])
                        //  但此时不可用( 交给外设按键操作)
                        tbExecute.setChecked(false);
                    }
                    break;

                case MACHINE_STOP_BTN:
                    tbExecute.setChecked(false);
                    break;

                case ROBOT_REBOOT:
                    initArmState();
                    break;

                default:
                    return false;
            }
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
            switch (action) {
                case QUERY_STATE:
                    byte[] detailData = transfer.getBody(dataReceive);
                    if (detailData[0] == (byte) 0x00) {
                        //停止中
                        tbExecute.setChecked(false);
                        sRobot.setState(RobotEntity.RobotState.FREE);
                    } else if (detailData[0] == (byte) 0x01 ||
                            detailData[0] == (byte) 0x03) {
                        //形式中  , 转向中
                        tbExecute.setChecked(true);
                        sRobot.setState(RobotEntity.RobotState.MOVING);
                    } else if (detailData[0] == (byte) 0x02) {
                        // 原地转向
                        tbExecute.setChecked(false);
                        sRobot.setState(RobotEntity.RobotState.WAITING);
                    } else if (detailData[0] == (byte) 0x04) {
                        //故障
                        tbExecute.setChecked(false);
                        sRobot.setState(RobotEntity.RobotState.ERROR);
                    }

                    if (mScheduleClient != null && mScheduleClient.isConnect()) {
                        mScheduleClient.updateStatus(sRobot);
                    }

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
                        tbExecute.setChecked(isSetMove);
                    }
                    //	 进入锁屏界面
//                    lockContentView(false);
                    break;
                case QUERY_TARGET:
                    List<Node> currentTargets = mArmUsbUtil.getNodesFromByte(dataReceive);
                    mExpanderAdapter.clearTask();
                    mExpanderAdapter.addTarget(currentTargets);
                    // 具体的工作区界面可能还未初始化
                    if (mIFragmentControl != null) {
                        mIFragmentControl.clearTask();
                        mIFragmentControl.addTask(currentTargets);
                    }
                    updateWashBtn();

                    // 更新调度
                    sRobot.setTasks(getIntTaskList());
                    if (mScheduleClient != null && mScheduleClient.isConnect()) {
                        mScheduleClient.updateStatus(sRobot);
                    }
                    break;


                //添加任务成功
                case ADD_TARGETS:
                    //成功添加的节点s
                    List<Node> addTargets = mArmUsbUtil.getNodesFromByte(dataSend);
                    //向任务列表 添加任务
                    mExpanderAdapter.addTarget(addTargets);
                    // 向Fragment添加任务..(fragment有判断,如果是撤台Node,不添加)
                    if (mIFragmentControl != null) {
                        mIFragmentControl.addTask(addTargets);
                    }
                    //更新撤台按键状态
                    updateWashBtn();

                    sRobot.setTasks(getIntTaskList());
                    if (mScheduleClient != null && mScheduleClient.isConnect()) {
                        mScheduleClient.updateStatus(sRobot);
                    }
                    break;

                case DEL_TARGETS:
                    // 成功删除的节点s
                    List<Node> delTargets = mArmUsbUtil.getNodesFromByte(dataSend);
                    mExpanderAdapter.delTarget(delTargets);
                    if (mIFragmentControl != null) {
                        mIFragmentControl.delTask(delTargets);
                    }
                    updateWashBtn();

                    sRobot.setTasks(getIntTaskList());
                    if (mScheduleClient != null && mScheduleClient.isConnect()) {
                        mScheduleClient.updateStatus(sRobot);
                    }
                    break;

                case QUERY_POWER:
                    byte[] percent = transfer.getBody(dataReceive);
                    L.e(TAG, "更新电量为 :" + Arrays.toString(percent));
                    mDrawerMenuAdatper.updatePowerState(percent[0]);
                    break;

                case QUERY_USB_STATE:
                    // 收到ARM回复,表示连接正常
                    mDrawerMenuAdatper.updateUsbState(true);
                    break;

                default:
                    return false;
            }
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
        setContentView(currentView);

        //初始化应用级对象 (机器人实体,USB通信,,调度通信)
        sRobot = RobotEntity.getInstance();
        mArmUsbUtil = RobotApplication.getArmUsbUtil();
        mArmUsbUtil.addObserver(TAG, new MainUsbHandler(this));
        // 调度系统客户端对象
        mScheduleClient = RobotApplication.getScheduleClient();
        //初始化 处理调度系统命令的handler
        this.initScheduleHandler();

//        initIAT();
        initView();
        initFragment();

        //初始化任务列表
        initExpanderListView();
        //初始化左滑菜单
        initDrawerListView();
        //初始化当前位置
        initCurrentNode();
        // 初始化ARM状态
        initArmState();
        // 初始化调度系统链接状态
        mDrawerMenuAdatper.updateScheduleState(mScheduleClient.isConnect(), mScheduleClient.getLoginState());

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
                    String command = json.getString(ScheduleProtocal.COMMAND);
                    if (command != null) {
                        L.i(TAG, "接收到调度服务端指令: " + command);
                        switch (command) {
                            case RobotCommand.COMMAND_MOVE:
                                // 命令机器人继续行走...
                                mArmUsbUtil.setMove(true);
                                break;
                            case RobotCommand.COMMAND_STOP:
                                //命令机器人停止行走
                                mArmUsbUtil.setMove(false);
                                break;
                            default:
                                L.e(TAG, "未知的调度服务端指令");
                                break;
                        }
                    }
                    /**
                     * 添加任务
                     */
                    if (json.has(ADD_TASKS) && !json.isNull(ADD_TASKS)) {
                        JSONArray addTaskArray = json.getJSONArray(ADD_TASKS);
                        if (addTaskArray != null && addTaskArray.length() > 0 && mArmUsbUtil != null) {
                            List<Node> nodeList = new ArrayList<>();
                            for (int i = 0; i < addTaskArray.length(); i++) {
                                Node node = mDatabaseHelper.getNodeByID(addTaskArray.getInt(i));
                                if (node != null) {
                                    nodeList.add(node);
                                }
                            }
                            mArmUsbUtil.addTarget(nodeList);
                        }
                    }

                    /**
                     * 删除任务
                     */
                    if (json.has(DEL_TASKS) && !json.isNull(DEL_TASKS)) {
                        JSONArray delTaskArray = json.getJSONArray(DEL_TASKS);
                        if (delTaskArray != null && delTaskArray.length() > 0 && mArmUsbUtil != null) {
                            List<Node> nodeList = new ArrayList<>();
                            for (int i = 0; i < delTaskArray.length(); i++) {
                                Node node = mDatabaseHelper.getNodeByID(delTaskArray.getInt(i));
                                if (node != null) {
                                    nodeList.add(node);
                                }
                            }
                            mArmUsbUtil.delTarget(nodeList);
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
     * 读到RFID卡的处理..分2种情况:
     */
    private void arriveNode(Node currentNode) {
        //  mExpanderAdapter.delTarget(node)  ---> 返回 true: 是任务点 ; 返回 false : 非任务点.
        if (mExpanderAdapter.delTarget(currentNode)) {
            // 同时更新 fragment...
            if (mIFragmentControl != null) {
                mIFragmentControl.delTask(currentNode);
            }
            L.i(TAG, "到达目的点 : " + currentNode.getName());
            T.show(" 已到达 : " + currentNode.getName());
            intentDealer.sendTtsIntent(TTS_START_SPEAK, " 已到达 : " + currentNode.getName());
            lockContentView(false);
        } else {
            L.i(TAG, "经过点 : " + currentNode.getName());
            setCurrentNode(currentNode);
        }

        sRobot.setTasks(getIntTaskList());

        //通知调度系统..
        if (mScheduleClient != null) {
            mScheduleClient.updateStatus(sRobot);
        }
    }

    /**
     * @return 是否找到初始位置
     */
    private boolean initCurrentNode() {
        //从SP中获取当前位置  并且需要支持 手动设置当前位置
        if (getCurrentNodeFromSP() != null) {
            currentNode = getCurrentNodeFromSP();
            Workspace currentWorkspace = mDatabaseHelper.getWorkspaceByID(currentNode.getWorkspaceID());
            // 如果 设置中 打开了  直接进入上次工作区
            if ((boolean) SP.get(this, AUTO_ENTER_WORKSPACE, false)) {
                changeToDetailFragment(currentWorkspace.getId());
            }
            return true;
        } else {
            // 第一次使用时
            if (mDatabaseHelper.getAllWorkspace().size() <= 0) {
                // 不存在工作区: 退出;
                T.show("无法定位:未找到任何工作区,请前往地图编辑");
                return false;
            } else {
                //默认 工作区
                Workspace workspace = mDatabaseHelper.getAllWorkspace().get(0);
                if (mDatabaseHelper.getAllNode(workspace.getId()).size() <= 0) {
                    T.show("未找到 任何节点,无法确定机器人当前位置");
                    return false;
                } else {
                    Node node = mDatabaseHelper.getAllNode(workspace.getId()).get(0);
                    currentNode = node;
                    // 保存到SP中
                    this.setCurrentNode(node);
                    T.show("无法确定机器人当前位置,已初始化到\n" + workspace.getName() + " : 节点 " + node.getName() + " 处");
                    return true;
                }
            }
        }
    }

    /**
     * 读到卡号后 更新, 或者手动 设置(非 EDIT模式下长按 节点)
     *
     * @param node
     */
    public void setCurrentNode(Node node) {
        currentNode = node;
        // 将 ID保存到SP中, 下次打开直接 .获取
        SP.put(this, CURRENT_NODE_ID, currentNode.getId());
        //		T.showLong(" 已将位置定位到 "+node.getName() );
        //		updateExecuteBtn();
    }

    /**
     * 将  SP 中的 nodeID 取出, 并获取 为 Node
     *
     * @return
     */
    private Node getCurrentNodeFromSP() {
        Node node = mDatabaseHelper.getNodeByID((int) SP.get(this, CURRENT_NODE_ID, -1));
        return node;
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
        initCurrentNode();
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
                            mDrawerMenuAdatper.updateScheduleState(true, false);
                            break;

                        case ScheduleClient.SOCKET_DISCONNECT:
                            mDrawerMenuAdatper.updateScheduleState(false, false);
                            break;

                        case ScheduleClient.LOGIN_SUCCESS:
                            mDrawerMenuAdatper.updateScheduleState(true, true);
                            break;

                        case ScheduleClient.LOGIN_FAILED:
                            mDrawerMenuAdatper.updateScheduleState(true, false);
                            break;

                        case ScheduleClient.MAP_UPDATE:
                            mIFragmentControl = null;
                            workspaceFragment = null;
                            changeToMenuFragment();
                            break;
                    }
                    super.handleMessage(msg);
                }
            });
        }
        if (mIFragmentControl != null) {
            FragmentManager fragmentManager = getFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.hide((Fragment) mIFragmentControl);
            fragmentTransaction.commit();
            mIFragmentControl = null;
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
        fragmentTransaction.replace(R.id.taskFragment, workspaceFragment);
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
        boolean isListingMode = (boolean) SP.get(this, IS_MAP_MODEL, true);
        boolean isMatching = isListingMode ?
                mIFragmentControl instanceof ListingFragment : mIFragmentControl instanceof MapFragment;

        if (mIFragmentControl != null) {
            if (!isMatching) {
                //如果不为空,但是 模式不匹配,则销毁当前fragment并新建
                fragmentTransaction.remove((Fragment) mIFragmentControl);
                mIFragmentControl = isListingMode ?
                        (IFragmentControl) ListingFragment.getInstance(wspId, getIntTaskList())
                        : new MapFragment();
            }
        } else {
            // 初始化 新的 fragment
            mIFragmentControl = isListingMode ?
                    (IFragmentControl) ListingFragment.getInstance(wspId, getIntTaskList())
                    : new MapFragment();

            fragmentTransaction.add(R.id.taskFragment, (Fragment) mIFragmentControl);
        }

        /**
         * 配置 fragment参数, 此时fragment的onCreateView还未调用...
         */
        // 设置回调...当用户点击 fragment中的元素时调用
        mIFragmentControl.setCallback(new UserControlFragmentCallback() {
            @Override
            public void onUserAdd(Node node) {
                mArmUsbUtil.addTarget(node);
            }

            @Override
            public void onUserDel(Node node) {
                mArmUsbUtil.delTarget(node);
            }
        });
        fragmentTransaction.show((Fragment) mIFragmentControl);
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
        if (mIFragmentControl != null) {
            fragmentTransaction.hide((Fragment) mIFragmentControl);
        }
        if (workspaceFragment == null) {
            workspaceFragment = new WorkspaceFragment();
            fragmentTransaction.replace(R.id.taskFragment, workspaceFragment);
        }
        fragmentTransaction.show(workspaceFragment);
        fragmentTransaction.commit();
        IS_WORKSPACE_FRAGMENT = true;
    }

    //初始化 按键
    private void initView() {
        //执行按键 extend from Button
        tbExecute = (ToggleButton) findViewById(R.id.tbExecute);
        tbWash = (ToggleButton) findViewById(R.id.tbWash);
        ibHomeMenu = (ImageButton) findViewById(R.id.ibHome);


        HomePageOnClickListener clickListener = new HomePageOnClickListener();
        tbExecute.setOnClickListener(clickListener);
        tbWash.setOnClickListener(clickListener);
        ibHomeMenu.setOnClickListener(clickListener);
        // 左滑菜单中的设置按键
        findViewById(R.id.btnSetting).setOnClickListener(clickListener);
        // 左滑菜单中的退出按键
        findViewById(R.id.btnQuit).setOnClickListener(clickListener);
        //清空 按键
        findViewById(R.id.btnClearTask).setOnClickListener(clickListener);
        // 刷新 按键
        findViewById(R.id.btnRefreshTask).setOnClickListener(clickListener);


        warningDialog.setOnWarningChangeListenner(new WarningDialogCallback() {
            public void onWarningChange(int warningNum) {
                if (warningNum == 0) {
                    updateHomeMenuBtn(true);
                } else {
                    updateHomeMenuBtn(false);
                }
                mDrawerMenuAdatper.updateWarningState(warningNum);
            }

            public void onAddWarning(String ttsStr) {
                intentDealer.sendTtsIntent(TTS_START_SPEAK, ttsStr);
            }
        });
    }

    private void initDrawerListView() {
        // 左滑菜单 初始化
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ListView lvDrawerMenu = (ListView) findViewById(R.id.lvDrawerMenu);
        mDrawerMenuAdatper = new DrawerMenuAdapter(this);
        lvDrawerMenu.setAdapter(mDrawerMenuAdatper);
        DrawerMenuClickListener menuClickListener = new DrawerMenuClickListener();
        mDrawerMenuAdatper.setClickListnner(menuClickListener);
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

        mExpanderAdapter = new ExpanderAdapter(this, new ExpanderAdapter.TaskChangeCallback() {
            @Override
            public void onTaskAdd(int groupPostion) {
                // 展开该Group...
                taskListView.expandGroup(groupPostion);
                //TODO 滚动到新增点的位置
                taskListView.smoothScrollByOffset(groupPostion);
            }

            @Override
            public void onDeleteTask(Node node) {
                mArmUsbUtil.delTarget(node);
            }
        });


        taskListView.setAdapter(mExpanderAdapter);
        // 设置group的 伸缩图标
        //		taskListView.setGroupIndicator(getResources().getDrawable(R.drawable.route));
        taskListView.setGroupIndicator(null);
    }

    /**
     * 更新任务列表,通过 node对象
     * 先 查找 是否已有node, 在进行更新 列表
     * int currentRouteID = 0;
     * // 获取 到达该节点的所有路线
     */
    public void updateTasklist(Node node) {
        mExpanderAdapter.addTarget(node);
        //更新 撤台按键的状态
        updateWashBtn();
        //无论如何 刷新UI
        handler.sendEmptyMessage(REFRESH);
    }


    /**
     * 完成任务 或者 删除任务..
     *
     * @param node
     * @return 删除任务是否成功
     */
    public boolean deleteTask(Node node) {
        boolean deleceResult = false;
        // refresh ExpandableListView
        deleceResult = mExpanderAdapter.delTarget(node);
        // send to ARM
        mArmUsbUtil.delTarget(node);

        // send to Schedule
//        mScheduleClient.updateTask(getIntTaskList(), null);

        return deleceResult;
    }

    // 如果有 清洗去被选中... 将撤台按键设为选定状态
    private void updateWashBtn() {
        List<Node> washNodes = mDatabaseHelper.getWashNode();
        if (washNodes == null) {
            return;
        }
        //遍历所有已选任务
        for (Node node : mExpanderAdapter.getTasks()) {
            //遍历所有 清洗区
            for (Node washNode : washNodes) {
                if (node.equals(washNode)) {
                    tbWash.setChecked(true);
                    return;
                }
            }
        }
        tbWash.setChecked(false);
    }


    /**
     * 左滑 菜单中按键的 点击事件
     */
    private class DrawerMenuClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            switch ((int) v.getTag()) {
                case DrawerMenuAdapter.BACK_BTN:
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    break;
                case DrawerMenuAdapter.SCHEDULE_BTN:
                    if (mScheduleClient == null) {
                        mScheduleClient = RobotApplication.getScheduleClient();
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
                case DrawerMenuAdapter.USB_BTN:
                    mArmUsbUtil.query(QueryUsbState);
                    break;
                case DrawerMenuAdapter.POWER_BTN:
                    mArmUsbUtil.query(QueryPower);
                    break;
                case DrawerMenuAdapter.WARNNING_BTN:
                    if (warningDialog.getWarningCount() != 0) {
                        warningDialog.showDialog();
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                    }
                    break;
            }
        }
    }

    private class HomePageOnClickListener implements OnClickListener {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.tbExecute:
                    //如果还未按下 --> 显示的是 执行, isChecked()为false
                    // 点击后,系统会先改变 checked 为true , 所以要判断是否 为true
                    if (tbExecute.isChecked()) {
                        //点击后先不改变状态
                        tbExecute.setChecked(false);
                        // 让USB发送 , 收到回复后, 在onReceive中会改变 按键的状态
                        mArmUsbUtil.setMove(true);
                    } else {
                        tbExecute.setChecked(true);
                        mArmUsbUtil.setMove(false);
                    }
                    break;

                case R.id.tbWash:
                    // 按下撤台后, 查找清洗区的NODE
                    //1.如果找到,添加到列表,
                    //2 如果没找到, 提示出错
                    //TODO 工作区ID如何获取
                    //点击后先不改变状态 ( 点击后,获取getChecked是已经改变过的,所以设置为!相反)
                    tbWash.setChecked(!tbWash.isChecked());
                    List<Node> nodeList = mDatabaseHelper.getWashNode();
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
                        if (tbWash.isChecked()) {
                            mArmUsbUtil.delTarget(node);
                        } else {
                            mArmUsbUtil.addTarget(node);
                        }
                        break;
                    }


                case R.id.btnClearTask:
                    if (tbExecute.isChecked()) {
                        T.show("请先停止执行,再操作");
                        break;
                    }
                    // 清空所有的任务
                    mArmUsbUtil.delTarget(mExpanderAdapter.getTasks());
                    // 应该等ARM回复后再操作 任务列表
//                    mExpanderAdapter.clearTask();

                    break;

                case R.id.btnSetting:
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    PasswordManager.login(MainActivity.this);
                    break;


                case R.id.ibHome:
                    //				changeFragment(false, null);
                    if (!IS_WORKSPACE_FRAGMENT) {
                        changeToMenuFragment();
                    }
                    break;

                case R.id.btnQuit:
                    MainActivity.this.finish();
                    break;


                case R.id.btnRefreshTask:
                    //TODO 先改为测试按键
//				intentDealer.sendIntent(USB_SEND,QueryTarget);
//				T.show("ToastTest: "+ new Random().nextInt()+"");
//				T.show("ToastTest: "+ new Random().nextInt()+"",R.drawable.warning_none);
//				startActivity( new Intent(MainActivity.this,SettingsActivity.class) );
//                    mScheduleClient.login();

                    break;

                default:
                    break;
            }
        }
    }

    /**
     * @param lock true: 切换到表情模式  false : 切换到操作模式
     */
    private void lockContentView(boolean lock) {
        if (isLocked == lock) {
            return;
        }
        isLocked = lock;
        if (lock == true) {
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
        mArmUsbUtil.query(QueryUsbState);

        //查询当前任务
        mArmUsbUtil.query(QueryTarget);

        // 初始化电量信息 ( 默认为无法获取)
        mArmUsbUtil.query(QueryPower);

        //查询运动状态
        mArmUsbUtil.query(QueryState);
    }


    public void updateHomeMenuBtn(boolean isNormal) {
        if (isNormal == true) {
            ibHomeMenu.setBackgroundResource(R.drawable.uniform_blue_button);
        } else {
            ibHomeMenu.setBackgroundResource(R.drawable.uniform_red_button);
        }
    }

    private void resetActivity() {
        mExpanderAdapter.clearTask();
        if (mIFragmentControl != null) {
            mIFragmentControl.clearTask();
        }
        warningDialog.clearWarning();
        tbExecute.setChecked(false);
        tbWash.setChecked(false);
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

}
