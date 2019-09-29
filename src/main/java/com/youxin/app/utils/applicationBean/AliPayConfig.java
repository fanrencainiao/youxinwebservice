package com.youxin.app.utils.applicationBean;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "aliPayConfig")
public class AliPayConfig {
	// 支付宝认证应用Id
	private String appid;
	// 应用私钥
	private String app_private_key;
	// 字符编码格式
	private String charset;
	// 支付宝公钥
	private String alipay_public_key;
	// 支付宝回调地址
	private String callBackUrl;
	// 支付宝银行卡验证接口code码
	private String appCode;
}
