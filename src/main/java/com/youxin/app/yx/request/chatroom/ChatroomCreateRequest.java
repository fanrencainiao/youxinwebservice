package com.youxin.app.yx.request.chatroom;

import lombok.Data;
/**
 * 	创建聊天室
 * @author cf
 *
 */
@Data
public class ChatroomCreateRequest {

	/**
	 * String	是	聊天室属主的账号accid
	 */
	private String creator;
	/**
	 * String	是	聊天室名称，长度限制128个字符
	 */
	private String name;
	/**
	 * String	否	公告，长度限制4096个字符
	 */
	private String announcement;
	/**
	 * String	否	直播地址，长度限制1024个字符
	 */
	private String broadcasturl;
	/**
	 * String	否	扩展字段，最长4096字符
	 */
	private String ext;
	/**
	 * int	否	队列管理权限：0:所有人都有权限变更队列，1:只有主播管理员才能操作变更。默认0
	 */
	private int queuelevel;
	
	
}
