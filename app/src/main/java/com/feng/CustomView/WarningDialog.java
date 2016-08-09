package com.feng.CustomView;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import com.feng.Usb.ArmProtocol;
import com.feng.Constant.I_Parameters;
import com.feng.RSS.R;
import com.feng.Utils.IntentDealer;
import com.feng.Utils.L;
import com.feng.Utils.Transfer;

import java.util.HashMap;
import java.util.Map;
/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间  2016-1-23 上午12:31:36
 */
public class WarningDialog extends Dialog implements ArmProtocol,I_Parameters{

	private Context context;
	//存储当前的 报警 信息 ( key 为 action) 这样的话
	private Map<String,LinearLayout> warningMap; 
	/**
	 * XML 中的内容
	 */
	private LinearLayout warningContent;  // 警报的显示位置
	private TextView tvTitle; //  	// 设置Title的Tv
	private Button btnConfirm,btnClear;  // dialog底部的Button
	private WarningDialogCallback onChangeListenner;

	public WarningDialog(Context context) {
		this(context, R.style.warning_dialog,null);
	}
	public WarningDialog(Context context, int theme,
			WarningDialogCallback onChangeListenner) {
		super(context, theme);
		this.context=context;
		this.init(R.layout.dialog_warning,onChangeListenner);
	}


	/**
	 * 返回一个Dialog , 作为Activity的成员对象,方便Activity中操作
	 * @return dialog对象
	 */
	private void init(int resourseID,WarningDialogCallback okCallback){
		// 必须先 show 再 setContentView
		this.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);  
		this.show();
		// ContentView是总的 dialog
		this.setContentView(resourseID);
		warningMap=new HashMap<String,LinearLayout>();
		// warningContent是 dialog中间的警报显示
		warningContent=(LinearLayout) this.findViewById(R.id.dialog_content);

		tvTitle=(TextView)this.findViewById(R.id.dialogTitle);
		tvTitle.setText("警告!");

		btnConfirm=(Button)this.findViewById(R.id.btn_dialog_confirm);
		if( okCallback !=null ){
			btnConfirm.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					//					okCallback.onWarningClear(dialog)
				}
			});
		}

		btnConfirm.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				WarningDialog.this.dismiss();
			}
		});
		btnClear=(Button)this.findViewById(R.id.btn_dialog_clear);
		btnClear.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				warningMap.clear();
				warningContent.removeAllViews();
				onChangeListenner.onWarningChange(0);
				WarningDialog.this.dismiss();
			}
		});
		//TODO 隐藏
		this.dismiss();
	}
	/**
	 * 根据当前的Action,显示内置的错误代码, 并将解释文本显示
	 * @param protocolActions  协议中的Actions
	 * @param explainStr  解释文本
	 */
	public void addWarning(boolean isError,String protocolActions,String explainStr,View.OnClickListener operate){
		/**
		 * 如果已经有该类型报警,只播放语音提示
		 */
		if(	warningMap.containsKey(protocolActions) ){
			//			new IntentDealer().sendIntent()
			return ;
		}
		/**
		 *  每条Waring 对应一个warn
		 */
		LayoutInflater inflater=LayoutInflater.from(context);
		LinearLayout linearLayout=(LinearLayout) inflater.inflate(R.layout.dialog_warning_content,null);
		linearLayout.setId(warningContent.getChildCount());
		linearLayout.setBackgroundResource(R.drawable.warning_yellow);
		TextView tvErrCode=(TextView)linearLayout.findViewById(R.id.tvErrorCode);
		TextView tvErrExplanation=(TextView)linearLayout.findViewById(R.id.tvErrorExplanation);
		ImageView ivErrIcon=(ImageView)linearLayout.findViewById(R.id.ivErrorIcon);
		ScrollView svContent=(ScrollView)findViewById(R.id.svContent);
		//错误代码 tv
		try {
			int errCode=(int) WarningCode.get(protocolActions);
			tvErrCode.setText(" 错误代码 :  "+ String.valueOf(errCode)+"   ");
			new IntentDealer(new Transfer()).sendTtsIntent(UNIFORM_TTS,
					WarningTTS.get(protocolActions));
		} catch (Exception e) {
			e.printStackTrace();
			L.e("WarningDialog未找到对应的错误代码,或者未找到对应的错误语音");
		}
		// 错误解释文本 tv
		tvErrExplanation.setText(explainStr);
		// 图标 iv ( 分为警报 和 错误)
		Drawable iconDrawable=null;
		if(isError==true ){
			iconDrawable=context.getResources().getDrawable(R.drawable.error);
			iconDrawable.setBounds(0, 0, 100, 100);
		}else{
			iconDrawable=context.getResources().getDrawable(R.mipmap.drawer_menu_warn);
			iconDrawable.setBounds(0, 0, 75, 75);
		}
		ivErrIcon.setImageDrawable(iconDrawable);
		ivErrIcon.setOnClickListener(operate);

		svContent.smoothScrollTo(0, 
				svContent.getChildAt(0).getMeasuredHeight()-svContent.getScrollY());

		warningMap.put(protocolActions,linearLayout);
		// 添加到Content中
		warningContent.addView(linearLayout);
		//		L.e(""+warningContent.getChildCount());

		if( onChangeListenner !=null ){
			onChangeListenner.onWarningChange( this.getWarningCount() );
			onChangeListenner.onAddWarning( WarningTTS.get(protocolActions));
		}
	}
	public void addWarning(View view){
		warningContent.addView(view);
	}
	/**
	 * 接收到 ARM的取消报警后, 将 该警报删除
	 * @param protocalActions
	 */
	public void removeWarning(String protocalActions){
		try {
			// 移除view
			warningContent.removeView( warningMap.get(protocalActions));
			//移除 map中对应的 值
			warningMap.remove(protocalActions);
			onChangeListenner.onWarningChange( this.getWarningCount() );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public int getWarningCount(){
		if( warningMap!=null){
			return warningMap.size();
		}else{
			return 0;
		}
	}
	public void showDialog(){
		this.show();
		L.e("当前有 :" + getWarningCount()+"个报警信息");
	}
	public void clearWarning(){
		warningMap.clear();
		onChangeListenner.onWarningChange( this.getWarningCount());
	}
	public void setOnWarningChangeListenner(WarningDialogCallback wdcb){
		this.onChangeListenner=wdcb;
	}
}
