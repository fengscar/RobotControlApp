package com.feng.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.renderscript.Int2;
import android.renderscript.Int4;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2015-11-20 下午3:49:10
 * @功能 具体的数据库存取类 实现地图数据的存取接口
 */
public class DatabaseManager {
	private final static String LOG = DatabaseManager.class.getSimpleName();

	protected MapDatabaseHelper dbHelper;
	protected SQLiteDatabase db;

	protected DatabaseManager(){
		dbHelper = MapDatabaseHelper.getInstance();
		if(db==null || !db.isOpen()){
			db = dbHelper.getWritableDatabase();
		}
	}

	public DatabaseManager(Context context) {
		dbHelper = MapDatabaseHelper.getInstance();
		db = dbHelper.getWritableDatabase();
//		L.i("数据库构造函数执行完成" + " ---并打开了数据库");
	}
	public boolean addToDatabase() throws Exception {
		throw  new Exception("未重载具体的操作方法");
	}

	public boolean delFromDatabase() throws Exception {
		throw  new Exception("未重载具体的操作方法");

	}
	public boolean updateData(DatabaseManager newData) throws Exception {
		throw  new Exception("未重载具体的操作方法");

	}

	/**
	 * 获取  路径的 起 点坐标 ,终点坐标
 	 */
	public List<Int4> getPathPositionsByRouteID(int routeID){
		List<Int4> result=new ArrayList<Int4>();
		for(Int2 int2:getPathsByRouteID(routeID) ){
			//int.x 是 起点的NODE的ID, 该方法得到该NODE的坐标
			Int2 startPostion=getNodePositionByID( int2.x );
			//int.x 是 终点的NODE的ID, 该方法得到该NODE的坐标
			Int2 endPostion=getNodePositionByID( int2.y );
			// 将2点坐标 放入Int4
			Int4 temp=new Int4(startPostion.x, startPostion.y, endPostion.x, endPostion.y);
			result.add(temp);
		}
		return result;
	}
	//这两个方法的SQL语句怎么合并..?
	/**
	 * 返回 指定 路线下 的所有路径的 起点和终点 的节点ID
	 * @param routeID
	 * @return
	 */
	private List<Int2> getPathsByRouteID(int routeID){
		List<Int2> list=new ArrayList<Int2>();
		String sql="select nodeID,endNodeID from path where routeID=?";
		Cursor cur=db.rawQuery(sql, new String[]{routeID+""});
		while(cur.moveToNext()){
			Int2 int2=new Int2(cur.getInt(cur.getColumnIndex("nodeID")),
					cur.getInt(cur.getColumnIndex("endNodeID")));
			list.add(int2);
		}
		return list;
	}
	/**
	 *  通过ID 返回该节点的坐标 
	 * @param nodeID
	 * @return
	 */
	private Int2 getNodePositionByID(int nodeID){
		String sql="select positionX,positionY from node where id=?";
		Cursor cur=db.rawQuery(sql, new String[]{nodeID+""});
		cur.moveToNext();
		return new Int2(cur.getInt(cur.getColumnIndex("positionX")),cur.getInt(cur.getColumnIndex("positionY")));

	}
}
