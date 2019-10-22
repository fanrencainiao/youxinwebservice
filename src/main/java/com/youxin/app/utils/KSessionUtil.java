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
import com.youxin.app.controller.TestController;
import com.youxin.app.entity.Config;
import com.youxin.app.entity.KSession;
import com.youxin.app.entity.User;
import com.youxin.app.utils.redis.RedisUtil;
import com.youxin.app.yx.UUIDUtil;

@Component
public final class KSessionUtil {
	private static Log log = LogFactory.getLog(KSessionUtil.class);

	private static RedisUtil redisUtil;

	@Autowired
	public KSessionUtil(RedisUtil redisUtil) {
		KSessionUtil.redisUtil = redisUtil;
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

	public static Map<String, Object> loginSaveAccessToken(Object userKey, Object userId, String accessToken) {
		HashMap<String, Object> data = new HashMap<String, Object>();
		try {
			
			int expire = KConstants.Expire.DAY7 * 5;
			String atKey = String.format(GET_ACCESS_TOKEN_BY_USER_ID, userKey);
			if (StringUtils.isBlank(accessToken))
//				accessToken = redisUtil.getKey(atKey);
//			if (StringUtils.isBlank(accessToken))
				accessToken = UUIDUtil.getUUID();
			removeAccessToken(userKey);
			redisUtil.saveTimeKey(atKey, accessToken, expire);
			String userIdKey = String.format(GET_USERID_BYTOKEN, accessToken);
			redisUtil.saveTimeKey(userIdKey, String.valueOf(userId), expire);

			data.put("access_token", accessToken);
			data.put("expires_in", expire);
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
		String access_token = redisUtil.getKey(key);

		if (!StringUtils.isBlank(access_token)) {
			redisUtil.removeKey(key);
		}
		if (!StringUtils.isBlank(access_token)) {
			String userIdKey = String.format(GET_USERID_BYTOKEN, access_token);
			redisUtil.removeKey(userIdKey);
		}
	}

	public static void removeToken(Object token) {
		Object userId = null;
		if (!StringUtils.isBlank(token.toString())) {
			String userIdKey = String.format(GET_USERID_BYTOKEN, token);
			userId = redisUtil.getKey(userIdKey);
			redisUtil.removeKey(userId.toString());
		}

		// 根据userKey拿token
		String key = String.format(GET_ACCESS_TOKEN_BY_USER_ID, userId);
		String access_token =redisUtil.getKey(key);
		if (!StringUtils.isBlank(access_token)) {
			redisUtil.removeKey(key);
		}

	}

	public static KSession getSession(String access_token) {
		String key = String.format(GET_SESSION_BY_ACCESS_TOKEN, access_token);
		if (StringUtils.isBlank(access_token))
			return null;
		String value = redisUtil.getKey(key);
		return StringUtils.isBlank(value) ? null : JSON.parseObject(value, KSession.class);
	}

	public static void saveSession(String access_token, KSession kSession) {
		String key = String.format(GET_SESSION_BY_ACCESS_TOKEN, access_token);
		String value = kSession.toString();
		redisUtil.saveString(key, value);
	}

	public static void setAccessToken(String access_token, KSession kSession) {
		String key = String.format(GET_SESSION_BY_ACCESS_TOKEN, access_token);
		String value = kSession.toString();
		redisUtil.saveString(key, value);
		// pipe.expire(key, KConstants.Expire.DAY7);

		key = String.format(GET_ACCESS_TOKEN_BY_USER_ID, kSession.getUserId());
		value = access_token;
		redisUtil.saveString(key, value);
		// pipe.expire(key, KConstants.Expire.DAY7);

	}

	public static String getAccess_token(Object userId) {
		String key = String.format(GET_ACCESS_TOKEN_BY_USER_ID, userId);
		return redisUtil.getKey(key);
	}

	public static String getUserIdBytoken(String token) {
		String key = String.format(GET_USERID_BYTOKEN, token);
		return redisUtil.getKey(key);
	}

	/**
	 * User
	 * 
	 * @param userId
	 * @return
	 */
	public static User getUserByUserId(Object userId) {
		String key = String.format(GET_USER_BY_USERID, userId);
		String value = redisUtil.getKey(key);
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
		user.setPassword("");
		redisUtil.saveTimeKey(key, user.toString(), KConstants.Expire.DAY1);

	}

	public static void deleteUserByUserId(Integer userId) {
		String key = String.format(GET_USER_BY_USERID, userId);
		redisUtil.removeKey(key);
	}

	public static void setConfig(Config config) {
		redisUtil.saveString(GET_CONFIG, config.toString());
	}
	public static Config getConfig() {
		String config=redisUtil.getKey(GET_CONFIG);
		return StringUtil.isEmpty(config) ? null : JSON.parseObject(config, Config.class);
	}

	
	public static final String GET_ADDRESS_BYIP="clientIp:%s";
	public static String getAddressByIp(String ip){
		String key = String.format(GET_ADDRESS_BYIP, ip);
		return redisUtil.getKey(key);
	}
	public static void setAddressByIp(String ip,String address){
		String key = String.format(GET_ADDRESS_BYIP, ip);
		redisUtil.saveTimeKey(key, address,KConstants.Expire.HOUR12);
		
	}
	
}
