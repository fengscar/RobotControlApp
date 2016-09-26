package com.feng.Database.Map;

import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import com.feng.Constant.I_MapData;
import com.feng.RSS.R;
import com.feng.Utils.T;

import java.util.List;

/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2015-11-20 下午9:36:11
 */
public class Path extends DatabaseManager implements I_MapData {
    private static final String LOG = Path.class.getSimpleName();

    private int routeID;  //所属的route 移除,不需要?
    private int nodeID; //路径的原点
    private int endNode;  //方便连线
    private int orderID;  //处于路线上的顺序

    private int distance;     // 到下个节点的 距离 单位米
    private int yaw; // 到下个节点的转向角 顺时针为正,逆时针为负
    private int angle; // 该节点 转向餐桌的角度 顺时针为正,逆时针为负
    private int maxSpeed; //到下个节点的 最大速度

    private int turnType; //转向方式 0 圆弧, 1: 原地转 2:转完停止

    //region Constructor
    public Path() {
    }

    public Path(int routeID, int startNode, int endNode) {
        super();
        this.routeID = routeID;
        this.nodeID = startNode;
        this.endNode = endNode;
    }

    public Path(int routeID, int orderID, int nodeID, int endNode) {
        super();
        this.routeID = routeID;
        this.orderID = orderID;
        this.nodeID = nodeID;
        this.endNode = endNode;
    }

    public Path(int routeID, int nodeID, int endNode, int orderID,
                int yaw, int distance, int angle, int maxSpeed) {
        super();
        this.routeID = routeID;
        this.nodeID = nodeID;
        this.endNode = endNode;
        this.orderID = orderID;
        this.distance = distance;
        this.yaw = yaw;
        this.angle = angle;
        this.maxSpeed = maxSpeed;
    }
    //endregion

    //region Getter/Setter
    public int getRouteID() {
        return routeID;
    }

    public void setRouteID(int routeID) {
        this.routeID = routeID;
    }

    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public int getEndNode() {
        return endNode;
    }

    public void setEndNode(int endNode) {
        this.endNode = endNode;
    }

    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public int getYaw() {
        return yaw;
    }

    public void setYaw(int yaw) {
        this.yaw = yaw;
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public int getTurnType() {
        return turnType;
    }

    public void setTurnType(int turnType) {
        this.turnType = turnType;
    }

    //endregion

    //region hashCode,equals
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + endNode;
        result = prime * result + nodeID;
        result = prime * result + routeID;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Path other = (Path) obj;
        if (endNode != other.endNode)
            return false;
        if (nodeID != other.nodeID)
            return false;
        if (routeID != other.routeID)
            return false;
        return true;
    }
    //endregion

    /**
     *
     */
    public static Path loadPath(Path path, List<View> viewList) {
        MapDatabaseHelper instance = MapDatabaseHelper.getInstance();
        int wspID = instance.getWorkspaceByPath(path).getId();
        for (View currentView : viewList) {
            try {
                switch (currentView.getId()) {
                    case R.id.spPathYaw:
                        // 选中第一个为0度,直走..
                        // 选中第二个为左转, -90度
                        // 选中第三个为右转, 90度
                        int selectItem = ((Spinner) currentView).getSelectedItemPosition();
                        int yaw = getAngleFromSpinnner(selectItem);
                        path.setYaw(yaw);
                        break;
                    case R.id.spPathAngle:
                        int selectItem2 = ((Spinner) currentView).getSelectedItemPosition();
                        int angle = getAngleFromSpinnner(selectItem2);
                        path.setAngle(angle);
                        break;
                    case R.id.etPathDistance:
                        path.setDistance((Integer.parseInt(((EditText) currentView).getText().toString())));
                        break;
                    case R.id.etPathMaxSpeed:
                        path.setMaxSpeed((Integer.parseInt(((EditText) currentView).getText().toString())));
                        break;

                    case R.id.spStartNode:
                        if (((Spinner) currentView).getSelectedItem() == null) {
                            throw new IllegalArgumentException("路径未选择起点");
                        }
                        String nodeName = ((Spinner) currentView).getSelectedItem().toString();
                        Node node = MapDatabaseHelper.getInstance().getNodeByName(wspID, nodeName);
                        path.setNodeID(node.getId());
                        break;
                    case R.id.spEndNode:
                        if (((Spinner) currentView).getSelectedItem() == null) {
                            throw new IllegalArgumentException("路径未选择终点");
                        }
                        String endNodeName = ((Spinner) currentView).getSelectedItem().toString();
                        Node endNode = MapDatabaseHelper.getInstance().getNodeByName(wspID, endNodeName);
                        path.setEndNode(endNode.getId());
                        break;
                    case R.id.spPathTurnType:
                        int turnTypeSelectItem = ((Spinner) currentView).getSelectedItemPosition();
                        path.setTurnType(turnTypeSelectItem);
                        break;
                }
            } catch (IllegalArgumentException e) {
                T.show("添加失败,请完整输入数据");
                return null;
            }
        }
        return path;
    }

    private static int getAngleFromSpinnner(int spSelectionItem) {
        switch (spSelectionItem) {
            case 0:
                return 0; //直走
            case 1:
                return -90; //左转
            case 2:
                return 90;//右转
            case 3:
                return -180; //掉头
        }
        return 0;
    }

//    /**
//     * 如果添加完成 并且 前级路线不为0 , 需要添加 尾节点.. ARM需要该数据
//     */
//    if (isCompleted() == true && this.getPreRouteID() != 0) {
//        // 获取尾节点的 目标点
//        int lastStart = this.getEndNode();
//        // 获取前级路线
//        int preRouteID = this.getPreRouteID();
//        //获取最后一个节点
//        Path lastPath = new Path().getPathByRouteAndNode(preRouteID, lastStart);
//        if (lastPath != null) {
//            //更改path属性  路线改为 -> 当前所在路线
//            lastPath.setRouteID(this.getRouteID());
//            //更改path属性  排序 Order 改为 -> 当前顺序
//            int currentOrder = new Route(this.getRouteID()).getCurrentPathOrderID();
//            lastPath.setOrderID(currentOrder);
//            // 添加数据到 db 这里要防止递归! 所以不使用 addTodatabase()
//            db.execSQL(sql, new Object[]{
//                    lastPath.getNodeID(), lastPath.getEndNode(),
//                    lastPath.getRouteID(), lastPath.getOrderID(),
//                    lastPath.getYaw(), lastPath.getDistance(),
//                    lastPath.getAngle(), lastPath.getMaxSpeed()});
//
//        }
//    }
}
