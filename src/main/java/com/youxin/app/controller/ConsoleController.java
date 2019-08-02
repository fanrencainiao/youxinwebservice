package com.youxin.app.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import com.youxin.app.entity.User;
import com.youxin.app.filter.LoginSign;
import com.youxin.app.service.AdminConsoleService;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.ResultCode;
import com.youxin.app.utils.StringUtil;

@RestController
@RequestMapping("/console/")
public class ConsoleController {

	@Autowired
	AdminConsoleService consoleService;

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

}
