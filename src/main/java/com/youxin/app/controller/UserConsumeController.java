package com.youxin.app.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;
import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.repository.UserRepository;
import com.youxin.app.service.UserService;
import com.youxin.app.service.impl.ConsumeRecordManagerImpl;
import com.youxin.app.utils.AuthServiceUtils;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KConstants;
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
public class UserConsumeController extends AbstractController{
	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository repository;
	@Autowired
	private SMSServiceImpl sendSms;
	@Autowired
	private ConsumeRecordManagerImpl consumeRecordServer;
	
	@ApiOperation(value = "用户充值")
	@ApiImplicitParams({ @ApiImplicitParam(name = "payType", value = "充值类型（1支付宝，2微信）", required = true, paramType = "query")
	, @ApiImplicitParam(name = "price", value = "充值金额", required = true, paramType = "query")
	, @ApiImplicitParam(name = "time", value = "充值时间", required = true, paramType = "query")
	, @ApiImplicitParam(name = "secret", value = "安全加密", required = true, paramType = "query")})
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
	
}
