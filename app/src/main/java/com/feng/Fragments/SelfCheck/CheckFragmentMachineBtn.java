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

public class CheckFragmentMachineBtn extends CheckFragment{
	public static CheckFragment newInstance(int expRes,int imgRes) {
		CheckFragment f =new CheckFragmentMachineBtn();
		Bundle args = new Bundle();
		args.putInt(Explain_RES, expRes);
		args.putInt(Image_RES,imgRes);
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
		setKeyStr("执行按键:", "停止按键", null);
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
		if ( detailAction.equals(MACHINE_START_BTN)){
			if( data[0]==0x00){
				btn1.setChecked(false);
			}else if( data[0]==0x01){
				btn1.setChecked(true);
			}
		}else if( detailAction.equals(MACHINE_STOP_BTN)){
			if( data[0]==0x00){
				btn2.setChecked(false);
			}else if( data[0]==0x01){
				btn2.setChecked(true);
			}
		}
	}
	public void startCheck(){
		new IntentDealer(new Transfer()).sendIntent(USB_SEND,SelfCheckMachineBtn, null);
	}
}

