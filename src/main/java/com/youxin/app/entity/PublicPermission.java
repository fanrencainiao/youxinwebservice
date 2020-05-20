package com.youxin.app.entity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.NotSaved;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Entity(value = "public_permission", noClassnameStored = true)
@Getter
@Setter
@ApiModel(value = "平台授权")
public class PublicPermission {
	@Id
	private ObjectId id;
	@ApiModelProperty("主键字符串形式")
	@NotSaved
	private String sid;
	@ApiModelProperty("用户加密id")
	@NotSaved
	private String userid;
	@ApiModelProperty("授权对象")
	private String toObj;
	@ApiModelProperty("授权接口访问的ip")
	private String api;
	@ApiModelProperty("授权访问url")
	private String url;
	@ApiModelProperty("备注")
	private String des;
	@ApiModelProperty("授权类型")
	private int type;
	@ApiModelProperty("授权状态")
	private int state;
	@ApiModelProperty("创建时间")
	private Long createTime;
	@ApiModelProperty("修改时间")
	private Long updateTime;
	@ApiModelProperty("用户授权集合")
	private Set<Integer> uvList=new HashSet<Integer>();
	@ApiModelProperty("平台用户id加密集合")
	private Set<String> umd5List=new HashSet<String>();
	@ApiModelProperty("用户访问集合")
	private List<Integer> pvList=new ArrayList<Integer>();

}
