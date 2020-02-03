package com.youxin.app.controller;



import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.youxin.app.utils.Result;
@RestController
@RequestMapping("/message/")
public class ReceiveMsgController extends AbstractController{
	
	@PostMapping("receive")
	public Object register(Map<String, Object> obj){
		System.out.println("消息抄送"+obj); 
		return Result.success();
	}
	
	
}
