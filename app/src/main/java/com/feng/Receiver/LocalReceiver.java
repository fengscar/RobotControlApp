/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间  2016-1-24 下午9:54:36
 */
package com.feng.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.feng.Usb.ArmProtocol;
import com.feng.Utils.IntentDealer;
import com.feng.Utils.L;
import com.feng.Utils.Transfer;

/**
 *  接收本机Service的广播信息, 在Activity中注册, 然后进行使用
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间  2016-1-24 下午9:55:01
 */
public class LocalReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
		boolean finish=false;
		byte[] data=new IntentDealer(new Transfer()).getSendData(intent);
		while( finish == false ){
			switch (intent.getAction()) {
			/**
			 * 以下是本机的 广播
			 */
			case ArmProtocol.SOCKET_CONNECT_FAILED:
				L.e("mainActivity 已经接收到广播, 直到了socket连接失败,准备弹出重连窗口");
				break;
			case ArmProtocol.WIFI_DISABLED:
			case ArmProtocol.WIFI_DISCONNECTED:
				
				break;
			default:
				break;
			}
		}
		finish=true;
	}
}

