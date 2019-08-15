package com.youxin.app.yx.request.chatroom;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年8月14日 上午11:09:07
 */
@Data
public class ChatroomAddRobotRequest {
	/**
	 * 聊天室id
	 */
	private long roomid;
	/**
	 * JSONArray	是	机器人账号accid列表，必须是有效账号，账号数量上限100个
	 */
	private String accids;
	/**
	 * 		否	机器人信息扩展字段，请使用json格式，长度4096字符
	 */
	private String roleExt;
	/**
	 * 		否	机器人进入聊天室通知的扩展字段，请使用json格式，长度2048字符
	 */
	private String notifyExt;
}
