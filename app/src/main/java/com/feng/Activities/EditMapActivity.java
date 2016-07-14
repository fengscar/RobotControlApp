package com.feng.Activities;

import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import com.feng.Base.BaseActivity;
import com.feng.Constant.I_Parameters;
import com.feng.CustomView.CustomDialog;
import com.feng.CustomView.CustomDialogCallback;
import com.feng.CustomView.IconButton;
import com.feng.CustomView.PopupWindowManager;
import com.feng.Database.*;
import com.feng.MapModule.MapFragment;
import com.feng.RSS.R;
import com.feng.Utils.L;

import java.util.ArrayList;
import java.util.List;


public class EditMapActivity extends BaseActivity implements I_Parameters {
    // 工作区名称 点击后返回上一页
    private MapDatabaseHelper mMapDatabaseHelper = MapDatabaseHelper.getInstance();
    private List<Route> routeListData;
    private ListView mRouteList;
    private Workspace mWorkspace;
    //传递 当前选择的路线 值
    private MapFragment mapFragment;
    // 高亮当前选择的值
    private RouteListViewAdapter listViewAdapter;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_map);

        mWorkspace = mMapDatabaseHelper.getWorkspaceByID(getIntent().getIntExtra("workspaceID", -1));
        if (mWorkspace == null) {
            L.e(EditMapActivity.class.getSimpleName(), "初始化工作区失败!");
            return;
        }
        initView();
        initFragment();
    }

    private void initView() {
        //整体布局
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.dlEditMap);
        //默认打开抽屉
        drawerLayout.openDrawer(GravityCompat.START);
        // 返回按键
        IconButton iconBtn = (IconButton) findViewById(R.id.iconBtnBack);
        // 工作区名称
        iconBtn.setText(mWorkspace.getName());
        iconBtn.setBackgroundColor(Color.TRANSPARENT);
        iconBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                EditMapActivity.this.finish();
            }
        });

        // 添加路线 按键
        findViewById(R.id.addRoute).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showAddRouteDialog();
            }
        });

        // 自动调整的按键
        findViewById(R.id.btnAdjust).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mapFragment != null) {
                    mapFragment.autoAdjust();
                }
            }
        });

        routeListData = getRouteListData();
        // 路线的ListView
        mRouteList = (ListView) findViewById(R.id.routeListView);
        listViewAdapter = new RouteListViewAdapter(this);
        if (routeListData.size() >= 1) {
            listViewAdapter.setSelectItem(0);
        }
        mRouteList.setAdapter(listViewAdapter);

        //region 点击路线 -> 切换该路线
        mRouteList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                listViewAdapter.setSelectItem(position);
                listViewAdapter.notifyDataSetInvalidated();
                // 刷新Fragment
                mapFragment.setRouteID(routeListData.get(position).getId());
                mapFragment.refreshPath();
            }
        });
        //endregion

        //region 长按路线 -> 编辑/删除
        mRouteList.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           final int position, long id) {
                final PopupWindowManager pwm = new PopupWindowManager(EditMapActivity.this);
                pwm.showPopupWindowAsDropDown(view,
                        R.layout.popup_edit_route,
                        view.getWidth(), -view.getHeight(),
                        new int[]{R.id.popupEditPath, R.id.popupEditRoute, R.id.popupDelRoute},
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
                                                    listViewAdapter.notifyDataSetInvalidated();
                                                    //将 当前路径设置为 0
                                                    if (routeListData.size() != 0) {
                                                        listViewAdapter.setSelectItem(0);
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
        FragmentTransaction fragmentTranscation = fragmentManager.beginTransaction();
        mapFragment = new MapFragment();
        Bundle bd = new Bundle();
        if (routeListData.size() != 0) {
            bd.putInt("routeID", routeListData.get(0).getId());
        }
        bd.putInt("workspaceID", mWorkspace.getId());
        mapFragment.setArguments(bd);
        fragmentTranscation.replace(R.id.edit_map_frame, mapFragment);
        fragmentTranscation.commit();
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
            List<Route> routeList = mMapDatabaseHelper.getAllRoute(mWorkspace.getId());
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
                            listViewAdapter.setSelectItem(routeListData.size() - 1);
                            listViewAdapter.notifyDataSetInvalidated();

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
                            listViewAdapter.notifyDataSetInvalidated();
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final RouteHolder routeHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.listview_of_route, null);
                routeHolder = new RouteHolder();
                //	wsHolder.imageBtn=(ImageButton)convertView.findViewById(R.id.girdViewOfWorkspaceImageBtn);
                routeHolder.ID = (TextView) convertView.findViewById(R.id.listViewRouteID);
                routeHolder.name = (TextView) convertView.findViewById(R.id.listViewRouteName);
                routeHolder.preID = (TextView) convertView.findViewById(R.id.listViewPreRouteID);
                //				routeHolder.enableImage=(ImageView)convertView.findViewById(R.id.listVIewRouteEnable);
                convertView.setTag(routeHolder);
            } else {
                routeHolder = (RouteHolder) convertView.getTag();
            }
            //这里应该从数据库获取 还有 修改数据库接口(还未返回图片)
            Route route = routeListData.get(position);
            routeHolder.name.setText(route.getName());
            routeHolder.ID.setText(String.valueOf(route.getId()));
            routeHolder.preID.setText(String.valueOf(route.getPreID()));

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

    private void showAddPathDialog(final Route route, Node startNode) {
        CustomDialog.Builder mBuilder = new CustomDialog.Builder(this);
        mBuilder.setResourceID(R.layout.dialog_path_edit);
        //初始化Spinner选项
        String[] selections = new String[]{PATH_TURN.STRAIGHT, PATH_TURN.LEFT, PATH_TURN.RIGHT};
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, selections);
        View dialogLayout = mBuilder.getConvertView();
        //  航向角度
        Spinner directionSpinner = (Spinner) dialogLayout.findViewById(R.id.spPathYaw);
        directionSpinner.setAdapter(adapter);
        // 到点转向
        Spinner angleSpinner = (Spinner) dialogLayout.findViewById(R.id.spPathAngle);
        angleSpinner.setAdapter(adapter);
        // 起始点
        Spinner spStart = (Spinner) dialogLayout.findViewById(R.id.spStartNode);
        Spinner spEnd = (Spinner) dialogLayout.findViewById(R.id.spEndNode);
        // 如果起始点为空,表示当前路线还未选择第一个开始点
        if (startNode == null) {
            List<Node> selectableNodes = new ArrayList<>();
            // 如果当前路线是第一条路线,则起始点在工作区内随便选
            if (route.getPreID() == 0) {
                selectableNodes = mMapDatabaseHelper.getAllNode(route.getWorkspaceID());
            } else {
                //如果当前路线不是第一条路线,则在前级路线上选择起始点
                selectableNodes = mMapDatabaseHelper.getNodeByRoute(route.getPreID());
            }
            String[] selection = new String[selectableNodes.size()];
            for (int i = 0; i < selectableNodes.size(); i++) {
                selection[i] = selectableNodes.get(i).getName();
            }
            spStart.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, selection));

            //当 起点选项更新时,刷新 终点的可选项
            spStart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String nodeName = ((TextView) view).getText().toString();
                    Node node = MapDatabaseHelper.getInstance().getNodeByName(route.getWorkspaceID(), nodeName);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        } else {
            String[] startSelection = new String[]{startNode.getName()};
            spStart.setEnabled(false);
            spStart.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, startSelection));

            List<Node> selectableNodes = mMapDatabaseHelper.getSelectableNode(route, startNode);
            String[] selection = new String[selectableNodes.size()];
            for (int i = 0; i < selectableNodes.size(); i++) {
                selection[i] = selectableNodes.get(i).getName();
            }
            spEnd.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, selection));
        }


        // 获取 已经确定的值
        final Path pathAdd = new Path();
        pathAdd.setRouteID(route.getId());
        int orderID = mMapDatabaseHelper.getMaxPathOrder(route.getId()) + 1;
        pathAdd.setOrderID(orderID);
        mBuilder.setCancelBtnClick(R.id.dialogCancelBtn, null)
                .setTitle("添加路径")
                .setButtonText("添加", "取消")
                .setOkBtnClick(R.id.dialogOKBtn, new CustomDialogCallback() {
                    public boolean onDialogBtnClick(List<View> viewList) {
                        Path newPath = Path.loadPath(pathAdd, viewList);
                        if (newPath == null) {
                            L.e("装载 路径 时出错");
                            return false;
                        } else {
                            return mMapDatabaseHelper.addData(newPath);
                        }
                    }
                })
                .create(new int[]{R.id.spStartNode, R.id.spEndNode,
                        R.id.addPathRouteID, R.id.addPathOrderID,
                        R.id.spPathYaw, R.id.etPathDistance,
                        R.id.spPathAngle, R.id.etPathMaxSpeed}, new Object[]{
                        0, 0, route.getId(), orderID,
                        null, null, null, null})
                .show();
    }

    private void showEditPathDialog(final Path path) {
        CustomDialog.Builder mBuilder = new CustomDialog.Builder(this);
        mBuilder.setResourceID(R.layout.dialog_path_edit);
        //初始化Spinner选项
        String[] selections = new String[]{PATH_TURN.STRAIGHT, PATH_TURN.LEFT, PATH_TURN.RIGHT};
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, selections);
        View dialogLayout = mBuilder.getConvertView();
        //  航向角度
        Spinner directionSpinner = (Spinner) dialogLayout.findViewById(R.id.spPathYaw);
        directionSpinner.setAdapter(adapter);
        int currentDirectionSelect = getPathTurnSpinnerItemOrder(path.getYaw());
        // 到点转向
        Spinner angleSpinner = (Spinner) dialogLayout.findViewById(R.id.spPathAngle);
        angleSpinner.setAdapter(adapter);
        int currentAngleSelect = getPathTurnSpinnerItemOrder(path.getAngle());

        // 起始点
        Spinner spStart = (Spinner) dialogLayout.findViewById(R.id.spStartNode);
        spStart.setEnabled(false);
        String[] startNodeSelection = new String[]{mMapDatabaseHelper.getNodeByID(path.getNodeID()).getName()};
        spStart.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, startNodeSelection));
        // 终点
        Spinner spEnd = (Spinner) dialogLayout.findViewById(R.id.spEndNode);
        spEnd.setEnabled(false);
        String[] endNodeSelection = new String[]{mMapDatabaseHelper.getNodeByID(path.getEndNode()).getName()};
        spEnd.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, endNodeSelection));

        mBuilder.setCancelBtnClick(R.id.dialogCancelBtn, null)
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
                            return true;
                        } else {
                            return false;
                        }
                    }
                })
                .create(new int[]{R.id.spStartNode, R.id.spEndNode,
                        R.id.addPathRouteID, R.id.addPathOrderID,
                        R.id.spPathYaw, R.id.etPathDistance,
                        R.id.spPathAngle, R.id.etPathMaxSpeed}, new Object[]{
                        0, 0, path.getRouteID(), path.getOrderID(),
                        currentDirectionSelect, null, currentAngleSelect, null})
                .show();

    }

    private void showChoosePathDialog(final Route route) {
        final CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setResourceID(R.layout.dialog_path_choose);
        //初始化标题,2个按键( 添加路径+取消)
        builder.setTitle(route.getName() + "包含的路径");
        builder.setButtonText("添加路径", "取消");
        builder.setCancelBtnClick(R.id.dialogCancelBtn, null);
        //当前路线的最后一个节点. 如果没有则为空
        final Node node = mMapDatabaseHelper.getLastNodeByRouteID(route.getId());
        builder.setOkBtnClick(R.id.dialogOKBtn, new CustomDialogCallback() {
            @Override
            public boolean onDialogBtnClick(List<View> viewList) {
                showAddPathDialog(route, node);
                return true;
            }
        });
        // 如果该节点不可以添加路径,将添加按键设置为不可用
        Button addPath = (Button) builder.getViewByID(R.id.dialogOKBtn);
        if (mMapDatabaseHelper.isCompleted(route)) {
            addPath.setEnabled(false);
        }
        Dialog dialog = builder.create(null, null);
        ListView lvPaths = (ListView) builder.getViewByID(R.id.lvPathList);
        PathListViewAdapter pathChooseDialogAdapter = new PathListViewAdapter(this, route, dialog);
        lvPaths.setAdapter(pathChooseDialogAdapter);
        dialog.show();
    }

    private int getPathTurnSpinnerItemOrder(int turnYaw) {
        if (turnYaw == 0) {
            return 0; //直走
        }
        // 负值代表左转 --> 1 . 正直代表右转 -->第2个选项
        return turnYaw > 0 ? 2 : 1;
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
            ViewHolder holder = null;
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
                        showEditPathDialog(path);
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

        class ViewHolder {
            TextView tvPathOrder;
            TextView tvPathName;
            ImageButton ibPathDel;
        }
    }
}
