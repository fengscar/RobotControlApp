/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间  2016-1-15 下午3:48:41
 * 
 * 接收开机后的 BOOT_COMPLETED广播
 * 实现APP的自动运行( 要到开机后一分钟才能运行) 是否有用? 是否会在手动运行后再次自动运行?
 */
package com.feng.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.feng.Activities.MainActivity;

public class RobotAppBroadcastReceiver extends BroadcastReceiver {
	static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ACTION)) {
			Intent mainActivityIntent = new Intent(context, MainActivity.class);  // 要启动的Activity
			mainActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(mainActivityIntent);
		}
	}
}

