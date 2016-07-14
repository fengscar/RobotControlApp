package com.feng.Utils;

import android.util.Log;
/**
 * 
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2015-11-18 上午9:54:37
 * @功能 封装LOG调试工具类
 */
public class L {
	// 当DEBUG为true时 输出LOG
	public static  boolean LogFlag=true;
	public static  String TAG="RobotSoftSystem";
	private static L mLogUtil;

	private L(){
	}
	
	public static L getInstance(){
		if(mLogUtil==null){
			mLogUtil=new L();
		}
		return mLogUtil;
	}
	//不定参数的 
	public static void o(Object...objs){
		StringBuilder sb=new StringBuilder();
		for(Object o:objs){
			sb.append(o.toString()+"┋");
		}
		Log.i(TAG,sb.toString());
	}
	public static void i(String str){
		i(TAG,str);
	}
	public static void i(String log,String str){
		if(LogFlag==true){
			Log.i(log,str);
		}
	}
	
	public static void w(String str){
		w(TAG,str);
	}
	public static void w(String log,String str){
		if(LogFlag==true){
			Log.w(log,str);
		}
	}
	
	public static void e(String str){
		e(TAG,str);
	}
	public static void e(String log,String str){
		if(LogFlag==true){
			Log.e(log,str);
		}
	}
	
	public static void d(String str){
		d(TAG,str);
	}
	public static void d(String log,String str){
		if(LogFlag==true){
			Log.d(log,str);
		}
	}
}
