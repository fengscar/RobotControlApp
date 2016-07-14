package com.feng.Utils;

import android.content.Intent;
import com.feng.Constant.ArmProtocol;

/**
 *  各类数据的转换 convert 以及 byte的处理( 合并等)
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间 2015-11-30 下午8:38:12
 * @功能
 */
public class Transfer implements ArmProtocol{
	public int byteToInt(byte[] data){
		return 256*(int)data[1]+(int)data[0];
	}
	/**
	 * @param value  支持 2byte(字节) 十六进制的Int输入
	 */
	public byte[] intTo2Byte(int value){
		byte src[] = new byte[2];
		src[1] = (byte) ((value >> 8) & 0xFF);
		src[0] = (byte) (value & 0xFF);
		return src;
	}
	/**
	 * @param value  支持 3byte(字节) 十六进制的Int输入
	 */
	public  byte[] intTo3Byte(int value) {
		byte[] src = new byte[3];
		src[2] = (byte) ((value >> 16) & 0xFF);
		src[1] = (byte) ((value >> 8) & 0xFF);
		src[0] = (byte) (value & 0xFF);
		return src;
	}
	/**
	 * @param value  支持 4byte(字节) 十六进制的Int输入( For example:0x1234abcd...)
	 * @return byte[4] look like : byte[3]=12, byte[2]=34,byte[1]=ab,byte[0]=cd;  //小端模式的数据( CD AB 34 12
	 */
	public  byte[] intTo4Byte(int value) {
		byte[] src = new byte[4];
		src[3] = (byte) ((value >> 24) & 0xFF);
		src[2] = (byte) ((value >> 16) & 0xFF);
		src[1] = (byte) ((value >> 8) & 0xFF);
		src[0] = (byte) (value & 0xFF);
		return src;
	}
	/**
	 *  将 "eeff" 转换成 byte[0]=ff byte[1]=ee
	 * @param hex 
	 * @return   返回 转换成小端的 byte数组  低位在前)
	 */
	public  byte[] hexToByte(String hex) {
		int len = 2; //固定2byte
		byte[] result = new byte[len];
		char[] achar = hex.toCharArray();
		for (int i = 0; i < len; i++) {
			int pos = 2-i*2;
			result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
		}
		return result;
	}
	private  int toByte(char c) {
		byte b = (byte) "0123456789ABCDEF".indexOf(c);
		if( b==-1){
			b = (byte) "0123456789abcdef".indexOf(c);
		}
		return b;
	}
	/**
	 * 计算校验位  并将校验结果 添加到buffer的末尾  用于发送数据时校验位的计算
	 * @param buffer  
	 * @return  添加了校验位的byte[]
	 */
	public  byte[] addCheckBit(byte[] buffer){
		byte verify[] =new byte[1];
		for(int v=0;v<buffer.length;v++){
			verify[0]+=buffer[v];
		}
		return add2Byte(buffer, verify);
	}
	/**
	 * 返回 byte b1+b2
	 * @param b1
	 * @param b2
	 * @return
	 */
	public  byte[] add2Byte(byte[] b1,byte[] b2){
		byte res[]=new byte[b1.length+b2.length];
		System.arraycopy(b1, 0, res, 0, b1.length);
		System.arraycopy(b2, 0, res, b1.length,b2.length );
		return res;
	}
	/**
	 * 返回 byte[]:b1+b2+b3
	 * @param b1
	 * @param b2
	 * @param b3
	 * @return
	 */
	public  byte[] add3Byte(byte[] b1,byte[] b2,byte[] b3){
		byte res[]=new byte[b1.length+b2.length+b3.length];
		System.arraycopy(b1, 0, res, 0, b1.length);
		System.arraycopy(b2, 0, res, b1.length,b2.length );
		System.arraycopy(b3, 0, res, b1.length+b2.length,b3.length );
		return res;
	}
	/** 
	 * 获取 接收到的数据中的 data( 去掉 头 及 校验位)
	 * @param buffer
	 */
	public  byte[] getData(byte[] buffer){
		return this.getData(buffer, 0);
	}
	/**
	 * @param srcData
	 * @param offset  返回的数据的 偏移量
	 * @return
	 */
	public  byte[] getData(byte[] srcData,int offset){
		if( srcData[ArmProtocol.DATA_LENGTH]==0){
			return null;
		}
		if( srcData.length <= offset ){
			return null;
		}
		// 新的数据( 去掉了头以及校验位的) 
		byte[] dstData=new byte[srcData.length-offset-5];
		System.arraycopy(srcData, ArmProtocol.DATA+offset, dstData,0, dstData.length);
		return dstData;
	}
	public byte[] getData(Intent intent){
		return this.getData( intent.getByteArrayExtra(ArmProtocol.UNIFORM_RECEIVE) );
	}
	/*
	 * 数据中第一位(唯一位)  0x01 return true, else return false
	 */
	public boolean getFlag(byte[] buffer){
		byte flag=this.getData(buffer)[0];
		return flag==(byte)0x01 ? true : false;
	}
	/**
	 * 获取 buffer中的头三位
	 * @param buffer
	 * @return
	 */
	public  byte[] getHead(byte[] buffer){
		return new byte[]{buffer[0],buffer[1],buffer[2]};
	}
	public byte[] getHead(Intent intent){
		return this.getHead( intent.getByteArrayExtra(ArmProtocol.UNIFORM_SEND) );
	}
	/**
	 * 自动计算 数据长度(Para.length)
	 * @param head     头 
	 * @param data		数据(  不包括 数据长度, 本方法会自动计算并添加)
	 * @return  返回打包好的数据 ( 可以发送给ARM的数据) 
	 */
	public byte[] packingByte(byte[] head, byte[] data) {
		if (data == null) {
			byte datalength[] = new byte[] { 0x00 };
			return addCheckBit( add2Byte(head, datalength));
		} else {
			byte datalength[] = new byte[] { (byte) data.length };
			return addCheckBit( add3Byte(head, datalength, data));
		}
	}

	public String getAction(byte[] data){
		Verifier verifier=new Verifier();
		for (String action : SEND_ACTIONS.keySet()) {
			if (verifier.compareHead(data, SEND_ACTIONS.get(action)) == true) {
				return action;
			}
		}
		for (String action : RECEIVE_ACTIONS.keySet()) {
			if (verifier.compareHead(data, RECEIVE_ACTIONS.get(action)) == true) {
				return action;
			}
		}
		return null;
	}
}
