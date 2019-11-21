package com.youxin.app.controller;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.domain.Account;
import com.mongodb.DBObject;
import com.youxin.app.entity.SdkLoginInfo;
import com.youxin.app.entity.User;
import com.youxin.app.entity.User.UserSettings;
import com.youxin.app.entity.UserVo;
import com.youxin.app.entity.exam.UserExample;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.repository.UserRepository;
import com.youxin.app.service.UserService;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KSessionUtil;
import com.youxin.app.utils.Md5Util;
import com.youxin.app.utils.ReqUtil;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.ResultCode;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.sms.SMSServiceImpl;
import com.youxin.app.yx.SDKService;
import com.youxin.app.yx.request.Friends;

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
	@Autowired
	private SMSServiceImpl sendSms;
	@Autowired
	@Qualifier("get")
	private Datastore dfds;
	
	@ApiOperation(value = "注册",notes="若是sdk注册，请携带sdkType和loginInfo值")
	@PostMapping("register")
	public Object register(@RequestBody @Valid User user){
		if (StringUtil.isEmpty(user.getMobile())) {
			throw new ServiceException(0, "手机号必填");
		}
		if (StringUtil.isEmpty(user.getSmsCode())) {
			throw new ServiceException(0, "短信验证码必填");
		}
		if (!sendSms.isAvailable("86" + user.getMobile(), user.getSmsCode()))
			throw new ServiceException("短信验证码不正确!");
		long mobileCount = userService.mobileCount(user.getMobile());
		if (mobileCount >= 1) {
			throw new ServiceException(0, "手机号已被注册");
		}
		Map<String, Object> data=userService.register(user);
		return Result.success(data);
	}
	
	@ApiOperation(value = "sdk登录",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "loginInfo", value = "sdk登录标识", required = true, paramType = "query"),
		 @ApiImplicitParam(name = "sdkType", value = "第三方类型：1qq, 2微信", required = true, paramType = "query")})
	@PostMapping("sdkLogin")
	public Object sdkLogin(@RequestParam int sdkType,
			@RequestParam String loginInfo) {
		SdkLoginInfo sdkLoginInfo = userService.findSdkLoginInfo(sdkType, loginInfo);
		if (sdkLoginInfo != null) {
			User user = userService.getUserFromDB(sdkLoginInfo.getUserId());
			user.setLoginType(3);
			Object data = userService.login(user);
			return Result.success(data);
		} else {
			// 未绑定手机号码
			return Result.failure(ResultCode.UNBindingTelephone);
		}
	}
	/**
	 * 绑定
	 * @param loginInfo
	 * @return
	 */
	@ApiOperation(value = "绑定第三方sdk",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "loginInfo", value = "sdk登录标识", required = true, paramType = "query"),
		 @ApiImplicitParam(name = "sdkType", value = "第三方类型：1qq, 2微信", required = true, paramType = "query")})
	@PostMapping("bingdingSDK")
	public Object bingdingSDK(@RequestParam int sdkType,@RequestParam String loginInfo) {
		SdkLoginInfo sdkLoginInfo = userService.findSdkLoginInfo(sdkType, loginInfo);
		if (sdkLoginInfo == null) {
			sdkLoginInfo=new SdkLoginInfo();
			sdkLoginInfo.setUserId(ReqUtil.getUserId());
			sdkLoginInfo.setLoginInfo(loginInfo);
			sdkLoginInfo.setType(sdkType);
			sdkLoginInfo.setCreateTime(DateUtil.currentTimeSeconds());
			dfds.save(sdkLoginInfo);
			return Result.success(sdkLoginInfo);
		} else {
			// 微信已经被绑定
			return Result.failure(ResultCode.BINGDINGWXED);
		}
	}
	/**
	 * 解绑
	 * @param loginInfo
	 * @return
	 */
	@ApiOperation(value = "解绑第三方sdk",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "loginInfo", value = "sdk登录标识", required = true, paramType = "query"),
		 @ApiImplicitParam(name = "sdkType", value = "第三方类型：1qq, 2微信", required = true, paramType = "query")})
	@DeleteMapping("unBingding")
	public Object unBingdingSDK(@RequestParam int sdkType,@RequestParam String loginInfo) {
		return Result.success(userService.delSdkLoginInfo(sdkType,loginInfo));
	}
	/**
	 * 获取绑定信息
	 * @param loginInfo
	 * @return
	 */
	@ApiOperation(value = "获取第三方绑定集合",response=Result.class)
	@GetMapping("getBingding")
	public Object getBingding() {
		return Result.success(userService.getSdkLoginInfo());
	}
	
	/**
	 * 获取微信openid等信息
	 * @return
	 */
	@ApiOperation(value = "获取微信openid等信息",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "code", value = "code", required = true, paramType = "query")})
	@GetMapping("getOpenids")
	public Object getOpenids(@RequestParam String code) {
		return Result.success(userService.getWxOpenId(code));
	}
	
	
	/**
	 * 绑定手机
	 * @param loginInfo
	 * @return
	 */
	@ApiOperation(value = "修改手机",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "mobile", value = "新手机号", required = true, paramType = "query"),
		})
	@PostMapping("updateMoblie")
	public Object updateMoblie(@RequestParam String mobile) {
		long mobileCount = userService.mobileCount(mobile);
		if (mobileCount>0) {
			return Result.failure(ResultCode.BINGDINGMOBILED);
		} else {
			userService.updateMobile(mobile);
			return Result.success(mobile);
		}
	}
	
	@ApiOperation(value = "获取用户信息（优先从缓存获取）",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "userId", value = "userId", required = true, paramType = "query") })
	@GetMapping("get")
	public Object get(@RequestParam Integer userId){
		User u=userService.getUser(userId);
		return Result.success(u);
	}
	@ApiOperation(value = "获取用户信息根据accid",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "toAccid", value = "toAccid", required = true, paramType = "query") })
	@GetMapping("getByAccid")
	public Object getByAccid(@RequestParam String toAccid){
		
		User u=userService.getUser(toAccid,toAccid);
		return Result.success(u);
	}
	@ApiOperation(value = "登录",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "mobile", value = "手机号", required = true, paramType = "query")
	,@ApiImplicitParam(name = "account", value = "友讯号", paramType = "query"),
	@ApiImplicitParam(name = "password", value = "密码", paramType = "query"),
	@ApiImplicitParam(name = "loginType", value = "登录类型(0：账号密码登录，1：短信验证登录,2：友讯号登录)", paramType = "query"),
	@ApiImplicitParam(name = "smsCode", value = "短信验证码", paramType = "query"),
	})
	@PostMapping("login")
	public Object login(@RequestParam(defaultValue="") String mobile,@RequestParam(defaultValue="") String account,@RequestParam(defaultValue="") String password,@RequestParam(defaultValue="") String smsCode,@RequestParam(defaultValue="-1") int loginType){
		User user=new User();
		user.setMobile(mobile);
		user.setAccount(account);
		user.setPassword(password);
		user.setLoginType(loginType);
		user.setSmsCode(smsCode);
		Map<String, Object> u=userService.login(user);
		return Result.success(u);
	}
	@ApiOperation(value = "密码验证",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "pwd", value = "密码", required = true, paramType = "query"),
    @ApiImplicitParam(name = "type", value = "密码类型(1登录密码，2支付密码)", required = true, paramType = "query"),
		})
	@PostMapping("validPwd")
	public Object validPwd(String pwd,int type){
		Integer userId = ReqUtil.getUserId();
		User userFromDB = userService.getUserFromDB(userId);
		if(type==1) {
			if(pwd.equals(userFromDB.getPassword())) {
				return Result.success();
			}
		}else if(type==2) {
			if(pwd.equals(userFromDB.getPayPassword())) {
				return Result.success();
			}
		}else {
			Result.error("验证类型错误");
		}
		return Result.error("密码错误");
	}
	@ApiOperation(value = "退出登录",response=Result.class)
	@DeleteMapping("logout")
	public Object logout(){
		Integer userId = ReqUtil.getUserId();
		// 维护redis中的数据
		KSessionUtil.removeAccessToken(userId);
		KSessionUtil.deleteUserByUserId(userId);
		return Result.success();
	}
	@ApiOperation(value = "修改密码",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "mobile", value = "手机号", required = true, paramType = "query"),
	@ApiImplicitParam(name = "password", value = "密码", required = true, paramType = "query"),
	@ApiImplicitParam(name = "newPassword", value = "新密码", required = true, paramType = "query"),
	@ApiImplicitParam(name = "type", value = "密码类型(1登录密码，2支付密码)", required = true, paramType = "query"),
	})
	@PostMapping("updatePwd")
	public Object updatePwd(@RequestParam int type,@RequestParam String mobile,@RequestParam String password,@RequestParam String newPassword){
	
		Query<User> q = repository.createQuery();
		
		q.field("mobile").equal(mobile);
		if(type==1) 
			q.field("password").equal(password);
		else if(type==2)
			q.field("payPassword").equal(password);
		else
			return Result.error("修改失败，密码类型错误");
			
		User u = repository.findOne(q);
		if(u!=null&&StringUtils.isNotBlank(u.getAccid())) {
			if(type==1) 
				u.setPassword(newPassword);
			else
				u.setPayPassword(newPassword);
			
			
			repository.save(u);
			updatePwdTosdk(u);
		}else {
			return Result.failure(ResultCode.USER_LOGIN_ERROR);
		}
		return Result.success(u);
	}

	private void updatePwdTosdk(User u) {
		if(StringUtils.isNotBlank(u.getEx())) {
			JSONObject exs = JSONObject.parseObject(u.getEx());
			exs.put("password", u.getPassword());
			exs.put("payPassword", u.getPayPassword());
		}
		com.youxin.app.yx.request.User.User uq=new com.youxin.app.yx.request.User.User();
		BeanUtils.copyProperties(u, uq);
		SDKService.updateUinfo(uq);
	}
	@ApiOperation(value = "修改密码_短信",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "mobile", value = "手机号", required = true, paramType = "query")
	,@ApiImplicitParam(name = "smsCode", value = "短信验证码", required = true, paramType = "query"),
	@ApiImplicitParam(name = "newPassword", value = "新密码", required = true, paramType = "query"),
	@ApiImplicitParam(name = "type", value = "密码类型(1登录密码，2支付密码)", required = true, paramType = "query"),
	})
	@PostMapping("updatePwdForSms")
	public Object updatePwdForSms(@RequestParam int type,@RequestParam String mobile,@RequestParam String smsCode,@RequestParam String newPassword){
		
		Query<User> q = repository.createQuery();
		q.field("mobile").equal(mobile);
		User u = repository.findOne(q);
		if(u!=null&&StringUtils.isNotBlank(u.getAccid())) {
			if (!sendSms.isAvailable("86"+mobile, smsCode))
				throw new ServiceException("短信验证码不正确!");
			if(type==1) 
				u.setPassword(newPassword);
			else if(type==2)
				u.setPayPassword(newPassword);
			else
				return Result.error("修改失败，密码类型错误");
			repository.save(u);
			updatePwdTosdk(u);
		}else {
			return Result.failure(ResultCode.USER_NOT_EXIST);
		}
		return Result.success(u);
	}
	@ApiOperation(value = "初始化密码",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "payPassword", value = "密码", required = true, paramType = "query")
	})
	@PostMapping("setPayPassword")
	public Object setPayPassword(@RequestParam(defaultValue="") String payPassword){
		if(StringUtil.isEmpty(payPassword)) 
			return Result.error("请输入密码");

		User u = userService.getUserFromDB(ReqUtil.getUserId());
		if(u!=null&&StringUtil.isEmpty(u.getPayPassword())) {
			u.setPayPassword(payPassword);
			repository.save(u);
			KSessionUtil.saveUserByUserId(u.getId(), u);
		}else {
			return Result.error("密码已存在，不可初始化");
		}
		return Result.success();
	}
	@ApiOperation(value = "刷新token(云信指定token)",response=Result.class)
	@PostMapping("refreshToken")
	public Object refreshToken(@RequestParam(required=true) String accid){
		JSONObject refreshToken = SDKService.refreshToken(accid);
		if(refreshToken.getIntValue("code")==200) {
			User u = repository.findOne("accid", accid);
			u.setToken(refreshToken.getJSONObject("info").getString("token"));
			//更新本地数据库
			repository.save(u);
			//更新redis
			u.setPassword("");
			KSessionUtil.saveUserByUserId(u.getId(), u);
			return Result.success(refreshToken.getJSONObject("info"));
		}
		return Result.errorMsg(refreshToken.toJSONString());
	}
	
	@ApiOperation(value = "封禁用户",response=Result.class)
	@PostMapping("blockUser")
	public Object blockUser(@RequestParam(required=true) String accid,@RequestParam() String needkick){
		JSONObject refreshToken = SDKService.block(accid, needkick);
		if(refreshToken.getIntValue("code")==200) {
			return Result.success();
		}
		return Result.error();
	}
	@ApiOperation(value = "解禁用户",response=Result.class)
	@PostMapping("unblockUser")
	public Object unblockUser(@RequestParam(required=true) String accid){
		JSONObject refreshToken = SDKService.unblock(accid);
		if(refreshToken.getIntValue("code")==200) {
			return Result.success();
		}
		return Result.errorMsg(refreshToken.toJSONString());
	}
	@ApiOperation(value = "更新用户基本信息",response=Result.class)
	@PostMapping("updateUinfo")
	public Object updateUinfo(@RequestBody(required=true) com.youxin.app.yx.request.User.User user){
		if(StringUtils.isBlank(user.getAccid())) {
			return Result.errorMsg("accid不能为空");
		}
		User u = repository.findOne("accid", user.getAccid());
		if(u!=null&&StringUtils.isNotBlank(u.getAccid())) {
			JSONObject json = SDKService.updateUinfo(user);
			if(json.getIntValue("code")==200) {
				BeanUtils.copyProperties(user, u,"id","mobile","token");
				//更新本地数据库
				repository.save(u);
				//更新redis
				u.setPassword("");
				KSessionUtil.saveUserByUserId(u.getId(), u);
				return Result.success();
			}else
				return Result.errorMsg(json.toJSONString());
		}
		return Result.error();
	}
	@ApiOperation(value = "设置黑名单/静音 拉黑/取消拉黑；设置静音/取消静音",response=Result.class)
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
				return Result.success();
			}
		
		return Result.errorMsg(json.toJSONString());
	}
	
	@ApiOperation(value = "查看用户的黑名单和静音列表",response=Result.class)
	@PostMapping("listBlackAndMuteList")
	public Object listBlackAndMuteList(@RequestParam String accid){

			JSONObject json = SDKService.listBlackAndMuteList(accid);
			if(json.getIntValue("code")==200) {
				JSONObject jsonr=new JSONObject();
				jsonr.put("mutelist", json.get("mutelist"));
				jsonr.put("blacklist", json.get("blacklist"));
				return Result.success(jsonr);
			}
		
		return Result.errorMsg(json.toJSONString());
	}
	
	@ApiOperation(value = "搜索用户",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "mobile", value = "手机号",  paramType = "query"),
	@ApiImplicitParam(name = "account", value = "友讯号",  paramType = "query")
	})
	@GetMapping("searchUser")
	public Object searchUser(@RequestParam(defaultValue="") String mobile,@RequestParam(defaultValue="") String account){
		UserVo vo=new UserVo();
		if (!StringUtil.isEmpty(mobile))
			vo.setMobile(mobile);
		if (!StringUtil.isEmpty(account))
			vo.setAccount(account);
		List<DBObject> u = userService.queryUser(vo);
		return Result.success(u);
	}
	
	@ApiOperation(value = "修改友讯号",response=Result.class)
	@ApiImplicitParams({@ApiImplicitParam(name = "account", value = "友讯号",  paramType = "query")
	})
	@PostMapping("updateAccount")
	public Object updateAccount(@RequestParam(defaultValue="") String account){
		return Result.success(userService.updateAccount(account));
	}
	
	@ApiOperation(value = "修改用户隐私设置",response=Result.class)
	@PostMapping("updateSettings")
	public Object updateSettings(@RequestBody UserSettings settings){
		userService.updateSettings(settings);
		return Result.success();
	}
	
	@ApiOperation(value = "第三方加好友，如小程序",response=Result.class)
	@PostMapping("addByOther")
	public Object addByOther(@RequestParam String name,@RequestParam String icon,@RequestParam String loginInfo,@RequestParam int sdkType,@RequestParam String accid){
		SdkLoginInfo sdkLoginInfo = userService.findSdkLoginInfo(sdkType, loginInfo);
		log.debug(loginInfo+"=="+sdkType+"=="+accid);
		Friends f=new Friends();
		User newUser=new User();
		f.setAccid(accid);
		f.setType(1);
		f.setMsg("小程序添加好友");
		if(sdkLoginInfo!=null) {
			log.debug("已注册添加好友开始");
			//已经注册
			f.setFaccid(sdkLoginInfo.getAccid());
			SDKService.friendAdd(f);
			log.debug("已注册添加好友结束");
		}else {
			log.debug("未注册添加好友开始");
			//未注册
			newUser.setName(name);
			newUser.setIcon(icon);
			newUser.setMobile(loginInfo);
			newUser.setPassword(Md5Util.md5Hex("123456"));
			Map<String, Object> register = userService.register(newUser);
			String newAccid = register.get("accid").toString();
			
			f.setFaccid(newAccid);
			SDKService.friendAdd(f);
			log.debug("未注册添加好友结束");
			return Result.success(register);
		}
		return Result.success();
	}
	
	

}
