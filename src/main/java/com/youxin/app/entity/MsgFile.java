package com.youxin.app.entity;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;


import lombok.Data;

@Data
@Entity(value = "msgfile", noClassnameStored = true)
public class MsgFile {

	@Id
	private ObjectId id;
	/**
	 * 字符流base64串(Base64.encode(bytes)) ，最大15M的字符流
	 * 最大15M的字符流
	 */
	private String content;

	/**
	 * 上传文件类型
	 */
	private String type;
	/**
	 * 返回的url是否需要为https的url，true或false，默认false
	 */
	private String ishttps;
	/**
	 * 文件过期时长，单位：秒，必须大于等于86400
	 */
	private Integer expireSec;
	/**
	 * 文件的应用场景，不超过32个字符
	 */
	private String tag;
	/**
	 * 返回的地址
	 */
	private String url;

}
