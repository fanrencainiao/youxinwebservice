package com.youxin.app.service;

import com.youxin.app.entity.CodePay;

public interface CodePayService {

	Integer analysisCode(String paymentCode);

	void paymentCodePay(String paymentCode, Integer userId, Integer fromUserId, String money, String desc);

	boolean checkPaymentCode(Integer userId, String paymentCode);

	void receipt(Integer userId, Integer fromUserId, String money, String desc);

	void saveCodePay(CodePay entity);
	
	
}
