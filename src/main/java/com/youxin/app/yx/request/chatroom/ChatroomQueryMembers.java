package com.youxin.app.yx.request.chatroom;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年8月30日 上午10:50:06
 */
@Data
public class ChatroomQueryMembers {
	/**
	 * 		是	聊天室id
	 */
	private long roomid;
	/**
	 * 	JSONArray	是	["abc","def"], 账号列表，最多200条
	 */
	private String accids;
	

}
