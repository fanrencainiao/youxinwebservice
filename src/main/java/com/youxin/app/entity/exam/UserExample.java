package com.youxin.app.entity.exam;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class UserExample extends BaseExample {

	private Long birthday;
	
	private String description;
	
	private String idcard;
	
	private String idcardUrl;
	
	private String name;
	
	private  String password;
	
	private Integer gender; //0:男   1:女
	
	private  String mobile;

	
	private String robitImg; // 机器人头像
	
	/**
	 * 账号
	 */
	private String account;
	
	private int userId=0;
	
	private String areaCode="86";
	
	private String randcode;
	
	private String phone;
	
	private String uphone;//绑定手机号  update方法处理字段
	
	private Integer userType;
	
	private String appId;//ios  当前包名
	
	private int xmppVersion; //xmpp 心跳包的时候用到
	
	private Integer d = 0;
	
	private Integer w = 0;
	
	private String email;
	
	private String payPassWord; //支付密码
	
	private int multipleDevices=-1; //多设备登陆
	
	private byte isSmsRegister = 0; //是否使用短信验证码注册 0:不是  1:是
	
	private String area;// 用户地理位置
	
	private String myInviteCode; //我的邀请码
	
	private String inviteCode; //注册时填写的邀请码
	
	private Integer regFirstUserId; // 注册时填写上级用户id
	private Integer regSecondUserId; // 注册时填写上上级用户id

	// 直邀人数
	private  Integer inviteFNum;
	// 直邀人数(vip)）
	private  Integer inviteFVipNum;

	// 二级邀请人数
	private  Integer inviteSNum;
	// 二级邀请人数(vip)
	private  Integer inviteSVipNum;
	
	// 总人数
	private Integer inviteTNum;
	
	// 总人数(vip)
	private  Integer inviteTVipNum;

	private String msgBackGroundUrl;// 朋友圈背景URL
	
	private int isSdkLogin;// 第三方登录标识  0 不是     1 是
	
	private int loginType;// 登录类型 0：账号密码登录，1：短信验证码登录，2.友信号密码登录
	
	private String verificationCode;// 短信验证码

	//当前登陆设备
	private String deviceKey;



}
