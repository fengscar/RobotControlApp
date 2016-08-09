/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间  2016-1-6 下午3:22:04
 */
package com.feng.Activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.feng.Usb.ArmProtocol;
import com.feng.Constant.I_Parameters;
import com.feng.RSS.R;
import com.feng.Sound.Speaker;
import com.feng.Sound.VoiceManager;
import com.feng.Utils.L;

import java.io.File;


public class LoadingActivity extends Activity implements I_Parameters,ArmProtocol{
	private ImageView loadImage;
	private TextView loadText,loadSystemName;
	private ProgressBar loadProgress;

	private BroadcastReceiver receiver;
	boolean loadingFinished=false;

	Handler handler=new Handler(){
		private Intent intent;

		public void handleMessage(android.os.Message msg) {
			if( msg.what== REFRESH){
				loadProgress.setProgress(loadProgress.getProgress()+1);
			}
			if( msg.arg1 == 999){
				L.e("LoadingActivity", "载入完成");
				loadingFinished=true;
				intent = new Intent();
				intent.setClass(LoadingActivity.this,MainActivity.class);
				startActivity(intent);
				// 结束掉 LoadingActivity
				finish();
			}
		}
	};
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);
		initView();
		//显示 动画
		showAnimation();
		// 开始载入
		loading();
	}

	private void loading() {

		//TODO 验证 试用期限

		// 网络连接
		initService();  //初始化 socket连接服务

		//载入 语音
		Speaker.getInstance().init();

		// 载入 音效
		VoiceManager.getInstance().playAlarm();
		//TODO 检测各个 音效路径 是否有效

		//		L.e( getSdCardPath() );
		//		L.e( getDefaultFilePath());
		//		L.e( getFilesDir().toString());
		new Thread(new Runnable() {
			public void run() {
				for( int i=0; i<1000 ;i++){
					try {
						Thread.sleep(1);
						Message msg=new Message();
						msg.arg1=i;
						msg.what=REFRESH;
						handler.sendMessage(msg);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	//不绑定的开启服务
	private void initService() {
		// 开启 TCP 客户端SOCKET 服务
		//		startService(new Intent().setClass(this, ARM_TCP_Service.class));
		//		startService(new Intent().setClass(this, ARM_UDP_Service.class));
		// 开启 召唤系统 服务端 SOCKET 服务
//		startService(new Intent().setClass(this, CorServerService.class));
		// 开启USB 连接服务

	}
	public static String getSdCardPath() {  
		String sdpath = "";  
		sdpath = Environment.getExternalStorageDirectory()  
				.getAbsolutePath();  
		return sdpath;  
	}  
	public static String getDefaultFilePath() {  
		String filepath = "";  
		File file = new File(Environment.getExternalStorageDirectory(),  
				"abc.txt");  
		if (file.exists()) {  
			filepath = file.getAbsolutePath();  
		} else {  
			filepath = "不适用";  
		}  
		return filepath;  
	}  

	private void showAnimation() {
		// 操作 loadImage + loadSystemName
	}

	private void initView() {
		loadImage=(ImageView)findViewById(R.id.loadImage);
		loadSystemName=(TextView)findViewById(R.id.loadSystemName);
		loadProgress=(ProgressBar)findViewById(R.id.loadProgress);
		loadText=(TextView)findViewById(R.id.loadText);
	}

}

