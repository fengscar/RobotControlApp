package com.feng.Utils;

import com.feng.Constant.I_Parameters;
import com.feng.RSS.R;
import com.feng.RobotApplication;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2015-11-18 上午9:55:03
 * @功能 Toast显示工具类
 */
public class T implements I_Parameters{
	private static boolean ToastFlag=true;

	private static Toast mToast;
	private static ImageView mIv;
	private static TextView mTv;
	
	/**
	 * 附带View的显示
	 * @param msg  要显示的信息
	 * @param isShowLong true: 显示3秒; false : 显示1秒
	 */
	public static void show(CharSequence msg,int imageViewRes,boolean isShowLong,boolean needTTS){
		if(ToastFlag){
			if( mToast== null){
				initToast();
			}
			if( imageViewRes!=0){
				mIv.setVisibility(View.VISIBLE);
				mIv.setImageResource(imageViewRes);
			}else{
				mIv.setVisibility(View.GONE);
			}
			mToast.setDuration(isShowLong?Toast.LENGTH_LONG:Toast.LENGTH_SHORT);
			mTv.setText(msg);
			mToast.show();
			if( needTTS ==true ){
				new IntentDealer(new Transfer()).sendTtsIntent(TTS_START_SPEAK,msg.toString());
			}
		}
	}

	public static void show(CharSequence msg,int imageRes){
		show(msg, imageRes,true,true);
	}
	//默认 是 显示3秒
	public static void show(CharSequence msg){
		show(msg,0,true,true);
	}
	
	
	private static void initToast(){
		View view=LayoutInflater.from(RobotApplication.getContext()).inflate(R.layout.toast, null);
		mTv=(TextView)view.findViewById(R.id.toastText) ;
		mIv=(ImageView)view.findViewById(R.id.toastImage);
		mToast=new Toast(RobotApplication.getContext());
		mToast.setView(view);
		mToast.setGravity(Gravity.CENTER, 0, 500);
	}
}
