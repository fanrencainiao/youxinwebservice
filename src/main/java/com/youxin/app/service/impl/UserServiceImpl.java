package com.youxin.app.service.impl;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.internal.operation.WriteConcernHelper;
import com.youxin.app.entity.User;
import com.youxin.app.entity.User.UserSettings;
import com.youxin.app.entity.UserVo;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.repository.UserRepository;
import com.youxin.app.service.UserService;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.KSessionUtil;
import com.youxin.app.utils.Md5Util;
import com.youxin.app.utils.ReqUtil;
import com.youxin.app.utils.ResultCode;
import com.youxin.app.utils.StringUtil;
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
		if (StringUtil.isEmpty(bean.getMobile())) {
			throw new ServiceException(0, "手机号必填");
		}
		if (StringUtil.isEmpty(bean.getSmsCode())) {
			throw new ServiceException(0, "短信验证码必填");
		}
		if (!smsServer.isAvailable("86" + bean.getMobile(), bean.getSmsCode()))
			throw new ServiceException("短信验证码不正确!");
		long mobileCount = this.mobileCount(bean.getMobile());
		if (mobileCount >= 1) {
			throw new ServiceException(0, "手机号已被注册");
		}
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
		// 加系统客服好友
		Friends friends = new Friends();
		friends.setAccid(accid);
		friends.setFaccid(Md5Util.md5Hex("10000"));
		friends.setType(1);
		friends.setMsg("加客服好友");
		SDKService.friendAdd(friends);

		// 缓存用户token
		Map<String, Object> data = KSessionUtil.loginSaveAccessToken(bean.getId(), bean.getId(), null);
		data.put("id", bean.getId());
		data.put("accid", bean.getAccid());
		data.put("account", bean.getAccount());
		data.put("name", bean.getName());
		data.put("birth", bean.getBirth());
		data.put("icon", bean.getIcon());
		data.put("token", bean.getToken());
		data.put("mobile", bean.getMobile());

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
		user.setPayPassword("");
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
			user.setEx("");
			user.setPassword("");
			user.setPayPassword("");
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

		}
		if (null == user) {
			throw new ServiceException(20004, "帐号不存在, 请注册!");
		} else {
			if (bean.getLoginType() == 0) {
				// 账号密码登录
				String password = bean.getPassword();
				if (!password.equals(user.getPassword()))
					throw new ServiceException(20002, "帐号或密码错误");
			} else if (1 == bean.getLoginType()) {
				// 短信验证码登录
				if (null == bean.getSmsCode())
					throw new ServiceException("短信验证码不能为空!");
				if (!smsServer.isAvailable("86" + user.getMobile(), bean.getSmsCode()))
					throw new ServiceException("短信验证码不正确!");
			} else {
				throw new ServiceException(20002, "登录方式错误");
			}

		}
		user.setEx("");
		user.setPassword("");
		user.setPayPassword("");
		KSessionUtil.saveUserByUserId(user.getId(), user);
		Map<String, Object> data = KSessionUtil.loginSaveAccessToken(user.getId(), user.getId(), null);
//		Object token = data.get("access_token");
		data.put("id", user.getId());
		data.put("accid", user.getAccid());
		data.put("account", user.getAccount());
		data.put("name", user.getName());
		data.put("birth", user.getBirth());
		data.put("icon", user.getIcon());
		data.put("token", user.getToken());
		data.put("mobile", user.getMobile());
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
			if (null == user)
				return 0.0;
			DecimalFormat df = new DecimalFormat("#.00");
			money = Double.valueOf(df.format(money));

			if (KConstants.MOENY_ADD == type) {
				ops.inc("balance", money);
				ops.inc("totalRecharge", money);
				user.setBalance(Double.valueOf(df.format(user.getBalance() + money)));
			} else {
				if (this.getUser(userId).getBalance() < money) {
					// 余额不足
					return 0.0;
				}
				ops.inc("balance", -money);
				ops.inc("totalConsume", money);
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

}
