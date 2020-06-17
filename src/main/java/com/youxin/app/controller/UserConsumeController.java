package com.youxin.app.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.YopResponse;
import com.yeepay.g3.sdk.yop.client.YopRsaClient;
import com.youxin.app.entity.Config;
import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.entity.RedPacket;
import com.youxin.app.entity.User;
import com.youxin.app.entity.WalletFour;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.repository.UserRepository;
import com.youxin.app.service.CodePayService;
import com.youxin.app.service.ConfigService;
import com.youxin.app.service.UserService;
import com.youxin.app.service.impl.ConsumeRecordManagerImpl;
import com.youxin.app.service.impl.RedPacketManagerImpl;
import com.youxin.app.utils.AuthServiceUtils;
import com.youxin.app.utils.CollectionUtil;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.NumberUtil;
import com.youxin.app.utils.PageResult;
import com.youxin.app.utils.PageVO;
import com.youxin.app.utils.ReqUtil;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.alipay.util.AliPayUtil;
import com.youxin.app.utils.sms.SMSServiceImpl;
import com.youxin.app.utils.wxpay.utils.WXPayConfig;
import com.youxin.app.utils.wxpay.utils.WXPayUtil;
import com.youxin.app.utils.wxpay.utils.WxPayDto;
import com.youxin.app.utils.yoppay.YeePayUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import jodd.util.URLDecoder;

@Api(tags = "用户消费管理")
@RestController
@RequestMapping("/user/consume")
public class UserConsumeController extends AbstractController {
	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository repository;
	@Autowired
	private SMSServiceImpl sendSms;
	@Autowired
	private ConsumeRecordManagerImpl consumeRecordServer;
	@Autowired
	private CodePayService payService;
	@Autowired
	RedPacketManagerImpl redServer;
	@Autowired
	ConfigService cs;
	/**
	 * 人人商城小程序支付订单
	 * @param price
	 * @param nonceStr
	 * @param time
	 * @param secret
	 * @return
	 */
	@PostMapping(value = "/rrorder")
	public Object RenrenOrder(@RequestParam(defaultValue="") String openid,@RequestParam(defaultValue="") String price,@RequestParam(defaultValue="") String nonceStr) {
		System.out.println("openid"+openid);
		System.out.println("price"+price);
		System.out.println("nonceStr"+nonceStr);
//		ConsumeRecord entity = new ConsumeRecord();
//		entity.setUserId("");
//		entity.setTime(DateUtil.currentTimeSeconds());
//		entity.setType(KConstants.ConsumeType.BUY_SHOP);
//		entity.setDesc("人人商城小程序支付");
//		entity.setStatus(KConstants.OrderStatus.CREATE);
//		entity.setTradeNo(nonceStr);
//		entity.setPayType(6);
//		entity.setMoney(new Double(price));
		
		String order = YeePayUtil.getOrderXCX(nonceStr,price,"小程序商品","测试商品");
		Assert.notNull(order,"创建订单失败");
		String otoken = JSON.parseObject(order).getString("token");
		String url = YeePayUtil.getXCXUrl(openid,otoken,getRequestIp());
//		consumeRecordServer.saveConsumeRecord(entity);
		return Result.success(url);
		
	}

	@ApiOperation(value = "用户充值", response = Result.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "payType", value = "充值类型（1支付宝，2微信,6易宝充值）", required = true, paramType = "query"),
			@ApiImplicitParam(name = "price", value = "充值金额", required = true, paramType = "query"),
			@ApiImplicitParam(name = "time", value = "充值时间", required = true, paramType = "query"),
			@ApiImplicitParam(name = "secret", value = "安全加密 md5( md5(apikey+time) +userid+token)", required = true, paramType = "query") })
	@PostMapping(value = "/recharge")
	public Object getSign(@RequestParam int payType, @RequestParam String price,@RequestParam(defaultValue="APP") String tradeType,
			@RequestParam(defaultValue = "0") long time, @RequestParam(defaultValue = "") String secret) {
		String token = getAccess_token();
		Integer userId = ReqUtil.getUserId();
		Config config = cs.getConfig();
		// 充值接口授权
		if (!AuthServiceUtils.authUser(userId + "", token, time, secret)) {
			log.debug("userId:" + userId + ",token:" + token + ",time:" + time + ",secret:" + secret);
			return Result.errorMsg("密码错误!");
		}
		Map<String, String> map = Maps.newLinkedHashMap();
		String orderInfo = "";
		if (0 < payType) {
			
			String orderNo = AliPayUtil.getOutTradeNo();
			ConsumeRecord entity = new ConsumeRecord();
			entity.setUserId(ReqUtil.getUserId());
			entity.setTime(DateUtil.currentTimeSeconds());
			entity.setType(KConstants.ConsumeType.USER_RECHARGE);
			entity.setDesc("余额充值");
			entity.setStatus(KConstants.OrderStatus.CREATE);
			entity.setTradeNo(orderNo);
			entity.setPayType(payType);
			entity.setMoney(new Double(price));
			if (KConstants.PayType.ALIPAY == payType) {
				Assert.isTrue(config.getAliState()<1, JSON.toJSONString(Result.error("支付宝充值暂不可用", config)));
//				orderInfo = AliPayUtil.getOrderInfo("余额充值", "余额充值", price, orderNo);
				if(tradeType.equals("MWEB"))
					orderInfo = AliPayUtil.getH5From("余额充值", "余额充值", price, orderNo);
				else
					orderInfo = AliPayUtil.getOrderInfo("余额充值", "余额充值", price, orderNo);
				consumeRecordServer.saveConsumeRecord(entity);
				map.put("orderInfo", orderInfo);
				System.out.println("orderInfo>>>>>" + orderInfo);
				return Result.success(map);
			} else if(KConstants.PayType.YEEPAY==payType){
				Assert.isTrue(config.getYeeState()<1, JSON.toJSONString(Result.error("银行卡充值暂不可用", config)));
				String order = YeePayUtil.getOrder(orderNo,price);
				Assert.notNull(order,"创建订单失败");
				String otoken = JSON.parseObject(order).getString("token");
				String url = YeePayUtil.getUrl(otoken);
				consumeRecordServer.saveConsumeRecord(entity);
				return Result.success(url);
			}else {
				Assert.isTrue(config.getWxState()<1, JSON.toJSONString(Result.error("微信充值暂不可用", config)));
				WxPayDto tpWxPay = new WxPayDto();
				// tpWxPay.setOpenId(openId);
				tpWxPay.setBody("余额充值");
				tpWxPay.setOrderId(orderNo);
				tpWxPay.setSpbillCreateIp(WXPayConfig.WXSPBILL_CREATE_IP);
				tpWxPay.setTotalFee(price);
				tpWxPay.setTradeType(tradeType);
				consumeRecordServer.saveConsumeRecord(entity);
				Object data = WXPayUtil.getPackage(tpWxPay);
				return Result.success(data);
			}
		}
		return Result.errorMsg("没有选择支付类型");
	}
	@ApiOperation(value = "用户发送支付宝红包", response = Result.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "payType", value = "红包类型（1支付宝红包）", required = true, paramType = "query"),
			@ApiImplicitParam(name = "price", value = "红包金额", required = true, paramType = "query"),
			@ApiImplicitParam(name = "count", value = "红包个数", required = true, paramType = "query"),
			@ApiImplicitParam(name = "greetings", value = "祝福语", required = true, paramType = "query"),
			@ApiImplicitParam(name = "toUserId", value = "发送给某人", required = false, paramType = "query"),
			@ApiImplicitParam(name = "roomJid", value = "发送给某群", required = false, paramType = "query"),
			@ApiImplicitParam(name = "time", value = "发送时间", required = true, paramType = "query"),
			@ApiImplicitParam(name = "secret", value = "安全加密 md5( md5(apikey+time) +userid+token)", required = true, paramType = "query") })
	@PostMapping(value = "/sendAliCoupon")
	public Object sendAliCoupon(@RequestParam int payType, @RequestParam String price,@RequestParam(defaultValue = "1") long count,
			@RequestParam(defaultValue = "") String greetings,
			@RequestParam(defaultValue = "") String toUserId,
			@RequestParam(defaultValue = "") String roomJid,
			@RequestParam(defaultValue = "0") long time, @RequestParam(defaultValue = "") String secret) {
		if(true) 
			return Result.error("红包已升级，请更新app至最新版本！");
		String token = getAccess_token();
		Integer userId = ReqUtil.getUserId();
		if(Double.valueOf(price) < 0.01 || 20000 < Double.valueOf(price)){
			return Result.error("红包金额在0.01~20000之间哦!");
		}
		// 充值接口授权
		if (!AuthServiceUtils.authUser(userId + "", token, time, secret)) {
			log.debug("userId:" + userId + ",token:" + token + ",time:" + time + ",secret:" + secret);
			return Result.errorMsg("权限验证失败!");
		}
		Map<String, String> map = Maps.newLinkedHashMap();
		String orderInfo = "";
		if (0 < payType) {
			String orderNo = AliPayUtil.getOutTradeNo();
			ConsumeRecord entity = new ConsumeRecord();
			entity.setUserId(ReqUtil.getUserId());
			entity.setTime(DateUtil.currentTimeSeconds());
			entity.setType(KConstants.ConsumeType.ALI_COUPON);
			entity.setDesc("支付宝红包");
			entity.setStatus(KConstants.OrderStatus.CREATE);
			entity.setTradeNo(orderNo);
			entity.setPayType(payType);
			entity.setMoney(new Double(price));
			entity.setCount(count);
			if (KConstants.PayType.ALIPAY == payType) {
				orderInfo = AliPayUtil.getOrderInfoByCoupon("支付宝红包", "支付宝红包", price, orderNo);
				consumeRecordServer.saveConsumeRecord(entity);
				map.put("orderInfo", orderInfo);
				System.out.println("orderInfo>>>>>" + URLDecoder.decode(orderInfo));
				System.out.println("orderInfo>>>>>" + orderInfo);
				return Result.success(map);
			} 

		}
		return Result.errorMsg("没有选择支付类型");
	}
	@ApiOperation(value = "用户发送支付宝红包v1", response = Result.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "price", value = "红包金额", required = true, paramType = "query"),
			@ApiImplicitParam(name = "time", value = "发送时间", required = true, paramType = "query"),
			@ApiImplicitParam(name = "secret", value = "安全加密 md5( md5(apikey+time) +userid+token)", required = true, paramType = "query") })
	@PostMapping(value = "/sendAliCouponv1")
	public Object sendAliCouponv1(
			@RequestBody RedPacket packet,@RequestParam(defaultValue = "") String price,
			@RequestParam(defaultValue = "0") long time, @RequestParam(defaultValue = "") String secret) {
		Config config = cs.getConfig();
		Assert.isTrue(config.getAliRedPacketState()<1, JSON.toJSONString(Result.error("支付宝红包暂不可用", config)));
		
		String token = getAccess_token();
		Integer userId = ReqUtil.getUserId();
		// 红包接口授权
		WalletFour wallet_Four = redServer.getUserWallet(userId, packet.getRoomJid());
		if(Double.valueOf(packet.getMoney()) < 0.01 || 20000 < Double.valueOf(packet.getMoney())){
			return Result.error("红包金额在0.01~20000之间哦!");
		}else if((packet.getMoney()/packet.getCount()) < 0.01){
			return Result.error("每人最少 0.01元 !");
		}else if(packet.getType()==4&&CollectionUtil.isEmpty(packet.getToUserIds())){
			return Result.error("定向红包请选择指定领取人!");
		}
		// 充值接口授权
		if (!AuthServiceUtils.authUser(userId + "", token, time, secret)) {
//			log.debug("userId:" + userId + ",token:" + token + ",time:" + time + ",secret:" + secret);
			return Result.errorMsg("权限验证失败!");
		}
		if (null != wallet_Four && wallet_Four.getIsSetUpMoney() == 1) {
			packet.setCount(wallet_Four.getRedPackegeNumber());
			packet.setMoney(wallet_Four.getRedPackgeMoney());
			price=wallet_Four.getRedPackgeMoney().toString();
		} 
		//支付宝红包
		if(packet.getPayType()==1) {
			BigDecimal money = NumberUtil.getBigDecimalForDouble(packet.getMoney());
			BigDecimal count = NumberUtil.getBigDecimalForDouble(packet.getCount());
			//单个红包金额
			double divideMoney = money.divide(count,2,BigDecimal.ROUND_HALF_UP).doubleValue();
//			log.debug("money:"+money+"count:"+count+"=divideMoney:"+divideMoney);
			if(divideMoney>200)
				return Result.error("单个红包不能超过200元");
			Double sendTotalMoney = redServer.sendBill(userId);
			if(NumberUtil.add(sendTotalMoney, packet.getMoney(), null)>20000) 
				return Result.error("每日发送总金额不能超过20000,您24小时内已经发送了"+sendTotalMoney+"元红包");
		}
		
		Map<String, String> map = Maps.newLinkedHashMap();
		String orderInfo = "";
		if (0 < packet.getPayType()) {
			String orderNo = AliPayUtil.getOutTradeNo();
			ConsumeRecord entity = new ConsumeRecord();
			entity.setUserId(ReqUtil.getUserId());
			entity.setTime(DateUtil.currentTimeSeconds());
			entity.setType(KConstants.ConsumeType.ALI_COUPON);
			entity.setDesc("支付宝红包");
			entity.setStatus(KConstants.OrderStatus.CREATE);
			entity.setTradeNo(orderNo);
			entity.setPayType(packet.getPayType());
			entity.setMoney(new Double(price));
			if(packet.getToUserId()!=null)
				entity.setToUserId(String.valueOf(packet.getToUserId()));
			entity.setRoomJid(String.valueOf(packet.getRoomJid()));
			entity.setCount(packet.getCount());
			entity.setRedType(packet.getType());
			entity.setGreetings(packet.getGreetings());
			entity.setUserName(packet.getUserName());
			entity.setToUserIds(packet.getToUserIds()==null?new ArrayList<Integer>():packet.getToUserIds());
			if (KConstants.PayType.ALIPAY == packet.getPayType()) {
				orderInfo = AliPayUtil.getOrderInfoByCoupon("支付宝红包", "支付宝红包", price, orderNo);
				consumeRecordServer.saveConsumeRecord(entity);
				map.put("orderInfo", orderInfo);
//				System.out.println("orderInfo>>>>>" + URLDecoder.decode(orderInfo));
//				System.out.println("orderInfo>>>>>" + orderInfo);
				return Result.success(map);
			} 

		}
		return Result.errorMsg("没有选择支付类型");
	}
	@ApiOperation(value = "退还支付宝发送的金额", response = Result.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aliNo", value = "支付宝订单号", required = true, paramType = "query"),
			@ApiImplicitParam(name = "outNo", value = "平台订单号", required = true, paramType = "query"),
			@ApiImplicitParam(name = "moneyStr", value = "退款金额", required = true, paramType = "query"),
			})
	@PostMapping(value = "/backAliRedPacket")
	public Object backAliRedPacket(@RequestParam String aliNo, @RequestParam String outNo,
			@RequestParam(defaultValue = "PERSONAL_PAY") String moneyStr ) {
		
		String orderId = AliPayUtil.backTransUni("友讯红包退款", aliNo, moneyStr, outNo);
		if(StringUtil.isEmpty(orderId))
			return Result.error();
		return Result.success();
	}
	@ApiOperation(value = "查询支付宝红包实际付款打款状态", response = Result.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "aliNo", value = "支付宝订单号", required = true, paramType = "query"),
			@ApiImplicitParam(name = "outNo", value = "平台订单号", required = true, paramType = "query"),
			@ApiImplicitParam(name = "type", value = "查询类型  PERSONAL_PAY，C2C现金红包-发红包 PERSONAL_COLLECTION，C2C现金红包-领红包", defaultValue="PERSONAL_PAY", required = true, paramType = "query"),
			})
	@PostMapping(value = "/queryAliNoStatus")
	public Object queryAliNoStatus(@RequestParam String aliNo, @RequestParam String outNo,
			@RequestParam(defaultValue = "PERSONAL_PAY") String type ) {
		
		String orderId = AliPayUtil.commonQueryRequest(outNo, aliNo, type);
		if(StringUtil.isEmpty(orderId))
			return Result.error();
		return Result.success();
	}

	@ApiOperation(value = "用户充值记录", response = Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "pageIndex", value = "页码", defaultValue = "0", paramType = "query"),
			@ApiImplicitParam(name = "pageSize", value = "长度", defaultValue = "10", paramType = "query") })
	@GetMapping("/user/rechargeRecord")
	public Object getList(@RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "10") int pageSize) {
		System.out.println(pageIndex);
		System.out.println(pageSize);
		Object data = consumeRecordServer.reChargeList(ReqUtil.getUserId(), pageIndex, pageSize);
		return Result.success(data);
	}

	@ApiOperation(value = "用户消费记录", response = Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "pageIndex", value = "页码", defaultValue = "0", paramType = "query"),
			@ApiImplicitParam(name = "pageSize", value = "长度", defaultValue = "10", paramType = "query") })
	@GetMapping("/user/consumeRecord")
	public Object consumeRecordList(@RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "10") int pageSize) {
		try {
			Integer userId = ReqUtil.getUserId();
			if (userId == null || userId < 0) {
				return Result.errorMsg("登录过期，请重新登录!");
			}
			PageResult<ConsumeRecord> result = consumeRecordServer.consumeRecordList(userId, pageIndex, pageSize);
			PageVO data = new PageVO(result.getData(), result.getCount(), pageIndex, pageSize);
			return Result.success(data);
		} catch (Exception e) {
			return Result.errorMsg(e.getMessage());
		}

	}

	/**
	 * 二维码收款
	 * 
	 * @param toUserId 收款人（金钱增加）
	 * @param money
	 * @param secret
	 * @return
	 */
	@ApiOperation(value = "二维码收款", response = Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "toUserId", value = "收款方id", required = true, paramType = "query"),
			@ApiImplicitParam(name = "money", value = "收款金额", required = true, paramType = "query"),
			@ApiImplicitParam(name = "desc", value = "描述", paramType = "query"),
			@ApiImplicitParam(name = "secret", value = "密钥(md5(md5(apiKey+time+money+payPassword)+userId+token))", required = true, paramType = "query"),
			@ApiImplicitParam(name = "time", value = "时间", required = true, paramType = "query") })
	@PostMapping(value = "/codeReceive")
	public Object codeReceive(@RequestParam(defaultValue = "") Integer toUserId,
			@RequestParam(defaultValue = "") String money, @RequestParam(defaultValue = "0") long time,
			@RequestParam(defaultValue = "") String desc, @RequestParam(defaultValue = "") String secret) {
		Config config = cs.getConfig();
		Assert.isTrue(config.getCodeReceiveState()<1, JSON.toJSONString(Result.error("二维码收款暂不可用", config)));
		Integer userId = ReqUtil.getUserId();
		if (userId == toUserId) {
			return Result.errorMsg("不支持向自己付款");
		}
		String token = getAccess_token();
		User user = userService.getUserFromDB(userId);
		// 校验加密规则
		if (!AuthServiceUtils.authQRCodeReceipt(userId.toString(), token, money, time, user.getPayPassword(), secret)) {
			return Result.errorMsg("支付密码错误");
		}

		try {
			payService.receipt(userId, toUserId, money, desc);
			return Result.success();
		} catch (Exception e) {
			e.printStackTrace();
			return Result.errorMsg(e.getMessage());
		}
	}

	/**
	 * 条码、付款码支付(付款)
	 * 
	 * @param paymentCode
	 * @param money
	 * @param secret
	 * @return
	 */
	@ApiOperation(value = "二维码付款", response = Result.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "paymentCode", value = "付款码((userId+n+opt)长度+(userId+n+opt)+opt+(time/opt))   n=9,<100opt<999随机数,time当前时间精确到秒", required = true, paramType = "query"),
			@ApiImplicitParam(name = "money", value = "付款金额", required = true, paramType = "query"),
			@ApiImplicitParam(name = "desc", value = "描述", paramType = "query"),
			@ApiImplicitParam(name = "secret", value = "密钥(md5(md5(apikey+time+money+paymentCode)+userId+token))", required = true, paramType = "query"),
			@ApiImplicitParam(name = "time", value = "时间", required = true, paramType = "query") })
	@PostMapping(value = "/codePayment")
	public Object codePayment(@RequestParam(defaultValue = "") String paymentCode,
			@RequestParam(defaultValue = "") String money, @RequestParam(defaultValue = "0") long time,
			@RequestParam(defaultValue = "") String desc, @RequestParam(defaultValue = "") String secret) {
		// 解析付款码
		Integer fromUserId = payService.analysisCode(paymentCode);
		if (fromUserId == null) {
			return Result.errorMsg("付款码错误");
		}
		// 校验付款码唯一性
		if (payService.checkPaymentCode(fromUserId, paymentCode)) {
			return Result.error("付款码已失效");
		}
		Integer userId = ReqUtil.getUserId();

		// 校验加密规则
		if (!AuthServiceUtils.authPaymentCode(paymentCode, userId.toString(), money, getAccess_token(), time, secret)) {
			return Result.error("付款码支付失败，授权验证失败");
		}

		try {
			// 用户金额操作
			payService.paymentCodePay(paymentCode, userId, fromUserId, money, desc);
			return Result.success();
		} catch (ServiceException e) {
			return Result.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(e.getMessage());
		}

	}
}
