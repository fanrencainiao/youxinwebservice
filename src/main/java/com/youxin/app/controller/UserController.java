package com.youxin.app.controller;

import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.youxin.app.entity.User;
import com.youxin.app.repository.UserRepository;
import com.youxin.app.service.UserService;
import com.youxin.app.utils.KSessionUtil;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.ResultCode;
import com.youxin.app.yx.SDKService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(tags = "用户管理")
@RestController
@RequestMapping("/user/")
public class UserController extends AbstractController{
	

	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository repository;
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
	@ApiOperation(value = "修改密码")
	@ApiImplicitParams({ @ApiImplicitParam(name = "mobile", value = "手机号", required = true, paramType = "query")
	,@ApiImplicitParam(name = "password", value = "密码", required = true, paramType = "query"),
	@ApiImplicitParam(name = "newPassword", value = "新密码", required = true, paramType = "query"),
	})
	@PostMapping("updatePwd")
	public Object updatePwd(@RequestParam String mobile,@RequestParam String password,@RequestParam String newPassword){
		
		Query<User> q = repository.createQuery();
		q.field("mobile").equal(mobile).field("password").equal(password);
		User u = repository.findOne(q);
		if(u!=null&&StringUtils.isNotBlank(u.getAccid())) {
			u.setPassword(newPassword);
			repository.save(u);
		}else {
			Result.failure(ResultCode.USER_LOGIN_ERROR);
		}
		return Result.success(u);
	}
	@ApiOperation(value = "刷新token(云信指定token)")
	@PostMapping("refreshToken")
	public Object refreshToken(@RequestParam(required=true) String accid){
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
	@ApiOperation(value = "更新用户基本信息")
	@PostMapping("updateUinfo")
	public Object updateUinfo(@RequestBody(required=true) com.youxin.app.yx.request.User.User user){
		if(StringUtils.isBlank(user.getAccid())) {
			return Result.errorMsg("accid不能为空");
		}
		User u = repository.findOne("accid", user.getAccid());
		if(u!=null&&StringUtils.isNotBlank(u.getAccid())) {
			JSONObject json = SDKService.updateUinfo(user);
			if(json.getIntValue("code")==200) {
				BeanUtils.copyProperties(user, u,"id");
				//更新本地数据库
				repository.save(u);
				//更新redis
				KSessionUtil.saveUserByUserId(u.getId(), u);
				Result.success();
			}
		}
		return Result.error();
	}
	@ApiOperation(value = "设置黑名单/静音 拉黑/取消拉黑；设置静音/取消静音")
	@ApiImplicitParams({ @ApiImplicitParam(name = "accid", value = "用户帐号", required = true, paramType = "query")
	,@ApiImplicitParam(name = "targetAcc", value = "被加黑或加静音的帐号", required = true, paramType = "query"),
	@ApiImplicitParam(name = "relationType", value = "本次操作的关系类型,1:黑名单操作，2:静音列表操作", required = true, paramType = "query"),
	@ApiImplicitParam(name = "value", value = "0:取消黑名单或静音，1:加入黑名单或静音", required = true, paramType = "query"),
	})
	@PostMapping("setSpecialRelation")
	public Object setSpecialRelation(@RequestParam String accid,@RequestParam String targetAcc,@RequestParam int relationType,@RequestParam int value){

			JSONObject json = SDKService.setSpecialRelation(accid, targetAcc, relationType+"", value+"");
			if(json.getIntValue("code")==200) {
//				User u = repository.findOne("accid", accid);
//				//更新本地数据库
//				repository.save(u);
//				//更新redis
//				KSessionUtil.saveUserByUserId(u.getId(), u);
				Result.success();
			}
		
		return Result.error();
	}
	
	@ApiOperation(value = "查看用户的黑名单和静音列表")
	@PostMapping("listBlackAndMuteList")
	public Object listBlackAndMuteList(@RequestParam String accid){

			JSONObject json = SDKService.listBlackAndMuteList(accid);
			if(json.getIntValue("code")==200) {
				JSONObject jsonr=new JSONObject();
				jsonr.put("mutelist", json.get("mutelist"));
				jsonr.put("blacklist", json.get("blacklist"));
				Result.success(jsonr);
			}
		
		return Result.error();
	}
	
	
	
	

}
