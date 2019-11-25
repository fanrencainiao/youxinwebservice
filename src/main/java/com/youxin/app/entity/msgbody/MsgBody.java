package com.youxin.app.entity.msgbody;

import lombok.Data;

@Data
public class MsgBody {
	
	private int wxType;//布局类型
	private int type;//消息类型
	private Object data;//数据体
	public MsgBody(int wxType,int type,Object data) {
		this.wxType=wxType;
		this.type=type;
		this.data=data;
	}
	@Data
	public static class ID {
		private String id;
	}

}
