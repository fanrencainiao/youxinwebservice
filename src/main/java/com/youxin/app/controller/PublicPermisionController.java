package com.youxin.app.controller;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.youxin.app.entity.PublicPermission;
import com.youxin.app.repository.PPSRepository;
import com.youxin.app.service.PublicPermissionService;
import com.youxin.app.utils.Md5Util;
import com.youxin.app.utils.ReqUtil;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.ThreadUtil;
import com.youxin.app.utils.supper.Callback;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import jodd.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

@Api(tags = "平台授权")
@RestController
@RequestMapping("/pp/")
public class PublicPermisionController extends AbstractController{
	

//	public static void main(String[] args) throws UnsupportedEncodingException {
//		String msg="https://www.baidu.com/?toObj%3Dtest";
//		System.out.println(URLEncoder.encode(msg,"utf-8"));
//		System.out.println(new String(msg.getBytes("ISO-8859-1"),"UTF-8"));
//		System.out.println(StringEscapeUtils.unescapeHtml4(msg));
//	}
	
	@Autowired
	private PublicPermissionService service;
	@Autowired
	private PPSRepository ppr;
	@ApiOperation(value = "链接是否授权",response=Result.class,notes="授权，返回code1,未授权或者部分异常，返回code 0")
	@ApiImplicitParams({
//		@ApiImplicitParam(name = "toObj", value = "授权对象，url中获取", required = true, paramType = "query")
	})
	@PostMapping(value = "isPermission")
	public Object pplist(@RequestBody Perp cpp) {
		System.out.println("pp url:"+cpp.getUrl());
		System.out.println("pp toObj:"+cpp.getToObj());
		if(StringUtil.isEmpty(cpp.getToObj())) 
			return Result.error("未授权对象");
		PublicPermission pp=new PublicPermission();
		pp.setToObj(cpp.getToObj());
		pp.setUrl(cpp.getUrl());
		pp.setState(1);
		List<PublicPermission> isper = service.getPPlist(pp);
		if(isper.size()>0) {
			Integer userId = ReqUtil.getUserId();
			PublicPermission pps = isper.get(0);
			ThreadUtil.executeInThread(new Callback() {
				@Override
				public void execute(Object obj) {
					System.out.println("权限obj:"+obj);
					List<Integer> pvList = pps.getPvList();
					pvList.add(userId);
					pps.setPvList(pvList);
					ppr.save(pps);
				}
			}, userId);
			Map<String, Object> r=new HashMap<String, Object>();
			r.put("id", pps.getId());
			r.put("userId", Md5Util.md5Hex(userId+pps.getId().toString()));
			return Result.success(r);
		}
		else
			return Result.error("未授权链接");
		
	}
	@ApiOperation(value = "更新链接授权记录",response=Result.class)
	@ApiImplicitParams({
	})
	@PostMapping(value = "updatePPUv")
	public Object updatePPUv(@RequestParam String id) {
		
		if(StringUtil.isEmpty(id)) 
			return Result.error("不存在id");
		PublicPermission pp = service.getPP(id);
		if(pp!=null) {
			Integer userId = ReqUtil.getUserId();
			ThreadUtil.executeInThread(new Callback() {
				@Override
				public void execute(Object obj) {
					System.out.println("权限obj:"+obj);
					Set<Integer> uvList = pp.getUvList();
					uvList.add(userId);
					pp.setUvList(uvList);
					ppr.save(pp);
				}
			}, userId);
			return Result.success();
		}
		else
			return Result.error("不存在");
		
	}
	@ApiOperation(value = "获取相关访问统计",response=Result.class)
	@ApiImplicitParams({
	})
	@GetMapping(value = "getDataCount")
	public Object getDataCount(@RequestParam String toObj,@RequestParam String url) {
		
		if(StringUtil.isEmpty(url)||StringUtil.isEmpty(toObj)) 
			return Result.error("请查看数据是否正确");
		Query<PublicPermission> q = ppr.createQuery().field("toObj").equal(toObj).field("url").equal(url);
		PublicPermission pp = ppr.findOne(q);
		if(pp!=null) {
			Map<String, Object> r=new HashMap<String, Object>();
			r.put("uv", pp.getUvList().size());
			r.put("pv", pp.getPvList().size());
			return Result.success(r);
		}
		else
			return Result.error("不存在");
		
	}
	@Getter
	@Setter
	public static class Perp{
		private String toObj;
		private String url;
	}
	
	
}
