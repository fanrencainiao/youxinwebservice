package com.youxin.app.yx.request.chatroom;

import lombok.Data;
/**
 * 
 * @author cf
 * @date 2019年8月14日 上午10:24:39
 */
@Data
public class ChatroomGetRequest {

	/**
	 * 是	聊天室id
	 */
	private long roomid;
	/**
	 * 否	是否需要返回在线人数，true或false，默认false
	 */
	private String needOnlineUserCount="false";

	
	
}
