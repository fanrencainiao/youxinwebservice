package com.youxin.app.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.youxin.app.entity.Config;
import com.youxin.app.entity.KSession;
import com.youxin.app.entity.User;
import com.youxin.app.entity.User.DeviceInfo;
import com.youxin.app.utils.jedis.RedisCRUD;
import com.youxin.app.yx.UUIDUtil;



@Component
public final class KSessionUtil {
	private static Log log = LogFactory.getLog(KSessionUtil.class);

//	private static RedisUtil redisUtil;

//	@Autowired
//	public KSessionUtil(RedisUtil redisUtil) {
//		KSessionUtil.redisUtil = redisUtil;
//	}
	private static RedisCRUD redisCRUD;
	@Autowired
	public KSessionUtil(RedisCRUD redisCRUD) {
		KSessionUtil.redisCRUD=redisCRUD;
	}
	/**
	 * 根据用户Id获取access_token
	 */
	// public static final String GET_USERID_BYTOKEN = "at_%1$s";
	public static final String GET_USERID_BYTOKEN = "loginToken:userId:%s";

	// public static final String GET_ACCESS_TOKEN_BY_USER_ID ="uk_%1$s";
	public static final String GET_ACCESS_TOKEN_BY_USER_ID = "loginToken:token:%s";

	/**
	 * 根据access_token获取Session
	 */
	public static final String GET_SESSION_BY_ACCESS_TOKEN = "login:%s:session";

	public static final String GET_USER_BY_USERID = "user:%s:data";

	public static final String GET_CONFIG = "app:config";

	public static final String GET_CLIENTCONFIG = "clientConfig";
	
	public static final String GET_LOGIN_KEY="login:upd:%s";

	public static Map<String, Object> loginSaveAccessToken(Object userKey, Object userId, String accessToken) {
		HashMap<String, Object> data = new HashMap<String, Object>();
		try {
			
			int expire = KConstants.Expire.DAY7 * 5;
			String atKey = String.format(GET_ACCESS_TOKEN_BY_USER_ID, userKey);
			if (StringUtils.isBlank(accessToken))
				accessToken = redisCRUD.get(atKey);
			if (StringUtils.isBlank(accessToken))
				accessToken = UUIDUtil.getUUID();
			try {
//				removeAccessToken(userKey);
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("redis链接失败");
			}
			System.out.println("atKey"+atKey);
			redisCRUD.setWithExpireTime(atKey, accessToken, expire);
			String userIdKey = String.format(GET_USERID_BYTOKEN, accessToken);
			System.out.println("userIdKey"+userIdKey);
			redisCRUD.setWithExpireTime(userIdKey, String.valueOf(userId), expire);

			data.put("access_token", accessToken);
			data.put("expires_in", expire);
			System.out.println("access_token=="+accessToken+"expires_in==="+expire);
			// data.put("userId", userId);
			// data.put("nickname", user.getNickname());

			return data;
		} catch (Exception e) {
			e.printStackTrace();
			return data;
		}
	}

	public static void removeAccessToken(Object userKey) {
		log.info("  removeAccessToken  =====  userKey  ======= :" + userKey);
		// 根据userKey拿token
		String key = String.format(GET_ACCESS_TOKEN_BY_USER_ID, userKey);
		String access_token = redisCRUD.get(key);

		if (!StringUtils.isBlank(access_token)) {
			redisCRUD.delete(key);
		}
		if (!StringUtils.isBlank(access_token)) {
			String userIdKey = String.format(GET_USERID_BYTOKEN, access_token);
			redisCRUD.del(userIdKey);
		}
	}

	public static void removeToken(Object token) {
		Object userId = null;
		if (!StringUtils.isBlank(token.toString())) {
			String userIdKey = String.format(GET_USERID_BYTOKEN, token);
			userId = redisCRUD.get(userIdKey);
			redisCRUD.del(userId.toString());
		}

		// 根据userKey拿token
		String key = String.format(GET_ACCESS_TOKEN_BY_USER_ID, userId);
		String access_token =redisCRUD.get(key);
		if (!StringUtils.isBlank(access_token)) {
			redisCRUD.delete(key);
		}

	}

	public static KSession getSession(String access_token) {
		String key = String.format(GET_SESSION_BY_ACCESS_TOKEN, access_token);
		if (StringUtils.isBlank(access_token))
			return null;
		String value = redisCRUD.get(key);
		return StringUtils.isBlank(value) ? null : JSON.parseObject(value, KSession.class);
	}

	public static void saveSession(String access_token, KSession kSession) {
		String key = String.format(GET_SESSION_BY_ACCESS_TOKEN, access_token);
		String value = kSession.toString();
		redisCRUD.set(key, value);
	}

	public static void setAccessToken(String access_token, KSession kSession) {
		String key = String.format(GET_SESSION_BY_ACCESS_TOKEN, access_token);
		String value = kSession.toString();
		redisCRUD.set(key, value);
		// pipe.expire(key, KConstants.Expire.DAY7);

		key = String.format(GET_ACCESS_TOKEN_BY_USER_ID, kSession.getUserId());
		value = access_token;
		redisCRUD.set(key, value);
		// pipe.expire(key, KConstants.Expire.DAY7);

	}

	public static String getAccess_token(Object userId) {
		String key = String.format(GET_ACCESS_TOKEN_BY_USER_ID, userId);
		return redisCRUD.get(key);
	}

	public static String getUserIdBytoken(String token) {
		String key = String.format(GET_USERID_BYTOKEN, token);
		System.out.println("key:"+key);
		return redisCRUD.get(key);
	}

	/**
	 * User
	 * 
	 * @param userId
	 * @return
	 */
	public static User getUserByUserId(Object userId) {
		String key = String.format(GET_USER_BY_USERID, userId);
		String value = redisCRUD.get(key);
		User user = null;
		try {
			user = JSON.parseObject(value, User.class);
		} catch (JSONException e) {
			return null;
		}
		// return StringUtils.isBlank(value) ? null : JSON.parseObject(value,
		// User.class);
		return user;

	}

	public static void saveUserByUserId(Integer userId, User user) {
		String key = String.format(GET_USER_BY_USERID, userId);
		user.setEx("");
		user.setAccid("");
		user.setPassword("");
		if(StringUtil.isEmpty(user.getPayPassword())||"0".equals(user.getPayPassword())) 
			user.setPayPassword("0");
		else
			user.setPayPassword("1");
		user.setMobile(StringUtil.phoneEncryption(user.getMobile()));
		redisCRUD.setWithExpireTime(key, user.toString(), KConstants.Expire.DAY1);

	}

	public static void deleteUserByUserId(Integer userId) {
		String key = String.format(GET_USER_BY_USERID, userId);
		redisCRUD.del(key);
	}

	public static void setConfig(Config config) {
		redisCRUD.set(GET_CONFIG, config.toString());
	}
	public static Config getConfig() {
		String config=redisCRUD.get(GET_CONFIG);
		return StringUtil.isEmpty(config) ? null : JSON.parseObject(config, Config.class);
	}

	
	public static final String GET_ADDRESS_BYIP="clientIp:%s";
	public static String getAddressByIp(String ip){
		String key = String.format(GET_ADDRESS_BYIP, ip);
		return redisCRUD.get(key);
	}
	public static void setAddressByIp(String ip,String address){
		String key = String.format(GET_ADDRESS_BYIP, ip);
		redisCRUD.setWithExpireTime(key, address,KConstants.Expire.HOUR12);
		
	}
	
	/**
	* @Description: TODO(保存 android 设备 信息)
	* @param @param userId
	* @param @param info    参数
	 */
	public static void saveAndroidToken(Integer userId,DeviceInfo info){
		String key=String.format(GET_LOGIN_KEY, userId);
		redisCRUD.setWithExpireTime(key, info.toString(),KConstants.Expire.DAY7);
		
		
	}
	/**
	* @Description: TODO(获取 android 设备的 信息)
	* @param @param userId
	* @param @return    参数
	 */
	public static DeviceInfo getAndroidToken(Integer userId){
		String key=String.format(GET_LOGIN_KEY, userId);
		String value = redisCRUD.get(key);
		return StringUtil.isEmpty(value) ? null : JSON.parseObject(value, DeviceInfo.class);
		
	}
	public static void removeAndroidToken(Integer userId){
		String key=String.format(GET_LOGIN_KEY, userId);
		redisCRUD.del(key);
	}
	
}
