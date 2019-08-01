package com.youxin.app.utils;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;


/**
 * 各种 加密 权限验证的类
 * @author lidaye
 *
 */
public class AuthServiceUtils {
	
	@Value("youxin.apiKey")
	private static String apiKey;
	
	@Value("youxin.isAuth")
	private static int isAuth;
	
	/**
	 * 检验接口请求时间
	 * @param time
	 * @return
	 */
	public static boolean authRequestTime(long time) {
		long currTime=DateUtil.currentTimeSeconds();
		//允许 5分钟时差
		if(((currTime-time)<600&&(currTime-time)>-600)) {
			return true;
		}else {
			System.out.println(String.format("====> authRequestTime error server > %s client %s", currTime,time));
			return false;
		}
	}
	
	
	/**
	 * 检验 开放的 不需要 token 的接口
	 * @param time
	 * @return
	 */
	public static boolean authOpenApiSecret(long time,String secret) {
		/**
		 * 判断  系统配置是否要校验
		 */
		if(0==isAuth) {
			return true;
		}
		if(!authRequestTime(time)) {
			return false;
		}
		if(StringUtils.isEmpty(secret)) {
			return false;
		}
	
		String key =new StringBuffer()
					.append(apiKey)
					.append(time).toString();
		
		return secret.equals(Md5Util.md5Hex(key));
		
	}
	
	/**
	 * 普通接口授权
	 * @param userId
	 * @param time
	 * @param token
	 * @param secret
	 * @return
	 */
	public static boolean authRequestApi(Integer userId,long time,String token,String secret,String url) {
		if(KConstants.filterSet.contains(url)) {
			return true;
		}
		
		/**
		 * 判断  系统配置是否要校验
		 */
		if(0==isAuth) {
			return true;
		}
		if(!authRequestTime(time)) {
			return false;
		}
		if(StringUtils.isEmpty(secret)) {
			return false;
		}
		String secretKey=getRequestApiSecret(userId, time, token);
		
		if(!secretKey.equals(secret)) {
			return false;
		}else {
			return true;
		}
		
	}
	
	public static String getRequestApiSecret(Integer userId,long time,String token) {
		
		/**
		 * 密钥 
			md5(apikey+time+userid+token) 
		 */
		
		
		/**
		 *  apikey+time+userid+token
		 */
		String key =new StringBuffer()
					.append(apiKey)
					.append(time)
					.append(userId)
					.append(token).toString();
		
		return Md5Util.md5Hex(key);
		
	}
	/**
	 * 发送短信验证码 授权
	 * @param userId
	 * @param time
	 * @param token
	 * @return
	 */
	public static boolean authSendTelMsgSecret(String userId,long time,String secret) {
		
		/**
		 * 密钥 
			md5(apikey+time+userid+token) 
		 */
		
		
		/**
		 *  apikey+time+userid+token
		 */
		String key =new StringBuffer()
					.append(apiKey)
					.append(time)
					.append(userId).toString();
		
		return secret.equals(Md5Util.md5Hex(key));
		
	}
	

	

	/** @Description:（应用授权的 加密 认证方法） 
	* @param appId
	* @param userId
	* @param appSecret
	* @param token
	* @param time
	* @param secret
	* @return
	**/ 
	public static boolean getAppAuthorization(String appId,String appSecret,long time,String secret) {
		boolean flag = false;
		if(!authRequestTime(time)) {
			return flag;
		}
		if(StringUtils.isEmpty(appId)) {
			return flag;
		}
		if(StringUtils.isEmpty(appSecret)){
			return flag;
		}
		String secretKey = getAppAuthorizationSecret(appId, time, appSecret);
		if(!secretKey.equals(secret)) {
			return flag;
		}else {
			return !flag;
		}
	}
	
	public static boolean getAuthInterface(String appId,String userId,String token,long time,String appSecret,String secret){
		boolean flag=false;
		if(!authRequestTime(time)) {
			return flag;
		}
		if(StringUtils.isEmpty(appId)) {
			return flag;
		}
		if(StringUtils.isEmpty(appSecret)){
			return flag;
		}
		String secretKey = getAuthInterfaceSecret(appId, userId, token, time, appSecret);
		if(!secretKey.equals(secret)) {
			return flag;
		}else {
			return !flag;
		}
	}
	
	public static String getAppAuthorizationSecret(String appId,long time,String appSecret){
		// secret=md5(appId+md5(time)+md5(appSecret))	
		/**
		 * md5(time)
		 */
		String times = new StringBuffer()
				.append(time).toString();
		String md5Time = Md5Util.md5Hex(times);
		
		/**
		 * appId+md5(time)
		 */
		String AppIdMd5time = new StringBuffer()
				.append(appId)
				.append(md5Time).toString();
		
		/**
		 * appId+md5(time)+md5(appSecret)
		 */
		String md5AppSecret = Md5Util.md5Hex(appSecret);
		
		String secret=new StringBuffer()
				.append(AppIdMd5time)
				.append(md5AppSecret).toString();
		
		
		String key = Md5Util.md5Hex(secret);
		
		return key;
	}
	
	public static String getAuthInterfaceSecret(String appId,String userId,String token,long time,String appSecret){
		// secret=md5(apikey+appId+userid+md5(token+time)+md5(appSecret))
		
		/**
		 * md5(appSecret)
		 */
		String md5AppSecret=Md5Util.md5Hex(appSecret);
		
		/**
		 * md5(token+time)
		 */
		
		String tokenTime=new StringBuffer()
				.append(token)
				.append(time).toString();
		String md5TokenTime=Md5Util.md5Hex(tokenTime);
		
		/**
		 * apikey+appId+userId
		 */
		
		String apiKeyAppIdUserId=new StringBuffer()
				.append(apiKey)
				.append(appId)
				.append(userId).toString();
		
		String secret=new StringBuffer()
				.append(apiKeyAppIdUserId)
				.append(md5TokenTime)
				.append(md5AppSecret).toString();
		
		String key=Md5Util.md5Hex(secret);
		return key;
	}
	

}
