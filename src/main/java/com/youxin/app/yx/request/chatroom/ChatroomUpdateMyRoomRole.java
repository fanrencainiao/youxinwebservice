package com.youxin.app.yx.request.chatroom;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年8月30日 上午10:54:00
 */
@Data
public class ChatroomUpdateMyRoomRole {
	/**
	 * 是 聊天室id
	 */
	private long roomid;
	/**
	 * 是 需要变更角色信息的accid
	 */
	private String accid;
	/**
	 * boolean 否 变更的信息是否需要持久化，默认false，仅对聊天室固定成员生效
	 */
	private String save;
	/**
	 * boolean 否 是否需要做通知
	 */
	private String needNotify;
	/**
	 * 否 通知的内容，长度限制2048
	 */
	private String notifyExt;
	/**
	 * 否 聊天室室内的角色信息：昵称，不超过64个字符
	 */
	private String nick;
	/**
	 * 否 聊天室室内的角色信息：头像
	 */
	private String avator;
	/**
	 * 否 聊天室室内的角色信息：开发者扩展字段
	 */
	private String ext;

}
