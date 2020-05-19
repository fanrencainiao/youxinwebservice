package com.youxin.app.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.youxin.app.entity.Config;
import com.youxin.app.entity.HelpCenter;
import com.youxin.app.service.ConfigService;
import com.youxin.app.utils.IpSearch;
import com.youxin.app.utils.NetworkUtil;
import com.youxin.app.utils.Result;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(tags = "客户端信息")
@RestController
@RequestMapping("/config")
public class ConfigController extends AbstractController{
	@Autowired
	private ConfigService cs;
	@Autowired
	@Qualifier("get")
	private Datastore dfds;
	@ApiOperation(value = "获取客户端信息-未登录",response=Result.class)
	@GetMapping(value = "/get")
	public Object getConfig(HttpServletRequest request) {
		String ip=NetworkUtil.getIpAddress(request);
		String area=IpSearch.getArea(ip);
		log.info("==Client-IP===>  {"+ip+"}  ===Address==>  {"+area+"} ");
		Config config = new Config(cs.getConfig());
		config.setIpAddress(ip);
		config.setArea(area);
		
		 Query<HelpCenter> q = dfds.createQuery(HelpCenter.class);
		 q.field("type").equal(8);
		 List<HelpCenter> asList = q.asList();
		 if(asList.size()>0)
			 config.setAdd(asList.get(0));
		return Result.success(config);
	}
	@ApiOperation(value = "获取客户端信息-登录",response=Result.class)
	@GetMapping(value = "/getOnline")
	public Object getConfigByOnline(HttpServletRequest request) {
		String ip=NetworkUtil.getIpAddress(request);
		String area=IpSearch.getArea(ip);
		log.info("==Client-IP===>  {"+ip+"}  ===Address==>  {"+area+"} ");
		Config config = cs.getConfig();
		config.setIosAppUrl(config.getIosDownUrl());
		config.setIpAddress(ip);
		config.setArea(area);
		return Result.success(config);
	}
	@ApiOperation(value = "获取零钱相关信息-登录",response=Result.class)
	@GetMapping(value = "/getPConfig")
	public Object getPConfig(HttpServletRequest request) {
		String ip=NetworkUtil.getIpAddress(request);
		String area=IpSearch.getArea(ip);
		log.info("==Client-IP===>  {"+ip+"}  ===Address==>  {"+area+"} ");
		Config config = cs.getConfig();
		Map<String, Object> mConfig = config.getMoneyConfig(config);
		return Result.success(mConfig);
	}

}