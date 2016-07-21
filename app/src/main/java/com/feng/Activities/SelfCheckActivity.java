package com.feng.Activities;


import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.feng.Base.BaseActivity;
import com.feng.Constant.ArmProtocol;
import com.feng.Constant.I_Parameters;
import com.feng.Fragments.SelfCheck.*;
import com.feng.RSS.R;
import com.feng.Utils.L;
import com.feng.Utils.Verifier;

import java.util.ArrayList;
import java.util.Arrays;

public class SelfCheckActivity extends BaseActivity implements I_Parameters, ArmProtocol {
    public final static int NORMAL = 0;
    public final static int SELECT = 1;
    public final static int OK = 2;
    public final static int ERROR = 3;
    //用户 权限等级
    private String userGroup;
    // 控制按键
    private Button btnOk, btnError, btnNext;

    //当前项目
    private int currentCheck = -1;
    private boolean startCheck = false;
    //自检选项
    private ListView lvCheckItem;
    public ArrayList<CheckItem> checkList;
    public ArrayList<CheckFragment> checkFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_self_check);
        // TODO 这里简单的把  用户作为action传递过来, 是否需要改成使用 BUNDLE
        userGroup = this.getIntent().getAction();
        initView();
    }

    @Override
    protected void onStart() {
        super.configureReceiver(new SelfCheckReceiver());
        super.onStart();
    }

    private void initView() {
        ((TextView) findViewById(R.id.tvUniformTitleCenter)).setText(R.string.menu_self_check);
        findViewById(R.id.btnUniformTitleRight).setVisibility(View.INVISIBLE);
        OnClickListener lis = new SelfCheckOnClickListenner();
        //		返回按键
        findViewById(R.id.btnUniformTitleLeft).setOnClickListener(lis);
        //  check操作按键
        btnOk = (Button) findViewById(R.id.btnCheckOK);
        btnError = (Button) findViewById(R.id.btnCheckError);
        btnNext = (Button) findViewById(R.id.btnCheckNext);
        btnOk.setOnClickListener(lis);
        btnError.setOnClickListener(lis);
        btnNext.setOnClickListener(lis);

        //初始化数据 ListView 列表
        checkList = new ArrayList<CheckItem>();
        checkList.add(new CheckItem(R.string.selfCheckListBarrier));
        checkList.add(new CheckItem(R.string.selfCheckListRFID));
        checkList.add(new CheckItem(R.string.selfCheckListMag));
        checkList.add(new CheckItem(R.string.selfCheckListInfra));
        checkList.add(new CheckItem(R.string.selfCheckListBtn));
        checkList.add(new CheckItem(R.string.selfCheckListUltra));
        checkList.add(new CheckItem(R.string.selfCheckListMotor));

        lvCheckItem = (ListView) findViewById(R.id.lvSelfCheckItem);
        lvCheckItem.setAdapter(new SelfCheckListViewAdapter());
        //初始化 Fragment
        checkFragment = new ArrayList<CheckFragment>();
        checkFragment.add(CheckFragmentBarrier.newInstance(
                R.string.selfCheckExplainBarrier, R.drawable.check_barrier));
        checkFragment.add(CheckFragmentRFID.newInstance(
                R.string.selfCheckExplainRFID, R.drawable.check_rfid));
        checkFragment.add(CheckFragmentMag.newInstance(
                R.string.selfCheckExplainMag, R.drawable.check_mag));
        checkFragment.add(CheckFragmentInfra.newInstance(
                R.string.selfCheckExplainInfra, R.drawable.check_infra));
        checkFragment.add(CheckFragmentMachineBtn.newInstance(
                R.string.selfCheckExplainBtn, R.drawable.check_btn));
        checkFragment.add(CheckFragmentUltra.newInstance(
                R.string.selfCheckExplainUltra, R.drawable.check_ultra));
        checkFragment.add(CheckFragmentMotor.newInstance(
                R.string.selfCheckExplainMotor, 0));
        checkFragment.add(new CheckResultFragment());

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        for (CheckFragment cFragment : checkFragment) {
            fragmentTransaction.add(R.id.flSelfCheckFragment, cFragment);
            fragmentTransaction.hide(cFragment);
        }
        fragmentTransaction.commit();

        refreshBtn();
    }

    class SelfCheckOnClickListenner implements OnClickListener {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnUniformTitleLeft:
                    SelfCheckActivity.this.finish();
                    break;
                case R.id.btnCheckOK:
                case R.id.btnCheckError:
                    if (v.getId() == R.id.btnCheckOK) {
                        checkFragment.get(currentCheck).setCheckResult(OK);
                    } else {
                        checkFragment.get(currentCheck).setCheckResult(ERROR);
                    }
                    // 不用break...继续操作
                case R.id.btnCheckNext:
                    if (startCheck == false) {
                        intentDealer.sendIntent(USB_SEND, SelfCheckStartStop, (byte) 0x01);
                        break;
                    }
                    //如果还未选择fragment, 跳转到下一个fragment
                    if (currentCheck + 2 < checkFragment.size()) {
                        changeFragment(checkFragment.get(++currentCheck));
                    } else if (currentCheck + 2 == checkFragment.size()) {
                        // 如果要输出结果...
                        currentCheck++;
                        String[] str = new String[checkFragment.size() - 1];
                        for (int i = 0; i < str.length; i++) {
                            str[i] = checkFragment.get(i).getCheckResult();
                        }
                        CheckResultFragment crf = (CheckResultFragment) checkFragment.get(currentCheck);
                        crf.setCheckResultStr(str);
                        changeFragment(crf);
                    } else {
                        // 保存结果 (重置 )
                        intentDealer.sendIntent(USB_SEND, SelfCheckStartStop, (byte) 0x00);
                    }
                    refreshList();
                    refreshBtn();
                    break;

                default:
                    break;
            }
        }
    }

    class SelfCheckReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            byte[] receiveData = intent.getByteArrayExtra(UNIFORM_RECEIVE);
            byte[] sendData = intent.getByteArrayExtra(UNIFORM_SEND);
            if (intent.getAction().equals(SEND_SUCCESS) &&
                    SELF_CHECK_START_STOP.equals(getSendAction(intent))) {
                //如果是   开启自检
                if (transfer.getBody(sendData)[0] == (byte) 0x01) {
                    startCheck = true;
                    if (currentCheck != -1) {
                        changeFragment(checkFragment.get(currentCheck));
                        checkFragment.get(currentCheck).startCheck();
                    } else {
                        changeFragment(checkFragment.get(++currentCheck));
                        checkFragment.get(currentCheck).startCheck();
                    }
                    refreshList();
                    //  停止自检
                } else {
                    startCheck = false;
                    currentCheck = -1;
                    ((CheckResultFragment) checkFragment.get(checkFragment.size() - 1)).saveCheckLog();
                }
                return;
            }
            if (intent.getAction() != USB_RECEIVE) {
                return;
            }
            L.e("SelfCheckOnReceive [" + intent.getAction() + "]-" +
                    "Rec:" + Arrays.toString(receiveData) + ",Send:" + Arrays.toString(sendData));
            if (currentCheck != -1) {
                checkFragment.get(currentCheck).onDataChange(intent);
            }
        }
    }

    private void changeFragment(CheckFragment targetFragment) {
        if (startCheck == true) {
            targetFragment.startCheck();
        }
        // 初始化 FragmentManage
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // 隐藏当前所有 Fragment
        for (CheckFragment cFragment : checkFragment) {
            fragmentTransaction.hide(cFragment);
        }
        fragmentTransaction.show(targetFragment);
        fragmentTransaction.commit();
        //
        refreshBtn();
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
            tvCheckItem.setBackgroundColor(cItem.getBackgroundColor());
            tvCheckItem.setTextColor(cItem.getTextColor());
            tvCheckItem.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    // 避免重复 切换
                    if (position != currentCheck) {
                        currentCheck = position;
                        changeFragment(checkFragment.get(position));
                        refreshList();
                        refreshBtn();
                    }
                }
            });

            return convertView;
        }
    }

    // 刷新界面上的 LISTVIEW
    private void refreshList() {
        for (int i = 0; i < checkList.size(); i++) {
            if (i == currentCheck) {
                checkList.get(i).setCheckState(SELECT);
            } else {
                checkList.get(i).setCheckState(NORMAL);
            }
        }
        lvCheckItem.setAdapter(new SelfCheckListViewAdapter());
    }

    private void refreshBtn() {
        if (startCheck == false) {
            btnOk.setVisibility(View.INVISIBLE);
            btnError.setVisibility(View.INVISIBLE);
            btnNext.setText("开始检测!");
            return;
        }
        //如果是最后一个 检测项目
        if (currentCheck + 2 <= checkFragment.size()) {
            btnOk.setVisibility(View.VISIBLE);
            btnError.setVisibility(View.VISIBLE);
            btnNext.setText("跳过");
        } else {
            btnOk.setVisibility(View.INVISIBLE);
            btnError.setVisibility(View.INVISIBLE);
            btnNext.setText("完成检测");
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

        public int getBackgroundColor() {
            switch (this.checkState) {
                case NORMAL:
                    return Color.WHITE;
                case SELECT:
                    //蓝色
                    return Color.rgb(54, 194, 242);
                case OK:
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

}
