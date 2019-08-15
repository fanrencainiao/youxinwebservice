package com.youxin.app.yx.request.chatroom;

import lombok.Data;
/**
 * 更新聊天室信息
 * @author cf
 * @date 2019年8月14日 上午10:35:28
 */
@Data
public class ChatroomUpdateRequest {

	/**
	 * long	是	聊天室id
	 */
	private long roomid;
	/**
	 * String	否	聊天室名称，长度限制128个字符
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
	 * String	否	true或false,是否需要发送更新通知事件，默认true
	 */
	private String needNotify="true";
	/**
	 * String	否	通知事件扩展字段，长度限制2048
	 */
	private String notifyExt;
	/**
	 * int	否	队列管理权限：0:所有人都有权限变更队列，1:只有主播管理员才能操作变更。默认0
	 */
	private int queuelevel;
	
	
}
