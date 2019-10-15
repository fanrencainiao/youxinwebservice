package com.youxin.app.entity;

import java.math.BigDecimal;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
/**
 * 群尾包控制
 * @author cf
 * @date 2019年9月2日 上午9:41:49
 */
@Entity(value = "LastWallet", noClassnameStored = true)
public class LastWallet {
	@Id
	private ObjectId walletId;
	
	private String roomId;
	
	private String roomJid;
	
	private Double redPackgeMoney;
	
	private int state;
	
	private String name;// 房间名称
	
	// 创建时间
	private Long createTime;
	// 创建者Id
	private Integer userId;
	// 创建者昵称
	private String nickname;
	public ObjectId getWalletId() {
		return walletId;
	}
	public void setWalletId(ObjectId walletId) {
		this.walletId = walletId;
	}
	public String getRoomId() {
		return roomId;
	}
	public void setRoomId(String roomId) {
		this.roomId = roomId;
	}
	
	public int getState() {
		return state;
	}
	public void setState(int state) {
		this.state = state;
	}
	public Long getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Double getRedPackgeMoney() {
		return redPackgeMoney;
	}
	public void setRedPackgeMoney(Double redPackgeMoney) {
		this.redPackgeMoney = redPackgeMoney;
	}
	public String getRoomJid() {
		return roomJid;
	}
	public void setRoomJid(String roomJid) {
		this.roomJid = roomJid;
	}
	
	
	
}
