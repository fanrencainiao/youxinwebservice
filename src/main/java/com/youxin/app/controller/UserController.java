package com.youxin.app.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBObject;
import com.youxin.app.entity.NearbyUser;
import com.youxin.app.entity.Opinion;
import com.youxin.app.entity.SdkLoginInfo;
import com.youxin.app.entity.User;
import com.youxin.app.entity.User.DeviceInfo;
import com.youxin.app.entity.User.LoginLog;
import com.youxin.app.entity.User.MyFreids;
import com.youxin.app.entity.User.MyItemCode;
import com.youxin.app.entity.User.MyTeam;
import com.youxin.app.entity.User.UserSettings;
import com.youxin.app.entity.UserVo;
import com.youxin.app.entity.exam.BaseExample;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.repository.UserRepository;
import com.youxin.app.service.UserService;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KSessionUtil;
import com.youxin.app.utils.Md5Util;
import com.youxin.app.utils.MongoUtil;
import com.youxin.app.utils.PageVO;
import com.youxin.app.utils.ReqUtil;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.ResultCode;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.alipay.util.AliPayUtil;
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
			if(StringUtil.isEmpty(user.getLoginInfo())) {
				throw new ServiceException(0, "手机号已被注册");
			}else {
				// 第三方绑定
				SdkLoginInfo findSdkLoginInfo = userService.findSdkLoginInfo(user.getSdkType(), user.getLoginInfo());
//				return q.asList();
				if(findSdkLoginInfo==null) {
					User ubm = userService.getUserByMobile(user.getMobile());
					Query<SdkLoginInfo> q = dfds.createQuery(SdkLoginInfo.class).field("userId").equal(ubm.getId());
					if(!CollectionUtils.isEmpty(q.asList())) {
						throw new ServiceException(0, "此用户已绑定有第三方账户");
					}
					SdkLoginInfo sdkLoginInfo=new SdkLoginInfo();
					sdkLoginInfo.setCreateTime(DateUtil.currentTimeSeconds());
					sdkLoginInfo.setLoginInfo(user.getLoginInfo());
					sdkLoginInfo.setType(user.getSdkType());
					sdkLoginInfo.setUserId(ubm.getId());
					sdkLoginInfo.setAccid(ubm.getAccid());
					dfds.save(sdkLoginInfo);
					ubm.setLoginLog(user.getLoginLog());
					return Result.success(userService.saveLoginInfo(ubm));
				}else {
					throw new ServiceException(0, "账户已绑定");
				}	
			}
			
		}
		//注册与绑定注册
		Map<String, Object> data=userService.register(user);
		return Result.success(data);
	}
	
	@ApiOperation(value = "sdk登录",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "loginInfo", value = "sdk登录标识", required = true, paramType = "query"),
		 @ApiImplicitParam(name = "sdkType", value = "第三方类型：1qq, 2微信", required = true, paramType = "query")})
	@PostMapping("sdkLogin")
	public Object sdkLogin(@RequestParam int sdkType,
			@RequestParam String loginInfo,@RequestBody LoginLog loginlog) {
		SdkLoginInfo sdkLoginInfo = userService.findSdkLoginInfo(sdkType, loginInfo);
		if (sdkLoginInfo != null) {
			User user = userService.getUserFromDB(sdkLoginInfo.getUserId());
			user.setLoginType(3);
			user.setLoginLog(loginlog);
			Object data = userService.login(user);
			return Result.success(data);
		} else {
			// 未绑定手机号码
			return Result.failure(ResultCode.UNBindingTelephone);
		}
	}
	@ApiOperation(value = "注销",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "pwd", value = "密码验证（md5加密）", required = true, paramType = "query"),
		})
	@DeleteMapping("delUser")
	public Object delUser(@RequestParam String pwd) {
		User relUser = userService.getUserFromDB(ReqUtil.getUserId());
		if(!pwd.equals(relUser.getPassword()))
			return Result.error("密码错误");
		User user=new User();
		//设置为注销状态
		user.setIsDelUser(1);
		//手机号：手机号+用户id
		user.setMobile(relUser.getMobile()+relUser.getId());
		//密码：yxdel+用户id
		user.setPassword("yxdel"+relUser.getId());
		user.setId(ReqUtil.getUserId());
		userService.updateUserByEle(user);
		logout();
		
		return Result.success();
		
	}
	/**
	 * 绑定
	 * @param loginInfo
	 * @return
	 */
	@ApiOperation(value = "绑定第三方sdk",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "loginInfo", value = "sdk登录标识", required = true, paramType = "query"),
		 @ApiImplicitParam(name = "sdkType", value = "第三方类型：1qq, 2微信,3支付宝", required = true, paramType = "query")})
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
			// 账户已经被绑定
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
	 * 组装支付宝授权信息
	 * @return
	 */
	@ApiOperation(value = "组装支付宝授权信息",response=Result.class)
	@GetMapping("getAliAuthInfo")
	public Object getAliAuthInfo() {
		return Result.success(AliPayUtil.getAuthInfoStr());
	}
	
	/**
	 * 获取支付宝accesstoken信息
	 * @return
	 */
	@ApiOperation(value = "获取支付宝accesstoken信息",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "code", value = "code", required = true, paramType = "query")})
	@GetMapping("getAliAccesstoken")
	public Object getAliAccesstoken(@RequestParam String code) {
		return Result.success(AliPayUtil.getAccesstoken(code, null));
	}
	/**
	 * 获取支付宝用户授权信息
	 * @return
	 */
	@ApiOperation(value = "获取支付宝用户信息",response=Result.class,notes="若返回支付宝用户id则授权成功，否则失败")
	@GetMapping("getAliUserInfo")
	public Object getAliUserInfo() {
		User reluser = userService.getUserFromDB(ReqUtil.getUserId());
		String aliUserAuthInfo = AliPayUtil.getAliUserInfo(reluser.getAliAppAuthToken());
		if (StringUtil.isEmpty(aliUserAuthInfo)) 
			return Result.error("无授权信息");
		return Result.success(aliUserAuthInfo);
	}
	
	
	/**
	 * 绑定手机
	 * @param loginInfo
	 * @return
	 */
	@ApiOperation(value = "修改手机",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "mobile", value = "新手机号", required = true, paramType = "query"),
		@ApiImplicitParam(name = "smsCode", value = "手机验证码", required = true, paramType = "query"),
		})
	@PostMapping("updateMoblie")
	public Object updateMoblie(@RequestParam String mobile,@RequestParam String smsCode) {
		long mobileCount = userService.mobileCount(mobile);
		if (StringUtil.isEmpty(smsCode)) {
			throw new ServiceException(0, "短信验证码必填");
		}
		if (!sendSms.isAvailable("86" + mobile, smsCode))
			throw new ServiceException("短信验证码不正确!");
		if (mobileCount>0) {
			return Result.failure(ResultCode.BINGDINGMOBILED);
		} else {
			userService.updateMobile(mobile);
			return Result.success(StringUtil.phoneEncryption(mobile));
		}
	}
	
	@ApiOperation(value = "获取用户信息（优先从缓存获取）",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "userId", value = "userId", required = true, paramType = "query") })
	@GetMapping("get")
	public Object get(@RequestParam Integer userId){
		User u=userService.getUser(userId);
		return Result.success(u);
	}
//	@ApiOperation(value = "获取用户信息根据accid",response=Result.class)
//	@ApiImplicitParams({ @ApiImplicitParam(name = "toAccid", value = "toAccid", required = true, paramType = "query") })
//	@GetMapping("getByAccid")
//	public Object getByAccid(@RequestParam String toAccid){
//		
//		User u=userService.getUser(toAccid,toAccid);
//		return Result.success(u);
//	}
	@ApiOperation(value = "登录",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "mobile", value = "手机号", required = true, paramType = "query")
	,@ApiImplicitParam(name = "account", value = "友讯号", paramType = "query"),
	@ApiImplicitParam(name = "password", value = "密码", paramType = "query"),
	@ApiImplicitParam(name = "loginType", value = "登录类型(0：账号密码登录，1：短信验证登录,2：友讯号登录)", paramType = "query"),
	@ApiImplicitParam(name = "smsCode", value = "短信验证码", paramType = "query"),
	})
	@PostMapping("login")
	public Object login(@RequestParam(defaultValue="") String mobile,@RequestParam(defaultValue="") String account,@RequestParam(defaultValue="") String password,@RequestParam(defaultValue="") String smsCode,@RequestParam(defaultValue="-1") int loginType,@RequestBody LoginLog loginInfo){
		User user=new User();
		user.setMobile(mobile);
		user.setAccount(account);
		user.setPassword(password);
		user.setLoginType(loginType);
		user.setSmsCode(smsCode);
		user.setLoginLog(loginInfo);
		Map<String, Object> u=userService.login(user);
		return Result.success(u);
	}
	@ApiOperation(value = "修改登录信息",response=Result.class)
	@PostMapping("login/upd")
	public Object loginUpd(DeviceInfo info,LoginLog log) {

		Integer userId = ReqUtil.getUserId();
		userService.saveLoginToken(userId, info,log);
		KSessionUtil.saveAndroidToken(userId, info);

		return Result.success();
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
		userService.updateLoginoutLogTime(userId);
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
	public Object updateUinfo( com.youxin.app.yx.request.User.User user, BaseExample be){
		if(StringUtils.isBlank(user.getAccid())) {
			return Result.errorMsg("accid不能为空");
		}
		User u = repository.findOne("accid", user.getAccid());
		if(u!=null&&StringUtils.isNotBlank(u.getAccid())) {
			JSONObject json = SDKService.updateUinfo(user);
			if(json.getIntValue("code")==200) {
				BeanUtils.copyProperties(user, u,"id","mobile","token","ex");
				//更新本地数据库
				repository.save(u);
				User u1=new  User();
				u1.setId(ReqUtil.getUserId());
				BeanUtils.copyProperties(be, u1);
				userService.updateUserByEle(u1);
				return Result.success();
			}else
				return Result.errorMsg(json.toJSONString());
		}
		
		return Result.success();

	}
	@ApiOperation(value = "更新支付宝用户id",response=Result.class)
	@PostMapping("updateAliUserId")
	public Object updateAliUserId(@RequestParam String aliUserId){
		if(StringUtils.isBlank(aliUserId)) {
			return Result.errorMsg("更新授权失败");
		}
		User user = new User();
		user.setAliUserId(aliUserId);
		user.setId(ReqUtil.getUserId());
		userService.updateUserByEle(user);
		return Result.success();
	}
	@ApiOperation(value = "更新支付宝用户id",response=Result.class)
	@PostMapping("updateAliUserIdV1")
	public Object updateAliUserIdV1(@RequestParam String code){
		String token = AliPayUtil.getAccesstoken(code, null);
		if(StringUtil.isEmpty(token)) 
			return Result.errorMsg("token不能为空");
		String aliUserId = AliPayUtil.getAliUserInfo(token);
		if(StringUtil.isEmpty(aliUserId)) 
			return Result.errorMsg("授权失败");
		User user = new User();
		user.setAliUserId(aliUserId);
		user.setAliAppAuthToken(token);
		user.setId(ReqUtil.getUserId());
		userService.updateUserByEle(user);
		return Result.success();
	}
//	@ApiOperation(value = "更新用户地址",response=Result.class)
//	@PostMapping("updateAddr")
//	public Object updateAddr(@RequestBody(required=true) BaseExample be){
//		User u=new  User();
//		u.setId(ReqUtil.getUserId());
//		BeanUtils.copyProperties(be, u);
//		userService.updateUserByEle(u);
//		return Result.success();
//	}
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
	@ApiOperation(value = "保存或者修改用户好友添加方式",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "addType", value = "添加方式1二维码添加对方，2搜索手机号，3有讯号，4群聊，5名片分享，6附近的人",  paramType = "query"),
	@ApiImplicitParam(name = "toUserId", value = "好友userid",  paramType = "query")
	})
	@PostMapping("addType")
	public Object addType(@RequestParam int addType,@RequestParam int toUserId){
		MyFreids mf = dfds.createQuery(MyFreids.class).field("userId").equal(ReqUtil.getUserId()).field("toUserId").equal(toUserId).get();
		MyFreids tomf = dfds.createQuery(MyFreids.class).field("userId").equal(toUserId).field("toUserId").equal(ReqUtil.getUserId()).get();
		
		if(mf==null) 
			mf=new MyFreids();
		mf.setAddType(addType);
		mf.setUserId(ReqUtil.getUserId());
		mf.setToUserId(toUserId);
		dfds.save(mf);
		if(tomf==null) 
			tomf=new MyFreids();
		tomf.setAddType(-addType);
		tomf.setUserId(toUserId);
		tomf.setToUserId(ReqUtil.getUserId());
		dfds.save(tomf);
		return Result.success();
	}
	@ApiOperation(value = "获取用户好友信息",response=Result.class)
	@ApiImplicitParams({ 
		@ApiImplicitParam(name = "toUserId", value = "好友userid",  paramType = "query")
	})
	@GetMapping("getFriedsInfo")
	public Object getFriedsInfo(@RequestParam Integer toUserId){
		Map<String, Object> data=new HashMap<String, Object>();
		MyFreids myFreids = dfds.createQuery(MyFreids.class).field("userId").equal(ReqUtil.getUserId()).field("toUserId").equal(toUserId).get();
		if(myFreids!=null)
			data.put("addType", myFreids.getAddType());
		else
			data.put("addType", 0);
		return Result.success(data);
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
	@ApiOperation(value = "刷新二维码",response=Result.class)
	@PostMapping("refreshCode")
	public Object refreshCode(){
		String codeSign=StringUtil.randomString(4);
		User user = new User();
		user.setId(ReqUtil.getUserId());
		user.setCodeSign(codeSign);
		userService.updateUserByEle(user);
		return Result.success(codeSign);
	}
	@ApiOperation(value = "刷新群二维码",response=Result.class)
	@PostMapping("refreshItemCode")
	public Object refreshItemCode(@RequestParam Long itemId){
		String codeSign=StringUtil.randomString(6);
		MyItemCode mic=new MyItemCode();
		mic.setTeamId(itemId);
		mic.setErCode(codeSign);
		dfds.save(mic);
		return Result.success(codeSign);
	}
	@ApiOperation(value = "获取群信息",response=Result.class)
	@GetMapping("getItemInfo")
	public Object getItemInfo(@RequestParam Long itemId){
		Map<String, Object> data=new HashMap<String, Object>();
		MyItemCode myItemCode = dfds.createQuery(MyItemCode.class).field("_id").equal(itemId).get();
		if(myItemCode!=null)
			data.put("erCode", myItemCode.getErCode());
		else
			data.put("erCode", 0);
		return Result.success(data);
	}

	@ApiOperation(value = "用户举报",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "toUserId", value = "举报人",  paramType = "query"),
		 @ApiImplicitParam(name = "webUrl", value = "举报网址",  paramType = "query"),
		 @ApiImplicitParam(name = "roomId", value = "举报群组",  paramType = "query"),
	@ApiImplicitParam(name = "reason", value = "原因id",  paramType = "query")
	})
	@PostMapping("report")
	public Result report(@RequestParam(defaultValue = "0") Integer toUserId,
			@RequestParam(defaultValue = "0") Long roomId, @RequestParam(defaultValue = "") String webUrl,
			@RequestParam(defaultValue = "0") int reason) {
		userService.report(ReqUtil.getUserId(), toUserId, reason, roomId, webUrl);
		return Result.success();
	}

	@ApiOperation(value = "检测举报网址",response=Result.class)
	@ApiImplicitParams({ 
		 @ApiImplicitParam(name = "webUrl", value = "举报群组",  paramType = "query")
	})
	@GetMapping("checkReportUrl")
	public Result checkReportUrl(@RequestParam(defaultValue = "") String webUrl, HttpServletResponse response)
			throws IOException {
		boolean flag;
		if (StringUtil.isEmpty(webUrl)) {
			return Result.error("webUrl is null");
		} else {
			try {
				// urlEncode 解码
				webUrl = URLDecoder.decode(webUrl);
				flag = userService.checkReportUrlImpl(webUrl);
				return Result.success(flag);
			} catch (ServiceException e) {
//				response.sendRedirect("/pages/report/prohibit.html");
				return Result.error(e.getMessage());
			}
		}

	}
	
	@ApiOperation(value = "附近的人",response=Result.class)
	@GetMapping(value = "/near")
	public Object nearbyUser(@ModelAttribute NearbyUser poi) {
		try {
			List<User> nearbyUser =null;
			nearbyUser=userService.nearbyUser(poi);
			if(null != nearbyUser && nearbyUser.size()>0)
				return Result.success(nearbyUser);
			else
				return Result.error("附近无用户");
			
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}
		
	}
	@ApiOperation(value = "意见提交",response=Result.class)
	@PostMapping(value = "/putOpinion")
	public Object putOpinion(@RequestParam(defaultValue="") String opinion) {
		try {
			Opinion o=new Opinion();
			o.setUserId(ReqUtil.getUserId());
			o.setOpinion(opinion);
			o.setCreateTime(DateUtil.currentTimeSeconds());
			dfds.save(o);
			return Result.success();
		} catch (Exception e) {
			return Result.error("提交失败");
		}
		
	}
	
	
	@ApiOperation(value = "用户保存的群组",response=Result.class)
	@PostMapping(value = "/teamList")
	public Result teamList() {
		try {
			Query<MyTeam> q = createMyteamQuery();
			return Result.success(q.get().getTeams());
		} catch (Exception e) {
			return Result.error("无数据");
		}
		
	}
	@ApiOperation(value = "添加到我的群组",response=Result.class)
	@PostMapping(value = "/saveTeam")
	public Object saveTeam(@RequestParam(defaultValue="") Long teamId) {
		try {
			Query<MyTeam> q = createMyteamQuery();
			MyTeam teams = q.get();
			if(teams==null) {
				teams=new MyTeam();
				teams.setUserId(ReqUtil.getUserId());
				teams.setTeams(new HashSet<>());
			}
			Set<Long> teams2 = teams.getTeams();
			if(teams2==null) {
				teams2=new HashSet<>();
			}
			teams2.add(teamId);
			teams.setTeams(teams2);
			dfds.save(teams);
			return Result.success();
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("系统繁忙");
		}
		
	}
	@ApiOperation(value = "从我的群组移除",response=Result.class)
	@PostMapping(value = "/removeTeam")
	public Object removeTeam(@RequestParam(defaultValue="") Long teamId) {
		try {
			Query<MyTeam> q = createMyteamQuery();
			MyTeam teams = q.get();
			teams.getTeams().remove(teamId);
			dfds.save(teams);
			return Result.success();
		} catch (Exception e) {
			return Result.error("系统繁忙");
		}
		
	}

	private Query<MyTeam> createMyteamQuery() {
		Query<MyTeam> q = dfds.createQuery(MyTeam.class);
		q.field("_id").equal(ReqUtil.getUserId());
		return q;
	}
	
	@ApiOperation(value = "获取所有用户",response=Result.class)
	@ApiImplicitParams({ 
		 @ApiImplicitParam(name = "keyWorld", value = "姓名，手机号，id",  paramType = "query")
	})
	@GetMapping(value = "/getAllUser")
	public Object getAllUser(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int limit, 
			@RequestParam(defaultValue = "") String keyWorld) {
		try {
			Integer sysuserid = ReqUtil.getUserId();
			if(sysuserid==null|| sysuserid>10000)
				return Result.error("非法操作");
			Query<User> query = repository.createQuery();

			if (!StringUtil.isEmpty(keyWorld)) {
				Integer userId = null;
				if (StringUtil.isNumeric(keyWorld)&&keyWorld.length()<10) {
					userId = Integer.valueOf(keyWorld);
				}
				
				query.or(query.criteria("name").containsIgnoreCase(keyWorld), query.criteria("_id").equal(userId),
						query.criteria("mobile").containsIgnoreCase(keyWorld));
			}
			query.field("_id").greaterThan(10000);
			// 排序、分页
			List<User> pageData = query.order("name").asList(MongoUtil.pageFindOption(page, limit));
			return Result.success(new PageVO(pageData, query.count(), page, limit));
		} catch (Exception e) {
			return Result.error("系统繁忙");
		}
		
	}

}
