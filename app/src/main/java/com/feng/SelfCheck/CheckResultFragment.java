/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间  2016-3-23 下午3:34:36
 */
package com.feng.SelfCheck;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.feng.RSS.R;
import com.feng.Usb.UsbData;
import com.feng.Utils.FileUtil;
import com.feng.Utils.MyDate;
import com.feng.Utils.T;

import java.io.IOException;

public class CheckResultFragment extends CheckFragment{
	public static CheckFragment newInstance(String[]  resultStr) {
		CheckFragment f = new CheckResultFragment();
		Bundle args = new Bundle();
		args.putStringArray(Explain_RES, resultStr);
		f.setArguments(args);
		return f;
	}
	private ListView lvCheckResult;
	private String[] resultStr;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view=inflater.inflate(R.layout.fragment_self_check_result, null);
		lvCheckResult=(ListView)view.findViewById(R.id.lvCheckResult);

		return view;
	}

	@Override
	public boolean startSelfCheck() {
		return false;
	}

	@Override
	public void onReceiveArmData(UsbData usbData) {

	}

	public void setCheckResultStr(String[] str){
		this.resultStr=str;
		lvCheckResult.setAdapter(new ArrayAdapter<String>(
				this.getActivity(), 
				android.R.layout.simple_expandable_list_item_1,
				resultStr));

	}
	public void saveCheckLog(){
		for( String str : resultStr){
			try {
				FileUtil.writeSDFile("CheckLog",MyDate.getDateEN()+ "  " +str);
			} catch (IOException e) {
				e.printStackTrace();
				return ;
			}
		}
		T.show("成功保存到 根目录下的 CheckLog.txt");
	}
}

