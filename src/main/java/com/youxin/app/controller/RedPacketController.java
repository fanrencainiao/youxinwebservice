//package com.youxin.app.controller;
//
//
//import javax.annotation.Resource;
//
//import org.bson.types.ObjectId;
//import org.mongodb.morphia.Datastore;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.youxin.app.entity.RedPacket;
//import com.youxin.app.service.UserService;
//import com.youxin.app.utils.ReqUtil;
//import com.youxin.app.utils.Result;
//
//
//
//@RestController
//public class RedPacketController extends AbstractController{
//	@Resource(name = "get")
//	Datastore dsdt;
//	@Autowired
//	UserService userService;
//
//	@SuppressWarnings("static-access")
//	@RequestMapping("/redPacket/sendRedPacket")
//	public Result sendRedPacket(RedPacket packet,
//			@RequestParam(defaultValue="0") long time,
//			@RequestParam(defaultValue="") String secret) {
//		String token=getAccess_token();
//		Integer userId = ReqUtil.getUserId();
//		
//		if(userService.getUserMoeny(userId)<packet.getMoney()){
//			//余额不足
//			return Result.failure("余额不足,请先充值!");
//		}else if(packet.getMoney()<0.01||500<packet.getMoney()){
//			return Result.failure("红包总金额在0.01~500之间哦!");
//		}else if((packet.getMoney()/packet.getCount())<0.01){
//			return Result.failure("每人最少 0.01元 !");
//		}
//		//红包接口授权
//		User user=SKBeanUtils.getUserManager().getUserFromDB(userId);
//		if(StringUtil.isEmpty(user.getPayPassword())){
//			return Result.failure("请设置支付密码");
//		}
//		if(!AuthServiceUtils.authRedPacket(user.getPayPassword(),userId+"", token, time,secret)) {
//			return Result.failure("支付密码错误!");
//		}
//		packet.setUserId(userId);
//		packet.setUserName(SKBeanUtils.getUserManager().getUser(userId).getNickname());
//		packet.setOver(packet.getMoney());
//		long cuTime=DateUtil.currentTimeSeconds();
//		packet.setSendTime(cuTime);
//		packet.setOutTime(cuTime+KConstants.Expire.DAY1);
//		Object data=SKBeanUtils.getRedPacketManager().saveRedPacket(packet);
//		//修改金额
//		SKBeanUtils.getUserManager().rechargeUserMoeny(userId, packet.getMoney(), KConstants.MOENY_REDUCE);
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
//				SKBeanUtils.getConsumeRecordManager().save(record);
//			}
//		});
//
//		return Result.success(null,data);
//	}
//	
//	/**
//	 * 新版本发送红包
//	 * @param packet
//	 * @param time
//	 * @param secret
//	 * @return
//	 */
//	@SuppressWarnings("static-access")
//	@RequestMapping("/redPacket/sendRedPacket/v1")
//	public Result sendRedPacketV1(RedPacket packet,
//			@RequestParam(defaultValue="0") long time,@RequestParam(defaultValue="") String moneyStr,
//			@RequestParam(defaultValue="") String secret) {
//		String token = getAccess_token();
//		Integer userId = ReqUtil.getUserId();
//		packet.setMoney(Double.valueOf(moneyStr));
//		Double redPackgeMoney;
//		if(SKBeanUtils.getUserManager().getUserMoeny(userId) < packet.getMoney()){
//			//余额不足
//			return Result.failure("余额不足,请先充值!");
//		}else if(packet.getMoney() < 0.01 || 500 < packet.getMoney()){
//			return Result.failure("红包总金额在0.01~500之间哦!");
//		}else if((packet.getMoney()/packet.getCount()) < 0.01){
//			return Result.failure("每人最少 0.01元 !");
//		}
//		//红包接口授权
//		User user = SKBeanUtils.getUserManager().getUserFromDB(userId);
//		WalletFour wallet_Four = SKBeanUtils.getUserManager().getUserWallet(userId, packet.getRoomJid());
//		if(StringUtil.isEmpty(user.getPayPassword())){
//			return Result.failure("请设置支付密码");
//		}
//		if(!AuthServiceUtils.authRedPacketV1(user.getPayPassword(),userId+"", token, time,moneyStr,secret)) {
//			return Result.failure("支付密码错误!");
//		}
//		packet.setUserId(userId);
//		packet.setUserName(SKBeanUtils.getUserManager().getUser(userId).getNickname());
//		if(null != wallet_Four && wallet_Four.getIsSetUpMoney()==1) {
//			redPackgeMoney = wallet_Four.getRedPackgeMoney();
//			packet.setCount(wallet_Four.getRedPackegeNumber());
//			packet.setMoney(wallet_Four.getRedPackgeMoney());
//		}else {
//			redPackgeMoney = packet.getMoney();
//		}
//		packet.setOver(redPackgeMoney);
//		long cuTime = DateUtil.currentTimeSeconds();
//		packet.setSendTime(cuTime);
//		packet.setOutTime(cuTime + KConstants.Expire.DAY1);
//		Object data = SKBeanUtils.getRedPacketManager().saveRedPacket(packet);
//		//修改金额
//		SKBeanUtils.getUserManager().rechargeUserMoeny(userId, redPackgeMoney, KConstants.MOENY_REDUCE);
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
//				//record.setMoney(packet.getMoney());
//				record.setMoney(redPackgeMoney);
//				record.setStatus(KConstants.OrderStatus.END);
//				record.setType(KConstants.ConsumeType.SEND_REDPACKET);
//				record.setPayType(KConstants.PayType.BALANCEAY); //余额支付
//				record.setDesc("红包发送");
//				record.setTime(DateUtil.currentTimeSeconds());
//				SKBeanUtils.getConsumeRecordManager().save(record);
//			}
//		});
//
//		return Result.success(null,data);
//	}
//	
//	
//	//获取红包详情
//	@RequestMapping("/redPacket/getRedPacket")
//	public Result getRedPacket(String id) {
//		Result result=SKBeanUtils.getRedPacketManager().getRedPacketById(ReqUtil.getUserId(), ReqUtil.parseId(id));
//		//System.out.println("获取红包  ====>  "+result);
//		return result;
//	}
//	//回复红包
//	@RequestMapping("/redPacket/reply")
//	public Result replyRedPacket(String id,String reply) {
//		try {
//			if(StringUtil.isEmpty(reply))
//				return Result.failure("回复不能为 null!");
//			SKBeanUtils.getRedPacketManager().replyRedPacket(id, reply);
//			return Result.success();
//		} catch (Exception e) {
//			return Result.failure(e.getMessage());
//		}
//	}
//	//打开红包
//	@RequestMapping("/redPacket/openRedPacket")
//	public Result openRedPacket(String id,
//			@RequestParam(defaultValue="0") long time,
//			@RequestParam(defaultValue="") String secret) {
//		String token=getAccess_token();
//		Integer userId = ReqUtil.getUserId();
//		//红包接口授权
//		if(!AuthServiceUtils.authRedPacket(userId+"", token, time, secret)) {
//			return Result.failure("权限验证失败!");
//		}
//		Result result=SKBeanUtils.getRedPacketManager().openRedPacketById(ReqUtil.getUserId(), ReqUtil.parseId(id));
//		//System.out.println("打开红包  ====>  "+result);
//		return result;
//	}
//	//查询发出的红包
//	@RequestMapping("/redPacket/getSendRedPacketList")
//	public Result getSendRedPacketList(@RequestParam(defaultValue="0")int pageIndex,@RequestParam(defaultValue="10")int pageSize) {
//		Object data=SKBeanUtils.getRedPacketManager().getSendRedPacketList(ReqUtil.getUserId(),pageIndex,pageSize);
//		return Result.success(null, data);
//	}
//	//查询收到的红包
//	@RequestMapping("/redPacket/getRedReceiveList")
//	public Result getRedReceiveList(@RequestParam(defaultValue="0")int pageIndex,@RequestParam(defaultValue="10")int pageSize) {
//		Object data=SKBeanUtils.getRedPacketManager().getRedReceiveList(ReqUtil.getUserId(),pageIndex,pageSize);
//		return	Result.success(null, data);
//	}
//	
//	
//	
//	
//}
