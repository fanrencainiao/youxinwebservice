package com.youxin.app.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
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

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.entity.Role;
import com.youxin.app.entity.User;
import com.youxin.app.entity.User.UserLoginLog;
import com.youxin.app.entity.exam.UserExample;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.filter.LoginSign;
import com.youxin.app.repository.UserRepository;
import com.youxin.app.service.AdminConsoleService;
import com.youxin.app.service.UserService;
import com.youxin.app.service.impl.ConsumeRecordManagerImpl;
import com.youxin.app.utils.BeanUtils;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.MongoUtil;
import com.youxin.app.utils.PageResult;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.ResultCode;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.alipay.util.AliPayUtil;
import com.youxin.app.yx.SDKService;

import io.swagger.annotations.ApiOperation;


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
			BeanUtils.copyProperties(example, u);
			User userFromDB = userService.getUserFromDB(example.getId());
			BeanUtils.copyProperties(example, userFromDB);
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


}
