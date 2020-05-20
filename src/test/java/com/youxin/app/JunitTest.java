//package com.youxin.app;
//
//import java.io.BufferedInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.DataInputStream;
//import java.io.DataOutputStream;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.io.PrintStream;
//import java.io.UnsupportedEncodingException;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.ProtocolException;
//import java.net.URL;
//import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//
//import javax.net.ssl.HttpsURLConnection;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.message.BasicNameValuePair;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mongodb.morphia.Datastore;
//import org.mongodb.morphia.query.Query;
//import org.mongodb.morphia.query.UpdateOperations;
//import org.mongodb.morphia.query.UpdateResults;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.mongodb.DBCollection;
//import com.yeepay.g3.sdk.yop.config.support.SDKConfigUtils;
//import com.youxin.app.entity.RedPacket;
//import com.youxin.app.entity.User;
//import com.youxin.app.entity.msgbody.MsgBody;
//import com.youxin.app.entity.msgbody.MsgBody.ID;
//import com.youxin.app.utils.HttpUtil;
//import com.youxin.app.utils.KConstants;
//import com.youxin.app.utils.KSessionUtil;
//import com.youxin.app.utils.Md5Util;
//import com.youxin.app.utils.StringUtil;
//import com.youxin.app.utils.alipay.util.AliPayUtil;
//import com.youxin.app.yx.ApplicationType;
//import com.youxin.app.yx.HttpClientUtil;
//import com.youxin.app.yx.SDKService;
//import com.youxin.app.yx.UUIDUtil;
//import com.youxin.app.yx.request.Msg;
//import com.youxin.app.yx.request.MsgRequest;
//
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes=YouxinApplication.class)
//public class JunitTest {
//
//	@Autowired
//	@Qualifier("get")
//	private Datastore dfds;
//
////	@Test
//	public void sendMsgTest() {
//		//1100  1e6e0a04d20f50967c64dac2d639a577
//		MsgRequest messageBean = new MsgRequest();
//		messageBean.setType(100);// 自定义
//		messageBean.setOpe(0);// 个人消息
////		if (toUser!=null) {
////			messageBean.setFrom(toUser.getAccid());
////		}else {
//			messageBean.setFrom("1e6e0a04d20f50967c64dac2d639a577");
////		}
//		messageBean.setTo("971b71a3fd88a7088267deb89bd36d60");
//		ID id=new ID();
//		id.setId("5dd7aaa212e8e35d83d2c2a3");
//		System.out.println(new MsgBody(0, KConstants.MsgType.BACKREDPACKET, "5dd7aaa212e8e35d83d2c2a3"));
//		messageBean.setBody(JSON.toJSONString(new MsgBody(0, KConstants.MsgType.BACKREDPACKET, id)));
//		try {
//			SDKService.sendMsg(messageBean);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	@Test
//	public void sendStation() {
//		
////		Map<String, Object> params =new HashMap<>();
////        params.put("userName", "陈名柱");
////        params.put("idCard", "43252419891003843X");
////        params.put("airways", "南方");
////        params.put("flightNo", "3654");
////        params.put("startStation", "北京");
////        params.put("terminalStation", "贵阳");
////        params.put("flightDate", "2019-12-01");
////        params.put("appointCount", "17788902253");
////        params.put("validateCode", "1111");
////        params.put("token", "29531116a5bab48ff041c8d796448f2f");
////        params.put("smsCode", "1111");
////        String urlPost = HttpUtil.URLPost("http://www.gzairports.com:11111/appointment.action", params);
////        System.out.println(urlPost);
//		
//		
//		//自定义消息通知
////		Msg msg=new Msg();
////		msg.setAttach("{\"msg\":\"测试\"}");
////		msg.setFrom(Md5Util.md5HexToAccid("10000"));
////		msg.setTo("a15000d0c030f046440854c8f0b7db38");
////		msg.setType(0);//文本
////		msg.setSave(2);
////		JSONObject sendAttachMsg = SDKService.sendAttachMsg(msg);
////		System.out.println(sendAttachMsg);
////	
//
//	}
//	
////	@Test
//	public void sdkTest() {
//		
//		JSONObject uinfos = SDKService.getUinfos("b170a070802741f667201b54880c925f");
//		
//		System.out.println(uinfos);
//	
//
//	}
//	
//	
//
//	private static void sendPhoneCode() {
//		for (int i = 0; i < 10; i++) {
//			System.out.println("第"+i+"次发送短信");
//			Map<String, Object> params =new HashMap<>();
//			params.put("telNumber", "13628271337");
//			params.put("startStation", "北京首都");
//			params.put("terminalStation", "贵阳");
//			params.put("idCard", "43252419891003843X");
//			String urlPost = HttpUtil.URLPost("http://www.gzairports.com:11111/sendSms.action", params);
//			if(!StringUtil.isEmpty(urlPost)) {
//				JSONObject parseObject = JSON.parseObject(urlPost);
//				System.out.println(parseObject.toJSONString());
//				String msg=parseObject.getJSONObject("result").getString("msg");
//				System.out.println(msg);
//				
//				if(!StringUtil.isEmpty(msg)) {
//					if(!msg.contains("发送验证码失败")) {
//						System.out.println("发送成功:"+msg);
//						break;
//					}
//					System.out.println(parseObject.getJSONObject("result").getString("code"));
//				}
//			}else {
//				System.out.println("短信发送接口请求失败");
//			}
//			try {
//				Thread.sleep(1000);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//				System.out.println("定时失败");
//			}
//		}
//	}
//
//	private static void sendStations() {
//		Map<String, Object> params =new HashMap<>();
//		params.put("userName", "陈名柱");
//		params.put("idCard", "43252419891003843X");
//		params.put("airways", "南方");
//		params.put("flightNo", "3654");
//		params.put("startStation", "北京");
//		params.put("terminalStation", "贵阳");
//		params.put("flightDate", "2019-12-01");
//		params.put("appointCount", "2");
//		params.put("telNumber", "17788902253");
//		params.put("validateCode", "1111");
//		params.put("token", "29531116a5bab48ff041c8d796448f2f");
//		params.put("smsCode", "1111");
//		String urlPost = HttpUtil.URLPost("http://www.gzairports.com:11111/appointment.action", params);
//		System.out.println(urlPost);
//	}
//
//	private static String getPicCode() throws MalformedURLException, IOException, ProtocolException {
//
//		Map<String, Object> urlPostBuffer = HttpUtil.URLPostBuffer("http://www.gzairports.com:11111/order/creatImgCode.action", null);
//		
//		System.out.println(urlPostBuffer.get("qrBytesEncoder"));
//		//定义API地址
//		URL url = new URL("https://v2-api.jsdama.com/upload");
//		//这里是图片base64后的编码
//		String captchaData = urlPostBuffer.get("qrBytesEncoder").toString();
//				//整个字符串是报文
//		
//		String content = "{" +
//		        "    \"softwareId\": 17221," +
//		        "    \"softwareSecret\": \"Dy2uxCq8Mxaew1wUoXBvoPa3gC3RW36C2JBwYCFt\"," +
//		        "    \"username\": \"a475972878\"," +
//		        "    \"password\": \"Aa.475972878\"," +
//		        "    \"captchaData\": \"" + captchaData + "\"," +
//		        "    \"captchaType\": 1001," +
//		        "    \"captchaMinLength\": 0," +
//		        "    \"captchaMaxLength\": 0," +
//		        "    \"workerTipsId\": 0" +
//		        "}";
//		//通过url建立连接
//		HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
//		//设定需要打开输出流
//		connection.setDoOutput(true);
//		//设定请求方式为POST
//		connection.setRequestMethod("POST");
//
//		//获取远端输出流,用于给API发送数据
//		PrintStream pw = new PrintStream(connection.getOutputStream(), true);
//		pw.println(content);
//		pw.close();
//
//		//获取远端输出流返回的内容
//		BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
//		byte[] buf = new byte[1024];
//		int len = bis.read(buf);
//		String responseData = new String(buf, 0, len, StandardCharsets.UTF_8);
//		bis.close();
//		JSONObject json=JSON.parseObject(responseData);
//		if(json!=null) {
//			JSONObject data = json.getJSONObject("data");
//			if(data!=null) {
//				String code=data.getString("recognition");
//				return code;
//			}
//			
//		}
//		return null;
//	}
//
//	
////	@Test
//	public void aliTest() {
////		String code = AliPayUtil.getAuthInfoStr();
////		System.out.println(code);
//		//PERSONAL_PAY，C2C现金红包-发红包PERSONAL_COLLECTION，C2C现金红包-领红包
//		String oderId = AliPayUtil.commonQueryRequest("281579254219583", "20200117110075000006820073811194", "PERSONAL_PAY");
//		System.out.println("查询结果："+oderId);
//	}
////	@Test
//	public void redisTest() {
////		KSessionUtil.deleteUserByUserId("user*");
//		DBCollection userCollection = dfds.getCollection(User.class);
//		if (userCollection != null && userCollection.count() > 0) {
//
//			Query<User> q = dfds.createQuery(User.class);
//			UpdateOperations<User> ops = dfds.createUpdateOperations(User.class);
//			ops.set("online",0);
//			UpdateResults update = dfds.update(q, ops);
//			System.out.println(update.getUpdatedCount());
//			q.filter("_id >", 10000005).forEach(u->KSessionUtil.deleteUserByUserId(u.getId()));
//			
//		}
//		
//	}
//	
//	  public static void main(String[] args) throws IOException {
//
////	        System.out.println(getPicCode());
////	        sendStations();
////		  sendPhoneCode();
////		  String urlPost = HttpUtil.URLPost("https://app.crv.com.cn/app_timelimit/v1/dc-timelimit/presale/presaleLog/addTimeLimit", null);
////		  System.out.println("返回"+urlPost);
//	  }
//
//}
