package com.youxin.app.entity;

import java.text.DecimalFormat;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.NotSaved;

//消费记录实体
@Entity(value = "ConsumeRecord", noClassnameStored = true)
public class ConsumeRecord {

	private @Id ObjectId id; //记录id
	
	private @Indexed String tradeNo; //交易单号
	
	private @Indexed Integer userId; //用户Id
	
	private Double money; //金额
	
	private Double startMoney;
	
	private Double endMoney;
	
	private long time; //时间
	
	private @Indexed int type; //类型  1:用户充值, 2:用户提现, 3:后台充值, 4:发红包, 5:领取红包, 6:红包退款  7:转账   8:接受转账   9:转账退回   10:付款码付款   11:付款码到账   12:二维码付款  13:二维码到账 14:vip充值 15:vip充值提成16:后台金额扣除
	
	private @Indexed ObjectId orderId; //type=2 消费时会有订单Id
	
	private String desc;  //消费备注
	
	private int payType;  //支付方式  1：支付宝支付 , 2：微信支付, 3：余额支付, 4:系统支付
	
	private @Indexed int status; //交易状态 0：创建  1：支付完成  2：交易完成  -1：交易关闭
	
	@NotSaved
	private String userName;// 用户昵称

	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public Double getMoney() {
		if(0<money){
			DecimalFormat df = new DecimalFormat("#.00");
			 money = Double.valueOf(df.format(money));
		}
		return money;
	}
	public void setMoney(Double money) {
		if(0<money){
			DecimalFormat df = new DecimalFormat("#.00");
			 money= Double.valueOf(df.format(money));
		}
		 
		this.money = money;
	}
	public Double getStartMoney() {
		return startMoney;
	}
	public void setStartMoney(Double startMoney) {
		this.startMoney = startMoney;
	}
	public Double getEndMoney() {
		return endMoney;
	}
	public void setEndMoney(Double endMoney) {
		this.endMoney = endMoney;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public ObjectId getOrderId() {
		return orderId;
	}
	public void setOrderId(ObjectId orderId) {
		this.orderId = orderId;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public int getPayType() {
		return payType;
	}
	public void setPayType(int payType) {
		this.payType = payType;
	}
	public String getTradeNo() {
		return tradeNo;
	}
	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
}
