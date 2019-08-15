package com.youxin.app.yx.request.chatroom;

import lombok.Data;
/**
 * 
 * @author cf
 * @date 2019年8月14日 上午10:24:39
 */
@Data
public class ChatroomGetBatchRequest {

	/**
	 * String	是	多个roomid，格式为：["6001","6002","6003"]（JSONArray对应的roomid，如果解析出错，会报414错误），限20个roomid
	 */
	private String roomids;
	/**
	 * 否	是否需要返回在线人数，true或false，默认false
	 */
	private String needOnlineUserCount="false";

	
	
}
