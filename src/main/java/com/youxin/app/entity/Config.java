package com.youxin.app.entity;

import org.mongodb.morphia.annotations.Entity;

import org.mongodb.morphia.annotations.Id;

import com.alibaba.fastjson.JSON;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@Entity(value = "Config",noClassnameStored=true)
public class Config {
	
	private @Id long id=10000;
	
	/**
	 * 开启swagger 0否，1是
	 */
	private int isOpenSwagger;
	/**
	 *开启权限验证  0否，1是
	 */
	private int isAuthApi;
	/**
	 * 自动导入通讯录 0否，1是
	 */
	private int isAutoAddressBook;
	/**
	 * 接口地址
	 */
	private String apiUrl;
	/**
	 * 注册通知语
	 */
	private String regNotice="欢迎来到友信app";
	/**
	 * 用户银行卡提现是否短信通知0不通知，1通知
	 */
	private int bankMsgState=0;  
	/**
	 * 发送手机号
	 */
	private String sendPhone;  
	
	/**
	 * 短信类型
	 */
	private String SMSType="aliyun";
	
	
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
	 * ios禁用版本号 多个版本，隔开
	 */
	private String iosVersionDisable;
	/**
	 * ios审核版本号 多个版本，隔开
	 */
	private String iosVersionVerify;
	/**
	 * android版本号
	 */
	private String androidVersion;
	
	/**
	 * android禁用版本号 多个版本，隔开
	 */
	private String androidVersionDisable;
	
	
	
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
	
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
