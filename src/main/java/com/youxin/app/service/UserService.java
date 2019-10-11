package com.youxin.app.service;

import java.util.List;
import java.util.Map;

import com.mongodb.DBObject;
import com.youxin.app.entity.User;
import com.youxin.app.entity.UserVo;


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
	 * 根据指定字段修改用户信息
	 * @param bean
	 * @return
	 */
	void updateUser(User bean);
	
	/**
	 * 获取用户信息
	 * @param accid
	 * @param toaccid
	 * @return
	 */
	User getUser(String accid,String toaccid);
	
	/**
	 * 获取用户信息
	 * @param userId
	 * @return
	 */
	User getUser(Integer userId);
	
	/**
	 * 获取用户昵称
	 * @param userId
	 * @return
	 */
	String getUserName(Integer userId);
	
	Map<String, Object> login(User bean);
	/**
	 * 手机注册数量
	 * @param mobile
	 * @return
	 */
	long mobileCount(String mobile);
	
	Double rechargeUserMoeny(Integer userId, Double money, int type);
	
	/**
	 * 查询指定字段的
	 * @param example
	 * @return
	 */
	List<DBObject> queryUser(UserVo example);
	/**
	 * 通过id直接从数据库获取用户信息
	 * @param userId
	 * @return
	 */
	User getUserFromDB(Integer userId);
}
