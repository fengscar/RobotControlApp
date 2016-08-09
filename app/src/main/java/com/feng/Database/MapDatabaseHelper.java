package com.feng.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.util.Log;
import com.feng.Constant.I_MapData;
import com.feng.Constant.I_Parameters;
import com.feng.RobotApplication;
import com.feng.Utils.L;
import com.feng.Utils.T;
import com.feng.Utils.WindowUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2015-11-20 下午4:08:37
 * @功能 数据库操作基础类 (创建db,升级db)
 */
public class MapDatabaseHelper extends SQLiteOpenHelper implements I_MapData {
    private final static String LOG = MapDatabaseHelper.class.getSimpleName();
    private static int VERSION = 4;

    //region 构造和获取实例
    private SQLiteDatabase mDatabase;

    private MapDatabaseHelper(Context context) {
        super(context, MAP_DATABASE, null, VERSION);
        mDatabase = getWritableDatabase();
    }

    private volatile static MapDatabaseHelper mInstance;

    public static MapDatabaseHelper getInstance() {
        if (mInstance == null) {
            synchronized (MapDatabaseHelper.class) {
                if (mInstance == null) {
                    mInstance = new MapDatabaseHelper(RobotApplication.getContext());
                }
            }
        }
        return mInstance;
    }
    //endregion

    //region 重写的父类方法
    // 首次打开时,将会调用
    @Override
    public void onCreate(SQLiteDatabase db) {
        L.i(LOG, "首次使用,创建数据库: " + MAP_DATABASE + ".db");
        createAllTable(db);
    }

    // 当调用构造函数 发现已存在同名数据库时,升级版本
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        String sqlAddWidthColumn = "alter table workspace add " + WORKSPACE_WIDTH + " integer";
//        String sqlAddHeightColumn = "alter table workspace add " + WORKSPACE_HEIGHT + " integer";
//        // SQLite似乎不支持一次添加多个字段,需要分开
//        db.execSQL(sqlAddWidthColumn);
//        db.execSQL(sqlAddHeightColumn);
//        L.i(LOG, "更新版本: 从" + oldVersion + "升级到" + newVersion + ",本次更新在Workspace表中添加了2个字段:工作区的尺寸大小");
    }

    // SQLite defaults to PRAGMA foreign_keys = OFF every time you open the database.
    //this method called before onCreate/onOpen/onUpdate
    // I set foreigh_keys = On here, so the delete cascade is working  after this time
    @Override
    public void onConfigure(SQLiteDatabase db) {
        L.i(LOG, "开启外键关联: set foreigh_key=ON ");
        db.execSQL("PRAGMA foreign_keys = ON;");
        super.onConfigure(db);
    }
    //endregion

    //region 创建表,删除表
    private void createAllTable(SQLiteDatabase db) {
        String createWorkspaceTableSQL = "create table if not exists Workspace(" +
                WORKSPACE_ID + " integer not null primary key," +
                WORKSPACE_NAME + " text not null," +
                WORKSPACE_FLOOR + " integer," +
                WORKSPACE_MAP + " BLOB," +
                WORKSPACE_WIDTH + " integer," +
                WORKSPACE_HEIGHT + " integer)";
        String createNodeTableSQL = "create table if not exists Node(" +
                NODE_ID + " integer primary key autoincrement," +
                NODE_NAME + " text not null," +
                NODE_TYPE + " text not null," +
                NODE_RFID + " text not null unique," +
                NODE_POSITIONX + " integer not null," +
                NODE_POSITIONY + " integer not null," +
                NODE_WORKSPACE + " integer not null," +
                "Unique(" + NODE_WORKSPACE + "," + NODE_NAME + ")," + // 同一工作区内不能有相同名称的两个节点
                "Foreign key(" + NODE_WORKSPACE + ") References " +
                " workspace(" + WORKSPACE_ID + ") on delete cascade )";
        String createRouteTableSQL = "create table if not exists Route(" +
                ROUTE_ID + " integer not null primary key," +
                ROUTE_PREID + " integer not null," +
                ROUTE_NAME + " text ," +
                ROUTE_WORKSPACEID + " integer not null," +
                ROUTE_ENABLED + " boolean ," +
                "Unique(" + ROUTE_WORKSPACEID + "," + ROUTE_NAME + ")," + // 同一工作区内不能有相同名称的两个路线
                "Foreign key(" + ROUTE_WORKSPACEID + ") References " +
                " workspace(" + WORKSPACE_ID + ") on delete cascade )";
        String createPathTableSQL = "create table if not exists Path(" +
                PATH_NODEID + " integer not null," +
                PATH_ENDNODEID + " integer not null," +
                PATH_ROUTEID + " integer not null," +
                PATH_ORDERID + " integer not null," +
                PATH_DISTANCE + " integer," +
                PATH_YAW + " integer," +
                PATH_ANGLE + " integer," +
                PATH_MAXSPEED + " integer," +
                "primary key(" + PATH_ROUTEID + "," + PATH_ORDERID + ")," +
                //		"unique("+PATH_ROUTEID+","+PATH_ENDNODEID+"),"+ // 约束: 同一路线只能到达一次相同目的
                "unique(" + PATH_ROUTEID + "," + PATH_NODEID + "," + PATH_ENDNODEID + ")," + // 约束: (起始点,目的点)是唯一的
                "Foreign key(" + PATH_ROUTEID + ") References " +
                " route(" + ROUTE_ID + ") on delete cascade  ," +
                "Foreign key(" + PATH_NODEID + ") References " +
                " node(" + NODE_ID + ") on delete cascade  ," +
                "Foreign key(" + PATH_ENDNODEID + ") References " +
                " node(" + NODE_ID + ") on delete cascade)";

        try {
            db.execSQL(createWorkspaceTableSQL);
            db.execSQL(createRouteTableSQL);
            db.execSQL(createNodeTableSQL);
            db.execSQL(createPathTableSQL);
            L.i("| 数据库 |  初始化地图 Table : Node,Workspace,Path and Route");
        } catch (SQLiteException e) {
            L.e(LOG, e.toString());
        }
    }

    private void dropAllTable(SQLiteDatabase db) {
        db.execSQL("drop table if exists node");
        db.execSQL("drop table if exists path");
        db.execSQL("drop table if exists route");
        db.execSQL("drop table if exists workspace");
        L.i(LOG, "删除旧表");
    }

    //暂时不用( 因为使用FK 的on delete cascade 来实现了 级联删除)
    private void createTrigger(SQLiteDatabase db) {
        String updateRouteIdTrigger =
                "create trigger updateRouteIdTrigger " +
                        "after update of " + ROUTE_ID + " on Route " +
                        "begin " +
                        "update path " +
                        " set " + PATH_ROUTEID + "=new." + ROUTE_ID +
                        " where " + PATH_ROUTEID + "=old." + ROUTE_ID + ";" +   //注意这个 ;号
                        "end";
        try {
            db.execSQL(updateRouteIdTrigger);
        } catch (SQLiteException e) {
            L.e(e.toString());
        }

    }

    //endregion


    public SQLiteDatabase getDatabase() {
        return mDatabase;
    }

    public void closeDatabase() {
        mInstance.close();
        mInstance = null;
        mDatabase.close();
        mDatabase = null;
    }

    public void reopenDatabase() {
        mDatabase.close();
        mDatabase = getWritableDatabase();
    }

    //得到 执行 rawQuery后的 cursor,用于自定义sql手动查询
    public Cursor getCursor(String sql, String[] selectionArgs) {
        try {
            return mDatabase.rawQuery(sql, selectionArgs);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //region Node相关的数据库操作
    //region 增 / 删 / 改
    //向数据库添加节点
    public boolean addData(Node node) {
        try {
            if (nodeExist(node.getWorkspaceID(), node.getName(), node.getRFID())) {
                T.show("添加失败\n该节点已经存在");
                return false;
            }
            String sql = "insert into Node(" + NODE_TABLE_ROW + ") values(?,?,?,?,?,?,?)";
            mDatabase.execSQL(sql, new Object[]{
                    node.getId(), node.getName(), node.getType(), node.getRFID(),
                    node.getPositionX(), node.getPositionY(), node.getWorkspaceID()});
        } catch (Exception e) {
            L.e(LOG, e.toString());
            T.show("添加节点失败");
            return false;
        }
        return true;
    }

    public boolean delData(Node node) {
        String sql = "delete from node where " + NODE_ID + " =?";
        try {
            mDatabase.execSQL(sql, new String[]{String.valueOf(node.getId())});
        } catch (Exception e) {
            L.e(LOG, e.toString());
            T.show("删除失败 ");
            return false;
        }
        return true;
    }

    //更新节点( 不变的是NodeID)
    public boolean updateData(Node newData) {
        String sql = "update node set " + NODE_POSITIONX + "=?, " + NODE_POSITIONY + "=?, "
                + NODE_NAME + "=?," + NODE_RFID + "=?," + NODE_TYPE + "=? " +
                "where " + NODE_ID + " =?";
        try {
            mDatabase.execSQL(sql, new Object[]{
                    newData.getPositionX(), newData.getPositionY(),
                    newData.getName(), newData.getRFID(), newData.getType(),
                    newData.getId()});
        } catch (Exception e) {
            L.e(LOG, e.toString());
            T.show("修改失败,与现有节点冲突\n请检查名称和RFID是否唯一");
            return false;
        }
        return true;

    }
    //endregion

    //region 查询
    // 获取Node数据,使用前请先将cur移到目标row, 使用后请手动释放 cur
    private Node getNode(Cursor cur) {
        Node node = new Node();
        node.setId(cur.getInt(cur.getColumnIndex(NODE_ID)));
        node.setName(cur.getString(cur.getColumnIndex(NODE_NAME)));
        node.setType(cur.getString(cur.getColumnIndex(NODE_TYPE)));
        node.setRFID(cur.getString(cur.getColumnIndex(NODE_RFID)));
        node.setPositionX(cur.getInt(cur.getColumnIndex(NODE_POSITIONX)));
        node.setPositionY(cur.getInt(cur.getColumnIndex(NODE_POSITIONY)));
        node.setWorkspaceID(cur.getInt(cur.getColumnIndex(NODE_WORKSPACE)));
        return node;
    }

    //得到当前ID号最大的NODE,如果当前没有任何节点,返回null
    public Node getMaxIdNode() {
        String sql = "select * from node order by " + NODE_ID
                + " desc limit 1";
        Cursor cur = mDatabase.rawQuery(sql, null);
        if (cur.moveToNext()) {
            Node maxNode = getNode(cur);
            cur.close();
            return maxNode;
        } else {
            return null;
        }
    }

    //获取所有cur查询到的node数据,cur将自动释放
    private List<Node> getNodeList(Cursor cur) {
        List<Node> nodeList = new ArrayList<>();
        while (cur.moveToNext()) {
            nodeList.add(getNode(cur));
        }
        cur.close();
        return nodeList;
    }

    // 获取清洗区节点 (以后可能有多个清洗区)
    public List<Node> getWashNode() {
        String sql = "select * from Node where " + NODE_TYPE + "=?";
        Cursor cur = mDatabase.rawQuery(sql, new String[]{I_Parameters.NODE_TYPE.WASH});
        if (cur.getColumnCount() <= 0) {
            return null;
        }
        return getNodeList(cur);
    }

    //得到 工作区内的所有节点
    public List<Node> getAllNode(int wspID) {
        Cursor cur = mDatabase.rawQuery("select * from node where " + WORKSPACE_ID
                + "=? ", new String[]{String.valueOf(wspID)});
        return getNodeList(cur);
    }

    // 得到所有可显示节点 (非交叉点)
    public List<Node> getVisibleNode(int wspID) {
        Cursor cur = mDatabase.rawQuery(
                "select * from node  where " + WORKSPACE_ID + "=? and " + NODE_TYPE + "!=?",
                new String[]{String.valueOf(wspID), I_Parameters.NODE_TYPE.CROSS});
        return getNodeList(cur);
    }

    /**
     * 得到 工作区 内 所有可以到达的节点 ( 存在 可用路线 -> 所有 路径 -> 终点 )
     *
     * @param wspID     工作区ID
     * @param nodeTypes 需要显示的类型,{@link com.feng.Constant.I_Parameters.NODE_TYPE} 中的String,
     * @param orderType 排序方式, {@link com.feng.Constant.I_MapData} 中节点表的类型
     */
    public List<Node> getReachableNode(int wspID, String[] nodeTypes, String orderType) {
        if (nodeTypes == null) {
            return null;
        }
        // in(?,?) 中的参数 , nodeType有n个 ,就有n个?
        StringBuilder stringBuffer = new StringBuilder();
        for (String type : nodeTypes) {
            stringBuffer.append('?').append(',');// 将删除条件添加到StringBuffer对象中
        }
        //删掉最后一个  ,
        stringBuffer.deleteCharAt(stringBuffer.length() - 1);

        // SQL join 操作
        String sql = "select Node." + NODE_ID + " from Node,Path,Route where "
                + "Route." + ROUTE_WORKSPACEID + "=" + wspID + " and " + // 匹配路线 wspID
                "Route." + ROUTE_ENABLED + "=1 and " + // 匹配 路线 enable ==1
                "Path." + PATH_ROUTEID + "=Route." + ROUTE_ID + " and " + // 匹配 路径.routeID==Route.ID
                "Node." + NODE_ID + "=Path." + PATH_ENDNODEID + " and " +// 匹配 NODE ==PATH.END_NODE
                "Node." + NODE_TYPE + " in( " + stringBuffer + " ) " + // (只显示 传入的节点类型)
                " group by Node.ID " + // 合并 相同ID ( 某个节点的 入度可能> 1)
                " order by Node." + orderType; // 排序
        Cursor cur = mDatabase.rawQuery(sql, nodeTypes);

        List<Node> nodeList = new ArrayList<>();
        while (cur.moveToNext()) {
            // 只有一列,获取第一列 : 节点ID
            int id = cur.getInt(0);
            Node node = getNodeByID(id);
            nodeList.add(node);
        }

        return nodeList;
    }

    //返回所查询ID的NODE,如果未找到返回NULL
    public Node getNodeByID(int id) {
        Cursor cur = mDatabase.rawQuery("select * from node where " + NODE_ID + "=?",
                new String[]{String.valueOf(id)});
        if (cur.moveToNext()) {
            Node target = getNode(cur);
            cur.close();
            return target;
        } else {
            cur.close();
            return null;
        }
    }

    public Node getNodeByName(int workID, String nodeName) {
        Cursor cur = mDatabase.rawQuery("select * from node where " + NODE_WORKSPACE + "=? and " + NODE_NAME + "=?",
                new String[]{String.valueOf(workID), nodeName});
        if (cur.moveToNext()) {
            Node target = getNode(cur);
            cur.close();
            return target;
        } else {
            cur.close();
            return null;
        }
    }

    //判断是否已存在 ( 相同名称 && 相同工作区) 或者 ( 相同RFID ) 的节点 --允许不同工作区存在相同名称的节点
    public boolean nodeExist(int workspaceID, String nodeName, String RFID) {
        String sql = "select * from Node where " +
                "(" + NODE_WORKSPACE + "=? and " + NODE_NAME + "=? )" +
                "or (" + NODE_RFID + "=?)";
        Cursor cur = mDatabase.rawQuery(sql,
                new String[]{String.valueOf(workspaceID), nodeName, RFID});
        // 如果没有结果 ( 查询结果是empty),则返回 false
        boolean hasExist = cur.moveToFirst();
        cur.close();
        return hasExist;
    }

    // 获取目标工作区中的节点数量
    public int getWorkspaceNodeCount(int workSpaceID) {
        String sql = "select count(*) from node where " + NODE_WORKSPACE + "=?";
        Cursor cursor = mDatabase.rawQuery(sql, new String[]{String.valueOf(workSpaceID)});
        if (cursor.moveToNext()) {
            int nodeCount = cursor.getInt(0);
            cursor.close();
            return nodeCount;
        } else {
            return 0;
        }
    }

    //得到当前路线的最后一个节点 (最后一条路径的终点)
    public Node getLastNodeByRouteID(int routeID) {
        String sql = "select * from node where " + NODE_ID + "=" +
                "(select " + PATH_ENDNODEID + " from path where " + PATH_ROUTEID + "=? order by " + PATH_ORDERID + " desc limit 1)";
        Cursor cur = mDatabase.rawQuery(sql, new String[]{String.valueOf(routeID)});
        if (cur.moveToNext()) {
            Node node = getNode(cur);
            cur.close();
            return node;
        } else {
            cur.close();
            return null;
        }
    }

    /**
     * 得到 到达 指定节点 所经过的 路径
     *
     * @param preNodes //所经过的节点( 递归调用前 已经获取到的节点)
     * @return
     */
    public List<Node> getPreNodes(int routeID, int targetNodeID, List<Node> preNodes) {
        List<Node> passNodes = new ArrayList<>();
        String sql =
                "select " + PATH_NODEID + " from PATH where " + PATH_ROUTEID + "=? and " + PATH_ORDERID + "<= " +
                        "(select " + PATH_ORDERID + " from PATH where " + PATH_ENDNODEID + "=? and " +
                        PATH_ROUTEID + "= ? ) " +
                        "order by " + PATH_ORDERID;
        // 得到了 当前路线上 到达targetNode 所要经过的节点 ( 按照order排序) ,并且包括 targetNode
        Cursor cur = mDatabase.rawQuery(sql, new String[]{String.valueOf(routeID), String.valueOf(targetNodeID), String.valueOf(routeID)});
        // 如果 路线上 未找到目标点 ,返回NULL
        if (cur.getCount() <= 0) {
            L.e(LOG, "编号为 " + routeID + " 的路线上 未找到 ID为" + targetNodeID + " 的节点");
            cur.close();
            return null;
        }
        while (cur.moveToNext()) {
            Node preNode = getNodeByID(cur.getInt(cur.getColumnIndex(PATH_NODEID)));
            passNodes.add(preNode);
        }
        cur.close();
        //存入 已获取的节点-preNodes
        if (preNodes != null) {
            passNodes.addAll(preNodes);
        } else {
            // 添加 最后一个节点
            passNodes.add(getNodeByID(targetNodeID));
        }
        // 判断是否已经没有前级路线
        int preRouteID = getRouteByID(routeID).getPreID();
        if (preRouteID != 0) {
            // 新路线中的targetNode
            //			L.t(this, "遍历一次: 路线:"+routeID+",目的节点:"+targetNodeID+
            //					"已经获取节点数"+( passNodes!=null?passNodes.size():0) );
            targetNodeID = passNodes.get(0).getId();
            // 继续遍历
            return getPreNodes(preRouteID, targetNodeID, passNodes);
        } else {
            //			L.t(this,"遍历完成: 获取的节点数为"+passNodes.size());
//			for( Node passNode: passNodes){
//				L.w(passNode.getName()	);
//			}
            //遍历完成 ,返回NODES
            return passNodes;
        }
    }

    /**
     * 得到 路线上 beginNode后 -> 回到 起点 所经过的节点
     * 如果还未形成环线, 就返回NULL
     *
     * @param routeID
     * @param beginNode
     * @return
     */
    public List<Node> getAfterNodes(int routeID, int beginNode, List<Node> afterNodes) {
        /**
         * 获取当前路线/指定节点的后续节点
         */
        List<Node> passNodes = new ArrayList<>();
        Route route = getRouteByID(routeID);
        String sql =
                "select " + PATH_ENDNODEID + " from PATH where " + PATH_ROUTEID + "=? and " + PATH_ORDERID + ">" +
                        "(select " + PATH_ORDERID + " from PATH where " + PATH_ENDNODEID + "=? and " +
                        PATH_ROUTEID + "= ? )" +
                        "order by " + PATH_ORDERID;
        Cursor cur = mDatabase.rawQuery(sql, new String[]{String.valueOf(route.getId()),
                String.valueOf(beginNode), String.valueOf(route.getId())});
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                Node afterNode = getNodeByID(cur.getInt(cur.getColumnIndex(PATH_ENDNODEID)));
                passNodes.add(afterNode);
                //如果 beginNode不是当前路线的最后节点, 设置beginNode为最后节点
            }
            beginNode = passNodes.get(passNodes.size() - 1).getId();
        } else {
            //如果beginNode是最后节点..,再去前级路线中查找beginNode( 相同的)
            //beginNode=beginNode;
        }
        cur.close();
        /**
         * 判断是否需要递归
         */
        // 加上之前的遍历结果,从前往后 ( 逆序,将之前的结果添加到前面)
        if (afterNodes != null) {
            passNodes.addAll(0, afterNodes);
        }
        //如果前级路线为0 ,表示 不再需要递归
        if (route.getPreID() == 0) {
            // 如果 之前获取的 节点不为空,添加到结果中
//			for( Node passNode: passNodes){
//				L.e(passNode.getName()	);
//			}
            //遍历完成 ,返回NODES
            return passNodes;
        } else {
            // 获取最后一个 NODE
            return getAfterNodes(route.getPreID(), beginNode, passNodes);
        }

    }

    // 得到该路线经过的节点(数据库中存在的, 不包括前级路线的)
    public List<Node> getNodeByRoute(int routeID) {
        String sql = "select * from node where " + NODE_ID + " in" +
                "(select " + PATH_NODEID + " from path where " + PATH_ROUTEID + "=?)";
        Cursor cursor = mDatabase.rawQuery(sql, new String[]{String.valueOf(routeID)});
        return getNodeList(cursor);
    }
    //endregion
    //endregion

    //region Path相关的数据库操作
    //region 增/ 删/ 改

    public boolean addData(Path path) {
        String sql = "insert into Path(" + PATH_TABLE_ROW + ") values(?,?,?,?,?,?,?,?)";
        try {
            mDatabase.execSQL(sql, new Object[]{
                    path.getNodeID(), path.getEndNode(), path.getRouteID(), path.getOrderID(),
                    path.getYaw(), path.getDistance(), path.getAngle(), path.getMaxSpeed()});
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            T.show("添加失败,已存在该路径");
            return false;
        }
    }

    // 更新path, 不变的是 routeID和orderID
    public boolean updateData(Path path) {
        String sql = "update path set " +
                PATH_NODEID + "=?," +
                PATH_ENDNODEID + "=?," +
                PATH_YAW + "=? ," +
                PATH_DISTANCE + "=? ," +
                PATH_ANGLE + "=? ," +
                PATH_MAXSPEED + " =? where " + PATH_ROUTEID + " =? and " + PATH_ORDERID + " =?";
        try {
            mDatabase.execSQL(sql, new Object[]{
                    path.getNodeID(), path.getEndNode(), path.getYaw(), path.getDistance(),
                    path.getAngle(), path.getMaxSpeed(), path.getRouteID(), path.getOrderID()});
        } catch (Exception e) {
            L.e(e.toString());
            T.show("修改路径失败");
            return false;
        }
        return true;
    }

    //删除 指定路线 指定Order(包含)之后的 所有路径 (无法实现通过级联删除实现,需要判断OrderID)
    public boolean delData(Path path) {
        //如果该path对象没有order,查找并设置
        if (path.getOrderID() == 0) {
            path.setOrderID(getPathOrder(path.getRouteID(), path.getNodeID(), path.getEndNode()));
        }
        String sql = "delete from path where " + PATH_ROUTEID + " =? and " + PATH_ORDERID + " >= ?";
        try {
            mDatabase.execSQL(sql, new Object[]{path.getRouteID(), path.getOrderID()});
        } catch (Exception e) {
            L.e(LOG, e.toString());
            T.show("删除路径失败 ");
            return false;
        }
        return true;
    }

    public int getPathOrder(int routeID, int startNodeID, int endNodeID) {
        String sql = "select " + PATH_ORDERID + " from path where " +
                PATH_ROUTEID + "=? and " + PATH_NODEID + "=? and " + PATH_ENDNODEID + "=?";
        Cursor cursor = mDatabase.rawQuery(sql, new String[]{
                String.valueOf(routeID), String.valueOf(startNodeID), String.valueOf(endNodeID)});
        if (cursor.moveToNext()) {
            int order = cursor.getInt(0);
            cursor.close();
            return order;
        } else {
            return 0;
        }
    }

    //endregion

    //region 查询

    //请先将cur指向需要查询的row
    public Path getPath(Cursor cur) {
        Path path = new Path();
        path.setRouteID(cur.getInt(cur.getColumnIndex(PATH_ROUTEID)));
        path.setOrderID(cur.getInt(cur.getColumnIndex(PATH_ORDERID)));
        path.setNodeID(cur.getInt(cur.getColumnIndex(PATH_NODEID)));
        path.setEndNode(cur.getInt(cur.getColumnIndex(PATH_ENDNODEID)));
        path.setDistance(cur.getInt(cur.getColumnIndex(PATH_DISTANCE)));
        path.setYaw(cur.getInt(cur.getColumnIndex(PATH_YAW)));
        path.setAngle(cur.getInt(cur.getColumnIndex(PATH_ANGLE)));
        path.setMaxSpeed(cur.getInt(cur.getColumnIndex(PATH_MAXSPEED)));
        return path;
    }

    public List<Path> getPathList(Cursor cur) {
        List<Path> list = new ArrayList<>();
        while (cur.moveToNext()) {
            list.add(getPath(cur));
        }
        cur.close();
        return list;
    }

    //根据 路线ID 和起始点ID,以及终点ID ,获取PATH ( 如果只有路线ID和起点ID, 在单行道将会得到2个PATH)
    public Path getPathByKey(int routeID, int startNodeID, int endNodeID) {
        String sql = "select * from path where " +
                PATH_ROUTEID + "=? and " + PATH_NODEID + " =? and " + PATH_ENDNODEID + " =?";
        Cursor cursor = mDatabase.rawQuery(sql, new String[]{
                String.valueOf(routeID), String.valueOf(startNodeID), String.valueOf(endNodeID)});
        if (cursor.moveToNext()) {
            Path path = getPath(cursor);
            cursor.close();
            return path;
        } else {
            return null;
        }
    }

    //查看 是否已经存在 路径
    public boolean pathExist(int routeID, int startNodeID, int endNodeID) {
        return getPathByKey(routeID, startNodeID, endNodeID) != null;
    }

    /**
     * 添加路径时的验证
     * 1. 起点是否正确
     * 2. 是否已经形成环线 ( 如果为true, 无法继续添加
     * X. 终点是否正确(改为在数据库中使用 unique( routeID,endNodeID) )来约束
     */
    public boolean verifyBeforeAddPath(Path path) {
        boolean isCompletedRoute = this.isCompleted(getRouteByID(path.getRouteID()));
        /**
         * 判断 Path 的起点是否正确( 该路径是否 和上一条PATH连接)
         * 0 , 如果是第一条路线 的第一条路径, 返回TRUE
         * 1.如果是 路线的第一条路径 ,起点是否在 前级路线上
         * 2. 如果不是 路线的第一条路径,起点是否在 上一个终点上
         */
        boolean isCorrectStart;
        int routeID = path.getRouteID();
        int orderID = path.getOrderID();
        int startNodeID = path.getNodeID();
        int preRouteID = getRouteByID(routeID).getPreID();
        // 条件 0:
        if (preRouteID == 0 && orderID == 1) {
            return !isCompletedRoute;
        }
        String sql = "select * from path where " + PATH_ENDNODEID + "=? and " + PATH_ROUTEID + "=? ";
        Cursor cursor = null;
        if (orderID == 1) {
            // 条件1 : .如果是 路线的第一条路径 ,[前级]路线是否能到达 该 PATH的起点
            cursor = mDatabase.rawQuery(sql, new String[]{String.valueOf(startNodeID), String.valueOf(preRouteID)});
        } else {
            // 条件2 : .如果不是 路线的第一条路径 , [当前]路线是否能到达 该 PATH的起点
            cursor = mDatabase.rawQuery(sql, new String[]{String.valueOf(startNodeID), String.valueOf(routeID)});
        }
        // 如果能 moveToNext() ,表示能到达该path的起点,返回true
        if (cursor.moveToNext()) {
            isCorrectStart = true;
        } else {
            T.show("错误的起始节点");
            isCorrectStart = false;
        }
        cursor.close();
        return isCorrectStart && !isCompletedRoute;
    }

    //查找所有以该节点为起点的路径..
    public List<Path> getPathByStartNode(int nodeID) {
        String sql = "select * from path where " + PATH_NODEID + "=?";
        return getPathList(mDatabase.rawQuery(sql, new String[]{String.valueOf(nodeID)}));
    }

    // 得到指定路线ID的整条Path ( 如果不是首条路线,补全)
    public List<Path> getWholeRoute(int routeID) {
        return getWholeRoute(routeID, null);
    }

    /**
     * @param routeID
     * @param lastPath 已经获取到的PATH (用来递归)
     * @return
     */
    public List<Path> getWholeRoute(int routeID, List<Path> lastPath) {
        Route route = getRouteByID(routeID);
        if (route == null) {
            return null;
        }
        //先获取 该路线的所有PATH
        String sql = "select * from path where " + PATH_ROUTEID + "=? order by " + PATH_ORDERID;
        Cursor cur = mDatabase.rawQuery(sql, new String[]{String.valueOf(routeID)});
        List<Path> currentPath = getPathList(cur);

        // 获取截断点ID
        if (lastPath != null && lastPath.size() != 0) {
            int breakNodeID, mergeNodeID;
            breakNodeID = lastPath.get(0).getNodeID();
            mergeNodeID = lastPath.get(lastPath.size() - 1).getEndNode();

            // 截断当前的path( 与LastPath合并)
            List<Path> prePaths = null;
            List<Path> afterPaths = null;
            for (int i = 0; i < currentPath.size(); i++) {
                if (currentPath.get(i).getEndNode() == breakNodeID) {
                    // subList 获取 index为[0,i+1)的值, 不包含第i+1个元素
                    // 并且获取的subList是 list的一个映射,如果subList改变,list也将改变.反之亦然
                    prePaths = currentPath.subList(0, i + 1);
                }
                if (currentPath.get(i).getNodeID() == mergeNodeID) {
                    afterPaths = currentPath.subList(i, currentPath.size());
                }
            }
            if (prePaths != null) {
                lastPath.addAll(0, prePaths);
            }
            if (afterPaths != null) {
                lastPath.addAll(afterPaths);
            }
        } else {//如果不需要截断,准备返回值
            lastPath = currentPath;
        }

        if (route.getPreID() != 0) {
            return getWholeRoute(route.getPreID(), lastPath);
        } else {
            return lastPath;
        }

    }


//endregion
//endregion

    //region Route相关的数据库操作
    //region 增/删/改
    // 添加路线

    public boolean addData(Route route) {
        if (!verifyPreRoute(route)) {
            return false;
        }
        try {
            mDatabase.execSQL("insert into Route(" + ROUTE_TABLE_ROW + ") values(?,?,?,?,?)",
                    new Object[]{route.getId(), route.getPreID(),
                            route.getName(), route.getWorkspaceID(), route.isEnabled()});
        } catch (Exception e) {
            L.e(LOG, "添加路线失败");
            T.show("添加失败,路线已存在,请检查名称");
            return false;
        }
        return true;
    }

    // 删除路线 (同时删除后续路线)
    public boolean delData(Route route) {
        String sqlString = "delete from route where " + ROUTE_ID + "=?";
        try {
            mDatabase.execSQL(sqlString, new String[]{String.valueOf(route.getId())});
            //递归操作
            String sqlAfterRoute = "select * from route where " + ROUTE_PREID + "=?";
            List<Route> afterRoutes = getRouteList(mDatabase.rawQuery(sqlAfterRoute, new String[]{String.valueOf(route.getId())}));
            for (Route after : afterRoutes) {
                delData(after);
            }
        } catch (Exception e) {
            T.show("删除失败");
            L.e(LOG, e.toString());
            return false;
        }

        return true;
    }

    // 更新路线信息
    public boolean updateData(Route newRoute) {
        if (!verifyPreRoute(newRoute)) {
            return false;
        }
        String sql = "update route set " + ROUTE_ID + "=?," + ROUTE_PREID + "=?," + ROUTE_NAME + "=? ,"
                + ROUTE_ENABLED + "=? where " + ROUTE_ID + "=?";
        try {
            mDatabase.execSQL(sql, new Object[]{
                    String.valueOf(newRoute.getId()), String.valueOf(newRoute.getPreID()), String.valueOf(newRoute.getName()),
                    newRoute.isEnabled() ? 1 : 0, String.valueOf(newRoute.getId())});
            L.i(LOG, "路线更新成功:" + newRoute.toString());
            if (!newRoute.isEnabled()) {
                // 如果 修改为 不可用, 递归弃用 后续ID
                deprecated(newRoute.getId());
            }
        } catch (Exception e) {
            T.show("更新失败");
            L.e(LOG, e.toString());
            return false;
        }
        return true;
    }

    //弃用 当前路线, 并且将 所有前级路线为 当前路线的 弃用( 使用递归)
    public void deprecated(int deprecatedRouteID) {
        // 将 指定 编号的路线 弃用
        String sqlUpdate = "update route set " + ROUTE_ENABLED + "=0 where " + ROUTE_ID + "=?";
        mDatabase.execSQL(sqlUpdate, new String[]{String.valueOf(deprecatedRouteID)});
        L.e("弃用了 路线: # " + deprecatedRouteID);
        // 得到 前级路线为 弃用ID 的所有路线的编号 ( 可能有多个)
        String sqlGetAfterRoute = "select " + ROUTE_ID + " from route where " + ROUTE_PREID + " = ?";
        Cursor cur = mDatabase.rawQuery(sqlGetAfterRoute, new String[]{String.valueOf(deprecatedRouteID)});
        while (cur.moveToNext()) {
            int id = cur.getInt(cur.getColumnIndex(ROUTE_ID));
            // 弃用掉该 ID 的路线
            deprecated(id);
        }
        cur.close();
    }

    //endregion

    //region 查询
//根据查询结果生成Route,请手动将cur移动到要查询的row
    public Route getRoute(Cursor cur) {
        Route route = new Route();
        route.setId(cur.getInt(cur.getColumnIndex(ROUTE_ID)));
        route.setName(cur.getString(cur.getColumnIndex(ROUTE_NAME)));
        route.setPreID(cur.getInt(cur.getColumnIndex(ROUTE_PREID)));
        route.setWorkspaceID(cur.getInt(cur.getColumnIndex(ROUTE_WORKSPACEID)));
        //return  1 : true   ,  0  : false
        route.setEnabled(cur.getInt(cur.getColumnIndex(ROUTE_ENABLED)) == 1);
        return route;
    }

    //获取所有cur查询到的 Route 数据,cur将自动释放
    public List<Route> getRouteList(Cursor cur) {
        List<Route> routeList = new ArrayList<>();
        while (cur.moveToNext()) {
            routeList.add(getRoute(cur));
        }
        cur.close();
        return routeList;
    }

    // 获取所有Route
    public List<Route> getAllRoute(int wspID) {
        Cursor cur = mDatabase.rawQuery("select * from route where " + WORKSPACE_ID + "=? order by " + ROUTE_ID,
                new String[]{String.valueOf(wspID)});
        return getRouteList(cur);
    }

    //得到所有的前级路线, 按照从后续往前级顺序排列
    public List<Route> getPreRoutes(Route route) {
        //TODO
        return null;
    }

    // 得到该路线可设置的前级路线
    // 前级路线可选项中排除自身,以及前级路线为自身的其他路线(比如: 本路线为2,若3的前级路线为2,本路线不能再设置前级为3)
    public List<Route> getLegalPreRoute(Route route) {
        String sql = "select * from route where " + ROUTE_WORKSPACEID + "=? and " + ROUTE_ID + "!=? and " + ROUTE_PREID + "!=?";
        Cursor cursor = mDatabase.rawQuery(sql, new String[]{
                String.valueOf(route.getWorkspaceID()),
                String.valueOf(route.getId()),
                String.valueOf(route.getId())});
        return getRouteList(cursor);
    }

    // 得到未弃用的路线
    public List<Route> getUsableRoute(int wspID) {
        Cursor cur = mDatabase.rawQuery("select * from route where " + WORKSPACE_ID + "=? and " +
                ROUTE_ENABLED + "=1 order by " + ROUTE_PREID, new String[]{String.valueOf(wspID)});
        return getRouteList(cur);
    }

    // 得到目标路线中的最大路径顺序
    public int getMaxPathOrder(int routeID) {
        int order = 0;
        try {
            Cursor cur = mDatabase.rawQuery("select orderID from Path where routeID=? order by orderID desc limit 1",
                    new String[]{String.valueOf(routeID)});
            // 返回 获取到的行数 ,如果该路线还没有路径,则返回 行数0
            if (cur.moveToNext()) {
                order = cur.getInt(cur.getColumnIndex(PATH_ORDERID));
            }
            cur.close();
        } catch (Exception e) {
            L.e(LOG, e.toString());
        }
        return order;
    }

    // 得到当前最大的路线ID ( 如果没有路线,返回0)
    public int getMaxRouteID() {
        int maxRouteID = 0;
        try {
            Cursor cur = mDatabase.rawQuery("select " + ROUTE_ID + " from route order by +" + ROUTE_ID + " desc limit 1", null);
            if (cur.moveToNext()) {
                maxRouteID = cur.getInt(cur.getColumnIndex(ROUTE_ID));
            }
            cur.close();
        } catch (Exception e) {
            L.e(e.toString());
        }
        return maxRouteID;
    }

    //判断 当前工作区内是否有前级路线为0的路线
    public boolean baseRouteExist(int workspace) {
        String sql = "select * from route where " + ROUTE_WORKSPACEID + "=? and " + ROUTE_PREID + "=?";
        Cursor cursor = mDatabase.rawQuery(sql, new String[]{String.valueOf(workspace), String.valueOf(0)});
        if (cursor.moveToNext()) {
            cursor.close();
            return true;
        } else {
            return false;
        }
    }

    // 根据ID 查询Route
    public Route getRouteByID(int routeID) {
        String sql = "select * from route where " + ROUTE_ID + "=?";
        Cursor cur = mDatabase.rawQuery(sql, new String[]{String.valueOf(routeID)});
        if (cur.moveToNext()) {
            Route target = getRoute(cur);
            cur.close();
            return target;
        } else {
            return null;
        }
    }

    // 根据 工作区和路线名称 获取Route
    public Route getRouteByName(int workspaceID, String routeName) {
        String sql = "select * from route where " + ROUTE_WORKSPACEID + "=? and " + ROUTE_NAME + "=?";
        Cursor cur = mDatabase.rawQuery(sql, new String[]{String.valueOf(workspaceID), routeName});
        cur.moveToNext();
        Route route = getRoute(cur);
        cur.close();
        return route;
    }

    /**
     * 验证 当前 路线的preRoute是否合法
     * 1. 前级是0 : 当前工作区没有 PreID 为0 路线 (除去自己)? true: false;
     * 2.前级不是0:
     * a: 前级路线不能是自身
     * b: 当前工作区 有 路线 ID ==该值(除去自己)? true: false;
     */
    private boolean verifyPreRoute(Route route) {
        String sqlSearchID = "select * from route where " + ROUTE_WORKSPACEID + "=? and " + ROUTE_ID + "=?";
        String sqlSearchPreID = "select * from route where " +
                ROUTE_WORKSPACEID + "=? and " + ROUTE_PREID + "=? and " + ROUTE_ID + "!=?";
        Cursor cur;
        if (route.getPreID() == 0) {
            cur = mDatabase.rawQuery(sqlSearchPreID,
                    new String[]{String.valueOf(route.getWorkspaceID()),
                            String.valueOf(route.getPreID()), String.valueOf(route.getId())});
            if (!cur.moveToNext()) {
                cur.close();
                return true;
            } else {
                cur.close();
                T.show("操作失败,当前工作区已有前级路线为0的路线");
                return false;
            }
        } else {
            //前级路线不能是本身
            if (route.getId() == route.getPreID()) {
                T.show("操作失败,路线的前级路线必须是其他路线");
                return false;
            }
            cur = mDatabase.rawQuery(sqlSearchID,
                    new String[]{String.valueOf(route.getWorkspaceID()), String.valueOf(route.getPreID())});
            if (!cur.moveToNext()) {
                T.show("操作失败,当前工作区未找到编号为 " + route.getPreID() + " 的前级路线");
                cur.close();
                return false;
            } else {
                cur.close();
                return true;
            }
        }
    }

    /**
     * 验证指定路线是否已经形成环线
     *
     * @return
     */
    public boolean isCompleted(Route route) {
        // 获取 path的RouteID, -> PreRouteID
        //匹配 PreRoute中是否有 终点 与 当前路线相同的,如果有 ,表示 已经完成后续路线
        // 如果前级路线是 0 ,判断该路线起点和终点是否 有一样的
        String firstID = "select " + PATH_NODEID + " from path where " + PATH_ROUTEID + "=?"
                + "order by " + PATH_ORDERID + " limit 1";
        String lastID = "select " + PATH_ENDNODEID + " from path where " + PATH_ROUTEID + "=?"
                + "order by " + PATH_ORDERID + " desc limit 1";
        Cursor first = mDatabase.rawQuery(firstID, new String[]{String.valueOf(route.getId())});
        Cursor last = mDatabase.rawQuery(lastID, new String[]{String.valueOf(route.getId())});
        int startID = -1, endID;
        if (!first.moveToNext() || !last.moveToNext()) {
            first.close();
            last.close();
            return false;
        } else {
            startID = first.getInt(first.getColumnIndex(PATH_NODEID));
            endID = last.getInt(last.getColumnIndex(PATH_ENDNODEID));
        }
        if (route.getPreID() == 0) {
            first.close();
            last.close();
            return startID == endID && startID != -1;
        } else {
            Route preRoute = getRouteByID(route.getPreID());
            String sql = "select * from Path where " + PATH_NODEID + "=? and "
                    + PATH_ROUTEID + "=? "; // 在路线ID为 PREID 的PATH中匹配 终点
            Cursor cur = mDatabase.rawQuery(sql, new String[]{String.valueOf(endID),
                    String.valueOf(preRoute.getId())});
            if (cur.moveToNext()) {
                T.show("当前路线已经形成环线");
                first.close();
                last.close();
                return true;
            } else {
                first.close();
                last.close();
                return false;
            }
        }
    }

    //判断该路线是否已经确定了起点
    public boolean isStart(Route route) {
        String sql = "select * from path where " + PATH_ROUTEID + "=?";
        Cursor cursor = mDatabase.rawQuery(sql, new String[]{String.valueOf(route.getId())});
        if (cursor.moveToNext()) {
            cursor.close();
            return true;
        } else {
            cursor.close();
            return false;
        }
    }

//endregion
    //endregion

    //region Workspace相关的数据库操作

    //region 增删改
    public boolean addData(Workspace ws) {
        String sql = "insert into Workspace(" + WORKSPACE_TABLE_ROW + ") values(?,?,?,?,?,?)";
        try {
            Point size = WindowUtil.getScreenSize();
            mDatabase.execSQL(sql, new Object[]{ws.getId(), ws.getName(), ws.getFloor(), ws.getMapPic(), size.x, size.y});
        } catch (Exception e) {
            L.e(e.toString());
            T.show("添加失败,与现有工作区冲突");
            return false;
        }
        return true;
    }

    //更新工作区信息
    public boolean updateData(Workspace ws) {
        String sql = "update workspace set " + WORKSPACE_NAME + " =?, " + WORKSPACE_FLOOR + "=? ," +
                WORKSPACE_MAP + "=?  where " + WORKSPACE_ID + " =?";
        try {
            // 将bitmap转换成 byte[]
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ws.getMapPic().compress(Bitmap.CompressFormat.PNG, 80, os);
            byte[] bitmapByte = os.toByteArray();
            mDatabase.execSQL(sql, new Object[]{ws.getName(), ws.getFloor(), bitmapByte, ws.getId()});
        } catch (Exception e) {
            L.e(LOG, e.toString());
            T.show("修改失败,该编号已存在");
            return false;
        }
        return true;
    }

    //删除工作区信息
    public boolean delData(Workspace ws) {
        String sql = "delete from Workspace where " + WORKSPACE_ID + "=?";
        try {
            mDatabase.execSQL(sql, new Object[]{ws.getId()});
        } catch (Exception e) {
            L.e(LOG, e.toString());
            T.show("删除失败");
            return false;
        }
        return true;
    }

    //设置工作区背景图片,保存到数据库中
    public void setWorkspaceBackground(int workspaceID, Bitmap map) {
        ContentValues values = new ContentValues();
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        map.compress(Bitmap.CompressFormat.PNG, 100, os);
        values.put(WORKSPACE_MAP, os.toByteArray());
        try {
            mDatabase.update("workspace", values, WORKSPACE_ID + "=?", new String[]{String.valueOf(workspaceID)});
        } catch (Exception e) {
            L.e(e.toString());
        }
    }
//endregion

    //region 查询
// 获取工作区,将先将 cur指向需要 获取的row
    public Workspace getWorkspace(Cursor cur) {
        Workspace ws = new Workspace();
        ws.setId(cur.getInt(cur.getColumnIndex(WORKSPACE_ID)));
        ws.setFloor(cur.getInt(cur.getColumnIndex(WORKSPACE_FLOOR)));
        ws.setName(cur.getString(cur.getColumnIndex(WORKSPACE_NAME)));
        ws.setWidth(cur.getInt(cur.getColumnIndex(WORKSPACE_WIDTH)));
        ws.setHeight(cur.getInt(cur.getColumnIndex(WORKSPACE_HEIGHT)));
        byte[] b = cur.getBlob(cur.getColumnIndex(WORKSPACE_MAP));
        if (b != null) {
            ws.setMapPic(BitmapFactory.decodeByteArray(b, 0, b.length));
        }
        return ws;
    }

    public List<Workspace> getWorkspaceList(Cursor cur) {
        List<Workspace> dataList = new ArrayList<>();
        while (cur.moveToNext()) {
            dataList.add(getWorkspace(cur));
        }
        cur.close();
        return dataList;
    }

    public List<Workspace> getAllWorkspace() {
        Cursor cur = mDatabase.rawQuery("select * from workspace order by workspaceID", null);
        return getWorkspaceList(cur);
    }

    public Workspace getWorkspaceByID(int i) {
        String sql = "select * from workspace where " + WORKSPACE_ID + "=? ";
        Cursor cur = mDatabase.rawQuery(sql, new String[]{String.valueOf(i)});
        if (cur.moveToNext()) {
            Workspace result = getWorkspace(cur);
            cur.close();
            return result;
        } else {
            return null;
        }
    }

    public Workspace getWorkspaceByPath(Path path) {
        String sql = "select * from workspace where " + WORKSPACE_ID + "=" +
                "(select " + ROUTE_WORKSPACEID + " from route where " + ROUTE_ID + "=?)";
        Cursor cursor = mDatabase.rawQuery(sql, new String[]{String.valueOf(path.getRouteID())});
        if (cursor.moveToNext()) {
            Workspace ws = getWorkspace(cursor);
            cursor.close();
            return ws;
        } else {
            cursor.close();
            return null;
        }
    }

    public int getMaxWorkspaceID() {
        int currentId = 0;
        try {
            Cursor cur = mDatabase.rawQuery("select " + WORKSPACE_ID + " from workspace order by +" + WORKSPACE_ID + " desc limit 1", null);
            if (cur.moveToNext()) {
                currentId = cur.getInt(cur.getColumnIndex(WORKSPACE_ID));
            }
            cur.close();
        } catch (Exception e) {
            L.e(e.toString());
        }
        return currentId;
    }
//endregion

//endregion

    //region 其他方法

    /**
     * 获取 到达 该节点的路线 对象
     */
    public Route getRouteByNodeId(int nodeID) {
        String sql = " select " + PATH_ROUTEID + " from path where " + PATH_ENDNODEID + "= ? order by " + PATH_ROUTEID + " limit 1";
        try {
            Cursor cur = mDatabase.rawQuery(sql, new String[]{String.valueOf(nodeID)});
            if (cur.moveToNext()) {
                Route route = getRouteByID(cur.getInt(cur.getColumnIndex(PATH_ROUTEID)));
                cur.close();
                return route;
            } else {
                cur.close();
                T.show("添加节点到任务列表失败,没有路线到达该节点,请前往地图编辑");
                return null;
            }
        } catch (Exception e) {
            L.e(e.toString());
            return null;
        }
    }

    /**
     * 根据 地图上的路径信息获取 所有相关的node..避免多次查询
     *
     * @param pathList
     * @return
     */
    public Map<Integer, Node> getNodeMap(List<Path> pathList) {
        Map<Integer, Node> nodeMap = new HashMap<>();
        if (pathList == null || pathList.size() == 0) {
            return nodeMap;
        }

        StringBuilder sqlBuilder = new StringBuilder("select * from node where id in(");
        for (Path path : pathList) {
            sqlBuilder.append(path.getNodeID()).append(",");
            sqlBuilder.append(path.getEndNode()).append(",");
        }
        sqlBuilder.deleteCharAt(sqlBuilder.length() - 1).append(")");

        Cursor cur = getCursor(sqlBuilder.toString(), null);
        while (cur.moveToNext()) {
            Node node = getNode(cur);
            nodeMap.put(node.getId(), node);
        }
        cur.close();
        return nodeMap;
    }

    // 将每个工作区中的所有节点与 该工作区的尺寸适应
    public void autoAdapterScreen() {
        Point currentSize = WindowUtil.getScreenSize();
        List<Workspace> allWorkspace = getAllWorkspace();
        for (Workspace workspace : allWorkspace) {
            if (workspace.getWidth() == 0 && workspace.getHeight() == 0 ||
                    workspace.getWidth() == currentSize.x && workspace.getHeight() == currentSize.y) {
                continue;
            }
            float scaleX = currentSize.x / workspace.getWidth();
            float scaleY = currentSize.y / workspace.getHeight();
            List<Node> allNode = getAllNode(workspace.getId());
            mDatabase.beginTransaction();  //手动设置开始事务
            try {
                mDatabase.setTransactionSuccessful(); //设置事务处理成功，不设置会自动回滚不提交
                for (Node node : allNode) {
                    node.setPositionX((int) (node.getPositionY() * scaleX));
                    node.setPositionY((int) (node.getPositionY() * scaleY));
                    updateData(node);
                }
            } catch (Exception e) {
                Log.e(LOG, "调整节点位置时出错");
            } finally {
                mDatabase.endTransaction(); //处理完成
            }
        }
    }

    // 根据当前路线选中的起始点,获取终点的可选范围 ( 去掉不可选就是可选的)
    // 以下情况的节点不可选:
    // 1. 起始点不可选
    public List<Node> getSelectableNode(Node startNode) {
        String sql = "select * from node where " + NODE_ID + "!=?";
        return getNodeList(mDatabase.rawQuery(sql, new String[]{String.valueOf(startNode.getId())}));
    }
    //endregion
}
