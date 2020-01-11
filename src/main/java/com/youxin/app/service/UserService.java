package com.youxin.app.service;

import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.DBObject;
import com.youxin.app.entity.NearbyUser;
import com.youxin.app.entity.SdkLoginInfo;
import com.youxin.app.entity.User;
import com.youxin.app.entity.User.DeviceInfo;
import com.youxin.app.entity.User.LoginLog;
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
//	User getUser(String accid,String toaccid);
	
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
	void updateLoginoutLogTime(int userId);
	/**
	 * 更新登录状态
	 * @param userId
	 * @param info
	 */
	void saveLoginToken(Integer userId, DeviceInfo info,LoginLog log);
	LoginLog getLogin(int userId);
	/**
	 * @Description: 获取举报列表
	 * @param type      0：用户相关，1：群组相关 2：web网页
	 * @param sender
	 * @param receiver
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 **/
	Map<String, Object> getReport(int type, int sender, String receiver, int pageIndex, int pageSize);
	/**
	 * @Description: 删除相关的举报信息
	 * @param userId
	 * @param roomId
	 **/
	void delReport(Integer userId, String roomId);
	boolean checkReportUrlImpl(String webUrl);
	void report(Integer userId, Integer toUserId, int reason, Long roomId, String webUrl);
	/**
	 * @Description:（附近的用户）
	 * @param poi
	 * @return
	 **/
	List<User> nearbyUser(NearbyUser poi);
	/**
	 * 更新用户在线状态
	 * @param id
	 * @param type
	 */
	void updateUserByOnline(int id, int type);
	/**
	 * 1: 每个月的数据 2:每天的数据 3.每小时数据 4.每分钟的数据
	 * @param startDate
	 * @param endDate
	 * @param timeUnit
	 * @return
	 */
	List<Object> userOnlineStatusCount(String startDate, String endDate, short timeUnit);
	/**
	 * 用户注册统计 时间单位每日，最好可选择：每日、每月、每分钟、每小时
	 * 
	 * @param startDate
	 * @param endDate
	 * @param counType  统计类型 1: 每个月的数据 2:每天的数据 3.每小时数据 4.每分钟的数据 (小时)
	 */
	List<Object> getUserRegisterCount(String startDate, String endDate, short timeUnit);


}
