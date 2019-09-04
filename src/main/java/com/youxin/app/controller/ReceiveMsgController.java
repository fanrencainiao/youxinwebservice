package com.youxin.app.controller;



import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.youxin.app.utils.Result;
@RestController
@RequestMapping("/message/")
public class ReceiveMsgController extends AbstractController{
	
	@PostMapping("receive")
	public Object register(JSONObject obj){
		System.out.println(obj); 
		return Result.success();
	}
	
	
}
