package com.youxin.app.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * 常量
 * 
 */
public class KConstants {
	
	public static boolean isDebug=true;
	
	public static final String PAGE_INDEX = "0";
	public static final String PAGE_SIZE = "15";
	
	public static final int MOENY_ADD = 1; //金钱增加
	public static final int MOENY_REDUCE = 2; //金钱减少
	public static final double LBS_KM=111.01;
	public static final int LBS_DISTANCE=50;
	
	// 不经过普通接口校验
	public static final Set<String> filterSet=new HashSet<String>(){{
		
		add("/redPacket/sendRedPacket");// 发送红包
		
		add("/redPacket/sendRedPacket/v1");// 发送红包新版
		
		add("/redPacket/openRedPacket");// 打开红包
		
		add("/user/recharge/getSign");// 充值
		
		add("/transfer/wx/pay");// 企业向个人支付转账
		
		add("/alipay/transfer");// 支付宝提现
		
		add("/skTransfer/sendTransfer");// 系统转账
		
		add("/skTransfer/receiveTransfer");// 接受转账
		
		add("/pay/codePayment");// 付款码支付
		
		add("/pay/codeReceipt");// 二维码收款
		
	}};

	public interface Expire {
		static final int DAY1 = 86400;
		static final int DAY7 = 604800;
		static final int HOUR12 = 43200;
		static final int HOUR=3600;
	}
	//订单状态
	public interface OrderStatus {
		public static final int CREATE = 0;// 创建
		public static final int END = 1;// 成功
		public static final int DELETE = -1;// 删除
	}
	// 消费类型 
	public interface ConsumeType {
		public static final int USER_RECHARGE = 1;// 用户充值
		public static final int PUT_RAISE_CASH = 2;// 用户提现
		public static final int SYSTEM_RECHARGE = 3;// 后台充值
		public static final int SEND_REDPACKET = 4;// 发红包
		public static final int RECEIVE_REDPACKET = 5;// 领取红包
		public static final int REFUND_REDPACKET = 6;// 红包退款
		public static final int SEND_TRANSFER = 7;// 转账
		public static final int RECEIVE_TRANSFER = 8;// 接受转账
		public static final int REFUND_TRANSFER = 9;// 转账退回
		public static final int SEND_PAYMENTCODE = 10;// 付款码付款
		public static final int RECEIVE_PAYMENTCODE = 11;// 付款码收款
		public static final int SEND_QRCODE = 12;// 二维码收款 付款方
		public static final int RECEIVE_QRCODE = 13;// 二维码收款 收款方
		public static final int VIP_RECHARGE = 14;// VIP充值
		public static final int VIP_COMMISSION = 15;// 推广用户VIP充值提成
		public static final int SYSTEM_REDUCE = 16;// 后台扣除（目前用于异常账号）
	}
	
	

//	public interface Result {
//		static final JSONMessage InternalException = new JSONMessage(1020101, "接口内部异常");
//		static final JSONMessage ParamsAuthFail = new JSONMessage(1010101, "请求参数验证失败，缺少必填参数或参数错误");
//		static final JSONMessage TokenEillegal = new JSONMessage(1030101, "缺少访问令牌");
//		static final JSONMessage TokenInvalid = new JSONMessage(1030102, "访问令牌过期或无效");
//		static final JSONMessage AUTH_FAILED = new JSONMessage(1030103, "权限验证失败");
//	}
	public interface ResultCode {
		
		//接口调用成功
		static final int Success = 1;
		
		//接口调用失败
		static final int Failure = 0;
		
		//请求参数验证失败，缺少必填参数或参数错误
		static final int ParamsAuthFail = 1010101;
		
		//缺少请求参数：
		static final int ParamsLack = 1010102;
		
		//接口内部异常
		static final int InternalException = 1020101;
		
		//链接已失效
		static final int Link_Expired = 1020102;
		
		//缺少访问令牌
		static final int TokenEillegal = 1030101;
		
		//访问令牌过期或无效
		static final int TokenInvalid = 1030102;
		
		//权限验证失败
		static final int AUTH_FAILED = 1030103;
		
		//帐号不存在
		static final int AccountNotExist = 1040101;
		
		//帐号或密码错误
		static final int AccountOrPasswordIncorrect = 1040102;
		
		//原密码错误
		static final int OldPasswordIsWrong = 1040103;
		
		//短信验证码错误或已过期
		static final int VerifyCodeErrOrExpired = 1040104;
		
		//发送验证码失败,请重发!
		static final int SedMsgFail = 1040105;
		
		//请不要频繁请求短信验证码，等待{0}秒后再次请求
		static final int ManySedMsg = 1040106;
		
		//手机号码已注册!
		static final int PhoneRegistered = 1040107;
		
		//余额不足
		static final int InsufficientBalance = 1040201;
		
	
		//请输入图形验证码
		static final int NullImgCode=1040215;
		
		//图形验证码错误
		static final int ImgCodeError=1040216;

		//没有选择支付方式!
		static final int NotSelectPayType = 1040301;
		
		//支付宝支付后回调出错：
		static final int AliPayCallBack_FAILED = 1040302;
		
		//你没有权限删除!
		static final int NotPermissionDelete = 1040303;
		
		//账号被锁定
		static final int ACCOUNT_IS_LOCKED = 1040304;
		
		// 第三方登录未绑定手机号码
		static final int UNBindingTelephone = 1040305;
		
		// 第三方登录提示账号不存在
		static final int SdkLoginNotExist = 1040306;
		
	}

		//支付方式
		public interface PayType {
			public static final int ALIPAY = 1;// 支付宝支付
			public static final int WXPAY = 2;// 微信支付
			public static final int BALANCEAY = 3;// 余额支付
			public static final int SYSTEMPAY = 4;// 系统支付
			public static final int IOSINBUY = 5;// ios内购
		}
	
	public interface ResultMsgs {
		
		static final Map<String,String> InternalException =new HashMap<String,String>(){
			{
				put("zh", "接口内部异常");
				put("en", "An exception occurs to internal interface.");
			}
		};
		static final Map<String,String> ParamsAuthFail =new HashMap<String,String>(){
			{
				put("zh", "请求参数验证失败，缺少必填参数或参数错误");
				put("en", "Request for parameter verification failed due to lack of required parameters or wrong parameters");
			}
		};
		static final Map<String,String> TokenEillegal =new HashMap<String,String>(){
			{
				put("zh", "缺少访问令牌");
				put("en", "Lack of access token");
			}
		};
		static final Map<String,String> TokenInvalid =new HashMap<String,String>(){
			{
				put("zh", "访问令牌过期或无效");
				put("en", "Access token gets expired or invalid");
			}
		};
		static final Map<String,String> AUTH_FAILED =new HashMap<String,String>(){
			{
				put("zh", "权限验证失败");
				put("en", "Permission verification failed");
			}
		};
		
		static final Map<String,String> AliPayCallBack_FAILED =new HashMap<String,String>(){
			{
				put("zh", "支付宝支付后回调出错：");
				put("en", "Retracement error occurs after payment through Alipay");
			}
		};
		
		static final Map<String,String> AccountNotExist =new HashMap<String,String>(){
			{
				put("zh", "帐号不存在");
				put("en", "The account isn't existed.");
			}
		};
		static final Map<String,String> AccountOrPasswordIncorrect =new HashMap<String,String>(){
			{
				put("zh", "帐号或密码错误");
				put("en", "The account or password is wrong");
			}
		};
		static final Map<String,String> OldPasswordIsWrong =new HashMap<String,String>(){
			{
				put("zh", "原密码错误");
				put("en", "The original password is wrong");
			}
		};
		static final Map<String,String> VerifyCodeErrOrExpired =new HashMap<String,String>(){
			{
				put("zh", "短信验证码错误或已过期");
				put("en", "The verification code is wrong or expired");
			}
		};
		static final Map<String,String> InsufficientBalance =new HashMap<String,String>(){
			{
				put("zh", "余额不足");
				put("en", "Insufficient balance");
			}
		};
		
	}

	public interface ErrCodes {
		static final String InternalException ="InternalException";
		static final String ParamsAuthFail ="ParamsAuthFail";
		static final String TokenEillegal ="TokenEillegal";
		static final String TokenInvalid ="TokenInvalid";
		static final String AUTH_FAILED ="AUTH_FAILED";
		static final String PublishVerify_FAILED ="PublishVerify_FAILED";
		static final String AliPayCallBack_FAILED="AliPayCallBack_FAILED";
		static final String NotExistSendResume_FAILED="NotExistSendResume_FAILED";
		static final String NotCreateResume="NotCreateResume";
		static final String NotSelectPayType="NotSelectPayType";
		static final String PhoneRegistered="PhoneRegistered";
		static final String AccountNotExist="AccountNotExist";
		static final String AccountOrPasswordIncorrect="AccountOrPasswordIncorrect";
		static final String OldPasswordIsWrong="OldPasswordIsWrong";
		static final String VerifyCodeErrOrExpired="VerifyCodeErrOrExpired";
		static final String InsufficientBalance="InsufficientBalance";
		static final String OpenTalkResumeNotDetailed="OpenTalkResumeNotDetailed";
		static final String Resume_ConditionNotSatisfied="Resume_ConditionNotSatisfied";
		static final String NotTalk_Oneself="NotTalk_Oneself";	
	}
	
	
	
	
}
