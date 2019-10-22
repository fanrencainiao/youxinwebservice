package com.youxin.app.entity;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import lombok.Data;
@Entity(value = "Config",noClassnameStored=true)
@Data
public class Config {
	
	private @Id long id=10000;
	/**
	 * 0否，1是
	 */
	private int isAutoAddressBook;
	/**
	 * 接口地址
	 */
	private String apiUrl;
	/**
	 * ios下载地址
	 */
	private String iosDownUrl;
	/**
	 * android下载地址
	 */
	private String androidDownUrl;
	
	/**
	 * ios版本号
	 */
	private String iosVersion;
	/**
	 * android版本号
	 */
	private String androidVersion;
	
	/**
	 * ios更新说明
	 */
	private String iosExplain;
	/**
	 * android更新说明
	 */
	private String androidExplain;
	
	/**
	 * 请求地址
	 */
	private String ipAddress;
	
	/**
	 * 请求区域
	 */
	private String area;
	

}
