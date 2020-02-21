package com.youxin.app.utils.sms;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.youxin.app.entity.Config;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.service.ConfigService;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.ThreadUtil;
import com.youxin.app.utils.applicationBean.SmsConfig;
import com.youxin.app.utils.jedis.RedisCRUD;
import com.youxin.app.utils.supper.Callback;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
public class SMSServiceImpl {

	private String app_id = "";
	private String app_secret = "";
	private String app_template_id_invite = "";
	private String app_template_id_random = "";
	public static final String SMS_ALI = "aliyun";
	public static final String SMS_TTGJ = "ttgj";
	public static final String RANDCODE = "SMSService:randcode:%s";
	
	@Autowired
	private RedisCRUD redisServer;
	@Autowired
	@Qualifier("scf")
	private SmsConfig smsConfig;
	@Autowired
	private ConfigService configService;
	
	
	public boolean isAvailable(String telephone, String randcode) {
		String key = String.format(RANDCODE, telephone);
		String _randcode = redisServer.get(key);
		return randcode.equals(_randcode);
	}
	
	public String sendSmsToInternational(String telephone,String areaCode,String language,String code) {
		
		String key = String.format(RANDCODE, telephone);
		Long ttl = redisServer.ttl(key);
		code=redisServer.get(key);
		if (ttl >540) {
			String msg="请不要频繁请求验证码";
			 msg=MessageFormat.format(msg,ttl-540);
			System.out.println("======  "+msg);
			throw new ServiceException(msg);
		}
		if(null==code)
			code=StringUtil.randomCode();
		final String smsCode=code;
		ThreadUtil.executeInThread(new Callback() {
			
			@Override
			public void execute(Object obj) {
				System.out.println("SMSCONFIG:"+JSONObject.toJSONString(smsConfig));
				if (1 == smsConfig.getOpenSMS()) { // 需要发送短信
					if (SMS_ALI.equals("aliyun"))
						aliSMS(telephone, language, smsCode, areaCode, key);
					redisServer.set(key, smsCode);
					redisServer.expire(key, 600);
					log.info("telephone : {}, smsCode : {}",telephone,smsCode);
				}
			}
		});
		 return code;
	}
	
	public String sendSmsToManager(String telephone,String areaCode,String language,String bankCard,String name,Double totalMoney) {
		 Config config = configService.getConfig();

	
		
		ThreadUtil.executeInThread(new Callback() {
			
			@Override
			public void execute(Object obj) {
				System.out.println("SMSCONFIG:"+JSONObject.toJSONString(smsConfig));
				if (1 == smsConfig.getOpenSMS()) { // 需要发送短信
					if (SMS_ALI.equals(config.getSMSType()))
						aliSMSToManager(telephone, language, bankCard, name,totalMoney,areaCode);
					log.info("telephone : {}",telephone);
				}
			}
		});
		 return "ok";
	}
	
	
	
	// 阿里云短信服务
	public void aliSMS(String telephone, String language,String smsCode, String areaCode,String key){
		try {
			SendSmsResponse sendSms = SMSVerificationUtils.sendSms(telephone, smsCode, areaCode);
			if(null!=sendSms&&"OK".equals(sendSms.getCode()))
				redisServer.set(key, smsCode);
				redisServer.expire(key, 600);
			if(!StringUtil.isEmpty(sendSms.getCode()) && !"OK".equals(sendSms.getCode()))
				throw new ServiceException(sendSms.getCode(), sendSms.getMessage(), language);
		} catch (ClientException e) {
			e.printStackTrace();
		}
	}
	public void aliSMSToManager(String telephone, String language,String bandCard,String name,Double totalMoney, String areaCode){
		try {
			SendSmsResponse sendSms = SMSVerificationUtils.sendSmsBank(telephone, bandCard,name,totalMoney,areaCode);
			if(null!=sendSms&&"OK".equals(sendSms.getCode()))
				log.info("aliSMSToManager====>OK");
			if(!StringUtil.isEmpty(sendSms.getCode()) && !"OK".equals(sendSms.getCode()))
				throw new ServiceException(sendSms.getCode(), sendSms.getMessage(), language);
		} catch (ClientException e) {
			e.printStackTrace();
		}
	}
	
	public boolean checkImgCode(String telephone, String imgCode) {
		String key = String.format(KConstants.Key.IMGCODE, telephone);
		String cached = redisServer.get(key);
		System.out.println("imgCode"+imgCode);
		System.out.println("telephone"+telephone);
		System.out.println("cached"+cached);
		System.out.println("imgCode.toUpperCase().equals(cached)"+(imgCode.toUpperCase().equals(cached)));
		return imgCode.toUpperCase().equals(cached);
	}
 	
	public static class Result {
		private String access_token;
		private Integer expires_in;
		private String idertifier;
		private String res_code;
		private String res_message;

		public String getAccess_token() {
			return access_token;
		}

		public Integer getExpires_in() {
			return expires_in;
		}

		public String getIdertifier() {
			return idertifier;
		}

		public String getRes_code() {
			return res_code;
		}

		public String getRes_message() {
			return res_message;
		}

		public void setAccess_token(String access_token) {
			this.access_token = access_token;
		}

		public void setExpires_in(Integer expires_in) {
			this.expires_in = expires_in;
		}

		public void setIdertifier(String idertifier) {
			this.idertifier = idertifier;
		}

		public void setRes_code(String res_code) {
			this.res_code = res_code;
		}

		public void setRes_message(String res_message) {
			this.res_message = res_message;
		}
	}
	
	public String getApp_id() {
		return app_id;
	}

	public String getApp_secret() {
		return app_secret;
	}

	public String getApp_template_id_invite() {
		return app_template_id_invite;
	}

	public String getApp_template_id_random() {
		return app_template_id_random;
	}

//	public String getToken() throws Exception {
//		String token = redisServer.get("open.189.access_token");
//		if (StringUtil.isNullOrEmpty(token)) {
//			Result result = getTokenObj();
//			if ("0".equals(result.getRes_code())) {
//				
//				redisServer.setWithExpireTime("open.189.access_token", result.getAccess_token(),result.getExpires_in());
//				token = result.getAccess_token();
//			}
//		}
//		return token;
//	}

//	public Result getTokenObj() throws Exception {
//		HttpUtil.Request request = new HttpUtil.Request();
//		request.setSpec("https://oauth.api.189.cn/emp/oauth2/v3/access_token");
//		request.setMethod(HttpUtil.RequestMethod.POST);
//		request.getData().put("grant_type", "client_credentials");
//		request.getData().put("app_id", app_id);
//		request.getData().put("app_secret", app_secret);
//		Result result = HttpUtil.asBean(request, Result.class);
//		return result;
//	}
//
//	public JSONMessage sendInvite(String telephone, String companyName, String username, String password) {
//		JSONMessage jMessage;
//
//		try {
//			Map<String, String> params = new HashMap<String, String>();
//			params.put("param1", companyName);
//			params.put("param2", username);
//			params.put("param3", password);
//			Result result = sendSms(app_template_id_invite, telephone, params);
//
//			if ("0".equals(result.getRes_code())) {
//				jMessage = JSONMessage.success();
//			} else {
//				jMessage = JSONMessage.failure(result.getRes_message());
//			}
//		} catch (Exception e) {
//			jMessage = KConstants.Result.InternalException;
//		}
//
//		return jMessage;
//	}
//
//	private Result sendSms(String template_id, String telephone, Map<String, String> params) throws Exception {
//		HttpUtil.Request request = new HttpUtil.Request();
//		request.setSpec("http://api.189.cn/v2/emp/templateSms/sendSms");
//		request.setMethod(HttpUtil.RequestMethod.POST);
//		request.getData().put("app_id", app_id);
//		request.getData().put("access_token", getToken());
//		request.getData().put("acceptor_tel", telephone);
//		request.getData().put("template_id", template_id);
//		request.getData().put("template_param", JSON.toJSONString(params));
//		request.getData().put("timestamp", DateUtil.getFullString());
//
//		return HttpUtil.asBean(request, Result.class);
//	}

}
