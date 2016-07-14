/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间  2016-3-9 下午3:11:58
 */
package com.feng.CustomView;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.feng.Constant.I_Parameters;
import com.feng.RSS.R;

public class ArmSettingView extends LinearLayout implements I_Parameters{
	public interface ArmSettingBtnCallback{
		void applyConfig(int currentNum);
	}
	
	private ArmSettingBtnCallback asbc;
	
	private TextView mainText;
	private TextView subText;
	private EditText etValue;
	private Button btnAdd;
	private Button btnSub;

	private int minNum;
	private int maxNum;
	private int defaultNum;
	private int currentNum;
	private String viewLimit;

	public ArmSettingView(Context context){
		this(context, null);
	}

	public ArmSettingView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public ArmSettingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater.from(context).inflate(R.layout.layout_arm_setting, this, true);
		mainText=(TextView)findViewById(R.id.tvMain);
		subText=(TextView)findViewById(R.id.tvSubtitle);
		etValue=(EditText)findViewById(R.id.etValue);
		btnAdd=(Button)findViewById(R.id.btnAdd);
		btnSub=(Button)findViewById(R.id.btnSub);
		//TODO 是否设置为可用? 可用 键盘输入似乎不好控制
		etValue.setEnabled(false);
		etValue.setFocusable(false);

		initValue(context, attrs, defStyle);  
		initListenner();
	}

	private void initValue(Context context, AttributeSet attrs, int defStyle) {
		TypedArray a = context.getTheme().obtainStyledAttributes(
				attrs, R.styleable.ArmSetting, defStyle, 0);  
		int n = a.getIndexCount();  

		for (int i = 0; i < n; i++)  
		{  
			int index = a.getIndex(i);  
			switch (index)  
			{  
			case R.styleable.ArmSetting_mainText:  
				mainText.setText( a.getString(index) ); 
				break;  
			case R.styleable.ArmSetting_subText:  
				subText.setText( a.getString(index) ); 
				break;  
			case R.styleable.ArmSetting_minNum: 
				this.minNum=a.getInt(index, 1);
				break;  
			case R.styleable.ArmSetting_maxNum:  
				this.maxNum=a.getInt(index, 10);
				break;  
			case R.styleable.ArmSetting_defaultNum:  
				this.defaultNum=a.getInt(index, 1);
				//TODO 应该放在 初始化 Activity中
				currentNum=a.getInt(index,1);
				etValue.setText(""+a.getInt(index, 1));
				break;  
			case R.styleable.ArmSetting_limit:
				this.viewLimit=a.getString(index);
			}  
		}  
		//如果没有子标题,( 描述该设置) ,
		if( subText.getText().equals("")){
			subText.setVisibility(GONE);
		}
		a.recycle();
	}
	

	private void initListenner(){
		btnAdd.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if( event.getAction()==MotionEvent.ACTION_DOWN
						|| event.getAction()==MotionEvent.ACTION_MOVE ){
					add();
				}
				if( event.getAction()==MotionEvent.ACTION_UP){
					asbc.applyConfig(currentNum);
				}
				return false;
			}
		});
		btnSub.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if( event.getAction()==MotionEvent.ACTION_DOWN
						|| event.getAction()==MotionEvent.ACTION_MOVE ){
					sub();
				}
				if( event.getAction()==MotionEvent.ACTION_UP ){
					asbc.applyConfig(currentNum);
				}
				return false;
			}
		});
	}
	
	public void setCallback(ArmSettingBtnCallback  iasbc){
		this.asbc=iasbc;
	}
	
	/**
	 * 根据权限来 设置 隐藏,或者不可设置( 不显示 + - 按键..)
	 * 用户权限 分为 1 , 3 , 5
	 */
	public void putUserLimit(String userGroup){
		// 获取 指定用户 对本控件的 权限
		int userLimit=2;
		if( userGroup.equals(USER_PROGRAMMER)){
			userLimit=0;
		}else if( userGroup.equals(USER_SERVICER)){
			userLimit=1;
		}
		char limit=viewLimit.charAt(userLimit);
		switch (limit) {
		case 'W':
			//不操作
			break;
		case 'R':	// 不可写, 隐藏 按键
			btnAdd.setVisibility(INVISIBLE);
			btnSub.setVisibility(INVISIBLE);
			break;
		case '-':// 不可读 , 隐藏 整个控件?
		default:
			this.setVisibility(GONE);
			limit='-';
			break;
		}
	}
	public void reset(){
		currentNum=defaultNum;
		this.etValue.setText(String.valueOf(currentNum));
	}

	public void add(){
		if( currentNum < maxNum){
			currentNum++;
			etValue.setText( String.valueOf(currentNum ) );
		}
	}

	public void sub(){
		if( currentNum > minNum){
			currentNum--;
			etValue.setText( String.valueOf(currentNum ) );
		}
	}
}

