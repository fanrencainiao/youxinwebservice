package com.youxin.app.entity;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Entity(value = "public_permission", noClassnameStored = true)
@Getter
@Setter
@ApiModel(value = "平台授权")
public class PublicPermission {
	private ObjectId id;
	@ApiModelProperty("主键字符串形式")
	private String sid;
	@ApiModelProperty("授权对象")
	private String toObj;
	@ApiModelProperty("授权接口")
	private String api;
	@ApiModelProperty("授权访问url")
	private String url;
	@ApiModelProperty("授权类型")
	private int type;
	@ApiModelProperty("授权状态")
	private int state;
	@ApiModelProperty("创建时间")
	private Long createTime;
	@ApiModelProperty("修改时间")
	private Long updateTime;

}
