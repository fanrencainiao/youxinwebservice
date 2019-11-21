package com.youxin.app.service.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.entity.Transfer;
import com.youxin.app.entity.TransferReceive;
import com.youxin.app.entity.User;
import com.youxin.app.entity.msgbody.MsgBody;
import com.youxin.app.repository.TransferRepository;
import com.youxin.app.service.UserService;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.ThreadUtil;
import com.youxin.app.utils.alipay.util.AliPayUtil;
import com.youxin.app.utils.supper.Callback;
import com.youxin.app.yx.SDKService;
import com.youxin.app.yx.request.Msg;
import com.youxin.app.yx.request.MsgRequest;

@Service
public class TransferManagerImpl{
	
	protected Log log=LogFactory.getLog("pay");
	@Autowired
	@Qualifier("get")
	private Datastore dfds;
	@Autowired
	private TransferRepository tr;
	@Autowired
	private UserService userService;
	@Autowired
	private ConsumeRecordManagerImpl crm;
	
	public Transfer saveTransfer(Transfer entity){
		ObjectId id= (ObjectId) tr.save(entity).getId();
		entity.setId(id);
		return entity;
	}
	
	/**
	 * 获取转账信息
	 * @param userId
	 * @param id
	 * @return
	 */
	public Result getTransferById(Integer userId,ObjectId id){
		Transfer transfer=tr.get(id);
//		Map<String,Object> map=Maps.newHashMap();
//		map.put("transfer", transfer);
		//判断转账是否超时
		if(DateUtil.currentTimeSeconds()>transfer.getOutTime()){
			return Result.error("该转账已超过24小时",transfer);
		}
		// 判断转账状态是否正常
		if(transfer.getStatus()!=1){
			return Result.error("该转账已完成或退款",transfer);
		}
		
		return Result.success(transfer);
	}
	
	public synchronized Result sendTransfer(Integer userId,String money,Transfer transfer){
		User user = userService.getUser(userId);
		if(user.getBalance()<Double.valueOf(money)){
			return Result.error("余额不足,请先充值!");
		}
		transfer.setUserId(userId);
		transfer.setUserName(user.getName());
		long cuTime=DateUtil.currentTimeSeconds();
		transfer.setCreateTime(cuTime);
		transfer.setOutTime(cuTime+KConstants.Expire.DAY1);
		if(StringUtil.isEmpty(transfer.getRemark())){
			transfer.setRemark("");
		}
		Object data=saveTransfer(transfer);
		//修改金额
		userService.rechargeUserMoeny(userId, transfer.getMoney(), KConstants.MOENY_REDUCE);
		new ThreadUtil();
		//开启一个线程 添加一条消费记录
		ThreadUtil.executeInThread(new Callback() {
			@Override
			public void execute(Object obj) {
				String tradeNo=AliPayUtil.getOutTradeNo();
				//创建消费记录
				ConsumeRecord record=new ConsumeRecord();
				record.setUserId(userId);
				record.setTradeNo(tradeNo);
				record.setMoney(transfer.getMoney());
				record.setStatus(KConstants.OrderStatus.END);
				record.setType(KConstants.ConsumeType.SEND_TRANSFER);
				record.setPayType(KConstants.PayType.BALANCEAY); //余额支付
				record.setDesc("转账");
				record.setTime(DateUtil.currentTimeSeconds());
				crm.saveConsumeRecord(record);
			}
		});
		return Result.success(data);
	}
	
	/**
	 * 转账收钱
	 * @param userId
	 * @param id
	 * @return
	 */
	public synchronized Result receiveTransfer(Integer userId,ObjectId id){
		Transfer transfer=tr.get(id);
		//判断转账是否超时
		if(DateUtil.currentTimeSeconds()>transfer.getOutTime()){
			return Result.error("该转账已超过24小时",transfer);
		}
		// 判断转账状态是否已经完成
		if(transfer.getStatus()!=1){
			return Result.error("该转账已完成或退款",transfer);
		}
		
		// 判断是否发送给该用户的转账
		if(!userService.getUserFromDB(transfer.getToAccid()).getId().equals(userId)){
			return Result.error("收款人不正确",transfer);
		}
		
		User user=userService.getUser(userId);
		UpdateOperations<Transfer> ops=tr.createUpdateOperations();
		ops.set("status", 2);
		ops.set("receiptTime", DateUtil.currentTimeSeconds());
		Query<Transfer> q = tr.createQuery().field("_id").equal(transfer.getId());
		tr.update(q, ops);
		
		TransferReceive receive=new TransferReceive();
		receive.setMoney(transfer.getMoney());
		receive.setSendId(transfer.getUserId());
		receive.setUserId(userId);
		receive.setSendName(transfer.getUserName());
		receive.setUserName(user.getName());
		receive.setTransferId(transfer.getId().toString());
		receive.setTime(DateUtil.currentTimeSeconds());
		dfds.save(receive);
		 
		//修改金额
		userService.rechargeUserMoeny(userId, transfer.getMoney(), KConstants.MOENY_ADD);
		// 发送xmpp消息

		MsgRequest messageBean = new MsgRequest();
		messageBean.setFrom(user.getAccid());
		messageBean.setType(100);// 文本
	
		messageBean.setOpe(0);// 个人消息
		messageBean.setTo(transfer.getAccid());
		transfer.setRid(transfer.getId().toString());
//		messageBean.setBody("{\"type\":"+KConstants.MsgType.TRANSFERRECIEVE+",\"data\":"+JSON.toJSONString(transfer)+"}");
		messageBean.setBody(JSON.toJSONString(new MsgBody(0, KConstants.MsgType.TRANSFERRECIEVE, transfer)));
		try {
			JSONObject json=SDKService.sendMsg(messageBean);
			if(json.getInteger("code")!=200) 
				log.debug("收钱 sdk消息发送失败");
		} catch (Exception e) {
			e.printStackTrace();
			log.debug("收钱 sdk消息发送失败"+e.getMessage());
		}
		
		//开启一个线程 添加一条消费记录
		new Thread(new Runnable() {
			@Override
			public void run() {
				String tradeNo=AliPayUtil.getOutTradeNo();
				//创建消费记录
				ConsumeRecord record=new ConsumeRecord();
				record.setUserId(userId);
				record.setTradeNo(tradeNo);
				record.setMoney(transfer.getMoney());
				record.setStatus(KConstants.OrderStatus.END);
				record.setType(KConstants.ConsumeType.RECEIVE_TRANSFER);
				record.setPayType(KConstants.PayType.BALANCEAY); //余额支付
				record.setDesc("接受转账");
				record.setTime(DateUtil.currentTimeSeconds());
				crm.saveConsumeRecord(record);
			}
		}).start();
		
		return Result.success(receive);
	}
	
	/**
	 * 发起转账列表
	 * @param userId
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	public List<Transfer> getTransferList(Integer userId,int pageIndex,int pageSize){
		Query<Transfer> query=tr.createQuery().field("userId").equal(userId);
		return query.order("-createTime").offset(pageIndex*pageSize).limit(pageSize).asList();
	}
	
	/**
	 * 接受转账列表
	 * @param userId
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 */
	public List<TransferReceive> getTransferReceiveList(Integer userId,int pageIndex,int pageSize){
		Query<TransferReceive> query=dfds.createQuery(TransferReceive.class).field("userId").equal(userId);
		return query.order("-time").offset(pageIndex*pageSize).limit(pageSize).asList();
	}
}
