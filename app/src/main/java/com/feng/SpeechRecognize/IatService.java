/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间  2016-3-25 上午11:50:39
 */
package com.feng.SpeechRecognize;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import com.feng.Constant.I_Parameters;
import com.feng.Utils.L;
import com.feng.RobotApplication;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class IatService extends Service implements I_Parameters{
	private final static String LOG=IatService.class.getSimpleName();
	// 引擎类型
	private BroadcastReceiver receiver;

	private static final int START_RECOGNIZE=888;
	public IBinder onBind(Intent intent) {
		return null;
	}
	//TODO 以后要弄到数据库中
	private HashMap<String, ArrayList<String>> robotSays;

	private IatListener mIatListener;
	private TtsSpeaker mTtsSpeaker;

	Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			removeMessages(START_RECOGNIZE);
		};
	};

	private IatCallback	icb=new IatCallback() {
		@Override
		public void iatReturn(String result) {
			L.i(LOG,"回调识别结果："+result);
			String iatResult= getResponse(result);
			if(iatResult==null ){
				L.i(LOG, "识别结果无法解析,继续开始识别!");
				mIatListener.startListen();
			}else{
				L.i(LOG,"识别结果可以解析,开始语音合成");
				mTtsSpeaker.speak(iatResult,icb);
			}
		}
		public void ttsFinish(){
			L.i(LOG,"TTS结束！ 继续开始识别!");
			mIatListener.startListen();
		}
	};

	public int onStartCommand(Intent intent, int flags, int startId) {
		SpeechUtility.createUtility(RobotApplication.getContext(), SpeechConstant.APPID + "=56eb5dfb");

		mIatListener = IatListener.getInstance(icb);
		mTtsSpeaker = TtsSpeaker.getInstance();

		initSays();
		initReceiver();
		L.i(LOG,"(＾－＾)V 语音识别 服务开启");

		mIatListener.startListen();
		return super.onStartCommand(intent, flags, startId);
	}
	private void startTTS(String ttsStr){
		mIatListener.stopListen();
		mTtsSpeaker.speak(ttsStr,icb);
	}
	private void initSays(){
		ArrayList<String> list1=new ArrayList<>();
		list1.add("你好呀,我是传菜机人胖胖,很高兴认识你");
		list1.add("你好呀,很高兴见到您");
		list1.add("欢迎光临,今天想吃什么!");
		list1.add("欢迎光临,今天天气真好!");
		list1.add("胖胖好累! 今天忙死啦!!!");
		list1.add("欢迎光临,我是传菜机器人胖胖");
		robotSays=new HashMap<>();
		robotSays.put("你好", list1);
		robotSays.put("你是谁", list1);
		robotSays.put("早上好", list1);
		robotSays.put("下午好", list1);
	}


	private String getResponse(String iatStr){
		//
		ArrayList<String> resp=null;
		//模糊识别
		for( String keys :robotSays.keySet()){

			if( iatStr.indexOf(keys)!=-1){
				resp=robotSays.get(keys);
				break;
			}
		}
		if( resp ==null ){
			return null;
		}else{
			return resp.get(new Random().nextInt(resp.size()));
		}
	}

	private void initReceiver() {
		IntentFilter filter = new IntentFilter();  
		/**  协议 部分*/
		filter.addAction(IAT_START_LISTENNING);
		filter.addAction(IAT_STOP_LISTENNING);
		filter.addAction(TTS_START_SPEAK);

		if( receiver==null){
			receiver= new IATReceiver();
		}
		L.i(LOG,"注册 IatReceiver");
		registerReceiver(receiver, filter);  
	}
	class IATReceiver extends BroadcastReceiver{
		public void onReceive(Context context, Intent intent) {
			switch (intent.getAction()) {
			case IAT_START_LISTENNING:
				mIatListener.startListen();
				break;
			case TTS_START_SPEAK:
				L.e(LOG,"准备语音合成: --->"+intent.getStringExtra(I_Parameters.UNIFORM_TTS));
				startTTS(intent.getStringExtra(I_Parameters.UNIFORM_TTS));
				break;

			default:
				break;
			}
		}
	}
	@Override
	public void onDestroy() {
		super.onDestroy();
		if( receiver!=null){
			L.e(LOG,"正在注销 IatReceiver...");
			unregisterReceiver(receiver);
		}
		// 退出时释放连接
		mIatListener.finishListen();
		L.e(LOG,"╮(╯_╰)╭语音识别 服务关闭");
	}
}

