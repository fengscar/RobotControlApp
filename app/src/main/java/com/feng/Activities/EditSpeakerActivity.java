/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2016-2-19 下午8:43:44
 */
package com.feng.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.feng.Base.BaseActivity;
import com.feng.RSS.R;

public class EditSpeakerActivity extends BaseActivity {
    private Button cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_speaker);

        initView();
    }

    private void initView() {
        cancel = (Button) findViewById(R.id.btnUniformTitleLeft);
        cancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                EditSpeakerActivity.this.finish();
            }
        });

        ((TextView) findViewById(R.id.tvUniformTitleCenter)).setText(R.string.menu_edit_speaker);

        findViewById(R.id.btnUniformTitleRight).setVisibility(View.INVISIBLE);

        findViewById(R.id.btnAddMode).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final View view=View.inflate(EditSpeakerActivity.this,R.layout.dialog_input_oneline,null);
                new AlertDialog.Builder(EditSpeakerActivity.this)
                        .setTitle("请输入模式名称")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(view)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText etInputMode=(EditText)view.findViewById(R.id.etInput);
                                //TODO 此时还只有keyword,没有IATword 如何保存模式?
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });


    }
}

