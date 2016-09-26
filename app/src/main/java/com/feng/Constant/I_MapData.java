package com.feng.Constant;

/**
 * 有关数据库操作的 信息(列名)
 *
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2015-12-11 下午7:35:03
 */
public interface I_MapData {
    String MAP_DATABASE = "mapDatabase";

    /***
     * 节点表的 列名
     */
    String NODE_ID = "ID";
    String NODE_NAME = "name";
    String NODE_TYPE = "type";
    String NODE_RFID = "RFID";
    String NODE_POSITIONX = "positionX";
    String NODE_POSITIONY = "positionY";
    String NODE_WORKSPACE = "workspaceID";
    String NODE_TABLE_ROW = NODE_ID + "," + NODE_NAME + "," + NODE_TYPE + "," + NODE_RFID
            + "," + NODE_POSITIONX + "," + NODE_POSITIONY + "," + NODE_WORKSPACE;
    /**
     * 路径表 列名
     */
    String PATH_NODEID = "nodeID";
    String PATH_ENDNODEID = "endNodeID";
    String PATH_ROUTEID = "routeID";
    String PATH_ORDERID = "orderID";
    String PATH_DISTANCE = "distance";
    String PATH_YAW = "yaw";
    String PATH_ANGLE = "angle";
    String PATH_MAXSPEED = "maxSpeed";
    String PATH_TURNTYPE = "turnType";
    String PATH_TABLE_ROW =
            PATH_NODEID + "," + PATH_ENDNODEID + "," + PATH_ROUTEID + "," + PATH_ORDERID + "," +
                    PATH_YAW + "," + PATH_DISTANCE + "," + PATH_ANGLE + "," + PATH_MAXSPEED + "," + PATH_TURNTYPE;
    /***
     * 路线 列名
     */
    String ROUTE_ID = "ID";
    String ROUTE_PREID = "preRouteID";
    String ROUTE_NAME = "routeName";
    String ROUTE_WORKSPACEID = "workspaceID";
    String ROUTE_ENABLED = "routeEnabled";
    String ROUTE_TYPE = "routeType";
    String ROUTE_TABLE_ROW = ROUTE_ID + "," + ROUTE_PREID + "," + ROUTE_NAME + "," + ROUTE_WORKSPACEID + "," + ROUTE_ENABLED + ", " + ROUTE_TYPE;
    /**
     * 工作区 列名
     */
    String WORKSPACE_ID = "workspaceID";
    String WORKSPACE_NAME = "workspaceName";
    String WORKSPACE_FLOOR = "floor";
    String WORKSPACE_MAP = "map";
    String WORKSPACE_WIDTH = "workspaceWidth";
    String WORKSPACE_HEIGHT = "workspaceHeight";
    String WORKSPACE_TABLE_ROW = WORKSPACE_ID + "," + WORKSPACE_NAME + "," + WORKSPACE_FLOOR + "," + WORKSPACE_MAP + "," +
            WORKSPACE_WIDTH + "," + WORKSPACE_HEIGHT;

}
