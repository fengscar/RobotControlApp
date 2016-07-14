/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间  2016-3-22 上午9:57:37
 */
package com.feng.Fragments.SelfCheck;

import java.util.ArrayList;
import java.util.Arrays;

import com.feng.Base.BaseActivity;
import com.feng.RSS.R;
import com.feng.Utils.IntentDealer;
import com.feng.Utils.Transfer;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

public class CheckFragmentMag extends CheckFragment{
	private GridView gvMag;
	private boolean[] magList;
	
	public static CheckFragment newInstance(int expRes,int imgRes) {
		CheckFragment f =new CheckFragmentMag();
		Bundle args = new Bundle();
		args.putInt(Explain_RES, expRes);
		args.putInt(Image_RES, imgRes);
		f.setArguments(args);
		return f;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view=inflater.inflate(R.layout.fragment_self_check_mag, null);
		initView();
		initValue();
		
		return view;
	}
	@Override
	protected void initView() {
		ivSignPic=(ImageView)view.findViewById(R.id.ivSignPic);
		tvExplain=(TextView)view.findViewById(R.id.tvExplain);
		
		tvExplain.setText( this.getArguments().getInt(Explain_RES));
		if( getArguments().getInt(Image_RES) !=0){
			ivSignPic.setImageResource( this.getArguments().getInt(Image_RES));
		}else{
			ivSignPic.setVisibility(View.GONE);
		}
	}
	private void initValue() {
		magList=new boolean[20];
		
		gvMag=(GridView)view.findViewById(R.id.gvMag);
		gvMag.setAdapter(new MagGridViewAdapter());
	}
	
	
	class MagGridViewAdapter extends BaseAdapter{
		public int getCount() {
			return magList.length;
		}
		public Object getItem(int position) {
			return magList[position];
		}
		public long getItemId(int position) {
			return position;
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView=LayoutInflater.from(getActivity()).inflate(R.layout.gridview_of_mag, null);
			ToggleButton tbMag=(ToggleButton)convertView.findViewById(R.id.tbMag);
			tbMag.setTextOn( String.valueOf(position+1));
			tbMag.setTextOff( String.valueOf(position+1));
			tbMag.setText(String.valueOf(position+1));
			tbMag.setChecked(magList[position]);
			
			return convertView;
		}
	}
	@Override
	public void onDataChange(Intent intent) {
		byte[] data=null;
		String detailAction=null;
		if((data= getCheckData(intent) ) ==null
				|| (detailAction=getCheckAction(intent) )==null){
			return ;
		}
		if ( detailAction.equals(SELF_CHECK_MAG_POSITION)){
			for( byte b:data){
				if( (int)b >20 || (int)b<1){
					continue;
				}
				magList[(int)b-1]=true;
			}
			gvMag.setAdapter(new MagGridViewAdapter());
		}
	}
	public void startCheck(){
		new IntentDealer(new Transfer()).sendIntent(USB_SEND,SelfCheckMag, null);
	}
}

