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
import com.youxin.app.ex.ServiceException;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.yx.request.Friends;
import com.youxin.app.yx.request.Msg;
import com.youxin.app.yx.request.MsgFile;
import com.youxin.app.yx.request.MsgRequest;
import com.youxin.app.yx.request.User.User;
import com.youxin.app.yx.request.chatroom.ChatroomAddRobotRequest;
import com.youxin.app.yx.request.chatroom.ChatroomCreateRequest;
import com.youxin.app.yx.request.chatroom.ChatroomGetRequest;
import com.youxin.app.yx.request.chatroom.ChatroomMembersByPage;
import com.youxin.app.yx.request.chatroom.ChatroomMuteRoom;
import com.youxin.app.yx.request.chatroom.ChatroomQueryMembers;
import com.youxin.app.yx.request.chatroom.ChatroomQueryUserRoomIds;
import com.youxin.app.yx.request.chatroom.ChatroomQueueBatchUpdateElements;
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
import com.youxin.app.yx.request.chatroom.ChatroomTopn;
import com.youxin.app.yx.request.chatroom.ChatroomUpdateMyRoomRole;
import com.youxin.app.yx.request.chatroom.ChatroomUpdateRequest;
import com.youxin.app.yx.request.history.QueryBroadcastMsg;
import com.youxin.app.yx.request.history.QueryBroadcastMsgById;
import com.youxin.app.yx.request.history.QuerySessionMsg;
import com.youxin.app.yx.request.history.QueryTeamMsg;
import com.youxin.app.yx.request.history.QueryUserEvents;
import com.youxin.app.yx.request.subscribe.SubscribeAdd;
import com.youxin.app.yx.request.subscribe.SubscribeBatchdel;
import com.youxin.app.yx.request.subscribe.SubscribeDelete;
import com.youxin.app.yx.request.subscribe.SubscribeQuery;
import com.youxin.app.yx.request.team.Add;
import com.youxin.app.yx.request.team.AddOrUpdateManager;
import com.youxin.app.yx.request.team.ChangeOwner;
import com.youxin.app.yx.request.team.Create;
import com.youxin.app.yx.request.team.GetMarkReadInfo;
import com.youxin.app.yx.request.team.JoinTeams;
import com.youxin.app.yx.request.team.Kick;
import com.youxin.app.yx.request.team.Leave;
import com.youxin.app.yx.request.team.ListTeamMute;
import com.youxin.app.yx.request.team.MuteTeam;
import com.youxin.app.yx.request.team.MuteTlist;
import com.youxin.app.yx.request.team.MuteTlistAll;
import com.youxin.app.yx.request.team.Query;
import com.youxin.app.yx.request.team.QueryDetail;
import com.youxin.app.yx.request.team.Remove;
import com.youxin.app.yx.request.team.Update;
import com.youxin.app.yx.request.team.UpdateTeamNick;

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
			logger.debug("httpRes:" + res);
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
			params.add(new BasicNameValuePair("accid", user.getAccid()));
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
			logger.debug("httpRes:" + res);
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
			logger.debug("httpRes:" + res);
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
			logger.debug("httpRes:" + res);
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
			logger.debug("httpRes:" + res);
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
			logger.debug("httpRes:" + res);
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
			logger.debug("httpRes:" + res);
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
			logger.debug("httpRes:" + res);
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
			logger.debug("httpRes:" + res);
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
			logger.debug("httpRes:" + res);
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
			logger.debug("httpRes:" + res);
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
			logger.debug("httpRes:" + res);
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
			logger.debug("httpRes:" + res);
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
			logger.debug("httpRes:" + res);
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
			logger.debug("httpRes:" + res);
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
			logger.debug("httpRes:" + res);
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
			logger.debug("httpRes:" + res);
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
			logger.debug("httpRes:" + res);
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
			logger.debug("httpRes:" + res);
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

		String url = "https://api.netease.im/nimserver/chatroom/create.action";

		return postServer(request, url);
	}

	/**
	 * 查询聊天室信息
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomGet(ChatroomGetRequest request) {

		String url = "https://api.netease.im/nimserver/chatroom/get.action";

		return postServer(request, url);
	}

	/**
	 * 批量查询聊天室信息
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomGetBatch(ChatroomGetRequest request) {

		String url = "https://api.netease.im/nimserver/chatroom/getBatch.action";

		return postServer(request, url);
	}

	/**
	 * 更新聊天室信息
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomUpdate(ChatroomUpdateRequest request) {

		String url = "https://api.netease.im/nimserver/chatroom/update.action";

		return postServer(request, url);
	}

	/**
	 * 修改聊天室开/关闭状态
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomToggleCloseStat(ChatroomToggleCloseStatRequest request) {

		String url = "https://api.netease.im/nimserver/chatroom/toggleCloseStat.action";

		return postServer(request, url);
	}

	/**
	 * 设置聊天室内用户角色
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomSetMemberRole(ChatroomSetMemberRoleRequest request) {
		String url = "https://api.netease.im/nimserver/chatroom/setMemberRole.action";
		return postServer(request, url);
	}

	/**
	 * 请求聊天室地址 往聊天室内发消息
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomRequestAddr(ChatroomRequestAddrRequest request) {
		String url = "https://api.netease.im/nimserver/chatroom/requestAddr.action";
		return postServer(request, url);
	}

	/**
	 * 发送聊天室消息
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomSendMsg(ChatroomSendMsgRequest request) {
		String url = "https://api.netease.im/nimserver/chatroom/sendMsg.action";
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
		String url = "https://api.netease.im/nimserver/chatroom/addRobot.action";
		return postServer(request, url);
	}

	/**
	 * 从聊天室内删除机器人
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomRemoveRobot(ChatroomRemoveRobotRequest request) {
		String url = "https://api.netease.im/nimserver/chatroom/removeRobot.action";
		return postServer(request, url);
	}

	/**
	 * 设置临时禁言状态
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomTemporaryMute(ChatroomTemporaryMuteRequest request) {
		String url = "https://api.netease.im/nimserver/chatroom/temporaryMute.action";
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
		String url = "https://api.netease.im/nimserver/chatroom/queueOffer.action";
		try {
			JSONObject json = null;
			List<NameValuePair> params = reflect(request);
			params.add(new BasicNameValuePair("transient", transients));
			// UTF-8编码,解决中文问题
			HttpEntity entity = new UrlEncodedFormEntity(params, "UTF-8");

			String res = NIMPost.postNIMServer(url, entity, APPKEY, SECRET);
			json = getJson(json, res);
			logger.debug("httpRes:" + res);
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
		String url = "https://api.netease.im/nimserver/chatroom/queuePoll.action";
		return postServer(request, url);
	}

	/**
	 * 排序列出队列中所有元素
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomQueueList(ChatroomQueueListRequest request) {
		String url = "https://api.netease.im/nimserver/chatroom/queueList.action";
		return postServer(request, url);
	}

	/**
	 * 删除清理整个队列
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomQueueDrop(ChatroomQueueDrop request) {
		String url = "https://api.netease.im/nimserver/chatroom/queueDrop.action";
		return postServer(request, url);
	}

	/**
	 * 初始化队列
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomQueueInit(ChatroomQueueInit request) {
		String url = "https://api.netease.im/nimserver/chatroom/queueInit.action";
		return postServer(request, url);
	}

	/**
	 * 将聊天室整体禁言 设置聊天室整体禁言状态（仅创建者和管理员能发言）
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomMuteRoom(ChatroomMuteRoom request) {
		String url = "https://api.netease.im/nimserver/chatroom/muteRoom.action";
		return postServer(request, url);
	}

	/**
	 * 查询聊天室统计指标TopN 1、根据时间戳，按指定周期列出聊天室相关指标的TopN列表 2、当天的统计指标需要到第二天才能查询
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomTopn(ChatroomTopn request) {
		String url = "https://api.netease.im/nimserver/chatroom/topn.action";
		return postServer(request, url);
	}

	/**
	 * 分页获取成员列表 1、根据时间戳，按指定周期列出聊天室相关指标的TopN列表 2、当天的统计指标需要到第二天才能查询
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomMembersByPage(ChatroomMembersByPage request) {
		String url = "https://api.netease.im/nimserver/chatroom/membersByPage.action";
		return postServer(request, url);
	}

	/**
	 * 批量获取在线成员信息
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomQueryMembers(ChatroomQueryMembers request) {
		String url = "https://api.netease.im/nimserver/chatroom/queryMembers.action";
		return postServer(request, url);
	}

	/**
	 * 变更聊天室内的角色信息
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomUpdateMyRoomRole(ChatroomUpdateMyRoomRole request) {
		String url = "https://api.netease.im/nimserver/chatroom/updateMyRoomRole.action";
		return postServer(request, url);
	}

	/**
	 * 批量更新聊天室队列元素
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomQueueBatchUpdateElements(ChatroomQueueBatchUpdateElements request) {
		String url = "https://api.netease.im/nimserver/chatroom/queueBatchUpdateElements.action";
		return postServer(request, url);
	}

	/**
	 * 查询用户创建的开启状态聊天室列表
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject chatroomQueryUserRoomIds(ChatroomQueryUserRoomIds request) {
		String url = "https://api.netease.im/nimserver/chatroom/queryUserRoomIds.action";
		return postServer(request, url);
	}

	// ====================================================================================================
	// =======================================历史信息=====================================================
	// ====================================================================================================
	/**
	 * 单聊云端历史消息查询 查询存储在网易云通信服务器中的单人聊天历史消息，只能查询在保存时间范围内的消息
	 * 
	 * 跟据时间段查询点对点消息，每次最多返回100条； 不提供分页支持，第三方需要跟据时间段来查询。
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject querySessionMsg(QuerySessionMsg request) {
		String url = "https://api.netease.im/nimserver/history/querySessionMsg.action";
		return postServer(request, url);
	}

	/**
	 * 群聊云端历史消息查询
	 * 
	 * 
	 * 查询存储在网易云通信服务器中的群聊天历史消息，只能查询在保存时间范围内的消息
	 * 
	 * 跟据时间段查询群消息，每次最多返回100条； 不提供分页支持，第三方需要跟据时间段来查询。
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject queryTeamMsg(QueryTeamMsg request) {
		String url = "https://api.netease.im/nimserver/history/queryTeamMsg.action";
		return postServer(request, url);
	}

	/**
	 * 用户登录登出事件记录查询
	 * 
	 * 
	 * 接口描述 1.跟据时间段查询用户的登录登出记录，每次最多返回100条。 2.不提供分页支持，第三方需要跟据时间段来查询。
	 * 3.此接口需要联系客户经理开通方能生效，生效后可以查询。不支持查询开通前的登录登出事件记录。
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject queryUserEvents(QueryUserEvents request) {
		String url = "https://api.netease.im/nimserver/history/queryUserEvents.action";
		return postServer(request, url);
	}

	/**
	 * 批量查询广播消息
	 * 
	 * @param request
	 * @return
	 */

	public static JSONObject queryBroadcastMsg(QueryBroadcastMsg request) {
		String url = "https://api.netease.im/nimserver/history/queryBroadcastMsg.action";
		return postServer(request, url);
	}

	/**
	 * 查询单条广播消息
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject queryBroadcastMsgById(QueryBroadcastMsgById request) {
		String url = "https://api.netease.im/nimserver/history/queryBroadcastMsgById.action";
		return postServer(request, url);
	}
	// ====================================================================================================
	// =======================================在线状态=====================================================
	// ====================================================================================================

	/**
	 * 订阅指定人员的在线状态事件，每个账号最大有效订阅账号不超过3000个
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject subscribeAdd(SubscribeAdd request) {
		String url = "https://api.netease.im/nimserver/event/subscribe/add.action";
		return postServer(request, url);
	}

	/**
	 * 取消订阅指定人员的在线状态事件
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject subscribeDelete(SubscribeDelete request) {
		String url = "https://api.netease.im/nimserver/event/subscribe/delete.action";
		return postServer(request, url);
	}

	/**
	 * 取消全部在线状态事件订阅
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject subscribeBatchdel(SubscribeBatchdel request) {
		String url = "https://api.netease.im/nimserver/event/subscribe/batchdel.action";
		return postServer(request, url);
	}

	/**
	 * 查询在线状态事件订阅关系
	 * 
	 * 查询指定人员的有效在线状态事件订阅关系
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject subscribeQuery(SubscribeQuery request) {
		String url = "https://api.netease.im/nimserver/event/subscribe/query.action";
		return postServer(request, url);
	}

	// ==================================================================================
	// =========================高级群组================================
	// ==================================================================================
	/**
	 * 创建群
	 * 
	 * 创建高级群，以邀请的方式发送给用户；
	 * 
	 * custom 字段是给第三方的扩展字段，第三方可以基于此字段扩展高级群的功能，构建自己需要的群；
	 * 建群成功会返回tid，需要保存，以便于加人与踢人等后续操作； 每个用户可创建的群数量有限制，限制值由 IM 套餐的群组配置决定，可登录管理后台查看。
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject teamCreate(Create request) {
		String url = "https://api.netease.im/nimserver/team/create.action";
		return postServer(request, url);
	}

	/**
	 * 拉人入群
	 * 
	 * 
	 * 1.可以批量邀请，邀请时需指定群主； 2.当群成员达到上限时，再邀请某人入群返回失败； 3.当群成员达到上限时，被邀请人“接受邀请"的操作也将返回失败。
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject teamAdd(Add request) {
		String url = "https://api.netease.im/nimserver/team/add.action";
		return postServer(request, url);
	}

	/**
	 * 踢人出群
	 * 
	 * 高级群踢人出群，需要提供群主accid以及要踢除人的accid。
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject teamKick(Kick request) {
		String url = "https://api.netease.im/nimserver/team/kick.action";
		return postServer(request, url);
	}

	/**
	 * 解散群 删除整个群，会解散该群，需要提供群主accid，谨慎操作！
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject teamRemove(Remove request) {
		String url = "https://api.netease.im/nimserver/team/remove.action";
		return postServer(request, url);
	}

	/**
	 * 编辑群资料
	 * 
	 * 高级群基本信息修改
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject teamUpdate(Update request) {
		String url = "https://api.netease.im/nimserver/team/update.action";
		return postServer(request, url);
	}

	/**
	 * 群信息与成员列表查询
	 * 
	 * 
	 * 高级群信息与成员列表查询，一次最多查询30个群相关的信息，跟据ope参数来控制是否带上群成员列表；
	 * 查询群成员会稍微慢一些，所以如果不需要群成员列表可以只查群信息； 此接口受频率控制，某个应用一分钟最多查询30次，超过会返回416，并且被屏蔽一段时间；
	 * 群成员的群列表信息中增加管理员成员admins的返回。
	 * 
	 * @param request
	 * @return
	 */
	public static JSONObject teamQuery(Query request) {
		String url = "https://api.netease.im/nimserver/team/query.action";
		return postServer(request, url);
	}
	/**
	 * 获取群组详细信息
	 * 
	 * 查询指定群的详细信息（群信息+成员详细信息）
	 * @param request
	 * @return
	 */
	public static JSONObject teamQueryDetail(QueryDetail request) {
		String url = "https://api.netease.im/nimserver/team/queryDetail.action";
		return postServer(request, url);
	}
	/**
	 * 获取群组已读消息的已读详情信息
	 * @param request
	 * @return
	 */
	public static JSONObject teamGetMarkReadInfo(GetMarkReadInfo request) {
		String url = "https://api.netease.im/nimserver/team/getMarkReadInfo.action";
		return postServer(request, url);
	}
	
	/**
	 * 获取群组已读消息的已读详情信息
	 * @param request
	 * @return
	 */
	public static JSONObject teamChangeOwner(ChangeOwner request) {
		String url = "https://api.netease.im/nimserver/team/changeOwner.action";
		return postServer(request, url);
	}

	
	/**
	 * 任命管理员
	 * 提升普通成员为群管理员，可以批量，但是一次添加最多不超过10个人。
	 * @param request
	 * @return
	 */
	public static JSONObject teamAddManager(AddOrUpdateManager request) {
		String url = "https://api.netease.im/nimserver/team/addManager.action";
		return postServer(request, url);
	}
	
	
	/**
	 * 移除管理员
	 * 解除管理员身份，可以批量，但是一次解除最多不超过10个人
	 * @param request
	 * @return
	 */
	public static JSONObject teamRemoveManager(AddOrUpdateManager request) {
		String url = "https://api.netease.im/nimserver/team/removeManager.action";
		return postServer(request, url);
	}
	
	/**
	 * 获取某用户所加入的群信息
	 * 获取某个用户所加入高级群的群信息
	 * @param request
	 * @return
	 */
	public static JSONObject teamJoinTeams(JoinTeams request) {
		String url = "https://api.netease.im/nimserver/team/joinTeams.action";
		return postServer(request, url);
	}
	
	/**
	 * 修改群昵称
	 * 修改指定账号在群内的昵称
	 * @param request
	 * @return
	 */
	public static JSONObject teamUpdateTeamNick(UpdateTeamNick request) {
		String url = "https://api.netease.im/nimserver/team/updateTeamNick.action";
		return postServer(request, url);
	}
	

	/**
	 * 修改消息提醒开关
	 * 修改消息提醒开关
	 * @param request
	 * @return
	 */
	public static JSONObject teamMuteTeam(MuteTeam request) {
		String url = "https://api.netease.im/nimserver/team/muteTeam.action";
		return postServer(request, url);
	}
	/**
	 * 禁言群成员
	 * 高级群禁言群成员
	 * @param request
	 * @return
	 */
	public static JSONObject teamMuteTlist(MuteTlist request) {
		String url = "https://api.netease.im/nimserver/team/muteTlist.action";
		return postServer(request, url);
	}
	
	/**
	 * 主动退群
	 * 高级群主动退群
	 * @param request
	 * @return
	 */
	public static JSONObject teamLeave(Leave request) {
		String url = "https://api.netease.im/nimserver/team/leave.action";
		return postServer(request, url);
	}
	/**
	 * 将群组整体禁言
	 * 禁言群组，普通成员不能发送消息，创建者和管理员可以发送消息
	 * @param request
	 * @return
	 */
	public static JSONObject teamMuteTlistAll(MuteTlistAll request) {
		String url = "https://api.netease.im/nimserver/team/muteTlistAll.action";
		return postServer(request, url);
	}
	
	/**
	 * 获取群组禁言列表
	 * @param request
	 * @return
	 */
	public static JSONObject teamListTeamMute(ListTeamMute request) {
		String url = "https://api.netease.im/nimserver/team/listTeamMute.action";
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
			logger.debug("httpRes:" + res);
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
				throw new ServiceException(-1, json.getString("desc"));
		} else
			throw new ServiceException(-1, "sdk异常");

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
