package com.youxin.app.entity;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import lombok.Data;

@Entity(value = "user_station", noClassnameStored = true)
@Data
public class UserStation {

	@Id
	private String telNumber;
	private String userName;
	private String idCard;
	private String airways;
	private String flightNo;
	private String startStation;
	private String terminalStation;
	private String flightDate;
	private String appointCount;
	private String validateCode;
	private String token;
	private String smsCode;
	private Long time;
	
	
}
