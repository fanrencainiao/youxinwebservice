package com.youxin.app.controller;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayFundTransOrderQueryRequest;
import com.alipay.api.request.AlipayFundTransToaccountTransferRequest;
import com.alipay.api.response.AlipayFundTransOrderQueryResponse;
import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;
import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.entity.User;
import com.youxin.app.repository.ConsumeRecordRepository;
import com.youxin.app.repository.UserRepository;
import com.youxin.app.service.UserService;
import com.youxin.app.service.impl.ConsumeRecordManagerImpl;
import com.youxin.app.utils.BeanUtils;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.alipay.util.AliPayParam;
import com.youxin.app.utils.alipay.util.AliPayUtil;



@RestController
@RequestMapping("/alipay")
public class AlipayController extends AbstractController{
	
//	@Autowired
//	private TransfersRecordManagerImpl transfersManager;
	@Autowired
	private ConsumeRecordRepository crpository;
	@Autowired
	private ConsumeRecordManagerImpl cr;
	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository repository;
	@Autowired
	@Qualifier("get")
	private Datastore dfds;
	@RequestMapping("/callBack")
	public String payCheck(HttpServletRequest request, HttpServletResponse response){
		Map<String,String> params = new HashMap<String,String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
		    String name = (String) iter.next();
		    String[] values = (String[]) requestParams.get(name);
		    String valueStr = "";
		    for (int i = 0; i < values.length; i++) {
		        valueStr = (i == values.length - 1) ? valueStr + values[i]
		                    : valueStr + values[i] + ",";
		  	}
		    //乱码解决，这段代码在出现乱码时使用。
			//valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
			params.put(name, valueStr);
		}
		try {
			String tradeNo = params.get("out_trade_no");
			log.info("订单号    "+tradeNo);
			
			boolean flag = AlipaySignature.rsaCheckV1(params,AliPayUtil.ALIPAY_PUBLIC_KEY, AliPayUtil.CHARSET,"RSA2");
			if(flag){
				log.info("支付宝回调成功"+flag);

				String tradeStatus = params.get("trade_status");// 获取交易状态 
				
				ConsumeRecord entity = cr.getConsumeRecordByNo(tradeNo);
				long total_amount = new BigDecimal(params.get("total_amount")).multiply(new BigDecimal(100)).longValue();
				long sys_total_amount=new BigDecimal(entity.getMoney()).multiply(new BigDecimal(100)).longValue();
				System.out.println("支付宝支付成功金额："+total_amount);
				System.out.println("支付宝支付成功本系统中金额："+entity.getMoney());
				System.out.println("支付宝支付返回订单状态："+tradeStatus);
				if(entity.getStatus()!=KConstants.OrderStatus.END
						&&("TRADE_SUCCESS".equals(tradeStatus)||"TRADE_FINISHED".equals(tradeStatus))
						&&total_amount==sys_total_amount){
					entity.setStatus(KConstants.OrderStatus.END);
					//把支付宝返回的订单信息存到数据库
					AliPayParam aliCallBack=new AliPayParam();
					BeanUtils.populate(aliCallBack, params);
					User user=userService.getUser(entity.getUserId());
					user.setAliUserId(aliCallBack.getBuyer_id());
					crpository.save(entity);
					dfds.save(aliCallBack);
					userService.rechargeUserMoeny(entity.getUserId(), entity.getMoney(), KConstants.MOENY_ADD);
				}

				return "success";
			}else{
				log.info("支付宝回调失败"+flag);
				return "failure";
				
			}
		} catch (AlipayApiException e) {
			e.printStackTrace();
			log.info("支付宝回调失败"+e.getMessage());
			return "failure";
		}
	}
	
	/**
	 * 支付宝提现
	 * @param amount
	 * @param time
	 * @param secret
	 * @param callback
	 * @return
	 */
//	@RequestMapping(value = "/transfer")
//	public JSONMessage transfer(@RequestParam(defaultValue="") String amount,@RequestParam(defaultValue="0") long time,
//			@RequestParam(defaultValue="") String secret, String callback){
//		if(StringUtil.isEmpty(amount)) {
//			return JSONMessage.failure("请输入提现金额！");
//		}else if(StringUtil.isEmpty(secret)) {
//			return JSONMessage.failure("缺少提现密钥");
//		}
//		String orderId=AliPayUtil.getOutTradeNo();
//		int userId = ReqUtil.getUserId();
//		User user=SKBeanUtils.getUserManager().get(userId);
//		String token = getAccess_token();
//		if(StringUtil.isEmpty(user.getAliUserId())){
//			return JSONMessage.failure("请先 支付宝授权 没有授权不能提现 ");
//		}else if(!AuthServiceUtils.authWxTransferPay(user.getPayPassword(),userId+"", token, amount,user.getAliUserId(),time, secret)){
//			return JSONMessage.failure("授权认证失败");
//		}
//		// 提现金额
//		double total=(Double.valueOf(amount));
//		
//		/**
//		 * 提现手续费 0.6%
//		 * 支付宝是没有手续费，但是因为充值是收取0.6%费用，在这里提现收取0.6%的费用
//		 */
//		DecimalFormat df = new DecimalFormat("#.00");
//		double fee =Double.valueOf(df.format(total*0.006));
//		if(0.01>fee) {
//			fee=0.01;
//		}else  {
//			fee=NumberUtil.getCeil(fee, 2);
//		}
//		
//		/**
//		 * 
//		 * 实际到账金额  = 提现金额-手续费
//		 */
//		Double totalFee= Double.valueOf(df.format(total-fee));
//		
//		if(totalFee>user.getBalance()) {
//			return JSONMessage.failure("账号余额不足 请先充值 ");
//		}
//		
//		AliPayTransfersRecord record=new AliPayTransfersRecord();
//		record.setUserId(userId);
//		record.setAppid(AliPayUtil.APP_ID);
//		record.setOutTradeNo(orderId);
//		record.setAliUserId(user.getAliUserId());
//		record.setTotalFee(amount);
//		record.setFee(fee+"");
//		record.setRealFee(totalFee+"");
//		record.setCreateTime(DateUtil.currentTimeSeconds());
//		record.setStatus(0);
//		
//		AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest();
////		request.setBizModel(bizModel);
//		
//		request.setBizContent("{" +
//				"    \"out_biz_no\":\""+orderId+"\"," +  // 订单Id
//				"    \"payee_type\":\"ALIPAY_USERID\"," + // 收款人的账户类型
//				"    \"payee_account\":\""+user.getAliUserId()+"\"," + // 收款人
//				"    \"amount\":\""+totalFee+"\"," +	// 金额
//				"    \"payer_show_name\":\"余额提现\"," +
//				"    \"remark\":\"转账备注\"," +
//				"  }");
//		try {
//			AlipayFundTransToaccountTransferResponse response = AliPayUtil.getAliPayClient().execute(request);
//			System.out.println("支付返回结果  "+response.getCode());
//			if(response.isSuccess()){
//				record.setResultCode(response.getCode());
//				record.setCreateTime(DateUtil.toTimestamp(response.getPayDate()));
//				record.setStatus(1);
//				transfersManager.transfersToAliPay(record);
//				
//				logger.info("支付宝提现成功");
//				return JSONMessage.success();
//			} else {
//				record.setErrCode(response.getErrorCode());
//				record.setErrDes(response.getMsg());
//				record.setStatus(-1);
//				transfersManager.saveEntity(record);
//				logger.info("支付宝提现失败");
//				return JSONMessage.failure("支付宝提现失败");
//			}
//		} catch (AlipayApiException e) {
//			e.printStackTrace();
//			return JSONMessage.failure("支付宝提现失败");
//		}
//		
//	}
//	
//	/**
//	 * 支付宝提现查询
//	 * @param tradeno
//	 * @param callback
//	 * @return
//	 */
//	@RequestMapping(value ="/aliPayQuery")
//	public JSONMessage aliPayQuery(String tradeno,String callback){
//		if (StringUtil.isEmpty(tradeno)) {
//			return null;
//		}
//		AlipayFundTransOrderQueryRequest request = new AlipayFundTransOrderQueryRequest();
//		request.setBizContent("{" +
//				"\"out_biz_no\":\""+tradeno+"\"," + // 订单号
//				"\"order_id\":\"\"" +
//				"  }");
//		try {
//			AlipayFundTransOrderQueryResponse response = AliPayUtil.getAliPayClient().execute(request);
//			System.out.println("支付返回结果  "+response.getCode());
//			if(response.isSuccess()){
//				System.out.println("调用成功");
//			} else {
//				System.out.println("调用失败");
//			}
//		} catch (AlipayApiException e) {
//			e.printStackTrace();
//		}
//
//		return JSONMessage.success();
//	}
//	
	
}
