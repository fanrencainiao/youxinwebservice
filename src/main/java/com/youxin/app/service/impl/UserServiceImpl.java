package com.youxin.app.service.impl;

import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.internal.operation.WriteConcernHelper;
import com.youxin.app.entity.User;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.repository.UserRepository;
import com.youxin.app.service.UserService;
import com.youxin.app.utils.KSessionUtil;
import com.youxin.app.utils.ResultCode;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.yx.SDKService;



@Service
public class UserServiceImpl implements UserService {
	@Autowired
	private UserRepository repository;

	@Override
	public Map<String, Object> register(User bean) {
		Map<String, Object> map=addUser(bean);
		return map;
	}
	
	public synchronized Map<String, Object> addUser(User bean) {
		String accid=StringUtil.randomUUID();
		bean.setAccid(accid);
		
		if(StringUtil.isEmpty(bean.getMobile())) {
			throw new ServiceException(0, "手机号必填");
		}
		long mobileCount = this.mobileCount(bean.getMobile());
		if(mobileCount>=1) {
//			throw new ServiceException(0, "手机号已被注册");
		}
		
		//sdk注册
		JSONObject json= SDKService.createUser(bean);
		User us=JSONObject.toJavaObject(json, User.class);
		//注册成功
		bean.setToken(us.getToken());
		if(StringUtil.isEmpty(bean.getToken())) {
			throw new ServiceException(0, "token缺失，注册失败");
		}
		//保存本地数据库
		repository.save(bean);
		//缓存用户token
		Map<String, Object> data = KSessionUtil.loginSaveAccessToken(accid, accid, null);
		data.put("accid", bean.getAccid());
		data.put("name", bean.getName());
		data.put("birth", bean.getBirth());
		data.put("icon", bean.getIcon());
		data.put("token", bean.getToken());
		data.put("mobile", bean.getMobile());
		
		return data;
	}

	@Override
	public User getUser(String accid, String toaccid) {
		
		return null;
	}
	
	public User getUser(String accid) {
		// 先从 Redis 缓存中获取
		User user = KSessionUtil.getUserByUserId(accid);
		if (null == user) {
			user = repository.findOne("accid", accid);
			if (null == user) {
				System.out.println("accid为" + accid + "的用户不存在");
				return null;
			}
			KSessionUtil.saveUserByUserId(accid, user);
		}

		return user;
	}

	@Override
	public Map<String, Object> login(User bean) {
		User user = null;
		if (!StringUtil.isEmpty(bean.getAccid()))
			user = repository.findOne("accid", bean.getAccid());
		else {
			user = repository.findOne("mobile", bean.getMobile());
			
		}
		if (null == user) {
			throw new ServiceException(20004, "帐号不存在, 请注册!");
		}  else {
			if(bean.getLoginType()==0) {
			
				// 账号密码登录
				String password = bean.getPassword();
				if (!password.equals(user.getPassword()))
					throw new ServiceException(20002, "帐号或密码错误");
			} else if (1 == bean.getLoginType()) {
				// 短信验证码登录
//				if (null == example.getVerificationCode())
//					throw new ServiceException("短信验证码不能为空!");
//				if (!SKBeanUtils.getSMSService().isAvailable(user.getTelephone(), example.getVerificationCode()))
//					throw new ServiceException("短信验证码不正确!");
			}else {
				throw new ServiceException(20002, "登录方式错误");
			}
			
		}
		KSessionUtil.saveUserByUserId(bean.getAccid(), user);
		Map<String, Object> data = KSessionUtil.loginSaveAccessToken(bean.getAccid(), bean.getAccid(), null);
		Object token = data.get("access_token");
		data.put("accid", user.getAccid());
		data.put("name", user.getName());
		data.put("birth", user.getBirth());
		data.put("icon", user.getIcon());
		data.put("token", user.getToken());
		data.put("mobile", user.getMobile());
		return data;
	}

	@Override
	public long mobileCount(String mobile) {
		
		return repository.count("mobile", mobile);
	}

	
	

}
