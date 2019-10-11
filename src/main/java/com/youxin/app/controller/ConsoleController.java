package com.youxin.app.controller;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.youxin.app.entity.User;
import com.youxin.app.entity.User.UserLoginLog;
import com.youxin.app.filter.LoginSign;
import com.youxin.app.repository.UserRepository;
import com.youxin.app.service.AdminConsoleService;
import com.youxin.app.service.UserService;
import com.youxin.app.utils.MongoUtil;
import com.youxin.app.utils.PageResult;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.ResultCode;
import com.youxin.app.utils.StringUtil;
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
			if (StringUtil.isNumeric(keyWorld)) {
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


}
