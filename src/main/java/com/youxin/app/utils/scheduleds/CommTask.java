package com.youxin.app.utils.scheduleds;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.TaskManagementConfigUtils;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.entity.RedPacket;
import com.youxin.app.entity.SysApiLog;
import com.youxin.app.entity.Transfer;
import com.youxin.app.entity.User;
import com.youxin.app.entity.UserStatusCount;
import com.youxin.app.entity.msgbody.MsgBody;
import com.youxin.app.entity.msgbody.MsgBody.ID;
import com.youxin.app.service.UserService;
import com.youxin.app.service.impl.ConsumeRecordManagerImpl;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.HttpUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.Md5Util;
import com.youxin.app.utils.MongoOperator;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.ThreadUtil;
import com.youxin.app.utils.alipay.util.AliPayUtil;
import com.youxin.app.utils.supper.Callback;
import com.youxin.app.yx.SDKService;
import com.youxin.app.yx.request.Msg;
import com.youxin.app.yx.request.MsgRequest;

import lombok.extern.slf4j.Slf4j;
@Component
@EnableScheduling
public class CommTask implements ApplicationListener<ApplicationContextEvent>{

	protected Log log=LogFactory.getLog("sche");
	@Autowired
	@Qualifier("get")
	private Datastore ds;
	
	public static final int STATUS_START=1;//红包发出状态
	public static final int STATUS_END=2;//已领完红包状态
	public static final int STATUS_RECEDE=-1;//已退款红包状态
	
	public static final int TRANSFER_START=1;// 转账发出状态
	public static final int TRANSFER_RECEDE=-1;// 转账退款状态
	//public static final int STATUS_RECEDE=3;//已退款红包状态
	@Autowired
	private UserService userManager;
	
	@Autowired
	private ConsumeRecordManagerImpl recordManager;
	
	@Resource(name = TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)
	private ScheduledAnnotationBeanPostProcessor scheduledProcessor;
	 public CommTask() {
			super();
	 }
	 @Value("${youxin.openTask}")
	 private int openTask;
	 @Override
	public void onApplicationEvent(ApplicationContextEvent event) {
		 if(event.getApplicationContext().getParent() != null)
			 return;
		 //root application context 没有parent，他就是老大.
		 //需要执行的逻辑代码，当spring容器初始化完成后就会执行该方法。
		
				
				if(0==openTask){
						
						ThreadUtil.executeInThread(new Callback() {
							@Override
							public void execute(Object obj) {
								 try {
										Thread.currentThread().sleep(10000);
										scheduledProcessor.destroy();
										System.out.println("====定时任务被关闭了=======》");
								 	} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
							}
						});
						
				   }else System.out.println("====定时任务开启中=======》");
	}
	
	@Scheduled(cron = "0 0 0/1 * * ?")
	public void execute() {
		long start = System.currentTimeMillis();
		// 刷新红包
		autoRefreshRedPackect();
		System.out.println("刷新红包成功,耗时" + (System.currentTimeMillis() - start) + "毫秒");
		// 刷新转账
		autoRefreshTransfer();
		System.out.println("刷新转账成功,耗时" + (System.currentTimeMillis() - start) + "毫秒");
		
	}
	
	@Scheduled(cron = "0 0/5 * * * ?")
 	public void refreshUserStatusCount(){
		DBObject q = new BasicDBObject("online",1);
		
		long count =ds.getCollection(User.class).getCount(q);
		
		UserStatusCount userCount=new UserStatusCount();
		//long count=(long)(Math.random()*(1000-100+1)+100);
		userCount.setType(1);
		userCount.setCount(count);
		userCount.setTime(DateUtil.currentTimeSeconds());
		ds.save(userCount);
		log.debug("刷新用户状态统计======》" +count);
	}
	
	@Scheduled(cron = "0 0 0/1 * * ?")
 	public void refreshUserStatusHour(){
		
		long currentTime =new Date().getTime()/1000;
		//DBObject q =null;
		Query<UserStatusCount> q=null;
		long startTime=currentTime-KConstants.Expire.HOUR;
			
		long endTime=currentTime;
		
		List<UserStatusCount> counts=null;
		UserStatusCount uCount=null;
		long sum=0;
		
		
			//q = new BasicDBObject();
			q=ds.createQuery(UserStatusCount.class);
			q.enableValidation();
			
			sum=0;
			System.out.println("当前时间:"+DateUtil.TimeToStr(new Date()));
			q.field("time").greaterThanOrEq(startTime);
			q.field("time").lessThan(endTime);
			q.field("type").equal(1);
			counts=q.asList();
			 uCount=new UserStatusCount();
			for (UserStatusCount userStatus : counts) {
				sum+=userStatus.getCount();
			}
			System.out.println("List Size======="+counts.size());
			if(sum>0){
				uCount.setTime(startTime);
				uCount.setType(2);
				uCount.setCount(sum/counts.size());
				ds.save(uCount);
				log.debug("平均用户在线======》" +uCount.getCount());
			}
	}
	
	@Scheduled(cron = "0 0 10 * * ?")
 	public void refreshUserStatusDay(){
		Date yesterday=DateUtil.getYesterdayMorning();
		//long currentTime =new Date().getTime()/1000;
		//DBObject q =null;
		Query<UserStatusCount> q=null;
		long startTime=yesterday.getTime()/1000;
		long endTime=startTime+KConstants.Expire.DAY1;
		List<UserStatusCount> counts=null;
		UserStatusCount uCount=null;
		long sum=0;
		
		
			//q = new BasicDBObject();
			q=ds.createQuery(UserStatusCount.class);
			q.enableValidation();
			sum=0;
			System.out.println("Day_Count 当前时间:"+DateUtil.TimeToStr(new Date()));
			q.field("time").greaterThanOrEq(startTime);
			q.field("time").lessThan(endTime);
			q.field("type").equal(2);
			counts=q.asList();
			 uCount=new UserStatusCount();
			for (UserStatusCount userStatus : counts) {
				sum+=userStatus.getCount();
			}
			System.out.println("Day_Count List Size======="+counts.size());
			if(sum>0){
				uCount.setTime(startTime);
				uCount.setType(3);
				uCount.setCount(sum/counts.size());
				ds.save(uCount);
				log.debug("Day_Count 平均用户在线======》" +uCount.getCount());
			}
	}
	/**
	 * 四点都设为不在线，重新统计
	 */
	@Scheduled(cron = "0 0 4 * * ?")
	public void refreshUserStatus(){
		BasicDBObject q = new BasicDBObject("_id",new BasicDBObject(MongoOperator.GT,1000));
		q.append("online", 1);
		DBObject values = new BasicDBObject();
		values.put(MongoOperator.SET,new BasicDBObject("online",0));
		ds.getCollection(User.class).update(q, values, false, true);
	}
	//12点
//	@Scheduled(cron = "30 59 23 * * ?")
	public void sendPhoneCode() {
		System.out.println("定时任务开始:"+new Date());
		System.out.println("定时任务开始:"+DateUtil.currentTimeSeconds());
		for (int i = 0; i < 200; i++) {
			System.out.println("定时任务第"+i+"次发送短信");
			Map<String, Object> params =new HashMap<>();
			params.put("telNumber", "17788902253");
			params.put("startStation", "北京首都");
			params.put("terminalStation", "贵阳");
			params.put("idCard", "43252419891003843X");
			String urlPost = HttpUtil.URLPost("http://www.gzairports.com:11111/sendSms.action", params);
			if(!StringUtil.isEmpty(urlPost)) {
				JSONObject parseObject = JSON.parseObject(urlPost);
				System.out.println(parseObject.toJSONString());
				String msg=parseObject.getJSONObject("result").getString("msg");
				System.out.println(msg);
				
				if(!StringUtil.isEmpty(msg)) {
					if(!msg.contains("发送验证码失败")) {
						System.out.println("发送成功:"+msg);
						break;
					}
					System.out.println(parseObject.getJSONObject("result").getString("code"));
				}
			}else {
				System.out.println("短信发送接口请求失败");
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("定时失败");
			}
		}
	}


	
	
	
	
	/** 
	* @Description:（每天凌晨定时清除十五天前的系统日志） 
	**/ 
	public void deleteSysLogs(){
		long beginTime = DateUtil.getOnedayNextDay(DateUtil.currentTimeSeconds(), 15, 1);
		Query<SysApiLog> query = ds.createQuery(SysApiLog.class);
		query.field("time").lessThanOrEq(beginTime);
		log.info("累积清除   "+DateUtil.strToDateTime(beginTime)+"  前的  "+ query.count() +"  条系统日志记录");
		ds.delete(query);
	}
	
	
	//红包超时未领取 退回余额
	private void autoRefreshRedPackect(){
		//q.put("status", new BasicDBObject(MongoOperator.NE,STATUS_RECEDE).append(MongoOperator.NE,STATUS_END));
		long currentTime=DateUtil.currentTimeSeconds();
		long sendTime=DateUtil.currentTimeSeconds();
		DBObject obj=null;
		Integer userId=0;
		Integer toUserId=0;
		String payNo=null;
		String aliPayNo=null;
		Integer payType=0;
		String roomJid="";
		ObjectId redPackectId=null;
		Double money=0.0;
		DBObject values = new BasicDBObject();
		List<DBObject> objs=new ArrayList<DBObject>();
		DBObject q = new BasicDBObject("outTime",new BasicDBObject(MongoOperator.LT,currentTime));
		q.put("over",new BasicDBObject(MongoOperator.GT,0));
		q.put("status",STATUS_START);//只查询发出状态的红包
		DBCursor cursor =ds.getCollection(RedPacket.class).find(q);
		
			while (cursor.hasNext()) {
				 obj = (BasicDBObject) cursor.next();
				objs.add(obj);
			}
		if(0<objs.size()){
			values.put(MongoOperator.SET,new BasicDBObject("status", STATUS_RECEDE));
			ds.getCollection(RedPacket.class).update(q, values,false,true);
		}
		for (DBObject dbObject : objs) {
			 userId= (Integer) dbObject.get("userId");
			 money =(Double) dbObject.get("over");
			 roomJid=(String) dbObject.get("roomJid");
			 redPackectId=(ObjectId) dbObject.get("_id");
			 toUserId=(Integer) dbObject.get("toUserId");
			 payType = (Integer)dbObject.get("payType");
			 payNo=(String)dbObject.get("payNo");
			 aliPayNo=(String)dbObject.get("aliPayNo");
			 sendTime=(Long)dbObject.get("sendTime");
			 if(payType!=null&&payType==1) 
				 recedeMoney(userId,toUserId,roomJid,money,redPackectId,1,payNo,aliPayNo,DateUtil.strToDateTime(sendTime));
			 else
				 recedeMoney(userId,toUserId,roomJid,money,redPackectId,0,"","",DateUtil.strToDateTime(sendTime));
		}
			
		System.out.println("红包超时未领取的数量 ======> "+objs.size());
		
	}
	
	private void recedeMoney(Integer userId,Integer toUserId,String roomJid,Double money,ObjectId id,int payType,String payNo,String aliPayNo,String sendTime){
		
		if(0<money){
			DecimalFormat df = new DecimalFormat("#.00");
			 money= Double.valueOf(df.format(money));
		}else 
			return;
		//实例化一天交易记录
		ConsumeRecord record=new ConsumeRecord();
		String tradeNo=AliPayUtil.getOutTradeNo();
		record.setTradeNo(tradeNo);
		record.setMoney(money);
		record.setUserId(userId);
		record.setTime(DateUtil.currentTimeSeconds());
		
		if(payType==1) {
			record.setType(KConstants.ConsumeType.ALI_BACK_COUPON);
			record.setPayType(KConstants.PayType.ALIPAY);
			record.setStatus(KConstants.OrderStatus.CREATE);
			record.setDesc("支付宝红包退款");
			String backTransUni = AliPayUtil.backTransUni("支付宝红包超时退款",aliPayNo, money+"", payNo);
			log.debug("支付宝红包超时退款信息"+backTransUni);
//			JSONObject btu = JSON.parseObject(backTransUni);
//			log.debug(btu);
//			if(!"SUCCESS".equalsIgnoreCase(btu.getString("status"))) {
//				return;
//			}
		}else {
			record.setType(KConstants.ConsumeType.REFUND_REDPACKET);
			record.setPayType(KConstants.PayType.BALANCEAY);
			record.setStatus(KConstants.OrderStatus.END);
			record.setDesc("红包退款");
			recordManager.saveConsumeRecord(record);
			userManager.rechargeUserMoeny(userId, money, KConstants.MOENY_ADD);
		}
		
		User toUser=userManager.getUser(toUserId);
	
		MsgRequest messageBean = new MsgRequest();
		messageBean.setType(100);// 自定义
//		messageBean.setType(0);// 文字
		messageBean.setOpe(0);// 个人消息
		if (toUser!=null) {
			messageBean.setFrom(Md5Util.md5HexToAccid(toUserId+""));
		}else {
			messageBean.setFrom(Md5Util.md5HexToAccid("1100"));
		}
		messageBean.setTo(Md5Util.md5HexToAccid(userId+""));
		ID ids=new ID();
		ids.setId(id.toString());
		messageBean.setBody(JSON.toJSONString(new MsgBody(0, KConstants.MsgType.BACKREDPACKET, ids)));
//		String notice="退款通知。退款方式：发送红包时使用的支付宝账户。红包发送时间："+sendTime+"。退款金额："+money+"元。退款原因：红包超过24小时未被领取。备注：你可以打开支付宝APP，在账单中搜索\"红包\"，查看红包相关记录。";
//		messageBean.setBody("{\"msg\":\""+notice+"\"}");
		try {
			JSONObject json=SDKService.sendMsg(messageBean);
			if(json.getInteger("code")!=200) 
				log.debug("红包领取 sdk消息发送失败");
		} catch (Exception e) {
			e.printStackTrace();
			log.debug("红包领取 sdk消息发送失败"+e.getMessage());
		}
		
		
		log.debug(userId+"  发出的红包,剩余金额   "+money+"  未领取  退回余额!");
	}

	// 转账超时未领取 退回余额
	public void autoRefreshTransfer(){
		long currentTime=DateUtil.currentTimeSeconds();
		DBObject obj=null;
		List<DBObject> objs=new ArrayList<DBObject>();
		DBObject values = new BasicDBObject();
		Integer userId=0;
		Double money=0.0;
		Integer toUserId=0;
		ObjectId transferId=null;
		DBObject q = new BasicDBObject("outTime",new BasicDBObject(MongoOperator.LT,currentTime));
		q.put("status",TRANSFER_START);//只查询发出状态的转账
		
		DBCursor cursor =ds.getCollection(Transfer.class).find(q);
		while (cursor.hasNext()) {
			 obj = (BasicDBObject) cursor.next();
			objs.add(obj);
		}
		
		if(0<objs.size()){
			values.put(MongoOperator.SET,new BasicDBObject("status", TRANSFER_RECEDE));
			ds.getCollection(Transfer.class).update(q, values,false,true);
		}
		
		for (DBObject dbObject : objs) {
			 userId= (Integer) dbObject.get("userId");
			 money =(Double) dbObject.get("money");
			 toUserId=(Integer) dbObject.get("toUserId");
			 transferId=(ObjectId) dbObject.get("_id");
			 transferRecedeMoney(userId, money, transferId);
		}
		System.out.println("转账超时未领取的数量 ======> "+objs.size());
	}
	
	// 转账退回
	public void transferRecedeMoney(Integer userId,double money,ObjectId transferId){
		if(0<money){
			// 格式化数据
			DecimalFormat df = new DecimalFormat("#.00");
			 money= Double.valueOf(df.format(money));
		}else 
			return;
		
		ConsumeRecord record=new ConsumeRecord();
		String tradeNo=AliPayUtil.getOutTradeNo();
		record.setTradeNo(tradeNo);
		record.setMoney(money);
		record.setUserId(userId);
		record.setType(KConstants.ConsumeType.REFUND_TRANSFER);
		record.setPayType(KConstants.PayType.BALANCEAY);
		record.setTime(DateUtil.currentTimeSeconds());
		record.setStatus(KConstants.OrderStatus.END);
		record.setDesc("转账退款");
		
		recordManager.saveConsumeRecord(record);
		
		userManager.rechargeUserMoeny(userId, money, KConstants.MOENY_ADD);
		
//		User sysUser=userManager.getUser(1100);
		Transfer transfer=ds.createQuery(Transfer.class).field("_id").equal(transferId).get();
		transfer.setId(null);

		MsgRequest messageBean = new MsgRequest();
		messageBean.setFrom(Md5Util.md5HexToAccid("1100"));
		messageBean.setType(100);// 文本
	
		messageBean.setOpe(0);// 个人消息
		messageBean.setTo(Md5Util.md5HexToAccid(userId+""));
		transfer.setRid(transferId.toString());
//		messageBean.setBody("{\"msg\":"+JSON.toJSONString(transfer)+"}");
//		messageBean.setBody("{\"type\":"+KConstants.MsgType.TRANSFERBACK+",\"data\":"+JSON.toJSONString(transfer)+"}");
		messageBean.setBody(JSON.toJSONString(new MsgBody(0, KConstants.MsgType.TRANSFERBACK, transfer)));
		try {
			JSONObject json=SDKService.sendMsg(messageBean);
			if(json.getInteger("code")!=200) 
				log.debug("转账退款 sdk消息发送失败");
		} catch (Exception e) {
			e.printStackTrace();
			log.debug("转账退款 sdk消息发送失败"+e.getMessage());
		}
		
		log.debug(userId+"  发出转账,剩余金额   "+money+"  未收钱  退回余额!");
	}
	
	
	/**
	 * 航班提交
	 */
	private void sendStation() {
		
		Map<String, Object> urlPostBuffer = HttpUtil.URLPostBuffer("http://www.gzairports.com:11111/order/creatImgCode.action", null);
		System.out.println(urlPostBuffer);
	}
}
