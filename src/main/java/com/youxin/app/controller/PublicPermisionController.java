package com.youxin.app.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.youxin.app.entity.PublicPermission;
import com.youxin.app.service.PublicPermissionService;
import com.youxin.app.utils.Result;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import jodd.util.StringUtil;

@Api(tags = "平台授权")
@RestController
@RequestMapping("/pp/")
public class PublicPermisionController extends AbstractController{
	

	
	@Autowired
	private PublicPermissionService service;
	@ApiOperation(value = "链接是否授权",response=Result.class,notes="授权，返回code1,未授权或者部分异常，返回code 0")
	@ApiImplicitParams({ @ApiImplicitParam(name = "toObj", value = "授权对象，url中获取", required = true, paramType = "query")
	})
	@GetMapping(value = "isPermission")
	public Object pplist(@RequestParam(defaultValue="") String toObj,@RequestParam(defaultValue="") String url) {
		if(StringUtil.isEmpty(toObj)) 
			return Result.error("未授权对象");
		PublicPermission pp=new PublicPermission();
		pp.setToObj(toObj);
		pp.setUrl(url);
		List<PublicPermission> isper = service.getPPlist(pp);
		if(isper.size()>0)
			return Result.success();
		else
			return Result.error("未授权链接");
		
	}
	
	
}
