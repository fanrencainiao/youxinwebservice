package com.youxin.app.entity;

import java.util.List;

import javax.validation.constraints.Min;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
/**
 *  收藏
 * @author cf
 * @date 2020年3月18日 下午2:34:59
 */
@Entity(value = "user_collect", noClassnameStored = true)
@Getter
@Setter
@ApiModel(value = "收藏")
public class UserCollect {
	@Id
	@ApiModelProperty(hidden = true)
	private ObjectId id;
	@ApiModelProperty("用以修改时使用，cid=id,类型不同")
	private String cid;
	@Indexed
	@ApiModelProperty("用户id")
	private Integer userId;
	@ApiModelProperty("发送者id")
	@Min(1000)
	private Integer send;
	@ApiModelProperty("发送者sessionid")
	private String sendSessionId;
	@ApiModelProperty("发送类型，1单聊，2群里")
	private Integer sendType;
	@ApiModelProperty("发送时间")
	private Long sendTime;
	@ApiModelProperty("收藏时间")
	private Long collectTime;
	@ApiModelProperty("收藏类型：1文字，2图片，3视频，4位置，5链接")
	private Integer collectType;
	private String textContent;
	@ApiModelProperty("url地址，2.3.5对应相应类型的url地址")
	private String urlContent;
	@ApiModelProperty("跳转地址")
	private String targetUrlContent;
	@ApiModelProperty(value = "纬度")
	protected double latitude;
	@ApiModelProperty(value = "经度")
	protected double longitude;
	@ApiModelProperty(value = "标题")
	protected String title;
	@ApiModelProperty("描述")
	private String des;

}
