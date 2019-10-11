package com.youxin.app.entity;

import java.util.List;
import java.util.Map;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.NotSaved;

import com.youxin.app.entity.exam.UserExample;
import com.youxin.app.utils.DateUtil;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户
 * 
 * @author cf
 *
 */
@Entity(value = "user", noClassnameStored = true)
@Data
@ApiModel(value = "用户")
public class User {

	@Id
	@ApiModelProperty(hidden = true)
	private Integer id;
	/**
	 * 用户帐号，最大长度32字符，必须保证一个APP内唯一
	 */
	@Indexed(unique = true)
	@ApiModelProperty(hidden = true)
	private String accid;
	/**
	 * 用户昵称，最大长度64字符，可设置为空字符串
	 */
	@ApiModelProperty(value = "用户昵称，最大长度64字符，可设置为空字符串", required = true)
	private String name;
	/**
	 * json属性，第三方可选填，最大长度1024字符
	 */
	private String props;
	/**
	 * 用户头像，最大长度1024字节，可设置为空字符串
	 */
	@ApiModelProperty(value = "用户头像，最大长度1024字节，可设置为空字符串")
	private String icon;
	/**
	 * 网易云通信ID可以指定登录token值， 最大长度128字符，并更新，如果未指定， 会自动生成token，并在创建成功后返回
	 */
	@ApiModelProperty(hidden = true)
	@Indexed(unique = true)
	private String token;
	/**
	 * 用户签名，最大长度256字符，可设置为空字符串
	 */
	@ApiModelProperty(value = " 用户签名，最大长度256字符，可设置为空字符串")
	private String sign;
	/**
	 * 用户email，最大长度64字符，可设置为空字符串
	 */
	@ApiModelProperty(value = "用户email，最大长度64字符，可设置为空字符串")
	private String email;
	/**
	 * 用户生日，最大长度16字符，可设置为空字符串
	 */
	@ApiModelProperty(value = "用户生日，最大长度16字符，可设置为空字符串")
	private String birth;
	/**
	 * 用户mobile，最大长度32字符，非中国大陆手机号码需要填写国家代码(如美国：+1-xxxxxxxxxx)或地区代码(如香港：+852-xxxxxxxx)，可设置为空字符串
	 */
	@ApiModelProperty(value = "用户mobile，最大长度32字符，非中国大陆手机号码需要填写国家代码(如美国：+1-xxxxxxxxxx)或地区代码(如香港：+852-xxxxxxxx)，可设置为空字符串")
	private String mobile;
	/**
	 * 用户性别，0表示未知，1表示男，2女表示女，其它会报参数错误
	 */
	@ApiModelProperty(value = "用户性别，0表示未知，1表示男，2女表示女，其它会报参数错误")
	private int gender;
	/**
	 * 用户名片扩展字段，最大长度1024字符，用户可自行扩展，建议封装成JSON字符串，也可以设置为空字符串
	 */
	@ApiModelProperty(value = "用户名片扩展字段，最大长度1024字符，用户可自行扩展，建议封装成JSON字符串，也可以设置为空字符串")
	private String ex;

	// ============================================================================================
	// ==============================扩展字段=============================================
	// ============================================================================================
	@ApiModelProperty(value = "登录类型 0：账号密码登录，1：短信验证码登录，2.友信号密码登录", required = true)
	private int loginType;// 登录类型 0：账号密码登录，1：短信验证码登录，2.友信号密码登录
	@ApiModelProperty(value = "密码", required = true)
	private String password;
	@ApiModelProperty(value = "角色", hidden = true)
	@NotSaved
	private List<Integer> role;//
	@ApiModelProperty(value = "创建时间", hidden = true)
	private Long createTime;
	@ApiModelProperty(value = "更新时间", hidden = true)
	private Long updateTime;
	@ApiModelProperty(value = "短信验证码")
	@NotSaved
	private String smsCode;
	@ApiModelProperty(value = "用户总余额")
	private Double balance = 0.0; // 用户余额
	@ApiModelProperty(value = "充值总金额")
	private Double totalRecharge = 0.0;// 充值总金额
	@ApiModelProperty(value = "消费总金额")
	private Double totalConsume = 0.0;// 消费总金额
	@ApiModelProperty(value = "支付密码")
	private String payPassword;

	@ApiModelProperty(value = "支付宝买家id")
	private String aliUserId;
	
	@ApiModelProperty(value = "禁用用户（1禁用 0解禁）")
	private int disableUser;

	/**
	 * 用户设置
	 */
	@ApiModelProperty(value = "用户设置")
	private UserSettings settings;

	/**
	 * 用户登录日志
	 */
	@ApiModelProperty(value = "用户登录日志")
	private @NotSaved LoginLog loginLog;// 登录日志

	@Data
	public static class UserSettings {
		@ApiModelProperty(value = "可根据手机号搜索（0否 1是）")
		private int searchByMobile;
	}

	@Data
	public static class DeviceInfo {

		private long loginTime;
		/**
		 * 设备号 android ios web
		 */
		private String deviceKey;

		private String adress;// 地区标识 例 CN HK

		private int online;// 在线状态

		// ios 推送 用到的 appId
		private String appId;

		/**
		 * 推送平台厂商 华为 huawei 小米 xiaomi 百度 baidu apns ios
		 */
		private String pushServer;

		/**
		 * 推送平台的 token
		 */
		private String pushToken;

		/**
		 * VOip 推送 token
		 */
		private String voipToken;

		/**
		 * 同时使用多个推送平台的
		 */
		// private Map<String,String> pushMap;

		/**
		 * 下线时间
		 */
		private long offlineTime;

	}

	@Data
	public static class LoginLog {

		private int isFirstLogin;
		private long loginTime;
		private String apiVersion;
		private String osVersion;
		private String model;
		private String serial;
		private double latitude;
		private double longitude;
		private String location;
		private String address;

		private long offlineTime;
	}

	@Entity(value = "userLoginLog", noClassnameStored = true)
	@Data
	public static class UserLoginLog {

		@Id
		private Integer userId;
		/**
		 * 
		 * @Description: TODO(登陆日志信息)
		 * @author lidaye
		 * @date 2018年8月18日
		 */
		@Embedded
		private LoginLog loginLog;

		/**
		 * 登陆设备列表 web DeviceInfo android DeviceInfo ios DeviceInfo
		 */
		private Map<String, DeviceInfo> deviceMap;

		public UserLoginLog() {
			super();
		}

		public static LoginLog init(UserExample example, boolean isFirst) {
			LoginLog info = new LoginLog();
			info.setIsFirstLogin(isFirst ? 1 : 0);
			info.setLoginTime(DateUtil.currentTimeSeconds());
			info.setApiVersion(example.getApiVersion());
			info.setOsVersion(example.getOsVersion());

			info.setModel(example.getModel());
			info.setSerial(example.getSerial());
			info.setLatitude(example.getLatitude());
			info.setLongitude(example.getLongitude());
			info.setLocation(example.getLocation());
			info.setAddress(example.getAddress());
			info.setOfflineTime(0);

			return info;
		}

	}

}
