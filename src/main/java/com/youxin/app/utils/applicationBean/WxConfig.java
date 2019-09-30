package com.youxin.app.utils.applicationBean;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
//@Component
//@ConfigurationProperties(prefix = "wxConfig")
public class WxConfig {
	// 微信认证的自己应用ID
	private String appid;
	// 商户ID
	private String mchid;
	// App secret
	private String secret;
	// api API密钥
	private String apiKey;
	//
	/**
	 * 微信支付 回调 通知 url 默认 http://imapi.shiku.co/user/recharge/wxPayCallBack
	 * 
	 */
	private String callBackUrl;
	// 证书文件 名称
	private String pkPath;
	// 小程序appid
	private String xcxappid;
	// 小程序secret
	private String xcxsecret;
}
