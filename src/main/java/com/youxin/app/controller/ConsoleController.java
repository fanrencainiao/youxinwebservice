package com.youxin.app.controller;

import static org.mongodb.morphia.aggregation.Group.grouping;
import static org.mongodb.morphia.aggregation.Group.id;
import static org.mongodb.morphia.aggregation.Group.sum;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.codec.digest.DigestUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.aggregation.Accumulator;
import org.mongodb.morphia.aggregation.AggregationPipeline;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteResult;
import com.youxin.app.entity.Advert;
import com.youxin.app.entity.BankRecord;
import com.youxin.app.entity.Config;
import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.entity.HelpCenter;
import com.youxin.app.entity.IPDisable;
import com.youxin.app.entity.MessageReceive;
import com.youxin.app.entity.MongdbGroup;
import com.youxin.app.entity.Opinion;
import com.youxin.app.entity.PublicPermission;
import com.youxin.app.entity.RedPacket;
import com.youxin.app.entity.RedReceive;
import com.youxin.app.entity.Report;
import com.youxin.app.entity.Role;
import com.youxin.app.entity.SysApiLog;
import com.youxin.app.entity.Transfer;
import com.youxin.app.entity.User;
import com.youxin.app.entity.UserKeyWord;
import com.youxin.app.entity.RedPacket.SendRedPacket;
import com.youxin.app.entity.User.MyCard;
import com.youxin.app.entity.User.UserLoginLog;
import com.youxin.app.entity.group.TeamGroup;
import com.youxin.app.entity.msgbody.MsgBody;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.filter.LoginSign;
import com.youxin.app.repository.IPDisableRepository;
import com.youxin.app.repository.UserRepository;
import com.youxin.app.service.AdminConsoleService;
import com.youxin.app.service.ConfigService;
import com.youxin.app.service.IPDisableService;
import com.youxin.app.service.MessageReceiveService;
import com.youxin.app.service.PublicPermissionService;
import com.youxin.app.service.UserService;
import com.youxin.app.service.impl.ConsumeRecordManagerImpl;
import com.youxin.app.service.impl.RedPacketManagerImpl;
import com.youxin.app.service.impl.TransferManagerImpl;
import com.youxin.app.utils.BeanUtils;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.FileUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.Md5Util;
import com.youxin.app.utils.MongoUtil;
import com.youxin.app.utils.PageResult;
import com.youxin.app.utils.PageVO;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.ResultCode;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.ThreadUtil;
import com.youxin.app.utils.alipay.util.AliPayUtil;
import com.youxin.app.utils.jedis.RedisCRUD;
import com.youxin.app.utils.sms.SMSServiceImpl;
import com.youxin.app.utils.supper.Callback;
import com.youxin.app.yx.SDKService;
import com.youxin.app.yx.request.Msg;
import com.youxin.app.yx.request.MsgFile;
import com.youxin.app.yx.request.MsgRequest;
import com.youxin.app.yx.request.team.JoinTeams;
import com.youxin.app.yx.request.team.MuteTlistAll;
import com.youxin.app.yx.request.team.QueryDetail;
import com.youxin.app.yx.request.team.Remove;

@RestController
@RequestMapping("/console/")
public class ConsoleController extends AbstractController {
//	private static String RELURL="D:/pic";
//	private static String ADPATH="/ad";
	private static String RELURL = "/data/pic";
	private static String ADPATH = "/ad";
	@Autowired
	AdminConsoleService consoleService;

	@Autowired
	UserRepository ur;
	@Autowired
	UserService userService;
	@Autowired
	ConsumeRecordManagerImpl crm;
	@Autowired
	@Qualifier("get")
	private Datastore dfds;
	@Autowired
	RedPacketManagerImpl rpm;
	@Autowired
	ConfigService cs;
	@Autowired
	TransferManagerImpl tfm;
	@Autowired
	MessageReceiveService mrs;
	@Autowired
	private PublicPermissionService pps;
	@Autowired
	private SMSServiceImpl smsServer;
	@Autowired
	private RedisCRUD redisServer;
	@Autowired
	private IPDisableService ipds;

	@PostMapping(value = "login")
	public Object login(String name, String password, String imgCode, HttpServletRequest request) {
		User login = consoleService.login(name, password);
		boolean checkImgCode = smsServer.checkImgCode(name, imgCode);
		if (!checkImgCode)
			return Result.error("验证码错误");
		if (login != null) {
			request.getSession().setAttribute(LoginSign.LOGIN_USER_KEY, login);

			String s = JSONObject.toJSONString(login);
			JSONObject json = JSONObject.parseObject(s);
			json.put("role", 6);
			json.put("loginTime", login.getLoginLog().getLoginTime());
			json.put("createTime", login.getCreateTime());
			return Result.success(json);
		}
		return Result.failure(ResultCode.USER_LOGIN_ERROR);

	}

	@PostMapping(value = "updatePassword")
	public Object updatePassword(Integer userId, String password) {
		User ru = userService.getUserFromDB(userId);

		if (ru != null && ru.getUserType() == 6) {
			User nu = new User();
			nu.setPassword(password);
			nu.setId(userId);
			userService.updateUserByEle(nu);
			return Result.success();
		}
		return Result.failure(ResultCode.USER_NOT_LOGGED_IN);

	}

	@PostMapping(value = "logout")
	public Object logout(HttpServletRequest request) {
		request.getSession().removeAttribute(LoginSign.LOGIN_USER_KEY);
		System.out.println(request.getSession().getAttribute(LoginSign.LOGIN_USER_KEY));
		return Result.success();

	}

	@RequestMapping(value = { "navs" })
	public Object get(HttpServletRequest request, HttpServletResponse response) {
		DBCollection collection = dfds.getDB().getCollection("navs");
//		BasicDBObject q = new BasicDBObject();
		DBCursor find = collection.find();
		List<DBObject> list = find.toArray();
		DBObject dbObject = list.get(0);
		return dbObject;
	}

	@GetMapping(value = "userList")
	public Object userList(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit,
			@RequestParam(defaultValue = "") Integer online, @RequestParam(defaultValue = "") String keyWorld) {
		Query<User> query = ur.createQuery();

		if (!StringUtil.isEmpty(keyWorld)) {
			Integer userId = null;
			if (StringUtil.isNumeric(keyWorld) && keyWorld.length() < 10) {
				userId = Integer.valueOf(keyWorld);
			}

			query.or(query.criteria("name").containsIgnoreCase(keyWorld), query.criteria("_id").equal(userId),
					query.criteria("mobile").containsIgnoreCase(keyWorld),
					query.criteria("accid").containsIgnoreCase(keyWorld),
					query.criteria("account").containsIgnoreCase(keyWorld));
		}
		if (online != null) {
			if (online == 0 || online == 1) {
				query.filter("online =", online);
			}
			if (online == 2) {
				query.filter("isDelUser =", 1);
			}
			if (online == 3) {
				query.filter("isDelUser !=", 1);
			}

		}
		// 排序、分页
		List<User> pageData = query.order("-disableUser,-createTime").asList(MongoUtil.pageFindOption(page - 1, limit));
		pageData.forEach(userInfo -> {
			Query<UserLoginLog> loginLog = dfds.createQuery(UserLoginLog.class).field("userId").equal(userInfo.getId());
			if (null != loginLog.get())
				userInfo.setLoginLog(loginLog.get().getLoginLog());
		});
		PageResult<User> result = new PageResult<User>();
		result.setData(pageData);
		result.setCount(query.count());
		return Result.success(result);
	}

	/**
	 * 禁用 解禁用户
	 * 
	 * @param id
	 * @param accid
	 * @param disableUser
	 * @param disableUserSign 禁用标签
	 * @return
	 */
	@PostMapping("blockUser")
	public Object blockUser(@RequestParam(required = true) int id, @RequestParam(defaultValue = "") String accid,
			@RequestParam(required = true) int disableUser,@RequestParam("") String disableUserSign) {
		JSONObject block = null;
		if (StringUtil.isEmpty(accid)) {
			accid = Md5Util.md5HexToAccid(id + "");
		}
		User user = new User();
		if (disableUser == -1)
			block = SDKService.block(accid, "true");
		else if (disableUser == 1) {
			User userFromDB = userService.getUserFromDB(accid);
			Query<User> q = dfds.createQuery(User.class).field("mobile").equal(userFromDB.getMobile())
					.field("disableUser").equal(1);
			if (q.asList().size() > 0)
				return Result.error("该用户注册的手机号在系统已经存在未被禁用的账号，不可解禁");
			block = SDKService.unblock(accid);
		}

		else
			return Result.error();

		if (block.getIntValue("code") == 200) {
			user.setDisableUser(disableUser);
			user.setId(id);
			user.setAccid(accid);
			user.setDisableUserSign(disableUserSign);
			userService.updateUser(user);
			return Result.success();
		}

		return Result.error();
	}

	/**
	 * 获取用户信息
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "getUpdateUser")
	public Result updateUser(@RequestParam(defaultValue = "0") Integer userId) {
		User user = null;
		if (0 == userId)
			user = new User();
		else {
			user = userService.getUserFromDB(userId);

			Query<Role> q = dfds.createQuery(Role.class).field("userId").equal(userId);
			List<Role> userRoles = q.asList();
			System.out.println("用户角色：" + JSONObject.toJSONString(userRoles));
			if (null != userRoles) {
				for (Role role : userRoles) {
					if (role.getRole() == 2) {
						user.setUserType(2);
					} else {
						user.setUserType(0);
					}
				}
			}
		}
		return Result.success(user);
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @param userId
	 * @param example
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "updateUser")
	public Result saveUserMsg(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue = "") String accid, @ModelAttribute User example) throws Exception {
		if (!StringUtil.isEmpty(example.getMobile())) {
			example.setMobile(example.getMobile());
		}
		// 后台注册用户(后台注册传的密码没有加密，这里进行加密)
		if (!StringUtil.isEmpty(example.getPassword()))
			example.setPassword(DigestUtils.md5Hex(example.getPassword()));
		long mobileCount = userService.mobileCount(example.getMobile());
		// 保存到数据库
		if (StringUtil.isEmpty(accid) && (example.getId() == null || example.getId() <= 0)) {
			// 验证
			if (StringUtil.isEmpty(example.getMobile())) {
				throw new ServiceException(0, "手机号必填");
			}
			if (mobileCount >= 1) {
				throw new ServiceException(0, "手机号已被注册");
			}
			userService.register(example);

		} else {
			com.youxin.app.yx.request.User.User u = new com.youxin.app.yx.request.User.User();
			User userFromDB = userService.getUserFromDB(example.getId());
			if (userFromDB.getDisableUser() != 1)
				throw new ServiceException(0, "禁用的用户不可进行修改");
			if (StringUtil.isEmpty(example.getPassword()))
				example.setPassword(null);
			if (!userFromDB.getMobile().equals(example.getMobile()) && mobileCount >= 1) {
				throw new ServiceException(0, "手机号已被注册");
			}
			BeanUtils.copyProperties(example, userFromDB);
			BeanUtils.copyProperties(userFromDB, u);
			JSONObject updateUinfo = SDKService.updateUinfo(u);
			if (updateUinfo.getIntValue("code") == 200) {
				ur.save(userFromDB);
			} else {
				return Result.error();
			}
		}

		return Result.success();
	}

	/**
	 * 后台充值
	 * 
	 * @param money
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "recharge")
	public Result Recharge(Double money, int userId) throws Exception {
		// 核验用户是否存在
		if (null == userService.getUser(userId)) {
			return Result.error("充值失败, 用户不存在!");
		}

		String tradeNo = AliPayUtil.getOutTradeNo();

		Map<String, Object> data = Maps.newHashMap();
		// 创建充值记录
		ConsumeRecord record = new ConsumeRecord();
		record.setUserId(userId);
		record.setTradeNo(tradeNo);
		record.setMoney(Math.abs(money));
		record.setStatus(KConstants.OrderStatus.END);
		if (money >= 0) {
			record.setType(KConstants.ConsumeType.SYSTEM_RECHARGE);
			record.setDesc("后台余额充值");
		} else {
			record.setType(KConstants.ConsumeType.SYSTEM_REDUCE);
			record.setDesc("后台余额扣除");
		}
		record.setPayType(KConstants.PayType.SYSTEMPAY); // type = 3 ：管理后台充值
		record.setTime(DateUtil.currentTimeSeconds());
		crm.saveConsumeRecord(record);
		try {
			Double balance = 0.0;
			if (money >= 0)
				balance = userService.rechargeUserMoeny(userId, Math.abs(money), KConstants.MOENY_ADD);
			else
				balance = userService.rechargeUserMoeny(userId, Math.abs(money), KConstants.MOENY_REDUCE);
			data.put("balance", balance);
			return Result.success(data);
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}

	}

	/**
	 * 用户所在群
	 * 
	 * @param accid
	 * @param page
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	@GetMapping(value = "userTeam")
	public Object userTeam(@RequestParam String accid, int page, int limit) throws Exception {
		try {
			if (accid.length() < 10) {
				accid = Md5Util.md5HexToAccid(accid);
			}
			// 核验用户是否存在
			if (null == userService.getUserFromDB(accid)) {
				return Result.error("用户不存在!");
			}
			JoinTeams joinTeams = new JoinTeams();
			joinTeams.setAccid(accid);
			JSONObject ttjson = SDKService.teamJoinTeams(joinTeams);
			if (ttjson.getIntValue("code") == 200) {
				JSONArray jsonArray = ttjson.getJSONArray("infos");
//				List<Object> res=new ArrayList<>();
//				for (int i = 0; i < jsonArray.size(); i++) {
//					Object object = jsonArray.get(i);
//					res.add(object);
//				}
				PageResult<Object> pageResult = new PageResult<>(Optional.ofNullable(jsonArray).orElse(new JSONArray()),
						ttjson.getIntValue("count"));
				return Result.success(pageResult);
			} else {
				return Result.error("sdk查询出错");
			}

		} catch (Exception e) {
			return Result.error(e.getMessage());
		}

	}

	/**
	 * 群信息与成员列表
	 * 
	 * @param tid    群id
	 * @param action 行为 0 查询 ，1全部禁言
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "teamUserList")
	public Object teamUserList(@RequestParam Long tid, @RequestParam int action) throws Exception {
		// isAdmin 0普通成员，1管理员，2群主
		try {
			User user = new User();
			QueryDetail q = new QueryDetail();
			q.setTid(tid);
			JSONObject ttjson = SDKService.teamQueryDetail(q);
			if (ttjson.getIntValue("code") == 200) {
				JSONObject json = ttjson.getJSONObject("tinfo");
				JSONObject owner = json.getJSONObject("owner");
				owner.put("isAdmin", 2);
				JSONArray admins = json.getJSONArray("admins");
				for (int i = 0; i < admins.size(); i++) {
					admins.getJSONObject(i).put("isAdmin", 1);
				}
				JSONArray members = json.getJSONArray("members");
				admins.add(0, owner);
				admins.addAll(members);
				// 全体成员禁用
				if (action == 1) {
					for (int i = 0; i < admins.size(); i++) {
						String accid = admins.getJSONObject(i).getString("accid");
						// 云信禁用
						SDKService.block(accid, "true");
						// 无论云信禁用是否成功 本系统都要禁用
						user.setDisableUser(-1);
						Integer userId = Md5Util.accidToUserId(accid);
						if(userId==0)
							log.debug("全体群成员禁用，系统accidToUserid,转换失败");
						user.setId(userId);
						user.setAccid(accid);
						user.setDisableUserSign("全体群成员禁用");
						userService.updateUser(user);
					}
					return Result.success();
				}
				PageResult<Object> pageResult = new PageResult<>(admins, admins.size());
				return Result.success(pageResult);
			} else {
				return Result.error("sdk查询出错");
			}

		} catch (Exception e) {
			return Result.error(e.getMessage());
		}

	}

	/**
	 * 用户账单
	 * 
	 * @param userId
	 * @param page
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	@GetMapping(value = "userBill")
	public Object userBill(@RequestParam int userId, int page, int limit) throws Exception {
		try {

			// 核验用户是否存在
			if (null == userService.getUserFromDB(userId)) {
				return Result.error("用户不存在!");
			}
			PageResult<ConsumeRecord> result = crm.consumeRecordListByBill(userId, page - 1, limit);
			User userFromDB = userService.getUserFromDB(userId);
			result.setTotal(userFromDB.getBalance());
			return Result.success(result);

		} catch (Exception e) {
			return Result.error(e.getMessage());
		}

	}

	/**
	 * @Description:（红包记录）
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 **/
	@GetMapping("/redPacketList")
	public Object getRedPacketList(@RequestParam(defaultValue = "0") int payType,
			@RequestParam(defaultValue = "") String userName, @RequestParam(defaultValue = "0") int userId,
			@RequestParam(defaultValue = "0") int toUserId, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int limit) {
		try {
			PageResult<RedPacket> result = rpm.getRedPacketList(payType, userName, userId, toUserId, page - 1, limit);
			return Result.success(result);
		} catch (ServiceException e) {
			return Result.error(e.getErrMessage());
		}
	}

	@RequestMapping("receiveWater")
	public Object receiveWater(@RequestParam(defaultValue = "") String redId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit) {
		try {
			PageResult<RedReceive> result = rpm.receiveWater(redId, page - 1, limit);
			return Result.success(result);
		} catch (ServiceException e) {
			return Result.error(e.getErrMessage());
		}
	}

	@RequestMapping(value = "config")
	public Object getConfig() {
		Config config = cs.getConfig();
		return Result.success(config);
	}

	// 设置配置
	@RequestMapping(value = "/config/set", method = RequestMethod.POST)
	public Object setConfig(@ModelAttribute Config config) throws Exception {
		try {
			Config config2 = cs.getConfig();
			cs.setConfig(config);

			if (checkSendMsg(config, config2)) {
				// 批量发送自定义消息
//				sendBatchConfig(config);
				// 发送广播消息
				JSONObject json = SDKService.broadcastMsg(
						JSON.toJSONString(new MsgBody(0, KConstants.MsgType.MONEYCONFIG, config)),
						Md5Util.md5HexToAccid("10000"), "true", 7, "");
				if (json.getIntValue("code") != 200) {
					log.debug("广播消息发送失败");
					return Result.success("广播消息发送失败");
				}

			}

			return Result.success();
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}
	}

	private void sendBatchConfig(Config config) {
		Query<User> q = ur.createQuery();
		q.field("disableUser").notEqual(-1).field("isDelUser").notEqual(1);
		Double count = (double) q.count();
		int ceil = (int) Math.ceil(count / 500.00);
		// 分页查询用户进行消息发送
		for (int i = 0; i < ceil; i++) {
			List<User> asList = q.asList(MongoUtil.pageFindOption(i, 500));
			List<String> accids = asList.stream().map(User::getAccid).collect(Collectors.toList());
			Msg msg = new Msg();
			msg.setBody(JSON.toJSONString(new MsgBody(0, KConstants.MsgType.MONEYCONFIG, config)));
			msg.setFrom(Md5Util.md5HexToAccid("10000"));
			msg.setTo(JSON.toJSONString(accids));
			msg.setType(100);// 文本
			int j = i;
			ThreadUtil.executeInThread(new Callback() {
				@Override
				public void execute(Object obj) {
					JSONObject json = SDKService.sendBatchMsg(msg);
					if (json.getIntValue("code") == 200) {
						log.debug("配置更改消息发送成功，第" + (j + 1) + "页");
					}
				}
			});
		}
		log.debug("总计" + count + "人");
	}

	private boolean checkSendMsg(Config config, Config config2) {
		if (config2.getAliState() != config.getAliState() || config2.getWxState() != config.getWxState()
				|| config2.getYeeState() != config.getYeeState()
				|| config2.getRedPacketState() != config.getRedPacketState()
				|| config2.getAliRedPacketState() != config.getAliRedPacketState()
				|| config2.getTransferState() != config.getTransferState()
				|| config2.getBankState() != config.getBankState()
				|| config2.getRrShopState() != config.getRrShopState()
				|| config2.getCodeReceiveState() != config.getCodeReceiveState()
				|| config2.getMoneyState() != config.getMoneyState()
				|| config2.getAliCodeState() != config.getAliCodeState()
				|| !config2.getAndroidVersion().equals(config.getAndroidVersion())
				|| !config2.getIosVersionDisable().equals(config.getIosVersionDisable()))
			return true;
		else
			return false;
	}

	/**
	 * 得到银行卡提现记录
	 * 
	 * @param bankCard
	 * @param page
	 * @param limit
	 * @return
	 */
	@RequestMapping("/getBankList")
	public Object getBankList(@RequestParam(defaultValue = "") String bankCard,
			@RequestParam(defaultValue = "0") Integer userId, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int limit) {
		try {
			PageResult<BankRecord> result = consoleService.getBankRecordList(bankCard, userId, page - 1, limit);
			return Result.success(result);
		} catch (ServiceException e) {
			return Result.error(e.getErrMessage());
		}
	}

	/**
	 * 完成提现
	 * 
	 * @param bankCard
	 * @param page
	 * @param limit
	 * @return
	 */
	@RequestMapping("/updateStatus")
	public Object updateStatus(@RequestParam Integer status, @RequestParam String id) {
		try {
			MsgRequest messageBean = null;
			int UserId = 1100;
			User admin = userService.getUserFromDB(UserId);
//			PageResult<BankRecord> result = SKBeanUtils.getAdminManager().getBankRecordList(bankCard, page, limit);

			Query<BankRecord> query = dfds.createQuery(BankRecord.class).field("_id").equal(new ObjectId(id));
			BankRecord bankRecord = query.get();
			if (null == bankRecord)
				return Result.error("数据系统出错");
			UpdateOperations<BankRecord> ops = dfds.createUpdateOperations(BankRecord.class);
			if (status == 1 || status == 0) {
				long currentTimeSeconds = DateUtil.currentTimeSeconds();
				ops.set("status", status);
				ops.set("payTime", currentTimeSeconds);
				dfds.update(query, ops);
				String card = bankRecord.getBankCard();

				List<MyCard> tocard = dfds.createQuery(MyCard.class).field("bankCard").equal(card).asList();
				messageBean = new MsgRequest();
				messageBean.setType(100);

				BankRecord sendReulst = new BankRecord();
				sendReulst.setTotalFee(bankRecord.getTotalFee());
				sendReulst.setFee(bankRecord.getFee());
				sendReulst.setRealFee(bankRecord.getRealFee());
				sendReulst.setPayTime(currentTimeSeconds);
				sendReulst.setBankCard(card.substring(card.length() - 4));
				sendReulst.setBankName(tocard.get(0).getBankName());
				sendReulst.setDes("预计2小时内到账，请注意查收!");

				messageBean.setFrom(admin.getAccid());

				messageBean.setOpe(0);// 个人消息
				messageBean.setTo(Md5Util.md5HexToAccid(bankRecord.getUserId() + ""));
				messageBean.setBody(JSON.toJSONString(new MsgBody(0, KConstants.MsgType.BANKOVERMONEY, sendReulst)));
				try {
					JSONObject json = SDKService.sendMsg(messageBean);
					if (json.getInteger("code") != 200)
						log.debug("银行卡提现 sdk消息发送失败");
					else
						log.debug("银行卡提现 sdk消息发送失败");
				} catch (Exception e) {
					e.printStackTrace();
					log.debug("银行卡提现 sdk消息发送失败" + e.getMessage());
				}
			} else if (status == 3) {
				ops.set("status", status);
				dfds.update(query, ops);
			}

			return Result.success();
		} catch (ServiceException e) {
			return Result.error(e.getErrMessage());
		}
	}

	/**
	 * @Description:（被举报的用户和群组列表）
	 * @param type     (type = 0查询被举报的用户,type=1查询被举报的群主,type=2查询被举报的网页)
	 * @param pageSize
	 * @return
	 **/
	@SuppressWarnings("static-access")
	@RequestMapping(value = "/beReport")
	public Object beReport(@RequestParam(defaultValue = "0") int type, @RequestParam(defaultValue = "0") int sender,
			@RequestParam(defaultValue = "") String receiver, @RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "25") int pageSize) {
		Map<String, Object> dataMap = Maps.newConcurrentMap();
		Result Result = new Result();
		try {
			dataMap = userService.getReport(type, sender, receiver, pageIndex, pageSize);
			log.debug("举报详情：" + JSONObject.toJSONString(dataMap.get("data")));
			if (!dataMap.isEmpty()) {
				List<Report> reportList = (List<Report>) dataMap.get("data");
				long total = (long) dataMap.get("count");
				return Result.success(new PageVO(reportList, total, pageIndex, pageSize, total));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
		return Result;

	}

	@RequestMapping("/isLockRoom")
	public Result isLockRoom(@RequestParam(defaultValue = "") String roomId,
			@RequestParam(defaultValue = "-1") int roomStatus) {
		if (StringUtil.isEmpty(roomId))
			return Result.error("room is null");
//		Query<Report> query = dfds.createQuery(Report.class).field("roomId").equal(Long.valueOf(roomId));
//		if (null == query.get())
//			return Result.error("暂无该链接的举报数据");
		// 禁言所有群成员
		// 查询群详细信息
		QueryDetail roomquery = new QueryDetail();
		roomquery.setTid(Long.valueOf(roomId));
		JSONObject eqd = SDKService.teamQueryDetail(roomquery);
		String oaccid = eqd.getJSONObject("tinfo").getJSONObject("owner").getString("accid");
		// 禁言
		MuteTlistAll ma = new MuteTlistAll();
		ma.setOwner(oaccid);
		ma.setTid(roomId);
		if (roomStatus == 1)
			ma.setMuteType(0);
		else
			ma.setMuteType(3);
		JSONObject json = SDKService.teamMuteTlistAll(ma);
		if (json.getIntValue("code") == 200) {
//			UpdateOperations<Report> ops = dfds.createUpdateOperations(Report.class);
//			ops.set("roomStatus", roomStatus);
//			dfds.update(query, ops);
			return Result.success(json);
		}

		return Result.error("失败");
	}

	/**
	 * 解散群
	 * 
	 * @param roomId
	 * @param owner
	 * @return
	 */
	@RequestMapping("/deleteRoom")
	public Result deleteRoom(@RequestParam(defaultValue = "") String roomId,
			@RequestParam(defaultValue = "") String owner) {
		if (StringUtil.isEmpty(roomId))
			return Result.error("room is null");
		if (StringUtil.isEmpty(owner))
			return Result.error("owner is null");

		Remove remove = new Remove();
		remove.setOwner(owner);
		remove.setTid(roomId);
		JSONObject json = SDKService.teamRemove(remove);
		if (json.getIntValue("code") == 200) {
			return Result.success(json);
		}

		return Result.error("失败");
	}

	@RequestMapping("/isLockWebUrl")
	public Result isLockWebUrl(@RequestParam(defaultValue = "") String webUrlId,
			@RequestParam(defaultValue = "-1") int webStatus) {
		if (StringUtil.isEmpty(webUrlId))
			return Result.error("webUrl is null");
		Query<Report> query = dfds.createQuery(Report.class).field("_id").equal(new ObjectId(webUrlId));
		if (null == query.get())
			return Result.error("暂无该链接的举报数据");
		UpdateOperations<Report> ops = dfds.createUpdateOperations(Report.class);
		ops.set("webStatus", webStatus);
		dfds.update(query, ops);
		return Result.success();
	}

	/**
	 * 删除举报
	 * 
	 * @param response
	 * @param id
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/deleteReport")
	public Result deleteReport(@RequestParam String id) {
//		BasicDBObject query = new BasicDBObject("_id", parse(id));
//		dfds.getDB().getCollection("Report").remove(query);
		Query<Report> q = dfds.createQuery(Report.class).field("_id").equal(parse(id));
		dfds.delete(q);
		return Result.success();
	}

	/**
	 * 意见查询
	 * 
	 * @param opinion
	 * @return
	 */
	@RequestMapping(value = "/opinionList")
	public Object opinionList(@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "15") int limit) {
		PageResult<Opinion> result = new PageResult<>();
		Query<Opinion> createQuery = dfds.createQuery(Opinion.class);
		createQuery.order("state,-createTime");
		result.setCount(createQuery.count());
		result.setData(createQuery.asList(MongoUtil.pageFindOption(page - 1, limit)));
		return Result.success(result);

	}

	/**
	 * 意见状态修改
	 * 
	 * @param opinion
	 * @return
	 */
	@RequestMapping(value = "/overOpinion")
	public Object overOpinion(@RequestParam(defaultValue = "0") int state, @RequestParam(defaultValue = "") String id) {
		if (StringUtil.isEmpty(id))
			return Result.error("id不能为空");
		UpdateOperations<Opinion> uo = dfds.createUpdateOperations(Opinion.class);
		uo.set("state", state);
		uo.set("updateTime", DateUtil.currentTimeSeconds());
		UpdateResults update = dfds.update(dfds.createQuery(Opinion.class).field("_id").equal(parse(id)), uo);
		return Result.success(update);
	}

	/**
	 * sdk图片上传
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/uploadSdkImage")
	public Object uploadSdkImage(@RequestParam(value = "file", required = false) MultipartFile file) throws Exception {
		String fileName = file.getOriginalFilename();// 获取文件名加后缀
		if (StringUtil.isEmpty(fileName))
			return Result.layuieditimg(-1, "图片为空", "", "");
		File multipartFileToFile = FileUtil.multipartFileToFile(file);
		if (multipartFileToFile == null) {
			return Result.layuieditimg(-1, "转file失败", "", "");
		}
		String base64 = FileUtil.base64(multipartFileToFile);
		if (base64 == null) {
			return Result.layuieditimg(-1, "转base64失败", "", "");
		}

		MsgFile mf = new MsgFile();
		System.out.println(base64);
		mf.setContent(base64);
//		SDKService.fileUpload(mf);
		mf.setType("1");
		// 50年
		mf.setExpireSec(3600 * 24 * 30 * 12 * 50 + "");
		mf.setTag("图文");
		JSONObject upload = SDKService.upload(mf);
		System.out.println(upload);
		String url = upload.getString("url");
		return Result.layuieditimg(0, "成功", url, url);

	}

	/**
	 * 帮助中心列表
	 * 
	 * @param pageIndex
	 * @param pageSize
	 * @param type
	 * @param state
	 * @param nickName
	 * @return
	 */
	@RequestMapping(value = "/helpCenterList")
	public Object helpCenterList(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "25") int limit, @RequestParam(defaultValue = "0") int type,
			@RequestParam(defaultValue = "-2") int state, @RequestParam(defaultValue = "") String nickName) {
		PageResult<HelpCenter> result = new PageResult<>();
		Query<HelpCenter> q = dfds.createQuery(HelpCenter.class);
		if (type > 0)
			q.field("type").equal(type);

		if (state == 0)
			q.field("state").equal(state);
		else if (state == 1)
			q.filter("state in", new Integer[] { -1, 1 });
		if (!StringUtil.isEmpty(nickName))
			q.field("title").contains(nickName);
		q.order("-createTime,-updateTime");
		result.setCount(q.count());
		result.setData(q.asList(MongoUtil.pageFindOption(page - 1, limit)));
		return Result.success(result);
	}

	/**
	 * 获取帮助实体
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/getCenterList")
	public Object getCenterList(@RequestParam(defaultValue = "") String id) {
		if (StringUtil.isEmpty(id)) {
			return Result.error("id为空");
		}
		Query<HelpCenter> q = dfds.createQuery(HelpCenter.class).field("_id").equal(parse(id));

		return Result.success(q.get());
	}

	/**
	 * 修改或者保存帮助实体
	 * 
	 * @param hcid
	 * @param hc
	 * @return
	 */
	@RequestMapping(value = "/saveCenterList")
	public Object saveCenterList(@RequestParam(defaultValue = "") String hcid, @ModelAttribute HelpCenter hc) {
		if (StringUtil.isEmpty(hcid)) {
			hc.setCreateTime(DateUtil.currentTimeSeconds());
			hc.setState(-1);
		} else {
			Query<HelpCenter> q = dfds.find(HelpCenter.class);
			q.field("_id").equal(parse(hcid));
			HelpCenter helpCenter = q.get();
			hc.setCreateTime(helpCenter.getCreateTime());
			hc.setUpdateTime(DateUtil.currentTimeSeconds());
			hc.setId(parse(hcid));
		}
		Key<HelpCenter> save = dfds.save(hc);
		return Result.success(save);
	}

	@RequestMapping(value = "/delCenterList")
	public Object delCenterList(@RequestParam(defaultValue = "") String id) {
		if (StringUtil.isEmpty(id)) {
			return Result.error("id为空");
		}
		String[] ids = StringUtil.getStringList(id, ",");
		for (String idd : ids) {
			Query<HelpCenter> q = dfds.createQuery(HelpCenter.class).field("_id").equal(parse(idd));
			dfds.delete(q);
		}

		return Result.success();
	}

	/**
	 * 广告列表
	 * 
	 * @param pageIndex
	 * @param pageSize
	 * @param type
	 * @param state
	 * @param nickName
	 * @return
	 */
	@RequestMapping(value = "/adList")
	public Object adList(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int limit,
			@RequestParam(defaultValue = "0") int type, @RequestParam(defaultValue = "0") int state,
			@RequestParam(defaultValue = "") String nickName) {
		PageResult<Advert> result = new PageResult<>();
		Query<Advert> q = dfds.createQuery(Advert.class);
		if (type > 0)
			q.field("type").equal(type);
		if (state != 0)
			q.field("state").equal(state);
		if (!StringUtil.isEmpty(nickName))
			q.field("title").contains(nickName);
		q.order("-createTime,-updateTime");
		result.setCount(q.count());
		result.setData(q.asList(MongoUtil.pageFindOption(page - 1, limit)));
		return Result.success(result);
	}

	/**
	 * 获取广告实体
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/getAd")
	public Object getAd(@RequestParam(defaultValue = "") String id) {
		if (StringUtil.isEmpty(id)) {
			return Result.error("id为空");
		}
		Query<Advert> q = dfds.createQuery(Advert.class).field("_id").equal(parse(id));

		return Result.success(q.get());
	}

	/**
	 * 修改或者保存广告
	 * 
	 * @param hcid
	 * @param hc
	 * @return
	 */
	@RequestMapping(value = "/saveAd")
	public Object saveAd(@ModelAttribute Advert advert,
			@RequestParam(value = "file", required = false) MultipartFile file, HttpServletRequest request) {
		if (StringUtil.isEmpty(advert.getCid())) {
			advert.setCreateTime(DateUtil.currentTimeSeconds());
//			advert.setState(1);
		} else {
			Query<Advert> q = dfds.find(Advert.class);
			q.field("_id").equal(parse(advert.getCid()));
			Advert advert1 = q.get();
			advert.setCreateTime(advert1.getCreateTime());
			advert.setUpdateTime(DateUtil.currentTimeSeconds());
			advert.setId(parse(advert.getCid()));
		}
		Key<Advert> save = dfds.save(advert);
		// 贷款图标
		String uploadPicture = FileUtil.uploadPicture(file, RELURL + ADPATH, ADPATH, save.getId().toString(), request);
		if (uploadPicture != null) {
			advert.setImg(uploadPicture);
			dfds.save(advert);
			return Result.success();
		} else {
			if (StringUtil.isEmpty(advert.getCid())) {
				dfds.delete(advert);
				return Result.error("图片上传失败，请重试");
			} else
				return Result.success();

		}

	}

	/**
	 * 删除广告
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/delAd")
	public Object delAd(@RequestParam(defaultValue = "") String id) {
		if (StringUtil.isEmpty(id)) {
			return Result.error("id为空");
		}
		String[] ids = StringUtil.getStringList(id, ",");
		for (String idd : ids) {
			Advert ad = dfds.createQuery(Advert.class).field("_id").equal(parse(idd)).get();
			dfds.delete(ad);
			// 存在url进行图片删除
			if (!StringUtil.isEmpty(ad.getImg())) {
				String fileF = ad.getImg().substring(ad.getImg().lastIndexOf("."), ad.getImg().length());// 文件后缀
				FileUtil.delFile(new File(RELURL + ADPATH + "/" + idd + fileF));
			}
		}
		return Result.success();
	}

	@RequestMapping(value = "/userKeyWordList")
	public Object userKeyWordList(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "25") int limit, @RequestParam(defaultValue = "") String keyWord,
			@RequestParam(defaultValue = "") String accid) {
		PageResult<UserKeyWord> result = new PageResult<>();
		Query<UserKeyWord> q = dfds.createQuery(UserKeyWord.class);
		if (!StringUtil.isEmpty(keyWord))
			q.field("keyWord").contains(keyWord);
		if (!StringUtil.isEmpty(accid))
			q.field("accid").contains(accid);
		q.order("-time");
		result.setCount(q.count());
		result.setData(q.asList(MongoUtil.pageFindOption(page - 1, limit)));
		return Result.success(result);
	}

	/**
	 * 删除记录
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/delUserKeyWord")
	public Object delUserKeyWord(@RequestParam(defaultValue = "") String id) {
		if (StringUtil.isEmpty(id)) {
			return Result.error("id为空");
		}
		String[] ids = StringUtil.getStringList(id, ",");
		for (String idd : ids) {
			UserKeyWord ad = dfds.createQuery(UserKeyWord.class).field("_id").equal(parse(idd)).get();
			dfds.delete(ad);
		}
		return Result.success();
	}

	/**
	 * @Description:（系统充值记录）
	 * @param userId
	 * @param status
	 * @param type
	 * @return
	 **/
	@RequestMapping("/systemRecharge")
	public Object systemRecharge(@RequestParam(defaultValue = "0") int userId,
			@RequestParam(defaultValue = "0") int type, @RequestParam(defaultValue = "0") int payType,
			@RequestParam(defaultValue = "") String desc, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "") String startDate,
			@RequestParam(defaultValue = "") String endDate) {
		try {
			PageResult<ConsumeRecord> result = crm.recharge(userId, type, payType, desc, page - 1, limit, startDate,
					endDate);
			return Result.success(result);
		} catch (ServiceException e) {
			return Result.error(e.getErrMessage());
		}
	}

	/**
	 * 付款记录
	 * 
	 * @param userId
	 * @param page
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/paymentCodeList")
	public Object paymentCodeList(@RequestParam(defaultValue = "0") int userId,
			@RequestParam(defaultValue = "0") int type, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int limit, @RequestParam(defaultValue = "") String startDate,
			@RequestParam(defaultValue = "") String endDate) {
		PageResult<ConsumeRecord> result = crm.payment(userId, type, page - 1, limit, startDate, endDate);
		return Result.success(result);
	}

	/**
	 * 转账记录
	 * 
	 * @param userId
	 * @param page
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/transferList")
	public Object transferList(@RequestParam(defaultValue = "") String userId,
			@RequestParam(defaultValue = "0") int toUserId, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int limit, @RequestParam(defaultValue = "") String startDate,
			@RequestParam(defaultValue = "") String endDate) {
		PageResult<Transfer> result = tfm.queryTransfer(page - 1, limit, userId, toUserId, startDate, endDate);
		return Result.success(result);
	}

	/**
	 * 总账统计
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping("/systemTotalBill")
	public Object systemTotalBill(@RequestParam(defaultValue = "0") Integer userId,
			@RequestParam(defaultValue = "") String startDate, @RequestParam(defaultValue = "") String endDate) {
		try {
			MongdbGroup g = getTotalBill(userId, startDate, endDate);
			return Result.success(g);
		} catch (ServiceException e) {
			return Result.error(e.getErrMessage());
		}
	}

	/**
	 * 检查用户异常集合
	 * 
	 * @return
	 */
	@RequestMapping("/checkUserBill")
	public Object checkUserBill() {
		try {
			DecimalFormat df = new DecimalFormat("#.00");
			Map<String, Object> map = new HashMap<>();
			// 用户充值记录
			PageResult<ConsumeRecord> result = crm.recharge(0, 1, 0, "", 0, 1000, "", "");
			map.put("count", 0);
			result.getData().forEach(c -> {
				MongdbGroup g = getTotalBill(c.getUserId(), "", "");
				Double totalBalance1 = g.getTotalBalance1();
				Double systemMoney = (g.getTotalSendRedPacket() - g.getTotalGetRedPacket() - g.getTotalBackRedPacket()
						+ g.getTotalTransferMoney() - g.getTotalGetTransferMoney() - g.getTotalBackTransferMoney()
						+ g.getTotalCodePay() - g.getTotalGetCodePay() + g.getTotalQRCodePay()
						- g.getTotalGetQRCodePay());
				Double left = g.getTotalRecharge() - g.getTotalCash();

				Double money = Double.valueOf(df.format(totalBalance1 + systemMoney + g.getTotalShopping() - left));
				if (money > 0) {
					map.put("userId", c.getUserId());
					map.put("money", money);
				}
				map.put("count", Integer.valueOf(map.get("count").toString()) + 1);

			});
			return Result.success(map);
		} catch (ServiceException e) {
			return Result.error(e.getErrMessage());
		}
	}

	private MongdbGroup getTotalBill(Integer userId, String startDate, String endDate) {
		log.debug("startDate:" + startDate);
		log.debug("endDate:" + endDate);
		long startTime = 0; // 开始时间（秒）
		long endTime = 0; // 结束时间（秒）,默认为当前时间
		startTime = StringUtil.isEmpty(startDate) ? 0 : DateUtil.toDate(startDate).getTime() / 1000;
		endTime = StringUtil.isEmpty(endDate) ? DateUtil.currentTimeSeconds()
				: DateUtil.toDate(endDate).getTime() / 1000;
		log.debug("startTime:" + startTime);
		log.debug("endTime:" + endTime);
		MongdbGroup g = new MongdbGroup();
		if (userId != null && userId > 0) {
			g.setUserId(userId);
		}
		g.setStartDate(startTime);
		g.setEndDate(endTime);

		// 用户总充值
		g = getTotalMoney("totalRecharge", "money", 1, new Integer[] { 1, 2, 3, 4, 6 }, new Integer[] { 1, 2 }, g);
		// 用户总提现
		g = getTotalMoney("totalCash", "money", 2, new Integer[] { 3 }, new Integer[] { 1, 2 }, g);
		// 用户总提现 手续费
		g = getTotalMoney("totalCash1", "money", 2, new Integer[] { 3 }, new Integer[] { 1, 2 }, g);
		// 微信总充值
		g = getTotalMoney("wxTotalRecharge", "money", 1, new Integer[] { 2 }, new Integer[] { 1, 2 }, g);
		// 支付宝总充值
		g = getTotalMoney("aliTotalRecharge", "money", 1, new Integer[] { 1 }, new Integer[] { 1, 2 }, g);
		// 易宝银行卡总充值
		g = getTotalMoney("yeeTotalRecharge", "money", 1, new Integer[] { 6 }, new Integer[] { 1, 2 }, g);
		// 后台总充值
		g = getTotalMoney("sysTotalRecharge", "money", 3, new Integer[] { 4 }, new Integer[] { 1, 2 }, g);
		// 后台总扣除
		g = getTotalMoney("sysTotalReduce", "money", 16, new Integer[] { 4 }, new Integer[] { 1, 2 }, g);
		// 红包总发送
		g = getTotalMoney("totalSendRedPacket", "money", 4, new Integer[] { 1, 2, 3, 4, 6 }, new Integer[] { 1, 2 }, g);
		// 红包总领取
		g = getTotalMoney("totalGetRedPacket", "money", 5, new Integer[] { 1, 2, 3, 4, 6 }, new Integer[] { 1, 2 }, g);
		// 红包总退款
		g = getTotalMoney("totalBackRedPacket", "money", 6, new Integer[] { 1, 2, 3, 4, 6 }, new Integer[] { 1, 2 }, g);
		// 总转账
		g = getTotalMoney("totalTransferMoney", "money", 7, new Integer[] { 1, 2, 3, 4, 6 }, new Integer[] { 1, 2 }, g);
		// 总接受转账
		g = getTotalMoney("totalGetTransferMoney", "money", 8, new Integer[] { 1, 2, 3, 4, 6 }, new Integer[] { 1, 2 },
				g);
		// 总退回转账
		g = getTotalMoney("totalBackTransferMoney", "money", 9, new Integer[] { 1, 2, 3, 4, 6 }, new Integer[] { 1, 2 },
				g);
		// 总付款码付款
		g = getTotalMoney("totalCodePay", "money", 10, new Integer[] { 1, 2, 3, 4, 6 }, new Integer[] { 1, 2 }, g);
		// 总付款码到账
		g = getTotalMoney("totalGetCodePay", "money", 11, new Integer[] { 1, 2, 3, 4, 6 }, new Integer[] { 1, 2 }, g);
		// 总二维码付款
		g = getTotalMoney("totalQRCodePay", "money", 12, new Integer[] { 1, 2, 3, 4, 6 }, new Integer[] { 1, 2 }, g);
		// 总二维码到账
		g = getTotalMoney("totalGetQRCodePay", "money", 13, new Integer[] { 1, 2, 3, 4, 6 }, new Integer[] { 1, 2 }, g);
		// 总vip充值
		g = getTotalMoney("totalVipRecharge", "money", 14, new Integer[] { 1, 2, 3, 4, 6 }, new Integer[] { 1, 2 }, g);
		// 总vip充值提成
		g = getTotalMoney("totalVipRechargeProfit", "money", 15, new Integer[] { 1, 2, 3, 4, 6 },
				new Integer[] { 1, 2 }, g);
		// 总商品消费
		g = getTotalMoney("totalShopping", "money", 20, new Integer[] { 1, 2, 3, 4, 6 }, new Integer[] { 1, 2 }, g);
		// 用户总余额
		g = getTotalMoney("totalBalance1", "balance", 20, new Integer[] { 1, 2, 3, 4, 6 }, new Integer[] { 1, 2 }, g);
		// 用户总充值
		g = getTotalMoney("totalRecharge1", "totalRecharge", 20, new Integer[] { 1, 2, 3, 4, 6 },
				new Integer[] { 1, 2 }, g);
		// 用户总消费
		g = getTotalMoney("totalConsume1", "totalConsume", 20, new Integer[] { 1, 2, 3, 4, 6 }, new Integer[] { 1, 2 },
				g);

		g.setTotalBalance(null);
		return g;
	}

	/**
	 * 
	 * @param total   统计的金额(记录值)
	 * @param sum     统计的金额
	 * @param type
	 * @param payType
	 * @param status
	 * @param g
	 * @return
	 */
	private MongdbGroup getTotalMoney(String total, String sum, int type, Integer[] payType, Integer[] status,
			MongdbGroup g) {
		DecimalFormat df = new DecimalFormat("#.00");
		AggregationPipeline pipeline = null;

		if (total.equals("totalBalance1") || total.equals("totalConsume1") || total.equals("totalRecharge1")) {
			Query<User> query = dfds.createQuery(User.class);
			if (g.getUserId() > 0)
				query.field("_id").equal(g.getUserId());

			pipeline = dfds.createAggregation(User.class);
			pipeline.match(query).group(grouping(total, sum(sum)));
		} else {
			Query<ConsumeRecord> query = dfds.createQuery(ConsumeRecord.class).order("-time");
			if (g.getUserId() > 0)
				query.field("userId").equal(g.getUserId());

			if (g.getStartDate() > 0)
				query.field("time").greaterThanOrEq(g.getStartDate());
			if (g.getEndDate() > 0)
				query.field("time").lessThanOrEq(g.getEndDate());

			if (total.equals("totalCash1")) {
				query.field("desc").equal("提现手续费");
			}
			pipeline = dfds.createAggregation(ConsumeRecord.class);
			pipeline.match(query.filter("type", type).filter("payType in", payType).filter("status in", status))
					.group(grouping(total, sum(sum)));
		}

		Iterator<MongdbGroup> iterator = pipeline.aggregate(MongdbGroup.class);
		System.out.println(iterator.hasNext());
		while (iterator.hasNext()) {
			MongdbGroup ug = iterator.next();
			switch (total) {
			case "totalRecharge":
				g.setTotalRecharge(Double.valueOf(df.format(ug.getTotalRecharge())));
				break;
			case "totalCash":
				g.setTotalCash(Double.valueOf(df.format(ug.getTotalCash())));
				break;
			case "totalCash1":
				g.setTotalCash1(Double.valueOf(df.format(ug.getTotalCash1())));
				break;
			case "wxTotalRecharge":
				g.setWxTotalRecharge(Double.valueOf(df.format(ug.getWxTotalRecharge())));
				break;
			case "aliTotalRecharge":
				g.setAliTotalRecharge(Double.valueOf(df.format(ug.getAliTotalRecharge())));
				break;
			case "yeeTotalRecharge":
				g.setYeeTotalRecharge(Double.valueOf(df.format(ug.getYeeTotalRecharge())));
				break;
			case "sysTotalRecharge":
				g.setSysTotalRecharge(Double.valueOf(df.format(ug.getSysTotalRecharge())));
				break;
			case "sysTotalReduce":
				g.setSysTotalReduce(Double.valueOf(df.format(ug.getSysTotalReduce())));
				break;
			case "totalSendRedPacket":
				g.setTotalSendRedPacket(Double.valueOf(df.format(ug.getTotalSendRedPacket())));
				break;
			case "totalGetRedPacket":
				g.setTotalGetRedPacket(Double.valueOf(df.format(ug.getTotalGetRedPacket())));
				break;
			case "totalBackRedPacket":
				g.setTotalBackRedPacket(Double.valueOf(df.format(ug.getTotalBackRedPacket())));
				break;
			case "totalTransferMoney":
				g.setTotalTransferMoney(Double.valueOf(df.format(ug.getTotalTransferMoney())));
				break;
			case "totalGetTransferMoney":
				g.setTotalGetTransferMoney(Double.valueOf(df.format(ug.getTotalGetTransferMoney())));
				break;
			case "totalBackTransferMoney":
				g.setTotalBackTransferMoney(Double.valueOf(df.format(ug.getTotalBackTransferMoney())));
				break;
			case "totalCodePay":
				g.setTotalCodePay(Double.valueOf(df.format(ug.getTotalCodePay())));
				break;
			case "totalGetCodePay":
				g.setTotalGetCodePay(Double.valueOf(df.format(ug.getTotalGetCodePay())));
				break;
			case "totalQRCodePay":
				g.setTotalQRCodePay(Double.valueOf(df.format(ug.getTotalQRCodePay())));
				break;
			case "totalGetQRCodePay":
				g.setTotalGetQRCodePay(Double.valueOf(df.format(ug.getTotalGetQRCodePay())));
				break;
			case "totalVipRecharge":
				g.setTotalVipRecharge(Double.valueOf(df.format(ug.getTotalVipRecharge())));
				break;
			case "totalVipRechargeProfit":
				g.setTotalVipRechargeProfit(Double.valueOf(df.format(ug.getTotalVipRechargeProfit())));
				break;
			case "totalShopping":
				g.setTotalShopping(Double.valueOf(df.format(ug.getTotalShopping())));
				break;
			case "totalBalance1":
				g.setTotalBalance1(Double.valueOf(df.format(ug.getTotalBalance1())));
				break;
			case "totalConsume1":
				g.setTotalConsume1(Double.valueOf(df.format(ug.getTotalConsume1())));
				break;
			case "totalRecharge1":
				g.setTotalRecharge1(Double.valueOf(df.format(ug.getTotalRecharge1())));
				break;

			default:
				break;
			}
			System.out.println(ug);
		}
		return g;
	}

	/**
	 * 用户，群组，单聊消息，好友关系数量 统计
	 */
	@RequestMapping(value = "/countNum")
	public Result countNum(HttpServletRequest request, HttpServletResponse response) {

		try {
			long userNum = dfds.getCollection(User.class).count();
			long roomNum = 0;
			long msgNum = 0;
			long friendsNum = 0;

			Map<String, Long> dataMap = new HashMap<String, Long>();
			dataMap.put("userNum", userNum);
			dataMap.put("roomNum", roomNum);
			dataMap.put("msgNum", msgNum);
			dataMap.put("friendsNum", friendsNum);
			return Result.success(dataMap);
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}

	}

	/**
	 * 用户在线数量统计
	 * 
	 * @param pageIndex
	 * @param pageSize
	 * @param sign
	 * @param startDate
	 * @param endDate
	 * @param timeUnit
	 * @throws Exception
	 */
	@RequestMapping(value = "/getUserStatusCount")
	public Result getUserStatusCount(@RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "100") int pageSize, @RequestParam(defaultValue = "2") short timeUnit,
			@RequestParam(defaultValue = "") String startDate, @RequestParam(defaultValue = "") String endDate)
			throws Exception {

		try {

			Object data = userService.userOnlineStatusCount(startDate.trim(), endDate.trim(), timeUnit);
			return Result.success(data);
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}
	}

	/**
	 * 统计用户注册信息
	 */
	@RequestMapping(value = "/getUserRegisterCount")
	public Result getUserRegisterCount(@RequestParam(defaultValue = "0") int pageIndex,
			@RequestParam(defaultValue = "100") int pageSize, @RequestParam(defaultValue = "2") short timeUnit,
			@RequestParam(defaultValue = "") String startDate, @RequestParam(defaultValue = "") String endDate) {

		try {
			Object data = userService.getUserRegisterCount(startDate.trim(), endDate.trim(), timeUnit);
			return Result.success(data);
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}

	}

	/**
	 * 消息抄送集合
	 * 
	 * @param fromAccount
	 * @param to
	 * @param eventType
	 * @param convType
	 * @param page
	 * @param limit
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	@RequestMapping("/getMessageReceiveList")
	public Object getMessageReceiveList(@RequestParam(defaultValue = "") String fromAccount,
			@RequestParam(defaultValue = "") String to, @RequestParam(defaultValue = "") String eventType,
			@RequestParam(defaultValue = "") String convType, @RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "") String startTime,
			@RequestParam(defaultValue = "") String endTime) {
		try {
			PageResult<MessageReceive> list = mrs.getList(fromAccount, to, eventType, convType, limit, page - 1,
					DateUtil.toTimestamp(startTime), DateUtil.toTimestamp(endTime));
			return Result.success(list);
		} catch (ServiceException e) {
			return Result.error(e.getErrMessage());
		}
	}

	/**
	 * 删除一月之前的消息
	 * 
	 * @return
	 */
	@RequestMapping("/delMessageReceive")
	public Object delMessageReceive() {
		try {
			// 一月之前的时间
			long lastMonth = DateUtil.getLastMonth().getTime();
			System.out.println(DateUtil.getLastMonth());
			mrs.delMessage(null, null, "1", null, 0l, lastMonth);
			return Result.success();
		} catch (ServiceException e) {
			return Result.error(e.getErrMessage());
		}
	}

	// ==============群组管理====================
	@RequestMapping("/getTeamList")
	public Object getTeamList(@RequestParam(defaultValue = "") String fromAccount,
			@RequestParam(defaultValue = "") String to, @RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "") String startTime,
			@RequestParam(defaultValue = "") String endTime) {
		try {
			Query<MessageReceive> q = dfds.createQuery(MessageReceive.class);
			q.field("convType").equal("TEAM");
			q.field("to").equal(to);
			long st = DateUtil.toTimestamp(startTime);
			long et = DateUtil.toTimestamp(endTime);
			if (st > 0)
				q.field("msgTimestamp").greaterThanOrEq(startTime);
			if (et > 0)
				q.field("msgTimestamp").lessThanOrEq(endTime);
			AggregationPipeline pipeline = dfds.createAggregation(MessageReceive.class);
			pipeline.match(q).group(id(grouping("to")));
//			pipeline.skip(page>1?(page-1)*limit:0);
//			pipeline.limit(limit);
			Iterator<TeamGroup> iterator = pipeline.aggregate(TeamGroup.class);
			System.out.println(iterator.hasNext());
//			 Set<String> tids=new HashSet<>();
			List<JSONObject> res = new ArrayList<JSONObject>();
			while (iterator.hasNext()) {
				TeamGroup ug = iterator.next();
				System.out.println(JSON.parseObject(ug.getTo()).getString("to"));
				System.out.println(ug.getCount());
//				 tids.add(JSON.parseObject(ug.getTo()).getString("to"));
				com.youxin.app.yx.request.team.QueryDetail tq = new com.youxin.app.yx.request.team.QueryDetail();
				tq.setTid(Long.valueOf(JSON.parseObject(ug.getTo()).getString("to")));
				res.add(SDKService.teamQueryDetail(tq).getJSONObject("tinfo"));
			}
//			 PageResult<MessageReceive> pr=new PageResult<>(mrList, count);
			return Result.success(res);
		} catch (ServiceException e) {
			return Result.error(e.getErrMessage());
		}
	}

	/**
	 * 批量发送消息
	 * 
	 * @param text
	 * @return
	 */
	@RequestMapping("/sendBatchMsg")
	public Object sendBatchMsg(@RequestParam(defaultValue = "") String text) {
		try {
			Query<User> q = ur.createQuery();
			q.field("disableUser").notEqual(-1).field("isDelUser").notEqual(1);
			Double count = (double) q.count();
			int ceil = (int) Math.ceil(count / 500.00);
			// 分页查询用户进行消息发送
			for (int i = 0; i < ceil; i++) {
				List<User> asList = q.asList(MongoUtil.pageFindOption(i, 500));
				List<String> accids = asList.stream().map(User::getAccid).collect(Collectors.toList());
				Msg msg = new Msg();
				msg.setBody("{\"msg\":\"" + text + "\"}");
				msg.setFrom(Md5Util.md5HexToAccid("10000"));
				msg.setTo(JSON.toJSONString(accids));
				msg.setType(0);// 文本
				JSONObject json = SDKService.sendBatchMsg(msg);
				if (json.getIntValue("code") == 200) {
					log.debug("群发消息成功，第" + (i + 1) + "页");
				}
			}
			log.debug("总计" + count + "人");
			return Result.success(count);
		} catch (ServiceException e) {
			return Result.error(e.getErrMessage());
		}
	}

	/**
	 * 发送广播消息
	 * 
	 * @param text
	 * @return
	 */
	@RequestMapping("/broadcastMsg")
	public Object broadcastMsg(@RequestParam(defaultValue = "") String text) {
		try {

			JSONObject json = SDKService.broadcastMsg(
					JSON.toJSONString(new MsgBody(0, KConstants.MsgType.BROADCASTMST_ALL, text)),
					Md5Util.md5HexToAccid("10000"), "true", 7, "");
			if (json.getIntValue("code") == 200)
				return Result.success();
			return Result.error();
		} catch (ServiceException e) {
			return Result.error(e.getErrMessage());
		}
	}

	/**
	 * 获取授权列表
	 * 
	 * @param pageIndex
	 * @param pageSize
	 * @param state
	 * @return
	 */
	@RequestMapping(value = "/pplist")
	public Object pplist(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int limit,
			@RequestParam(defaultValue = "0") int state, @RequestParam(defaultValue = "") String nickName) {
		return Result.success(pps.pageList());
	}

	/**
	 * 获取授权实体
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/getpp")
	public Object getpp(@RequestParam(defaultValue = "") String id) {
		Optional.ofNullable(id).orElseThrow(() -> new ServiceException("id为空"));
		return Result.success(pps.getPP(id));
	}

	/**
	 * 修改或者保存授权
	 * 
	 * @param hcid
	 * @param hc
	 * @return
	 */
	@RequestMapping(value = "/savepp")
	public Object savepp(@ModelAttribute PublicPermission pp) {
		pps.SaveOrUpdatePP(pp);
		return Result.success();
	}

	@RequestMapping(value = "/delpp")
	public Object delpp(@RequestParam(defaultValue = "") String id) {
		Optional.ofNullable(id).orElseThrow(() -> new ServiceException("id为空"));
		String[] ids = StringUtil.getStringList(id, ",");
		for (String idd : ids) {
			pps.delPP(idd);
		}
		return Result.success();
	}

	/**
	 * 获取授权列表
	 * 
	 * @param pageIndex
	 * @param pageSize
	 * @param state
	 * @return
	 */
	@RequestMapping(value = "/iplist")
	public Object iplist(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int limit,
			@RequestParam(defaultValue = "0") int disable, @RequestParam(defaultValue = "") String nickName) {
		return Result.success(ipds.pageList(disable, nickName, page, limit));
	}

	/**
	 * 获取授权实体
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/getip")
	public Object getip(@RequestParam(defaultValue = "") String id) {
		Optional.ofNullable(id).orElseThrow(() -> new ServiceException("id为空"));
		return Result.success(ipds.getObj(id));
	}

	/**
	 * 修改或者保存授权
	 * 
	 * @param hcid
	 * @param hc
	 * @return
	 */
	@RequestMapping(value = "/saveip")
	public Object saveip(@ModelAttribute IPDisable ip) {
		ipds.SaveOrUpdateObj(ip);
		return Result.success();
	}

	@RequestMapping(value = "/updisable")
	public Object updisable(@ModelAttribute IPDisable ip) {
		ipds.updisable(ip.getSid(), ip.getDisable());
		return Result.success();
	}

	@RequestMapping(value = "/delip")
	public Object delip(@RequestParam(defaultValue = "") String id) {
		Optional.ofNullable(id).orElseThrow(() -> new ServiceException("id为空"));
		String[] ids = StringUtil.getStringList(id, ",");
		for (String idd : ids) {
			ipds.delObj(idd);
		}
		return Result.success();
	}

	/**
	 * 接口访问列表
	 * 
	 * @param userId
	 * @param apiId
	 * @param page
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/sysapiList")
	public Object sysapiList(@RequestParam(defaultValue = "-1") Integer userId,
			@RequestParam(defaultValue = "") String apiId, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "15") int limit) {
		Query<SysApiLog> q = dfds.createQuery(SysApiLog.class);
		if (!StringUtil.isEmpty(apiId))
			q.field("apiId").containsIgnoreCase(apiId);
		if (userId >= 0)
			q.field("userId").equal(userId);
		q.order("-time");
		return Result.success(new PageResult<>(q.asList(MongoUtil.pageFindOption(page - 1, limit)), q.count()));
	}

	/**
	 * 删除七日前的接口日志
	 * 
	 * @return
	 */
	@RequestMapping(value = "/delsysapi")
	public Object delsysapi(int days) {
		Query<SysApiLog> q = dfds.createQuery(SysApiLog.class);
		q.field("time").lessThanOrEq(DateUtil.getOnedayNextDay(DateUtil.currentTimeSeconds(), days, 1));
		dfds.delete(q);
		return Result.success();
	}

	/**
	 * 10000 号 发送普通消息
	 * 
	 * @param text
	 * @param userId
	 * @return
	 */
	@RequestMapping("sendMsg")
	public Object sendMsg(@RequestParam(defaultValue = "") String text,
			@RequestParam(defaultValue = "") String userId) {
		MsgRequest messageBean = new MsgRequest();
		messageBean.setFrom(Md5Util.md5HexToAccid("10000"));
		messageBean.setType(0);// 文本
		messageBean.setOpe(0);// 个人消息
		messageBean.setTo(Md5Util.md5HexToAccid(userId));
		messageBean.setBody("{\"msg\":\"" + text + "\"}");
		JSONObject msgjson = SDKService.sendMsg(messageBean);
		if (msgjson.getInteger("code") != 200)
			return Result.error("消息发送失败");
		return Result.success();
	}

	@RequestMapping("getFriends")
	public Object getFriends(@RequestParam(defaultValue = "") String accid) {
		JSONObject friendGet = SDKService.friendGet(accid, 0l, null);
		if (friendGet.getInteger("code") != 200)
			return Result.error("获取好友失败");

		JSONArray jsonArray = friendGet.getJSONArray("friends");
		Assert.isTrue(jsonArray != null, "无好友");
		for (int i = 0; i < jsonArray.size(); i++) {
			User userFromDB = userService.getUserFromDB(jsonArray.getJSONObject(i).getString("faccid"));
			jsonArray.getJSONObject(i).put("nickname", userFromDB.getName());
		}
		PageResult<Object> pageResult = new PageResult<>(jsonArray, friendGet.getIntValue("size"));
		return Result.success(pageResult);
	}

}
