package com.youxin.app.yx.request.chatroom;

import lombok.Data;
/**
 * 修改聊天室开/关闭状态
 * @author cf
 * @date 2019年8月14日 上午10:24:39
 */
@Data
public class ChatroomToggleCloseStatRequest {

	/**
	 * 是	聊天室id
	 */
	private long roomid;
	/**
	 * String	是	操作者账号，必须是创建者才可以操作
	 */
	private String operator;
	/**
	 * String	是	true或false，false:关闭聊天室；true:打开聊天室

	 */
	private String valid;

	
	
}
