package com.feng.Activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.feng.Base.BaseActivity;
import com.feng.Constant.I_Parameters;
import com.feng.CustomView.CustomDialog;
import com.feng.CustomView.CustomDialogCallback;
import com.feng.CustomView.PopupWindowManager;
import com.feng.Database.Map.*;
import com.feng.MapModule.MapFragment;
import com.feng.RSS.R;
import com.feng.Utils.L;
import com.feng.Utils.T;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.ArrayList;
import java.util.List;


public class EditMapActivity extends BaseActivity implements I_Parameters {
    @BindView(R.id.famMenu)
    FloatingActionsMenu mFamMenu;
    @BindView(R.id.routeListView)
    ListView mRouteListView;
    @BindView(R.id.dlEditMap)
    DrawerLayout mDrawer;
    @BindView(R.id.iconBtnBack)
    Button mBtnBack;

    @OnClick({R.id.fabAutoAdjust, R.id.fabAddPortal, R.id.fabAddStation, R.id.fabAddRoute})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fabAutoAdjust:
                if (mapFragment != null) {
                    mapFragment.autoAdjust();
                }
                mFamMenu.collapse();
                break;

            //添加 传送点
            case R.id.fabAddPortal:
                showSimpleAddRouteDialog("添加连接点路线", Route.PORTAL_ROUTE);
                mFamMenu.collapse();
                break;

            //添加停靠点
            case R.id.fabAddStation:
                showSimpleAddRouteDialog("添加停靠点路线", Route.STATION_ROUTE);
                mFamMenu.collapse();
                break;

            // 添加路线 按键
            case R.id.fabAddRoute:
                showAddRouteDialog();
                mFamMenu.collapse();
                break;
        }
    }

    // 工作区名称 点击后返回上一页
    private MapDatabaseHelper mMapDatabaseHelper = MapDatabaseHelper.getInstance();
    private List<Route> routeListData;
    private Workspace mWorkspace;
    //传递 当前选择的路线 值
    private MapFragment mapFragment;
    // 高亮当前选择的值
    private RouteListViewAdapter mRouteListViewAdapter;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_map);
        ButterKnife.bind(this);

        mWorkspace = mMapDatabaseHelper.getWorkspaceByID(getIntent().getIntExtra("workspaceID", -1));
        if (mWorkspace == null) {
            L.e(EditMapActivity.class.getSimpleName(), "初始化工作区失败!");
            return;
        }
        initView();
        initFragment();
    }

    private void initView() {
        //默认打开抽屉
        mDrawer.openDrawer(GravityCompat.START);
        // 工作区名称
        mBtnBack.setText(mWorkspace.getName());
        mBtnBack.setBackgroundColor(Color.TRANSPARENT);
        mBtnBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditMapActivity.this.finish();
            }
        });

        routeListData = getRouteListData();

        mRouteListViewAdapter = new RouteListViewAdapter(this);
        if (routeListData.size() >= 1) {
            mRouteListViewAdapter.setSelectItem(0);
        }
        mRouteListView.setAdapter(mRouteListViewAdapter);

        //region 点击路线 -> 切换该路线
        mRouteListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                mRouteListViewAdapter.setSelectItem(position);
                mRouteListViewAdapter.notifyDataSetInvalidated();
                // 刷新Fragment
                mapFragment.setRouteID(routeListData.get(position).getId());
                mapFragment.refreshPath();
            }
        });
        //endregion

        //region 长按路线 -> 编辑/删除
        mRouteListView.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final PopupWindowManager pwm = new PopupWindowManager(EditMapActivity.this);
                pwm.showPopupWindowAsDropDown(view,
                        R.layout.popup_edit_route,
                        view.getWidth(), -view.getHeight(),
                        new int[]{R.id.popupEditPath1, R.id.popupEditRoute, R.id.popupDelRoute},
                        //region 编辑路径 (添加,修改,删除)
                        new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pwm.close();
                                showChoosePathDialog(routeListData.get(position));
                            }
                        },
                        //endregion
                        //region 编辑路线属性
                        new OnClickListener() {
                            public void onClick(View v) {
                                pwm.close();
                                showEditRouteDialog(routeListData.get(position));
                            }
                        },
                        //endregion

                        //region 删除路线
                        new OnClickListener() {
                            public void onClick(View v) {
                                pwm.close();
                                CustomDialog.Builder builder = new CustomDialog.Builder(EditMapActivity.this);
                                builder.getConfirmDialog("确认删除", "是否要删除路线 [" + routeListData.get(position).getName() + "]?" +
                                                "\n该路线的后续路线也将同时删除!!!",
                                        new CustomDialogCallback() {
                                            @Override
                                            public boolean onDialogBtnClick(List<View> viewList) {
                                                // 从数据库删除数据
                                                if (mMapDatabaseHelper.delData(routeListData.get(position))) {
                                                    // 获取最新数据
                                                    routeListData = getRouteListData();
                                                    mRouteListViewAdapter.notifyDataSetInvalidated();
                                                    //将 当前路径设置为 0
                                                    if (routeListData.size() != 0) {
                                                        mRouteListViewAdapter.setSelectItem(0);
                                                        mapFragment.setRouteID((routeListData.get(0)).getId());
                                                    } else {
                                                        mapFragment.setRouteID(0);
                                                    }
                                                    mapFragment.refreshPath();
                                                    return true;
                                                } else {
                                                    return false;
                                                }
                                            }
                                        }
                                ).show();
                            }
                        }
                        //endregion
                );
                return true;
            }
        });
        //endregion
    }

    private void initFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mapFragment = new MapFragment();
        Bundle bd = new Bundle();
        if (routeListData.size() != 0) {
            bd.putInt("routeID", routeListData.get(0).getId());
        }
        bd.putInt("workspaceID", mWorkspace.getId());
        mapFragment.setArguments(bd);
        fragmentTransaction.replace(R.id.edit_map_frame, mapFragment);
        fragmentTransaction.commit();
    }

    private List<Route> getRouteListData() {
        List<Route> dataList = new ArrayList<>();
        try {
            dataList = mMapDatabaseHelper.getAllRoute(mWorkspace.getId());
        } catch (Exception e) {
            L.e(e.toString());
        }
        return dataList;
    }

    private void showAddRouteDialog() {
        CustomDialog.Builder builder = new CustomDialog.Builder(EditMapActivity.this);
        builder.setResourceID(R.layout.dialog_route);

        final Route target = new Route();
        int currentRouteID = mMapDatabaseHelper.getMaxRouteID() + 1;
        target.setId(currentRouteID);
        target.setWorkspaceID(mWorkspace.getId());
        target.setType(Route.TASK_ROUTE);

        // 初始化 Spinner (隐藏 + 初始化数据)
        View dialogLayout = builder.getConvertView();
        boolean route0Exist = mMapDatabaseHelper.baseRouteExist(mWorkspace.getId());
        // 如果没有前级路线为0的存在,则隐藏PreRoute这一行
        if (!route0Exist) {
            dialogLayout.findViewById(R.id.spPreRoute).setVisibility(View.INVISIBLE);
            dialogLayout.findViewById(R.id.llPreRoute).setVisibility(View.GONE);
        } else {
            // 如果有路线存在, 初始化Spinner
            Spinner spPreRoute = (Spinner) dialogLayout.findViewById(R.id.spPreRoute);
            List<Route> routeList = mMapDatabaseHelper.getLegalPreRoute(target);
            String[] selections = new String[routeList.size()];
            for (int i = 0; i < routeList.size(); i++) {
                selections[i] = routeList.get(i).getName();
            }
            ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, selections);
            spPreRoute.setAdapter(adapter);
        }

        builder.setCancelBtnClick(R.id.dialogCancelBtn, null)
                .setTitle("添加路线")
                .setButtonText("添加", "取消")
                .setOkBtnClick(R.id.dialogOKBtn, new CustomDialogCallback() {
                    public boolean onDialogBtnClick(List<View> viewList) {
                        Route newRoute = Route.loadRoute(target, viewList);
                        if (newRoute != null && mMapDatabaseHelper.addData(newRoute)) {
                            routeListData = getRouteListData();
                            mRouteListViewAdapter.setSelectItem(routeListData.size() - 1);
                            mRouteListViewAdapter.notifyDataSetInvalidated();

                            mapFragment.setRouteID(newRoute.getId());
                            return true;
                        } else {
                            return false;
                        }
                    }
                })
                .create(new int[]{R.id.addRouteId, R.id.spPreRoute, R.id.etRouteName, R.id.swRouteEnable},
                        new Object[]{currentRouteID, null, null, true})
                .show();
    }

    private void showEditRouteDialog(final Route route) {
        CustomDialog.Builder builder = new CustomDialog.Builder(EditMapActivity.this);
        builder.setResourceID(R.layout.dialog_route);
        // 初始化 Spinner (初始化数据)
        View dialogLayout = builder.getConvertView();
        // 当前spinner选中位置
        int spinnerSelectionID = 0;
        // 如果前级路线为0,则隐藏PreRoute这一行
        if (route.getPreID() == 0) {
            dialogLayout.findViewById(R.id.spPreRoute).setVisibility(View.INVISIBLE);
            dialogLayout.findViewById(R.id.llPreRoute).setVisibility(View.GONE);
        } else {
            // 否则初始化Spinner数据
            Spinner spPreRoute = (Spinner) dialogLayout.findViewById(R.id.spPreRoute);
            List<Route> preRouteOptions = mMapDatabaseHelper.getLegalPreRoute(route);
            String[] selections = new String[preRouteOptions.size()];
            for (int i = 0; i < selections.length; i++) {
                selections[i] = preRouteOptions.get(i).getName();
                //初始化Spinner选中位置
                if (preRouteOptions.get(i).getId() == route.getPreID()) {
                    spinnerSelectionID = i;
                }
            }
            ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, selections);
            spPreRoute.setAdapter(adapter);
        }


        builder.setCancelBtnClick(R.id.dialogCancelBtn, null)
                .setTitle("编辑路线")
                .setButtonText("修改", "取消")
                .setOkBtnClick(R.id.dialogOKBtn, new CustomDialogCallback() {
                    public boolean onDialogBtnClick(List<View> viewList) {
                        Route newRoute = Route.loadRoute(route, viewList);
                        if (newRoute != null && mMapDatabaseHelper.updateData(newRoute)) {
                            routeListData = getRouteListData();
                            mRouteListViewAdapter.notifyDataSetInvalidated();
                            return true;
                        } else {
                            return false;
                        }
                    }
                })
                .create(new int[]{R.id.addRouteId, R.id.spPreRoute, R.id.etRouteName, R.id.swRouteEnable},
                        new Object[]{route.getId(), spinnerSelectionID, route.getName(), route.isEnabled()})
                .show();
    }

    private void showSimpleAddRouteDialog(String title, int RouteType) {
        final View dialogView = View.inflate(EditMapActivity.this, R.layout.dialog_route_simple, null);
        final EditText etInputMode = (EditText) dialogView.findViewById(R.id.etRouteName);

        final Route route = new Route();
        route.setId(mMapDatabaseHelper.getMaxRouteID() + 1);
        route.setType(RouteType);
        route.setEnabled(true);
        route.setWorkspaceID(mWorkspace.getId());

        final Spinner spPreRoute = (Spinner) dialogView.findViewById(R.id.spPreRoute);
        final List<Route> preRouteOptions = mMapDatabaseHelper.getLegalPreRoute(route);
        String[] selections = new String[preRouteOptions.size()];
        for (int i = 0; i < selections.length; i++) {
            selections[i] = preRouteOptions.get(i).getName();
        }
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, selections);
        spPreRoute.setAdapter(adapter);

        new AlertDialog.Builder(EditMapActivity.this)
                .setTitle(title)
                .setIcon(RouteType == Route.STATION_ROUTE ? R.mipmap.fab_btn_map_add_station : R.mipmap.fab_btn_map_add_portal)
                .setView(dialogView)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = etInputMode.getText().toString();
                        if (TextUtils.isEmpty(name)) {
                            T.show("请输入线路名称");
                        } else {
                            route.setName(name);
                            int preRouteID = preRouteOptions.get(spPreRoute.getSelectedItemPosition()).getId();
                            route.setPreID(preRouteID);

                            if (mMapDatabaseHelper.addData(route)) {
                                routeListData = getRouteListData();
                                mRouteListViewAdapter.setSelectItem(routeListData.size() - 1);
                                mRouteListViewAdapter.notifyDataSetInvalidated();

                                mapFragment.setRouteID(route.getId());
                            }
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
        mFamMenu.collapse();
    }


    class RouteListViewAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private int selectItem = -1;

        public void setSelectItem(int p) {
            selectItem = p;
        }

        public RouteListViewAdapter(Context cx) {
            inflater = LayoutInflater.from(cx);
        }

        public int getCount() {
            return routeListData.size();
        }

        public Object getItem(int position) {
            return routeListData.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public Route getSelectRoute() {
            if (selectItem == -1) {
                return null;
            } else {
                return (Route) getItem(selectItem);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final RouteHolder routeHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.listview_of_route, null);
                routeHolder = new RouteHolder();
                //	wsHolder.imageBtn=(ImageButton)convertView.findViewById(R.id.girdViewOfWorkspaceImageBtn);
                routeHolder.ID = (TextView) convertView.findViewById(R.id.listViewRouteID);
                routeHolder.name = (TextView) convertView.findViewById(R.id.listViewRouteName);
//                routeHolder.preID = (TextView) convertView.findViewById(R.id.listViewPreRouteID);
                //				routeHolder.enableImage=(ImageView)convertView.findViewById(R.id.listVIewRouteEnable);
                convertView.setTag(routeHolder);
            } else {
                routeHolder = (RouteHolder) convertView.getTag();
            }
            //这里应该从数据库获取 还有 修改数据库接口(还未返回图片)
            Route route = routeListData.get(position);
            routeHolder.name.setText(route.getName());
            routeHolder.ID.setText(String.valueOf(route.getId()));
//            routeHolder.preID.setText(String.valueOf(route.getPreID()));

            // 这里是样式
            if (position == selectItem) {
                convertView.setBackgroundResource(R.drawable.round_yellow_1379);
            } else {
                if (route.isEnabled()) {
                    convertView.setBackgroundResource(R.drawable.skyblue);
                } else {
                    convertView.setBackgroundResource(R.drawable.gray);
                }
            }
            return convertView;
        }

        class RouteHolder {
            public TextView ID;
            public TextView name;
            public TextView preID;
            public ImageView enableImage;
        }
    }

    public Route getCurrentRoute() {
        if (routeListData.size() <= 0) {
            return null;
        }
        return mRouteListViewAdapter.getSelectRoute();
    }

    public void showAddPathDialog(final Route currentRoute, Node selectNode) {
        final Node routeLastNode = mMapDatabaseHelper.getLastNodeByRouteID(currentRoute.getId());

        CustomDialog.Builder mBuilder = new CustomDialog.Builder(this);
        mBuilder.setResourceID(R.layout.dialog_path_edit);
        final PathDialogViewHolder holder = new PathDialogViewHolder(mBuilder.getConvertView());
        // 如果起始点为空,表示当前路线还未选择第一个开始点
        if (routeLastNode == null) {
            List<Node> selectableNodes;
            // 如果当前路线是第一条路线,则起始点在工作区内随便选
            if (currentRoute.getPreID() == 0) {
                selectableNodes = mMapDatabaseHelper.getAllNodeOrderById(currentRoute.getWorkspaceID());
            } else {
                //如果当前路线不是第一条路线,则在前级路线上选择起始点
                selectableNodes = mMapDatabaseHelper.getNodeByRoute(currentRoute.getPreID());
            }
            String[] selection = new String[selectableNodes.size()];
            for (int i = 0; i < selectableNodes.size(); i++) {
                selection[i] = selectableNodes.get(i).getName();
            }
            holder.mSpStartNode.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, selection));

            //当 起点选项更新时,刷新 终点的可选项
            holder.mSpStartNode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String nodeName = ((TextView) view).getText().toString();
                    Node node = mMapDatabaseHelper.getNodeByName(currentRoute.getWorkspaceID(), nodeName);
                    List<Node> selectableNode = mMapDatabaseHelper.getSelectableNode(node);
                    String[] selection = new String[selectableNode.size()];
                    for (int i = 0; i < selectableNode.size(); i++) {
                        selection[i] = selectableNode.get(i).getName();
                    }
                    holder.mSpEndNode.setAdapter(new ArrayAdapter<>(EditMapActivity.this, android.R.layout.simple_spinner_item, selection));
                    holder.mSpEndNode.invalidate();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        } else {
            String[] startSelection = new String[]{routeLastNode.getName()};
            holder.mSpStartNode.setEnabled(false);
            holder.mSpStartNode.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, startSelection));

//            List<Node> selectableNodes = mMapDatabaseHelper.getSelectableNode(selectNode);
//            String[] selection = new String[selectableNodes.size()];
//            for (int i = 0; i < selectableNodes.size(); i++) {
//                selection[i] = selectableNodes.get(i).getName();
//            }
            String[] endSelection = new String[]{selectNode.getName()};
            holder.mSpEndNode.setEnabled(false);
            holder.mSpEndNode.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, endSelection));
        }

        // 获取 已经确定的值
        final Path pathAdd = new Path();
        pathAdd.setRouteID(currentRoute.getId());
        int orderID = mMapDatabaseHelper.getMaxPathOrder(currentRoute.getId()) + 1;
        pathAdd.setOrderID(orderID);
        mBuilder.setCancelBtnClick(R.id.dialogCancelBtn, new CustomDialogCallback() {
            @Override
            public boolean onDialogBtnClick(List<View> viewList) {
//                if (chooseDialog != null) {
//                    chooseDialog.show();
//                }
                return true;
            }
        })
                .setTitle("添加路径")
                .setButtonText("添加", "取消")
                .setOkBtnClick(R.id.dialogOKBtn, new CustomDialogCallback() {
                    public boolean onDialogBtnClick(List<View> viewList) {
                        Path newPath = Path.loadPath(pathAdd, viewList);
                        if (newPath == null) {
                            L.e("装载 路径 时出错");
                            return false;
                        } else if (mMapDatabaseHelper.addData(newPath)) {
                            mapFragment.refreshAll();
//                            if (chooseDialog != null) {
//                                chooseDialog.show();
//                            }
//                            if (chooseDialogAdapter != null) {
//                                chooseDialogAdapter.notifyDataSetChanged();
//                            }
                            return true;
                        } else {
                            return false;
                        }
                    }
                })
                .create(new int[]{R.id.spStartNode, R.id.spEndNode,
                        R.id.addPathRouteID, R.id.addPathOrderID,
                        R.id.spPathYaw, R.id.etPathDistance,
                        R.id.spPathAngle, R.id.etPathMaxSpeed,
                        R.id.spPathTurnType}, new Object[]{
                        0, 0, currentRoute.getId(), orderID,
                        null, null, null, null, null})
                .show();
    }


    //显示编辑path的dialog, 当操作结束,继续显示chooseDialog
    public void showEditPathDialog(final Route route, final Path path) {
        final CustomDialog.Builder mBuilder = new CustomDialog.Builder(this);
        mBuilder.setResourceID(R.layout.dialog_path_edit);
        final PathDialogViewHolder holder = new PathDialogViewHolder(mBuilder.getConvertView());

        // 起始点
        holder.mSpStartNode.setEnabled(false);
        String[] startNodeSelection = new String[]{mMapDatabaseHelper.getNodeByID(path.getNodeID()).getName()};
        holder.mSpStartNode.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, startNodeSelection));
        // 终点
        holder.mSpEndNode.setEnabled(false);
        String[] endNodeSelection = new String[]{mMapDatabaseHelper.getNodeByID(path.getEndNode()).getName()};
        holder.mSpEndNode.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, endNodeSelection));

        // 删除
        holder.mBtnDel.setVisibility(View.VISIBLE);
        holder.mBtnDel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBuilder.getDialog() != null) {
                    mBuilder.getDialog().dismiss();
                }
                CustomDialog.Builder builder = new CustomDialog.Builder(EditMapActivity.this);
                builder.getConfirmDialog("确认删除", "是否要删除路径 [" + path.getNodeID() + "->" + path.getEndNode() + "]?" +
                                "\n后续路径也将同时删除!!!",
                        new CustomDialogCallback() {
                            @Override
                            public boolean onDialogBtnClick(List<View> viewList) {
                                //删除成功
                                if (mMapDatabaseHelper.delData(path)) {
                                    mapFragment.refreshAll();
                                    return true;
                                }
                                return false;
                            }
                        })
                        .show();
            }
        });
        mBuilder.setCancelBtnClick(R.id.dialogCancelBtn, new CustomDialogCallback() {
            @Override
            public boolean onDialogBtnClick(List<View> viewList) {
//                chooseDialog.show();
                return true;
            }
        })
                .setTitle("编辑路径")
                .setButtonText("修改", "取消")
                .setOkBtnClick(R.id.dialogOKBtn, new CustomDialogCallback() {
                    public boolean onDialogBtnClick(List<View> viewList) {
                        Path newPath = Path.loadPath(path, viewList);
                        if (newPath == null) {
                            L.e("装载 路径 时出错");
                            return false;
                        } else if (mMapDatabaseHelper.updateData(newPath)) {
                            mapFragment.refreshAll();
//                            chooseDialog.show();
                            return true;
                        } else {
                            return false;
                        }
                    }
                })
                .create(new int[]{R.id.spStartNode, R.id.spEndNode,
                        R.id.addPathRouteID, R.id.addPathOrderID,
                        R.id.spPathYaw, R.id.etPathDistance,
                        R.id.spPathAngle, R.id.etPathMaxSpeed,
                        R.id.spPathTurnType}, new Object[]{
                        0, 0, path.getRouteID(), path.getOrderID(),
                        getPathTurnSpinnerItemOrder(path.getYaw()), null,
                        getPathTurnSpinnerItemOrder(path.getAngle()), null,
                        path.getTurnType()})
                .show();
    }

    private void showChoosePathDialog(final Route route) {
        final CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setResourceID(R.layout.dialog_path_choose);
        //初始化标题,2个按键( 添加路径+取消)
        builder.setTitle(route.getName() + "包含的路径");
        builder.setButtonText("添加路径", "取消");
        builder.setCancelBtnClick(R.id.dialogCancelBtn, null);
        final Dialog chooseDialog = builder.create(null, null);

        ListView lvPaths = (ListView) builder.getViewByID(R.id.lvPathList);
        final PathListViewAdapter pathChooseDialogAdapter = new PathListViewAdapter(this, route, chooseDialog);
        lvPaths.setAdapter(pathChooseDialogAdapter);

        // 如果该节点不可以添加路径,将添加按键设置为不可用
        Button addPath = (Button) builder.getViewByID(R.id.dialogOKBtn);
        addPath.setText("添加路径");
        if (mMapDatabaseHelper.isCompleted(route)) {
            addPath.setEnabled(false);
        } else {
            //当前路线的最后一个节点. 如果没有则为空
            final Node node = mMapDatabaseHelper.getLastNodeByRouteID(route.getId());
            addPath.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseDialog.hide();
//                    showAddPathDialog(chooseDialog, pathChooseDialogAdapter, route, node);
                }
            });
        }

        chooseDialog.show();
    }

    private int getPathTurnSpinnerItemOrder(int turnYaw) {
        switch (turnYaw) {
            case 0:
                return 0; //直走
            case -90:
                return 1; //左转
            case 90:
                return 2;//右转
            case -180:
                return 3; //掉头
        }
        return 0;
    }


    // 路径选择 dialog的adapter
    class PathListViewAdapter extends BaseAdapter {
        private Route mRoute;
        private List<Path> mPathList;
        private Dialog mDialog;//传入dialog,方便关闭与隐藏
        private LayoutInflater mInflater;

        public PathListViewAdapter(Context context, Route route, Dialog dialog) {
            mDialog = dialog;
            mInflater = LayoutInflater.from(context);
            mRoute = route;
            mPathList = mMapDatabaseHelper.getWholeRoute(mRoute.getId());
        }


        public void setData(List<Path> pathList) {
            mPathList = pathList;
        }

        @Override
        public int getCount() {
            return mPathList.size();
        }

        @Override
        public Object getItem(int position) {
            return mPathList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.dialog_path_choose_item, null);
                holder.tvPathOrder = (TextView) convertView.findViewById(R.id.tvPathOrder);
                holder.tvPathName = (TextView) convertView.findViewById(R.id.tvPathName);
                holder.ibPathDel = (ImageButton) convertView.findViewById(R.id.ibDel);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            final Path path = mPathList.get(position);
            //  如果是当前路线的Path
            if (path.getRouteID() == mRoute.getId()) {
                //显示 order
                holder.tvPathOrder.setText(String.valueOf(path.getOrderID()));
                holder.tvPathOrder.setVisibility(View.VISIBLE);
                holder.tvPathName.setTextColor(Color.BLACK);
                holder.tvPathName.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        mDialog.dismiss();
                        mDialog.hide();
//                        showEditPathDialog(mDialog, path);
                    }
                });

                holder.ibPathDel.setVisibility(View.VISIBLE);
                holder.ibPathDel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CustomDialog.Builder builder = new CustomDialog.Builder(EditMapActivity.this);
                        builder.getConfirmDialog("确认删除", "是否要删除路径 [" + path.getNodeID() + "->" + path.getEndNode() + "]?" +
                                        "\n后续路径也将同时删除!!!",
                                new CustomDialogCallback() {
                                    @Override
                                    public boolean onDialogBtnClick(List<View> viewList) {
                                        //TODO 删除时应该刷新 路径选择dialog的添加按键...暂时设置为隐藏吧
                                        mDialog.dismiss();
                                        //删除成功
                                        if (mMapDatabaseHelper.delData(path)) {
                                            mapFragment.refreshAll();

                                            mPathList = mMapDatabaseHelper.getWholeRoute(mRoute.getId());
                                            notifyDataSetChanged();

                                            return true;
                                        }
                                        return false;
                                    }
                                })
                                .show();
                    }
                });
            } else {
                holder.tvPathOrder.setVisibility(View.INVISIBLE);
                holder.tvPathName.setTextColor(Color.GRAY);
                holder.ibPathDel.setVisibility(View.INVISIBLE);
            }

            Node node = mMapDatabaseHelper.getNodeByID(mPathList.get(position).getNodeID());
            holder.tvPathName.setText(node.getName());

            return convertView;
        }

        @Override
        public void notifyDataSetChanged() {
            mPathList = mMapDatabaseHelper.getWholeRoute(mRoute.getId());
            super.notifyDataSetChanged();
        }

        class ViewHolder {
            TextView tvPathOrder;
            TextView tvPathName;
            ImageButton ibPathDel;
        }
    }

    static class PathDialogViewHolder {
        @BindView(R.id.dialogTitle)
        TextView mDialogTitle;
        @BindView(R.id.spStartNode)
        Spinner mSpStartNode;
        @BindView(R.id.spEndNode)
        Spinner mSpEndNode;
        @BindView(R.id.addPathRouteID)
        TextView mAddPathRouteID;
        @BindView(R.id.addPathOrderID)
        TextView mAddPathOrderID;
        @BindView(R.id.etPathMaxSpeed)
        EditText mEtPathMaxSpeed;
        @BindView(R.id.etPathDistance)
        EditText mEtPathDistance;
        @BindView(R.id.spPathYaw)
        Spinner mSpPathYaw;
        @BindView(R.id.spPathAngle)
        Spinner mSpPathAngle;
        @BindView(R.id.dialogOKBtn)
        Button mDialogOKBtn;
        @BindView(R.id.dialogCancelBtn)
        Button mDialogCancelBtn;
        @BindView(R.id.btnDelPath)
        Button mBtnDel;

        PathDialogViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
