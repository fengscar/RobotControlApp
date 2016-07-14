package com.feng.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import com.feng.Base.BaseActivity;
import com.feng.Constant.I_Parameters;
import com.feng.RSS.R;
import com.feng.RobotApplication;
import com.feng.Utils.SP;

public class MenuActivity extends BaseActivity implements I_Parameters {
    private GridView menuGrid;
    private String userGroup;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // 禁用不需要的按键
        initUser();
        initView();
    }

    private void initUser() {
        // 获取传递过来的用户组
        userGroup = getIntent().getStringExtra(LAST_LOGIN_USER);
        //获取用户权限
        switch (userGroup) {
            case USER_CUSTOMER:
                //			btnEditMap.setEnabled(false);
                break;

            case USER_SERVICER:

                break;

            case USER_PROGRAMMER:

                break;

            default:
                break;
        }

    }

    private void initView() {
        findViewById(R.id.btnUniformTitleLeft).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                MenuActivity.this.finish();
            }
        });
        findViewById(R.id.btnUniformTitleRight).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //准备删除登录信息...
                SP.remove(RobotApplication.getContext(), I_Parameters.LAST_LOGIN_TIME);
                MenuActivity.this.finish();
            }
        });
        ((TextView) findViewById(R.id.tvUniformTitleCenter)).setText(R.string.setting);

        menuGrid = (GridView) findViewById(R.id.menuGridView);
        menuGrid.setAdapter(new MenuGridAdapter());
    }

    private int[] btnTexts = new int[]{
            R.string.menu_edit_map, R.string.menu_edit_speaker,
            R.string.menu_edit_rss, R.string.menu_edit_arm,
            R.string.menu_edit_password, R.string.menu_self_check,
            R.string.menu_edit_version,
    };
    private int[] btnDrawables = new int[]{
            R.drawable.edit_map, R.drawable.edit_spearker,
            R.drawable.edit_rss, R.drawable.edit_paras,
            R.drawable.edit_pwd, R.drawable.menu_self_check
            , R.drawable.edit_version
    };

    class MenuGridAdapter extends BaseAdapter {
        public int getCount() {
            return btnTexts.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (null == convertView) {
                LayoutInflater factory = LayoutInflater.from(MenuActivity.this);
                View mView = factory.inflate(R.layout.gridview_of_menu, null);
                convertView = mView;
            }

            Button btn = (Button) convertView.findViewById(R.id.btnMenu);
            btn.setTag(position);
            initButton(btn);
            btn.setOnClickListener(new MenuOnClickListener());

            return convertView;
        }

    }

    private void initButton(Button btn) {
        //图标
        int btnTag = (int) btn.getTag();
        Drawable map = getResources().getDrawable(btnDrawables[btnTag]);
        map.setBounds(0, 0, 100, 100);
        btn.setCompoundDrawables(map, null, null, null);
        //文本
        String btnText = getResources().getString(btnTexts[btnTag]);
        btn.setText(btnText);
        btn.setTextColor(Color.BLACK);
    }

    class MenuOnClickListener implements OnClickListener {
        public void onClick(View v) {
            int tag = Integer.parseInt(v.getTag().toString());
            Intent intent = new Intent();
            // 设置当前用户权限
            intent.setAction(userGroup);
            switch (tag) {
                //编辑地图
                case 0:
                    intent.setClass(MenuActivity.this, EditWorkspaceActivity.class);
                    startActivity(intent);
                    break;
                // 语音设置
                case 1:
                    intent.setClass(MenuActivity.this, EditSpeakerActivity.class);
                    startActivity(intent);
                    break;
                //本机设置
                case 2:
                    intent.setClass(MenuActivity.this, EditRcsActivity.class);
                    startActivity(intent);
                    break;
                // ARM设置 ,需要输入权限
                case 3:
                    intent.setClass(MenuActivity.this, EditArmActivity.class);
                    startActivity(intent);
                    break;
                // 修改密码
                case 4:
                    intent.setClass(MenuActivity.this, EditPwdActivity.class);
                    startActivity(intent);
                    break;
                //自检测室
                case 5:
                    intent.setClass(MenuActivity.this, SelfCheckActivity.class);
                    startActivity(intent);
                    break;
                //系统设置
                case 6:
                    intent.setClass(MenuActivity.this, EditVersionActivity.class);
                    startActivity(intent);
                    break;

                default:
                    break;
            }
        }
    }
}
