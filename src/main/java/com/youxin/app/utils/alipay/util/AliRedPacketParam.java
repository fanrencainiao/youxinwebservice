package com.youxin.app.utils.alipay.util;


import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import lombok.Data;
@Entity(value = "AliRedPacket", noClassnameStored = true)
@Data
public class AliRedPacketParam {
	//
	private @Id ObjectId id;
	private String msg_method;//变更通知 接口方法
	private String biz_content;//参数内容
	private Long utc_timestamp;//通知时间
	private Long app_id;
	private String version;
	private String notify_id;

}




