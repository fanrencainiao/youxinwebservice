package com.youxin.app.yx.request.chatroom;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年8月14日 上午11:26:56
 */
@Data
public class ChatroomTemporaryMuteRequest {
	/**
	 * 是 聊天室id
	 */
	private long roomid;
	/**
	 * 是 操作者accid,必须是管理员或创建者
	 */
	private String operator;
	/**
	 * 是 被禁言的目标账号accid
	 */
	private String target;
	/**
	 * 是 0:解除禁言;>0设置禁言的秒数，不能超过2592000秒(30天)
	 */
	private long muteDuration;

	/**
	 * 否 操作完成后是否需要发广播，true或false，默认true
	 */
	private String needNotify;
	/**
	 * 否 通知广播事件中的扩展字段，长度限制2048字符
	 */
	private String notifyExt;
}
