package com.youxin.app.utils.applicationBean;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
/**
 * 易宝支付
 * @author cf
 * @date 2020年5月13日 下午6:12:32
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "yeepayconfig")
public class YeePayConfig {
	
	private String oprkey;
	private String appkey;
	private String yeePrivateKey;
	private String yeePublicKey;
	private String callBackUrl;
	private String callBackUrlxcx;

}
