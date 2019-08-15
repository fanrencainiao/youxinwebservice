package com.youxin.app.yx.request.chatroom;

import lombok.Data;
/**
 * 
 * @author cf
 * @date 2019年8月14日 上午10:24:39
 */
@Data
public class ChatroomQueuePollRequest {

	/**
	 * 是	聊天室id
	 */
	private long roomid;
	/**
	 * 否	目前元素的elementKey,长度限制128字符，不填表示取出头上的第一个
	 */
	private String key;

	
	
}
