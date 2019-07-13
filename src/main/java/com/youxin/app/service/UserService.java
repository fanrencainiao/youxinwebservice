package com.youxin.app.service;

import java.util.Map;

import com.youxin.app.entity.User;


public interface UserService {
	/**
	 * 注册
	 * @param bean
	 * @return
	 */
	Map<String, Object> register(User bean);
	/**
	 * 同步添加用户
	 * @param bean
	 * @return
	 */
	Map<String, Object> addUser(User bean);
	/**
	 * 获取用户信息
	 * @param accid
	 * @param toaccid
	 * @return
	 */
	User getUser(String accid,String toaccid);
	
	/**
	 * 获取用户信息
	 * @param accid
	 * @param toaccid
	 * @return
	 */
	User getUser(String accid);
	
	Map<String, Object> login(User bean);
	
	long mobileCount(String mobile);
}
