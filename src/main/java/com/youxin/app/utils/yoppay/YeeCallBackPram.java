package com.youxin.app.utils.yoppay;

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
 * 易宝 支付回调参数
 * @author cf
 * @date 2020年5月15日 下午4:46:45
 */
@Entity(value = "yee_call_back_pram", noClassnameStored = true)
@Getter
@Setter
public class YeeCallBackPram {
	@Id
	private ObjectId id;
	private Integer userid;
	//=====易宝 支付回调参数=====
	private String parentMerchantNo;
	private String merchantNo;
	private String orderId;
	private String uniqueOrderNo;
	private String bankTrxId;
	private String bankOrderId;
	private String status;
	private String orderAmount;
	private String payAmount;
//	private String requestDate;
	private String paySuccessDate;
	private String requestDate;
	private String instCompany;
	private String instNumber;
	private String cardType;
	private String paymentProduct;
	private String platformType;
	private String bankId;
	private String openID;
	private String unionID;
	private String pyerAcctTp;
	private String pyerAcctId;
	private String pyerAcctNm;


}
