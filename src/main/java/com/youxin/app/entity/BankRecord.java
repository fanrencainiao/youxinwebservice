package com.youxin.app.entity;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

import lombok.Data;

/**
 * 用户银行卡提现记录  实体
 * @author lidaye
 *
 */
@Data
@Entity(value="bankRecord",noClassnameStored=true)
public class BankRecord {

	private @Id ObjectId id;
	
	private int userId;
	
	private long createTime;
	/**
	 * 订单状态   0 创建   1 支付成功  -1 支付失败
	 */
	private int status;
	
	private String nonceStr;
	
	
	/**
	 * 商户转账订单号
	 */
	private @Indexed String outTradeNo;
	
	/**
	 * 提现银行卡
	 */
	private String bankCard;
	
	/**
	 * 银行卡名称
	 */
	private String bankName;
	
	/**
	 * 提现银行卡户主
	 */
	private String name;

	
	/**
	 * 提现金额  
	 */
	private String totalFee;
	/**
	 * 手续费
	 */
	private String fee;
	
	/**
	 * 实际到账金额
	 */
	private String realFee;
	
	/**
	 * 银行卡支付成功时间
	 */
	private Long payTime;
	
	/**
	 * 银行卡订单号
	 */
	private String payNo;
	
	/**
	 * 备注
	 */
	private String des;
	
	private String resultCode;
	private String returnCode;
	
	/**
	 * 错误信息
	 */
	private String errCode;
	private String errDes;
}
