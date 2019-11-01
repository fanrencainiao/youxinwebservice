package com.youxin.app.utils.scheduleds;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

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
import com.youxin.app.service.UserService;
import com.youxin.app.service.impl.ConsumeRecordManagerImpl;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.MongoOperator;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.ThreadUtil;
import com.youxin.app.utils.alipay.util.AliPayUtil;
import com.youxin.app.utils.supper.Callback;
import com.youxin.app.yx.SDKService;
import com.youxin.app.yx.request.Msg;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Component
@EnableScheduling
public class CommTask implements ApplicationListener<ApplicationContextEvent>{

	
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
		DBObject obj=null;
		Integer userId=0;
		Integer toUserId=0;
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
			 recedeMoney(userId,toUserId,roomJid,money,redPackectId);
		}
			
		System.out.println("红包超时未领取的数量 ======> "+objs.size());
		
	}
	
	private void recedeMoney(Integer userId,Integer toUserId,String roomJid,Double money,ObjectId id){
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
		record.setType(KConstants.ConsumeType.REFUND_REDPACKET);
		record.setPayType(KConstants.PayType.BALANCEAY);
		record.setTime(DateUtil.currentTimeSeconds());
		record.setStatus(KConstants.OrderStatus.END);
		record.setDesc("红包退款");
		
		recordManager.saveConsumeRecord(record);
		userManager.rechargeUserMoeny(userId, money, KConstants.MOENY_ADD);
		User toUser=userManager.getUser(toUserId);
//		MessageBean messageBean=new MessageBean();
//		messageBean.setType(KXMPPServiceImpl.RECEDEREDPAKET);
//		if(toUser!=null){
//			messageBean.setFromUserId(toUser.getUserId().toString());
//			messageBean.setFromUserName(toUser.getNickname());
//		}else {
//			messageBean.setFromUserId(roomJid);
//			messageBean.setFromUserName(roomJid);
//		}
//		
//		if(roomJid!=null){
//			messageBean.setObjectId(roomJid);
//		}
//		messageBean.setContent(id.toString());
//		messageBean.setToUserId(userId.toString());
//		messageBean.setMsgType(0);// 单聊消息
//		messageBean.setMessageId(StringUtil.randomUUID());
//		try {
//			KXMPPServiceImpl.getInstance().send(messageBean);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		
		System.out.println(userId+"  发出的红包,剩余金额   "+money+"  未领取  退回余额!");
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
			 transferRecedeMoney(userId, toUserId, money, transferId);
		}
		System.out.println("转账超时未领取的数量 ======> "+objs.size());
	}
	
	// 转账退回
	public void transferRecedeMoney(Integer userId,Integer toUserId,double money,ObjectId transferId){
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
		
		User sysUser=userManager.getUser(1100);
		Transfer transfer=ds.createQuery(Transfer.class).field("_id").equal(transferId).get();
		transfer.setId(null);
//		MessageBean messageBean=new MessageBean();
//		messageBean.setType(KXMPPServiceImpl.REFUNDTRANSFER);
//		
//		messageBean.setFromUserId(sysUser.getUserId().toString());
//		messageBean.setFromUserName(sysUser.getNickname());
//		
//		messageBean.setContent(JSONObject.toJSON(transfer));
//		messageBean.setToUserId(userId.toString());
//		messageBean.setMsgType(0);// 单聊消息
//		messageBean.setMessageId(StringUtil.randomUUID());
//		try {
//			KXMPPServiceImpl.getInstance().send(messageBean);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		Msg messageBean = new Msg();
		messageBean.setFrom(sysUser.getId().toString());
		
		messageBean.setOpe(0);//单聊
		messageBean.setMsgtype(0);//点对点
		messageBean.setTo(userId.toString());
		
		messageBean.setAttach("");//自定义内容 json
		messageBean.setPushcontent(JSONObject.toJSONString(transfer));//推送文案，android以此为推送显示文案；ios若未填写payload，显示文案以pushcontent为准。超过500字符后，会对文本进行截断。
//		messageBean.setPayload("");//ios 推送对应的payload,必须是JSON,不能超过2k字符
//		messageBean.setSound("");//声音
		messageBean.setSave(2);//会存离线
//		messageBean.setOption("");
		messageBean.setMsgid(StringUtil.randomUUID());
		try {
			JSONObject sendAttachMsg = SDKService.sendAttachMsg(messageBean);
			if(sendAttachMsg.getInteger("code")!=200) 
				log.debug("转账退款发送失败");
		} catch (Exception e) {
			e.printStackTrace();
			log.debug("转账退款消息发送失败"+e.getMessage());
		}
		
		System.out.println(userId+"  发出转账,剩余金额   "+money+"  未收钱  退回余额!");
	}
}
