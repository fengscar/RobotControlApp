package com.feng.Database;

import android.database.Cursor;
import com.feng.Constant.I_MapData;

import java.util.ArrayList;
import java.util.List;

/**
 * ARM需要的路线 数据格式
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2015-12-3 下午10:35:58
 * @功能
 */
public class ArmRoutes extends DatabaseManager implements I_MapData{
	private int routeID;
	private int preRouteID;
	private int nodeNum;
	
	public ArmRoutes() {
	}
	public int getRouteID() {
		return routeID;
	}
	public void setRouteID(int routeID) {
		this.routeID = routeID;
	}
	public int getPreRouteID() {
		return preRouteID;
	}
	public void setPreRouteID(int preRouteID) {
		this.preRouteID = preRouteID;
	}
	public int getNodeNum() {
		return nodeNum;
	}
	public void setNodeNum(int nodeNum) {
		this.nodeNum = nodeNum;
	}
	private Cursor getArmRouteCursor(){
		if (!db.isOpen()) {  
			db = dbHelper.getWritableDatabase();  
		}  
		/**  sql语句 (另一种)
		select id,preRouteID,(select count(*) from newpath 
				where route.[id]= newPath.[routeID] group by routeID)
		from route ,newpath
		group by id
		order by id
		 */
		String sql="select "+ROUTE_ID+","+ROUTE_PREID+
				",(select count(1) from path where Route."+ROUTE_ID+"=path."+
					PATH_ROUTEID +" group by Path."+PATH_ROUTEID+") " +
				"from Route " +
				"left join Path " +
				"on route.ID=path.routeID " +
				" group by  Route."+ROUTE_ID+
				" order by Route."+ROUTE_ID;
		return db.rawQuery(sql, null);
	}
	/**
	 * 将 数据库中获取的ARM ROUTE数据  载入到LIST中
	 * @return
	 */
	public List<ArmRoutes> getArmRoutes(){
		Cursor cur=this.getArmRouteCursor();
		List<ArmRoutes> list=new ArrayList<ArmRoutes>();
		while(cur.moveToNext()){
			ArmRoutes data=new ArmRoutes();
			data.setRouteID(cur.getInt(cur.getColumnIndex("ID")));
			data.setPreRouteID(cur.getInt(cur.getColumnIndex("preRouteID")));
			data.setNodeNum(cur.getInt(2)); 
			//前级路线不为0 的路线  节点数要补上一个( 尾节点)
			if( data.getPreRouteID() > 0){
				data.setNodeNum(data.getNodeNum()+1);
			}
			list.add(data);
		}
		return  list;
	}
	
	/**
	 * 将 armRoute对象中的属性转换为byte形式
	 * @return
	 */
	private byte[] convertToByte(){
		return new byte[]{(byte)routeID,(byte)preRouteID,(byte)nodeNum};
	}
	/**
	 *  获取发送给ARM的byte[] 数组( list)
	 *  不包括路线数 ( 路线总数交给 transfer计算!)
	 *  例如这样 
	 *  [1,0,5]
	 *  [2,1,5]
	 *  [3,2,5]
	 */
	public List<byte[]> getArmRouteBytes(){
		List<byte[] > byteList=new ArrayList<>();
		List<ArmRoutes> armList=this.getArmRoutes();
		for(ArmRoutes armRoute: armList){
			byteList.add( armRoute.convertToByte() );
		}
		return byteList;
	}
}
