package com.youxin.app.controller;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.youxin.app.entity.Advert;
import com.youxin.app.utils.Result;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 广告管理接口
 * @author cf
 * @date 2020年3月12日 下午12:37:03
 */

@Api(tags = "广告管理接口")
@RestController
@RequestMapping("/ad/")
public class AdvertController {
	@Autowired
	@Qualifier("get")
	private Datastore dfds;
	@ApiOperation(value = "sdk登录",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "type", value = "广告类型 0所有 1启动页广告", required = true, paramType = "query"),
		})
	@PostMapping("adList")
	public Object adList(@RequestParam(defaultValue="0") int type) {
		Query<Advert> q = dfds.createQuery(Advert.class);
		if(type>0) 
			q.field("type").equal(type);
		return q.asList();
	}

}
