package com.youxin.app.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.youxin.app.entity.CodePay;
import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.entity.User;
import com.youxin.app.entity.RedPacket.SendRedPacket;
import com.youxin.app.entity.msgbody.MsgBody;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.repository.CodePayRepository;
import com.youxin.app.service.CodePayService;
import com.youxin.app.service.UserService;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.Md5Util;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.alipay.util.AliPayUtil;
import com.youxin.app.yx.SDKService;
import com.youxin.app.yx.request.Msg;
import com.youxin.app.yx.request.MsgRequest;


/**
 * 
 * @author cf
 * @date 2019年10月10日 上午11:03:51
 */
@Service
public class CodePayServiceImpl implements CodePayService {
	
	protected Log log=LogFactory.getLog("pay");
	
	@Autowired
	private CodePayRepository payRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private ConsumeRecordManagerImpl consumeRecordManager;
	@Autowired
	private RedisServiceImpl redisService;

	/**
	 * 解析20位支付码
	 * 加密规则   (userId+n+opt)长度+(userId+n+opt)+opt+(time/opt)
	 * @param paymentCode
	 * @return
	 */
	@Override
	public Integer analysisCode(String paymentCode){
		int n=9;// 固定值
		String userIdCodeLength=paymentCode.substring(0, 1);// 第一位数（userId+n+opt）的长度
		
		// userIdCode=userId+n+opt
		String userIdCode=paymentCode.substring(userIdCodeLength.length(),Integer.valueOf(userIdCodeLength)+1);
		
		int three=userIdCodeLength.length()+userIdCode.length();
		String opt=paymentCode.substring(three, three+3);
		
		int four=three+3;
		// timeCode=time/opt
		String timeCode=paymentCode.substring(four,paymentCode.length());
		
		int userId=Integer.valueOf(userIdCode)-n-Integer.valueOf(opt);
		
		long time=Integer.valueOf(timeCode)*Integer.valueOf(opt);
		if(System.currentTimeMillis()/1000-time<256){
			return userId;
		}else{
			time=Integer.valueOf(timeCode)*(Integer.valueOf(opt)-100);
			if(System.currentTimeMillis()/1000-time<256){
				return userId;
			}else{
				return null;
			}
		}
		
	}
	
	/**
	 * 付款码操作账户金额
	 * @param userId 收线方
	 * @param fromUserId 付款方--码的所有方
	 * @param money
	 */
	@Override
	public synchronized void paymentCodePay(String paymentCode,Integer userId,Integer fromUserId,String money,String desc){
		User fromUser=userService.getUser(fromUserId);
		User user=userService.getUser(userId);
		if(fromUser.getBalance()<Double.valueOf(money)){
			throw new ServiceException("对方余额不足,扣款失败");
		}
		CodePay codePay=new CodePay();
		codePay.setUserId(fromUserId);
		codePay.setUserName(fromUser.getName());
		codePay.setType(1);
		codePay.setToUserId(userId);
		codePay.setToUserName(user.getName());
		codePay.setMoney(Double.valueOf(money));
		codePay.setCreateTime(DateUtil.currentTimeSeconds());
		saveCodePay(codePay);
		// 减钱
		userService.rechargeUserMoeny(fromUserId, Double.valueOf(money), KConstants.MOENY_REDUCE);
		String lessTradeNo=AliPayUtil.getOutTradeNo();
		//创建减钱消费记录
		ConsumeRecord lessRecord=new ConsumeRecord();
		lessRecord.setUserId(fromUserId);
		lessRecord.setTradeNo(lessTradeNo);
		lessRecord.setMoney(Double.valueOf(money));
		lessRecord.setStatus(KConstants.OrderStatus.END);
		lessRecord.setType(KConstants.ConsumeType.SEND_PAYMENTCODE);
		lessRecord.setPayType(KConstants.PayType.BALANCEAY); //余额支付
		if(!StringUtil.isEmpty(desc))
			lessRecord.setDesc(desc);
		else
			lessRecord.setDesc("付款码已付款");
		lessRecord.setTime(DateUtil.currentTimeSeconds());
		consumeRecordManager.saveConsumeRecord(lessRecord);
		
		// 发送xmpp扣款消息通知
		User sysUser=userService.getUser(1100);
		Msg messageBean = new Msg();
		messageBean.setFrom(sysUser.getId().toString());
		messageBean.setMsgtype(0);//点对点
		messageBean.setOpe(0);//点对点
		messageBean.setTo(fromUserId.toString());
		messageBean.setAttach("");//自定义内容 json
		messageBean.setPushcontent(JSONObject.toJSONString(codePay));//推送文案，android以此为推送显示文案；ios若未填写payload，显示文案以pushcontent为准。超过500字符后，会对文本进行截断。
//		messageBean.setPayload("");//ios 推送对应的payload,必须是JSON,不能超过2k字符
//		messageBean.setSound("");//声音
		messageBean.setSave(2);//会存离线
//		messageBean.setOption("");

		try {
			JSONObject sendAttachMsg = SDKService.sendAttachMsg(messageBean);
			if(sendAttachMsg.getInteger("code")!=200) 
				log.debug("付款 sdk消息发送失败");
		} catch (Exception e) {
			e.printStackTrace();
			log.debug(e.getMessage());
		}
		
		// 加钱
		userService.rechargeUserMoeny(userId, Double.valueOf(money), KConstants.MOENY_ADD);
		String addTradeNo=AliPayUtil.getOutTradeNo();
		//创建加钱消费记录
		ConsumeRecord addRecord=new ConsumeRecord();
		addRecord.setUserId(userId);
		addRecord.setTradeNo(addTradeNo);
		addRecord.setMoney(Double.valueOf(money));
		addRecord.setStatus(KConstants.OrderStatus.END);
		addRecord.setType(KConstants.ConsumeType.RECEIVE_PAYMENTCODE);
		addRecord.setPayType(KConstants.PayType.BALANCEAY); //余额支付
		addRecord.setDesc("付款码已收款");
		addRecord.setTime(DateUtil.currentTimeSeconds());
		consumeRecordManager.saveConsumeRecord(addRecord);
		
		// 发送xmpp通知收款成功
		Msg message = new Msg();
		message.setFrom(sysUser.getId().toString());
		message.setMsgtype(0);//点对点
		message.setOpe(0);//点对点
		message.setTo(userId.toString());
		message.setAttach("");//自定义内容 json
		message.setPushcontent(JSONObject.toJSONString(codePay));//推送文案，android以此为推送显示文案；ios若未填写payload，显示文案以pushcontent为准。超过500字符后，会对文本进行截断。
//		message.setPayload("");//ios 推送对应的payload,必须是JSON,不能超过2k字符
//		message.setSound("");//声音
		message.setSave(2);//会存离线
//		message.setOption("");

		try {
			JSONObject sendAttachMsg = SDKService.sendAttachMsg(message);
			if(sendAttachMsg.getInteger("code")!=200) 
				log.debug("收款 sdk消息发送失败");
		} catch (Exception e) {
			e.printStackTrace();
			log.debug(e.getMessage());
		}
		
//		MessageBean message=new MessageBean();
//		message.setFromUserId(sysUser.getUserId().toString());
//		message.setFileName(sysUser.getNickname());
//		message.setType(KXMPPServiceImpl.CODEARRIVAL);
//		message.setMsgType(0);
//		message.setMessageId(StringUtil.randomUUID());
//		message.setContent(JSONObject.toJSONString(codePay));
//		message.setToUserId(userId.toString());
//		message.setToUserName(user.getNickname());
//		try {
//			KXMPPServiceImpl.getInstance().send(message);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		// 保存用户付款码缓存
		redisService.savePaymentCode(paymentCode, fromUserId);
	}
	
	/**
	 * 检验付款码唯一性
	 * @param userId
	 * @param paymentCode
	 * @return
	 */
	@Override
	public boolean checkPaymentCode(Integer userId,String paymentCode){
		Integer value=redisService.getPaymentCode(paymentCode);
		if(null!=value){
			return true;
		}else{
			return false;
		}
		
	}
	
	/**
	 * 二维码收钱操作金额
	 * @param userId  付款方(金额减少)
	 * @param fromUserId  收线方(金额增加)--码的所有者
	 * @param money
	 */
	@Override
	public synchronized void receipt(Integer userId,Integer fromUserId,String money,String desc){
		User user = userService.getUser(userId);
		User fromUser = userService.getUser(fromUserId);
		if(user.getBalance()<Double.valueOf(money)){
			throw new ServiceException("余额不足，交易失败");
		}
		
		CodePay codePay=new CodePay();
		codePay.setUserId(fromUserId);
		codePay.setType(2);
		codePay.setUserName(fromUser.getName());
		codePay.setToUserId(userId);
		codePay.setToUserName(user.getName());
		codePay.setMoney(Double.valueOf(money));
		codePay.setCreateTime(DateUtil.currentTimeSeconds());
		saveCodePay(codePay);
		
		userService.rechargeUserMoeny(userId, Double.valueOf(money), KConstants.MOENY_REDUCE);
		String lessTradeNo=AliPayUtil.getOutTradeNo();
		//创建减钱消费记录 
		ConsumeRecord lessRecord=new ConsumeRecord();
		lessRecord.setUserId(userId);
		lessRecord.setTradeNo(lessTradeNo);
		lessRecord.setMoney(Double.valueOf(money));
		lessRecord.setStatus(KConstants.OrderStatus.END);
		lessRecord.setType(KConstants.ConsumeType.SEND_QRCODE);
		lessRecord.setPayType(KConstants.PayType.BALANCEAY); // 余额支付
		if(!StringUtil.isEmpty(desc))
			lessRecord.setDesc(desc);
		else
			lessRecord.setDesc("二维码收款已付款");
		lessRecord.setTime(DateUtil.currentTimeSeconds());
		consumeRecordManager.saveConsumeRecord(lessRecord);
		
		User sysUser=userService.getUser(1100);
		
		MsgRequest messageBean = new MsgRequest();
		messageBean.setType(100);// 自定义

			messageBean.setOpe(0);// 个人消息
			messageBean.setFrom(Md5Util.md5HexToAccid(sysUser.getId().toString()));
			messageBean.setTo(Md5Util.md5HexToAccid(userId + ""));
			log.debug("userId" + userId);

		messageBean.setBody(JSON.toJSONString(new MsgBody(0,
				KConstants.MsgType.CODEREDUCE,
				codePay)));

		try {
			JSONObject json = SDKService.sendMsg(messageBean);
			if (json.getInteger("code") != 200)
				log.debug("二维码扣款发送 sdk消息发送失败");
		} catch (Exception e) {
			e.printStackTrace();
			log.debug("二维码扣款发送 sdk消息发送失败" + e.getMessage());
		}

		
		// 加钱
		userService.rechargeUserMoeny(fromUserId, Double.valueOf(money), KConstants.MOENY_ADD);
		String addTradeNo=AliPayUtil.getOutTradeNo();
		// 创建加钱消费记录
		ConsumeRecord addRecord=new ConsumeRecord();
		addRecord.setUserId(fromUserId);
		addRecord.setTradeNo(addTradeNo);
		addRecord.setMoney(Double.valueOf(money));
		addRecord.setStatus(KConstants.OrderStatus.END);
		addRecord.setType(KConstants.ConsumeType.RECEIVE_QRCODE);
		addRecord.setPayType(KConstants.PayType.BALANCEAY); //余额支付
		addRecord.setDesc("二维码收款已到账");
		addRecord.setTime(DateUtil.currentTimeSeconds());
		consumeRecordManager.saveConsumeRecord(addRecord);
		
		// 发送xmpp通知收款成功
		MsgRequest messageBean1 = new MsgRequest();
		messageBean1.setType(100);// 自定义

		messageBean1.setOpe(0);// 个人消息
		messageBean1.setFrom(Md5Util.md5HexToAccid(sysUser.getId().toString()));
		messageBean1.setTo(Md5Util.md5HexToAccid(fromUserId + ""));
			log.debug("userId" + userId);

			messageBean1.setBody(JSON.toJSONString(new MsgBody(0,
				KConstants.MsgType.CODEADD,
				codePay)));

		try {
			JSONObject json = SDKService.sendMsg(messageBean1);
			if (json.getInteger("code") != 200)
				log.debug("二维码收款发送 sdk消息发送失败");
		} catch (Exception e) {
			e.printStackTrace();
			log.debug("二维码收款发送 sdk消息发送失败" + e.getMessage());
		}
	}
	@Override
	public void saveCodePay(CodePay entity){
		payRepository.save(entity);
		
	}

}
