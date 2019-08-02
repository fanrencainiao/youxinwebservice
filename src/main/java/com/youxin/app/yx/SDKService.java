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
import com.youxin.app.entity.Friends;
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
	 * 网易云通信ID基本信息更新
	 * @param accid 1 网易云通信ID，最大长度32字符，必须保证一个APP内唯一 
	 * @param props 0 json属性，第三方可选填，最大长度1024字符
	 * @param token 0 网易云通信ID可以指定登录token值，最大长度128字符

	 * @return
	 */
	public static JSONObject update(String accid,String props,String token) {
		try {
			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/user/update.action";
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
	 * 更新并获取新token
	 * @param accid
	 * @return
	 */
	public static JSONObject refreshToken(String accid) {
		try {
			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/user/refreshToken.action";
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			params.add(new BasicNameValuePair("accid", accid));

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
	 * 封禁网易云通信ID
	 * @param accid
	 * @param needkick 是否踢掉被禁用户，true或false，默认false
	 * @return
	 */
	public static JSONObject block(String accid,String needkick) {
		try {
			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/user/block.action";
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			params.add(new BasicNameValuePair("accid", accid));
			params.add(new BasicNameValuePair("needkick", needkick));
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
	 * 解禁网易云通信ID
	 * @param accid
	 * @return
	 */
	public static JSONObject unblock(String accid) {
		try {
			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/user/unblock.action";
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			params.add(new BasicNameValuePair("accid", accid));
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
	 * 获取用户名片
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
	 * 更新用户名片
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
	 * 账号全局禁言
	 * @param accid
	 * @param mute true：全局禁言，false:取消全局禁言
	 * @return
	 */
	public static JSONObject mute(String accid,String mute) {
		try {
			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/user/mute.action";
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("accid", accid));
			params.add(new BasicNameValuePair("mute", mute));
			
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
	 * 账号全局禁用音视频
	 * @param accid
	 * @param mute true：全局禁言，false:取消全局禁言
	 * @return
	 */
	public static JSONObject muteAv(String accid,String mute) {
		try {
			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/user/muteAv.action";
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("accid", accid));
			params.add(new BasicNameValuePair("mute", mute));
			
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
	 * 加好友
	 * @param friends 
	 *  accid	String	是	加好友发起者accid
	 *  faccid	String	是	加好友接收者accid
	 *	type	int	是	1直接加好友，2请求加好友，3同意加好友，4拒绝加好友
	 *	msg	String	否	加好友对应的请求消息，第三方组装，最长256字符
	 *	serverex	String	否	服务器端扩展字段，限制长度256
	 *	此字段client端只读，server端读写
	 * @return
	 */
	public static JSONObject friendAdd(Friends friends) {
		try {
			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/friend/add.action";
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("accid", friends.getAccid()));
			params.add(new BasicNameValuePair("faccid", friends.getFaccid()));
			params.add(new BasicNameValuePair("type", friends.getType()+""));
			params.add(new BasicNameValuePair("msg", friends.getMsg()));
			params.add(new BasicNameValuePair("serverex", friends.getServerex()));
			
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
	 * 更新好友相关信息
	 * @param friends
	 * accid	String	是	发起者accid
	 * faccid	String	是	要修改朋友的accid
	 * alias	String	否	给好友增加备注名，限制长度128，可设置为空字符串
	 * ex		String	否	修改ex字段，限制长度256，可设置为空字符串
	 * serverex	String	否	修改serverex字段，限制长度256，可设置为空字符串
	 *	此字段client端只读，server端读写
	 * @return
	 */
	public static JSONObject friendUpdate(Friends friends) {
		try {
			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/friend/update.action";
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("accid", friends.getAccid()));
			params.add(new BasicNameValuePair("faccid", friends.getFaccid()));
			params.add(new BasicNameValuePair("alias", friends.getAlias()));
			params.add(new BasicNameValuePair("ex", friends.getEx()));
			params.add(new BasicNameValuePair("serverex", friends.getServerex()));
			
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
	 *   删除好友关系
	 * @param accid 发起者accid
	 * @param faccid 要删除朋友的accid
	 * @param isDeleteAlias 默认false:不需要，true:需要
	 * @return
	 */
	public static JSONObject friendDelete(String accid,String faccid,String isDeleteAlias) {
		try {
			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/friend/delete.action";
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("accid", accid));
			params.add(new BasicNameValuePair("faccid", faccid));
			params.add(new BasicNameValuePair("isDeleteAlias", isDeleteAlias));
			
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
	 * 获取好友关系
	 * 查询某时间点起到现在有更新的双向好友
	 *	accid	String	是	发起者accid
	 *	updatetime	Long	是	更新时间戳，接口返回该时间戳之后有更新的好友列表
	 *	createtime	Long	否	【Deprecated】定义同updatetime
	 * @return
	 */
	public static JSONObject friendGet(String accid,Long updatetime,Long createtime) {
		try {
			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/friend/get.action";
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("accid", accid));
			params.add(new BasicNameValuePair("updatetime", updatetime+""));
			params.add(new BasicNameValuePair("createtime", createtime+""));
			
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
	 * 设置黑名单/静音
	 * 拉黑/取消拉黑；设置静音/取消静音
	 * @param accid String	是	用户帐号，最大长度32字符，必须保证一个 APP内唯一
	 * @param targetAcc String	是	被加黑或加静音的帐号
	 * @param relationType int	是	本次操作的关系类型,1:黑名单操作，2:静音列表操作
	 * @param value int	是	操作值，0:取消黑名单或静音，1:加入黑名单或静音
	 * @return
	 */
	public static JSONObject setSpecialRelation(String accid,String targetAcc,String relationType,String value) {
		try {
			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/user/setSpecialRelation.action";
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("accid", accid));
			params.add(new BasicNameValuePair("targetAcc", targetAcc));
			params.add(new BasicNameValuePair("relationType", relationType));
			params.add(new BasicNameValuePair("value", value));
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
	 * 查看用户的黑名单和静音列表
	 * @param accid
	 * @return
	 */
	public static JSONObject listBlackAndMuteList(String accid) {
		try {
			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/user/listBlackAndMuteList.action";
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("accid", accid));
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
			if (json.getIntValue("code") != 200) 
				throw new ServiceException(0, json.getString("desc"));
		} else 
			throw new ServiceException(0, "sdk异常");
		
		return json;
	}
	public static void main(String[] args) {
		JSONObject json=new JSONObject();
		System.out.println(json.get("info"));
	}

}
