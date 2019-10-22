package com.youxin.app.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.youxin.app.entity.Config;
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
	@ApiOperation(value = "获取客户端信息",response=Result.class)
	@GetMapping(value = "/get")
	public Object getConfig(HttpServletRequest request) {
		String ip=NetworkUtil.getIpAddress(request);
		String area=IpSearch.getArea(ip);
		log.info("==Client-IP===>  {"+ip+"}  ===Address==>  {"+area+"} ");
		Config config = cs.getConfig();
		config.setIpAddress(ip);
		config.setArea(area);
		return Result.success(config);
	}

}