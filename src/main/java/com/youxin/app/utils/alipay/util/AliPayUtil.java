package com.youxin.app.utils.alipay.util;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.youxin.app.entity.User.MyCard;
import com.youxin.app.utils.alipay.config.AlipayConfig;
import com.youxin.app.utils.alipay.sign.RSA;
import com.youxin.app.utils.applicationBean.AliPayConfig;
@Component
public class AliPayUtil{
	private static AliPayConfig aliPayConfig;
	@Autowired
	public AliPayUtil(AliPayConfig aliPayConfig){
		AliPayUtil.aliPayConfig=aliPayConfig;
	}
	
	public static String APP_ID(){
		return aliPayConfig.getAppid();
	}
	public static String APP_PRIVATE_KEY() {
		return aliPayConfig.getAppPrivateKey();
	}
	public static String CHARSET() {return aliPayConfig.getCharset();}
	public static String ALIPAY_PUBLIC_KEY() {return aliPayConfig.getAlipayPublicKey();}
	public static String callBackUrl() {return aliPayConfig.getCallBackUrl();}
	
	public static String AppCode() {return aliPayConfig.getAppCode();}
	
	static AlipayClient alipayClient;
	
	public static AlipayClient getAliPayClient(){
		if(alipayClient!=null){
			return alipayClient;
		}else{
			alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID(), APP_PRIVATE_KEY(), "json", CHARSET(), ALIPAY_PUBLIC_KEY(), "RSA2");
		}
		return alipayClient;
	}
	
	 public static String getOutTradeNo() {
		 int r1 = (int) (Math.random() * (10));// 产生2个0-9的随机数
			int r2 = (int) (Math.random() * (10));
			long now = System.currentTimeMillis();// 一个13位的时间戳
			String id = String.valueOf(r1) + String.valueOf(r2)
					+ String.valueOf(now);// 订单ID
			return id;
	   }
	 /**
	    * create the order info. 创建订单信息
	    *
	    */
	 public static String getOrderInfo(String subject, String body, String price,String orderNo) {

		//实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
			AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
			//SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
			AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
			
			model.setBody(body);
			model.setSubject(subject);
			model.setOutTradeNo(orderNo);
			model.setTimeoutExpress("30m");
			model.setTotalAmount(price);
			model.setProductCode("QUICK_MSECURITY_PAY");
//			model.setGoodsType("0");
			request.setBizModel(model);
			request.setNotifyUrl(callBackUrl());
			try {
		        //这里和普通的接口调用不同，使用的是sdkExecute
		        AlipayTradeAppPayResponse response = getAliPayClient().sdkExecute(request);
		        System.out.println("返回order  "+response.getBody());//就是orderString 可以直接给客户端请求，无需再做处理。
		        
		        return response.getBody();
		    } catch (AlipayApiException e) {
		        e.printStackTrace();
		        return null;
		}
	   }
	   
	   
	   /**
	    * sign the order info. 对订单信息进行签名
	    *
	    * @param content
	    *            待签名订单信息
	    */
	 public static String sign(String content) {
	      return RSA.sign(content, AlipayConfig.private_key,AlipayConfig.input_charset);
	   }

	   /**
	    * get the sign type we use. 获取签名方式
	    *
	    */
	  /* private String getSignType() {
	      return "sign_type=\"RSA\"";
	   }*/
	 
	 /**
		 * 解析支付宝支付成功后返回的数据
		 * 
		 * @param request
		 * @return
		 * @throws UnsupportedEncodingException
		 */
		@SuppressWarnings("rawtypes")
		public static Map<String, String> getAlipayResult(javax.servlet.http.HttpServletRequest request) {
			// 获取支付宝POST过来反馈信息
			Map<String, String> params;
			params = new TreeMap<String, String>();
			Map requestParams = request.getParameterMap();
			for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {

				String name = (String) iter.next();
				String[] values = (String[]) requestParams.get(name);
				String valueStr = "";
				for (int i = 0; i < values.length; i++) {
					valueStr = (i == values.length - 1) ? valueStr + values[i]
							: valueStr + values[i] + ",";
				}
				// 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
				// valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
				params.put(name, valueStr);
			}
			return params;
		}
		
	public String getAuthInfo() throws AlipayApiException{
		AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
		getAliPayClient().execute(request);
		return null;
	}
	

	 public static String checkBankCard(MyCard myCard) {
		 String returnStr = null; // 返回结果定义
		 String host = "https://yunyidata.market.alicloudapi.com";
		    String path = "/bankAuthenticate4";
		    String method = "POST";
		    String appcode = AppCode();
		    Map<String, String> headers = new HashMap<String, String>();
		    //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
		    headers.put("Authorization", "APPCODE " + appcode);
		    //根据API的要求，定义相对应的Content-Type
		    headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		    Map<String, String> querys = new HashMap<String, String>();
		    Map<String, String> bodys = new HashMap<String, String>();
		    bodys.put("ReturnBankInfo", "YES");
		    bodys.put("cardNo", myCard.getBankCard());
		    bodys.put("idNo", myCard.getIdCard());
		    bodys.put("name", myCard.getName());
		    bodys.put("phoneNo", myCard.getPhone());


		    try {
		    	
		    	HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
		    	System.out.println(response.toString());
		    	//获取response的body
//		    	System.out.println(EntityUtils.toString(response.getEntity()));
		    	returnStr=EntityUtils.toString(response.getEntity());
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
		return returnStr;
		
	   }

	
}
