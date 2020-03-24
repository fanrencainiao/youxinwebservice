package com.youxin.app.controller;

import java.util.List;

import javax.validation.Valid;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mongodb.WriteResult;
import com.youxin.app.entity.User;
import com.youxin.app.entity.UserCollect;
import com.youxin.app.repository.CollectRepository;
import com.youxin.app.utils.MongoUtil;
import com.youxin.app.utils.PageVO;
import com.youxin.app.utils.ReqUtil;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.StringUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 收藏
 * 
 * @author cf
 * @date 2020年3月18日 下午2:36:15
 */
@Api(tags = "收藏")
@RestController
@RequestMapping("/collect/")
public class CollectController extends AbstractController{
	
	@Autowired
	private CollectRepository repository;
	@Autowired
	@Qualifier("get")
	private Datastore dfds;
	
	@ApiOperation(value = "保存或编辑收藏")
	@PostMapping("putCollect")
	public Object putCollect(@RequestBody @Valid UserCollect collect){
		collect.setUserId(ReqUtil.getUserId());
		Key<UserCollect> save = repository.save(collect);
		return Result.success(save.getId());
	}
	@ApiOperation(value = "移除收藏，支持批量")
	@ApiImplicitParams({ @ApiImplicitParam(name = "id", value = "收藏id(批量形式 1,2,3,4)", required = true, paramType = "query")
	})
	@PostMapping("delCollect")
	public Object delCollect(@RequestParam  String id){
		String[] ids = StringUtil.getStringList(id, ",");
		for (String idd:ids) {
			WriteResult deleteById = repository.deleteById(parse(idd));
			log.debug("移除收藏："+deleteById);
		}
		return Result.success();
	}
	@ApiOperation(value = "查询收藏")
	@ApiImplicitParams({ @ApiImplicitParam(name = "pageIndex", value = "页码 默认0", required = true, paramType = "query")
	, @ApiImplicitParam(name = "pageSize", value = "长度 默认10", required = true, paramType = "query")
	, @ApiImplicitParam(name = "sendType", value = "发送类型 参照保存接口实体模型", required = false, paramType = "query")
	, @ApiImplicitParam(name = "collectType", value = "收藏类型 参照保存接口实体模型", required = false, paramType = "query")
	})
	@PostMapping("selectCollect")
	public Object selectCollect(@RequestParam(defaultValue = "0") Integer pageIndex,
			@RequestParam(defaultValue = "10") Integer pageSize,
			@RequestParam(defaultValue="0") Integer sendType,@RequestParam(defaultValue="0") Integer collectType){
		Query<UserCollect> q = repository.createQuery();
		q.field("userId").equal(ReqUtil.getUserId());
		if(sendType>0)
			q.field("sendType").equal(sendType);
		if(collectType>0)
			q.field("collectType").equal(collectType);
		q.order("-collectTime");
		List<UserCollect> datas = q.asList(MongoUtil.pageFindOption(pageIndex, pageSize));
		return Result.success(new PageVO(datas, q.count(), pageIndex, pageSize));
	}


}
