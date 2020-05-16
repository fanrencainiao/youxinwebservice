package com.youxin.app.utils.yoppay;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.YopResponse;
import com.yeepay.g3.sdk.yop.client.YopRsaClient;
import com.yeepay.g3.sdk.yop.encrypt.CertTypeEnum;
import com.yeepay.g3.sdk.yop.encrypt.DigestAlgEnum;
import com.yeepay.g3.sdk.yop.encrypt.DigitalSignatureDTO;
import com.yeepay.g3.sdk.yop.utils.DigitalEnvelopeUtils;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.applicationBean.YeePayConfig;

import sun.misc.BASE64Decoder;
@Component
public class YeePayUtil {
	private static YeePayConfig yeePayConfig;

	@Autowired
	public YeePayUtil(YeePayConfig yeePayConfig) {
		YeePayUtil.yeePayConfig = yeePayConfig;
	}
	public static String getUrl(String token) {
		// TODO Auto-generated method stub
		String appkey = yeePayConfig.getAppkey();
		StringBuffer url = new StringBuffer();
		//地址
		url.append("https://cash.yeepay.com/cashier/std");
		//参数

		StringBuilder stringBuilder = new StringBuilder();
		
		stringBuilder.append( "merchantNo=" + yeePayConfig.getOprkey() );
		stringBuilder.append("&" + "token=" + token );
		stringBuilder.append("&" + "timestamp=" + String.valueOf(System.currentTimeMillis()/1000) );
		stringBuilder.append("&" + "directPayType=" + "" );	
		stringBuilder.append("&" + "cardType=" + "" );	
		stringBuilder.append("&" + "userNo=" + "xk001" );
		stringBuilder.append("&" + "userType=" + "USER_ID" );
		stringBuilder.append("&" + "ext=" + "{\"appId\":\"\",\"openId\":\"\",\"clientId\":\"\"}" );

		String sign = getSign(stringBuilder.toString(),appkey);
		url.append("?sign="+sign+"&"+stringBuilder);
		System.out.println(url.toString());
		return url.toString();
	}
	
	public static String getOrder(String orderNo,String orderAmount) {
//		System.out.println("appkey:"+yeePayConfig.getAppkey());
		
		//secretKey:商户私钥（字符串形式）
		String OPRkey = yeePayConfig.getYeePrivateKey();
//		String OPRkey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCqSVrBTiEEDKZukFScaTRIX80IMgMg6gpCC5KM5MdMIw6fWmIponUo66seRS7MglNYYjNNz/9lDE8mjvAoPoPYdjI5eToGPQ7M63x4EQB2HICxB4l2Kqat9r4sUIj9/ANpM+yJR2zfZY7EsR0heMfJkQfYLgEfKI3as/4GgaDorVprKk0y/iK30e7NmDbVE4i68FiT/B5XezBq+orOatZlLIaHQqXUHS3Cj7M+E39WNvDNWp9/9ZZUaipIyun7MwKhlhRnW5mo/3qecutkO5g/b7wFWwQoqwq5ZN3m3z9gsvcxAY+yeOQCxTYgQgNUcrURp/s/gsXH3Mz8c2deNQ59AgMBAAECggEADp4Is1U7PPdqW01Pu17F2nyWw2BU/RX6AXIFh3X6rEzKFuBm/Wnw+AKa3W3/jBXtL5tmX1npHh8bYD+e2pAwjdE/kx1r31iNv7DAg3ykzH66hqJoqK2/7Gj6KXWpp3ENnec+I7PvjZGl2rkqu1J8ho9I3BlphpIwSG4bnSqyyaY2pwsNNlqhEtxtG+NxmMW5NKpaqahuhun3qGsx2GX5WSMFzIWXRdcSDskqOnN1ROJLsuaTl8bBmeIVN1zgqwsKF9zLfXNTPy1k0ChHjONVyO0U3VHxut8/BM3QpnvW53TpgTLUi2navN1rbQwjaxJyIudFdQ8USIFBHyE7hTYzhQKBgQDSa1nh8eF0ktO+hk2IQ51UGzDqM9RyjDrjSoX8m1oz5EBFSgGhmN0HsHM/VF8DgQw9aT0R8ByD3BflJ0KwBMsJ/ONYVvCWWw2bk9vrTvUZ1iF8gBGkl26cpOAhbEk4bgkCx7SQcFpvX4mikeV0bT99kajXH6GbfI02vtLGsFWFkwKBgQDPLHf9XrKtj5rxJdHjPSPm4e7g8XsTG0Xyw+NhVY0mY6k2cd8Fj9SAgycCbIh2S7a9prko+mvuqILZt5sQeubZpKxFneN3FBfSzjrZcO3ZLvFtDbixdKT2K5jzUJeh7UmTTA5Bo9ja9mEyRiIrGXmTBsQakYdGnh87NjloAR2lrwKBgHNPFOB2tsA1PggofRBxTSQsCnAtmvxy0EqCKk61q4bITFgsKBywMl/mWCGaUL8Q1u5IX4kW9elkkUuoaikfV0zP4p4kdo9OsnRRYLDggfx4lb0uSXzS53C8AX8PYkikNBfr7I1CpKxnxHrsTLuyqppbWhUZZmxYouIfTE5Jj3Q1AoGAM2w7QEWgHhp2AAM+LKRBZA6SZ30o6l4rp41dxAwjI/M6zgvHqq6/tUJYjW55FLvIWRyn+vblkXB8QiQjthx7bmxEYmdFTYpMO4P68XvpXa4cONBeFpX4WC4MIeDQMl4elBQducc8jWT4TS1BT+db2NWmGV4j8LBQ2jakWx9jx3sCgYEAuM2tnYZ076ob5lVRDhSTEND2yYT+wuFbx8Wh/KTvwjFtWCSy9Q/s7B6+PnCI6sL7CQr3Vh64RoQDkpk7REI/gWALFz7rIvx0mq4eqpADaqHhdOyfv99ppFjWo5gfuV+JyDWsasXLqZoP0S+HBVZgEPYzUmSyM5yUW/x/bxjet4c=";
		//step1  生成yop请求对象
		//arg0:appkey（举例授权扣款是SQKK+商户编号，亿企通是OPR:+商户编号，具体是什么请参考自己开通产品的手册。
		//arg1:商户私钥字符串
		System.out.println("appkey:"+yeePayConfig.getAppkey()+"oprkey:"+yeePayConfig.getOprkey());
		YopRequest request = new YopRequest(yeePayConfig.getAppkey(),OPRkey);
		
		//step2 配置参数
	    //arg0:参数名
	    //arg1:参数值
		request.addParam("parentMerchantNo", yeePayConfig.getOprkey());
		request.addParam("merchantNo", yeePayConfig.getOprkey());
		request.addParam("orderId",orderNo);
		request.addParam("orderAmount", orderAmount);
		request.addParam("notifyUrl", yeePayConfig.getCallBackUrl());
		request.addParam("goodsParamExt", "{\"goodsName\":\"充值\" ,\"goodsDesc\" : \"\" }");
		//request.addParam("fundProcessType", "");
		//request.addParam("divideDetail", "");
		
	
	    //step3 发起请求
		//arg0:接口的uri（参见手册）
		//arg1:配置好参数的请求对象
		System.out.println(request.getParams().toString());
	    YopResponse response;
		try {
			response = YopRsaClient.post("/rest/v1.0/std/trade/order", request);
			System.out.println("获取order"+response);
			System.out.println("获取order"+response.toString());
			System.out.println("获取order"+response.getStringResult());
			return response.getStringResult();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	/**
	  * 实例化公钥
	  * 
	  * @return
	  */
	public static PublicKey getPubKey() {
	  PublicKey publicKey = null;
	  try {

	   // 自己的公钥(测试)
		 String publickey=yeePayConfig.getYeePublicKey();
		java.security.spec.X509EncodedKeySpec bobPubKeySpec = new java.security.spec.X509EncodedKeySpec(
	     new BASE64Decoder().decodeBuffer(publickey));
	   // RSA对称加密算法
	   java.security.KeyFactory keyFactory;
	   keyFactory = java.security.KeyFactory.getInstance("RSA");
	   // 取公钥匙对象
	   publicKey = keyFactory.generatePublic(bobPubKeySpec);
	  } catch (NoSuchAlgorithmException e) {
	   e.printStackTrace();
	  } catch (InvalidKeySpecException e) {
	   e.printStackTrace();
	  } catch (IOException e) {
	   e.printStackTrace();
	  }
	  return publicKey;
	 }

	 public static PrivateKey getPrivateKey() {
		  PrivateKey privateKey = null;
		  String priKey = yeePayConfig.getYeePrivateKey();
		  PKCS8EncodedKeySpec priPKCS8;
		  try {
		   priPKCS8 = new PKCS8EncodedKeySpec(new BASE64Decoder().decodeBuffer(priKey));
		   KeyFactory keyf = KeyFactory.getInstance("RSA");
		   privateKey = keyf.generatePrivate(priPKCS8);
		  } catch (IOException e) {
		   e.printStackTrace();
		  } catch (NoSuchAlgorithmException e) {
		   e.printStackTrace();
		  } catch (InvalidKeySpecException e) {
		   e.printStackTrace();
		  }
		  return privateKey;
		 }
		//获取sign
		public static String getSign(String stringBuilder, String appKey){

			PrivateKey isvPrivateKey = getPrivateKey();
			DigitalSignatureDTO digitalSignatureDTO = new DigitalSignatureDTO();
			digitalSignatureDTO.setAppKey(appKey);
			digitalSignatureDTO.setCertType(CertTypeEnum.RSA2048);
			digitalSignatureDTO.setDigestAlg(DigestAlgEnum.SHA256);
			digitalSignatureDTO.setPlainText(stringBuilder.toString());
			String sign = DigitalEnvelopeUtils.sign0(digitalSignatureDTO,isvPrivateKey);
			return sign;
		}
}
