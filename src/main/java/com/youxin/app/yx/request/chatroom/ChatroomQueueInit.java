package com.youxin.app.yx.request.chatroom;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年8月14日 上午10:24:39
 */
@Data
public class ChatroomQueueInit {

	/**
	 * 是 聊天室id
	 */
	private long roomid;
	/**
	 * 是 队列长度限制，0~1000
	 * 
	 */
	private long sizeLimit;

}
