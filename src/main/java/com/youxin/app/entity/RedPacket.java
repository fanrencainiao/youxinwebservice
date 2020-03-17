package com.youxin.app.entity;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.NotSaved;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

///红包实体
@ApiModel(value = "红包")
@Entity(value="RedPacket",noClassnameStored=true)
public class RedPacket {
	@ApiModelProperty(hidden = true)
	private @Id ObjectId id;
	@ApiModelProperty(hidden = true)
	@NotSaved
	private String rid;
	//发送者用户Id
	@ApiModelProperty(value="发送者用户Id")
	private @Indexed Integer userId; 
	@ApiModelProperty(value="发送者用户accid")
	private @Indexed String accid; 
	@ApiModelProperty(value="发送到那个房间")
	private String roomJid;// 发送到那个房间
	@ApiModelProperty(value="发送给那个人")
	private Integer toUserId;// 发送给那个人
	@ApiModelProperty(value="发送给那个人")
	private String toAccid;// 发送给那个人
	
	@ApiModelProperty(value="红包发送者昵称")
	private String userName;
	@ApiModelProperty(value="祝福语")
	private String greetings;
	@ApiModelProperty(value="发送时间")
	private long sendTime;
	@ApiModelProperty(value="红包类型 1：普通红包  2：拼手气红包  3:口令红包 4:定向红包")
	private @Indexed int type; // 
	@ApiModelProperty(value="红包支付类型 0零钱支付，1支付宝支付")
	private @Indexed int payType; // 
	@ApiModelProperty(value="商户端支付宝订单号")
	private  String payNo;
	@ApiModelProperty(value="支付宝订单号")
	private  String aliPayNo;
	@ApiModelProperty(value="红包个数")
	private int count;
	@ApiModelProperty(value="已领取个数")
	private int receiveCount=0;
	@ApiModelProperty(value="红包金额")
	private Double money;
	
	@ApiModelProperty(value="红包剩余金额")
	private Double over;
	@ApiModelProperty(value="超时时间")
	private long outTime;
	@ApiModelProperty(value="红包状态  1 ：发出   2：已领完       -1：已退款        //3:未领完退款")
	private @Indexed int status=1;
	
	@ApiModelProperty(value="已经领取该红包的 userId")
	private List<Integer> userIds=new ArrayList<Integer>(); 
	@ApiModelProperty(value="指定领取该红包的 userId")
	private List<Integer> toUserIds=new ArrayList<Integer>(); 
	
	@Data
	public static class SendRedPacket{
		String redId;
		String greeting;
		int redType;
		String accId;
		int redGetType;
		String redMoney;
		
		public SendRedPacket() {
			super();
		}
		public SendRedPacket(String redId, String greeting, int redType, String accId, int redGetType,
				String redMoney) {
			super();
			this.redId = redId;
			this.greeting = greeting;
			this.redType = redType;
			this.accId = accId;
			this.redGetType = redGetType;
			this.redMoney = redMoney;
		}

		

	
		
	}
	
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	
	public String getRoomJid() {
		return roomJid;
	}
	public void setRoomJid(String roomJid) {
		this.roomJid = roomJid;
	}
	public String getGreetings() {
		return greetings;
	}
	public void setGreetings(String greetings) {
		this.greetings = greetings;
	}
	public long getSendTime() {
		return sendTime;
	}
	public void setSendTime(long sendTime) {
		this.sendTime = sendTime;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getReceiveCount() {
		return receiveCount;
	}
	public void setReceiveCount(int receiveCount) {
		this.receiveCount = receiveCount;
	}
	public Double getMoney() {
		return money;
	}
	public void setMoney(Double money) {
		this.money = money;
	}
	public long getOutTime() {
		return outTime;
	}
	public void setOutTime(long outTime) {
		this.outTime = outTime;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public List<Integer> getUserIds() {
		return userIds;
	}
	public void setUserIds(List<Integer> userIds) {
		this.userIds = userIds;
	}
	public Double getOver() {
		return over;
	}
	public void setOver(Double over) {
		this.over = over;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public Integer getToUserId() {
		return toUserId;
	}
	public void setToUserId(Integer toUserId) {
		this.toUserId = toUserId;
	}
	public String getAccid() {
		return accid;
	}
	public void setAccid(String accid) {
		this.accid = accid;
	}
	public String getToAccid() {
		return toAccid;
	}
	public void setToAccid(String toAccid) {
		this.toAccid = toAccid;
	}
	public String getRid() {
		return rid;
	}
	public void setRid(String rid) {
		this.rid = rid;
	}
	
	public List<Integer> getToUserIds() {
		return toUserIds;
	}
	public void setToUserIds(List<Integer> toUserIds) {
		this.toUserIds = toUserIds;
	}
	public int getPayType() {
		return payType;
	}
	public void setPayType(int payType) {
		this.payType = payType;
	}
	public String getPayNo() {
		return payNo;
	}
	public void setPayNo(String payNo) {
		this.payNo = payNo;
	}
	public String getAliPayNo() {
		return aliPayNo;
	}
	public void setAliPayNo(String aliPayNo) {
		this.aliPayNo = aliPayNo;
	}
	
	
}
