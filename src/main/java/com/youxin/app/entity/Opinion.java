package com.youxin.app.entity;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import com.youxin.app.utils.DateUtil;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Entity(value = "Opinion", noClassnameStored = true)
@Getter
@Setter
@ApiModel(value = "意见箱")
public class Opinion {
	@ApiModelProperty("id")
	private @Id ObjectId id;
	@ApiModelProperty("用户id")
	private Integer userId;
	@ApiModelProperty("用户意见")
	private String opinion;
	@ApiModelProperty("提交时间")
	private Long createTime;
	@ApiModelProperty("处理时间")
	private Long updateTime;
	@ApiModelProperty("处理状态")
	private Integer state=0;

}
