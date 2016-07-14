package com.feng.Sound;

import java.io.File;
import java.util.HashMap;

import com.feng.RSS.R;
import com.feng.RobotApplication;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Environment;

/**
 * 单例模式的 音频播放 根据设置的开关 ,控制播放音频( 还未支持选择文件)
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2015-12-10 下午6:42:52
 * @功能
 */
public class VoiceManager {
	private static VoiceManager voiceInstance;
	//私有的构造函数
	private VoiceManager(){
		
	}
	// 获取实例的外部接口
	public static VoiceManager getInstance(){
		if(voiceInstance==null){
			voiceInstance=new VoiceManager();
		}
		return voiceInstance;
	}
	//播放报警
	public void playAlarm(){
		this.playSound(2, 3);
	}
	
	public void init(){
		File sdcard=Environment.getExternalStorageDirectory();
		File audioFile=new File(sdcard.getPath()+"/Music/goodmorningandroid.mp3");
	}


	public void playSound(int sound, int loop) {
		SoundPool soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 100);

		HashMap<Integer, Integer> soundPoolMap = new HashMap<Integer, Integer>();  
		
		
		soundPoolMap.put(1, soundPool.load(RobotApplication.getContext(), R.raw.alarm, 1));        
		soundPoolMap.put(2, soundPool.load(RobotApplication.getContext(), R.raw.point, 1));       
		soundPoolMap.put(3, soundPool.load("/external/audio/media/6074",1));       
		
		AudioManager mgr = (AudioManager)RobotApplication.getContext().getSystemService(Context.AUDIO_SERVICE);   

		float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);   

		float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);       
		
		float volume = streamVolumeCurrent/streamVolumeMax;   
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		soundPool.play(soundPoolMap.get(sound), volume, volume, 1, loop, 1f);
		//参数：1、Map中取值   2、当前音量     3、最大音量  4、优先级   5、重播次数   6、播放速度

	} 
	

}
