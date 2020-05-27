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
@Entity(value = "user_key_word", noClassnameStored = true)
@Getter
@Setter
@ApiModel(value = "敏感词用户")
public class UserKeyWord {
	@Id
	@ApiModelProperty(hidden = true)
	private ObjectId id;
	@ApiModelProperty("用以修改时使用，cid=id,类型不同")
	private String cid;
	@Indexed
	@ApiModelProperty("用户id")
	private Integer userId;
	@Indexed
	@ApiModelProperty("用户accid")
	private String accid;
	@ApiModelProperty("消息id")
	private String msgid;
	@Indexed
	@ApiModelProperty("敏感词")
	private String keyWord;
	@Indexed
	@ApiModelProperty("记录时间")
	private Long time;
	

}
