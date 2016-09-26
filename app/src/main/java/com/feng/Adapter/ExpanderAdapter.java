package com.feng.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.feng.Database.Map.MapDatabaseHelper;
import com.feng.Database.Map.Node;
import com.feng.Database.Map.Route;
import com.feng.RSS.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by fengscar on 2016/5/20.
 */
public class ExpanderAdapter extends BaseExpandableListAdapter {
    // 回调接口...当任务添加时触发..
    private LayoutInflater inflater;
    private UserControlTaskListListener mUserControlTaskListener;


    private ArrayList<HashMap<Route, ArrayList<Node>>> taskList;


    /**
     * targetNode (show the target Icon)
     */
    private Node currentTarget;

    public ExpanderAdapter(Context context, UserControlTaskListListener tccb) {
        inflater = LayoutInflater.from(context);
        mUserControlTaskListener = tccb;

        taskList = new ArrayList<>();
    }

    public void addTarget(List<Node> nodes) {
        if (nodes != null) {
            for (Node node : nodes) {
                if (node != null) {
                    addTarget(node);
                }
            }
        }
    }

    public void addTarget(final Node node) {
        // 获取 到达该节点的所有路线
        Route route = MapDatabaseHelper.getInstance().getPassNodeRouteID(node.getId());
        if (route == null) {
            return;
        }
        //如果存在该路线...
        for (HashMap eachRoute : taskList) {
            // 如果该路线已存在,
            if (eachRoute.containsKey(route)) {
                if (!((ArrayList) eachRoute.get(route)).contains(node)) {
                    //添加完成
                    ((ArrayList) eachRoute.get(route)).add(node);
                    this.notifyDataSetChanged();
                    mUserControlTaskListener.onUserAddToExpander(taskList.indexOf(eachRoute));
                }
                return;
            }
        }
        // if not exists route
        HashMap<Route, ArrayList<Node>> newRoute = new HashMap<>();
        newRoute.put(route, new ArrayList<Node>() {
            {
                add(node);
            }
        });
        taskList.add(newRoute);
        this.notifyDataSetChanged();
        mUserControlTaskListener.onUserAddToExpander(taskList.indexOf(newRoute));

    }

    public void delTarget(List<Node> nodeList) {
        if (nodeList != null) {
            for (Node n : nodeList) {
                if (n != null) {
                    delTarget(n);
                }
            }
        }
    }

    public boolean delTarget(Node targetNode) {
        // 查找所有的路线
        for (HashMap route : taskList) {
            // 查找当前路线中的所有节点
            for (Object listNode : route.values()) {
                //如果节点列表中有该任务点...删除成功
                if (((ArrayList<Node>) listNode).remove(targetNode)) {
                    // 如果删除后,该路线没有任务了,删除掉该路线.
                    if (((ArrayList<Node>) listNode).size() == 0) {
                        taskList.remove(route);
                    }
                    this.notifyDataSetChanged();
                    return true;
                }
            }
        }
        return false;
    }

    //设置当前的目标点( 在childView 前面显示箭头标记)
    public void setCurrentTarget(Node node) {
        currentTarget = node;
        notifyDataSetChanged();
    }

    public List<Node> getTasks() {
        List<Node> nodes = new ArrayList<>();
        for (HashMap route : taskList) {
            for (Object nodelist : route.values())
                nodes.addAll((ArrayList<Node>) nodelist);
        }
        return nodes;
    }

    public void clearTask() {
        taskList.clear();
        notifyDataSetChanged();
    }


    /**
     * group  路线的view
     */
    @Override
    public int getGroupCount() {
        return taskList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        // 其实只遍历一次
        for (Route route : taskList.get(groupPosition).keySet()) {
            return route;
        }
        return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (taskList == null || taskList.size() == 0) {
            return null;
        }
        Group routeGroup = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.expandable_task_group, null);
            routeGroup = new Group();
            routeGroup.name = (TextView) convertView.findViewById(R.id.expandableLvGroupName);
//            routeGroup.btnUp = (Button) convertView.findViewById(R.id.expandableLvGroupBtnUp);
//            routeGroup.btnDown = (Button) convertView.findViewById(R.id.expandableLvGroupBtnDown);
            convertView.setTag(routeGroup);
        } else {
            routeGroup = (Group) convertView.getTag();
        }
        final Route route = (Route) getGroup(groupPosition);
        /** 路线名称 */
        routeGroup.name.setText(route.getName());

        /** 路线上的 ↑按键 ,不是头一个时添加*/
        //			if ( groupPosition > 0){
        //				routeGroup.btnUp.setVisibility(View.VISIBLE);
        //				//已在 XML 中设置不可获取焦点 ( 如果 不满足,虽然不显示 ,但是会 获取焦点 , group就无法expandable)
        //				//	routeGroup.btnUp.setFocusable(false);
        //				routeGroup.btnUp.setOnClickListener(new OnClickListener() {
        //					public void onClick(View v) {
        //						Log.e("tag","点击了"+groupPosition + "中的 ↑ ↑ ");
        //						swap(routeList, groupPosition, groupPosition-1);
        //						handler.sendEmptyMessage(REFRESH);
        //					}
        //				});
        //			}
        //			/** 路线上的 ↓按键 不是最后一个时添加*/
        //			if ( groupPosition!=routeList.size()-1){
        //				routeGroup.btnDown.setVisibility(View.VISIBLE);
        //				routeGroup.btnDown.setOnClickListener(new OnClickListener() {
        //					public void onClick(View v) {
        //						Log.e("tag","点击了"+groupPosition + "中的  ↓ ↓ ");
        //						swap(routeList, groupPosition, groupPosition+1);
        //						handler.sendEmptyMessage(REFRESH);
        //					}
        //				});
        //			}
        return convertView;
    }

    /**
     * node 目标任务的( path中的endNode) 的view
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        // 其实只遍历一次
        for (List<Node> nodeList : taskList.get(groupPosition).values()) {
            return nodeList.size();
        }
        return 0;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        // 其实只遍历一次
        for (List<Node> nodeList : taskList.get(groupPosition).values()) {
            return nodeList.get(childPosition);
        }
        return null;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        Child child = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.expandable_task_child, null);
            child = new Child();
            child.name = (TextView) convertView.findViewById(R.id.expandableLvChildName);
//            child.target = (ImageView) convertView.findViewById(R.id.expandableLvChildImg);
            convertView.setTag(child);
        } else {
            child = (Child) convertView.getTag();
        }
        final Node node = (Node) getChild(groupPosition, childPosition);
        child.name.setText(node.getName());

        //  如果是当前任务 ,显示 箭头标志
//        if (currentTarget != null) {
//            if (node.equals(currentTarget)) {
//                // 设置 可见
//                child.target.setVisibility(View.VISIBLE);
//            }
//        }
        // 点击后 回调 activity传入的方法.( 将通过USB发送删除命令, 任务列表还未更新)
        convertView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mUserControlTaskListener.onUserDelFromExpander(node);
            }
        });
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    // View holder
    class Group {
        TextView name;
        Button btnUp;
        Button btnDown;
    }

    class Child {
        ImageView target;
        TextView name;
    }
}