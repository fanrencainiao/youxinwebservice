package com.youxin.app.utils.sms;


import javax.annotation.Resource;

import org.mongodb.morphia.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.youxin.app.entity.SmsRecord;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.applicationBean.SmsConfig;



@Component
public class SMSVerificationUtils implements ApplicationContextAware{
	
	protected static Logger smsLogger=LoggerFactory.getLogger("SMSVerificationUtils");
	

	private static SmsConfig smsConfig;
	@Autowired
	@Qualifier("scf")
	private SmsConfig smsConfigs;
  
    private static Datastore ds;
    @Resource(name="get")
    private Datastore dss;


	public static final String SMSFORMAT = "00";
	
	public static SendSmsResponse sendSms(String telephone, String code, String areaCode) throws ClientException {
		// 可自助调整超时时间
		System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
		System.setProperty("sun.net.client.defaultReadTimeout", "10000");

		// 初始化acsClient,暂不支持region化
		IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", smsConfig.getAccesskeyid(),
				smsConfig.getAccesskeysecret());
		DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", smsConfig.getProduct(),
				smsConfig.getDomain());
		IAcsClient acsClient = new DefaultAcsClient(profile);

		// 组装请求对象-具体描述见控制台-文档部分内容
		SendSmsRequest request = new SendSmsRequest();
		// 必填:待发送手机号
		smsLogger.info("格式化手机号 :  00 + 国际区号 + 号码   ==========>"+"   SMSFORMAT :"+ SMSFORMAT +"     code: " + code + "========= areaCode: " + areaCode);
		request.setPhoneNumbers(SMSFORMAT + telephone);// 接收号码格式为00+国际区号+号码
		// 必填:短信签名-可在短信控制台中找到
		request.setSignName(smsConfig.getSignname());
		// 必填:短信模板-可在短信控制台中找到
		request.setTemplateCode(smsConfig.getEnglishTempletecode());
		if ("86".equals(areaCode) || "886".equals(areaCode) || "852".equals(areaCode))
			request.setTemplateCode(smsConfig.getChinaseTempletecode());

		// 可选:模板中的变量替换JSON串,如模板内容为"亲爱的用户,您的验证码为${code}"时,此处的值为
		request.setTemplateParam("{\"code\":\"" + code + "\"}");

		// 选填-上行短信扩展码(无特殊需求用户请忽略此字段)
		// request.setSmsUpExtendCode("90997");

		// 可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
		// request.setOutId("yourOutId");

		// hint 此处可能会抛出异常，注意catch
		SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
		smsLogger.info("阿里云短信服务回执详情："+JSONObject.toJSONString(sendSmsResponse));
		if (sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals("OK")) {
			smsLogger.info("短信发送成功！");
			saveSMSToDB(request.getPhoneNumbers(), areaCode, code, request.getSignName()+request.getTemplateCode(), sendSmsResponse.getRequestId());
		} else {
			smsLogger.info("短信发送失败！");
		}
		return sendSmsResponse;
	}
	
	public static SendSmsResponse sendSmsBank(String telephone, String bankCard,String name,Double totalMoney, String areaCode) throws ClientException {
		// 可自助调整超时时间
		System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
		System.setProperty("sun.net.client.defaultReadTimeout", "10000");

		// 初始化acsClient,暂不支持region化
		IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", smsConfig.getAccesskeyid(),
				smsConfig.getAccesskeysecret());
		DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", smsConfig.getProduct(),
				smsConfig.getDomain());
		IAcsClient acsClient = new DefaultAcsClient(profile);

		// 组装请求对象-具体描述见控制台-文档部分内容
		SendSmsRequest request = new SendSmsRequest();
		// 必填:待发送手机号
		smsLogger.info("格式化手机号 :  00 + 国际区号 + 号码   ==========>"+"   SMSFORMAT :"+ SMSFORMAT +"     bankCard: " + bankCard 
				+"     name: " + name+"     totalMoney: " + totalMoney+ "========= areaCode: " + areaCode);
		request.setPhoneNumbers(SMSFORMAT + telephone);// 接收号码格式为00+国际区号+号码
		// 必填:短信签名-可在短信控制台中找到
		request.setSignName(smsConfig.getSignname());
		// 必填:短信模板-可在短信控制台中找到
		request.setTemplateCode(smsConfig.getEnglishTempletecode());
		if ("86".equals(areaCode) || "886".equals(areaCode) || "852".equals(areaCode))
			request.setTemplateCode("SMS_170842723");

		// 可选:模板中的变量替换JSON串,如模板内容为"亲爱的用户,您的验证码为${code}"时,此处的值为
		request.setTemplateParam("{\"bankCard\":\""+bankCard+"\",\"name\":\""+name+"\",\"totalMoney\":"+totalMoney+"}");
		// 选填-上行短信扩展码(无特殊需求用户请忽略此字段)
		// request.setSmsUpExtendCode("90997");

		// 可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
		// request.setOutId("yourOutId");

		// hint 此处可能会抛出异常，注意catch
		SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
		smsLogger.info("阿里云短信服务回执详情："+JSONObject.toJSONString(sendSmsResponse));
		if (sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals("OK")) {
			smsLogger.info("短信发送成功！");
			saveSMSToDB(request.getPhoneNumbers(), areaCode, "000000", request.getSignName()+request.getTemplateCode(), sendSmsResponse.getRequestId());
		} else {
			smsLogger.info("短信发送失败！");
		}
		return sendSmsResponse;
	}

	
	/**
	 * @Description:（短信详情存库）
	 * @param telephone
	 * @param areaCode
	 * @param code
	 * @param content
	 * @param msgId
	 **/
	public static void saveSMSToDB(String telephone, String areaCode, String code, String content, String msgId) {
		SmsRecord sms = null;
		if (null == sms)
			sms = new SmsRecord();
		sms.setAreaCode(areaCode);
		sms.setTelephone(telephone);
		sms.setCode(code);
		sms.setContent(content);
		sms.setMsgId(msgId);
		sms.setTime(DateUtil.currentTimeSeconds());
		ds.save(sms);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		// TODO Auto-generated method stub
		ds=dss;
		smsConfig=smsConfigs;
	}
}
