/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间  2016-3-2 上午11:46:31
 */
package com.feng.Activities;


import android.os.Bundle;
import android.view.View;
import com.feng.Base.BaseActivity;
import com.feng.Usb.ArmProtocol;
import com.feng.RSS.R;

public class TestActivity extends BaseActivity implements ArmProtocol{
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test);

//		L.i("启动USB连接服务");
//
//		CustomButton cbtn=(CustomButton)findViewById(R.id.cbtn); 
//		cbtn.setState(R.drawable.add, "kai ");
//		
	}
	public void onClick(View v){
//		intentDealer.sendIntent(USB_SEND, QueryCard, QueryCard);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
//		intentDealer.sendIntent(USB_DISCONNECT);
	}
}

