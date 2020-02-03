package com.youxin.app.controller;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Map;

import javax.annotation.Resource;

import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.entity.RedPacket;
import com.youxin.app.entity.User;
import com.youxin.app.entity.WalletFour;
import com.youxin.app.service.UserService;
import com.youxin.app.service.impl.ConsumeRecordManagerImpl;
import com.youxin.app.service.impl.RedPacketManagerImpl;
import com.youxin.app.utils.AuthServiceUtils;
import com.youxin.app.utils.CollectionUtil;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.Md5Util;
import com.youxin.app.utils.NumberUtil;
import com.youxin.app.utils.ReqUtil;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.ThreadUtil;
import com.youxin.app.utils.alipay.util.AliPayUtil;
import com.youxin.app.utils.supper.Callback;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;


@Api(tags = "红包管理")
@RestController
@RequestMapping("/redPacket/")
public class RedPacketController extends AbstractController{
	@Resource(name = "get")
	Datastore dsdt;
	@Autowired
	UserService userService;
	@Autowired
	RedPacketManagerImpl redServer;
	@Autowired
	ConsumeRecordManagerImpl consumeServer;
	
//	@SuppressWarnings("static-access")
//	@RequestMapping("/redPacket/sendRedPacket")
//	public Result sendRedPacket(RedPacket packet,
//			@RequestParam(defaultValue="0") long time,
//			@RequestParam(defaultValue="") String secret) {
//		String token=getAccess_token();
//		Integer userId = ReqUtil.getUserId();
//		
//		User user=userService.getUserFromDB(userId);
//		if(user.getBalance()<packet.getMoney()){
//			//余额不足
//			return Result.error("余额不足,请先充值!");
//		}else if(packet.getMoney()<0.01||500<packet.getMoney()){
//			return Result.error("红包总金额在0.01~500之间哦!");
//		}else if((packet.getMoney()/packet.getCount())<0.01){
//			return Result.error("每人最少 0.01元 !");
//		}
//		//红包接口授权
//		if(StringUtil.isEmpty(user.getPayPassword())){
//			return Result.error("请设置支付密码");
//		}
//		if(!AuthServiceUtils.authRedPacket(user.getPayPassword(),userId+"", token, time,secret)) {
//			return Result.error("支付密码错误!");
//		}
//		packet.setUserId(userId);
//		packet.setUserName(user.getName());
//		packet.setOver(packet.getMoney());
//		long cuTime=DateUtil.currentTimeSeconds();
//		packet.setSendTime(cuTime);
//		packet.setOutTime(cuTime+KConstants.Expire.DAY1);
//		Object data=redServer.saveRedPacket(packet);
//		//修改金额
//		userService.rechargeUserMoeny(userId, packet.getMoney(), KConstants.MOENY_REDUCE);
//			
//		//开启一个线程 添加一条消费记录
//		new ThreadUtil().executeInThread(new Callback() {
//			@Override
//			public void execute(Object obj) {
//				String tradeNo = AliPayUtil.getOutTradeNo();
//				//创建充值记录
//				ConsumeRecord record = new ConsumeRecord();
//				record.setUserId(userId);
//				record.setTradeNo(tradeNo);
//				record.setMoney(packet.getMoney());
//				record.setStatus(KConstants.OrderStatus.END);
//				record.setType(KConstants.ConsumeType.SEND_REDPACKET);
//				record.setPayType(KConstants.PayType.BALANCEAY); //余额支付
//				record.setDesc("红包发送");
//				record.setTime(DateUtil.currentTimeSeconds());
//				consumeServer.saveConsumeRecord(record);
//			}
//		});
//
//		return Result.success(data);
//	}
	
	/**
	 * 新版本发送红包
	 * @param packet
	 * @param time
	 * @param secret
	 * @return
	 */
	
	@SuppressWarnings("static-access")
	@ApiOperation(value = "发送红包",response=Result.class)
	@ApiImplicitParams({
		@ApiImplicitParam(name = "moneyStr", value = "金额", required = true, paramType = "query"),
		@ApiImplicitParam(name = "time", value = "时间", required = true, paramType = "query"),
		@ApiImplicitParam(name = "secret", value = "密钥 md5(md5(apikey+time+money) +userid+token+payPassword", required = true, paramType = "query")})
	@PostMapping("sendRedPacket")
	public Result sendRedPacketV1(@RequestBody RedPacket packet,
			@RequestParam(defaultValue="0") long time,@RequestParam(defaultValue="") String moneyStr,
			@RequestParam(defaultValue="") String secret) {
		try {
			
		
		if(packet.getPayType()!=1)
			return Result.error("暂时只支持支付宝红包");
		String token = getAccess_token();
		Integer userId = ReqUtil.getUserId();
		User user=userService.getUserFromDB(userId);
		packet.setMoney(Double.valueOf(moneyStr));
		Double redPackgeMoney;
		if(user.getBalance() < packet.getMoney()&&packet.getPayType()!=1){
			//余额不足
			return Result.error("余额不足,请先充值!");
		}else if(packet.getMoney() < 0.01 || 20000 < packet.getMoney()){
			return Result.error("红包金额在0.01~20000之间哦!");
		}else if((packet.getMoney()/packet.getCount()) < 0.01){
			return Result.error("每人最少 0.01元 !");
		}else if(packet.getType()==4&&CollectionUtil.isEmpty(packet.getToUserIds())){
			return Result.error("定向红包请选择指定领取人!");
		}
		//支付宝红包
		if(packet.getPayType()==1) {
			BigDecimal money = NumberUtil.getBigDecimalForDouble(packet.getMoney());
			BigDecimal count = NumberUtil.getBigDecimalForDouble(packet.getCount());
			//单个红包金额
			double divideMoney = money.divide(count,2,BigDecimal.ROUND_HALF_UP).doubleValue();
			log.debug("money:"+money+"count:"+count+"=divideMoney:"+divideMoney);
			if(divideMoney>200)
				return Result.error("单个红包不能超过200元");
			Double sendTotalMoney = redServer.sendBill(userId);
			if(NumberUtil.add(sendTotalMoney, packet.getMoney(), null)>20000) 
				return Result.error("每日发送总金额不能超过20000,您24小时内已经发送了"+sendTotalMoney+"元红包");
		}
		
		//红包接口授权
		WalletFour wallet_Four = redServer.getUserWallet(userId, packet.getRoomJid());
		if(packet.getPayType()!=1&&StringUtil.isEmpty(user.getPayPassword())){
			return Result.error("请设置支付密码");
		}
		if(!AuthServiceUtils.authRedPacketV2(Md5Util.md5Hex("1"),userId+"", token, time,moneyStr,secret)) {
			return Result.error("权限验证错误!");
		}
		packet.setUserId(userId);
		packet.setAccid(user.getAccid());
		packet.setUserName(user.getName());
		if(null != wallet_Four && wallet_Four.getIsSetUpMoney()==1) {
			redPackgeMoney = wallet_Four.getRedPackgeMoney();
			packet.setCount(wallet_Four.getRedPackegeNumber());
			packet.setMoney(wallet_Four.getRedPackgeMoney());
		}else {
			redPackgeMoney = packet.getMoney();
		}
		packet.setOver(redPackgeMoney);
		long cuTime = DateUtil.currentTimeSeconds();
		packet.setSendTime(cuTime);
		packet.setOutTime(cuTime + KConstants.Expire.DAY1);
//		packet.setOutTime(cuTime + 60);
		Object data = null;
		//修改金额,非支付宝红包进行零钱修改
		if(packet.getPayType()!=1) {
			data = redServer.saveRedPacket(packet);
			userService.rechargeUserMoeny(userId, redPackgeMoney, KConstants.MOENY_REDUCE);

			//开启一个线程 添加一条消费记录
			new ThreadUtil().executeInThread(new Callback() {
				@Override
				public void execute(Object obj) {
					String tradeNo = AliPayUtil.getOutTradeNo();
					//创建充值记录
					ConsumeRecord record = new ConsumeRecord();
					record.setUserId(userId);
					record.setTradeNo(tradeNo);
					//record.setMoney(packet.getMoney());
					record.setMoney(redPackgeMoney);
					record.setStatus(KConstants.OrderStatus.END);
					record.setType(KConstants.ConsumeType.SEND_REDPACKET);
					record.setPayType(KConstants.PayType.BALANCEAY); //余额支付
					record.setDesc("红包发送");
					record.setTime(DateUtil.currentTimeSeconds());
					consumeServer.saveConsumeRecord(record);
				}
			});
		}else {
			//查询红包订单支付情况
			String orderid = AliPayUtil.commonQueryRequest(packet.getPayNo(),packet.getAliPayNo(), "PERSONAL_PAY");
			System.out.println("发送红包状态后实时查询订单号："+orderid);
			data = redServer.saveRedPacket(packet);
//			if(orderid==null||orderid=="") 
//				return Result.error("支付宝红包订单支付失败");
		}
		return Result.success(data);
		} catch (Exception e) {
			log.debug("发红包异常："+e.getMessage());
			AliPayUtil.backTransUni("支付宝红包异常退回", packet.getAliPayNo(), packet.getMoney()+"", packet.getPayNo());
			return Result.error("系统异常,金额已原路退回");
		}
	}
	
	
	//获取红包详情
	@ApiOperation(value = "获取红包详情",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "红包id", required = true, paramType = "query")})
	@GetMapping("getRedPacket")
	public Result getRedPacket(String id) {
		Result result=redServer.getRedPacketById(ReqUtil.getUserId(), ReqUtil.parseId(id));
		//System.out.println("获取红包  ====>  "+result);
		return result;
	}
	//回复红包
	@ApiOperation(value = "回复红包",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "红包id", required = true, paramType = "query"),
		@ApiImplicitParam(name = "reply", value = "回复", required = true, paramType = "query")})
	@PostMapping("reply")
	public Result replyRedPacket(String id,String reply) {
		try {
			if(StringUtil.isEmpty(reply))
				return Result.error("回复不能为 null!");
			redServer.replyRedPacket(id, reply);
			return Result.success();
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}
	}
	//打开红包
	@ApiOperation(value = "打开红包",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "红包id", required = true, paramType = "query"),
		@ApiImplicitParam(name = "time", value = "时间", required = true, paramType = "query"),
		@ApiImplicitParam(name = "secret", value = "密钥 md5(md5(apikey+time) +userid+token", required = true, paramType = "query")})
	@PostMapping("openRedPacket")
//	md5( md5(apikey+time) +userid+token) 
	public Result openRedPacket(String id,
			@RequestParam(defaultValue="0") long time,
			@RequestParam(defaultValue="") String secret) {
		String token=getAccess_token();
		Integer userId = ReqUtil.getUserId();
		//红包接口授权
		if(!AuthServiceUtils.authRedPacket(userId+"", token, time, secret)) {
			return Result.error("权限验证失败!");
		}
		Result result=redServer.openRedPacketById(ReqUtil.getUserId(), ReqUtil.parseId(id));
		//System.out.println("打开红包  ====>  "+result);
		return result;
	}
	//查询发出的红包
	@ApiOperation(value = "查询发出的红包",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "pageIndex", defaultValue="0",value = "页码", paramType = "query"),
		@ApiImplicitParam(name = "pageSize", value = "长度",   defaultValue="10",paramType = "query"),
		@ApiImplicitParam(name = "q", value = "参数查询（startTime，endTime）",   defaultValue="",paramType = "query")})
	@GetMapping("getSendRedPacketList")
	public Result getSendRedPacketList(@RequestParam(defaultValue="null") Map<String, String> q,@RequestParam(defaultValue="0")int pageIndex,@RequestParam(defaultValue="10")int pageSize) {
		Object data=redServer.getSendRedPacketList(ReqUtil.getUserId(),pageIndex,pageSize,q);
		return Result.success(data);
	}
	//查询收到的红包
	@ApiOperation(value = "查询收到的红包",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "pageIndex", defaultValue="0",value = "页码", paramType = "query"),
		@ApiImplicitParam(name = "pageSize", value = "长度",   defaultValue="10",paramType = "query"),
		@ApiImplicitParam(name = "q", value = "参数查询（startTime，endTime）",   defaultValue="",paramType = "query")})
	@GetMapping("getRedReceiveList")
	public Result getRedReceiveList(@RequestParam(defaultValue="null") Map<String, String> q,@RequestParam(defaultValue="0")int pageIndex,@RequestParam(defaultValue="10")int pageSize) {
		Object data=redServer.getRedReceiveList(ReqUtil.getUserId(),pageIndex,pageSize,q);
		return	Result.success(data);
	}
	

	
}
