package com.feng.Utils;

import android.view.View;
import android.widget.EditText;
import com.feng.Constant.ArmProtocol;

import java.util.Arrays;
import java.util.List;

public class Verifier implements ArmProtocol{
	private final static String LOG = Verifier.class.getSimpleName();


	public  boolean isAnyNull(List<View> list, String[] str) {
		boolean flag = true;
		for (int i = 0; i < list.size(); i++) {
			if (((EditText) list.get(i)).getText().toString().length() == 0) {
				T.show("请输入 " + str[i]);
				flag = false;
				break;
			}
		}
		return flag;
	}

	public  boolean isSomeNull(List<View> list, int... ins) {
		boolean flag = true;
		for (int i : ins) {
			if (((EditText) list.get(i)).getText().toString().length() == 0) {
				T.show("输入不完整");
				flag = false;
				break;
			}
		}
		return flag;
	}
	/**
	 * b1 b2 相同  返回 true
	 * @param b1
	 * @param b2
	 * @return
	 */
	public  boolean compare2Byte(byte[] b1,byte[] b2){
		if(b1==null || b2==null || b1.length!=b2.length ){
			return false;
		}else{
			for(int i = 0;i<b1.length;i++){
				if( b1[i]!=b2[i] ){
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * 确认 当前 ARM 返回的 byte[]的 head是否 与预期相同 ( 比较 buffer前3 byte 和 head)
	 * @return
	 */
	public  boolean compareHead(byte[] buffer,byte[] head){
		if(buffer==null || head==null ){
			return false;
		}else{
			for(int i = 0;i<head.length;i++){
				if( buffer[i]!=head[i] ){
					return false;
				}
			}
		}
		return true;
	}
	/**
	 * 确认当前发送的数据: 格式是否正确
	 * @param data
	 * @return
	 */
	public  boolean confirmSendData(byte[]  data){
		if(data==null || data.length<5){
			return false;
		}else{
			return true;
		}
	}

	public boolean confirmReceiveData(byte[] buffer) {
		if( buffer[ROBOT]  ==0x01){
			if(!verify(buffer) || buffer.length!=buffer[DATA_LENGTH]+5){
				return false;
			}
			return true;
		}else{
			L.w(LOG,"接收到ARM的调试信息: "+ Arrays.toString(buffer));
			return false;
		}
	}
	/**
	 * 验证接收的信息的校验位
	 * @param buffer
	 * @return
	 */
	public boolean verify(byte[] buffer){
		byte verifyBit = 0;
		byte checkBit= buffer[buffer.length-1];
		for(int v=0;v<buffer.length-1;v++){
			verifyBit+=buffer[v];
		}
		return checkBit == verifyBit;
	}

	public  byte[] getHead(byte[] buffer){
		return new byte[]{buffer[0],buffer[1],buffer[2]};
	}

}