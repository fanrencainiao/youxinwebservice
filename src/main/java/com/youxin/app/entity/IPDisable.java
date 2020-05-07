package com.youxin.app.entity;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import com.youxin.app.entity.User.Loc;
import com.youxin.app.entity.User.LoginLog;
import com.youxin.app.entity.User.UserSettings;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

/**
 * ip禁用	
 * @author cf
 * @date 2020年5月7日 上午10:30:03
 */
@Entity(value = "ip_disable", noClassnameStored = true)
@Getter
@Setter
public class IPDisable {
	@Id
	private ObjectId id;
	private String sid;
	//客户端ip
	private String cip;
	//服务端ip
	private String sip;
	//禁用 1禁用 -1解禁
	private int disable;
	private long createTime;
	private long updateTime;
	
}
