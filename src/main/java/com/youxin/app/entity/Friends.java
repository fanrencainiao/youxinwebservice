package com.youxin.app.entity;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import com.youxin.app.entity.User.UserSettings;

import lombok.Data;

/**
 * 用户
 * @author cf
 *
 */
@Entity(value = "friends", noClassnameStored = true)
@Data
public class Friends {

	@Id
	private ObjectId id;
	
	/**
	 * 发起者accid
	 */
	private String accid;
	/**
	 * 朋友的accid
	 */
	private String faccid;
	/**
	 * 备注名，限制长度128，可设置为空字符串
	 */
	private String alias;
	/**
	 * ex字段，限制长度256，可设置为空字符串
	 */
	private String ex;
	/**
	 * serverex字段，限制长度256，可设置为空字符串
	 *  此字段client端只读，server端读写
	 */
	private String serverex;
}
