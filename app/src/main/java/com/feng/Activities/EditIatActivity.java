/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2016-2-19 下午8:43:44
 */
package com.feng.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.feng.Base.BaseActivity;
import com.feng.Constant.I_Parameters;
import com.feng.Database.Iat.IatDbHelper;
import com.feng.Database.Iat.IatRecord;
import com.feng.RSS.R;
import com.feng.Utils.IntentDealer;
import com.feng.Utils.Transfer;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.List;

public class EditIatActivity extends BaseActivity {
    @BindView(R.id.tvUniformTitleCenter)
    TextView mTvUniformTitleCenter;
    @BindView(R.id.btnUniformTitleRight)
    Button mBtnUniformTitleRight;
    @BindView(R.id.lvRecord)
    ListView mLvRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_iat);
        ButterKnife.bind(this);

        initView();
    }

    private IatRecordAdapter mIatRecordAdapter;

    private void initView() {
        mTvUniformTitleCenter.setText(R.string.menu_edit_speaker);
        mBtnUniformTitleRight.setText(R.string.iat_clear_all_record);

        mRecordList = IatDbHelper.getInstance().getAllRecord();
        mIatRecordAdapter = new IatRecordAdapter(EditIatActivity.this);
        mLvRecord.setAdapter(mIatRecordAdapter);

    }

    private void showAddRecordDialog() {
        final View view = View.inflate(EditIatActivity.this, R.layout.dialog_input_twoline, null);
        new AlertDialog.Builder(EditIatActivity.this)
                .setTitle("添加识别词")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String key = ((EditText) view.findViewById(R.id.et1)).getText().toString();
                        String value = ((EditText) view.findViewById(R.id.et2)).getText().toString();
                        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
                            TastyToast.makeText(EditIatActivity.this, "识别词和语音输出不能为空", TastyToast.LENGTH_SHORT, TastyToast.WARNING);
                            return;
                        }
                        IatDbHelper.getInstance().addRecord(new IatRecord(key, value));
                        // 刷新ListView
                        mRecordList = IatDbHelper.getInstance().getAllRecord();
                        mIatRecordAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showEditRecordDialog(final IatRecord ir) {
        final View view = View.inflate(EditIatActivity.this, R.layout.dialog_input_twoline, null);
        ((EditText) view.findViewById(R.id.et1)).setText(ir.getKey());
        ((EditText) view.findViewById(R.id.et2)).setText(ir.getValue());

        new AlertDialog.Builder(EditIatActivity.this)
                .setTitle("添加识别词")
                .setIcon(android.R.drawable.ic_menu_edit)
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String key = ((EditText) view.findViewById(R.id.et1)).getText().toString();
                        String value = ((EditText) view.findViewById(R.id.et2)).getText().toString();
                        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)) {
                            TastyToast.makeText(EditIatActivity.this, "识别词和语音输出不能为空", TastyToast.LENGTH_SHORT, TastyToast.WARNING);
                            return;
                        }
                        ir.setKey(key);
                        ir.setValue(value);
                        IatDbHelper.getInstance().updateRecord(ir);
                        // 刷新ListView
                        mRecordList = IatDbHelper.getInstance().getAllRecord();
                        mIatRecordAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showDelAllRecordDialog() {
        new AlertDialog.Builder(EditIatActivity.this)
                .setTitle("删除所有识别词")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setMessage("删除后不可恢复!确认要删除吗?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        IatDbHelper.getInstance().delAllRecord();
                        mRecordList = IatDbHelper.getInstance().getAllRecord();
                        mIatRecordAdapter.notifyDataSetChanged();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @OnClick({R.id.btnUniformTitleLeft, R.id.btnUniformTitleRight, R.id.fabAddIatRecord})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnUniformTitleLeft:
                EditIatActivity.this.finish();
                break;
            case R.id.btnUniformTitleRight:
                showDelAllRecordDialog();
                break;
            case R.id.fabAddIatRecord:
                showAddRecordDialog();
                break;
        }
    }


    private List<IatRecord> mRecordList;

    public class IatRecordAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public IatRecordAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mRecordList.size();
        }

        @Override
        public Object getItem(int position) {
            return mRecordList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mRecordList.get(position).getID();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView != null) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                convertView = mInflater.inflate(R.layout.listview_of_iat_record, null);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            holder.mEtKey.setText(mRecordList.get(position).getKey());
            holder.mEtValue.setText(mRecordList.get(position).getValue());
            holder.mBtnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditRecordDialog(mRecordList.get(position));
                }
            });
            holder.mBtnDel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IatDbHelper.getInstance().delRecord(mRecordList.get(position));
                    mRecordList = IatDbHelper.getInstance().getAllRecord();
                    mIatRecordAdapter.notifyDataSetChanged();
                }
            });
            holder.mBtnPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new IntentDealer(new Transfer()).sendTtsIntent(I_Parameters.TTS_START_SPEAK, mRecordList.get(position).getValue());
                }
            });

            return convertView;
        }

        class ViewHolder {
            @BindView(R.id.etKey)
            EditText mEtKey;
            @BindView(R.id.etValue)
            EditText mEtValue;
            @BindView(R.id.btnEdit)
            Button mBtnEdit;
            @BindView(R.id.btnDel)
            Button mBtnDel;
            @BindView(R.id.btnPlay)
            Button mBtnPlay;

            public ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }
}

