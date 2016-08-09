package com.feng.SelfCheck;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.feng.Base.BaseActivity;
import com.feng.Constant.I_Parameters;
import com.feng.RSS.R;
import com.feng.Usb.ArmHandler.SystemHandler;
import com.feng.Usb.ArmProtocol;
import com.feng.Usb.ArmUsbManager;
import com.feng.Usb.UsbData;
import com.feng.Usb.UsbEvent;
import com.feng.Utils.T;
import com.feng.Utils.Verifier;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SelfCheckActivity extends BaseActivity implements I_Parameters, ArmProtocol {
    private static final String TAG = "SelfCheckActivity";

    public final static int NORMAL = 0;
    public final static int PASS = 2;
    public final static int ERROR = 3;

    @BindView(R.id.tvUniformTitleCenter)
    TextView mTvUniformTitleCenter;
    @BindView(R.id.btnUniformTitleRight)
    Button mBtnUniformTitleRight;
    @BindView(R.id.btnCheckPass)
    Button mBtnCheckPass;
    @BindView(R.id.btnCheckError)
    Button mBtnCheckError;
    @BindView(R.id.btnCheckBegin)
    Button mBtnCheckBegin;
    @BindView(R.id.btnCheckFinish)
    Button mBtnCheckFinish;
    @BindView(R.id.btnCheckNext)
    Button mBtnCheckNext;
    @BindView(R.id.lvSelfCheckItem)
    ListView mLvSelfCheckItem;


    //用户 权限等级
    private String userGroup;

    private boolean mIsChecking = false;
    //当前项目
    private int mCurrentCheck = -1;
    public CheckFragment mCurrentCheckFragment;
    //自检选项
    public ArrayList<CheckItem> checkList;

    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_self_check);
        ButterKnife.bind(this);
        // TODO 这里简单的把  用户作为action传递过来, 是否需要改成使用 BUNDLE
        userGroup = this.getIntent().getAction();

        ArmUsbManager.getInstance().addObserver(TAG, new SelfCheckHandler(this));

        initView();
        refreshBtn();
    }

    private void initView() {
        //返回按键 , 标题, 隐藏右侧按键
        mTvUniformTitleCenter.setText(R.string.menu_self_check);
        mBtnUniformTitleRight.setVisibility(View.GONE);

        //  check操作按键

        //初始化数据 ListView 列表
        checkList = new ArrayList<>();
        checkList.add(new CheckItem(R.string.selfCheckListBarrier));
        checkList.add(new CheckItem(R.string.selfCheckListRFID));
        checkList.add(new CheckItem(R.string.selfCheckListMag));
        checkList.add(new CheckItem(R.string.selfCheckListInfra));
        checkList.add(new CheckItem(R.string.selfCheckListBtn));
        checkList.add(new CheckItem(R.string.selfCheckListUltra));
        checkList.add(new CheckItem(R.string.selfCheckListMotor));

        final SelfCheckListViewAdapter selfCheckListViewAdapter = new SelfCheckListViewAdapter();
        mLvSelfCheckItem.setAdapter(selfCheckListViewAdapter);
        mLvSelfCheckItem.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 避免重复 切换
                if (position != mCurrentCheck) {
                    changeFragment(position);
                    refreshBtn();
                    selfCheckListViewAdapter.notifyDataSetChanged();
                }
            }
        });
        //初始化 Fragment

    }

    @OnClick({R.id.btnUniformTitleLeft, R.id.btnCheckPass, R.id.btnCheckError,
            R.id.btnCheckBegin, R.id.btnCheckNext, R.id.btnCheckFinish})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnUniformTitleLeft:
                SelfCheckActivity.this.finish();
                break;

            // 这里不再判断是否可以执行,在refreshBtn判断显示那个按键

            case R.id.btnCheckPass:
                checkList.get(mCurrentCheck).setCheckState(PASS);
                changeFragment(mCurrentCheck + 1);
                break;

            case R.id.btnCheckError:
                checkList.get(mCurrentCheck).setCheckState(ERROR);
                changeFragment(mCurrentCheck + 1);
                break;

            case R.id.btnCheckBegin:
                mExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        if (SystemHandler.getInstance().startSelfCheck()) {
                            mIsChecking = true;
                            if (mCurrentCheck == -1) {
                                mCurrentCheck = 0;
                            }
                            changeFragment(getFragment(mCurrentCheck));
                        }
                    }
                });
                refreshBtn();
                break;

            case R.id.btnCheckNext:
                changeFragment(mCurrentCheck + 1);
                break;

            case R.id.btnCheckFinish:
                SystemHandler.getInstance().stopSelfCheck();
                mIsChecking = false;
                break;

        }
    }

    class SelfCheckListViewAdapter extends BaseAdapter {
        public int getCount() {
            return checkList.size();
        }

        public Object getItem(int position) {
            return checkList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            TextView tvCheckItem = null;
            // 初始化 VIEW
            if (convertView == null) {
                convertView = LayoutInflater.from(SelfCheckActivity.this).inflate(R.layout.listview_of_check, null);
                tvCheckItem = (TextView) convertView.findViewById(R.id.tvCheckItem);
                convertView.setTag(tvCheckItem);
            } else {
                tvCheckItem = (TextView) convertView.getTag();
            }
            // 设置VIEW状态
            final CheckItem cItem = checkList.get(position);
            tvCheckItem.setText(cItem.getCheckName());
            tvCheckItem.setBackgroundColor(cItem.getBackgroundColor(position));
            tvCheckItem.setTextColor(cItem.getTextColor());

            return convertView;
        }
    }


    private void refreshBtn() {
        if (!mIsChecking) {
            mBtnCheckPass.setVisibility(View.INVISIBLE);
            mBtnCheckError.setVisibility(View.INVISIBLE);

            mBtnCheckBegin.setVisibility(View.VISIBLE);
            mBtnCheckNext.setVisibility(View.INVISIBLE);
            mBtnCheckFinish.setVisibility(View.INVISIBLE);
            return;
        }
        if (mCurrentCheck + 1 <= checkList.size()) {
            mBtnCheckPass.setVisibility(View.VISIBLE);
            mBtnCheckError.setVisibility(View.VISIBLE);

            mBtnCheckBegin.setVisibility(View.INVISIBLE);
            mBtnCheckNext.setVisibility(View.VISIBLE);
            mBtnCheckFinish.setVisibility(View.INVISIBLE);

        } else {
            //如果是在显示 结果
            mBtnCheckPass.setVisibility(View.INVISIBLE);
            mBtnCheckError.setVisibility(View.INVISIBLE);

            mBtnCheckBegin.setVisibility(View.INVISIBLE);
            mBtnCheckNext.setVisibility(View.INVISIBLE);
            mBtnCheckFinish.setVisibility(View.VISIBLE);
        }
    }

    class CheckItem {
        private int checkState;
        private int checkNameRES;

        public CheckItem(int nameRes) {
            checkState = NORMAL;
            checkNameRES = nameRes;
        }

        public String getCheckName() {
            return getResources().getString(this.checkNameRES);
        }

        public int getCheckState() {
            return checkState;
        }

        public void setCheckState(int checkState) {
            this.checkState = checkState;
        }

        public int getBackgroundColor(int position) {
            if (position == mCurrentCheck) {
                //蓝色
                return Color.rgb(54, 194, 242);
            }

            switch (this.checkState) {
                case NORMAL:
                    return Color.WHITE;
                case PASS:
                    return Color.GREEN;
                case ERROR:
                    return Color.RED;
                default:
                    return Color.WHITE;
            }
        }

        public int getTextColor() {
            switch (this.checkState) {
                case NORMAL:
                    return Color.BLACK;
                default:
                    return Color.WHITE;
            }
        }
    }

    private String getSendAction(Intent intent) {
        byte[] receiveData = intent.getByteArrayExtra(UNIFORM_SEND);
        for (String action : SELF_CHECK_ACTIONS.keySet()) {
            if (new Verifier().compareHead(receiveData, SELF_CHECK_ACTIONS.get(action)) == true) {
                return action;
            }
        }
        return null;
    }

    // 获取当前的Fragment
    @NonNull
    public CheckFragment getFragment(int position) {
        switch (position) {
            case 0:
                return CheckFragmentBarrier.newInstance(
                        R.string.selfCheckExplainBarrier, R.drawable.check_barrier);
            case 1:
                return CheckFragmentRFID.newInstance(
                        R.string.selfCheckExplainRFID, R.drawable.check_rfid);
            case 2:
                return CheckFragmentMag.newInstance(
                        R.string.selfCheckExplainMag, R.drawable.check_mag);
            case 3:
                return CheckFragmentInfra.newInstance(
                        R.string.selfCheckExplainInfra, R.drawable.check_infra);
            case 4:
                return CheckFragmentMachineBtn.newInstance(
                        R.string.selfCheckExplainBtn, R.drawable.check_btn);
            case 5:
                return CheckFragmentUltra.newInstance(
                        R.string.selfCheckExplainUltra, R.drawable.check_ultra);
            case 6:
                return CheckFragmentMotor.newInstance(
                        R.string.selfCheckExplainMotor, 0);
            default:
                return CheckResultFragment.newInstance(getResult());
        }
    }

    //显示 检测结果
    public String[] getResult() {
        List<String> result = new ArrayList<>();
        for (CheckItem ci : checkList) {
            switch (ci.getCheckState()) {
                case PASS:
                    result.add(ci.getCheckName() + " [通 过]");
                    break;
                case ERROR:
                    result.add(ci.getCheckName() + " [出 错]");
                    break;
                default:
                    result.add(ci.getCheckName() + " [未 检 测]");
                    break;
            }
        }
        return (String[]) result.toArray();
    }

    private static class SelfCheckHandler extends Handler {
        private WeakReference<SelfCheckActivity> activityWeakReference;

        public SelfCheckHandler(SelfCheckActivity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            SelfCheckActivity activity = activityWeakReference.get();
            if (activity == null || msg.obj == null) {
                return;
            }
            UsbData data = (UsbData) msg.obj;
            UsbEvent event = data.getEvent();
            byte[] dataReceive = data.getDataReceive();
            switch (event) {
                case UsbReceive:
                    activity.mCurrentCheckFragment.onReceiveArmData(data);
                    break;
                case UsbSendSuccess:

                    break;
            }
        }
    }

    private void changeFragment(final CheckFragment cf) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.flSelfCheckFragment, cf);
        fragmentTransaction.commit();

        refreshBtn();
        mCurrentCheckFragment = cf;

        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                if (!cf.startSelfCheck()) {
                    T.show("开始自检失败,请重试");
                }
            }
        });
    }

    private void changeFragment(int position) {
        changeFragment(getFragment(position));
    }


}
