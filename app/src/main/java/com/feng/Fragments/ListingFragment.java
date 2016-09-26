package com.feng.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.feng.Adapter.UserControlFragmentListener;
import com.feng.Constant.I_MapData;
import com.feng.Constant.I_Parameters;
import com.feng.Database.Map.MapDatabaseHelper;
import com.feng.Database.Map.Node;
import com.feng.RSS.R;
import com.feng.RobotApplication;
import com.feng.Utils.SP;

import java.util.ArrayList;
import java.util.List;


/**
 * 列表模式 下的操作界面
 *
 * @author 福建省和创伟业智能科技有限公司
 *         2015-12-22 上午10:05:40
 */
public class ListingFragment extends Fragment implements I_Parameters, I_MapData, IFragmentControl {
    @BindView(R.id.listingGridView)
    GridView mListingGridView;

    private UserControlFragmentListener mUserControlFragmentCallback;

    private MapDatabaseHelper mDatabaseHelper = MapDatabaseHelper.getInstance();
    // 数据库中的所有节点, 包含是否已选的属性
    private List<Target> mTargetList;
    private ListingAdapter mAdapter;
    private int mWorkspaceID;

    private ListingFragment() {
    }

    public static Fragment getInstance(int wspID, int[] currentTasks) {
        ListingFragment fragment = new ListingFragment();
        // 初始化fragment需要使用bundle来传递数据,不足的是从List<node> 转成 integer后又要转回来,不然就是使用序列化?
        Bundle bundle = new Bundle();
        bundle.putInt(BundleWorkspaceID, wspID);
        bundle.putIntArray(BundleTaskListIDS, currentTasks);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listing, null);
        view.findViewById(R.id.listingFrameLayout);

        ButterKnife.bind(this, view);

        // 根据传递的参数 初始化数据
        Bundle bundle = getArguments();
        this.initData(bundle.getInt(BundleWorkspaceID), bundle.getIntArray(BundleTaskListIDS));

        initListingGrid();

        return view;

    }

    private void initListingGrid() {
        // 获取 SP 中的参数设置  , 如果没有 获取默认 的3
        int gridViewColumnNum = (int) SP.get(getActivity(), COLUMN_NUM, 3);
        mListingGridView.setNumColumns(gridViewColumnNum);
        mAdapter = new ListingAdapter(this.getActivity(), gridViewColumnNum);
        mListingGridView.setAdapter(mAdapter);

        //TODO 手动设置当前位置，保留吧
//        mListingGridView.setOnItemLongClickListener(new OnItemLongClickListener() {
//            public boolean onItemLongClick(AdapterView<?> parent, View view,
//                                           final int position, long id) {
//                // 和mapFragment 中有相同的 方法 ( 作用都是将当前的位置定位成 长按的btn)
//                CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
//                builder.getConfirmDialog("定位", "是否将当前位置设置为" + listNode.get(position).getName(),
//                        new CustomDialogCallback() {
//                            public boolean onDialogBtnClick(List<View> viewList) {
//                                ((MainActivity) getActivity()).setCurrentNode(listNode.get(position));
//                                return true;
//                            }
//                        }).show();
//                return true;
//            }
//        });
    }

    class ListingAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        //传入列数,来设置相应的字体大小
        private int mColumnNum;

        public ListingAdapter(Context cx, int cn) {
            inflater = LayoutInflater.from(cx);
            mColumnNum = cn;
        }

        public int getCount() {
            return mTargetList.size();
        }

        public Object getItem(int position) {
            return mTargetList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder nodeHolder;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.gridview_of_node, null);
                nodeHolder = new ViewHolder(convertView);
                convertView.setTag(nodeHolder);
            } else {
                nodeHolder = (ViewHolder) convertView.getTag();
            }

            nodeHolder.btnNode.setText(mTargetList.get(position).getNode().getName());
            nodeHolder.tvRouteName.setText(mTargetList.get(position).getRouteName());
            // 根据列数大小来设置 【节点名称】字体大小 (成反比) : 标准时 3列 40sp
            int sizeOfNode = (int) (3.0f / mColumnNum * 40);
            // 根据列数大小来设置 【路线名称】字体大小 (成反比) : 标准时 3列 20sp
            int sizeOfRoute = (int) (3.0f / mColumnNum * 20);
            nodeHolder.btnNode.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeOfNode);
            nodeHolder.tvRouteName.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeOfRoute);
            // 如果是任务节点,变更按键颜色
            nodeHolder.btnNode.setSelected(mTargetList.get(position).isSelect());


            // 点击事件
            nodeHolder.btnNode.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // 点击后更新任务列表
                    if (mUserControlFragmentCallback == null) {
                        return;
                    }
                    if (mTargetList.get(position).isSelect()) {
                        mUserControlFragmentCallback.onUserDel(mTargetList.get(position).getNode());
                    } else {
                        mUserControlFragmentCallback.onUserAdd(mTargetList.get(position).getNode());
                    }
                }
            });
            return convertView;
        }

        class ViewHolder {
            @BindView(R.id.tvRouteName)
            TextView tvRouteName;
            @BindView(R.id.btnNode)
            Button btnNode;

            public ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }

    }

    /**
     * 设置工作区 并且初始化adapter、初始化数据
     *
     * @param wspId   工作区ID
     * @param taskIDs 已选的任务
     */
    private void initData(int wspId, int[] taskIDs) {
        mWorkspaceID = wspId;

        mTargetList = new ArrayList<>();

        //获取数据  (可以根据 ID或者 类型来排序)
        String[] showNodeTypes = new String[]{I_Parameters.NODE_TYPE.TABLE, I_Parameters.NODE_TYPE.KITCHEN};
        List<Node> listNode;
        if (("ID").equals(SP.get(RobotApplication.getContext(), SORT_TYPE, "ID"))) {
            listNode = mDatabaseHelper.getReachableNode(wspId, showNodeTypes, NODE_ID);
        } else {
            listNode = mDatabaseHelper.getReachableNode(wspId, showNodeTypes, NODE_TYPE);
        }
        // 将 node， routeName， isSelect 封装成target
        for (Node node : listNode) {
            String routeName = mDatabaseHelper.getPassNodeRouteID(node.getId()).getName();
            Target target = new Target(node, routeName, false);

            // 如果初始化的任务列表 taskIDs中有该target，设置为已选
            if (taskIDs != null) {
                for (int id : taskIDs) {
                    if (id == node.getId()) {
                        target.setSelect(true);
                        break;
                    }
                }

            }
            mTargetList.add(target);
        }
    }


    @Override
    public int getWorkspaceID() {
        return mWorkspaceID;
    }

    @Override
    public void setCallback(UserControlFragmentListener ucc) {
        mUserControlFragmentCallback = ucc;
    }


    @Override
    public void addTask(Node node) {
        // 如果已选任务中不存在该节点, 并且当前显示的Grid(listNode)中有该节点
        for (Target target : mTargetList) {
            if (target.getNode().equals(node)) {
                target.setSelect(true);
                mAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void addTask(List<Node> nodeList) {
        if (nodeList != null) {
            for (Node n : nodeList) {
                addTask(n);
            }
        }
    }


    @Override
    public void delTask(Node node) {
        for (Target target : mTargetList) {
            if (target.getNode().equals(node)) {
                target.setSelect(false);
                mAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void delTask(List<Node> nodeList) {
        if (nodeList != null) {
            for (Node n : nodeList) {
                delTask(n);
            }
        }
    }

    @Override
    public void clearTask() {
        for (Target target : mTargetList) {
            target.setSelect(false);
        }
        mAdapter.notifyDataSetChanged();
    }

}