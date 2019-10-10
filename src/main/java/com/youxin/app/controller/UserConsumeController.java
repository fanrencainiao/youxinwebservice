package com.youxin.app.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;
import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.entity.User;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.repository.UserRepository;
import com.youxin.app.service.CodePayService;
import com.youxin.app.service.UserService;
import com.youxin.app.service.impl.ConsumeRecordManagerImpl;
import com.youxin.app.utils.AuthServiceUtils;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.PageResult;
import com.youxin.app.utils.PageVO;
import com.youxin.app.utils.ReqUtil;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.alipay.util.AliPayUtil;
import com.youxin.app.utils.sms.SMSServiceImpl;
import com.youxin.app.utils.wxpay.utils.WXPayConfig;
import com.youxin.app.utils.wxpay.utils.WXPayUtil;
import com.youxin.app.utils.wxpay.utils.WxPayDto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

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

	@ApiOperation(value = "用户充值", response = Result.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "payType", value = "充值类型（1支付宝，2微信）", required = true, paramType = "query"),
			@ApiImplicitParam(name = "price", value = "充值金额", required = true, paramType = "query"),
			@ApiImplicitParam(name = "time", value = "充值时间", required = true, paramType = "query"),
			@ApiImplicitParam(name = "secret", value = "安全加密", required = true, paramType = "query") })
	@PostMapping(value = "/recharge")
	public Object getSign(@RequestParam int payType, @RequestParam String price,
			@RequestParam(defaultValue = "0") long time, @RequestParam(defaultValue = "") String secret) {
		String token = getAccess_token();
		Integer userId = ReqUtil.getUserId();
		// 充值接口授权
		if (!AuthServiceUtils.authUser(userId + "", token, time, secret)) {
			System.out.println("userId:" + userId + ",token:" + token + ",time:" + time + ",secret:" + secret);
			return Result.errorMsg("权限验证失败!");
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
				orderInfo = AliPayUtil.getOrderInfo("余额充值", "余额充值", price, orderNo);
				consumeRecordServer.saveConsumeRecord(entity);
				map.put("orderInfo", orderInfo);
				System.out.println("orderInfo>>>>>" + orderInfo);
				return Result.success(map);
			} else {
				WxPayDto tpWxPay = new WxPayDto();
				// tpWxPay.setOpenId(openId);
				tpWxPay.setBody("余额充值");
				tpWxPay.setOrderId(orderNo);
				tpWxPay.setSpbillCreateIp(WXPayConfig.WXSPBILL_CREATE_IP);
				tpWxPay.setTotalFee(price);
				consumeRecordServer.saveConsumeRecord(entity);
				Object data = WXPayUtil.getPackage(tpWxPay);
				return Result.success(data);
			}
		}
		return Result.errorMsg("没有选择支付类型");
	}

	@ApiOperation(value = "用户充值记录", response = Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "pageIndex", value = "页码", defaultValue = "0", paramType = "query"),
			@ApiImplicitParam(name = "pageSize", value = "长度", defaultValue = "10", paramType = "query") })
	@GetMapping("/user/rechargeRecord")
	public Object getList(@RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "10") int pageSize) {
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
