/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间  2016-2-19 下午8:44:19
 */
package com.feng.Activities;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.feng.Base.BaseActivity;
import com.feng.RSS.R;

public class EditVersionActivity extends BaseActivity{
	private Button cancel;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_version);

		initView();
	}
	private  void initView() {
		cancel=(Button)findViewById(R.id.btnUniformTitleLeft);
		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditVersionActivity.this.finish();
			}
		});
		((TextView)findViewById(R.id.tvUniformTitleCenter)).setText(R.string.edit_version);

		findViewById(R.id.btnUniformTitleRight).setVisibility(View.INVISIBLE);


	}
}

