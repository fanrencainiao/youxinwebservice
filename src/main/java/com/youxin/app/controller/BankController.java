package com.youxin.app.controller;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import com.youxin.app.entity.BankRecord;
import com.youxin.app.entity.Config;
import com.youxin.app.entity.User;
import com.youxin.app.entity.User.MyCard;
import com.youxin.app.service.ConfigService;
import com.youxin.app.service.UserService;
import com.youxin.app.service.impl.TransfersRecordManagerImpl;
import com.youxin.app.utils.AuthServiceUtils;
import com.youxin.app.utils.CollectionUtil;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.NumberUtil;
import com.youxin.app.utils.ReqUtil;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.alipay.util.AliPayUtil;
import com.youxin.app.utils.applicationBean.WxConfig;
import com.youxin.app.utils.sms.SMSServiceImpl;
import com.youxin.app.utils.wxpay.utils.WXPayUtil;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;


/**
 * 银行卡 提现的接口
 * 
 * @author 
 * @version 2.2
 */

@Api(tags = "提现")
@RestController
@RequestMapping("/bank")
public class BankController extends AbstractController{

	
	
//	private static final String TRANSFERS_PAY = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers"; // 企业付款
//
//	private static final String TRANSFERS_PAY_QUERY = "https://api.mch.weixin.qq.com/mmpaymkttransfers/gettransferinfo"; // 企业付款查询
//	
	@Resource
	private WxConfig wxConfig;
	

	
	@Autowired
	private TransfersRecordManagerImpl transfersManager;
	@Autowired
	private UserService userService;
	@Autowired
	private ConfigService configService;
	@Autowired
	@Qualifier("get")
	private Datastore dfds;
	@Autowired
	private SMSServiceImpl smsServer;
	
	/**
	 * 银行卡转账
	 * @param request
	 * @param response
	 */
	@ApiOperation(value = "银行卡申请提现", response = Result.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "moneyStr", value = "金额",required = true, paramType = "query"),
			@ApiImplicitParam(name = "bankCard", value = "银行卡号",required = true, paramType = "query"),
			@ApiImplicitParam(name = "name", value = "姓名",required = true, paramType = "query"),
			@ApiImplicitParam(name = "idCard", value = "身份证号",required = true, paramType = "query"),
			@ApiImplicitParam(name = "time", value = "时间", required = true, paramType = "query"),
			@ApiImplicitParam(name = "secret", value = "安全加密 md5(apiKey+userid + md5(token+amount+time)+ md5(bankCard+name)+payPassword(md5) )", required = true, paramType = "query") })
	@PostMapping(value = "/pay")
	public synchronized Object transferPay(@RequestParam(defaultValue="") String moneyStr,@RequestParam(defaultValue="") String bankCard,
			@RequestParam(defaultValue="") String name,@RequestParam(defaultValue="") String idCard,@RequestParam(defaultValue="0") long time,
			@RequestParam(defaultValue="") String secret) {
		
		Config config=null;
		if(StringUtil.isEmpty(moneyStr)) {
			return Result.error("请输入提现金额！");
		}else if(StringUtil.isEmpty(secret)) {
			return Result.error("缺少提现密钥");
		}
		
		
		int userId = ReqUtil.getUserId();
		User user =userService.getUserFromDB(userId);
		if(null==user) {
			return Result.error("用户异常");
		}
		//验证用户实名银行卡
		MyCard myCard = dfds.createQuery(MyCard.class).field("userId")
		.equal(userId).field("state").equal(1)
		.field("bankCard").equal(bankCard)
		.field("idCard").equal(idCard)
		.field("name").equal(name)
		.get();
		
		if(myCard==null) {
			return Result.error("请勿非法操作");
		}
		//当日零点
		long mtime=DateUtil.getTodayMorning().getTime()/1000;
		//当日24点
		long ntime=DateUtil.getTodayNight().getTime()/1000;
		//当日已经提取金额
		BigDecimal todayMoney=BigDecimal.ZERO;
		List<BankRecord> todayRecords = dfds.createQuery(BankRecord.class).field("payTime").greaterThan(mtime)
		.field("payTime").lessThan(ntime).asList();
		if(!CollectionUtil.isEmpty(todayRecords)) {
			for (BankRecord bankRecord : todayRecords) {
				todayMoney=todayMoney.add(new BigDecimal(bankRecord.getTotalFee()));
			}
			//加本次提现总提取金额
			BigDecimal todayMoneyTotal=todayMoney.add(new BigDecimal(moneyStr));
			if(todayMoneyTotal.compareTo(new BigDecimal("20000"))>0) {
				return Result.error("今日已经提现"+todayMoney+"元,已超过当日最高额度20000元");
			}
		}
		
		

		
//		String openid=user.getOpenid();
		//业务判断 openid是否有收款资格
//		if(StringUtil.isEmpty(openid)) {
//			return Result.error("请先 微信授权 没有授权不能提现 ");
//		}else if(!AuthServiceUtils.authRequestTime(time)) {
//			return Result.error("授权认证失败");
//		}
		if(!AuthServiceUtils.authRequestTime(time)) {
			return Result.error("授权认证失败");
		}
		DecimalFormat df = new DecimalFormat("#.00");
		/**
		 * 1
		 * 提现金额
		 */
		double total=Double.valueOf(moneyStr);
		if(2>total) {
			return Result.error("提现最低限制 2元 ");
		}
		
		/**
		 * 0.01
		 * 
		 * 0.6%+1
		 * 提现手续费
		 */
		double fee =Double.valueOf(df.format((total*0.006)))+1;
		if(0.01>fee) {
			fee=0.01;
		}else  {
			fee=NumberUtil.getCeil(fee, 2);
		}
		
		/**
		 * 0.49
		 * 实际到账金额
		 */
		Double totalFee= Double.valueOf(df.format(total-fee));
		
		if(totalFee>user.getBalance()) {
			return Result.error("账号余额不足 请先充值 ");
		}
		
		/**
		 * 49.0
		 */
//		Double realFee=(totalFee);
		
		/**
		 * 49
		 */
//		String realFeeStr=realFee.intValue()+"";
		
		log.debug(String.format("=== transferPay userid %s username %s 提现金额   %s 手续费   %s  到账金额   %s ", 
				userId,user.getName(),total,fee,totalFee));
		/**
		 * ow9Ctwy_qP8OoLr_6T-5oMnBud8w
		 */
		
		String token = getAccess_token();
		
		
		if(!AuthServiceUtils.authBankTransferPay(user.getPayPassword(),userId+"", token, moneyStr,bankCard,name,time, secret)) {
			return Result.error("输入密码错误");
		}
		
		Map<String, String> restmap = null;
		
		BankRecord record=new BankRecord();
		try {
			record.setUserId(userId);
			record.setNonceStr(WXPayUtil.getNonceStr());
			record.setOutTradeNo(StringUtil.getOutTradeNo());
			record.setTotalFee(moneyStr);
			record.setFee(fee+"");
			record.setRealFee(totalFee+"");
			record.setCreateTime(DateUtil.currentTimeSeconds());
			record.setStatus(0);
			
		} catch (Exception e) {
			log.debug(e.getMessage());
		}

//		if (CollectionUtil.isNotEmpty(restmap) && "SUCCESS".equals(restmap.get("result_code"))) {
//			logger.info("转账成功：" + restmap.get("result_code") + ":" + restmap.get("return_code"));
			Map<String, String> transferMap = new HashMap<>();
			transferMap.put("payment_bankCard", bankCard);
			transferMap.put("payment_time", DateUtil.currentTimeSeconds()+""); //微信支付成功时间
			
//			record.setPayNo(restmap.get("payment_no"));
//			record.setPayTime(DateUtil.currentTimeSeconds());
			record.setCreateTime(DateUtil.currentTimeSeconds());
//			record.setResultCode(restmap.get("result_code"));
//			record.setReturnCode(restmap.get("return_code"));
			record.setStatus(0);//手动设为创建
			record.setBankCard(bankCard);
			record.setName(name);
			record.setBankName(name);
			transfersManager.transfersToBank(record);
			config = configService.getConfig();
			if(config!=null) {
				if(config.getBankMsgState()==1) {
					smsServer.sendSmsToManager(config.getSendPhone(), "86","zh",bankCard,name,totalFee);
				}
			}
			return Result.success(transferMap)	;

	}

	/**
	 * 实名认证银行卡并保存
	 * 
	 * @param myCard
	 * @return
	 */
	@ApiOperation(value = "实名认证银行卡", response = Result.class)
	@PostMapping(value = "/checkBankCardAndAdd")
	public synchronized Result checkBankCardAndAdd(@RequestBody MyCard myCard) {
		// 当前用户
		Integer userId = ReqUtil.getUserId();

		// 实名认证用户ids
		Set<Integer> uids = new HashSet<>();

		MyCard myCard2 = null;
		if (userId <= 10000000) {
			return Result.error("无效用户");
		}
		// 先检查系统中银行卡信息
		List<MyCard> sysMyCards = dfds.createQuery(MyCard.class).field("name")
				.equal(myCard.getName()).field("idCard").equal(myCard.getIdCard()).field("bankCard")
				.equal(myCard.getBankCard()).asList();
		// 先检查系统中实名信息
		List<MyCard> sysMyCardsid =dfds.createQuery(MyCard.class).field("name")
				.equal(myCard.getName()).field("idCard").equal(myCard.getIdCard()).asList();
		// 当前用户实名
		List<MyCard> localUserCard = dfds.createQuery(MyCard.class).field("userId").equal(userId)
				.field("state").equal(1).asList();
		String realName = "";
		String realBankCard = "";
		if (!CollectionUtils.isEmpty(localUserCard)) {
			realName = localUserCard.get(0).getName();
			realBankCard = localUserCard.get(0).getBankCard();
		}
		if (!CollectionUtils.isEmpty(sysMyCardsid)) {
			// 获取该银行卡实名认证并有效（存在未全部删除解绑银行卡的用户）的用户
			for (MyCard sysMyCard : sysMyCardsid) {
				if (sysMyCard.getState() == 1) {
					uids.add(sysMyCard.getUserId());
				}
			}
		}

		// 系统中不存在银行卡
		if (CollectionUtils.isEmpty(sysMyCards)) {
			if (uids.contains(userId)) {
				if ((realName.equals(myCard.getName()))) {
//						saveMyCardByUserId(userId, myCard);
//						return Result.success(myCard);
					return alibankcheckadd(myCard, userId);
				} else
					return Result.error("请使用账户已经实名认证的用户的银行卡");

			} else {
				if (uids.size() < 5) {
//							saveMyCardByUserId(userId, myCard);
//							return Result.success(myCard);
					return alibankcheckadd(myCard, userId);
				} else
					return Result.error("用户实名认证超过限制(5个)");
			}

		} else {
			// 系统中存在银行卡
			myCard2 = sysMyCards.get(0);

			// 实名认证有效用户名<=5(在有效实名内)
			if (uids.contains(userId)) {
				if (realName.equals(myCard.getName()))
					return saveOrUpdateMyCard(myCard, userId, myCard2, sysMyCards);
				else
					return Result.error("请使用账户已经实名认证的用户的银行卡");
			} else {
				// 不在有效实名内
				// 系统小于五个有效实名账户可增加有效实名认证银行卡
				if (uids.size() < 5) {
					if (realName.equals(myCard.getName()) || realName.equals(""))
						return saveOrUpdateMyCard(myCard, userId, myCard2, sysMyCards);
					else
						return Result.error("请使用账户已经实名认证的用户的银行卡");

				} else
					return Result.error("用户实名认证超过限制(5个)");
			}

		}

	}

	/**
	 * 阿里云银行卡验证并新增
	 * 
	 * @param myCard
	 * @param userId
	 * @return
	 */
	private Result alibankcheckadd(MyCard myCard, Integer userId) {
		// 阿里接口返回json字符串
		String result = null;
		// 收费操作（需谨慎）
		result = AliPayUtil.checkBankCard(myCard);
		if (!StringUtil.isEmpty(result)) {
			JSONObject rj = JSONObject.parseObject(result);
			if ("0000".equals(rj.getString("respCode"))) {
//				if (rj.getJSONObject("result").getInteger("res") == 1) {
//					// 保存银行卡信息
//					myCard.setType(rj.getJSONObject("result").getString("type"));
//					myCard.setBankName(rj.getJSONObject("result").getString("bankName"));
//					saveMyCardByUserId(userId, myCard);
//					return Result.success(myCard);
//				} else {
//					return Result.error(rj.getJSONObject("result").getString("description"));
//				}
				myCard.setType(rj.getString("bankType"));
				myCard.setBankName(rj.getString("bankName"));
				myCard.setName(rj.getString("name"));
				myCard.setIdCard(rj.getString("idNo"));
				myCard.setBankCard(rj.getString("cardNo"));
				myCard.setPhone(rj.getString("phoneNo"));
				saveMyCardByUserId(userId, myCard);
				return Result.success(myCard);
			}else {
				return Result.error(rj.getString("respMessage"));
			}
		}
//		saveMyCardByUserId(userId, myCard);
		return Result.error("ali验证失败");
	}

	private Result saveOrUpdateMyCard(MyCard myCard, Integer userId, MyCard myCard2, List<MyCard> sysMyCards) {
		// 系统中该登录人账户是否存在此银行卡
//		List<MyCard> collect = sysMyCards.stream().filter(item->(item.getBankCard().equals(myCard.getBankCard())&&item.getUserId()==userId)).collect(Collectors.toList());
		List<MyCard> collect = dfds.createQuery(MyCard.class).field("userId").equal(userId)
				.field("bankCard").equal(myCard.getBankCard()).asList();
		System.out.println(collect.size());
		if (CollectionUtils.isEmpty(collect)) {
			saveMyCardByUserId(userId, myCard2);
			return Result.success();
		} else {
			// 更新
			if (collect.get(0).getState() == 1) {
				return Result.error("该银行卡已经实名认证过");
			} else {

				Query<MyCard> query = dfds.createQuery(MyCard.class).field("userId").equal(userId)
						.field("bankCard").equal(myCard.getBankCard());
				if (null == query.get())
					return Result.error("数据系统出错");
				UpdateOperations<MyCard> ops = dfds.createUpdateOperations(MyCard.class);
				ops.set("state", 1);
				ops.set("updateTime", DateUtil.currentTimeSeconds());
				dfds.update(query, ops);
				return Result.success();
			}
		}
	}

	private void saveMyCardByUserId(Integer userId, MyCard myCard2) {
		myCard2.setUserId(userId);
		myCard2.setState(1);
		myCard2.setCreateTime(DateUtil.currentTimeSeconds());
		myCard2.setUpdateTime(DateUtil.currentTimeSeconds());
		myCard2.setId(null);
		dfds.save(myCard2);
	}

	/**
	 * 逻辑删除/禁用银行卡
	 * 
	 * @return
	 */
	@ApiOperation(value = "逻辑删除/禁用银行卡", response = Result.class)
	@DeleteMapping(value = "/disableBankCard")
	public Result disableBankCard(@RequestParam String bankCard) {
		// 当前用户
		Integer userId = ReqUtil.getUserId();
		if (userId <= 10000000)
			return Result.error("无效用户");
		MyCard myCard = dfds.createQuery(MyCard.class).field("userId").equal(userId)
				.field("bankCard").equal(bankCard).get();
		if (myCard != null) {
			Query<MyCard> query = dfds.createQuery(MyCard.class).field("userId").equal(userId)
					.field("bankCard").equal(bankCard);
			if (null == query.get())
				return Result.error("数据系统出错");
			UpdateOperations<MyCard> ops = dfds.createUpdateOperations(MyCard.class);
			ops.set("state", 0);
			ops.set("updateTime", DateUtil.currentTimeSeconds());
			dfds.update(query, ops);
			return Result.success();
		}
		return Result.error("银行卡号错误");
	}

	/**
	 * 获取用户银行卡
	 * 
	 * @return
	 */
	@ApiOperation(value = "获取用户银行卡", response = Result.class)
	@PostMapping(value = "/getMyCard")
	public Result getMyCard() {
		// 当前用户
		Integer userId = ReqUtil.getUserId();
		if (userId <= 10000000)
			return Result.error("无效用户");
		List<MyCard> myCard = dfds.createQuery(MyCard.class).field("userId").equal(userId)
				.field("state").equal(1).asList();

		return Result.success(myCard);
	}
	
	

}
