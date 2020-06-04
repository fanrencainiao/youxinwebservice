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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayFundCouponOrderAppPayModel;
import com.alipay.api.domain.AlipayFundTransAppPayModel;
import com.alipay.api.domain.AlipayFundTransCommonQueryModel;
import com.alipay.api.domain.AlipayFundTransRefundModel;
import com.alipay.api.domain.AlipayFundTransUniTransferModel;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.domain.ExtUserInfo;
import com.alipay.api.domain.Participant;
import com.alipay.api.request.AlipayFundCouponOrderAppPayRequest;
import com.alipay.api.request.AlipayFundTransAppPayRequest;
import com.alipay.api.request.AlipayFundTransCommonQueryRequest;
import com.alipay.api.request.AlipayFundTransRefundRequest;
import com.alipay.api.request.AlipayFundTransUniTransferRequest;
import com.alipay.api.request.AlipayOpenAuthTokenAppQueryRequest;
import com.alipay.api.request.AlipayOpenAuthTokenAppRequest;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.request.AlipayUserInfoAuthRequest;
import com.alipay.api.request.AlipayUserInfoShareRequest;
import com.alipay.api.response.AlipayFundCouponOrderAppPayResponse;
import com.alipay.api.response.AlipayFundTransAppPayResponse;
import com.alipay.api.response.AlipayFundTransCommonQueryResponse;
import com.alipay.api.response.AlipayFundTransRefundResponse;
import com.alipay.api.response.AlipayFundTransUniTransferResponse;
import com.alipay.api.response.AlipayOpenAuthTokenAppQueryResponse;
import com.alipay.api.response.AlipayOpenAuthTokenAppResponse;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.alipay.api.response.AlipayUserInfoAuthResponse;
import com.alipay.api.response.AlipayUserInfoShareResponse;
import com.youxin.app.entity.User.MyCard;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.ReqUtil;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.alipay.config.AlipayConfig;
import com.youxin.app.utils.alipay.sign.RSA;
import com.youxin.app.utils.applicationBean.AliPayConfig;

@Component
public class AliPayUtil {
	private static AliPayConfig aliPayConfig;

	@Autowired
	public AliPayUtil(AliPayConfig aliPayConfig) {
		AliPayUtil.aliPayConfig = aliPayConfig;
	}

	public static String APP_ID() {
		return aliPayConfig.getAppid();
	}

	public static String APP_PRIVATE_KEY() {
		return aliPayConfig.getAppPrivateKey();
	}

	public static String CHARSET() {
		return aliPayConfig.getCharset();
	}

	public static String ALIPAY_PUBLIC_KEY() {
		return aliPayConfig.getAlipayPublicKey();
	}

	public static String callBackUrl() {
		return aliPayConfig.getCallBackUrl();
	}

	// 支付宝应用公钥证书路径
	public static String pubPath() {
		return aliPayConfig.getPubPath();
	}

	// 支付宝根证书路径
	public static String rootPath() {
		return aliPayConfig.getRootPath();
	}

	// 支付公钥证书路径
	public static String pubJobPath() {
		return aliPayConfig.getPubJobPath();
	}

	public static String AppCode() {
		return aliPayConfig.getAppCode();
	}

	public static String pid() {
		return aliPayConfig.getPid();
	}

	public static AlipayClient alipayClient;

	/**
	 * 公钥方式
	 * 
	 * @return
	 */
	public static AlipayClient getAliPayClient() {
		if (alipayClient != null) {
			return alipayClient;
		} else {
			alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", APP_ID(), APP_PRIVATE_KEY(),
					"json", CHARSET(), ALIPAY_PUBLIC_KEY(), "RSA2");
		}
		return alipayClient;
	}

	/**
	 * 公钥证书方式
	 * 
	 * @return
	 * @throws AlipayApiException
	 */
	public static AlipayClient getAliPalyClientByCert() throws AlipayApiException {
		CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
		certAlipayRequest.setServerUrl("https://openapi.alipay.com/gateway.do");
		certAlipayRequest.setAppId(APP_ID());
		certAlipayRequest.setPrivateKey(APP_PRIVATE_KEY());
		certAlipayRequest.setFormat("json");
		certAlipayRequest.setCharset(CHARSET());
		certAlipayRequest.setSignType("RSA2");
		certAlipayRequest.setCertPath(pubPath());
		certAlipayRequest.setAlipayPublicCertPath(pubJobPath());
		certAlipayRequest.setRootCertPath(rootPath());
		certAlipayRequest.setPrivateKey(APP_PRIVATE_KEY());
		alipayClient = new DefaultAlipayClient(certAlipayRequest);
		return alipayClient;
	}

	public static String getOutTradeNo() {
		int r1 = (int) (Math.random() * (10));// 产生2个0-9的随机数
		int r2 = (int) (Math.random() * (10));
		long now = System.currentTimeMillis();// 一个13位的时间戳
		String id = String.valueOf(r1) + String.valueOf(r2) + String.valueOf(now);// 订单ID
		return id;
	}

	/**
	 * 获取支付宝授权 authinfo
	 * 
	 * @return
	 */
	public static String getAuthInfoStr() {
		StringBuilder strInfo = new StringBuilder();
		strInfo.append("apiname=com.alipay.account.auth");
		strInfo.append("&app_id=" + APP_ID());
		strInfo.append("&app_name=mc");
		strInfo.append("&auth_type=AUTHACCOUNT");
		strInfo.append("&biz_type=openservice");
		strInfo.append("&method=alipay.open.auth.sdk.code.get");
		strInfo.append("&pid=" + pid());
		strInfo.append("&product_id=APP_FAST_LOGIN");
		strInfo.append("&scope=kuaijie");
		strInfo.append("&sign_type=RSA2");
		strInfo.append("&target_id=" + AliPayUtil.getOutTradeNo());
		String sign = sign(strInfo.toString());
		strInfo.append("&sign=" + sign);
		return strInfo.toString();
	}

	/**
	 * 获取或者刷新支付宝token
	 * 
	 * @param code
	 * @param token
	 * @return
	 */
	public static String getAccesstoken(String code, String token) {

		// 实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
		AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();

		request.setGrantType("authorization_code");
		request.setCode(code);
		// 刷新token
		if (token != null || token != "")
			request.setRefreshToken(token);
		try {
			// 这里和普通的接口调用不同，使用的是sdkExecute
			AlipaySystemOauthTokenResponse response = getAliPalyClientByCert().certificateExecute(request);

			System.out.println("返回order  " + response.getBody());// 就是orderString 可以直接给客户端请求，无需再做处理。

			return response.getAccessToken();
		} catch (AlipayApiException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 获取支付宝用户授权信息
	 * 
	 * @param appAuthToken
	 * @return
	 */
	public static String getAliUserInfo(String token) {

		AlipayUserInfoShareRequest request = new AlipayUserInfoShareRequest();
		try {
			AlipayUserInfoShareResponse response = getAliPalyClientByCert().certificateExecute(request, token);
			System.out.println(response.getBody());
			return response.getUserId();
		} catch (AlipayApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * create the order info. 创建订单信息
	 *
	 */
	public static String getOrderInfo(String subject, String body, String price, String orderNo) {

		// 实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
		AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
		// SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
		AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();

		model.setBody(body);
		model.setSubject(subject);
		model.setOutTradeNo(orderNo);
		model.setTimeoutExpress("30m");
		model.setTotalAmount(price);
		model.setProductCode("QUICK_MSECURITY_PAY");
		//身份验证
//		ExtUserInfo ext=new ExtUserInfo();
		
//			model.setGoodsType("0");
		request.setBizModel(model);
		request.setNotifyUrl(callBackUrl());
		System.out.println("request:" + JSON.toJSONString(request));
		try {
			// 这里和普通的接口调用不同，使用的是sdkExecute
			AlipayTradeAppPayResponse response = getAliPalyClientByCert().sdkExecute(request);
			System.out.println("request:" + JSON.toJSONString(response));
//			System.out.println("返回order  " + response.getBody());// 就是orderString 可以直接给客户端请求，无需再做处理。

			return response.getBody();
		} catch (AlipayApiException e) {
			e.printStackTrace();
			return null;
		}
	}
/**
 * h5预支付
 * @param subject
 * @param body
 * @param price
 * @param orderNo
 * @return
 */
	public static String getH5From(String subject, String body, String price, String orderNo) {

		// 实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
		AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
		// SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
		AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();

		model.setBody(body);
		model.setSubject(subject);
		model.setOutTradeNo(orderNo);
		model.setTimeoutExpress("30m");
		model.setTotalAmount(price);
		model.setProductCode("QUICK_MSECURITY_PAY");
//				model.setGoodsType("0");
		request.setBizModel(model);
		request.setNotifyUrl(callBackUrl());
		try {
			// 这里和普通的接口调用不同，使用的是pageExecute
			AlipayTradeWapPayResponse response = getAliPayClient().pageExecute(request);
			System.out.println("返回form  " + response.getBody());//// 调用SDK生成表单

			return response.getBody();
		} catch (AlipayApiException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * create the order info. 创建支付宝红包订单信息
	 *
	 */
	public static String getOrderInfoByCoupon(String subject, String body, String price, String orderNo) {

		try {
			// 实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
			AlipayFundTransAppPayRequest request = new AlipayFundTransAppPayRequest();
			// SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
//		 AlipayFundTransAppPayModel model = new AlipayFundTransAppPayModel();
//		 	model.setOutBizNo(orderNo);
//		 	model.setTransAmount(price);
//		 	model.setProductCode("STD_RED_PACKET");
//			model.setBizScene("PERSONAL_PAY");
//			model.setRemark(body);
//			model.setOrderTitle(subject);
//			model.setBusinessParams("{\"sub_biz_scene\":\"REDPACKET\",\"payer_binded_alipay_uid:\"2088631593752425\",\"payee_show_name\":\"友讯红包\"}");
////	扩展参数		model.setExtraParam();
//			request.setBizModel(model);
//			request.setNotifyUrl(callBackUrl());
			request.setBizContent("{\"out_biz_no\":\"" + orderNo + "\",\"trans_amount\":" + price + ","
					+ "\"product_code\":\"STD_RED_PACKET\",\"biz_scene\":\"PERSONAL_PAY\"," + "\"remark\":\"" + body
					+ "\",\"order_title\":\"" + subject + "\","
					+ "\"business_params\":{\"sub_biz_scene\":\"REDPACKET\",\"payer_binded_alipay_uid\":\"" + pid()
					+ "\"}}");
			// 这里和普通的接口调用不同，使用的是sdkExecute
			AlipayFundTransAppPayResponse response = getAliPalyClientByCert().sdkExecute(request);
			System.out.println("返回response  " + JSON.toJSONString(response));

			return response.getBody();
		} catch (AlipayApiException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 支付宝红包打款
	 * 
	 * @param subject
	 * @param body
	 * @param price
	 * @param orderNo
	 * @return
	 */
	public static String transUni(String subject, String body, String price, String orderNo, String aliNo, String uid) {

		// 实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
		AlipayFundTransUniTransferRequest request = new AlipayFundTransUniTransferRequest();
		// SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
		AlipayFundTransUniTransferModel model = new AlipayFundTransUniTransferModel();
		model.setOutBizNo(getOutTradeNo());
		model.setTransAmount(price);
		model.setProductCode("STD_RED_PACKET");
		model.setBizScene("PERSONAL_COLLECTION");
		Participant p = new Participant();
		// 参与方的标识类型，目前支持如下枚举：ALIPAY_USER_ID 支付宝的会员ID
		p.setIdentityType("ALIPAY_USER_ID");
		// 参与方的标识ID，比如支付宝用户UID。
		p.setIdentity(uid);
		model.setPayeeInfo(p);
		model.setRemark(body);
		model.setOrderTitle(subject);
		model.setBusinessParams("{\"sub_biz_scene\":\"REDPACKET\",\"payer_show_name\":\"友讯红包\"}");
		model.setOriginalOrderId(aliNo);
//		扩展参数		model.setExtraParam();
		request.setBizModel(model);
//		request.setNotifyUrl(callBackUrl());
		try {
			System.out.println("orderNo==>" + orderNo + "aliNo==>" + aliNo + "uid==>" + uid);
			// 这里和普通的接口调用不同，使用的是sdkExecute
			AlipayFundTransUniTransferResponse response = getAliPalyClientByCert().certificateExecute(request);

			System.out.println("红包打款: " + response.getOrderId() + "状态:" + response.getStatus());// 就是orderString
																								// 可以直接给客户端请求，无需再做处理。

			return response.getStatus();
		} catch (AlipayApiException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 红包退回
	 * 
	 */
	public static String backTransUni(String remark, String orderId, String price, String orderNo) {

		// 实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
		AlipayFundTransRefundRequest request = new AlipayFundTransRefundRequest();
		// SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
		AlipayFundTransRefundModel model = new AlipayFundTransRefundModel();
		model.setOrderId(orderId);
		model.setOutRequestNo(orderNo);
		model.setRefundAmount(price);
		model.setRemark(remark);
//		扩展参数		model.setExtraParam();
		request.setBizModel(model);
//		request.setNotifyUrl(callBackUrl());
		try {
			// 这里和普通的接口调用不同，使用的是sdkExecute
			AlipayFundTransRefundResponse response = getAliPalyClientByCert().certificateExecute(request);
			System.out.println("红包退回:" + response.getOrderId() + "状态:" + response.getStatus());// 就是orderString
																								// 可以直接给客户端请求，无需再做处理。

			return response.getOrderId();
		} catch (AlipayApiException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 查询发送红包、红包打款、红包退回订单的信息
	 * 
	 * @param orderNo
	 * @param bizScene PERSONAL_PAY，C2C现金红包-发红包PERSONAL_COLLECTION，C2C现金红包-领红包
	 * @return
	 */
	public static String commonQueryRequest(String orderNo, String aliNo, String bizScene) {

		// 实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
		AlipayFundTransCommonQueryRequest request = new AlipayFundTransCommonQueryRequest();
		// SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
		/*
		 * AlipayFundTransCommonQueryModel model = new
		 * AlipayFundTransCommonQueryModel(); // model.setOrderId(orderId);
		 * model.setOutBizNo(orderNo); model.setBizScene(bizScene);//
		 * PERSONAL_PAY，C2C现金红包-发红包PERSONAL_COLLECTION，C2C现金红包-领红包
		 * model.setProductCode("STD_RED_PACKET"); model.setOrderId(aliNo); // 扩展参数
		 * model.setExtraParam(); request.setBizModel(model); //
		 * request.setNotifyUrl(callBackUrl());
		 */
		// PERSONAL_PAY，C2C现金红包-发红包 PERSONAL_COLLECTION，C2C现金红包-领红包
		request.setBizContent("{\"product_code\":\"STD_RED_PACKET\",\"biz_scene\":\"" + bizScene
				+ "\",\"out_biz_no\":\"" + orderNo + "\",\"order_id\":\"" + aliNo + "\"}");
		try {
			// 这里和普通的接口调用不同，使用的是sdkExecute
			AlipayFundTransCommonQueryResponse response = getAliPalyClientByCert().certificateExecute(request);
			System.out.println("红包查询  " + response.getOrderId() + "状态:" + response.getStatus());// 就是orderString
																								// 可以直接给客户端请求，无需再做处理。
			if (response.isSuccess()) {
				return response.getOrderId();
			} else
				return null;

		} catch (AlipayApiException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
//		JSONObject parseObject = JSON.parseObject("{\"body\":\"alipay_root_cert_sn=687b59193f3f462dd5336e5abf83c5d8_02941eef3187dddf3d3b83462e1dfcf6&alipay_sdk=alipay-sdk-java-4.9.5.ALL&app_cert_sn=a5573f6729d940a9ee439c66032e3146&app_id=2019103168821258&biz_content={\"biz_scene\":\"PERSONAL_PAY\",\"order_id\":\"20200116110075000006820073489855\",\"out_biz_no\":\"251579161719124\",\"product_code\":\"STD_RED_PACKET\"}&charset=utf-8&format=json&method=alipay.fund.trans.common.query&sign=G7tBxQYoorXO/J7CWJRTr7Dkrmdgm7rctX3j8h/eNmyZ36FKLXpvWpKQIaxIl793cs7MpWhjVEswoxOxI1jC2VpSqrbXZEGeWHxXOoI1E4xlrn/EybJmMbdPmY7iS2bpdR1ReqpREMc9ShpprSV5NyW7Y7e3A6zem+RQlawV6FeV5Hpa+dFcvy+6Q6GLOek+zt7CNG4aAIEgAH2NZd/liV2DTNFbi0rgaCQmwxljXd5rCJ4Qj+wl+HpbkoBCPwRJsDNmp8/YO4VnW7afRJMM4ZVqedBrip6z9Ft9/3Y8d6dpGnQdTiK5vsNG/cbfxIOkJoOK3SAbHosSJqaSFf7u2g==&sign_type=RSA2&timestamp=2020-01-16 16:02:40&version=1.0\",\"success\":true}");
//		System.out.println(parseObject);
	}

	/**
	 * sign the order info. 对订单信息进行签名
	 *
	 * @param content 待签名订单信息
	 */
	public static String sign(String content) {
		return RSA.sign(content, AlipayConfig.private_key, AlipayConfig.input_charset);
	}

	/**
	 * get the sign type we use. 获取签名方式
	 *
	 */
	/*
	 * private String getSignType() { return "sign_type=\"RSA\""; }
	 */

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
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}
			// 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
			// valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
			params.put(name, valueStr);
		}
		return params;
	}

	public String getAuthInfo() throws AlipayApiException {
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
		// 最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
		headers.put("Authorization", "APPCODE " + appcode);
		// 根据API的要求，定义相对应的Content-Type
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
			// 获取response的body
//		    	System.out.println(EntityUtils.toString(response.getEntity()));
			returnStr = EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnStr;

	}

}
