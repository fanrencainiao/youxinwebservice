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
 * 
 * @Description: TODO(转账实体)
 */
@Entity(value="Transfer",noClassnameStored=true)
@Data
@ApiModel(value = "转账")
public class Transfer {
	
	@ApiModelProperty(hidden = true)
	private @Id ObjectId id;
	@ApiModelProperty(hidden = true)
	@NotSaved
	private String rid;
	@ApiModelProperty(value="发送者用户Id")
	private @Indexed Integer userId;
	
	@ApiModelProperty(value="发送者用户accid")
	private @Indexed String accid;
	
	@ApiModelProperty(value="接受者Id")
	private Integer toUserId;
	
	@ApiModelProperty(value="发送者用户Id")
	private String toAccid;
	
	@ApiModelProperty(value="转账发送者昵称")
	private String userName;
	
	@ApiModelProperty(value="转账说明")
	private String remark;
	
	@ApiModelProperty(value="转账时间")
	private long createTime;
	
	@ApiModelProperty(value="转账金额")
	private Double money;
	
	@ApiModelProperty(value="超时时间")
	private long outTime;
	
	@ApiModelProperty(value="转账状态 1 ：发出  2：已收款  -1：已退款 ")
	private @Indexed int status=1;
	@ApiModelProperty(value="收款时间 ")
	private long receiptTime;
	
}
