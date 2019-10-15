package com.youxin.app.entity;


import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;


/**
 * 红包控制：发红包指定个数
 * @author Theron 2019年7月5日
 *
 */
@Entity(value = "UserWallet", noClassnameStored = true)
public class UserWallet {
	
	@Id
	private ObjectId walletId;	
	
	private String telephone;
	
	private Double redPackgeMoney;
	
	private int state;
		
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
	
	public UserWallet() {
		super();
	}

	public UserWallet(ObjectId walletId, String telephone, Double redPackgeMoney, Integer userId, String nickname) {
		super();
		this.walletId = walletId;
		this.telephone = telephone;
		this.redPackgeMoney = redPackgeMoney;
		this.userId = userId;
		this.nickname = nickname;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}
}

