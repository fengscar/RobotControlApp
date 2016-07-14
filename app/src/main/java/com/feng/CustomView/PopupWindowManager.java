package com.feng.CustomView;

import com.feng.RSS.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.PopupWindow;

public class PopupWindowManager {
	private Context mContext;
	private PopupWindow popupWindow;
	public PopupWindowManager(Context cx) {
		this.mContext=cx;
	}
	/**
	 * 
	 * @param view     弹出窗的父View
	 * @param resID		弹出窗的XML布局文件
	 * @param x			偏移量X (相对于左上角)
	 * @param y			偏移量Y
	 * @param buttonIDs	 各个按键的ID
	 * @param liss		各个按键的点击监听器(顺序与ID对应)
	 */
	public void showPopupWindowAsDropDown(View view,int resID,int x,int y,int []buttonIDs,OnClickListener ...liss) {
		// 一个自定义的布局，作为显示的内容
		View contentView = LayoutInflater.from(mContext).inflate(resID, null);
		// 设置按钮的点击事件
		int i=0;
		for(OnClickListener onClick:liss){
			contentView.findViewById(buttonIDs[i]).setOnClickListener(onClick);
			i++;
		}
		popupWindow = new PopupWindow(contentView,
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
		popupWindow.setTouchable(true);
		// 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
		popupWindow.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.round_green_gradient));
		popupWindow.setOutsideTouchable(true);
		// 设置好参数之后再show
		// 如果超过 屏幕底部 ,则显示在父VIew的上方, 这个是 shaoAsDropDown中实现的
		popupWindow.showAsDropDown(view,x,y);
	}
	/**
	 * 关闭 Popup
	 */
	public void close(){
		this.popupWindow.dismiss();
	}
}
