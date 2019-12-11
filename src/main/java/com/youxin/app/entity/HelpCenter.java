package com.youxin.app.entity;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Entity(value = "help_center", noClassnameStored = true)
@ApiModel("帮助中心")
@Data
public class HelpCenter {
	private @Id ObjectId id;
	@ApiModelProperty("标题")
	private String title;
	@ApiModelProperty("类型 1：帮助")
	private int type;
	@ApiModelProperty("状态 0：未反馈过，1：解决 ，-1：未解决")
	private int state=0;
	@ApiModelProperty("内容")
	private String content;
	@ApiModelProperty("解决反馈人")
	private List<Integer> overUserIds;
	@ApiModelProperty("未解决反馈人")
	private List<Integer> noUserIds;
	@ApiModelProperty("创建时间")
	private Long createTime;
	@ApiModelProperty("修改时间")
	private Long updateTime;
	
}
