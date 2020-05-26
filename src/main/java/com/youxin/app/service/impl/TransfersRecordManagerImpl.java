package com.youxin.app.service.impl;

import java.text.DecimalFormat;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.youxin.app.entity.BankRecord;
import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.entity.TransfersRecord;
import com.youxin.app.repository.TransferRecordRepository;
import com.youxin.app.service.UserService;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.ReqUtil;



@Service
public class TransfersRecordManagerImpl{

	protected Log log=LogFactory.getLog("pay");
	@Autowired
	@Qualifier("get")
	private Datastore dfds;
	
	@Autowired
	private TransferRecordRepository trr;
	@Autowired 
	private ConsumeRecordManagerImpl crmi;
	@Autowired 
	private UserService us;
	
	
	
	
	/**
	 * 微信提现
	 * @param record
	 */
	public synchronized void  transfersToWXUser(TransfersRecord record) {
		try {
			ConsumeRecord entity=new ConsumeRecord();
		 	entity.setUserId(ReqUtil.getUserId());
			entity.setTime(DateUtil.currentTimeSeconds());
			entity.setType(KConstants.ConsumeType.PUT_RAISE_CASH);
			entity.setDesc("微信提现");
			entity.setStatus(KConstants.OrderStatus.END);
			entity.setTradeNo(record.getOutTradeNo());
			entity.setPayType(KConstants.PayType.BALANCEAY);
			
			DecimalFormat df = new DecimalFormat("#.00");
			double total=Double.valueOf(record.getTotalFee())/100;
			
			total= Double.valueOf(df.format(total));
			
			entity.setMoney(total);
			
			trr.save(record);
			crmi.saveConsumeRecord(entity);
			us.rechargeUserMoeny(record.getUserId(), total, 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	/**
	 * 支付宝提现
	 * @param record
	 */
//	public synchronized void transfersToAliPay(AliPayTransfersRecord record){
//		try {
//			ConsumeRecord entity=new ConsumeRecord();
//			entity.setUserId(ReqUtil.getUserId());
//			entity.setTime(DateUtil.currentTimeSeconds());
//			entity.setType(KConstants.ConsumeType.PUT_RAISE_CASH);
//			entity.setDesc("支付宝提现");
//			entity.setStatus(KConstants.OrderStatus.END);
//			entity.setTradeNo(record.getOutTradeNo());
//			entity.setPayType(KConstants.PayType.BALANCEAY);
//			double total=Double.valueOf(record.getTotalFee());
//			entity.setMoney(total);
//			
//			saveEntity(record);
//			crmi.saveConsumeRecord(entity);
//			us.rechargeUserMoeny(record.getUserId(), total, 2);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * 银行卡
	 * @param record
	 */
	public synchronized void transfersToBank(BankRecord record){
		try {
			ConsumeRecord entity=new ConsumeRecord();
			entity.setUserId(ReqUtil.getUserId());
			entity.setTime(DateUtil.currentTimeSeconds());
			entity.setType(KConstants.ConsumeType.PUT_RAISE_CASH);
			entity.setDesc("实际提现");
			entity.setStatus(KConstants.OrderStatus.END);
			entity.setTradeNo(record.getOutTradeNo());
			entity.setPayType(KConstants.PayType.BALANCEAY);
			double realFee=Double.valueOf(record.getRealFee());
			entity.setMoney(realFee);
			
			dfds.save(record);
			crmi.saveConsumeRecord(entity);
			ObjectId id=entity.getId();
			entity.setDesc("提现手续费");
			double fee=Double.valueOf(record.getFee());
			entity.setMoney(fee);
			entity.setId(null);
			crmi.saveConsumeRecord(entity);
			us.rechargeUserMoeny(record.getUserId(), Double.valueOf(record.getTotalFee()), 2);
//			有问题
//			entity.setStatus(KConstants.OrderStatus.END);
//			crmi.saveConsumeRecord(entity);
//			entity.setId(id);
//			crmi.saveConsumeRecord(entity);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
