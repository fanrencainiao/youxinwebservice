package com.youxin.app.yx;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;

import com.alibaba.fastjson.JSONObject;
import com.youxin.app.entity.User;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.utils.StringUtil;

public class SDKService {
	protected static Log logger = LogFactory.getLog("sdk");
	private static String APPKEY = "faeb0ec0ce3871b699119420790c8789"; // AppKey
	private static String SECRET = "d53d9d0cd80b"; // AppSecret

	/**
	 * 网易云通信ID创建
	 * 
	 * @param accid
	 * @param name
	 * @param token
	 * @return
	 * @throws IOException
	 */
	public static JSONObject createUser(User user) {
		try {
			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/user/create.action";
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			params.add(new BasicNameValuePair("accid", user.getAccid()));
			params.add(new BasicNameValuePair("name", user.getName()));
			params.add(new BasicNameValuePair("props", user.getProps()));
			params.add(new BasicNameValuePair("icon", user.getIcon()));
			params.add(new BasicNameValuePair("token", user.getToken()));
			params.add(new BasicNameValuePair("sign", user.getSign()));
			params.add(new BasicNameValuePair("email", user.getEmail()));
			params.add(new BasicNameValuePair("birth", user.getBirth()));
			params.add(new BasicNameValuePair("mobile", user.getMobile()));
			params.add(new BasicNameValuePair("gender", user.getGender() + ""));
			params.add(new BasicNameValuePair("ex", user.getEx()));

			// UTF-8编码,解决中文问题
			HttpEntity entity;

			entity = new UrlEncodedFormEntity(params, "UTF-8");

			String res = NIMPost.postNIMServer(url, entity, APPKEY, SECRET);
			json = getJson(json, res);
			logger.debug("createUser httpRes: {}" + res);
			return json;
		} catch (UnsupportedEncodingException e) {
			throw new ServiceException(0, "sdk编码异常");
		} catch (IOException e) {
			throw new ServiceException(0, "sdkio异常");
		}
	}
	/**
	 * 网易云通信ID更新
	 * @param accid 1 网易云通信ID，最大长度32字符，必须保证一个APP内唯一 
	 * @param props 0 json属性，第三方可选填，最大长度1024字符
	 * @param token 0 网易云通信ID可以指定登录token值，最大长度128字符

	 * @return
	 */
	public static JSONObject update(String accid,String props,String token) {
		try {
			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/user/create.action";
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			params.add(new BasicNameValuePair("accid", accid));
	
			params.add(new BasicNameValuePair("props", props));

			params.add(new BasicNameValuePair("token", token));
		

			// UTF-8编码,解决中文问题
			HttpEntity entity;

			entity = new UrlEncodedFormEntity(params, "UTF-8");

			String res = NIMPost.postNIMServer(url, entity, APPKEY, SECRET);
			json = getJson(json, res);
			logger.debug("createUser httpRes: {}" + res);
			return json;
		} catch (UnsupportedEncodingException e) {
			throw new ServiceException(0, "sdk编码异常");
		} catch (IOException e) {
			throw new ServiceException(0, "sdkio异常");
		}
	}

	/**
	 * 获取用户信息
	 * 
	 * @param accids
	 * @return
	 * @throws IOException
	 */
	public static JSONObject getUinfos(String accids) {
		try {
			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/user/getUinfos.action";
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("accids", accids));
			// UTF-8编码,解决中文问题
			HttpEntity entity = new UrlEncodedFormEntity(params, "UTF-8");

			String res = NIMPost.postNIMServer(url, entity, APPKEY, SECRET);
			json = getJson(json, res);
			logger.debug("getUinfos httpRes:" + res);
			return json;
		} catch (UnsupportedEncodingException e) {
			throw new ServiceException(0, "sdk编码异常");
		} catch (IOException e) {
			throw new ServiceException(0, "sdkio异常");
		}
	}
	
	/**
	 * 更新用户信息
	 * 
	 * @param accids
	 * @return
	 * @throws IOException
	 */
	public static JSONObject updateUinfo(User user) {
		try {
			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/user/updateUinfo.action";
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("accids", user.getAccid()));
			params.add(new BasicNameValuePair("name", user.getName()));
			params.add(new BasicNameValuePair("icon", user.getIcon()));
			params.add(new BasicNameValuePair("sign", user.getSign()));
			params.add(new BasicNameValuePair("email", user.getEmail()));
			params.add(new BasicNameValuePair("birth", user.getBirth()));
			params.add(new BasicNameValuePair("mobile", user.getMobile()));
			params.add(new BasicNameValuePair("gender", user.getGender()+""));
			params.add(new BasicNameValuePair("ex", user.getEx()));
			// UTF-8编码,解决中文问题
			HttpEntity entity = new UrlEncodedFormEntity(params, "UTF-8");

			String res = NIMPost.postNIMServer(url, entity, APPKEY, SECRET);
			json = getJson(json, res);
			logger.debug("getUinfos httpRes:" + res);
			return json;
		} catch (UnsupportedEncodingException e) {
			throw new ServiceException(0, "sdk编码异常");
		} catch (IOException e) {
			throw new ServiceException(0, "sdkio异常");
		}
	}
	
	

	/**
	 * 转换json
	 * 
	 * @param json
	 * @param res
	 * @return
	 */
	private static JSONObject getJson(JSONObject json, String res) {
		if (!StringUtil.isEmpty(res)) {
			json = JSONObject.parseObject(res);
			if (json.getIntValue("code") == 200) {
					json = json.getJSONObject("info");
			} else {
				throw new ServiceException(0, json.getString("desc"));
			}
		} else {
			throw new ServiceException(0, "sdk异常");
		}
		return json;
	}
	public static void main(String[] args) {
		JSONObject json=new JSONObject();
		System.out.println(json.get("info"));
	}

}
