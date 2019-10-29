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

import com.alibaba.fastjson.JSONObject;
import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.entity.Transfer;
import com.youxin.app.entity.TransferReceive;
import com.youxin.app.entity.User;
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
			return Result.success("该转账已超过24小时");
		}
		// 判断转账状态是否正常
		if(transfer.getStatus()!=1){
			return Result.success("该转账已完成或退款");
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
		if(!transfer.getToUserId().equals(userId)){
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
//		Msg messageBean=new Msg();
//		messageBean.setType(0);
//		messageBean.setFromUserId(user.getId().toString());
//		messageBean.setFromUserName(user.getNickname());
//		messageBean.setContent(transfer.getId().toString());
//		messageBean.setToUserId(transfer.getUserId().toString());
//		messageBean.setMsgType(0);// 单聊消息
//		messageBean.setMessageId(StringUtil.randomUUID());
		Msg messageBean = new Msg();
		messageBean.setFrom(user.getId().toString());
		messageBean.setMsgtype(0);//点对点
		messageBean.setOpe(0);//点对点
		messageBean.setTo(transfer.getUserId().toString());
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
				log.debug("付款 sdk消息发送失败");
		} catch (Exception e) {
			e.printStackTrace();
			log.debug("付款 sdk消息发送失败"+e.getMessage());
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
