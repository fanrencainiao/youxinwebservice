package com.youxin.app.utils.applicationBean;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component("scf")
@ConfigurationProperties(prefix="smsconfig")
public class SmsConfig {
	private int openSMS = 1;// 是否发送短信验证码
	private int isstart = 1;// 发送短信是否启动图形验证码校验
	
	// 阿里云短信服务
	private String product;// 云通信短信API产品,无需替换
	private String domain;// 产品域名,无需替换
	private String accesskeyid;// AK key
	private String accesskeysecret;// AK value
	private String signname;// 短信签名
	private String chinaseTempletecode;// 中文短信模板标识
	private String englishTempletecode;// 英文短信模板标识
}
