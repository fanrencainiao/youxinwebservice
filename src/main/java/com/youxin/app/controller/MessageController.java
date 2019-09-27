package com.youxin.app.controller;

import java.util.Map;

import javax.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.youxin.app.entity.User;
import com.youxin.app.utils.Result;
import com.youxin.app.yx.SDKService;
import com.youxin.app.yx.request.Msg;
import com.youxin.app.yx.request.MsgFile;
import com.youxin.app.yx.request.MsgRequest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

@Api(tags = "消息管理")
@RestController
@RequestMapping("/msg/")
public class MessageController {
	
	@ApiOperation(value="发送普通消息",notes = "发送普通消息 给用户或者高级群发送普通消息，包括文本，图片，语音，视频和地理位置",response=Result.class)
	@PostMapping("sendMsg")
	public Object sendMsg(@RequestBody @Valid MsgRequest msg){
		JSONObject json = SDKService.sendMsg(msg);
		if(json.getIntValue("code")==200) {
			Result.success(json.getJSONObject("data"));
		}
		return Result.error();
	}
	@ApiOperation(value ="批量发送点对点普通消息",notes = "批量发送点对点普通消息 1.给用户发送点对点普通消息，包括文本，图片，语音，视频，地理位置和自定义消息。\n 2.最大限500人，只能针对个人,如果批量提供的帐号中有未注册的帐号，会提示并返回给用户。\n 3.此接口受频率控制，一个应用一分钟最多调用120次，超过会返回416状态码，并且被屏蔽一段时间； 具体消息参考下面描述。",response=Result.class)
	@PostMapping("sendBatchMsg")
	public Object sendBatchMsg(@RequestBody @Valid Msg msg){
		JSONObject json = SDKService.sendBatchMsg(msg);
		if(json.getIntValue("code")==200) {
			Result.success(json);
		}
		return Result.error();
	}
	
	@ApiOperation(value ="发送自定义系统通知",notes = "发送自定义系统通知 1.自定义系统通知区别于普通消息，方便开发者进行业务逻辑的通知； \n 2.目前支持两种类型：点对点类型和群类型（仅限高级群），根据msgType有所区别。 \n应用场景：如某个用户给另一个用户发送好友请求信息等，具体attach为请求消息体，第三方可以自行扩展，建议是json格式",response=Result.class)
	@PostMapping("sendAttachMsg")
	public Object sendAttachMsg(@RequestBody @Valid Msg msg){
		JSONObject json = SDKService.sendAttachMsg(msg);
		if(json.getIntValue("code")==200) {
			Result.success();
		}
		return Result.error();
	}
	
	@ApiOperation(value ="批量发送点对点自定义系统通知",notes = "批量发送点对点自定义系统通知 1.系统通知区别于普通消息，应用接收到直接交给上层处理，客户端可不做展示； 2.目前支持类型：点对点类型； 3.最大限500人，只能针对个人,如果批量提供的帐号中有未注册的帐号，会提示并返回给用户； 4.此接口受频率控制，一个应用一分钟最多调用120次，超过会返回416状态码，并且被屏蔽一段时间； 应用场景：如某个用户给另一个用户发送好友请求信息等，具体attach为请求消息体，第三方可以自行扩展，建议是json格式",response=Result.class)
	@PostMapping("sendBatchAttachMsg")
	public Object sendBatchAttachMsg(@RequestBody @Valid Msg msg){
		JSONObject json = SDKService.sendBatchAttachMsg(msg);
		if(json.getIntValue("code")==200) {
			Result.success();
		}
		return Result.error();
	}
	@ApiOperation(value = "字符流需要base64编码，最大15M。",response=Result.class)
	@PostMapping("upload")
	public Object upload(@RequestBody @Valid MsgFile file){
		JSONObject json = SDKService.upload(file);
		if(json.getIntValue("code")==200) {
			Result.success();
		}
		return Result.error();
	}
	@ApiOperation(value ="字上传NOS文件清理任务",notes = "字上传NOS文件清理任务，按时间范围和文件类下、场景清理符合条件的文件 每天提交的任务数量有限制，请合理规划。",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "startTime", value = "被清理文件的开始时间，必须小于endTime且大于0，endTime和startTime不能之间不能超过7天", required = true, paramType = "query")
	,@ApiImplicitParam(name = "endTime", value = "被清理文件的结束时间", required = true, paramType = "query"),
	@ApiImplicitParam(name = "contentType", value = "被清理的文件类型，文件类型包含contentType则被清理 如原始文件类型为\"image/png\"，contentType参数为\"image\",则满足被清理条件", paramType = "query"),
	@ApiImplicitParam(name = "tag", value = "被清理文件的应用场景，完全相同才被清理 如上传文件时知道场景为\"usericon\",tag参数为\"usericon\"，则满足被清理条件", paramType = "query"),
	})
	@PostMapping("jobNosDel")
	public Object jobNosDel(Long startTime, Long endTime, String contentType, String tag){
		JSONObject json = SDKService.jobNosDel(startTime, endTime, contentType, tag);
		if(json.getIntValue("code")==200) {
			Result.success(json);
		}
		return Result.error();
	}
	
	@ApiOperation(value = "消息撤回接口",response=Result.class)
	@PostMapping("recall")
	public Object recall(@RequestBody @Valid Msg msg){
		JSONObject json = SDKService.recall(msg);
		if(json.getIntValue("code")==200) {
			Result.success(json);
		}
		return Result.error();
	}
	@ApiOperation(value = "发送广播消息",response=Result.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "body", value = "广播消息内容，最大4096字符", required = true, paramType = "query")
	,@ApiImplicitParam(name = "from", value = "发送者accid, 用户帐号，最大长度32字符，必须保证一个APP内唯一", paramType = "query"),
	@ApiImplicitParam(name = "isOffline", value = "是否存离线，true或false，默认false", paramType = "query"),
	@ApiImplicitParam(name = "ttl", value = "存离线状态下的有效期，单位小时，默认7天", paramType = "query"),
	@ApiImplicitParam(name = "targetOs", value = "目标客户端，默认所有客户端，jsonArray，格式：[\"ios\",\"aos\",\"pc\",\"web\",\"mac\"]", paramType = "query"),
	})
	@PostMapping("broadcastMsg")
	public Object broadcastMsg(String body, String from, String isOffline, int ttl, String targetOs){
		JSONObject json = SDKService.broadcastMsg(body, from, isOffline, ttl, targetOs);
		if(json.getIntValue("code")==200) {
			Result.success(json);
		}
		return Result.error();
	}


}
