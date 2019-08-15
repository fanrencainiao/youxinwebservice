package com.youxin.app.yx.request.chatroom;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年8月14日 上午11:22:22
 */
@Data
public class ChatroomRemoveRobotRequest {
	/**
	 * 聊天室id
	 */
	private long roomid;
	/**
	 * JSONArray	是	机器人账号accid列表，必须是有效账号，账号数量上限100个
	 */
	private String accids;
}
