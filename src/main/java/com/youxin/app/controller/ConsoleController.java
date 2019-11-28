package com.youxin.app.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.youxin.app.entity.BankRecord;
import com.youxin.app.entity.Config;
import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.entity.RedPacket;
import com.youxin.app.entity.RedReceive;
import com.youxin.app.entity.Role;
import com.youxin.app.entity.User;
import com.youxin.app.entity.User.MyCard;
import com.youxin.app.entity.User.UserLoginLog;
import com.youxin.app.entity.msgbody.MsgBody;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.filter.LoginSign;
import com.youxin.app.repository.UserRepository;
import com.youxin.app.service.AdminConsoleService;
import com.youxin.app.service.ConfigService;
import com.youxin.app.service.UserService;
import com.youxin.app.service.impl.ConsumeRecordManagerImpl;
import com.youxin.app.service.impl.RedPacketManagerImpl;
import com.youxin.app.utils.BeanUtils;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.Md5Util;
import com.youxin.app.utils.MongoUtil;
import com.youxin.app.utils.PageResult;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.ResultCode;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.alipay.util.AliPayUtil;
import com.youxin.app.yx.SDKService;
import com.youxin.app.yx.request.MsgRequest;








@RestController
@RequestMapping("/console/")
public class ConsoleController extends AbstractController{

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

	
	@PostMapping(value = "login")
	public Object login(String name, String password, HttpServletRequest request) {
		User login = consoleService.login(name, password);
		if (login != null) {
			request.getSession().setAttribute(LoginSign.LOGIN_USER_KEY, login);
			
			String s=JSONObject.toJSONString(login);
			JSONObject json=JSONObject.parseObject(s);
			json.put("role", 6);
			return Result.success(json);
		}
		return Result.failure(ResultCode.USER_LOGIN_ERROR);

	}

	@PostMapping(value = "logout")
	public Object logout(HttpServletRequest request) {
		request.getSession().removeAttribute(LoginSign.LOGIN_USER_KEY);
		System.out.println(request.getSession().getAttribute(LoginSign.LOGIN_USER_KEY));
		return Result.success();

	}
	
	@GetMapping(value = "userList")
	public Object userList(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "") String onlinestate,
			@RequestParam(defaultValue = "") String keyWorld) {
		Query<User> query = ur.createQuery();

		if (!StringUtil.isEmpty(keyWorld)) {
			Integer userId = null;
			if (StringUtil.isNumeric(keyWorld)&&keyWorld.length()<10) {
				userId = Integer.valueOf(keyWorld);
			}
			
			query.or(query.criteria("name").containsIgnoreCase(keyWorld), query.criteria("_id").equal(userId),
					query.criteria("mobile").containsIgnoreCase(keyWorld));
		}
		if (!StringUtil.isEmpty(onlinestate)) {
			query.filter("onlinestate", Integer.valueOf(onlinestate));
		}
		// 排序、分页
		List<User> pageData = query.order("-createTime").asList(MongoUtil.pageFindOption(page-1, limit));
		pageData.forEach(userInfo -> {
			Query<UserLoginLog> loginLog = dfds.createQuery(UserLoginLog.class).field("userId")
					.equal(userInfo.getId());
			if (null != loginLog.get())
				userInfo.setLoginLog(loginLog.get().getLoginLog());
		});
		PageResult<User> result = new PageResult<User>();
		result.setData(pageData);
		result.setCount(query.count());
		return Result.success(result);
	}
	
	@PostMapping("blockUser")
	public Object blockUser(@RequestParam(required=true) int id,@RequestParam(required=true) String accid,@RequestParam(required=true) int disableUser){
		JSONObject block = null;
		User user=new User();
		if(disableUser==1)
			block=SDKService.block(accid, "false");
		else if(disableUser==0)
			block=SDKService.unblock(accid);
		else
			return Result.error();
		
		if(block.getIntValue("code")==200) {
			user.setDisableUser(disableUser);
			user.setId(id);
			user.setAccid(accid);
			userService.updateUser(user);
			return Result.success();
		}
		
		return Result.error();
	}
	/**
	 * 获取用户信息
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
					if (role.getRole()==2) {
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

		// 保存到数据库
		if (StringUtil.isEmpty(accid)&&example.getId()<=0) {
			//验证
			if (StringUtil.isEmpty(example.getMobile())) {
				throw new ServiceException(0, "手机号必填");
			}
			long mobileCount = userService.mobileCount(example.getMobile());
			if (mobileCount >= 1) {
				throw new ServiceException(0, "手机号已被注册");
			}
			userService.register(example);

		} else {
			com.youxin.app.yx.request.User.User u=new com.youxin.app.yx.request.User.User();
			User userFromDB = userService.getUserFromDB(example.getId());
			BeanUtils.copyProperties(example, userFromDB);
			BeanUtils.copyProperties(userFromDB, u);
			JSONObject updateUinfo = SDKService.updateUinfo(u);
			if(updateUinfo.getIntValue("code")==200) {
				ur.save(userFromDB);
			}else {
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
		record.setMoney(money);
		record.setStatus(KConstants.OrderStatus.END);
		if(money>=0) {
			record.setType(KConstants.ConsumeType.SYSTEM_RECHARGE);
			record.setDesc("后台余额充值");
		}else {
			record.setType(KConstants.ConsumeType.SYSTEM_REDUCE);
			record.setDesc("后台余额扣除");
		}
		record.setPayType(KConstants.PayType.SYSTEMPAY); // type = 3 ：管理后台充值
		record.setTime(DateUtil.currentTimeSeconds());
		crm.saveConsumeRecord(record);
		try {
			Double balance =0.0;
			if(money>=0)
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
	 * 	 用户账单
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
			PageResult<ConsumeRecord> result = crm.consumeRecordList(userId, page-1,
					limit);
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
	public Object getRedPacketList(@RequestParam(defaultValue = "") String userName,
			@RequestParam(defaultValue = "0") int userId,
			@RequestParam(defaultValue = "0") int toUserId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit) {
		try {
			PageResult<RedPacket> result = rpm.getRedPacketList(userName,userId,toUserId, page-1, limit);
			return Result.success(result);
		} catch (ServiceException e) {
			return Result.error(e.getErrMessage());
		}
	}

	@RequestMapping("receiveWater")
	public Object receiveWater(@RequestParam(defaultValue = "") String redId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit) {
		try {
			PageResult<RedReceive> result = rpm.receiveWater(redId, page-1, limit);
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
	// 设置服务端配置
	@RequestMapping(value = "/config/set", method = RequestMethod.POST)
	public Object setConfig(@ModelAttribute Config config) throws Exception {
		try {
			cs.setConfig(config);
			return Result.success();
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}
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
			PageResult<BankRecord> result = consoleService.getBankRecordList(bankCard, userId, page-1,
					limit);
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
			if (status == 1 || status == 0) {
				Query<BankRecord> query = dfds.createQuery(BankRecord.class).field("_id")
						.equal(new ObjectId(id));
				BankRecord bankRecord = query.get();
				if (null == bankRecord)
					return Result.error("数据系统出错");
				UpdateOperations<BankRecord> ops = dfds.createUpdateOperations(BankRecord.class);
				long currentTimeSeconds = DateUtil.currentTimeSeconds();
				ops.set("status", status);
				ops.set("payTime", currentTimeSeconds);
				dfds.update(query, ops);
				String card = bankRecord.getBankCard();

				List<MyCard> tocard = dfds.createQuery(MyCard.class).field("bankCard").equal(card)
						.asList();
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
				messageBean.setTo(Md5Util.md5Hex(bankRecord.getUserId()+""));
				messageBean.setBody(JSON.toJSONString(new MsgBody(0, KConstants.MsgType.BANKOVERMONEY, sendReulst)));
				try {
					JSONObject json=SDKService.sendMsg(messageBean);
					if(json.getInteger("code")!=200) 
						log.debug("银行卡提现 sdk消息发送失败");
				} catch (Exception e) {
					e.printStackTrace();
					log.debug("银行卡提现 sdk消息发送失败"+e.getMessage());
				}
			}
			return Result.success();
		} catch (ServiceException e) {
			return Result.error(e.getErrMessage());
		}
	}


}
