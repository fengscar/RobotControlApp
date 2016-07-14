package com.feng.Adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import com.feng.RSS.R;
import com.feng.CustomView.IconButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fengscar on 2016/6/16.
 */
public class DrawerMenuAdapter extends BaseAdapter {
    public static final int BACK_BTN = 0;
    public static final int USB_BTN = 1;
    public static final int SCHEDULE_BTN = 2;
    public static final int POWER_BTN = 3;
    public static final int WARNNING_BTN = 4;


    private Context mContext;
    private List<Button> mBtnList;
    private IconButton btnBack, btnUsb, btnSchedule, btnPower, btnWarning;

    public DrawerMenuAdapter(Context context) {
        mContext = context;
        init();
    }

    private void init() {
        mBtnList = new ArrayList<>();

        btnBack = new IconButton(mContext, IconButton.LEFT, R.mipmap.btn_back);
        btnUsb = new IconButton(mContext, IconButton.LEFT, R.mipmap.drawer_menu_usb_disconnect);
        btnSchedule = new IconButton(mContext, IconButton.LEFT, R.mipmap.drawer_menu_schedule_disconnect);
        btnPower = new IconButton(mContext, IconButton.LEFT, R.mipmap.drawer_menu_power20);
        btnWarning = new IconButton(mContext, IconButton.LEFT, R.mipmap.drawer_menu_warn);

        btnBack.setText("返回");
        btnUsb.setText(R.string.usbDisconnect);
        btnSchedule.setText(R.string.ScheduleDisconnect);
        btnPower.setText(R.string.powerUnknown);


        mBtnList.add(btnBack);
        mBtnList.add(btnUsb);
        mBtnList.add(btnSchedule);
        mBtnList.add(btnPower);
        mBtnList.add(btnWarning);
    }

    @Override
    public int getCount() {
        return mBtnList.size();
    }

    @Override
    public Object getItem(int position) {
        return mBtnList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return mBtnList.get(position);
    }

    public void addButton(Button btn) {
        mBtnList.add(btn);
    }


    public void updateUsbState(boolean isConnected) {
        btnUsb.setIconDrawableID(isConnected ? R.mipmap.drawer_menu_usb_connect : R.mipmap.drawer_menu_usb_disconnect);
        btnUsb.setText(isConnected ? R.string.usbConnect : R.string.usbDisconnect);
    }

    public void updateWarningState(int warningCount) {
        if (warningCount == 0) {
            btnWarning.setIconDrawableID(R.mipmap.drawer_menu_warn);
            btnWarning.setVisibility(View.GONE);
        } else {
            btnWarning.setIconDrawableID(R.mipmap.drawer_menu_warn);
            btnWarning.setText(warningCount + " 条报警信息");
            btnWarning.setVisibility(View.VISIBLE);
        }
    }

    public void updatePowerState(int percent) {

        if (percent <= 100 && percent > 80) {
            btnPower.setIconDrawableID(R.mipmap.drawer_menu_power100);

        } else if (percent <= 80 && percent > 60) {
            btnPower.setIconDrawableID(R.mipmap.drawer_menu_power80);
        } else if (percent <= 60 && percent > 40) {
            btnPower.setIconDrawableID(R.mipmap.drawer_menu_power60);
        } else if (percent <= 40 && percent > 20) {
            btnPower.setIconDrawableID(R.mipmap.drawer_menu_power40);
        } else if (percent <= 20 && percent >= 0) {
            btnPower.setIconDrawableID(R.mipmap.drawer_menu_power20);
        } else {
            //无法获取电源信息
            btnPower.setIconDrawableID(R.mipmap.drawer_menu_power100);
            btnPower.setText(R.string.powerUnknown);
            return;
        }
        btnPower.setText(" " + percent + "%");
    }

    /**
     * 更新连接调度系统的状态
     * 3种状态... 未连接Socket, 已连接socket, 已成功Login
     *
     * @param isSocketConnect 是否成功链接到调度系统
     * @param isLogined       是否已登录
     */
    public void updateScheduleState(boolean isSocketConnect, boolean isLogined) {
        if (!isSocketConnect) {
            btnSchedule.setIconDrawableID(R.mipmap.drawer_menu_schedule_disconnect);
            btnSchedule.setText(R.string.ScheduleDisconnect);
        } else {
            if (isLogined) {
                btnSchedule.setIconDrawableID(R.mipmap.drawer_menu_schedule_logined);
                btnSchedule.setText(R.string.ScheduleLogin);
            } else {
                btnSchedule.setIconDrawableID(R.mipmap.drawer_menu_schedule_connect);
                btnSchedule.setText(R.string.ScheduleConnect);
            }
        }
    }

    public void setClickListnner(View.OnClickListener ocl){
        btnBack.setTag(BACK_BTN);
        btnUsb.setTag(USB_BTN);
        btnSchedule.setTag(SCHEDULE_BTN);
        btnPower.setTag(POWER_BTN);
        btnWarning.setTag(WARNNING_BTN);

        this.btnBack.setOnClickListener(ocl);
        this.btnUsb.setOnClickListener(ocl);
        this.btnSchedule.setOnClickListener(ocl);
        this.btnPower.setOnClickListener(ocl);
        this.btnWarning.setOnClickListener(ocl);
    }
}
