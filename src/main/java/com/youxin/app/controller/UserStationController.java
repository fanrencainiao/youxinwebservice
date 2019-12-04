package com.youxin.app.controller;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.youxin.app.entity.UserStation;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.HttpUtil;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.ThreadUtil;
import com.youxin.app.utils.supper.Callback;



@RestController
@RequestMapping(value="/info")
@CrossOrigin
public class UserStationController {
	

	@Autowired
	@Qualifier("get")
	private Datastore dfds;

	@PostMapping(value = "/saveInfo")
	public Object saveInfo(String phone,String code){
		
		
		if(StringUtil.isEmpty(phone)) {
			return Result.errorMsg("手机号不能为空");
		}
		if(StringUtil.isEmpty(code)) {
			return Result.errorMsg("验证码不能为空");
		}
		UserStation us = dfds.createQuery(UserStation.class).field("_id").equal(phone).get();
		System.out.println(us);
		if(us!=null) {
			us.setSmsCode(code);
			us.setTime(DateUtil.currentTimeSeconds());
		}else {
//			UserStation uss =new UserStation();
//			uss.setTelNumber("17788902253");
//			dfds.save(uss);
			return Result.error("用户不存在");
		}
		
		try {
			String picCode = getPicCode();
			if(picCode==null) {
				System.out.println("获取piccode失败");
				return Result.error("获取piccode失败");
			}
			us.setValidateCode(picCode);
			dfds.save(us);
			
			//抢礼物
			 Map<String, Object> params =new HashMap<>();
		        params.put("userName", us.getUserName());
		        params.put("idCard", us.getIdCard());
		        params.put("airways", us.getAirways());
		        params.put("flightNo", us.getFlightNo());
		        params.put("startStation", us.getStartStation());
		        params.put("terminalStation",us.getTerminalStation());
		        params.put("flightDate", us.getFlightDate());
		        params.put("appointCount", "2");
		        params.put("telNumber", us.getTelNumber());
		        params.put("validateCode", us.getValidateCode());
		        params.put("token", "29531116a5bab48ff041c8d796448f2f");
		        params.put("smsCode", us.getSmsCode());
		      
		        ThreadUtil.executeInThread(new Callback() {
					@Override
					public void execute(Object obj) {
						 for (int i = 0; i < 20; i++) {
							  
							  try {
								  System.out.println(us.getTelNumber()+",开始抢礼物=====第"+i+"次"+params);
								  Thread.sleep(1000);
								  String urlPost = HttpUtil.URLPost("http://www.gzairports.com:11111/appointment.action", params);
								  System.out.println(us.getTelNumber()+",结束抢礼物=====第"+i+"次"+urlPost);
							} catch (Exception e) {
								 System.out.println(us.getTelNumber()+",异常结束抢礼物=====第"+i+"次"+e.getMessage());
							}
							 
						}
					}
		        });
		List<UserStation> asList = dfds.createQuery(UserStation.class).asList();
		System.out.println(asList);
		return Result.success(asList);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Result.error(e.getMessage());
		} catch (ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Result.error(e.getMessage());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
	}
	
	@RequestMapping(value = "/getInfo", method = { RequestMethod.GET })
	public Object getInfo(String phone){
		System.out.println("查询用户："+phone);
		UserStation us = dfds.createQuery(UserStation.class).field("_id").equal(phone).get();
		if(us==null) {
			return Result.errorMsg("无此用户");
		}
		
		return Result.success(us);
	}

	@RequestMapping(value = "/getListv1", method = { RequestMethod.GET })
	public Object getListv1(String phone){
		List<UserStation> ulist = dfds.createQuery(UserStation.class).field("_id").equal(phone).asList();
		return Result.success(ulist);
	}
	@RequestMapping(value = "/delByObjectId")
	public Object delByObjectId(String id){
		if(StringUtil.isEmpty(id)) {
			return Result.errorMsg("参数为空");
		}
		
		dfds.delete(dfds.createQuery(UserStation.class).field("_id").equal(id));
		return Result.success(id);
	}
	@RequestMapping(value = "/sendPhoneCode")
	public Object sendPhoneCode(){
		
		for (int i = 0; i < 200; i++) {
			System.out.println("手动第"+i+"次发送短信");
			Map<String, Object> params =new HashMap<>();
			params.put("telNumber", "17788902253");
			params.put("startStation", "北京首都");
			params.put("terminalStation", "贵阳");
			params.put("idCard", "43252419891003843X");
			String urlPost = HttpUtil.URLPost("http://www.gzairports.com:11111/sendSms.action", params);
			if(!StringUtil.isEmpty(urlPost)) {
				JSONObject parseObject = JSON.parseObject(urlPost);
				System.out.println(parseObject.toJSONString());
				String msg=parseObject.getJSONObject("result").getString("msg");
				System.out.println(msg);
				
				if(!StringUtil.isEmpty(msg)) {
					if(!msg.contains("发送验证码失败")) {
						System.out.println("发送成功:"+msg);
						break;
					}
					System.out.println(parseObject.getJSONObject("result").getString("code"));
				}
			}else {
				System.out.println("短信发送接口请求失败");
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("定时失败");
			}
		}
		return Result.success();
	}
	private static String getPicCode() throws MalformedURLException, IOException, ProtocolException {

		Map<String, Object> urlPostBuffer = HttpUtil.URLPostBuffer("http://www.gzairports.com:11111/order/creatImgCode.action", null);
		
		System.out.println(urlPostBuffer.get("qrBytesEncoder"));
		//定义API地址
		URL url = new URL("https://v2-api.jsdama.com/upload");
		//这里是图片base64后的编码
		String captchaData = urlPostBuffer.get("qrBytesEncoder").toString();
				//整个字符串是报文
		
		String content = "{" +
		        "    \"softwareId\": 17221," +
		        "    \"softwareSecret\": \"Dy2uxCq8Mxaew1wUoXBvoPa3gC3RW36C2JBwYCFt\"," +
		        "    \"username\": \"a475972878\"," +
		        "    \"password\": \"Aa.475972878\"," +
		        "    \"captchaData\": \"" + captchaData + "\"," +
		        "    \"captchaType\": 1001," +
		        "    \"captchaMinLength\": 0," +
		        "    \"captchaMaxLength\": 0," +
		        "    \"workerTipsId\": 0" +
		        "}";
		//通过url建立连接
		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
		//设定需要打开输出流
		connection.setDoOutput(true);
		//设定请求方式为POST
		connection.setRequestMethod("POST");

		//获取远端输出流,用于给API发送数据
		PrintStream pw = new PrintStream(connection.getOutputStream(), true);
		pw.println(content);
		pw.close();

		//获取远端输出流返回的内容
		BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
		byte[] buf = new byte[1024];
		int len = bis.read(buf);
		String responseData = new String(buf, 0, len, StandardCharsets.UTF_8);
		bis.close();
		JSONObject json=JSON.parseObject(responseData);
		if(json!=null) {
			JSONObject data = json.getJSONObject("data");
			if(data!=null) {
				String code=data.getString("recognition");
				return code;
			}
			
		}
		return null;
	}
	
}
