package com.youxin.app.yx;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
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
import com.youxin.app.yx.request.Friends;
import com.youxin.app.yx.request.Msg;
import com.youxin.app.yx.request.MsgFile;
import com.youxin.app.yx.request.MsgRequest;
import com.youxin.app.yx.request.chatroom.ChatroomAddRobotRequest;
import com.youxin.app.yx.request.chatroom.ChatroomCreateRequest;
import com.youxin.app.yx.request.chatroom.ChatroomGetRequest;
import com.youxin.app.yx.request.chatroom.ChatroomQueueDrop;
import com.youxin.app.yx.request.chatroom.ChatroomQueueInit;
import com.youxin.app.yx.request.chatroom.ChatroomQueueListRequest;
import com.youxin.app.yx.request.chatroom.ChatroomQueueOfferRequest;
import com.youxin.app.yx.request.chatroom.ChatroomQueuePollRequest;
import com.youxin.app.yx.request.chatroom.ChatroomRemoveRobotRequest;
import com.youxin.app.yx.request.chatroom.ChatroomRequestAddrRequest;
import com.youxin.app.yx.request.chatroom.ChatroomSendMsgRequest;
import com.youxin.app.yx.request.chatroom.ChatroomSetMemberRoleRequest;
import com.youxin.app.yx.request.chatroom.ChatroomTemporaryMuteRequest;
import com.youxin.app.yx.request.chatroom.ChatroomToggleCloseStatRequest;
import com.youxin.app.yx.request.chatroom.ChatroomUpdateRequest;

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
	 * @return {"code":200,"info":{"token":"xx","accid":"xx","name":"xx"}}
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
	 * 
	 * @param accid 1 网易云通信ID，最大长度32字符，必须保证一个APP内唯一
	 * @param props 0 json属性，第三方可选填，最大长度1024字符
	 * @param token 0 网易云通信ID可以指定登录token值，最大长度128字符
	 * 
	 * @return {"code":200}
	 */
	public static JSONObject update(String accid, String props, String token) {
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
	 * 
	 * @param accid
	 * @return {"code":200,"info":{"token":"xxx","accid":"xx"}}
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
	 * 
	 * @param accid
	 * @param needkick 是否踢掉被禁用户，true或false，默认false
	 * @return {"code":200}
	 */
	public static JSONObject block(String accid, String needkick) {
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
	 * 
	 * @param accid
	 * @return {"code":200}
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
	 * @return {"code":200,"uinfos":[{"email":"t1@163.com","accid":"t1","name":"abc","gender":1,"mobile":"18645454545"},{"accid":"t2","name":"def","gender":0}]}
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
	 * @return {"code":200}
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
			params.add(new BasicNameValuePair("gender", user.getGender() + ""));
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
	 * 
	 * @param accid
	 * @param mute  true：全局禁言，false:取消全局禁言
	 * @return {"code":200}
	 */
	public static JSONObject mute(String accid, String mute) {
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
	 * 
	 * @param accid
	 * @param mute  true：全局禁言，false:取消全局禁言
	 * @return {"code":200}
	 */
	public static JSONObject muteAv(String accid, String mute) {
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
	 * 
	 * @param friends accid String 是 加好友发起者accid faccid String 是 加好友接收者accid type
	 *                int 是 1直接加好友，2请求加好友，3同意加好友，4拒绝加好友 msg String 否
	 *                加好友对应的请求消息，第三方组装，最长256字符 serverex String 否 服务器端扩展字段，限制长度256
	 *                此字段client端只读，server端读写
	 * @return {"code":200}
	 */
	public static JSONObject friendAdd(Friends friends) {
		try {
			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/friend/add.action";
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("accid", friends.getAccid()));
			params.add(new BasicNameValuePair("faccid", friends.getFaccid()));
			params.add(new BasicNameValuePair("type", friends.getType() + ""));
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
	 * 
	 * @param friends accid String 是 发起者accid faccid String 是 要修改朋友的accid alias
	 *                String 否 给好友增加备注名，限制长度128，可设置为空字符串 ex String 否
	 *                修改ex字段，限制长度256，可设置为空字符串 serverex String 否
	 *                修改serverex字段，限制长度256，可设置为空字符串 此字段client端只读，server端读写
	 * @return {"code":200}
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
	 * 删除好友关系
	 * 
	 * @param accid         发起者accid
	 * @param faccid        要删除朋友的accid
	 * @param isDeleteAlias 默认false:不需要，true:需要
	 * @return {"code":200}
	 */
	public static JSONObject friendDelete(String accid, String faccid, String isDeleteAlias) {
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
	 * 获取好友关系 查询某时间点起到现在有更新的双向好友 accid String 是 发起者accid updatetime Long 是
	 * 更新时间戳，接口返回该时间戳之后有更新的好友列表 createtime Long 否 【Deprecated】定义同updatetime
	 * 
	 * @return {"code":200,"size":2,"friends":[{"createtime":1440037706987,"bidirection":true,"faccid":"t2"},{"createtime":1440037718190,"bidirection":true,"faccid":"t3","alias":"t3"}]}
	 */
	public static JSONObject friendGet(String accid, Long updatetime, Long createtime) {
		try {
			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/friend/get.action";
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("accid", accid));
			params.add(new BasicNameValuePair("updatetime", updatetime + ""));
			params.add(new BasicNameValuePair("createtime", createtime + ""));

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
	 * 设置黑名单/静音 拉黑/取消拉黑；设置静音/取消静音
	 * 
	 * @param accid        String 是 用户帐号，最大长度32字符，必须保证一个 APP内唯一
	 * @param targetAcc    String 是 被加黑或加静音的帐号
	 * @param relationType int 是 本次操作的关系类型,1:黑名单操作，2:静音列表操作
	 * @param value        int 是 操作值，0:取消黑名单或静音，1:加入黑名单或静音
	 * @return {"code":200}
	 */
	public static JSONObject setSpecialRelation(String accid, String targetAcc, String relationType, String value) {
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
	 * 
	 * @param accid
	 * @return {"mutelist":[//被静音的帐号列表"abc","cde"],"blacklist":[//加黑的帐号列表"abc"],"code":200}
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
	 * 发送普通消息 给用户或者高级群发送普通消息，包括文本，图片，语音，视频和地理位置
	 *
	 * @return {"code":200,"data":{"msgid":1200510468189,"timetag":1545635366312,//消息发送的时间戳"antispam":false}}
	 */
	public static JSONObject sendMsg(MsgRequest msg) {
		try {
			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/msg/sendMsg.action";
			List<NameValuePair> params = reflect(msg);
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
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(0, e.getMessage());
		}
	}

	/**
	 * 批量发送点对点普通消息
	 * 
	 * 1.给用户发送点对点普通消息，包括文本，图片，语音，视频，地理位置和自定义消息。
	 * 2.最大限500人，只能针对个人,如果批量提供的帐号中有未注册的帐号，会提示并返回给用户。
	 * 3.此接口受频率控制，一个应用一分钟最多调用120次，超过会返回416状态码，并且被屏蔽一段时间； 具体消息参考下面描述。
	 * 
	 * @param msg
	 * @return {"code":200,"msgids":{"aaa":1234,"bbb":1235}//消息接受者对应的消息ID，returnMsgId参数为true时才返回"timetag":1545635366312,//消息发送的时间戳"unregister":"["a","b"...]"//未注册的帐号}
	 */
	public static JSONObject sendBatchMsg(Msg msg) {
		try {

			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/msg/sendBatchMsg.action";
			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("fromAccid", msg.getFrom()));
			params.add(new BasicNameValuePair("toAccids", msg.getTo()));
			params.add(new BasicNameValuePair("type", msg.getType() + ""));
			params.add(new BasicNameValuePair("body", msg.getBody()));
			params.add(new BasicNameValuePair("option", msg.getOption()));
			params.add(new BasicNameValuePair("pushcontent", msg.getPushcontent()));
			params.add(new BasicNameValuePair("payload", msg.getPayload()));
			params.add(new BasicNameValuePair("ext", msg.getExt()));
			params.add(new BasicNameValuePair("bid", msg.getBid()));
			params.add(new BasicNameValuePair("useYidun", msg.getUseYidun() + ""));
			params.add(new BasicNameValuePair("returnMsgid", msg.getReturnMsgid()));

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
	 * 发送自定义系统通知
	 * 
	 * 1.自定义系统通知区别于普通消息，方便开发者进行业务逻辑的通知； 2.目前支持两种类型：点对点类型和群类型（仅限高级群），根据msgType有所区别。
	 *
	 * 应用场景：如某个用户给另一个用户发送好友请求信息等，具体attach为请求消息体，第三方可以自行扩展，建议是json格式
	 * 
	 * @param msg
	 * @return {"code":200}
	 */
	public static JSONObject sendAttachMsg(Msg msg) {
		try {

			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/msg/sendAttachMsg.action";
			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("from", msg.getFrom()));
			params.add(new BasicNameValuePair("msgtype", msg.getMsgtype() + ""));
			params.add(new BasicNameValuePair("to", msg.getTo()));
			params.add(new BasicNameValuePair("attach", msg.getAttach()));
			params.add(new BasicNameValuePair("pushcontent", msg.getPushcontent()));
			params.add(new BasicNameValuePair("payload", msg.getPayload()));
			params.add(new BasicNameValuePair("sound", msg.getSound()));
			params.add(new BasicNameValuePair("save", msg.getSave() + ""));
			params.add(new BasicNameValuePair("option", msg.getOption()));

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
	 * 批量发送点对点自定义系统通知
	 * 
	 * 1.系统通知区别于普通消息，应用接收到直接交给上层处理，客户端可不做展示； 2.目前支持类型：点对点类型；
	 * 3.最大限500人，只能针对个人,如果批量提供的帐号中有未注册的帐号，会提示并返回给用户；
	 * 4.此接口受频率控制，一个应用一分钟最多调用120次，超过会返回416状态码，并且被屏蔽一段时间；
	 * 
	 * 应用场景：如某个用户给另一个用户发送好友请求信息等，具体attach为请求消息体，第三方可以自行扩展，建议是json格式
	 * 
	 * @param msg
	 * @return {"code":200,"unregister":"["a","b"...]"//未注册的帐号}
	 */
	public static JSONObject sendBatchAttachMsg(Msg msg) {
		try {

			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/msg/sendBatchAttachMsg.action";
			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("fromAccid", msg.getFrom()));
			params.add(new BasicNameValuePair("toAccids", msg.getTo()));
			params.add(new BasicNameValuePair("attach", msg.getAttach()));
			params.add(new BasicNameValuePair("pushcontent", msg.getPushcontent()));
			params.add(new BasicNameValuePair("payload", msg.getPayload()));
			params.add(new BasicNameValuePair("sound", msg.getSound()));
			params.add(new BasicNameValuePair("save", msg.getSave() + ""));
			params.add(new BasicNameValuePair("option", msg.getOption()));

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
	 * 文件上传
	 * 
	 * 文件上传，字符流需要base64编码，最大15M。
	 * 
	 * @param file
	 * @return {"code":200,"url":"xxx"}
	 */
	public static JSONObject upload(MsgFile file) {
		try {

			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/msg/upload.action";
			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("content", file.getContent()));
			params.add(new BasicNameValuePair("type", file.getType()));
			params.add(new BasicNameValuePair("ishttps", file.getIshttps()));
			params.add(new BasicNameValuePair("expireSec", file.getExpireSec() + ""));
			params.add(new BasicNameValuePair("tag", file.getTag()));

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
	 * 文件上传（multipart方式）
	 * 
	 * 最大15M。
	 * 
	 * @param file
	 * @return {"code":200,"url":"xxx"}
	 */
	public static JSONObject fileUpload(MsgFile file) {
		try {

			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/msg/fileUpload.action";
			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("content", file.getContent()));
			params.add(new BasicNameValuePair("type", file.getType()));
			params.add(new BasicNameValuePair("ishttps", file.getIshttps()));
			params.add(new BasicNameValuePair("expireSec", file.getExpireSec() + ""));
			params.add(new BasicNameValuePair("tag", file.getTag()));

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
	 * 上传NOS文件清理任务
	 * 
	 * 
	 * 上传NOS文件清理任务，按时间范围和文件类下、场景清理符合条件的文件
	 * 
	 * 每天提交的任务数量有限制，请合理规划
	 * 
	 * @param startTime   是 被清理文件的开始时间，必须小于endTime且大于0，endTime和startTime不能之间不能超过7天
	 * @param endTime     是 被清理文件的结束时间，必须大于startTime且早于今天（即只可以清理今天以前的文件）
	 * @param contentType 否 被清理的文件类型，文件类型包含contentType则被清理
	 *                    如原始文件类型为"image/png"，contentType参数为"image",则满足被清理条件
	 * @param tag         否 被清理文件的应用场景，完全相同才被清理
	 *                    如上传文件时知道场景为"usericon",tag参数为"usericon"，则满足被清理条件
	 * @return {"code":200,"data":{"taskid":"1024030f3841440daf2af73672792f47"}}
	 */
	public static JSONObject jobNosDel(Long startTime, Long endTime, String contentType, String tag) {
		try {

			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/job/nos/del.action";
			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("startTime", startTime + ""));
			params.add(new BasicNameValuePair("endTime", endTime + ""));
			params.add(new BasicNameValuePair("contentType", contentType));
			params.add(new BasicNameValuePair("tag", tag));

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
	 * 消息撤回
	 * 
	 * 消息撤回接口，可以撤回一定时间内的点对点与群消息
	 * 
	 * @param msg
	 * @return {"code":200}
	 */
	public static JSONObject recall(Msg msg) {
		try {

			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/msg/recall.action";
			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("deleteMsgid", msg.getMsgid()));
			params.add(new BasicNameValuePair("timetag", msg.getTimetag() + ""));
			params.add(new BasicNameValuePair("type", msg.getType() + ""));
			params.add(new BasicNameValuePair("from", msg.getFrom()));
			params.add(new BasicNameValuePair("to", msg.getTo()));
			params.add(new BasicNameValuePair("msg", msg.getMsg()));
			params.add(new BasicNameValuePair("ignoreTime", msg.getIgnoreTime()));
			params.add(new BasicNameValuePair("pushcontent", msg.getPushcontent()));
			params.add(new BasicNameValuePair("payload", msg.getPayload()));

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
	 * 发送广播消息 1、广播消息，可以对应用内的所有用户发送广播消息，广播消息目前暂不支持第三方推送（APNS、小米、华为等）；
	 * 2、广播消息支持离线存储，并可以自定义设置离线存储的有效期，最多保留最近100条离线广播消息；
	 * 3、此接口受频率控制，一个应用一分钟最多调用10次，一天最多调用1000次，超过会返回416状态码； 4、该功能目前需申请开通，详情可咨询您的客户经理。
	 * 
	 * @param body      String 是 广播消息内容，最大4096字符
	 * @param from      String 否 发送者accid, 用户帐号，最大长度32字符，必须保证一个APP内唯一
	 * @param isOffline String 否 是否存离线，true或false，默认false
	 * @param ttl       int 否 存离线状态下的有效期，单位小时，默认7天
	 * @param targetOs  String 否
	 *                  目标客户端，默认所有客户端，jsonArray，格式：["ios","aos","pc","web","mac"]
	 * @return {"code":200,"msg":{"expireTime":1505502793520,"body":"abc","createTime":1505466793520,"isOffline":true,"broadcastId":48174937359009,"targetOs":["ios","pc","aos"]}}
	 */
	public static JSONObject broadcastMsg(String body, String from, String isOffline, int ttl, String targetOs) {
		try {

			JSONObject json = null;
			String url = "https://api.netease.im/nimserver/msg/broadcastMsg.action";
			List<NameValuePair> params = new ArrayList<>();
			params.add(new BasicNameValuePair("body", body));
			params.add(new BasicNameValuePair("from", from));
			params.add(new BasicNameValuePair("isOffline", isOffline));
			params.add(new BasicNameValuePair("ttl", ttl + ""));
			params.add(new BasicNameValuePair("targetOs", targetOs));

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
	 * 创建聊天室
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomCreate(ChatroomCreateRequest request) {

		String url = "https://api.netease.im/nimserver/chatroom/create.action HTTP/1.1";

		return postServer(request, url);
	}

	/**
	 * 查询聊天室信息
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomGet(ChatroomGetRequest request) {

		String url = "https://api.netease.im/nimserver/chatroom/get.action HTTP/1.1";

		return postServer(request, url);
	}

	/**
	 * 批量查询聊天室信息
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomGetBatch(ChatroomGetRequest request) {

		String url = "https://api.netease.im/nimserver/chatroom/getBatch.action HTTP/1.1";

		return postServer(request, url);
	}

	/**
	 * 更新聊天室信息
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomUpdate(ChatroomUpdateRequest request) {

		String url = "https://api.netease.im/nimserver/chatroom/update.action HTTP/1.1";

		return postServer(request, url);
	}

	/**
	 * 修改聊天室开/关闭状态
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomToggleCloseStat(ChatroomToggleCloseStatRequest request) {

		String url = "https://api.netease.im/nimserver/chatroom/toggleCloseStat.action HTTP/1.1";

		return postServer(request, url);
	}

	/**
	 * 设置聊天室内用户角色
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomSetMemberRole(ChatroomSetMemberRoleRequest request) {
		String url = "https://api.netease.im/nimserver/chatroom/setMemberRole.action HTTP/1.1";
		return postServer(request, url);
	}

	/**
	 * 请求聊天室地址 往聊天室内发消息
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomRequestAddr(ChatroomRequestAddrRequest request) {
		String url = "https://api.netease.im/nimserver/chatroom/requestAddr.action HTTP/1.1";
		return postServer(request, url);
	}

	/**
	 * 发送聊天室消息
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomSendMsg(ChatroomSendMsgRequest request) {
		String url = "https://api.netease.im/nimserver/chatroom/sendMsg.action HTTP/1.1";
		return postServer(request, url);

	}

	/**
	 * 往聊天室内添加机器人
	 * 
	 * 往聊天室内添加机器人，机器人过期时间为24小时
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomAddRobot(ChatroomAddRobotRequest request) {
		String url = "https://api.netease.im/nimserver/chatroom/addRobot.action HTTP/1.1";
		return postServer(request, url);
	}

	/**
	 * 从聊天室内删除机器人
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomRemoveRobot(ChatroomRemoveRobotRequest request) {
		String url = "https://api.netease.im/nimserver/chatroom/removeRobot.action HTTP/1.1";
		return postServer(request, url);
	}

	/**
	 * 设置临时禁言状态
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomTemporaryMute(ChatroomTemporaryMuteRequest request) {
		String url = "https://api.netease.im/nimserver/chatroom/temporaryMute.action HTTP/1.1";
		return postServer(request, url);
	}

	/**
	 * 往聊天室有序队列中新加或更新元素
	 * 
	 * @param request
	 * @param transients 否
	 *                   这个新元素的提交者operator的所有聊天室连接在从该聊天室掉线或者离开该聊天室的时候，提交的元素是否需要删除。true：需要删除；false：不需要删除。默认false。
	 *                   当指定该参数为true时，若operator当前不在该聊天室内，则会返回403错误。
	 * @return
	 */
	public static JSONObject chatroomQueueOffer(ChatroomQueueOfferRequest request, String transients) {
		String url = "https://api.netease.im/nimserver/chatroom/queueOffer.action HTTP/1.1";
		try {
			JSONObject json = null;
			List<NameValuePair> params = reflect(request);
			params.add(new BasicNameValuePair("transient", transients));
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new ServiceException(0, e.getMessage());
		}
	}

	/**
	 * 从队列中取出元素
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomQueuePoll(ChatroomQueuePollRequest request) {
		String url = "https://api.netease.im/nimserver/chatroom/queuePoll.action HTTP/1.1";
		return postServer(request, url);
	}

	/**
	 * 排序列出队列中所有元素
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomQueueList(ChatroomQueueListRequest request) {
		String url = "https://api.netease.im/nimserver/chatroom/queueList.action HTTP/1.1";
		return postServer(request, url);
	}

	/**
	 * 删除清理整个队列
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomQueueDrop(ChatroomQueueDrop request) {
		String url = "https://api.netease.im/nimserver/chatroom/queueDrop.action HTTP/1.1";
		return postServer(request, url);
	}

	/**
	 * 初始化队列
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomQueueInit(ChatroomQueueInit request) {
		String url = "https://api.netease.im/nimserver/chatroom/queueInit.action HTTP/1.1";
		return postServer(request, url);
	}

	
	
	
	
	
	private static <T> JSONObject postServer(T request, String url) {
		try {
			JSONObject json = null;
			List<NameValuePair> params = reflect(request);
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new ServiceException(0, e.getMessage());
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


	/**
	 * 获取实体属性和属性值
	 * 
	 * @param   <T>
	 * @param e
	 * @throws Exception
	 */
	public static <T> List<NameValuePair> reflect(T e) throws Exception {
		Class<? extends Object> cls = e.getClass();

		List<NameValuePair> params = new ArrayList<NameValuePair>();

		Field[] fields = cls.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			f.setAccessible(true);
			params.add(new BasicNameValuePair(f.getName(), f.get(e) + ""));
			System.out.println("属性名:" + f.getName() + " 属性值:" + f.get(e));
		}
		System.out.println(params);
		return params;
	}

	public static void main(String[] args) throws Exception {
//		JSONObject json=new JSONObject();
//		System.out.println(json.get("info"));
		Msg e = new Msg();
		reflect(e);
	}
}
