/**
 * @author 福建省和创伟业智能科技有限公司
 * @创建时间  2016-3-2 上午8:42:26
 */
package com.feng.Constant;

import java.io.IOException;

public interface ConnectService {
	/**
	 * 发送数据 (与 回复不同的是,发送多次)
	 * @param data 要发送的数据 
	 * @return 是否发送成功( 是否接收到正确的回复)
	 */
	public void send(byte[] data);
	/**
	 * 回复发送者 : 成功接收到数据!
	 * @param data
	 * @param isSuccess 是否接收完成( false表示接收失败,需要重新发送)
	 * @throws IOException 
	 */
	public void reply(byte[] data,boolean isSuccess) throws IOException;
	/**
	 * 接收数据
	 */
	public void receive(byte[] data);
	
	/**
	 * 连接
	 */
	public void connect();
	/**
	 * 重连
	 */
	public void reconnect();
	/**
	 * 断开当前连接
	 */
	public void disconnect();
}

