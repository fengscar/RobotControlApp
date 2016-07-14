package com.feng.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import com.feng.Activities.MainActivity;
import com.feng.Constant.I_MapData;
import com.feng.Constant.I_Parameters;
import com.feng.Database.MapDatabaseHelper;
import com.feng.Database.Node;
import com.feng.RSS.R;
import com.feng.CustomView.CustomDialog;
import com.feng.CustomView.CustomDialogCallback;
import com.feng.RobotApplication;
import com.feng.Utils.SP;

import java.util.ArrayList;
import java.util.List;


/**
 * 列表模式 下的操作界面
 *
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2015-12-22 上午10:05:40
 * @功能
 */
public class ListingFragment extends Fragment implements I_Parameters, I_MapData, IFragmentControl {
    private UserControlFragmentCallback mUserControlFragmentCallback;

    private MapDatabaseHelper mDatabaseHelper = MapDatabaseHelper.getInstance();
    private GridView listingGridView;
    // 数据库中的所有节点
    private List<Node> listNode;
    //	已选的 任务节点
    private List<Node> taskNode;

    private ListingAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listing, null);
        view.findViewById(R.id.listingFrameLayout);


        initListingGrid(view);

        // 根据传递的参数 初始化数据
        Bundle bundle = getArguments();
        this.setWorkspace(bundle.getInt(BundleWorkspaceID));
        this.initTasks(bundle.getIntArray(BundleTaskListIDS));

        return view;

    }

    private void initListingGrid(View view) {
        listNode = new ArrayList<>();
        taskNode = new ArrayList<>();
        mAdapter = new ListingAdapter(this.getActivity());
        // 适配 gridView
        listingGridView = (GridView) view.findViewById(R.id.listingGridView);
        // 获取 SP 中的参数设置  , 如果没有 获取默认 的3
        listingGridView.setNumColumns((int) SP.get(getActivity(), COLUMN_NUM, 3));

        listingGridView.setAdapter(mAdapter);

        //TODO 保留吧
        listingGridView.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           final int position, long id) {
                // 和mapFragment 中有相同的 方法 ( 作用都是将当前的位置定位成 长按的btn)
                CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
                builder.getConfirmDialog("定位", "是否将当前位置设置为" + listNode.get(position).getName(),
                        new CustomDialogCallback() {
                            public boolean onDialogBtnClick(List<View> viewList) {
                                ((MainActivity) getActivity()).setCurrentNode(listNode.get(position));
                                return true;
                            }
                        }).show();
                return true;
            }
        });
    }

    private void initTasks(int[] taskIDs) {
        if (taskIDs == null) {
            return;
        }
        Node node = new Node();
        for (int id : taskIDs) {
            taskNode.add(mDatabaseHelper.getNodeByID(id));
        }
    }


    class ListingAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public ListingAdapter(Context cx) {
            inflater = LayoutInflater.from(cx);
        }

        public int getCount() {
            return listNode.size();
        }

        public Object getItem(int position) {
            return listNode.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            NodeHolder nodeHolder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.gridview_of_node, null);
                nodeHolder = new NodeHolder();
                nodeHolder.btnNode = (Button) convertView.findViewById(R.id.btnNode);
                convertView.setTag(nodeHolder);
            } else {
                nodeHolder = (NodeHolder) convertView.getTag();
            }
            nodeHolder.btnNode.setText(listNode.get(position).getName());
            final NodeHolder finalNodeHolder = nodeHolder;
            nodeHolder.btnNode.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // 点击后更新任务列表
                    if (mUserControlFragmentCallback == null) {
                        return;
                    }
                    if (finalNodeHolder.btnNode.isSelected()) {
                        mUserControlFragmentCallback.onUserDel(listNode.get(position));
                    } else {
                        mUserControlFragmentCallback.onUserAdd(listNode.get(position));
                    }
                }
            });
            // 如果是任务节点,变更按键颜色
            if (isTask(listNode.get(position))) {
                nodeHolder.btnNode.setSelected(true);
            } else {
                nodeHolder.btnNode.setSelected(false);
            }
            return convertView;
        }

        private boolean isTask(Node node) {
            for (Node task : taskNode) {
                if (task.equals(node)) {
                    return true;
                }
            }
            return false;
        }

        class NodeHolder {
            Button btnNode;
        }

    }

    /**
     * 设置工作区 并且初始化adapter...
     *
     * @param wspId
     */
    private void setWorkspace(int wspId) {
        //获取数据  (可以根据 ID或者 类型来排序)
        String[] showNodeTypes = new String[]{I_Parameters.NODE_TYPE.TABLE, I_Parameters.NODE_TYPE.KITCHEN};
        if (("ID").equals(SP.get(RobotApplication.getContext(), SORT_TYPE, "ID"))) {
            listNode = mDatabaseHelper.getReachableNode(wspId, showNodeTypes, NODE_ID);
        } else {
            listNode = mDatabaseHelper.getReachableNode(wspId, showNodeTypes, NODE_TYPE);
        }
        mAdapter.notifyDataSetChanged();
    }

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
    public void setCallback(UserControlFragmentCallback ucc) {
        mUserControlFragmentCallback = ucc;
    }


    @Override
    public void addTask(Node node) {
        // 如果已选任务中不存在该节点, 并且当前显示的Grid(listNode)中有该节点
        if (!taskNode.contains(node) && listNode.contains(node)) {
            taskNode.add(node);
            mAdapter.notifyDataSetChanged();
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
        if (taskNode.remove(node)) {
            mAdapter.notifyDataSetChanged();
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
        taskNode.clear();
        mAdapter.notifyDataSetChanged();
    }

}