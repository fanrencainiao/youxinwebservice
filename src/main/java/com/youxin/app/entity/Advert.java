package com.youxin.app.entity;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;
import org.mongodb.morphia.annotations.NotSaved;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 广告位管理
 * @author cf
 * @date 2020年3月11日 下午2:20:54
 */
@Data
@Entity(value="Advert",noClassnameStored=true)
@ApiModel("广告位")
public class Advert {
	@Id
	@ApiModelProperty("主键标识")
	private ObjectId id;
	@ApiModelProperty(hidden=true)
	@NotSaved
	private String cid;
	@ApiModelProperty("标题")
	@Indexed
	private String title;
	@ApiModelProperty("内容")
	private String content;
	@ApiModelProperty("备注")
	private String des;
	@ApiModelProperty("图片")
	private String img;
	@ApiModelProperty("跳转地址")
	private String targetUrl;
	@ApiModelProperty("状态")
	private int state;
	@ApiModelProperty("类型")
	private int type;
	@ApiModelProperty("创建时间")
	private long createTime;
	@ApiModelProperty("更新时间")
	private long updateTime;
//	@ApiModelProperty("操作人")
//	private Integer userId;

}
