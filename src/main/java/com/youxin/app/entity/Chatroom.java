package com.youxin.app.entity;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import lombok.Data;

/**
 * 	聊天室
 * @author cf
 *
 */
@Entity(value = "chatroom", noClassnameStored = true)
@Data
public class Chatroom {
	@Id
	private ObjectId id;
	
	/**
	 *  聊天室属主的账号accid
	 */
	private String creator;
	/**
	 *  聊天室名称，长度限制128个字符
	 */
	private String name;
	/**
	 *  公告，长度限制4096个字符
	 */
	private String announcement;
	/**
	 *  扩展字段，最长4096字符
	 */
	private String ext;
	/**
	 *  队列管理权限：0:所有人都有权限变更队列，1:只有主播管理员才能操作变更。默认0
	 */
	private String queuelevel;
}
