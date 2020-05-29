package com.youxin.app.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mongodb.morphia.annotations.Entity;

import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.NotSaved;

import com.alibaba.fastjson.JSON;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity(value = "Config", noClassnameStored = true)
public class Config {

	private @Id long id = 10000;

	@NotSaved
	private HelpCenter add = new HelpCenter();

	/**
	 * 开启swagger 0否，1是
	 */
	private int isOpenSwagger;
	/**
	 * 开启权限验证 0否，1是
	 */
	private int isAuthApi;
	/**
	 * 自动导入通讯录 0否，1是
	 */
	private int isAutoAddressBook;
	/**
	 * 接口地址
	 */
	private String apiUrl;
	/**
	 * 云信key
	 */
	private String yunxinappkey;
	/**
	 * 注册通知语
	 */
	private String regNotice = "欢迎来到友信app";
	/**
	 * 注册通知语
	 */
	private String configChangeNotice = "账户相关系统维护";
	/**
	 * 用户银行卡提现是否短信通知0不通知，1通知
	 */
	private int bankMsgState = 0;
	/**
	 * 发送手机号
	 */
	private String sendPhone;

	/**
	 * 短信类型
	 */
	private String SMSType = "aliyun";

	/**
	 * ios下载地址
	 */
	private String iosDownUrl;
	private String iosAppUrl;
	/**
	 * android下载地址
	 */
	private String androidDownUrl;

	/**
	 * ios版本号
	 */
	private String iosVersion;
	/**
	 * ios禁用版本号 多个版本，隔开
	 */
	private String iosVersionDisable;
	/**
	 * ios审核版本号 多个版本，隔开
	 */
	private String iosVersionVerify;
	/**
	 * android版本号
	 */
	private String androidVersion;
	/**
	 * android需要更新的版本号
	 */
	private String androidGoVersion;

	/**
	 * android禁用版本号 多个版本，隔开
	 */
	private String androidVersionDisable;

	/**
	 * ios更新说明
	 */
	private String iosExplain;
	/**
	 * android更新说明
	 */
	private String androidExplain;

	/**
	 * 请求地址
	 */
	private String ipAddress;

	/**
	 * 请求区域
	 */
	private String area;

	private double afee = 0; // 手续费固定金额

	private double bfee = 0; // 手续费百分比
	
	private double todayLimitMoney=20000;//当日限额
	private double nowlimitMoney=10000;//单次限额
	private int todayLimitNumber=3;//当日提现次数
	/* ========钱包相关=========== */
	private int aliState = 0;// 阿里功能 0正常，1维护，2关闭
	private int wxState = 0;// 微信功能 0正常，1维护，2关闭
	private int yeeState = 0;// 易宝功能 0正常，1维护，2关闭
	private int redPacketState = 0;// 零钱红包功能 0正常，1维护，2关闭
	private int aliRedPacketState = 0;// 支付宝红包功能 0正常，1维护，2关闭
	private int transferState = 0;// 转账功能 0正常，1维护，2关闭
	private int bankState = 0;// 提现功能 0正常，1维护，2关闭
	private int rrShopState = 0;// 人人商城购物 0正常，1维护，2关闭
	private int codeReceiveState = 0;// 零钱二维码收款功能 0正常，1维护，2关闭
	private int moneyState = 0;// 零钱显示 0正常，2关闭
	private int aliCodeState = 0;// 支付宝码显示 0正常，2关闭
	
	
	public String getSendConfigChangeNotice() {
//		String msg=this.configChangeNotice;
		String msg=this.configChangeNotice;
		
		return msg;
	}
	
	public Map<String, Object> getMoneyConfig(Config config) {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("aliState", config.getAliState());
		result.put("wxState", config.getWxState());
		result.put("yeeState", config.getYeeState());
		result.put("redPacketState", config.getRedPacketState());
		result.put("aliRedPacketState", config.getAliRedPacketState());
		result.put("transferState", config.getTransferState());
		result.put("codeReceiveState", config.getCodeReceiveState());
		result.put("bankState", config.getBankState());
		result.put("rrShopState", config.getRrShopState());
		result.put("moneyState", config.getMoneyState());
		result.put("aliCodeState", config.getAliCodeState());
		return result;
	}

	/**
	 * 未登录能获取的config信息
	 * 
	 * @param config
	 */
	public Config() {
	}

	public Config(Config config) {
		super();
		this.apiUrl = config.getApiUrl();
		this.androidDownUrl = config.getAndroidDownUrl();
		this.androidExplain = config.getAndroidExplain();
		this.androidVersion = config.getAndroidVersion();
		this.androidVersionDisable = config.getAndroidVersionDisable();
		this.androidGoVersion = config.getAndroidGoVersion();
//		this.area=config.getArea();
		this.iosDownUrl = config.getIosDownUrl();
		this.iosExplain = config.getIosExplain();
		this.iosVersion = config.getIosVersion();
		this.iosVersionDisable = config.getIosVersionDisable();
		this.iosVersionVerify = config.getIosVersionVerify();
		this.iosAppUrl = config.getIosDownUrl();
//		this.ipAddress=config.getIpAddress();
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
