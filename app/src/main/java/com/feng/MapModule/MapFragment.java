package com.feng.MapModule;

import android.app.Fragment;
import android.app.Service;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.renderscript.Int2;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.PopupWindow;
import com.feng.Base.BaseActivity;
import com.feng.Constant.I_Parameters;
import com.feng.CustomView.CustomDialog;
import com.feng.CustomView.CustomDialogCallback;
import com.feng.CustomView.IconButton;
import com.feng.Database.MapDatabaseHelper;
import com.feng.Database.Node;
import com.feng.Database.Route;
import com.feng.Database.Workspace;
import com.feng.Fragments.IFragmentControl;
import com.feng.Fragments.UserControlFragmentCallback;
import com.feng.RSS.R;
import com.feng.RobotApplication;
import com.feng.Utils.L;
import com.feng.Utils.WidgetController;

import java.util.Arrays;
import java.util.List;

/**
 * 地图编辑模块 view视图层次如下:
 * EditMapActivity
 * -LinearLayout: menuLayout,左侧的路线选择层,将阻断Touch事件的传递(在该层的操作不会传递给下方的MapLayout)
 * -Fragment 本类 ( 与menuLayout同级)
 * --FrameLayout : mapLayout,全屏的地图编辑层,实现了Gesture接口,接收OnLongClick-添加按键
 * ---Button : 属于FrameLayout..接收LongClick, Click
 * ---Path: 属于FrameLayout..显示动态路径
 */
public class MapFragment extends Fragment implements I_Parameters, IFragmentControl {

    private static final String LOG = MapFragment.class.getSimpleName();
    // MVC 中的 Model
    private MapDatabaseHelper mDatabaseHelper = MapDatabaseHelper.getInstance();
    // 节点图标的大小
    private int mIconBtnSize;
    private int mIconSize = 20;
    private int mTextSize = 15;


    //    private static boolean isEdit; // true时:支持编辑地图,并显示所有节点; false:不显示交叉点,并有不同的点击响应
    //当前工作区
    private Workspace mWorkspace;
    // 当前所选路线
    private Route mRoute;
    //当前工作区的所有节点
    private List<Node> mAllNodes;

    private FrameLayout mapLayout;
    private View mView;
    // 显示当前的路线的  路径
    private PathView mPathView;
    private boolean hasDialog;
    // 保存当前的 节点按键,用来获取宽度
//    private Map<Node, IconButton> nodeBtnMap = new HashMap<>();

    // 提供的public刷新方法
    public void refreshAll() {
        refreshHandler.sendEmptyMessage(RefreshPath);
        refreshHandler.sendEmptyMessage(RefreshAll);
        refreshHandler.sendEmptyMessage(AutoRefresh);
    }

    public void refreshPath() {
        refreshHandler.sendEmptyMessage(RefreshPath);
    }

    private final static int RefreshPath = 1, RefreshAll = 2, AutoRefresh = 3;

    //region 刷新界面的Handler
    Handler refreshHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                // 只刷新 PathView
                case RefreshPath:
                    mPathView.invalidate();
                    break;

                // 刷新所有控件
                case RefreshAll:
                    mapLayout.removeAllViews();
                    //重新添加 path连线
                    mapLayout.addView(mPathView);
                    mPathView.initData(mWorkspace.getId());
                    mPathView.invalidate();
                    //重新添加 btn
                    mDatabaseHelper.getAllNode(mWorkspace.getId());
                    showButtons();
                    break;

                case AutoRefresh:
                    this.removeMessages(AutoRefresh);
                    mPathView.invalidate();
                    this.sendEmptyMessageDelayed(AutoRefresh, 25);
                    break;
            }
        }
    };
    //endregion


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_map, container, false);
        mapLayout = (FrameLayout) mView.findViewById(R.id.mapFrameLayout);

        mWorkspace = mDatabaseHelper.getWorkspaceByID(getArguments().getInt("workspaceID"));
        mRoute = mDatabaseHelper.getRouteByID(getArguments().getInt("routeID"));
        // 获取当前工作区内的所有节点
        mAllNodes = mDatabaseHelper.getAllNode(mWorkspace.getId());
        // 初始化缩放比
//        initScale();

        //初始化 路径图层
        mPathView = new PathView(this.getActivity());
        mPathView.setMainPathWidth((float) (mIconSize * 0.6));
        mPathView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (mRoute != null) {
            mPathView.initData(mWorkspace.getId(), mRoute.getId());
        } else {
            mPathView.initData(mWorkspace.getId());
        }
        // 设置pathView的偏移量, 并不准确, 要获取iconBtn的高度才行
        mPathView.setOffset(0, -mIconSize / 2);
        // 为了frameLayout的gesture能正确响应.需要设置为可点击的
        mapLayout.setClickable(true);
        mapLayout.addView(mPathView);
        // 开始循环刷新,动态绘制路径
        refreshHandler.sendEmptyMessage(AutoRefresh);


        // 显示按键图层 (似乎要在添加PathView后 添加button,否则点击事件会被阻挡)
        showButtons();

        //添加背景 , 如果还未设置,则显示logo
        if (mWorkspace.getMapPic() != null) {
            mapLayout.setBackgroundDrawable((new BitmapDrawable(getResources(), mWorkspace.getMapPic())));
        } else {
            mapLayout.setBackgroundResource(R.drawable.round_white_gradient);
        }

        // 自定义的 gesture监听类
        GestureDetector.OnGestureListener layoutGestureListener = new FrameLayoutGestureListener();
        // 系统的 gesture检测类
        final GestureDetector gestureDetector = new GestureDetector(RobotApplication.getContext(), layoutGestureListener);
        // 将 onTouch的事件交给 gesture处理
        mapLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });

        hasDialog = false;

        return mView;
    }

    /**
     * 从数据库获取节点, 并添加节点到界面上 , 并且为每个节点绑定监听器
     */
    private void showButtons() {
        for (final Node node : mAllNodes) {
            final IconButton iconBtn = new IconButton(this.getActivity());
            // 绑定编辑界面的 按键操作

            //region 单击IconBtn的操作
            iconBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    View pwContent = LayoutInflater.from(MapFragment.this.getActivity()).inflate(R.layout.popup_edit_node, null);
                    final PopupWindow pw = new PopupWindow(pwContent, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
                    pw.setFocusable(true);

                    pw.setTouchable(true);
                    pw.setOutsideTouchable(true);
                    pw.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

                    //region 绑定编辑节点的操作
                    pwContent.findViewById(R.id.popupEditNode).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showEditNodeDialog(node);
                            pw.dismiss();
                        }
                    });
                    //endregion

                    //region 绑定删除节点的操作
                    pwContent.findViewById(R.id.popupDelNode).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            CustomDialog.Builder builder = new CustomDialog.Builder(MapFragment.this.getActivity());
                            builder.getConfirmDialog("确认删除", "是否要删除节点 [" + node.getName() + "]?",
                                    new CustomDialogCallback() {
                                        @Override
                                        public boolean onDialogBtnClick(List<View> viewList) {
                                            //删除成功
                                            if (mDatabaseHelper.delData(node)) {
                                                mAllNodes.remove(node);
                                                refreshHandler.sendEmptyMessage(RefreshAll);
                                                return true;
                                            }
                                            return false;
                                        }
                                    })
                                    .show();
                            pw.dismiss();
                        }
                    });
                    //endregion

                    //region 绑定编辑路径的操作
                    pwContent.findViewById(R.id.popupEditPath).setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pw.dismiss();
                        }
                    });
                    //endregion

                    //region 设置弹出popup的位置
                    Int2 popupSize = WidgetController.getViewMeasure(pwContent);
                    Point screenSize = ((BaseActivity) getActivity()).getScreenSize();
                    // 根据node坐标...来获取偏移量
                    Int2 offset = new Int2();
                    if (screenSize.x - node.getPositionX() < popupSize.x / 2) {
                        offset.x = screenSize.x - popupSize.x;
                    } else {
                        offset.x = node.getPositionX() - popupSize.x / 2;
                    }
                    if (screenSize.y - node.getPositionY() < popupSize.y / 2) {
                        offset.y = screenSize.y - popupSize.y;
                    } else {
                        offset.y = node.getPositionY() - popupSize.y / 2;
                    }
                    pw.showAtLocation(iconBtn, Gravity.TOP | Gravity.LEFT,
                            offset.x >= 0 ? offset.x : 0, offset.y >= 0 ? offset.y : 0);
                    //endregion
                }
            });
            //endregion

            //region IconBtn的LongClick
            iconBtn.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //振动 表示开始移动..
                    Vibrator vibrator = (Vibrator) RobotApplication.getContext().getSystemService(Service.VIBRATOR_SERVICE);
                    // 振动100ms,重复次数: -1表示不重复
                    vibrator.vibrate(new long[]{0, 100}, -1);

                    MoveListener ml = new MoveListener();
                    // 原本是不需要初始化 lastX和lastY的, 因为该值会在 MoveListener的onDown中初始化..
                    // 但是 , btn的拖动 是由 onLongClick触发的... 这之后已经onDown过了..
                    ml.setLastX(node.getPositionX());
                    ml.setLastY(node.getPositionY());
                    ml.setMoveCallback(new MoveListener.MoveCallback() {
                        @Override
                        public void onMoveEnd(MotionEvent event) {
//                            L.i(LOG, "End:" + Arrays.toString(new float[]{event.getRawX(), event.getRawY(), event.getX(), event.getY()}));
                            // 保存新的节点位置到数据库
                            node.setPositionX((int) event.getRawX());
                            node.setPositionY((int) event.getRawY());
                            mDatabaseHelper.updateData(node);

                            refreshHandler.sendEmptyMessage(RefreshAll);
                        }

                        public void onMoving(MotionEvent event) {
                            // 实时刷新map 的PathView,注意偏移量
                            L.i(LOG, Arrays.toString(new float[]{event.getRawX(), event.getRawY(), event.getX(), event.getY()}));
                        }
                    });
                    iconBtn.setOnTouchListener(ml);
                    return false;
                }
            });
            //endregion

            //region 非编辑界面的操作 (暂时弃用)
            //如果不是在编辑界面,就是在主界面,则进行如下的click绑定(单击 后添加或者删除节点)
//            } else if (!isEdit) {
//                btn.setOnClickListener(new OnClickListener() {
//                    public void onClick(View v) {
//                        ((MainActivity) getActivity()).updateTasklist(node);
//                    }
//                });
//                btn.setOnLongClickListener(new OnLongClickListener() {
//                    public boolean onLongClick(View v) {
//                        CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
//                        builder.getConfirmDialog("定位", "是否将当前位置设置为" + node.getName(),
//                                new CustomDialogCallback() {
//                                    public boolean onDialogBtnClick(List<View> viewList) {
//                                        ((MainActivity) getActivity()).setCurrentNode(node);
//                                        return true;
//                                    }
//                                });
//                        return false;
//                    }
//                });
//            }
            //endregion (  (暂时弃用)

            //region 按键属性设置 (样式,布局,位置)
            iconBtn.setId(node.getId());
            iconBtn.setIconSize(mIconSize, mIconSize);
            iconBtn.setTextSize(mTextSize);
            iconBtn.setText(node.getName());
            iconBtn.setTextColor(Color.BLACK);
            iconBtn.setBackgroundColor(Color.TRANSPARENT);
            iconBtn.setIconPosition(IconButton.TOP);
            switch (node.getType()) {
                case NODE_TYPE.CROSS:
                    iconBtn.setIconDrawableID(R.drawable.map_button_cross);
                    break;
                case NODE_TYPE.KITCHEN:
                    iconBtn.setIconDrawableID(R.drawable.map_button_kitchen);
                    break;
                case NODE_TYPE.TABLE:
                    iconBtn.setIconDrawableID(R.drawable.map_button_table);
                    break;
                case NODE_TYPE.WASH:
                    iconBtn.setIconDrawableID(R.drawable.map_button_wash);
                    break;
            }
            // 设置btn在父容器中的位置
            // 缩放到屏幕内
            WidgetController.setViewLayoutInCenter(iconBtn, node.getPositionX(), node.getPositionY());
            mapLayout.addView(iconBtn);
            //endregion
        }

    }

    public void setRouteID(int rID) {
        mRoute = mDatabaseHelper.getRouteByID(rID);
        mPathView.setCurrentRouteID(rID);
        refreshPath();
    }

    private void showAddNodeDialog(final Node node) {
        Node maxIDNode = mDatabaseHelper.getMaxIdNode();
        int maxID = maxIDNode == null ? 0 : maxIDNode.getId();

        CustomDialog.Builder mBuilder = new CustomDialog.Builder(this.getActivity());

        mBuilder.setResourceID(R.layout.dialog_node)
                .setCancelBtnClick(R.id.dialogCancelBtn, null)
                .setTitle("添加节点")
                .setButtonText("添加", "取消")
                .setOkBtnClick(R.id.dialogOKBtn, new CustomDialogCallback() {
                    public boolean onDialogBtnClick(List<View> viewList) {
                        Node newNode = Node.loadNode(node, viewList);
                        if (newNode == null) {
                            L.e("装载 节点 时出错");
                            return false;
                        } else {
                            boolean result = mDatabaseHelper.addData(newNode);
                            // 如果添加成功
                            if (result) {
                                mAllNodes.add(newNode);
                                refreshHandler.sendEmptyMessage(RefreshAll);
                            }
                            return result;
                        }
                    }
                }).
                create(new int[]{R.id.tvDialogNodeID, R.id.dialogNodePosition,
                                R.id.etDialogNodeName, R.id.etDialogNodeRFID, R.id.spDialogNodeType},
                        new Object[]{maxID + 1,
                                "[" + node.getPositionX() + "," + node.getPositionY() + "]",
                                null, null, null})
                .show();
    }

    private void showEditNodeDialog(final Node node) {
        CustomDialog.Builder mBuilder = new CustomDialog.Builder(this.getActivity());
        mBuilder.setResourceID(R.layout.dialog_node)
                .setCancelBtnClick(R.id.dialogCancelBtn, null)
                .setTitle("编辑节点")
                .setButtonText("修改", "取消")
                .setOkBtnClick(R.id.dialogOKBtn, new CustomDialogCallback() {
                    public boolean onDialogBtnClick(List<View> viewList) {
                        Node newNode = Node.loadNode(node, viewList);
                        if (newNode == null) {
                            L.e("装载 节点 时出错");
                            return false;
                        } else {
                            return mDatabaseHelper.updateData(newNode);
                        }
                    }
                })
                .create(new int[]{R.id.tvDialogNodeID, R.id.dialogNodePosition,
                                R.id.etDialogNodeName, R.id.etDialogNodeRFID, R.id.spDialogNodeType},
                        new Object[]{node.getId(), "[" + node.getPositionX() + "," + node.getPositionY() + "]",
                                node.getName(), node.getRFID(), getNodeSpinnerItemID(node.getType())})
                .show();
    }


    // 由 节点的类型 (String) 得到 Spinner中对应选项的位置
    private int getNodeSpinnerItemID(String str) {
        switch (str) {
            case NODE_TYPE.TABLE:
                return 0;
            case NODE_TYPE.CROSS:
                return 1;
            case NODE_TYPE.KITCHEN:
                return 2;
            case NODE_TYPE.WASH:
                return 3;
            default:
                return 0;
        }
    }

    //region 为Fragment作为任务选择界面,所实现的接口
    @Override
    public void addTask(List<Node> node) {

    }

    @Override
    public void setCallback(UserControlFragmentCallback ucc) {

    }

    @Override
    public void addTask(Node node) {

    }

    @Override
    public void delTask(List<Node> node) {

    }

    @Override
    public void delTask(Node node) {

    }

    @Override
    public void clearTask() {

    }
    //endregion

    // 将所有节点自动调整,默认为 屏幕分割成 40* 30块
    public void autoAdjust() {
        if (mAllNodes == null) {
            return;
        }
        Point screenSize = ((BaseActivity) getActivity()).getScreenSize();
        int unit = Math.max(screenSize.x / 40, screenSize.y / 30);
        for (Node node : mAllNodes) {
            // adjust X direction Nodes
            // 如果 0<= x < unit*2 , 则 x设置为 unit
            // 如果 unit*2 <= x < unit* 4 ,则设置x为 unit*3
            int adjustX = ((node.getPositionX() / (unit * 2)) * 2 + 1) * unit;
            // 超过屏幕,左移一个区间
            if (adjustX > screenSize.x) {
                adjustX -= unit * 2;
            }
            node.setPositionX(adjustX);
            int adjustY = ((node.getPositionY() / (unit * 2)) * 2 + 1) * unit;
            if (adjustY > screenSize.y) {
                adjustY -= unit * 2;
            }
            node.setPositionY(adjustY);
            mDatabaseHelper.updateData(node);
        }
        // 完成后刷新
        refreshHandler.sendEmptyMessage(RefreshAll);
    }

    // Fragment主界面 -> mapFrameLayout的手势监听
    class FrameLayoutGestureListener implements GestureDetector.OnGestureListener {
        final String LOG = FrameLayoutGestureListener.class.getSimpleName();

        /**
         * 按下后 表示 已经关闭其他dialog了
         */
        @Override
        public boolean onDown(MotionEvent e) {
            L.i(LOG, "onDown");
            hasDialog = false;
            // 返回false,表示不阻挡事件传递. 如果返回true, 该容器内的Btn将无法触发click事件
            return false;
        }

        @Override
        /**
         * 空白处 长按 , 开始添加节点
         * 条件 1. 处于编辑状态 2. 没有对话框( 如果正在 编辑节点, hasDialog==true ) ,最好还是从 touch触发实现
         */
        public void onLongPress(MotionEvent e) {
            L.i(LOG, "onLongPress");
            if (!hasDialog) {
                //并且不能再选项栏处触发 添加节点
                Node node = new Node((int) e.getRawX(), (int) e.getRawY());
                node.setWorkspaceID(mWorkspace.getId());
                showAddNodeDialog(node);
            }
        }

        @Override
        public void onShowPress(MotionEvent e) {
            L.i(LOG, "showPress");
            // 点击  (短暂停留)  down -> showPress-> singleTapUp
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            L.i(LOG, "singleTapUp");

            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            L.i(LOG, "onFling");

            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                float distanceY) {
            L.i(LOG, "onScroll");

            return false;
        }

    }
//    private List<Point> getOffsetPoint(List<Node> nodeList) {
//        List<Point> result = new ArrayList<>();
//        for (Node node : nodeList) {
//            result.add(getOffsetPoint(node));
//        }
//        return result;
//    }
//
//    private Point getOffsetPoint(Node node) {
//        IconButton iconBtn = nodeBtnMap.get(node);
//        if (iconBtn == null) {
//            return new Point((int) (node.getPositionX() * mScale), (int) (node.getPositionY() * mScale));
//        } else {
//            return new Point((int) (node.getPositionX() * mScale), (int) ((node.getPositionY() - iconBtn.getTextSize() / 2) * mScale));
//        }
//    }
}