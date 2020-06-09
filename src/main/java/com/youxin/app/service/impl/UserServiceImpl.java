package com.youxin.app.service.impl;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;
import com.youxin.app.entity.NearbyUser;
import com.youxin.app.entity.Report;
import com.youxin.app.entity.SdkLoginInfo;
import com.youxin.app.entity.User;
import com.youxin.app.entity.User.DeviceInfo;
import com.youxin.app.entity.User.LoginLog;
import com.youxin.app.entity.User.UserLoginLog;
import com.youxin.app.entity.User.UserSettings;
import com.youxin.app.entity.UserStatusCount;
import com.youxin.app.entity.UserVo;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.repository.UserRepository;
import com.youxin.app.service.ConfigService;
import com.youxin.app.service.UserService;
import com.youxin.app.utils.CollectionUtil;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.KSessionUtil;
import com.youxin.app.utils.Md5Util;
import com.youxin.app.utils.MongoOperator;
import com.youxin.app.utils.MongoUtil;
import com.youxin.app.utils.ReqUtil;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.ThreadUtil;
import com.youxin.app.utils.WXUserUtils;
import com.youxin.app.utils.sms.SMSServiceImpl;
import com.youxin.app.utils.supper.Callback;
import com.youxin.app.yx.SDKService;
import com.youxin.app.yx.request.Friends;
import com.youxin.app.yx.request.MsgRequest;
import com.youxin.app.yx.request.team.QueryDetail;






@Service
public class UserServiceImpl implements UserService {
	protected Log log=LogFactory.getLog(this.getClass());
	@Autowired
	private UserRepository repository;
	@Autowired
	private ConfigService cs;
	@Autowired
	@Qualifier("get")
	private Datastore dfds;

	@Autowired
	private SMSServiceImpl smsServer;

	@Autowired
	private ConfigService configService;
	
	@Autowired
	private RedissonClient redissonClient;

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
		String accid = Md5Util.md5HexToAccid(bean.getId() + "");
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
		bean.setCreateTime(DateUtil.currentTimeSeconds());
		bean.setUpdateTime(DateUtil.currentTimeSeconds());
		bean.setBalance(0.0);
		bean.setTotalConsume(0.0);
		bean.setTotalRecharge(0.0);
		// 保存本地数据库
		repository.save(bean);
		// 第三方注册绑定信息
		if(!StringUtil.isEmpty(bean.getLoginInfo())) {
			SdkLoginInfo findSdkLoginInfo = findSdkLoginInfo(bean.getSdkType(), bean.getLoginInfo());
			if(findSdkLoginInfo==null) {
				SdkLoginInfo sdkLoginInfo=new SdkLoginInfo();
				sdkLoginInfo.setCreateTime(DateUtil.currentTimeSeconds());
				sdkLoginInfo.setLoginInfo(bean.getLoginInfo());
				sdkLoginInfo.setType(bean.getSdkType());
				sdkLoginInfo.setUserId(bean.getId());
				sdkLoginInfo.setAccid(accid);
				dfds.save(sdkLoginInfo);
			}
		}
		
	
		ThreadUtil.executeInThread(new Callback() {
			@Override
			public void execute(Object obj) {
				// 加系统客服好友并发送消息
				try {
//					Friends friends = new Friends();
//					friends.setAccid(accid);
//					friends.setFaccid(Md5Util.md5HexToAccid("10000"));
//					friends.setType(1);
//					friends.setMsg("加客服好友");
//					SDKService.friendAdd(friends);
					
					MsgRequest messageBean = new MsgRequest();
					messageBean.setFrom(Md5Util.md5HexToAccid("10000"));
					messageBean.setType(0);// 文本
				
					messageBean.setOpe(0);// 个人消息
					messageBean.setTo(accid);
					
//					messageBean.setBody("{\"type\":"+KConstants.MsgType.TRANSFERRECIEVE+",\"data\":"+JSON.toJSONString(transfer)+"}");
					messageBean.setBody("{\"msg\":"+cs.getConfig().getRegNotice()+"}");
					try {
						JSONObject msgjson=SDKService.sendMsg(messageBean);
						if(msgjson.getInteger("code")!=200) 
							log.debug("注册成功发送消息失败");
					} catch (Exception e) {
						e.printStackTrace();
						log.debug("注册成功发送消息失败"+e.getMessage());
					}
				} catch (Exception e) {
					log.debug("加客服好友失败");
				}
				
			}
		});
		

		// 缓存用户token
		Map<String, Object> data = saveLoginInfo(bean);
		
		return data;
	}

//	@Override
//	public User getUser(String accid, String toaccid) {
//
//		User user = repository.findOne("accid", accid);
//		if (null == user) {
//			log.debug("accid为" + accid + "的用户不存在");
//			return null;
//		}
//		user.setEx("");
//		user.setPassword("");
//		if(StringUtil.isEmpty(user.getPayPassword())||"0".equals(user.getPayPassword())) 
//			user.setPayPassword("0");
//		else
//			user.setPayPassword("1");
//		user.setMobile(StringUtil.phoneEncryption(user.getMobile()));
//		return user;
//	}
	
	public User getUser(Integer userId) {
		// 先从 Redis 缓存中获取
		User user = KSessionUtil.getUserByUserId(userId);
		if (null == user) {
			user = repository.findOne("_id", userId);
			if (null == user) {
				log.debug("id为" + userId + "的用户不存在");
				return null;
			}
		
			KSessionUtil.saveUserByUserId(userId, user);
		}

		return user;
	}

	@Override
	public Map<String, Object> login(User bean) {
		Query<User> q = repository.createQuery();
		User user = null;
		if (!StringUtil.isEmpty(bean.getAccid()))
			user = repository.findOne("_id", bean.getId());
		else {
			user = repository.findOne("mobile", bean.getMobile());
			if(null == user) {
				if(!StringUtil.isEmpty(bean.getAccount()))
					user = q.field("account").containsIgnoreCase(bean.getAccount()).get();
//				user=repository.findOne("account", bean.getAccount());
			}
		}
		if (null == user) {
			throw new ServiceException(20004, "帐号不存在, 请注册!");
		} else {
			if(user.getIsDelUser()==1)
				throw new ServiceException(20012, "帐号已经注销!");
			if(user.getDisableUser()==-1)
				throw new ServiceException(20013, "当前账号存在违规行为，已被禁用!");
			user.setLoginLog(bean.getLoginLog());
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
			} else if (bean.getLoginType()==3) {
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
	@Override
	public long ipCount(String ip) {

		return repository.count("ip", ip);
	}
	@Override
	public long serialCount(String serial) {

		return repository.count("serial", serial);
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

//		log.debug(SDKService.getUinfos("['2dd0c6c424d1afbf925b8be4fbe85981']"));

//		User u=new User();
//		u.setAccid("assa");;
//		u.setName("123");
//		System.out.println(JSONObject.toJSON(u));
		User user=new User();
		System.out.println("1"+user.getLoginLog());
//		user.setLoginLog(null);
//		System.out.println("2"+user.getLoginLog());
		user.setLoginLog(new User.LoginLog());
		System.out.println("3"+user.getLoginLog());
	}

	

	@Override
	public String getUserName(Integer userId) {
		return this.getUser(userId).getName();
	}

	// 用户充值 type 1 充值 2 消费
	public  Double rechargeUserMoeny(Integer userId, Double money, int type) {
		RLock lock = redissonClient.getLock("user:money:"+userId);
		try {
			Query<User> q = repository.createQuery();
			q.field("_id").equal(userId);
			
			boolean tryLock = lock.tryLock(2, 10, TimeUnit.SECONDS);
			if(tryLock) {
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
					log.debug("余额不足");
					return 0.0;
				}
				ops.set("balance", Double.valueOf(df.format(dbUser.getBalance() - money)));
				ops.set("totalConsume", Double.valueOf(df.format(dbUser.getTotalConsume() + money)));
				user.setBalance(Double.valueOf(df.format(user.getBalance() - money)));
			}
			repository.update(q, ops);
			KSessionUtil.saveUserByUserId(userId, user);
			
			}else {
				log.debug("金额操作失败 用户id：" +userId+"金额："+money);
			}
			return q.get().getBalance();
		} catch (Exception e) {
			e.printStackTrace();
			return 0.0;
		}finally {
			lock.unlock();
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
		if (!StringUtil.isEmpty(example.getMobile())) {
			ref.put("settings.searchByMobile", 1);
			//允许手机号搜索
			ref.put("mobile", example.getMobile());
		}
			
		if (!StringUtil.isEmpty(example.getAccount())) {
			ref.put("settings.searchByAccount", 1);
			ref.put("account", Pattern.compile(example.getAccount(),Pattern.CASE_INSENSITIVE));
		}
			
		
		
		ref.put("disableUser", 1);
//		if (null != example.getStartTime())
//			ref.put("birthday", new BasicDBObject("$gte", example.getStartTime()));
//		if (null != example.getEndTime())
//			ref.put("birthday", new BasicDBObject("$lte", example.getEndTime()));
		DBObject fields = new BasicDBObject();
		fields.put("password", 0);
		fields.put("accid", 0);
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
			obj.put("id", obj.get("_id"));
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
				log.debug("id为" + userId + "的用户不存在");
				return null;
			}

			return user;
	}
	public String getAccid(Integer userId) {
		User user = repository.findOne("_id", userId);
		if (null == user) {
			log.debug("id为" + userId + "的用户不存在");
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
		User user = q.get();
		if(bean.getDisableUser()==-1 || bean.getDisableUser()==1) {
			user.setDisableUser(bean.getDisableUser());
			user.setDisableUserSign(bean.getDisableUserSign());
		}
		user.setExs();
		com.youxin.app.yx.request.User.User yuser=new com.youxin.app.yx.request.User.User();
		BeanUtils.copyProperties(user, yuser);
		yuser.setMobile(null);
		JSONObject json = SDKService.updateUinfo(yuser);
		if(json.getIntValue("code")==200) {
			Key<User> save = repository.save(user);
			//维护数据
			KSessionUtil.deleteUserByUserId(bean.getId());
			log.debug(save);
			log.info("修改用户disableuser更新到云信成功"+save);
		}else
			throw new ServiceException(0, "修改disableuser更新到云信失败");
		
		
	}
	

	
	@Override
	public User updateAccount(String account) {
		if(account.length()<4) {
			throw new ServiceException(0, "友讯号长度不能小于4");
		}
		
		Integer userId=ReqUtil.getUserId();
		User user = getUserFromDB(userId);
		if(!StringUtil.isEmpty(user.getAccount()) && user.getAccount().contains("毛主席")) {
			throw new ServiceException(0, "友讯号已经修改过");
		}
		if(getUserByAccount(account)!=null) {
			throw new ServiceException(0, "友讯号已存在");
		}
//		Query<User> q = repository.createQuery();
//		q.field("_id").equal(userId);
//		
//		UpdateOperations<User> ops = repository.createUpdateOperations();
//		ops.set("account", account+"毛主席");
		user.setAccount(account+"毛主席");
		user.setExs();
		com.youxin.app.yx.request.User.User yuser=new com.youxin.app.yx.request.User.User();
		BeanUtils.copyProperties(user, yuser);
		JSONObject json = SDKService.updateUinfo(yuser);
		if(json.getIntValue("code")==200) {
			Key<User> save = repository.save(user);
			log.debug(save);
			log.info("修改友讯号更新到云信成功"+save);
		}else
			throw new ServiceException(0, "修改友讯号更新到云信失败");
//		UpdateResults update = repository.update(q, ops);
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
		log.debug(user.toString());
		repository.save(user);
		
		User user2 = getUser(userId);
		user2.setSettings(settings);
		KSessionUtil.saveUserByUserId(userId, user2);
	}

	@Override
	public User getUserByMobile(String mobile) {
		User user = repository.findOne("mobile", mobile);
		if (null == user) {
			log.debug("mobile为" + mobile + "的用户不存在");
			return null;
		}

		return user;
	}
	
	
	public List<User> getUserByAccount(String account) {
		List<User> users = repository.createQuery().field("account").containsIgnoreCase(account).asList();
		if (CollectionUtil.isEmpty(users)) {
			log.debug("account为" + account + "的用户不存在");
			return null;
		}
		
		return users;
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
			log.debug("accid为" + accid + "的用户不存在");
			return null;
		}

		return user;
	}

	@Override
	public Map<String, Object> saveLoginInfo(User user) {
		// 获取上次登录日志
		User.LoginLog login = getLogin(user.getId());
		if(user.getLoginLog()==null) 
			user.setLoginLog(new User.LoginLog());
		// 1=没有设备号、2=设备号一致、3=设备号不一致
		int serialStatus=1;
		try {
			 serialStatus = null == login ? 1 : (user.getLoginLog().getSerial().equals(login.getSerial()) ? 2 : 3);
		} catch (Exception e) {
			log.debug("loginInfo"+login);
		}
		
		// 保存登录日志
		updateUserLoginLog(user.getId(), user);
		//更新用户在线状态
		updateUserByOnline(user.getId(), 1);
		
		Map<String, Object> data = KSessionUtil.loginSaveAccessToken(user.getId(), user.getId(), null);
//		Object token = data.get("access_token");
		data.put("serialStatus", serialStatus);
		data.put("id", user.getId());
		data.put("isDelUser", user.getIsDelUser());
		data.put("account", user.getAccount());
		data.put("name", user.getName());
		data.put("birth", user.getBirth());
		data.put("icon", user.getIcon());
		try {
			JSONObject refreshToken = SDKService.refreshToken(Md5Util.md5HexToAccid(user.getId().toString()));
			if(refreshToken.getIntValue("code")==200) {
				String token = refreshToken.getJSONObject("info").getString("token");
				data.put("token1", token);
				user.setToken(token);
			}
		} catch (Exception e) {
			log.debug(e.getMessage());
		}
		data.put("token", user.getToken());
		data.put("mobile", StringUtil.phoneEncryption(user.getMobile()));
		data.put("codeSign", user.getCodeSign());
		data.put("latitude", user.getLatitude());
		data.put("longitude", user.getLongitude());
		data.put("login", login);
		log.debug(data);
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
		if(bean.getCityId()!=null&&bean.getCityId()>0) {
			ops.set("cityId", bean.getCityId());
		}
		if(!StringUtil.isEmpty(bean.getMobile())) {
			//没有同步更新云信手机号
			ops.set("mobile", bean.getMobile());
		}
		if(bean.getAreaId()!=null&&bean.getAreaId()>0) {
			ops.set("areaId", bean.getAreaId());
		}
		if(bean.getCountryId()!=null&&bean.getCountryId()>0) {
			ops.set("countryId", bean.getCountryId());
		}
		if(bean.getProvinceId()!=null&&bean.getProvinceId()>0) {
			ops.set("provinceId", bean.getProvinceId());
		}
		if(!StringUtil.isEmpty(bean.getAddress())) {
			ops.set("address", bean.getAddress());
		}
		if(!StringUtil.isEmpty(bean.getCityName())) {
			ops.set("cityName", bean.getCityName());
		}
		if(!StringUtil.isEmpty(bean.getPassword())) {
			ops.set("password", bean.getPassword());
		}
		if(!StringUtil.isEmpty(bean.getPayPassword())) {
			ops.set("payPassword", bean.getPayPassword());
		}
		if(bean.getLatitude()>0) {
			ops.set("latitude", bean.getLatitude());
			ops.set("loc.lat", bean.getLatitude());
		}
		if(bean.getLongitude()>0) {
			ops.set("longitude", bean.getLongitude());
			ops.set("loc.lng", bean.getLongitude());
		}
		if(bean.getUpdateTime()!=null&&bean.getUpdateTime()>0) {
			ops.set("updateTime",DateUtil.currentTimeSeconds());
		}
		if(!StringUtil.isEmpty(bean.getAliUserId())) {
			ops.set("aliUserId", bean.getAliUserId());
		}
		if(!StringUtil.isEmpty(bean.getAliAppAuthToken())) {
			ops.set("aliAppAuthToken", bean.getAliAppAuthToken());
		}
		
		if(bean.getIsDelUser()==1||bean.getIsDelUser()==-1) {
			ops.set("isDelUser", bean.getIsDelUser());
		}
		
		repository.update(q, ops);
		KSessionUtil.saveUserByUserId(bean.getId(), q.get());
	}
	@Override
	public void updateUserByOnline(int id,int type) {
		//容错处理
		if(id<1000) {
			log.debug("用户信息更新失败");
			return;
		}
			
		Query<User> q = repository.createQuery();
		q.field("_id").equal(id);
		UpdateOperations<User> ops = repository.createUpdateOperations();
		ops.set("online",type);
		repository.update(q, ops);
		KSessionUtil.saveUserByUserId(id, q.get());
	}
	@Override
	public void updateUserOnlineByAccid(String accid,int type) {
			
		Query<User> q = repository.createQuery();
		q.field("accid").equal(accid);
		UpdateOperations<User> ops = repository.createUpdateOperations();
		ops.set("online",type);
		repository.update(q, ops);
		User user = q.get();
		KSessionUtil.saveUserByUserId(user.getId(), user);
	}
	
	@Override
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

		BasicDBObject loginLog = new BasicDBObject("isFirstLogin", 1);
		loginLog.put("loginTime", DateUtil.currentTimeSeconds());
		loginLog.put("apiVersion", example.getLoginLog().getApiVersion());
		loginLog.put("osVersion", example.getLoginLog().getOsVersion());
		loginLog.put("model", example.getLoginLog().getModel());
		loginLog.put("serial", example.getLoginLog().getSerial());
		loginLog.put("latitude", example.getLoginLog().getLatitude());
		loginLog.put("longitude", example.getLoginLog().getLongitude());
		loginLog.put("location", example.getLoginLog().getLocation());
		loginLog.put("address", example.getLoginLog().getAddress());
		values.put("loginLog", loginLog);

		dfds.getCollection(UserLoginLog.class).update(query, new BasicDBObject(MongoOperator.SET, values),
				true, false);

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
	public void updateLoginoutLogTime(int userId) {
		updateUserByOnline(userId, 0);
		DBObject query = new BasicDBObject("_id", userId);

		DBObject values = new BasicDBObject();
		DBObject object = dfds.getCollection(UserLoginLog.class).findOne(query);
		BasicDBObject loginLog = null;
		if (null == object || null == object.get("loginLog")) {
			values.put("_id", userId);

			loginLog = new BasicDBObject("isFirstLogin", 0);
			loginLog.put("offlineTime", DateUtil.currentTimeSeconds());
			values.put("loginLog", loginLog);
			dfds.getCollection(UserLoginLog.class).update(query, new BasicDBObject(MongoOperator.SET, values),
					true, false);
		} else {
			values.put("loginLog.offlineTime", DateUtil.currentTimeSeconds());
			dfds.getCollection(UserLoginLog.class).update(query, new BasicDBObject(MongoOperator.SET, values),
					true, false);

		}

	}
	@Override
	public void saveLoginToken(Integer userId, DeviceInfo info,LoginLog log) {
		ThreadUtil.executeInThread(new Callback() {
			@Override
			public void execute(Object obj) {
				Query<UserLoginLog> query = dfds.createQuery(UserLoginLog.class);
				query.filter("_id", userId);
				UpdateOperations<UserLoginLog> ops =dfds.createUpdateOperations(UserLoginLog.class);
				try {
					if (!StringUtil.isEmpty(info.getDeviceKey())) {
						ops.set("deviceMap." + info.getDeviceKey(), info);
					}
					LoginLog login = getLogin(userId);
					login.setIsFirstLogin(0);
					login.setLoginTime(DateUtil.currentTimeSeconds());
					login.setAddress(log.getAddress());
					login.setLatitude(log.getLatitude());
					login.setLongitude(log.getLongitude());
					ops.set("loginLog", login);
					dfds.update(query, ops);
					
					updateLoginLogTime(userId);
					/**
					 * 更新用户最后出现时间
					 */
					User user = new User();
					user.setId(userId);
					user.setLatitude(log.getLatitude());
					user.setLongitude(log.getLongitude());
					updateUserByEle(user);
					KSessionUtil.removeAndroidToken(userId);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
		

	}
	@Override
	public void report(Integer userId, Integer toUserId, int reason, Long roomId, String webUrl) {

		if (toUserId == null && roomId>0 && StringUtil.isEmpty(webUrl)) {
			throw new ServiceException(KConstants.ResultCode.ParamsAuthFail);
		}
		Report report = new Report();
		report.setUserId(userId);
		report.setToUserId(toUserId);
		report.setReason(reason);
		report.setRoomId(roomId);
		if (!StringUtil.isEmpty(webUrl))
			report.setWebUrl(webUrl);
		report.setWebStatus(1);
		report.setTime(DateUtil.currentTimeSeconds());
		dfds.save(report);

	}
	@Override
	public boolean checkReportUrlImpl(String webUrl) {
		try {
			URL requestUrl = new URL(webUrl);
			webUrl = requestUrl.getHost();
		} catch (Exception e) {
			throw new ServiceException("参数对应的URL格式错误");
		}
		log.debug("URL HOST :" + webUrl);
		List<Report> reportList = dfds.createQuery(Report.class).field("webUrl").contains(webUrl).asList();
		if (null != reportList && reportList.size() > 0) {
			reportList.forEach(report -> {
				if (null != report && -1 == report.getWebStatus())
					throw new ServiceException("该网页地址已被举报");
			});
		}
		return true;
	}

	@Override
	public Map<String, Object> getReport(int type, int sender, String receiver, int pageIndex, int pageSize) {
		Map<String, Object> dataMap = Maps.newConcurrentMap();
		List<Report> data = null;
		try {
			if (type == 0) {
				Query<Report> q = dfds.createQuery(Report.class);
				if (0 != sender)
					q.field("userId").equal(sender);
				if (!StringUtil.isEmpty(receiver)) {
					q.field("toUserId").equal(Long.valueOf(receiver));
				} else {
					q.field("toUserId").notEqual(0);
				}
				q.field("roomId").equal(0);
				q.order("-time");
				q.offset(pageSize * pageIndex);
				data = q.limit(pageSize).asList();
				for (Report report : data) {
					report.setUserName(getUserName((int) report.getUserId()));
					report.setToUserName(getUserName((int) report.getToUserId()));
					if (KConstants.ReportReason.reasonMap.containsKey(report.getReason()))
						report.setInfo(KConstants.ReportReason.reasonMap.get(report.getReason()));
					if (null == getUser(Integer.valueOf(String.valueOf(report.getToUserId()))))
						report.setToUserStatus(-1);
					else {
						Integer disableUser = getUser(Integer.valueOf(String.valueOf(report.getToUserId()))).getDisableUser();
						report.setToUserStatus(disableUser);
					}
				}
				dataMap.put("count", q.count());
			} else if (type == 1) {
				Query<Report> q =dfds.createQuery(Report.class);
				if (0 != sender)
					q.field("userId").equal(sender);
				if (!StringUtil.isEmpty(receiver))
					q.field("roomId").equal(receiver);
				q.field("roomId").notEqual(0);
				q.order("-time");
				q.offset(pageSize * pageIndex);
				for (Report report : q.asList()) {
					report.setUserName(getUserName((int) report.getUserId()));
					QueryDetail roomquery=new QueryDetail();
					roomquery.setTid(report.getRoomId());
					JSONObject teamQueryDetail=new JSONObject();
					try {
						 teamQueryDetail = SDKService.teamQueryDetail(roomquery);
					} catch (Exception e) {
						continue;
					}
					String tname = teamQueryDetail.getJSONObject("tinfo").getString("tname");
					report.setRoomName(tname);
					//创建者与管理员是否被禁言
					String mute = teamQueryDetail.getJSONObject("tinfo").getString("mute");
					Integer muteType = teamQueryDetail.getJSONObject("tinfo").getInteger("muteType");
//					String adminMute = teamQueryDetail.getJSONObject("tinfo").getJSONObject("admins").getString("mute");
					if(mute.equals("true")&&muteType==3) {
						report.setRoomStatus(-1);
					}else {
						report.setRoomStatus(1);
					}
					if (KConstants.ReportReason.reasonMap.containsKey(report.getReason()))
						report.setInfo(KConstants.ReportReason.reasonMap.get(report.getReason()));
				}
				data = q.limit(pageSize).asList();
				dataMap.put("count", q.count());
			} else if (type == 2) {
				Query<Report> q = dfds.createQuery(Report.class);
				if (0 != sender)
					q.field("userId").equal(sender);
				if (!StringUtil.isEmpty(receiver))
					q.field("webUrl").equal(receiver);
				q.field("webUrl").notEqual(null);
				q.field("toUserId").equal(0);
				q.order("-time");
				for (Report report : q.asList()) {
					report.setUserName(getUserName((int) report.getUserId()));
					if (KConstants.ReportReason.reasonMap.containsKey(report.getReason()))
						report.setInfo(KConstants.ReportReason.reasonMap.get(report.getReason()));
				}
				q.offset(pageSize * pageIndex);
				data = q.limit(pageSize).asList();
				dataMap.put("count", q.count());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		dataMap.put("data", data);
		return dataMap;
	}

	@Override
	public void delReport(Integer userId, String roomId) {
		Query<Report> query = dfds.createQuery(Report.class);
		if (null != userId)
			query.or(query.criteria("userId").equal(userId), query.criteria("toUserId").equal(userId));
		else if (null != roomId)
			query.field("roomId").equal(roomId);
		dfds.delete(query);
	}


	@SuppressWarnings("deprecation")
	@Override
	public List<User> nearbyUser(NearbyUser poi) {
		List<User> data = null;
		try {
			// 过滤隐私设置中关闭手机号和昵称搜索的用户
//			Query<User> q = getDatastore().createQuery(User.class).field("settings.phoneSearch").notEqual(0)
//					.field("settings.nameSearch").notEqual(0);
			Query<User> q = dfds.createQuery(User.class);
			q.and(q.criteria("settings.searchByMobile").notEqual(0),q.criteria("disableUser").notEqual(-1));
			
			if (null != poi.getSex())
				q.field("gender").equal(poi.getSex());
			q.disableValidation();
			int distance = poi.getDistance();
			Double d = 0d;
			if (0 == distance)
				distance = KConstants.LBS_DISTANCE;
			d = distance / KConstants.LBS_KM;// 0.180180180.....

			if (0 != poi.getLatitude() && 0 != poi.getLongitude())
				q.field("loc").near(poi.getLongitude(), poi.getLatitude(), d);
			else 
				return null;
			/*if (!StringUtil.isEmpty(poi.getNickname())) {
				Config config = configService.getConfig();
				if (0 == config.getTelephoneSearchUser()) { // 手机号搜索关闭

					if (0 == config.getNicknameSearchUser()) { // 昵称搜索关闭
						return null;
					} else if (1 == config.getNicknameSearchUser()) { // 昵称精准搜索
						// q.field("nickname").equal(poi.getNickname());
						q.criteria("nickname").equal(poi.getNickname());
					} else if (2 == config.getNicknameSearchUser()) { // 昵称模糊搜索
						q.criteria("nickname").containsIgnoreCase(poi.getNickname());
					}

				} else if (1 == config.getTelephoneSearchUser()) { // 手机号精确搜索

					if (0 == config.getNicknameSearchUser()) { // 昵称搜索关闭
						q.or(q.criteria("account").equal(poi.getNickname()),
								q.criteria("phone").equal(poi.getNickname()));

					} else if (1 == config.getNicknameSearchUser()) { // 昵称精准搜索
						q.or(q.criteria("account").equal(poi.getNickname()),
								q.criteria("phone").equal(poi.getNickname()),
								q.criteria("nickname").equal(poi.getNickname()));
					} else if (2 == config.getNicknameSearchUser()) { // 昵称模糊搜索
						q.or(q.criteria("account").equal(poi.getNickname()),
								q.criteria("phone").equal(poi.getNickname()),
								q.criteria("nickname").containsIgnoreCase(poi.getNickname()));
					}

				} else if (2 == config.getTelephoneSearchUser()) { // 手机号模糊搜索

					if (0 == config.getNicknameSearchUser()) { // 昵称搜索关闭
						q.or(q.criteria("account").equal(poi.getNickname()),
								q.criteria("phone").containsIgnoreCase(poi.getNickname()));
					} else if (1 == config.getNicknameSearchUser()) { // 昵称精准搜索
						q.or(q.criteria("account").equal(poi.getNickname()),
								q.criteria("phone").containsIgnoreCase(poi.getNickname()),
								q.criteria("nickname").equal(poi.getNickname()));
					} else if (2 == config.getNicknameSearchUser()) { // 昵称模糊搜索
						q.or(q.criteria("account").equal(poi.getNickname()),
								q.criteria("phone").containsIgnoreCase(poi.getNickname()),
								q.criteria("nickname").containsIgnoreCase(poi.getNickname()));
					}

				}

			} else if (0 == poi.getLatitude() && 0 == poi.getLongitude()) { // 搜索关键字为空，且坐标没传的情况下不返回数据
				return null;
			}
*/
			if (null != poi.getUserId()) {
				q.field("_id").equal(poi.getUserId());
			}
		
			if (null != poi.getActive() && 0 != poi.getActive()) {
				q.field("updateTime").greaterThanOrEq(DateUtil.currentTimeSeconds() - poi.getActive() * 86400000);
				q.field("updateTime").lessThanOrEq(DateUtil.currentTimeSeconds());

			}
			// 排除系统号
			q.field("_id").greaterThan(100050);
//			q.offset(poi.getPageIndex() * (poi.getPageSize())).limit(poi.getPageSize());
			System.out.println(q);
			data = q.asList(MongoUtil.pageFindOption(poi.getPageIndex(), poi.getPageSize()));
			data.forEach(item->
				System.out.println(item.getId())
			);
//			System.out.println(dat));
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;

	}
		
		@Override
		public List<Object> userOnlineStatusCount(String startDate, String endDate, short timeUnit) {

			List<Object> countData = new ArrayList<>();

			long startTime = 0; // 开始时间（秒）

			long endTime = 0; // 结束时间（秒）,默认为当前时间

			/**
			 * 如时间单位为月和天，默认开始时间为当前时间的一年前 ; 时间单位为小时，默认开始时间为当前时间的一个月前;
			 * 时间单位为分钟，则默认开始时间为当前这一天的0点
			 */
			long defStartTime = timeUnit == 4 ? DateUtil.getTodayMorning().getTime() / 1000
					: timeUnit == 3 ? DateUtil.getLastMonth().getTime() / 1000 : DateUtil.getLastYear().getTime() / 1000;

			startTime = StringUtil.isEmpty(startDate) ? defStartTime : DateUtil.toDate(startDate).getTime() / 1000;
			endTime = StringUtil.isEmpty(endDate) ? DateUtil.currentTimeSeconds()
					: DateUtil.toDate(endDate).getTime() / 1000;

			BasicDBObject queryTime = new BasicDBObject("$ne", null);

			if (startTime != 0 && endTime != 0) {
				queryTime.append("$gte", startTime);
				queryTime.append("$lt", endTime);
			}

			// 用户在线采样标识, 对应 UserStatusCount 表的type 字段 1零时统计 2:小时统计 3:天数统计
			short minute_sampling = 1, hour_sampling = 2, day_sampling = 3;

			BasicDBObject queryType = new BasicDBObject("$eq", day_sampling); // 默认筛选天数据

			if (1 == timeUnit) { // 月数据
				queryType.append("$eq", day_sampling);
			} else if (2 == timeUnit) {// 天数据
				queryType.append("$eq", day_sampling);
			} else if (timeUnit == 3) {// 小时数据
				queryType.append("$eq", hour_sampling);
			} else if (timeUnit == 4) {// 分钟数据
				queryType.append("$eq", minute_sampling);
			}

			BasicDBObject query = new BasicDBObject("time", queryTime).append("type", queryType);

			// 获得用户集合对象
			DBCollection collection = dfds.getCollection(UserStatusCount.class);

			String mapStr = "function Map() { " + "var date = new Date(this.time*1000);" + "var year = date.getFullYear();"
					+ "var month = (\"0\" + (date.getMonth()+1)).slice(-2);" // month 从0开始，此处要加1
					+ "var day = (\"0\" + date.getDate()).slice(-2);" + "var hour = (\"0\" + date.getHours()).slice(-2);"
					+ "var minute = (\"0\" + date.getMinutes()).slice(-2);" + "var dateStr = date.getFullYear()" + "+'-'+"
					+ "(parseInt(date.getMonth())+1)" + "+'-'+" + "date.getDate();";

			if (timeUnit == 1) { // counType=1: 每个月的数据
				mapStr += "var key= year + '-'+ month;";
			} else if (timeUnit == 2) { // counType=2:每天的数据
				mapStr += "var key= year + '-'+ month + '-' + day;";
			} else if (timeUnit == 3) { // counType=3 :每小时数据
				mapStr += "var key= year + '-'+ month + '-' + day + '  ' + hour +' : 00';";
			} else if (timeUnit == 4) { // counType=4 :每分钟的数据
				mapStr += "var key= year + '-'+ month + '-' + day + '  ' + hour + ':'+ minute;";
			}

			mapStr += "emit(key,this.count);}";

			String reduce = "function Reduce(key, values) {" + "return Array.sum(values);" + "}";
			MapReduceCommand.OutputType type = MapReduceCommand.OutputType.INLINE;//
			MapReduceCommand command = new MapReduceCommand(collection, mapStr, reduce, null, type, query);

			MapReduceOutput mapReduceOutput = collection.mapReduce(command);
			Iterable<DBObject> results = mapReduceOutput.results();
			Map<String, Object> map = new HashMap<String, Object>();
			for (Iterator iterator = results.iterator(); iterator.hasNext();) {
				DBObject obj = (DBObject) iterator.next();

				map.put((String) obj.get("_id"), obj.get("value"));
				countData.add(JSON.toJSON(map));
				map.clear();
				// System.out.println("=======>>>> 用户在线 "+JSON.toJSON(obj));

			}

			return countData;

		}
		
		
	
		@Override
		public List<Object> getUserRegisterCount(String startDate, String endDate, short timeUnit) {

			List<Object> countData = new ArrayList<>();

			long startTime = 0; // 开始时间（秒）

			long endTime = 0; // 结束时间（秒）,默认为当前时间

			/**
			 * 如时间单位为月和天，默认开始时间为当前时间的一年前 ; 时间单位为小时，默认开始时间为当前时间的一个月前;
			 * 时间单位为分钟，则默认开始时间为当前这一天的0点
			 */
			long defStartTime = timeUnit == 4 ? DateUtil.getTodayMorning().getTime() / 1000
					: timeUnit == 3 ? DateUtil.getLastMonth().getTime() / 1000 : DateUtil.getLastYear().getTime() / 1000;

			startTime = StringUtil.isEmpty(startDate) ? defStartTime : DateUtil.toDate(startDate).getTime() / 1000;
			endTime = StringUtil.isEmpty(endDate) ? DateUtil.currentTimeSeconds()
					: DateUtil.toDate(endDate).getTime() / 1000;

			BasicDBObject queryTime = new BasicDBObject("$ne", null);

			if (startTime != 0 && endTime != 0) {
				queryTime.append("$gt", startTime);
				queryTime.append("$lt", endTime);
			}

			BasicDBObject query = new BasicDBObject("createTime", queryTime);

			// 获得用户集合对象
			DBCollection collection = dfds.getCollection(User.class);

			String mapStr = "function Map() { " + "var date = new Date(this.createTime*1000);"
					+ "var year = date.getFullYear();" + "var month = (\"0\" + (date.getMonth()+1)).slice(-2);" // month
																												// 从0开始，此处要加1
					+ "var day = (\"0\" + date.getDate()).slice(-2);" + "var hour = (\"0\" + date.getHours()).slice(-2);"
					+ "var minute = (\"0\" + date.getMinutes()).slice(-2);" + "var dateStr = date.getFullYear()" + "+'-'+"
					+ "(parseInt(date.getMonth())+1)" + "+'-'+" + "date.getDate();";

			if (timeUnit == 1) { // counType=1: 每个月的数据
				mapStr += "var key= year + '-'+ month;";
			} else if (timeUnit == 2) { // counType=2:每天的数据
				mapStr += "var key= year + '-'+ month + '-' + day;";
			} else if (timeUnit == 3) { // counType=3 :每小时数据
				mapStr += "var key= year + '-'+ month + '-' + day + '  ' + hour +' : 00';";
			} else if (timeUnit == 4) { // counType=4 :每分钟的数据
				mapStr += "var key= year + '-'+ month + '-' + day + '  ' + hour + ':'+ minute;";
			}

			mapStr += "emit(key,1);}";

			String reduce = "function Reduce(key, values) {" + "return Array.sum(values);" + "}";
			MapReduceCommand.OutputType type = MapReduceCommand.OutputType.INLINE;//
			MapReduceCommand command = new MapReduceCommand(collection, mapStr, reduce, null, type, query);

			MapReduceOutput mapReduceOutput = collection.mapReduce(command);
			Iterable<DBObject> results = mapReduceOutput.results();
			Map<String, Double> map = new HashMap<String, Double>();
			for (Iterator iterator = results.iterator(); iterator.hasNext();) {
				DBObject obj = (DBObject) iterator.next();

				map.put((String) obj.get("_id"), (Double) obj.get("value"));
				countData.add(JSON.toJSON(map));
				map.clear();
				// System.out.println("=======>>>> 用户注册 "+JSON.toJSON(obj));

			}

			return countData;
		}

}
