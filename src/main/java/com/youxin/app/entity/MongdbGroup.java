package com.youxin.app.entity;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import lombok.Data;


public class MongdbGroup {
	/**
	 * 用户id
	 */
	private int userId;
	/**
	 * 查询开始时间
	 */
	private long startDate;
	/**
	 * 查询结束时间
	 */
	private long endDate;

	/**
	 * 用户总充值
	 */
	private Double totalRecharge=0.0;
	/**
	 * 用户总提现
	 */
	private Double totalCash=0.0;
	/**
	 * 用户总余额
	 */
	private Double totalBalance=0.0;
	/**
	 * 微信总充值
	 */
	private Double wxTotalRecharge=0.0;
	/**
	 * 支付宝总充值
	 */
	private Double aliTotalRecharge=0.0;
	/**
	 * 系统总充值
	 */
	private Double sysTotalRecharge=0.0;
	/**
	 * 系统总扣除
	 */
	private Double sysTotalReduce=0.0;
	/**
	 * 红包总发送
	 */
	private Double totalSendRedPacket=0.0;
	/**
	 * 红包总领取
	 */
	private Double totalGetRedPacket=0.0;
	/**
	 * 红包总退款
	 */
	private Double totalBackRedPacket=0.0;
	/**
	 * 总转账
	 */
	private Double totalTransferMoney=0.0;
	/**
	 * 总接受转账
	 */
	private Double totalGetTransferMoney=0.0;
	/**
	 * 总退回转账
	 */
	private Double totalBackTransferMoney=0.0;
	/**
	 * 总付款码付款
	 */
	private Double totalCodePay=0.0;
	/**
	 * 总付款码到账
	 */
	private Double totalGetCodePay=0.0;
	/**
	 * 总二维码付款
	 */
	private Double totalQRCodePay=0.0;
	/**
	 * 总二维码到账
	 */
	private Double totalGetQRCodePay=0.0;
	/**
	 * 总vip充值
	 */
	private Double totalVipRecharge=0.0;
	/**
	 * 总vip充值提成
	 */
	private Double totalVipRechargeProfit=0.0;
	
	
	public Double getTotalRecharge() {
		return totalRecharge;
	}
	public void setTotalRecharge(Double totalRecharge) {
		this.totalRecharge = totalRecharge;
	}
	public Double getTotalCash() {
		return totalCash;
	}
	public void setTotalCash(Double totalCash) {
		this.totalCash = totalCash;
	
	}
	public Double getTotalBalance() {
		return totalBalance;
	}
	public void setTotalBalance(Double totalBalance) {
		DecimalFormat df=new DecimalFormat("#.00");
		if(totalBalance==null) {
			Double tb=totalRecharge-totalCash+sysTotalRecharge-sysTotalReduce-totalSendRedPacket+totalGetRedPacket+totalBackRedPacket-totalTransferMoney+totalGetTransferMoney+totalBackTransferMoney
					-totalCodePay+totalGetCodePay-totalQRCodePay+totalGetQRCodePay-totalVipRecharge+totalVipRechargeProfit;
					this.totalBalance=Double.valueOf(df.format(tb));
		}else
			this.totalBalance=totalBalance;
		
	}
	public Double getWxTotalRecharge() {
		return wxTotalRecharge;
	}
	public void setWxTotalRecharge(Double wxTotalRecharge) {
		this.wxTotalRecharge = wxTotalRecharge;
	}
	public Double getAliTotalRecharge() {
		return aliTotalRecharge;
	}
	public void setAliTotalRecharge(Double aliTotalRecharge) {
		this.aliTotalRecharge = aliTotalRecharge;
	}
	public Double getSysTotalRecharge() {
		return sysTotalRecharge;
	}
	public void setSysTotalRecharge(Double sysTotalRecharge) {
		this.sysTotalRecharge = sysTotalRecharge;
	}
	public Double getSysTotalReduce() {
		return sysTotalReduce;
	}
	public void setSysTotalReduce(Double sysTotalReduce) {
		this.sysTotalReduce = sysTotalReduce;
	}
	public Double getTotalSendRedPacket() {
		return totalSendRedPacket;
	}
	public void setTotalSendRedPacket(Double totalSendRedPacket) {
		this.totalSendRedPacket = totalSendRedPacket;
	}
	public Double getTotalGetRedPacket() {
		return totalGetRedPacket;
	}
	public void setTotalGetRedPacket(Double totalGetRedPacket) {
		this.totalGetRedPacket = totalGetRedPacket;
	}
	public Double getTotalBackRedPacket() {
		return totalBackRedPacket;
	}
	public void setTotalBackRedPacket(Double totalBackRedPacket) {
		this.totalBackRedPacket = totalBackRedPacket;
	}
	public Double getTotalTransferMoney() {
		return totalTransferMoney;
	}
	public void setTotalTransferMoney(Double totalTransferMoney) {
		this.totalTransferMoney = totalTransferMoney;
	}
	public Double getTotalGetTransferMoney() {
		return totalGetTransferMoney;
	}
	public void setTotalGetTransferMoney(Double totalGetTransferMoney) {
		this.totalGetTransferMoney = totalGetTransferMoney;
	}
	public Double getTotalBackTransferMoney() {
		return totalBackTransferMoney;
	}
	public void setTotalBackTransferMoney(Double totalBackTransferMoney) {
		this.totalBackTransferMoney = totalBackTransferMoney;
	}
	public Double getTotalCodePay() {
		return totalCodePay;
	}
	public void setTotalCodePay(Double totalCodePay) {
		this.totalCodePay = totalCodePay;
	}
	public Double getTotalGetCodePay() {
		return totalGetCodePay;
	}
	public void setTotalGetCodePay(Double totalGetCodePay) {
		this.totalGetCodePay = totalGetCodePay;
	}
	public Double getTotalQRCodePay() {
		return totalQRCodePay;
	}
	public void setTotalQRCodePay(Double totalQRCodePay) {
		this.totalQRCodePay = totalQRCodePay;
	}
	public Double getTotalGetQRCodePay() {
		return totalGetQRCodePay;
	}
	public void setTotalGetQRCodePay(Double totalGetQRCodePay) {
		this.totalGetQRCodePay = totalGetQRCodePay;
	}
	public Double getTotalVipRecharge() {
		return totalVipRecharge;
	}
	public void setTotalVipRecharge(Double totalVipRecharge) {
		this.totalVipRecharge = totalVipRecharge;
	}
	public Double getTotalVipRechargeProfit() {
		return totalVipRechargeProfit;
	}
	public void setTotalVipRechargeProfit(Double totalVipRechargeProfit) {
		this.totalVipRechargeProfit = totalVipRechargeProfit;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public long getStartDate() {
		return startDate;
	}
	public void setStartDate(long startDate) {
		this.startDate = startDate;
	}
	public long getEndDate() {
		return endDate;
	}
	public void setEndDate(long endDate) {
		this.endDate = endDate;
	}

	
	
	
}
