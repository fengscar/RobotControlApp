/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间  2016-3-22 上午9:57:37
 */
package com.feng.Fragments.SelfCheck;

import java.util.Arrays;
import java.util.Timer;

import com.feng.RSS.R;
import com.feng.Utils.IntentDealer;
import com.feng.Utils.L;
import com.feng.Utils.Transfer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CheckFragmentRFID extends CheckFragment{
	public static CheckFragment newInstance(int expRes,int imgRes) {
		CheckFragment f =new CheckFragmentRFID();
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
		setKeyStr("读到的卡号 :", null , null);
		btn1.setVisibility(View.INVISIBLE);
		btn1.setChecked(true);
		btn2.setVisibility(View.GONE);
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
		if ( detailAction.equals(CURRENT_RFID)){
			if( data.length !=2 ){
				L.e(" 自检卡号 长度错误 !=2..");
			}
			btn1.setVisibility(View.VISIBLE);
			btn1.setText( "0x"+
					(Integer.toHexString((int)(data[1]&0xff)).toUpperCase())+
					(Integer.toHexString((int)(data[0]&0xff)).toUpperCase()));
		}
	}
	public void startCheck(){
		new IntentDealer(new Transfer()).sendIntent(USB_SEND,SelfCheckRFID, null);
	}
}

