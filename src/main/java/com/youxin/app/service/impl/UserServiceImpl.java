package com.youxin.app.service.impl;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.youxin.app.entity.SdkLoginInfo;
import com.youxin.app.entity.User;
import com.youxin.app.entity.User.DeviceInfo;
import com.youxin.app.entity.User.LoginLog;
import com.youxin.app.entity.User.UserLoginLog;
import com.youxin.app.entity.User.UserSettings;
import com.youxin.app.entity.UserVo;
import com.youxin.app.entity.exam.UserExample;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.repository.UserRepository;
import com.youxin.app.service.UserService;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.KSessionUtil;
import com.youxin.app.utils.Md5Util;
import com.youxin.app.utils.MongoOperator;
import com.youxin.app.utils.ReqUtil;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.WXUserUtils;
import com.youxin.app.utils.sms.SMSServiceImpl;
import com.youxin.app.yx.SDKService;
import com.youxin.app.yx.request.Friends;








@Service
public class UserServiceImpl implements UserService {
	protected Log log=LogFactory.getLog(this.getClass());
	@Autowired
	private UserRepository repository;

	@Autowired
	@Qualifier("get")
	private Datastore dfds;

	@Autowired
	private SMSServiceImpl smsServer;

	@Value("${youxin.accountTitle}")
	private String accountTitle;
	@Override
	public Map<String, Object> register(User bean) {
		
		//创建主键
		Integer userId = createUserId();
		bean.setId(userId);
		//设置初始有讯号
		bean.setAccount(StringUtil.randomAccount(accountTitle) + userId);
		Map<String, Object> map = addUser(bean);
		return map;
	}

	public synchronized Map<String, Object> addUser(User bean) {
		String accid = Md5Util.md5Hex(bean.getId() + "");
		bean.setAccid(accid);
		com.youxin.app.yx.request.User.User u = new com.youxin.app.yx.request.User.User();
		// sdk注册
		BeanUtils.copyProperties(bean, u);
		// 扩展字段封装
		u.setEx(bean.setExs());
		JSONObject json = SDKService.createUser(u);

		User us = JSONObject.toJavaObject(json.getJSONObject("info"), User.class);
		// 注册成功
		bean.setToken(us.getToken());
		if (StringUtil.isEmpty(bean.getToken())) {
			throw new ServiceException(0, "token缺失，注册失败");
		}
		bean.setSettings(new UserSettings());
		// 保存本地数据库
		repository.save(bean);
		// 第三方注册绑定信息
		if(!StringUtil.isEmpty(bean.getLoginInfo())) {
			SdkLoginInfo findSdkLoginInfo = findSdkLoginInfo(bean.getSdkType(), bean.getLoginInfo());
			if(findSdkLoginInfo==null) {
				SdkLoginInfo sdkLoginInfo=new SdkLoginInfo();
				sdkLoginInfo.setCreateTime(DateUtil.currentTimeSeconds());
				sdkLoginInfo.setLoginInfo(bean.getLoginInfo());
				sdkLoginInfo.setType(2);
				sdkLoginInfo.setUserId(bean.getId());
				sdkLoginInfo.setAccid(accid);
				dfds.save(sdkLoginInfo);
			}
		}
		
		// 加系统客服好友
		Friends friends = new Friends();
		friends.setAccid(accid);
		friends.setFaccid(Md5Util.md5Hex("10000"));
		friends.setType(1);
		friends.setMsg("加客服好友");
		SDKService.friendAdd(friends);

		// 缓存用户token
		Map<String, Object> data = saveLoginInfo(bean);
		
		return data;
	}

	@Override
	public User getUser(String accid, String toaccid) {

		User user = repository.findOne("accid", accid);
		if (null == user) {
			System.out.println("accid为" + accid + "的用户不存在");
			return null;
		}
		user.setEx("");
		user.setPassword("");
		if(StringUtil.isEmpty(user.getPayPassword())||"0".equals(user.getPayPassword())) 
			user.setPayPassword("0");
		else
			user.setPayPassword("1");
		user.setMobile(StringUtil.phoneEncryption(user.getMobile()));
		return user;
	}
	
	public User getUser(Integer userId) {
		// 先从 Redis 缓存中获取
		User user = KSessionUtil.getUserByUserId(userId);
		if (null == user) {
			user = repository.findOne("_id", userId);
			if (null == user) {
				System.out.println("id为" + userId + "的用户不存在");
				return null;
			}
		
			KSessionUtil.saveUserByUserId(userId, user);
		}

		return user;
	}

	@Override
	public Map<String, Object> login(User bean) {
		User user = null;
		if (!StringUtil.isEmpty(bean.getAccid()))
			user = repository.findOne("_id", bean.getId());
		else {
			user = repository.findOne("mobile", bean.getMobile());
			if(null == user) {
				user=repository.findOne("account", bean.getAccount());
			}
		}
		if (null == user) {
			throw new ServiceException(20004, "帐号不存在, 请注册!");
		} else {
			if (bean.getLoginType() == 0) {
				// 账号密码登录
				String password = bean.getPassword();
				if (!password.equals(user.getPassword()))
					throw new ServiceException(20002, "密码错误");
			} else if (1 == bean.getLoginType()) {
				// 短信验证码登录
				if (null == bean.getSmsCode())
					throw new ServiceException("短信验证码不能为空!");
				if (!smsServer.isAvailable("86" + user.getMobile(), bean.getSmsCode()))
					throw new ServiceException("短信验证码不正确!");
			} else if (2 == bean.getLoginType()) {
				// 友讯号密码登录
				String password = bean.getPassword();
				if (!password.equals(user.getPassword()))
					throw new ServiceException(20002, "密码错误");
			} else if (3 == bean.getLoginType()) {
				// sdk登录
				
			}else {
				throw new ServiceException(20002, "登录方式错误");
			}

		}
	
		KSessionUtil.saveUserByUserId(user.getId(), user);
		Map<String, Object> data = saveLoginInfo(user);
		return data;
	}

	@Override
	public long mobileCount(String mobile) {

		return repository.count("mobile", mobile);
	}

	// 获取用户Id
	public synchronized Integer createUserId() {
		DBCollection collection = dfds.getDB().getCollection("idx_user");
		if (null == collection)
			return createIdxUserCollection(collection, 0);
		DBObject obj = collection.findOne();
		if (null != obj) {
			Integer id = new Integer(obj.get("id").toString());
			id += 1;
			collection.update(new BasicDBObject("_id", obj.get("_id")),
					new BasicDBObject("$inc", new BasicDBObject("id", 1)));
			return id;
		} else {
			return createIdxUserCollection(collection, 0);
		}

	}

	// 初始化自增长计数表数据
	private Integer createIdxUserCollection(DBCollection collection, long userId) {
		if (null == collection)
			collection = dfds.getDB().createCollection("idx_user", new BasicDBObject());
		BasicDBObject init = new BasicDBObject();
		Integer id = getMaxUserId();
		if (0 == id || id < 10000001)
			id = new Integer("10000001");
		id += 1;
		init.append("id", id);
//			init.append("stub", "id");
//			init.append("call", 300000);
//			init.append("videoMeetingNo", 350000);
//			init.append("inviteCodeNo", 1001);
		collection.insert(init);
		return id;
	}

	public Integer getMaxUserId() {
		BasicDBObject projection = new BasicDBObject("_id", 1);
		DBObject dbobj = dfds.getDB().getCollection("user").findOne(null, projection, new BasicDBObject("_id", -1));
		if (null == dbobj)
			return 0;
		Integer id = new Integer(dbobj.get("_id").toString());
		return id;
	}

	public static void main(String[] args) {
		// 加系统客服好友
//		Friends friends=new Friends();
//		friends.setAccid("2dd0c6c424d1afbf925b8be4fbe85981");
//		friends.setFaccid("1f62ff7760da2b49ee1468b19e90d80f");
//		friends.setType(1);
//		friends.setMsg("加客服好友");
//		SDKService.friendAdd(friends);

//		System.out.println(SDKService.getUinfos("['2dd0c6c424d1afbf925b8be4fbe85981']"));


	}

	

	@Override
	public String getUserName(Integer userId) {
		return this.getUser(userId).getName();
	}

	// 用户充值 type 1 充值 2 消费
	public synchronized Double rechargeUserMoeny(Integer userId, Double money, int type) {
		try {

			Query<User> q = repository.createQuery();
			q.field("_id").equal(userId);
			UpdateOperations<User> ops = repository.createUpdateOperations();
			User user = getUser(userId);
			User dbUser=q.get();
			if (null == user)
				return 0.0;
			DecimalFormat df = new DecimalFormat("#.00");
			money = Double.valueOf(df.format(money));

			if (KConstants.MOENY_ADD == type) {
				ops.set("balance", Double.valueOf(df.format(dbUser.getBalance() + money)));
				ops.set("totalRecharge", Double.valueOf(df.format(dbUser.getTotalRecharge() + money)));
				user.setBalance(Double.valueOf(df.format(user.getBalance() + money)));
			} else {
				if (this.getUserMoeny(userId) < money) {
					// 余额不足
					System.out.println("余额不足");
					return 0.0;
				}
				ops.set("balance", Double.valueOf(df.format(dbUser.getBalance() - money)));
				ops.set("totalConsume", Double.valueOf(df.format(dbUser.getTotalConsume() + money)));
				user.setBalance(Double.valueOf(df.format(user.getBalance() - money)));
			}
			repository.update(q, ops);
			KSessionUtil.saveUserByUserId(userId, user);
			return q.get().getBalance();
		} catch (Exception e) {
			e.printStackTrace();
			return 0.0;
		}
	}
	
	// 获取用户金额
	public Double getUserMoeny(Integer userId) {
		try {
			DecimalFormat df=new DecimalFormat("#.00");
			User user=this.getUserFromDB(userId);
			return Double.valueOf(df.format(user.getBalance())) ;
		} catch (Exception e) {
			e.printStackTrace();
			return 0.0;
		}
	}
	
	
	@Override
	public List<DBObject> queryUser(UserVo example) {
		List<DBObject> list = Lists.newArrayList();
		// Query<User> query = mongoDs.find(getEntityClass());
		// Query<User> query =mongoDs.createQuery(getEntityClass());
		// query.filter("_id<", param.getUserId());
		DBObject ref = new BasicDBObject();
		if (null != example.getId())
			ref.put("_id", new BasicDBObject("$lt", example.getId()));
		if (!StringUtil.isEmpty(example.getName()))
			ref.put("name", Pattern.compile(example.getName()));
		if (example.getGender()>0)
			ref.put("sex", example.getGender());
		if (!StringUtil.isEmpty(example.getMobile()))
			ref.put("mobile", example.getMobile());
		if (!StringUtil.isEmpty(example.getAccount()))
			ref.put("account", example.getAccount());
		//允许手机号搜索
		ref.put("settings.searchByMobile", 1);
//		if (null != example.getStartTime())
//			ref.put("birthday", new BasicDBObject("$gte", example.getStartTime()));
//		if (null != example.getEndTime())
//			ref.put("birthday", new BasicDBObject("$lte", example.getEndTime()));
		DBObject fields = new BasicDBObject();
		fields.put("password", 0);
		fields.put("payPassword", 0);
		fields.put("token", 0);
		fields.put("loginType", 0);
		fields.put("balance", 0);
		fields.put("totalRecharge", 0);
		fields.put("totalConsume", 0);
		DBCursor cursor = dfds.getDB().getCollection("user").find(ref, fields)
				.sort(new BasicDBObject("_id", -1)).limit(10);
		while (cursor.hasNext()) {
			DBObject obj = cursor.next();
			obj.put("userId", obj.get("_id"));
			obj.removeField("_id");
			obj.put("mobile", StringUtil.phoneEncryption(obj.get("mobile").toString()));
			
			list.add(obj);
		}

		return list;
	}

	@Override
	public User getUserFromDB(Integer userId) {
			User user = repository.findOne("_id", userId);
			if (null == user) {
				System.out.println("id为" + userId + "的用户不存在");
				return null;
			}

			return user;
	}
	public String getAccid(Integer userId) {
		User user = repository.findOne("_id", userId);
		if (null == user) {
			System.out.println("id为" + userId + "的用户不存在");
			return null;
		}

		return user.getAccid();
}

	@Override
	public void updateUser(User bean) {
		if(bean==null || bean.getId()==null ||bean.getId()<1000)
			log.debug("信息同步更新失败");
		Query<User> q = repository.createQuery();
		q.field("_id").equal(bean.getId());
		UpdateOperations<User> ops = repository.createUpdateOperations();
		if(bean.getDisableUser()==0 || bean.getDisableUser()==1) {
			ops.set("disableUser", bean.getDisableUser());
		}
		repository.update(q, ops);
	}
	

	
	@Override
	public User updateAccount(String account) {
		if(account.length()<4) {
			throw new ServiceException(0, "友讯号长度不能小于4");
		}
		
		Integer userId=ReqUtil.getUserId();
		User user = getUserFromDB(userId);
		if(!StringUtil.isEmpty(user.getAccount()) && user.getAccount().contains("毛主席")) {
			throw new ServiceException(0, "友讯号已经修改过或者字段合法");
		}
		if(getUserByAccount(account)!=null) {
			throw new ServiceException(0, "友讯号已存在");
		}
		Query<User> q = repository.createQuery();
		q.field("_id").equal(userId);
		
		UpdateOperations<User> ops = repository.createUpdateOperations();
		ops.set("account", account+"毛主席");
		
		UpdateResults update = repository.update(q, ops);
		System.out.println(update);
		
		
		User user2 = getUser(userId);
		user2.setAccount(account+"毛主席");
		KSessionUtil.saveUserByUserId(userId, user2);
		return user2;
	}

	@Override
	public void updateSettings(UserSettings settings) {
		Integer userId = ReqUtil.getUserId();
		User user = getUserFromDB(userId);
		
		user.setSettings(settings);
		System.out.println(user.toString());
		repository.save(user);
		
		User user2 = getUser(userId);
		user2.setSettings(settings);
		KSessionUtil.saveUserByUserId(userId, user2);
	}

	@Override
	public User getUserByMobile(String mobile) {
		User user = repository.findOne("mobile", mobile);
		if (null == user) {
			System.out.println("mobile为" + mobile + "的用户不存在");
			return null;
		}

		return user;
	}
	
	
	public User getUserByAccount(String account) {
		User user = repository.findOne("account", account);
		if (null == user) {
			System.out.println("account为" + account + "的用户不存在");
			return null;
		}

		return user;
	}
	
	@Override
	public SdkLoginInfo findSdkLoginInfo(int type, String loginInfo) {
		Query<SdkLoginInfo> query = dfds.createQuery(SdkLoginInfo.class).field("type").equal(type)
				.field("loginInfo").equal(loginInfo);
		return query.get();
	}
	@Override
	public List<SdkLoginInfo> delSdkLoginInfo(int type, String loginInfo) {
		Query<SdkLoginInfo> query = dfds.createQuery(SdkLoginInfo.class).field("type").equal(type)
				.field("loginInfo").equal(loginInfo);
		dfds.delete(query);
		return getSdkLoginInfo();
	}

	@Override
	public List<SdkLoginInfo> getSdkLoginInfo() {
		Query<SdkLoginInfo> q = dfds.createQuery(SdkLoginInfo.class).field("userId").equal(ReqUtil.getUserId());
		return q.asList();
	}
	
	@Override
	public JSONObject getWxOpenId(String code) {
		if (StringUtil.isEmpty(code)) {
			return null;
		}
		JSONObject jsonObject = WXUserUtils.getWxOpenId(code);
		String openid = jsonObject.getString("unionid");
		if (StringUtil.isEmpty(openid)) {
			return null;
		}
		return jsonObject;
	}

	@Override
	public void updateMobile(String mobile) {
		
		Query<User> q = repository.createQuery();
		q.field("_id").equal(ReqUtil.getUserId());
		
		UpdateOperations<User> ops = repository.createUpdateOperations();
		ops.set("mobile", mobile);
		repository.update(q, ops);
	}

	@Override
	public User getUserFromDB(String accid) {
		User user = repository.findOne("accid", accid);
		if (null == user) {
			System.out.println("accid为" + accid + "的用户不存在");
			return null;
		}

		return user;
	}

	@Override
	public Map<String, Object> saveLoginInfo(User user) {
		// 获取上次登录日志
		User.LoginLog login = getLogin(user.getId());

		// 保存登录日志
		updateUserLoginLog(user.getId(), user);
		
		
		Map<String, Object> data = KSessionUtil.loginSaveAccessToken(user.getId(), user.getId(), null);
//		Object token = data.get("access_token");
		data.put("id", user.getId());
		data.put("accid", user.getAccid());
		data.put("account", user.getAccount());
		data.put("name", user.getName());
		data.put("birth", user.getBirth());
		data.put("icon", user.getIcon());
		data.put("token", user.getToken());
		data.put("mobile", StringUtil.phoneEncryption(user.getMobile()));
		data.put("codeSign", user.getCodeSign());
		data.put("login", login);
		System.out.println(data);
		return data;
	}

	@Override
	public void updateUserByEle(User bean) {
		if(bean==null || bean.getId()==null ||bean.getId()<1000)
			log.debug("用户信息更新失败");
		Query<User> q = repository.createQuery();
		q.field("_id").equal(bean.getId());
		UpdateOperations<User> ops = repository.createUpdateOperations();
		if(!StringUtil.isEmpty(bean.getCodeSign())) {
			ops.set("codeSign", bean.getCodeSign());
		}
		repository.update(q, ops);
		KSessionUtil.saveUserByUserId(bean.getId(), q.get());
	}
	
//	@Override
	public User.LoginLog getLogin(int userId) {

		UserLoginLog userLoginLog = dfds.createQuery(UserLoginLog.class).field("_id").equal(userId).get();
		if (null == userLoginLog || null == userLoginLog.getLoginLog()) {
			UserLoginLog loginLog = new UserLoginLog();
			loginLog.setUserId(userId);
			loginLog.setLoginLog(new LoginLog());
			dfds.save(loginLog);
			return loginLog.getLoginLog();
		} else {
			return userLoginLog.getLoginLog();
		}

	}
	
	public void updateUserLoginLog(int userId, User example) {
		DBObject query = new BasicDBObject("_id", userId);

		DBObject values = new BasicDBObject();
		DBObject object = dfds.getCollection(UserLoginLog.class).findOne(query);
		if (null == object)
			values.put("_id", userId);

		BasicDBObject loginLog = new BasicDBObject("isFirstLogin", 0);
		loginLog.put("loginTime", DateUtil.currentTimeSeconds());
		loginLog.put("apiVersion", example.getApiVersion());
		loginLog.put("osVersion", example.getOsVersion());
		loginLog.put("model", example.getModel());
		loginLog.put("serial", example.getSerial());
		loginLog.put("latitude", example.getLatitude());
		loginLog.put("longitude", example.getLongitude());
		loginLog.put("location", example.getLocation());
		loginLog.put("address", example.getAddress());
		values.put("loginLog", loginLog);

		dfds.getCollection(UserLoginLog.class).update(query, new BasicDBObject(MongoOperator.SET, values),
				true, false);

		// updateAttribute(userId, "appId", example.getAppId());
	}

	public void updateLoginLogTime(int userId) {
		DBObject query = new BasicDBObject("_id", userId);

		DBObject values = new BasicDBObject();
		DBObject object = dfds.getCollection(UserLoginLog.class).findOne(query);
		BasicDBObject loginLog = null;
		if (null == object || null == object.get("loginLog")) {
			values.put("_id", userId);

			loginLog = new BasicDBObject("isFirstLogin", 0);
			loginLog.put("loginTime", DateUtil.currentTimeSeconds());
			values.put("loginLog", loginLog);
			dfds.getCollection(UserLoginLog.class).update(query, new BasicDBObject(MongoOperator.SET, values),
					true, false);
		} else {
			values.put("loginLog.loginTime", DateUtil.currentTimeSeconds());
			dfds.getCollection(UserLoginLog.class).update(query, new BasicDBObject(MongoOperator.SET, values),
					true, false);

		}

	}
	@Override
	public void saveLoginToken(Integer userId, DeviceInfo info) {
		Query<UserLoginLog> query = dfds.createQuery(UserLoginLog.class);
		query.filter("_id", userId);
		UpdateOperations<UserLoginLog> ops =dfds.createUpdateOperations(UserLoginLog.class);
		try {
			if (!StringUtil.isEmpty(info.getDeviceKey())) {
				ops.set("deviceMap." + info.getDeviceKey(), info);
			}
			dfds.update(query, ops);
			KSessionUtil.removeAndroidToken(userId);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
