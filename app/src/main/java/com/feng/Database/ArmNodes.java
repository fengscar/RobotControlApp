package com.feng.Database;

import android.database.Cursor;
import com.feng.Utils.L;
import com.feng.Utils.Transfer;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class ArmNodes extends DatabaseManager{

	private int nodeID;
	private int distance;
	private String RFID;
	private int yaw;
	private int angle;
	private int maxSpeed;
	
	private Transfer transfer;

	public ArmNodes() {
		transfer=new Transfer();
	}
	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public void setRFID(String rFID) {
		RFID = rFID;
	}

	public void setYaw(int yaw) {
		this.yaw = yaw;
	}

	public void setAngle(int angle) {
		this.angle = angle;
	}

	public void setMaxSpeed(int maxSpeed) {
		this.maxSpeed = maxSpeed;
	}
	/**
	 * 得到 cursor 
	 * @return
	 */
	private Cursor getArmNodeCursor(){
		String sql="select path.nodeID,path.distance,node.RFID,path.yaw," +
				"path.angle,path.maxspeed " +
				"from path left join node " +
				"where path.nodeID=node.ID " +
				"order by path.routeID,path.orderID";
		return db.rawQuery(sql, null);
	}
	/**
	 * 将获取的表中的数据 转换为NODE
	 * @return
	 */
	public List<ArmNodes> getArmNodes(){
		if (!db.isOpen()) {  
			db = dbHelper.getWritableDatabase();  
		}  
		List<ArmNodes> list=new ArrayList<>();
		try {
			// 得到本表的 cursor
			Cursor cur= this. getArmNodeCursor();
			while(cur.moveToNext()){
				ArmNodes data=new ArmNodes();
				data.setNodeID(cur.getInt(cur.getColumnIndex("nodeID")));
				data.setDistance(cur.getInt(cur.getColumnIndex("distance")));
				data.setRFID(cur.getString(cur.getColumnIndex("RFID")));
				data.setYaw(cur.getInt(cur.getColumnIndex("yaw")));
				data.setAngle(cur.getInt(cur.getColumnIndex("angle")));
				data.setMaxSpeed(cur.getInt(cur.getColumnIndex("maxSpeed")));
				/**
				 * 如果
				 */
				list.add(data);
			}
		} catch (Exception e) {	
			e.printStackTrace();
		}
		return  list;
	}
	/**
	 * 将INT形式的转换为 byte形式 
	 */
	private byte[] convertToByte(){
		byte nodeByte[]=new byte[12];
		try {
			// ID 2byte
			System.arraycopy(transfer.intTo2Byte(nodeID), 0, nodeByte, 0, transfer.intTo2Byte(nodeID).length);
			// RFID 2byte
			System.arraycopy(transfer.hexToByte(RFID), 0, nodeByte, 2, 2);
			// Distance 2byte
			System.arraycopy(transfer.intTo2Byte(distance), 0, nodeByte, 4, transfer.intTo2Byte(distance).length);
			// yaw 2byte
			System.arraycopy(transfer.intTo2Byte(yaw), 0, nodeByte, 6, transfer.intTo2Byte(yaw).length);
			// angle 2byte
			System.arraycopy(transfer.intTo2Byte(angle), 0, nodeByte, 8, transfer.intTo2Byte(angle).length);
			// maxSpeed 1byte
			System.arraycopy(new byte[]{(byte)maxSpeed}, 0, nodeByte, 10, 1);
		} catch (Exception e) {
			// TODO: handle exception
			L.e(e.toString());
		}
		return nodeByte;
	}
	/**
	 * 获取转换后的List<ArmNODE>
	 */
	public List<byte[] > getArmNodeBytes(){
		List<byte[]> byteList=new ArrayList<>();
		List<ArmNodes> armList=this.getArmNodes();
		for(ArmNodes armNode: armList){
			byteList.add( armNode.convertToByte() );
		}
		return byteList;
	}
}
