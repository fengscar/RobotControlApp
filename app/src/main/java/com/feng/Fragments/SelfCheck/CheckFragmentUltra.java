/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间  2016-3-22 上午9:57:37
 */
package com.feng.Fragments.SelfCheck;

import com.feng.RSS.R;
import com.feng.Utils.IntentDealer;
import com.feng.Utils.Transfer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CheckFragmentUltra extends CheckFragment{
	public static CheckFragment newInstance(int expRes,int imgRes) {
		CheckFragment f =new CheckFragmentUltra();
		Bundle args = new Bundle();
		args.putInt(Explain_RES, expRes);
		args.putInt(Image_RES, imgRes);
		f.setArguments(args);
		return f;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view=inflater.inflate(R.layout.fragment_self_check_3button, null);
		initView();
		init3Button();
		initValue();
		return view;
	}
	private void initValue() {
		setKeyStr("左前方超声波传感器:","右前方超声波传感器:", null);       
		btn1.setChecked(true);
		btn2.setChecked(true);
		btn1.setText(" 0 cm");
		btn2.setText(" 0 cm");
		btn3.setVisibility(View.GONE);
	}
	@Override
	public void onDataChange(Intent intent) {
		byte[] data=null;
		String detailAction=null;
		if((data= getCheckData(intent) ) ==null
				|| (detailAction=getCheckAction(intent) )==null){
			return ;
		}
		if ( detailAction.equals(ULTRASONIC_WARNING)){
			// 超声波有个10CM的盲区
			if(  (int)(data[0]&0xff)	<= 10){
				btn1.setText(  "< 10 cm");
				btn2.setText(  "< 10 cm");
			}else{
				// &0xff 是为了将 byte从有符号位转换为无符号位
				btn1.setText(  (int)(data[0]&0xff)	+"cm");
				btn2.setText(  (int)(data[0]&0xff)	+"cm");
			}
		}
	}
	public void startCheck(){
		new IntentDealer(new Transfer()).sendIntent(USB_SEND,SelfCheckUltra, null);
	}
}

