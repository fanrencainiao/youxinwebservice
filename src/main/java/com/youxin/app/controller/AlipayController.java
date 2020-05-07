package com.youxin.app.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.entity.RedPacket;
import com.youxin.app.entity.RedPacket.SendRedPacket;
import com.youxin.app.entity.User;
import com.youxin.app.entity.msgbody.MsgBody;
import com.youxin.app.entity.msgbody.MsgBody.ID;
import com.youxin.app.repository.ConsumeRecordRepository;
import com.youxin.app.service.UserService;
import com.youxin.app.service.impl.ConsumeRecordManagerImpl;
import com.youxin.app.service.impl.RedPacketManagerImpl;
import com.youxin.app.utils.BeanUtils;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.Md5Util;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.ThreadUtil;
import com.youxin.app.utils.alipay.util.AliPayParam;
import com.youxin.app.utils.alipay.util.AliPayUtil;
import com.youxin.app.utils.alipay.util.AliRedPacketParam;
import com.youxin.app.utils.supper.Callback;
import com.youxin.app.yx.SDKService;
import com.youxin.app.yx.request.MsgRequest;

@RestController
@RequestMapping("/alipay")
public class AlipayController {
	protected static Log log = LogFactory.getLog("pay");

//	@Autowired
//	private TransfersRecordManagerImpl transfersManager;
	@Autowired
	private ConsumeRecordRepository crpository;
	@Autowired
	private ConsumeRecordManagerImpl cr;
	@Autowired
	private UserService userService;

	@Autowired
	@Qualifier("get")
	private Datastore dfds;
	@Autowired
	RedPacketManagerImpl redServer;

	@RequestMapping("/callBack")
	public String payCheck(HttpServletRequest request, HttpServletResponse response) {
		log.debug("支付宝回调request    " + request);
		log.debug("支付宝request.getParameterMap    " + request.getParameterMap());

		Map<String, String> params = new HashMap<String, String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}
			// 乱码解决，这段代码在出现乱码时使用。
			// valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
			params.put(name, valueStr);
		}
		try {
			String tradeNo = params.get("out_trade_no");
			log.debug("订单号    " + tradeNo);
//			公钥方式验签
//			boolean flag = AlipaySignature.rsaCheckV1(params,AliPayUtil.ALIPAY_PUBLIC_KEY(), AliPayUtil.CHARSET(),"RSA2");
//			公钥证书方式验签
			boolean flag = AlipaySignature.rsaCertCheckV1(params, AliPayUtil.pubJobPath(), AliPayUtil.CHARSET(),
					"RSA2");
			if (flag) {
				log.debug("支付宝回调成功" + flag);

				String tradeStatus = params.get("trade_status");// 获取交易状态

				ConsumeRecord entity = cr.getConsumeRecordByNo(tradeNo);
				long total_amount = new BigDecimal(params.get("total_amount")).multiply(new BigDecimal(100))
						.longValue();
				long sys_total_amount = new BigDecimal(entity.getMoney()+"").multiply(new BigDecimal(100)).longValue();
				log.debug("支付宝支付成功金额：" + total_amount);
				log.debug("支付宝支付成功本系统中金额：" + entity.getMoney());
				log.debug("支付宝支付返回订单状态：" + tradeStatus);
				if (entity.getStatus() != KConstants.OrderStatus.END
						&& ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus))
						&& total_amount == sys_total_amount) {
					entity.setStatus(KConstants.OrderStatus.END);
					// 把支付宝返回的订单信息存到数据库
					AliPayParam aliCallBack = new AliPayParam();
					BeanUtils.populate(aliCallBack, params);
					log.debug("支付宝支付返回用户id：" + entity.getUserId());
					User user = new User();
					log.debug("支付宝支付返回用户：" + user);
					user.setAliUserId(aliCallBack.getBuyer_id());
//					userService.updateUserByEle(user);
					log.debug("支付宝支付返回aliid：" + aliCallBack.getBuyer_id());
					crpository.save(entity);
					log.debug("支付宝支付返回保存消费记录：");
					dfds.save(aliCallBack);
					log.debug("支付宝支付返回保存支付宝消费记录：");
					userService.rechargeUserMoeny(entity.getUserId(), entity.getMoney(), KConstants.MOENY_ADD);
					log.debug("支付宝支付进行金额处理：");
				}

				return "success";
			} else {
				log.debug("支付宝回调失败" + flag);
				return "failure";

			}
		} catch (AlipayApiException e) {
			e.printStackTrace();
			log.debug("支付宝回调失败" + e.getMessage());
			return "failure";
		}
	}

	public static void main(String[] args) throws InterruptedException {
//		JSONObject alicontent = new JSONObject();
//		System.out.println(Optional.ofNullable(null).orElse("12"));
//		Optional<String> ofNullable = Optional.ofNullable("  ");
//		System.out.println(ofNullable);
//		System.out.println(new BigDecimal(alicontent.getString("sd") == null ? "0" : alicontent.getString("sd")));
//		System.out.println(new BigDecimal(0.03d).multiply(new BigDecimal(100)).longValue());

//		for (int j2 = 0; j2 < 1000; j2++) {
//		
//			System.out.println(j2);
//			tothread(j2,j2+1);
//		}
	}

	private static void tothread(int j2, int j) {
		ThreadUtil.executeInThread(new Callback() {

			@Override
			public void execute(Object obj) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// TODO Auto-generated method stub
				System.out.println(j2 == (j - 1));
			}
		});
	}

	@RequestMapping("/change")
	public String change(HttpServletRequest request, HttpServletResponse response) {
		log.debug("支付宝支付状态改变 request    " + request);
		log.debug("支付宝request.getParameterMap    " + request.getParameterMap());

		Map<String, String> params = new HashMap<String, String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}
			// 乱码解决，这段代码在出现乱码时使用。
			// valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
			params.put(name, valueStr);
		}
		try {

			log.debug("params  " + params);
			JSONObject alicontent = JSON.parseObject(params.get("biz_content"));

//			公钥方式验签
//			boolean flag = AlipaySignature.rsaCheckV1(params,AliPayUtil.ALIPAY_PUBLIC_KEY(), AliPayUtil.CHARSET(),"RSA2");
//			公钥证书方式验签
			boolean flag = AlipaySignature.rsaCertCheckV1(params, AliPayUtil.pubJobPath(), AliPayUtil.CHARSET(),
					"RSA2");
			if (flag) {
				ThreadUtil.executeInThread(new Callback() {

					@Override
					public void execute(Object obj) {
						log.debug("订单变更验签成功");

						String msg_method = params.get("msg_method");// 订单变更，alipay.fund.trans.order.changed,超时退回
																		// alipay.fund.trans.refund.success
						log.debug("订单变更接口：" + msg_method);

						String aliNo = alicontent.getString("order_id");
						log.debug("变更订单aliNo    " + aliNo);

						String status = alicontent.getString("status");// 获取交易状态
						log.debug("订单变更订单状态：" + status);

						String tradeNo = alicontent.getString("out_biz_no");
						log.debug("变更订单号    " + tradeNo);

						long trans_amount = new BigDecimal(alicontent.getString("trans_amount") == null ? "0"
								: alicontent.getString("trans_amount")).multiply(new BigDecimal(100)).longValue();
						log.debug("订单变更金额：" + alicontent.getString("trans_amount"));
						log.debug("订单变更金额：" + trans_amount);
						String biz_scene = alicontent.getString("biz_scene"); // 1、PERSONAL_PAY，C2C现金红包-发红包；
																				// 2、PERSONAL_COLLECTION，C2C现金红包-领红包
																				// ；3、DIRECT_TRANSFER，B2C现金红包
						log.debug("订单变更领取还是支付方式：" + biz_scene);
						String pay_date = alicontent.getString("pay_date");// 支付完成 时间
						log.debug("订单变更时间：" + pay_date);

						String refund_date = alicontent.getString("refund_date");// 退款时间
						log.debug("订单退款时间：" + refund_date);
						// 退款金额
						long refund_amount = new BigDecimal(alicontent.getString("refund_amount") == null ? "0"
								: alicontent.getString("refund_amount")).multiply(new BigDecimal(100)).longValue();
						log.debug("订单退款金额：" + refund_amount);
						String out_request_no = alicontent.getString("out_request_no");// 退款订单号
						log.debug("订单退款订单号：" + out_request_no);
						log.debug("\"alipay.fund.trans.order.changed\".equals(msg_method)："
								+ "alipay.fund.trans.order.changed".equals(msg_method));
						if ("alipay.fund.trans.order.changed".equals(msg_method)) {
							ConsumeRecord entity = cr.getConsumeRecordByNo(tradeNo);
							if (entity == null) {
								log.debug("未查询到消费记录" + tradeNo);
								return;
							}
							log.debug("userId:" + entity.getUserId());
//							if(entity.getUserId()!=10001148&&entity.getUserId()!=10000006&&entity.getUserId()!=10000192&&entity.getUserId()!=10001156)
//								return "success";
							long sys_amount = new BigDecimal(String.valueOf(entity.getMoney()))
									.multiply(new BigDecimal(100)).longValue();
							log.debug("订单变更本系统中金额：" + entity.getMoney());
							log.debug("订单变更本系统中金额：" + sys_amount);
							log.debug("\"PERSONAL_PAY\".equals(biz_scene)：" + "PERSONAL_PAY".equals(biz_scene));
							if ("PERSONAL_PAY".equals(biz_scene)) {
								log.debug("entity.getStatus()：" + entity.getStatus());
								if (entity.getStatus() != KConstants.OrderStatus.END && "SUCCESS".equals(status)
										&& sys_amount == trans_amount) {
									// 订单结束
									entity.setStatus(KConstants.OrderStatus.END);

									// 保存红包
									RedPacket packet = new RedPacket();
									packet.setUserId(entity.getUserId());
									packet.setAccid(Md5Util.md5HexToAccid(entity.getUserId() + ""));
									packet.setUserName(entity.getUserName());
									packet.setAliPayNo(aliNo);
									packet.setPayNo(tradeNo);
									packet.setMoney(Double.valueOf(entity.getMoney()));
									packet.setOver(Double.valueOf(entity.getMoney()));
									packet.setCount((int) entity.getCount());
									packet.setToUserIds(entity.getToUserIds() == null ? new ArrayList<Integer>()
											: entity.getToUserIds());
									long cuTime = DateUtil.currentTimeSeconds();
									packet.setSendTime(cuTime);
									packet.setOutTime(cuTime + KConstants.Expire.DAY1);
									packet.setPayType(1);
									packet.setGreetings(entity.getGreetings());
									packet.setType(entity.getRedType());
									if (!StringUtil.isEmpty(entity.getToUserId()) && !"0".equals(entity.getToUserId())
											&& !"null".equals(entity.getToUserId()))
										packet.setToUserId(Integer.valueOf(entity.getToUserId()));
									if (entity.getRoomJid() != null)
										packet.setRoomJid(entity.getRoomJid());
									// 发送红包消息-需沟通
									RedPacket saveRed = redServer.saveRedPacket(packet);

									MsgRequest messageBean = new MsgRequest();
									messageBean.setType(100);// 自定义

									User toUser = userService.getUser(packet.getToUserId());
									if (toUser != null) {
										messageBean.setOpe(0);// 个人消息
										messageBean.setFrom(Md5Util.md5HexToAccid(packet.getUserId() + ""));
										messageBean.setTo(Md5Util.md5HexToAccid(packet.getToUserId() + ""));
										log.debug("touserId" + packet.getToUserId());
									} else {
										messageBean.setOpe(1);// 个人消息
										messageBean.setFrom(Md5Util.md5HexToAccid(packet.getUserId() + ""));
										messageBean.setTo(packet.getRoomJid());
										log.debug("roomid" + packet.getRoomJid());
									}

									messageBean.setBody(JSON.toJSONString(new MsgBody(0,
											KConstants.MsgType.SENDREDPACKET,
											new SendRedPacket(saveRed.getId().toString(), saveRed.getGreetings(),
													saveRed.getType(), saveRed.getAccid(), 0,
													String.valueOf(packet.getMoney()),1))));

									try {
										JSONObject json = SDKService.sendMsg(messageBean);
										if (json.getInteger("code") != 200)
											log.debug("红包发送 sdk消息发送失败");
									} catch (Exception e) {
										e.printStackTrace();
										log.debug("红包发送 sdk消息发送失败" + e.getMessage());
									}
									saveData(params, entity);

								}

							} else if ("PERSONAL_COLLECTION".equals(biz_scene)) {// 领取红包

							} else {

							}
						} else if ("alipay.fund.trans.refund.success".equals(msg_method)) {
							if ("SUCCESS".equals(status)) {
								RedPacket redPacket = redServer.getRedPacketByAliPayNo(aliNo);
								if (redPacket == null)
									log.debug("不存在此ali订单号" + aliNo);
								User toUser = userService.getUser(redPacket.getToUserId());
								MsgRequest messageBean = new MsgRequest();
								messageBean.setType(100);// 自定义
								messageBean.setOpe(0);// 个人消息
								if (toUser != null) {
									messageBean.setFrom(Md5Util.md5HexToAccid(redPacket.getToUserId() + ""));
								} else {
									messageBean.setFrom(Md5Util.md5HexToAccid("1100"));
								}
								messageBean.setTo(Md5Util.md5HexToAccid(redPacket.getUserId() + ""));
								ID ids = new ID();
								ids.setId(redPacket.getId().toString());
								messageBean.setBody(
										JSON.toJSONString(new MsgBody(0, KConstants.MsgType.BACKREDPACKET, ids)));
								try {
									JSONObject json = SDKService.sendMsg(messageBean);
									if (json.getInteger("code") != 200)
										log.debug("红包退款 sdk消息发送失败");
									else
										log.debug("红包退款 sdk消息发送成功");
								} catch (Exception e) {
									e.printStackTrace();
									log.debug("红包退款 sdk消息发送失败" + e.getMessage());
								}
								saveData(params, null);
							}
						} else {
							log.debug("其他接口" + msg_method);
						}

					}
				});

				return "success";
			} else {
				log.debug("支付宝变更状态失败" + flag);
				return "fail";
			}
		} catch (AlipayApiException e) {
			e.printStackTrace();
			log.debug("支付宝变更状态失败" + e.getMessage());
			return "fail";
		}
	}

	/**
	 * 修改订单状态以及保存支付订单信息
	 * 
	 * @param params
	 * @param entity
	 */
	private void saveData(Map<String, String> params, ConsumeRecord entity) {

		// 把支付宝返回的订单信息存到数据库
		AliRedPacketParam aliCallBack = new AliRedPacketParam();
		BeanUtils.populate(aliCallBack, params);
		if (entity != null) {
			Key<ConsumeRecord> save = crpository.save(entity);
			log.debug("支付宝支付返回保存消费记录：" + save);
		}
		Key<AliRedPacketParam> save2 = dfds.save(aliCallBack);
		log.debug("支付宝支付返回保存支付宝消费记录：" + save2);
	}

	/**
	 * 支付宝提现
	 * 
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
