/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间  2016-1-4 上午10:44:25
 */
package com.feng.Utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import com.feng.Usb.ArmProtocol;
import com.feng.Constant.I_Parameters;
import com.feng.RobotApplication;
import com.feng.Schedule.ScheduleProtocal;

import java.util.List;

public class IntentDealer implements ArmProtocol,I_Parameters,ScheduleProtocal {
	private Transfer transfer;
	
	public IntentDealer(Transfer transfer) {
		super();
		this.transfer = transfer;
	}
	public IntentDealer(){
		super();
		this.transfer=new Transfer();
	}
	public void sendTtsIntent(String protocolAction,String ttsStr){
		Intent intent=new Intent(protocolAction);
		intent.putExtra(UNIFORM_TTS,ttsStr);
		RobotApplication.getContext().sendBroadcast(intent);
	}
	public void sendIntent(String protocolAction){
		this.sendIntent(protocolAction,null,null);
	}
	public  void sendIntent(String protocolAction,byte[] data){
		this.sendIntent(protocolAction,data,null);
	}
	public  void sendIntent(String protocolAction,byte[] head,byte data){
		byte[] newByteArray=new byte[]{data};
		this.sendIntent(protocolAction,head,newByteArray);
	}
	/**
	 * @param protocolAction	(用来区分 广播)
	 * @param head	 数据头
	 * @param data	  数据体 ( 不包括 长度 .和校验位)
	 * @return 打包好的intent,包含了 head,以及 data,并发送广播, 并交给 wifiLinker打包处理后发送
	 */
	public  void sendIntent(String protocolAction,byte[] head,byte[] data){
		Intent intent = new Intent();
		intent.setAction(protocolAction);
		if( head==null && data==null){
			
		}else if(data==null){
			intent.putExtra(UNIFORM_SEND,transfer.packingByte(head,null));
		}else{
			intent.putExtra(UNIFORM_SEND, transfer.packingByte(head, data));
		}
		RobotApplication.getContext().sendBroadcast(intent);
	}
	public  void serviceReceive(String protocolAction,byte[] data){
		Intent intent=new Intent(protocolAction);
		intent.putExtra(UNIFORM_RECEIVE, data);
		RobotApplication.getContext().sendBroadcast(intent);
	}
	public void serviceReceive(String action,int clientID,byte[] data){
		Intent intent=new Intent(action);
		intent.putExtra(UNIFORM_RECEIVE, data);
		intent.putExtra(UNIFORM_CLIENT_ID, clientID);
		RobotApplication.getContext().sendBroadcast(intent);
	}
	public void serviceSend(String action,byte[] send,byte[] result){
		Intent intent=new Intent(action);
		intent.putExtra(UNIFORM_SEND, send);
		intent.putExtra(UNIFORM_RECEIVE, result);
		RobotApplication.getContext().sendBroadcast(intent);
	}
	public void sendIntent(String action,int clientID,byte[] head,byte[] data){
		Intent intent = new Intent();
		intent.setAction(action);
		if( head==null && data==null){
			
		}else if(data==null || head==null){
			intent.putExtra(UNIFORM_SEND,transfer.packingByte(head,null));
		}else{
			intent.putExtra(UNIFORM_SEND, transfer.packingByte(head, data));
		}
		//放入客户端ID
		intent.putExtra(UNIFORM_CLIENT_ID, clientID);
		RobotApplication.getContext().sendBroadcast(intent);
	}
	/**
	 * 获取发送的数据
	 * @param intent
	 * @return
	 */
	public  byte[] getSendData(Intent intent){
		return intent.getByteArrayExtra(ArmProtocol.UNIFORM_SEND);
	}
	/**
	 * 获取Intent中接收的数据 
	 * @param intent
	 * @return
	 */
	public  byte[] getResultData(Intent intent){
		return intent.getByteArrayExtra(ArmProtocol.UNIFORM_RECEIVE);
	}
	
	public int getClientID(Intent intent){
		return intent.getIntExtra(UNIFORM_CLIENT_ID, -1);
	}
	// 判断是否 服务正在运行
	public static boolean isServiceRunning(Context mContext,String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager)
        mContext.getSystemService(Context.ACTIVITY_SERVICE); 
        List<ActivityManager.RunningServiceInfo> serviceList 
                   = activityManager.getRunningServices(30);

        if (!(serviceList.size()>0)) {
            return false;
        }
        for (int i=0; i<serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }
}

