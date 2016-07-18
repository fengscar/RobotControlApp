package com.feng.Activities;


import android.os.Message;
import android.widget.*;
import com.feng.Base.BaseActivity;
import com.feng.Constant.I_Parameters;
import com.feng.Constant.ArmProtocol;
import com.feng.RSS.R;
import com.feng.Schedule.ScheduleClient;
import com.feng.Schedule.ScheduleServerInfo;
import com.feng.Utils.L;
import com.feng.RobotApplication;
import com.feng.Utils.SP;
import com.feng.Utils.T;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class EditRcsActivity extends BaseActivity implements I_Parameters, ArmProtocol {

    private Switch pointSwitch, bgmSwitch, alarmSwitch;
    private Switch mapModelSwitch, sortTypeSwitch;
    private Switch autoEnterSwitch;
    private EditText mEtIpAddress, mEtIpPort;
    private SeekBar columnNumberSeeker;
    private Button cancelToMenu;
    private ScheduleClient mScheduleClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_edit_rss);

        ButtonListener btnLis = new ButtonListener();
        // 返回上个界面
        findViewById(R.id.btnUniformTitleLeft).setOnClickListener(btnLis);
        // 扫描当前网段下1-255的IP地址,尝试连接并验证登录信息
        findViewById(R.id.btnScanLAN).setOnClickListener(btnLis);
        //测试当前输入框中的IP地址
        findViewById(R.id.btnTestConnect).setOnClickListener(btnLis);

        ((TextView) findViewById(R.id.tvUniformTitleCenter)).setText(R.string.para_setting);
        findViewById(R.id.btnUniformTitleRight).setVisibility(View.INVISIBLE);


        //初始化 各个Switch,绑定监听
        pointSwitch = (Switch) findViewById(R.id.audio_switch_point);
        bgmSwitch = (Switch) findViewById(R.id.audio_switch_BGM);
        alarmSwitch = (Switch) findViewById(R.id.audio_switch_alarm);
        mapModelSwitch = (Switch) findViewById(R.id.change_map_model);
        sortTypeSwitch = (Switch) findViewById(R.id.sort_type);
        autoEnterSwitch = (Switch) findViewById(R.id.autoEnterWorkspace);

        // 初始化 editText, 绑定监听
        mEtIpAddress = (EditText) findViewById(R.id.etIpAddress);
        mEtIpPort = (EditText) findViewById(R.id.etIpPort);
        EditTextListener etLis = new EditTextListener();
        mEtIpAddress.addTextChangedListener(etLis);

        // 获取SP的值 ,初始化 状态 (  要提前在 绑定监听前 )
        initValue();


        SwitchListener lis = new SwitchListener();
        pointSwitch.setOnCheckedChangeListener(lis);
        bgmSwitch.setOnCheckedChangeListener(lis);
        alarmSwitch.setOnCheckedChangeListener(lis);
        mapModelSwitch.setOnCheckedChangeListener(lis);
        sortTypeSwitch.setOnCheckedChangeListener(lis);
        autoEnterSwitch.setOnCheckedChangeListener(lis);

        // 初始化SEEKBAR
        initSeekbar();

        initNotifier();
    }

    private void initNotifier() {
        mScheduleClient = ScheduleClient.getInstance();
        mScheduleClient.putHandler(EditRcsActivity.class.getSimpleName(), new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ScheduleClient.SOCKET_CONNECT:
                        T.show("调度系统连接成功");
                        break;
                    case ScheduleClient.SOCKET_DISCONNECT:
                        T.show("调度系统连接失败");
                        break;
                }
            }
        });
    }

    private void initValue() {
        Context context = EditRcsActivity.this;
        pointSwitch.setChecked((boolean) SP.get(context, AUDIO_POINT, false));
        bgmSwitch.setChecked((boolean) SP.get(context, AUDIO_BGM, false));
        alarmSwitch.setChecked((boolean) SP.get(context, AUDIO_ALARM, false));
        mapModelSwitch.setChecked((boolean) SP.get(context, IS_MAP_MODEL, false));
        sortTypeSwitch.setChecked("NODE_TYPE".equals(SP.get(context, SORT_TYPE, "NODE_TYPE")));
        autoEnterSwitch.setChecked((boolean) SP.get(context, AUTO_ENTER_WORKSPACE, false));

        mEtIpAddress.setText((String) SP.get(context, IP_ADDRESS, ""));
        mEtIpPort.setText(String.valueOf((int) SP.get(context, IP_PORT, 12250)));
    }

    private void initSeekbar() {
        columnNumberSeeker = (SeekBar) findViewById(R.id.columnNumberSeeker);
        //获取默认值 [1,max]   ->  [0,max-1]s
        int index = (int) SP.get(getApplicationContext(), COLUMN_NUM, 1);
        columnNumberSeeker.setProgress(seekBarInit(index, 4));
        columnNumberSeeker.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                L.e("这里设置SP 中的列数为" + seekBarAdjuster(seekBar.getProgress(), 4));
                SP.put(EditRcsActivity.this, COLUMN_NUM, seekBarAdjuster(seekBar.getProgress(), 4));
                //设置当前进度到  固定点上   每+1 加 2份进度(tempX2)
                seekBar.setProgress(seekBarInit(seekBarAdjuster(seekBar.getProgress(), 4), 4));
            }

            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                Drawable dab = null;
                switch (seekBarAdjuster(seekBar.getProgress(), 4)) {
                    case 1:
                        dab = EditRcsActivity.this.getResources().getDrawable(HorizontalNumberSeekerbar.ONE);
                        break;
                    case 2:
                        dab = EditRcsActivity.this.getResources().getDrawable(HorizontalNumberSeekerbar.TWO);
                        break;
                    case 3:
                        dab = EditRcsActivity.this.getResources().getDrawable(HorizontalNumberSeekerbar.THREE);
                        break;
                    case 4:
                        dab = EditRcsActivity.this.getResources().getDrawable(HorizontalNumberSeekerbar.FOUR);
                        break;

                    default:
                        break;
                }
                if (dab != null) {
                    seekBar.setThumb(dab);
                }

            }
        });
    }

    /**
     * 把 0-100 转为 0-max
     *
     * @param currentValue 当前值(0-100)
     * @param maxSeek      seekbar的最大值 [ 1 , x ]
     * @return
     */
    private int seekBarAdjuster(int currentValue, int maxSeek) {
        // 每份
        float temp = 100.0F / ((float) (maxSeek - 1) * 2);
        for (int i = 0; i < maxSeek; i++) {
            if (currentValue >= temp * (i * 2 - 1) && currentValue < temp * (i * 2 + 1)) {
                L.e("当前最大值为: " + maxSeek + "每份为 " + temp + " 当前值为:" + currentValue + " 返回:" + i);
                return i + 1;
            }
        }
        return 0;
    }

    /**
     * 设置当前的 seekbar 的进度 1 -max 转为 0-100
     *
     * @return
     */
    private int seekBarInit(int index, int maxSeek) {
        int tempX2 = (int) (100.0F * 2 / ((maxSeek - 1) * 2));
        return (index - 1) * tempX2;

    }

    class ButtonListener implements OnClickListener {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnUniformTitleLeft:
                    EditRcsActivity.this.finish();
                    break;
                case R.id.btnScanLAN:
                    break;
                case R.id.btnTestConnect:
                    // 获取当前的EditText中的IP地址和端口，尝试连接该地址
                    String ipAdd = mEtIpAddress.getText().toString();
                    if (!ipAdd.isEmpty()) {
                        String reIP = "^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])" +
                                "\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)" +
                                "\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)" +
                                "\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$";
                        if (!ipAdd.matches(reIP)) {
                            T.show("IP地址格式错误,请检查输入");
                            return;
                        }
                    }
                    int port = Integer.valueOf(mEtIpPort.getText().toString());
                    ScheduleServerInfo scheduleServerInfo = ScheduleServerInfo.getInstance();
                    scheduleServerInfo.setIp(ipAdd);
                    scheduleServerInfo.setPort(port);
                    RobotApplication.getScheduleClient().reconnect();
                    break;

                default:
                    break;
            }
        }

    }

    class SwitchListener implements OnCheckedChangeListener {

        public void onCheckedChanged(CompoundButton buttonView,
                                     boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.audio_switch_point:
                    SP.put(RobotApplication.getContext(), AUDIO_POINT, isChecked);
                    T.show(isChecked ? "到点提示开启" : "到点提示关闭");
                    break;

                case R.id.audio_switch_BGM:
                    SP.put(RobotApplication.getContext(), AUDIO_BGM, isChecked);
                    T.show(isChecked ? "背景音乐开启" : "背景音乐关闭");
                    break;

                case R.id.audio_switch_alarm:
                    SP.put(RobotApplication.getContext(), AUDIO_ALARM, isChecked);
                    T.show(isChecked ? "报警提示开启" : "报警提示关闭");
                    break;

                case R.id.change_map_model:
                    SP.put(RobotApplication.getContext(), IS_MAP_MODEL, isChecked);
                    T.show(isChecked ? "切换为地图模式" : "切换为列表模式");
                    break;

                case R.id.sort_type:
                    SP.put(RobotApplication.getContext(), SORT_TYPE, isChecked ? "NODE_TYPE" : "ID");
                    T.show(isChecked ? "排序方式:按类型" : "排序方式:按编号");
                    break;

                case R.id.autoEnterWorkspace:
                    SP.put(RobotApplication.getContext(), AUTO_ENTER_WORKSPACE, isChecked);
                    T.show(isChecked ? "自动进入工作区:开启" : "自动进入工作区:关闭");
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * IP 输入栏的输入完成监听
     *
     * @author 福建省和创伟业智能科技有限公司
     * @创建时间 2016-1-29 下午3:11:53
     */
    class EditTextListener implements TextWatcher {
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if (getPointNum(s.toString()) > 3) {
                T.show("错误的IP地址");
            }
        }

        public void afterTextChanged(Editable s) {
            String ipStr = s.toString();
            // 如果 " ." 等于3个 && . 后面还有str
            //1 .延迟2秒后 关闭软键盘
            // 2. 保存 到SP
            if (getPointNum(ipStr) == 3 && ipStr.length() > ipStr.lastIndexOf(".") + 3) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        mEtIpAddress.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mEtIpAddress.getWindowToken(), 0);
                    }
                }, 100);
                String beforeAddress = (String) SP.get(EditRcsActivity.this, IP_ADDRESS, " ");
                SP.put(EditRcsActivity.this, IP_ADDRESS, s.toString());
                SP.put(EditRcsActivity.this, IP_PORT, Integer.parseInt(mEtIpPort.getText().toString()));
                if (!beforeAddress.equals(s.toString())) { //如果IP地址较之前改变, 重连
                    intentDealer.sendIntent(SOCKET_RECONNECT, null, null);
                }
            }
        }

        private int getPointNum(String str) {
            int num = 0;
            for (int i = 0; i < str.length(); i++) {
                if (str.substring(i, i + 1).equals(".")) {
                    num++;
                }
            }
            return num;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        SP.put(EditRcsActivity.this, IP_ADDRESS, mEtIpAddress.getText().toString());
    }
}
