package com.youxin.app.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.mongodb.morphia.query.UpdateResults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.youxin.app.entity.BankRecord;
import com.youxin.app.entity.Config;
import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.entity.HelpCenter;
import com.youxin.app.entity.Opinion;
import com.youxin.app.entity.RedPacket;
import com.youxin.app.entity.RedReceive;
import com.youxin.app.entity.Report;
import com.youxin.app.entity.Role;
import com.youxin.app.entity.User;
import com.youxin.app.entity.User.MyCard;
import com.youxin.app.entity.User.UserLoginLog;
import com.youxin.app.entity.msgbody.MsgBody;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.filter.LoginSign;
import com.youxin.app.repository.UserRepository;
import com.youxin.app.service.AdminConsoleService;
import com.youxin.app.service.ConfigService;
import com.youxin.app.service.UserService;
import com.youxin.app.service.impl.ConsumeRecordManagerImpl;
import com.youxin.app.service.impl.RedPacketManagerImpl;
import com.youxin.app.utils.BeanUtils;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.FileUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.Md5Util;
import com.youxin.app.utils.MongoUtil;
import com.youxin.app.utils.PageResult;
import com.youxin.app.utils.PageVO;
import com.youxin.app.utils.ReqUtil;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.ResultCode;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.utils.alipay.util.AliPayUtil;
import com.youxin.app.yx.SDKService;
import com.youxin.app.yx.request.MsgFile;
import com.youxin.app.yx.request.MsgRequest;
import com.youxin.app.yx.request.team.MuteTeam;
import com.youxin.app.yx.request.team.MuteTlistAll;
import com.youxin.app.yx.request.team.QueryDetail;

import io.netty.handler.codec.base64.Base64Encoder;
import io.swagger.annotations.ApiOperation;








@RestController
@RequestMapping("/console/")
public class ConsoleController extends AbstractController{

	@Autowired
	AdminConsoleService consoleService;
	
	@Autowired
	UserRepository ur;
	@Autowired
	UserService userService;
	@Autowired
	ConsumeRecordManagerImpl crm;
	@Autowired
	@Qualifier("get")
	private Datastore dfds;
	@Autowired
	RedPacketManagerImpl rpm;
	@Autowired
	ConfigService cs;

	
	@PostMapping(value = "login")
	public Object login(String name, String password, HttpServletRequest request) {
		User login = consoleService.login(name, password);
		if (login != null) {
			request.getSession().setAttribute(LoginSign.LOGIN_USER_KEY, login);
			
			String s=JSONObject.toJSONString(login);
			JSONObject json=JSONObject.parseObject(s);
			json.put("role", 6);
			json.put("loginTime", login.getLoginLog().getLoginTime());
			json.put("createTime", login.getCreateTime());
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
	
	@GetMapping(value = "userList")
	public Object userList(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int limit, @RequestParam(defaultValue = "") String onlinestate,
			@RequestParam(defaultValue = "") String keyWorld) {
		Query<User> query = ur.createQuery();

		if (!StringUtil.isEmpty(keyWorld)) {
			Integer userId = null;
			if (StringUtil.isNumeric(keyWorld)&&keyWorld.length()<10) {
				userId = Integer.valueOf(keyWorld);
			}
			
			query.or(query.criteria("name").containsIgnoreCase(keyWorld), query.criteria("_id").equal(userId),
					query.criteria("mobile").containsIgnoreCase(keyWorld));
		}
		if (!StringUtil.isEmpty(onlinestate)) {
			query.filter("onlinestate", Integer.valueOf(onlinestate));
		}
		// 排序、分页
		List<User> pageData = query.order("-createTime").asList(MongoUtil.pageFindOption(page-1, limit));
		pageData.forEach(userInfo -> {
			Query<UserLoginLog> loginLog = dfds.createQuery(UserLoginLog.class).field("userId")
					.equal(userInfo.getId());
			if (null != loginLog.get())
				userInfo.setLoginLog(loginLog.get().getLoginLog());
		});
		PageResult<User> result = new PageResult<User>();
		result.setData(pageData);
		result.setCount(query.count());
		return Result.success(result);
	}
	
	@PostMapping("blockUser")
	public Object blockUser(@RequestParam(required=true) int id,@RequestParam(defaultValue="") String accid,@RequestParam(required=true) int disableUser){
		JSONObject block = null;
		if(StringUtil.isEmpty(accid)) {
			accid=Md5Util.md5HexToAccid(id+"");
		}
		User user=new User();
		if(disableUser==-1)
			block=SDKService.block(accid, "false");
		else if(disableUser==1)
			block=SDKService.unblock(accid);
		else
			return Result.error();
		
		if(block.getIntValue("code")==200) {
			user.setDisableUser(disableUser);
			user.setId(id);
			user.setAccid(accid);
			userService.updateUser(user);
			return Result.success();
		}
		
		return Result.error();
	}
	/**
	 * 获取用户信息
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "getUpdateUser")
	public Result updateUser(@RequestParam(defaultValue = "0") Integer userId) {
		User user = null;
		if (0 == userId)
			user = new User();
		else {
			user = userService.getUserFromDB(userId);
			
			Query<Role> q = dfds.createQuery(Role.class).field("userId").equal(userId);
			List<Role> userRoles = q.asList();
			System.out.println("用户角色：" + JSONObject.toJSONString(userRoles));
			if (null != userRoles) {
				for (Role role : userRoles) {
					if (role.getRole()==2) {
						user.setUserType(2);
					} else {
						user.setUserType(0);
					}
				}
			}
		}
		return Result.success(user);
	}
	/**
	 * 
	 * @param request
	 * @param response
	 * @param userId
	 * @param example
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "updateUser")
	public Result saveUserMsg(HttpServletRequest request, HttpServletResponse response,
			@RequestParam(defaultValue = "") String accid, @ModelAttribute User example) throws Exception {
		if (!StringUtil.isEmpty(example.getMobile())) {
			example.setMobile(example.getMobile());
		}
		// 后台注册用户(后台注册传的密码没有加密，这里进行加密)
		if (!StringUtil.isEmpty(example.getPassword()))
			example.setPassword(DigestUtils.md5Hex(example.getPassword()));

		// 保存到数据库
		if (StringUtil.isEmpty(accid)&&example.getId()<=0) {
			//验证
			if (StringUtil.isEmpty(example.getMobile())) {
				throw new ServiceException(0, "手机号必填");
			}
			long mobileCount = userService.mobileCount(example.getMobile());
			if (mobileCount >= 1) {
				throw new ServiceException(0, "手机号已被注册");
			}
			userService.register(example);

		} else {
			com.youxin.app.yx.request.User.User u=new com.youxin.app.yx.request.User.User();
			User userFromDB = userService.getUserFromDB(example.getId());
			BeanUtils.copyProperties(example, userFromDB);
			BeanUtils.copyProperties(userFromDB, u);
			JSONObject updateUinfo = SDKService.updateUinfo(u);
			if(updateUinfo.getIntValue("code")==200) {
				ur.save(userFromDB);
			}else {
				return Result.error();
			}
		}

		return Result.success();
	}
	
	/**
	 * 后台充值
	 * 
	 * @param money
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "recharge")
	public Result Recharge(Double money, int userId) throws Exception {
		// 核验用户是否存在
		if (null == userService.getUser(userId)) {
			return Result.error("充值失败, 用户不存在!");
		}

		String tradeNo = AliPayUtil.getOutTradeNo();

		Map<String, Object> data = Maps.newHashMap();
		// 创建充值记录
		ConsumeRecord record = new ConsumeRecord();
		record.setUserId(userId);
		record.setTradeNo(tradeNo);
		record.setMoney(money);
		record.setStatus(KConstants.OrderStatus.END);
		if(money>=0) {
			record.setType(KConstants.ConsumeType.SYSTEM_RECHARGE);
			record.setDesc("后台余额充值");
		}else {
			record.setType(KConstants.ConsumeType.SYSTEM_REDUCE);
			record.setDesc("后台余额扣除");
		}
		record.setPayType(KConstants.PayType.SYSTEMPAY); // type = 3 ：管理后台充值
		record.setTime(DateUtil.currentTimeSeconds());
		crm.saveConsumeRecord(record);
		try {
			Double balance =0.0;
			if(money>=0)
			    balance = userService.rechargeUserMoeny(userId, Math.abs(money), KConstants.MOENY_ADD);
			else
				balance = userService.rechargeUserMoeny(userId, Math.abs(money), KConstants.MOENY_REDUCE);
			data.put("balance", balance);
			return Result.success(data);
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}

	}
	
	/**
	 * 	 用户账单
	 * @param userId
	 * @param page
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	@GetMapping(value = "userBill")
	public Object userBill(@RequestParam int userId, int page, int limit) throws Exception {
		try {

			// 核验用户是否存在
			if (null == userService.getUserFromDB(userId)) {
				return Result.error("用户不存在!");
			}
			PageResult<ConsumeRecord> result = crm.consumeRecordList(userId, page-1,
					limit);
			User userFromDB = userService.getUserFromDB(userId);
			result.setTotal(userFromDB.getBalance());
			return Result.success(result);

		} catch (Exception e) {
			return Result.error(e.getMessage());
		}

	}
	
	/**
	 * @Description:（红包记录）
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 **/
	@GetMapping("/redPacketList")
	public Object getRedPacketList(@RequestParam(defaultValue = "") String userName,
			@RequestParam(defaultValue = "0") int userId,
			@RequestParam(defaultValue = "0") int toUserId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit) {
		try {
			PageResult<RedPacket> result = rpm.getRedPacketList(userName,userId,toUserId, page-1, limit);
			return Result.success(result);
		} catch (ServiceException e) {
			return Result.error(e.getErrMessage());
		}
	}

	@RequestMapping("receiveWater")
	public Object receiveWater(@RequestParam(defaultValue = "") String redId,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int limit) {
		try {
			PageResult<RedReceive> result = rpm.receiveWater(redId, page-1, limit);
			return Result.success(result);
		} catch (ServiceException e) {
			return Result.error(e.getErrMessage());
		}
	}
	
	@RequestMapping(value = "config")
	public Object getConfig() {
		Config config = cs.getConfig();
		return Result.success(config);
	}
	// 设置服务端配置
	@RequestMapping(value = "/config/set", method = RequestMethod.POST)
	public Object setConfig(@ModelAttribute Config config) throws Exception {
		try {
			cs.setConfig(config);
			return Result.success();
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}
	}
	
	/**
	 * 得到银行卡提现记录
	 * 
	 * @param bankCard
	 * @param page
	 * @param limit
	 * @return
	 */
	@RequestMapping("/getBankList")
	public Object getBankList(@RequestParam(defaultValue = "") String bankCard,
			@RequestParam(defaultValue = "0") Integer userId, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int limit) {
		try {
			PageResult<BankRecord> result = consoleService.getBankRecordList(bankCard, userId, page-1,
					limit);
			return Result.success(result);
		} catch (ServiceException e) {
			return Result.error(e.getErrMessage());
		}
	}

	/**
	 * 完成提现
	 * 
	 * @param bankCard
	 * @param page
	 * @param limit
	 * @return
	 */
	@RequestMapping("/updateStatus")
	public Object updateStatus(@RequestParam Integer status, @RequestParam String id) {
		try {
			MsgRequest messageBean = null;
			int UserId = 1100;
			User admin = userService.getUserFromDB(UserId);
//			PageResult<BankRecord> result = SKBeanUtils.getAdminManager().getBankRecordList(bankCard, page, limit);
			if (status == 1 || status == 0) {
				Query<BankRecord> query = dfds.createQuery(BankRecord.class).field("_id")
						.equal(new ObjectId(id));
				BankRecord bankRecord = query.get();
				if (null == bankRecord)
					return Result.error("数据系统出错");
				UpdateOperations<BankRecord> ops = dfds.createUpdateOperations(BankRecord.class);
				long currentTimeSeconds = DateUtil.currentTimeSeconds();
				ops.set("status", status);
				ops.set("payTime", currentTimeSeconds);
				dfds.update(query, ops);
				String card = bankRecord.getBankCard();

				List<MyCard> tocard = dfds.createQuery(MyCard.class).field("bankCard").equal(card)
						.asList();
				messageBean = new MsgRequest();
				messageBean.setType(100);

				BankRecord sendReulst = new BankRecord();
				sendReulst.setTotalFee(bankRecord.getTotalFee());
				sendReulst.setFee(bankRecord.getFee());
				sendReulst.setRealFee(bankRecord.getRealFee());
				sendReulst.setPayTime(currentTimeSeconds);
				sendReulst.setBankCard(card.substring(card.length() - 4));
				sendReulst.setBankName(tocard.get(0).getBankName());
				sendReulst.setDes("预计2小时内到账，请注意查收!");
				
			
				messageBean.setFrom(admin.getAccid());
			
				messageBean.setOpe(0);// 个人消息
				messageBean.setTo(Md5Util.md5Hex(bankRecord.getUserId()+""));
				messageBean.setBody(JSON.toJSONString(new MsgBody(0, KConstants.MsgType.BANKOVERMONEY, sendReulst)));
				try {
					JSONObject json=SDKService.sendMsg(messageBean);
					if(json.getInteger("code")!=200) 
						log.debug("银行卡提现 sdk消息发送失败");
				} catch (Exception e) {
					e.printStackTrace();
					log.debug("银行卡提现 sdk消息发送失败"+e.getMessage());
				}
			}
			return Result.success();
		} catch (ServiceException e) {
			return Result.error(e.getErrMessage());
		}
	}
	
	/**
	 * @Description:（被举报的用户和群组列表）
	 * @param type     (type = 0查询被举报的用户,type=1查询被举报的群主,type=2查询被举报的网页)
	 * @param pageSize
	 * @return
	 **/
	@SuppressWarnings("static-access")
	@RequestMapping(value = "/beReport")
	public Object beReport(@RequestParam(defaultValue = "0") int type,
			@RequestParam(defaultValue = "0") int sender, @RequestParam(defaultValue = "") String receiver,
			@RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "25") int pageSize) {
		Map<String, Object> dataMap = Maps.newConcurrentMap();
		Result Result = new Result();
		try {
			dataMap = userService.getReport(type, sender, receiver, pageIndex, pageSize);
			log.debug("举报详情：" + JSONObject.toJSONString(dataMap.get("data")));
			if (!dataMap.isEmpty()) {
				List<Report> reportList = (List<Report>) dataMap.get("data");
				long total = (long) dataMap.get("count");
				return Result.success(new PageVO(reportList, total, pageIndex, pageSize, total));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
		return Result;

	}
	
	@RequestMapping("/isLockRoom")
	public Result isLockRoom(@RequestParam(defaultValue = "") String roomId,
			@RequestParam(defaultValue = "-1") int roomStatus) {
		if (StringUtil.isEmpty(roomId))
			return Result.error("room is null");
		Query<Report> query = dfds.createQuery(Report.class).field("roomId")
				.equal(Long.valueOf(roomId));
		if (null == query.get())
			return Result.error("暂无该链接的举报数据");
		//禁言所有群成员
		//查询群详细信息
		QueryDetail roomquery=new QueryDetail();
		roomquery.setTid(Long.valueOf(roomId));
		JSONObject eqd = SDKService.teamQueryDetail(roomquery);
		String oaccid = eqd.getJSONObject("tinfo").getJSONObject("owner").getString("accid");
		//禁言
		MuteTlistAll ma=new MuteTlistAll();
		ma.setOwner(oaccid);
		ma.setTid(roomId);
		if(roomStatus==1)
			ma.setMuteType(0);
		else
			ma.setMuteType(3);
		JSONObject json = SDKService.teamMuteTlistAll(ma);
		if(json.getIntValue("code")==200) {
//			UpdateOperations<Report> ops = dfds.createUpdateOperations(Report.class);
//			ops.set("roomStatus", roomStatus);
//			dfds.update(query, ops);
			return Result.success();
		}
		
		return Result.error("失败");
	}

	@RequestMapping("/isLockWebUrl")
	public Result isLockWebUrl(@RequestParam(defaultValue = "") String webUrlId,
			@RequestParam(defaultValue = "-1") int webStatus) {
		if (StringUtil.isEmpty(webUrlId))
			return Result.error("webUrl is null");
		Query<Report> query = dfds.createQuery(Report.class).field("_id")
				.equal(new ObjectId(webUrlId));
		if (null == query.get())
			return Result.error("暂无该链接的举报数据");
		UpdateOperations<Report> ops = dfds.createUpdateOperations(Report.class);
		ops.set("webStatus", webStatus);
		dfds.update(query, ops);
		return Result.success();
	}

	/**
	 * 删除举报
	 * 
	 * @param response
	 * @param id
	 * @return
	 * @throws IOException
	 */
	@RequestMapping(value = "/deleteReport")
	public Result deleteReport(HttpServletResponse response, @RequestParam String id) throws IOException {
		BasicDBObject query = new BasicDBObject("_id", parse(id));
		dfds.getDB().getCollection("Report").remove(query);
		return Result.success();
	}
	
	/**
	 * 意见查询
	 * @param opinion
	 * @return
	 */
	@RequestMapping(value = "/opinionList")
	public Object opinionList(@RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "25") int pageSize) {
			PageResult<Opinion> result=new PageResult<>();
			 Query<Opinion> createQuery = dfds.createQuery(Opinion.class);
			 createQuery.order("state,-createTime");
			result.setCount(createQuery.count());
			result.setData(createQuery.asList(MongoUtil.pageFindOption(pageIndex-1, pageSize)));
			return Result.success(result);
		
		
	}
	/**
	 * 意见状态修改
	 * @param opinion
	 * @return
	 */
	@RequestMapping(value = "/overOpinion")
	public Object overOpinion(@RequestParam(defaultValue = "0") int state,@RequestParam(defaultValue = "") String id) {
			if(StringUtil.isEmpty(id))
				return Result.error("id不能为空");
			UpdateOperations<Opinion> uo=dfds.createUpdateOperations(Opinion.class);
			uo.set("state", state);
			uo.set("updateTime", DateUtil.currentTimeSeconds());
			UpdateResults update = dfds.update(dfds.createQuery(Opinion.class).field("_id").equal(parse(id)), uo);
			return Result.success(update);
	}
	
	/**
	 * sdk图片上传
	 * @param 
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value = "/uploadSdkImage")
	public Object uploadSdkImage(@RequestParam(value="file",required=false)MultipartFile file) throws Exception {
		 String fileName=file.getOriginalFilename();//获取文件名加后缀	
		if(StringUtil.isEmpty(fileName))
				return Result.layuieditimg(-1, "图片为空", "", "");
		File multipartFileToFile =FileUtil.multipartFileToFile(file);
		if(multipartFileToFile==null) {
			return Result.layuieditimg(-1, "转file失败", "", "");
		}
		String base64 = FileUtil.base64(multipartFileToFile);
		if(base64==null) {
			return Result.layuieditimg(-1, "转base64失败", "", "");
		}
		
		MsgFile mf=new MsgFile();
		System.out.println(base64);
		mf.setContent(base64);
//		SDKService.fileUpload(mf);
		mf.setType("1");
		//50年
		mf.setExpireSec(3600*24*30*12*50+"");
		mf.setTag("图文");
		JSONObject upload = SDKService.upload(mf);
		System.out.println(upload);
		String url = upload.getString("url");
		return Result.layuieditimg(0, "成功", url, url);
			
	}

	@RequestMapping(value = "/helpCenterList")
	public Object helpCenterList(@RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "25") int pageSize
			,@RequestParam(defaultValue = "0") int type,@RequestParam(defaultValue = "-2") int state
			,@RequestParam(defaultValue = "") String nickName ) {
		PageResult<HelpCenter> result=new PageResult<>();
		 Query<HelpCenter> q = dfds.createQuery(HelpCenter.class);
		 if(type>0) 
				q.field("type").equal(type);
	
			if(state==0) 
				q.field("state").equal(state);
			else if(state==1)
				q.filter("state in", new Integer[] {-1,1});
			if(!StringUtil.isEmpty(nickName))
				q.field("title").contains(nickName);
		 q.order("-createTime,-updateTime");
		result.setCount(q.count());
		result.setData(q.asList(MongoUtil.pageFindOption(pageIndex-1, pageSize)));
		return Result.success(result);
	}
	@RequestMapping(value = "/getCenterList")
	public Object getCenterList(@RequestParam(defaultValue = "") String id) {
		if(StringUtil.isEmpty(id)) {
			return Result.error("id为空");
		}
		Query<HelpCenter> q = dfds.createQuery(HelpCenter.class).field("_id").equal(parse(id));
		
		return Result.success(q.get());
	}
	@RequestMapping(value = "/saveCenterList")
	public Object saveCenterList(@RequestParam(defaultValue = "") String hcid,@ModelAttribute HelpCenter hc) {
		if(StringUtil.isEmpty(hcid)) {
			hc.setCreateTime(DateUtil.currentTimeSeconds());
			hc.setState(0);
		}else {
			hc.setId(parse(hcid));
			hc.setUpdateTime(DateUtil.currentTimeSeconds());
		}
		Key<HelpCenter> save = dfds.save(hc);
		return Result.success(save);
	}
	@RequestMapping(value = "/delCenterList")
	public Object delCenterList(@RequestParam(defaultValue = "") String id) {
		if(StringUtil.isEmpty(id)) {
			return Result.error("id为空");
		}
		String[] ids = StringUtil.getStringList(id, ",");
		for(String idd:ids) {
			Query<HelpCenter> q = dfds.createQuery(HelpCenter.class).field("_id").equal(parse(idd));
			dfds.delete(q);
		}
		
		return Result.success();
	}
	

}
