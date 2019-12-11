package com.youxin.app.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
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
import com.alipay.api.domain.Account;
import com.mongodb.DBObject;
import com.youxin.app.entity.HelpCenter;
import com.youxin.app.entity.NearbyUser;
import com.youxin.app.entity.Opinion;
import com.youxin.app.entity.SdkLoginInfo;
import com.youxin.app.entity.User;
import com.youxin.app.entity.User.DeviceInfo;
import com.youxin.app.entity.User.LoginLog;
import com.youxin.app.entity.User.UserLoginLog;
import com.youxin.app.entity.User.UserSettings;
import com.youxin.app.entity.UserVo;
import com.youxin.app.entity.exam.BaseExample;
import com.youxin.app.entity.exam.UserExample;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.repository.UserRepository;
import com.youxin.app.service.UserService;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.KSessionUtil;
import com.youxin.app.utils.Md5Util;
import com.youxin.app.utils.MongoUtil;
import com.youxin.app.utils.PageResult;
import com.youxin.app.utils.PageVO;
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

@Api(tags = "图文管理")
@RestController
@RequestMapping("/article/")
public class HelpCenterController extends AbstractController{
	

	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository repository;
	@Autowired
	private SMSServiceImpl sendSms;
	@Autowired
	@Qualifier("get")
	private Datastore dfds;
	@ApiOperation(value = "帮助中心列表",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "type", value = "类型 1帮助中心图文", required = true, paramType = "query")
	})
	@GetMapping(value = "/helpCenterList")
	public Object helpCenterList(@RequestParam(defaultValue = "0") int type,@RequestParam(defaultValue = "0") int pageIndex, @RequestParam(defaultValue = "25") int pageSize) {
		PageResult<HelpCenter> result=new PageResult<>();
		 Query<HelpCenter> q = dfds.createQuery(HelpCenter.class);
		if(type>0) {
			 q.field("type").equal(type);
		}	
		 q.order("-createTime,-updateTime");
		result.setCount(q.count());
		result.setData(q.asList(MongoUtil.pageFindOption(pageIndex, pageSize)));
		PageVO data = new PageVO(result.getData(), result.getCount(), pageIndex, pageSize);
		return Result.success(data);
	}
	@ApiOperation(value = "修改状态",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "state", value = "状态 -1未解决 1解决", required = true, paramType = "query")
	})
	@PostMapping(value = "/overHelp")
	public Object overHelp(@RequestParam(defaultValue = "") String id,@RequestParam(defaultValue = "0") int state) {
		Query<HelpCenter> q = dfds.createQuery(HelpCenter.class);
		if(!StringUtil.isEmpty(id)) {
			 q.field("_id").equal(id);
			 UpdateOperations<HelpCenter> ops = dfds.createUpdateOperations(HelpCenter.class);
			 ops.set("state", state);
			 if(state==1) {
				 ops.push("overUserIds", ReqUtil.getUserId());
			 }
			 if(state==-1) {
				 ops.push("noUserIds", ReqUtil.getUserId());
			 }
			 dfds.update(q, ops);
		}else
			Result.error("id不能为空");
		return Result.success();
	}
	
}
