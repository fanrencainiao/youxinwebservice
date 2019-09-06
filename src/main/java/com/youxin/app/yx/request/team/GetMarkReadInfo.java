package com.youxin.app.yx.request.team;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年9月6日 下午4:56:10
 */
@Data
public class GetMarkReadInfo {
	/**
	 * 是 群id，群唯一标识，创建群时会返回
	 * 
	 */
	private long tid;
	/**
	 * 是 发送群已读业务消息时服务器返回的消息ID
	 * 
	 */
	private long msgid;
	/**
	 * 是 消息发送者账号
	 * 
	 */
	private String fromAccid;
	/**
	 * 否 是否返回已读、未读成员的accid列表，默认为false
	 */
	private String snapshot;
}
