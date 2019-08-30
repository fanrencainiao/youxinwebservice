package com.youxin.app.yx.request.chatroom;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年8月30日 上午10:19:49
 */
@Data
public class ChatroomMuteRoom {
	/**
	 * 是 聊天室id
	 */
	private long roomid;
	/**
	 * 是 操作者accid，必须是管理员或创建者
	 */
	private String operator;
	/**
	 * 是 true或false
	 */
	private String mute;
	/**
	 * 否 true或false，默认true
	 */
	private String needNotify;
	/**
	 * 否 通知扩展字段
	 */
	private String notifyExt;
}
