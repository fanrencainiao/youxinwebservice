package com.youxin.app.entity;


import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
/**
 * 用户
 * @author cf
 *
 */
@Entity(value = "user", noClassnameStored = true)
@Data
@ApiModel(value="用户")
public class User {
	
	@Id
	@ApiModelProperty(hidden=true)
	private Integer id;
	/**
	 * 用户帐号，最大长度32字符，必须保证一个APP内唯一
	 */
	@Indexed(unique=true)
	@ApiModelProperty(hidden=true)
	private String accid;
	/**
	 * 用户昵称，最大长度64字符，可设置为空字符串
	 */
	@ApiModelProperty(value="用户昵称，最大长度64字符，可设置为空字符串",required=true)
	private String name;
	/**
	 * json属性，第三方可选填，最大长度1024字符
	 */
	private String props;
	/**
	 * 用户头像，最大长度1024字节，可设置为空字符串
	 */
	@ApiModelProperty(value="用户头像，最大长度1024字节，可设置为空字符串")
	private String icon;
	/**
	 *  	网易云通信ID可以指定登录token值，
	 *  	最大长度128字符，并更新，如果未指定，
	 *  	会自动生成token，并在创建成功后返回
	 */
	@ApiModelProperty(hidden=true)
	@Indexed(unique=true)
	private String token;
	/**
	 * 用户签名，最大长度256字符，可设置为空字符串
	 */
	@ApiModelProperty(value=" 用户签名，最大长度256字符，可设置为空字符串")
	private String sign;
	/**
	 * 用户email，最大长度64字符，可设置为空字符串
	 */
	@ApiModelProperty(value="用户email，最大长度64字符，可设置为空字符串")
	private String email;
	/**
	 * 用户生日，最大长度16字符，可设置为空字符串
	 */
	@ApiModelProperty(value="用户生日，最大长度16字符，可设置为空字符串")
	private String birth;
	/**
	 * 用户mobile，最大长度32字符，非中国大陆手机号码需要填写国家代码(如美国：+1-xxxxxxxxxx)或地区代码(如香港：+852-xxxxxxxx)，可设置为空字符串
	 */
	@ApiModelProperty(value="用户mobile，最大长度32字符，非中国大陆手机号码需要填写国家代码(如美国：+1-xxxxxxxxxx)或地区代码(如香港：+852-xxxxxxxx)，可设置为空字符串")
	private String mobile;
	/**
	 * 用户性别，0表示未知，1表示男，2女表示女，其它会报参数错误
	 */
	@ApiModelProperty(value="用户性别，0表示未知，1表示男，2女表示女，其它会报参数错误")
	private int gender;
	/**
	 * 用户名片扩展字段，最大长度1024字符，用户可自行扩展，建议封装成JSON字符串，也可以设置为空字符串
	 */
	@ApiModelProperty(value="用户名片扩展字段，最大长度1024字符，用户可自行扩展，建议封装成JSON字符串，也可以设置为空字符串")
	private String ex;
	@ApiModelProperty(value="登录类型 0：账号密码登录，1：短信验证码登录，2.友信号密码登录",required=true)
	private int loginType;// 登录类型 0：账号密码登录，1：短信验证码登录，2.友信号密码登录
	@ApiModelProperty(value="密码",required=true)
	private String password;//
	/**
	 * 用户设置
	 */
	@ApiModelProperty(value="用户设置")
	private UserSettings settings;
	
	@Data
	public static class UserSettings {
		/**
		 * 是否全局禁言(文字)：
		 * true：全局禁言，false:取消全局禁言
		 */
		private Integer tmute;
		
		/**
		 * 是否全局禁言(音频)：
		 * true：全局禁言，false:取消全局禁言
		 */
		private Integer vmute;
		/**
		 * 被静音的帐号列表
		 */
		private List<String> mutelist;
		
		/**
		 * 加黑的帐号列表
		 */
		private List<String> blacklist;
		
	}
	
	
	

}
