package com.youxin.app.entity;


import java.util.List;
import java.util.Map;

import javax.validation.constraints.Min;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.NotSaved;

import lombok.Data;

/**
 * 红包控制：发红包指定个数
 * @author Theron 2019年6月26日
 *
 */
@Entity(value = "Wallet_Four", noClassnameStored = true)
public class WalletFour {
	@Id
	private ObjectId walletId;
	
	private ObjectId roomId;
	
	private String jid; //群的id
	
	private String name;// 房间名称
	
	private String telephone;
	
	private Double redPackgeMoney;
	
	@Min(1)
	private Integer redPackegeNumber;
	
	private Integer isSetUpMoney = 0;
	
	// 创建时间
	private Long createTime;
	// 创建者Id
	private Integer userId;
	// 创建者昵称
	private String nickname;
	
	private Map<String, Double> listRedPackgeMap;
	@NotSaved
	private List<RedPackgeNumber> listRedPackgeNumber;
	
	@Data
	public static class RedPackgeNumber{
		//红包个数从1自增+1编号
		private Integer redPackgeId;
		//红包编号对应的金额
		private Double redPackgeMoney;
	}

	public ObjectId getWalletId() {
		return walletId;
	}

	public void setWalletId(ObjectId walletId) {
		this.walletId = walletId;
	}

	public ObjectId getRoomId() {
		return roomId;
	}

	public void setRoomId(ObjectId roomId) {
		this.roomId = roomId;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public Double getRedPackgeMoney() {
		return redPackgeMoney;
	}

	public void setRedPackgeMoney(Double redPackgeMoney) {
		this.redPackgeMoney = redPackgeMoney;
	}

	public Integer getRedPackegeNumber() {
		return redPackegeNumber;
	}

	public void setRedPackegeNumber(Integer redPackegeNumber) {
		this.redPackegeNumber = redPackegeNumber;
	}

	public Integer getIsSetUpMoney() {
		return isSetUpMoney;
	}

	public void setIsSetUpMoney(Integer isSetUpMoney) {
		this.isSetUpMoney = isSetUpMoney;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public List<RedPackgeNumber> getListRedPackgeNumber() {
		return listRedPackgeNumber;
	}

	public void setListRedPackgeNumber(List<RedPackgeNumber> listRedPackgeNumber) {
		this.listRedPackgeNumber = listRedPackgeNumber;
	}
	
	public WalletFour() {
		super();
	}

	public WalletFour(ObjectId walletId, ObjectId roomId, String jid,String name, String telephone, Double redPackgeMoney, Integer redPackegeNumber, 
			Integer isSetUpMoney, Integer userId, String nickname, List<RedPackgeNumber> listRedPackgeNumber) {
		super();
		this.walletId = walletId;
		this.roomId = roomId;
		this.jid = jid;
		this.name = name;
		this.telephone = telephone;
		this.redPackgeMoney = redPackgeMoney;
		this.redPackegeNumber = redPackegeNumber;
		this.isSetUpMoney = isSetUpMoney;
		this.userId = userId;
		this.nickname = nickname;
		this.listRedPackgeNumber = listRedPackgeNumber;
	}

	public Map<String, Double> getListRedPackgeMap() {
		return listRedPackgeMap;
	}

	public void setListRedPackgeMap(Map<String, Double> listRedPackgeMap) {
		this.listRedPackgeMap = listRedPackgeMap;
	}
}

