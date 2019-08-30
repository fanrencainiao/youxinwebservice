package com.youxin.app.yx.request.chatroom;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年8月30日 上午10:31:44
 */
@Data
public class ChatroomMembersByPage {
	/**
	 * 是 聊天室id
	 */
	private long roomid;
	/**
	 * 是 需要查询的成员类型,0:固定成员;1:非固定成员;2:仅返回在线的固定成员
	 */
	private int type;
	/**
	 * 是 单位毫秒，按时间倒序最后一个成员的时间戳,0表示系统当前时间
	 */
	private long endtime;
	/**
	 * 是 返回条数，<=100
	 */
	private long limit;

}
