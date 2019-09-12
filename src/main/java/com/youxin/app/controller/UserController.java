package com.youxin.app.controller;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.youxin.app.entity.User;
import com.youxin.app.service.UserService;
import com.youxin.app.utils.Result;
import com.youxin.app.yx.SDKService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
@Api(tags = "用户管理")
@RestController
@RequestMapping("/user/")
public class UserController extends AbstractController{
	

	@Autowired
	private UserService userService;
	
	@ApiOperation(value = "注册")
	@PostMapping("register")
	public Object register(@RequestBody @Valid User user){
		Map<String, Object> data=userService.register(user);
		return Result.success(data);
	}
	
	@ApiOperation(value = "获取用户信息（优先从缓存获取）")
	@ApiImplicitParams({ @ApiImplicitParam(name = "userId", value = "userId", required = true, paramType = "query") })
	@GetMapping("get")
	public Object get(@RequestParam Integer userId){
		
		User u=userService.getUser(userId);
		return Result.success(u);
	}
	@ApiOperation(value = "登录")
	@ApiImplicitParams({ @ApiImplicitParam(name = "mobile", value = "手机号", required = true, paramType = "query")
	,@ApiImplicitParam(name = "password", value = "密码", required = false, paramType = "query"),
	@ApiImplicitParam(name = "loginType", value = "登录类型(0：账号密码登录)", required = true, paramType = "query"),
	})
	@PostMapping("login")
	public Object login(@RequestParam String mobile,@RequestParam String password,@RequestParam int loginType){
		User user=new User();
		user.setMobile(mobile);
		user.setPassword(password);
		user.setLoginType(loginType);
		Map<String, Object> u=userService.login(user);
		return Result.success(u);
	}
	@ApiOperation(value = "刷新token(云信指定token)")
	@PostMapping("refreshToken")
	public Object refreshToken(@RequestParam String accid){
		JSONObject refreshToken = SDKService.refreshToken(accid);
		if(refreshToken.getIntValue("code")==200) {
			Result.success(refreshToken.getJSONObject("info"));
		}
		return Result.error();
	}
	
	@ApiOperation(value = "封禁用户")
	@PostMapping("blockUser")
	public Object blockUser(@RequestParam(required=true) String accid,@RequestParam() String needkick){
		JSONObject refreshToken = SDKService.block(accid, needkick);
		if(refreshToken.getIntValue("code")==200) {
			Result.success();
		}
		return Result.error();
	}
	@ApiOperation(value = "解禁用户")
	@PostMapping("unblockUser")
	public Object unblockUser(@RequestParam(required=true) String accid){
		JSONObject refreshToken = SDKService.unblock(accid);
		if(refreshToken.getIntValue("code")==200) {
			Result.success();
		}
		return Result.error();
	}
	
	
	

}
