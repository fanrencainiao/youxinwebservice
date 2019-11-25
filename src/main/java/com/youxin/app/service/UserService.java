package com.youxin.app.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBObject;
import com.youxin.app.entity.SdkLoginInfo;
import com.youxin.app.entity.User;
import com.youxin.app.entity.User.UserSettings;
import com.youxin.app.entity.UserVo;
import com.youxin.app.utils.PageResult;


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
	 * 禁用用户解禁用户
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
	/**
	 * 通过accid直接从数据库获取用户信息
	 * @param userId
	 * @return
	 */
	User getUserFromDB(String accid);
	/**
	 * 通过手机号直接从数据库获取用户信息
	 * @param userId
	 * @return
	 */
	User getUserByMobile(String mobile);
	/**
	 * 更改有讯号
	 * @param account
	 */
	User updateAccount(String account);
	/**
	 * 更改手机号
	 * @param account
	 */
	void updateMobile(String mobile);
	/**
	 * 修改用户隐私设置
	 * @param settings
	 * @return
	 */
	void updateSettings(UserSettings settings);
	/**
	 * 根据平台与表示查找信息
	 * @param type
	 * @param loginInfo
	 * @return
	 */
	SdkLoginInfo findSdkLoginInfo(int type, String loginInfo);
	/**
	 * 删除sdk绑定信息
	 * @param type
	 * @param loginInfo
	 * @return 
	 */
	List<SdkLoginInfo> delSdkLoginInfo(int type, String loginInfo);
	/**
	 * 获取sdk绑定信息
	 * @param type
	 * @param loginInfo
	 * @return 
	 */
	List<SdkLoginInfo> getSdkLoginInfo();
	/**
	 * 获取微信openid
	 * @param code
	 * @return
	 */
	JSONObject getWxOpenId(String code);
	
	Map<String, Object> saveLoginInfo(User user);
	/**
	 * 根据字段更新用户信息
	 * @param bean
	 */
	void updateUserByEle(User bean);

}
