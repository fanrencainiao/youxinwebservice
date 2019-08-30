package com.youxin.app.yx.request.chatroom;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年8月30日 上午10:57:40
 */
@Data
public class ChatroomQueueBatchUpdateElements {
	/**
	 * 		是	聊天室id
	 */
	private long roomid;
	/**
	 * 		是	操作者accid,必须是管理员或创建者
	 */
	private String operator;
	/**
	 * 		是	更新的key-value对，最大200个，示例：{"k1":"v1","k2":"v2"}
	 */
	private String elements;
	/**
	 * 	boolean	否	true或false,是否需要发送更新通知事件，默认true
	 */
	private String needNotify;
	/**
	 * 		否	通知事件扩展字段，长度限制2048
	 */
	private String notifyExt;
	
}
