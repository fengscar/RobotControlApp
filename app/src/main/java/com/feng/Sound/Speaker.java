package com.feng.Sound;

import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import com.feng.RobotApplication;
import com.feng.Utils.L;
import com.feng.Utils.T;

import java.util.Locale;

public class Speaker {
	private static Speaker instance;
	private static TextToSpeech mTextToSpeech;
	
	
	private Speaker(){
		init();
	}

	public static Speaker getInstance(){
		if(instance==null){
			instance=new Speaker();
		}
		return instance;
	}
	public void init(){
		//实例并初始化TTS对象
		mTextToSpeech = new TextToSpeech(RobotApplication.getContext(),new TextToSpeech.OnInitListener()
		{
			@Override
			public void onInit(int status) 
			{
				if(status == TextToSpeech.SUCCESS)
				{
					int supported = mTextToSpeech.setLanguage(Locale.CHINA);
					if((supported != TextToSpeech.LANG_AVAILABLE)&&(supported != TextToSpeech.LANG_COUNTRY_AVAILABLE))
					{
						T.show("不支持当前语言！");
					}
				}
			}
		});     
		mTextToSpeech.setOnUtteranceProgressListener(
				new UtteranceProgressListener() {
			public void onStart(String utteranceId) {
				L.i("语音播报开始");
			}
			
			public void onError(String utteranceId) {
				
			}
			public void onDone(String utteranceId) {
				L.i("语音播报结束");
			}
		});
	}
	public void say(String str){
		mTextToSpeech.speak(str,TextToSpeech.QUEUE_FLUSH, null);
		
	}
}
