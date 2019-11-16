//package com.youxin.app.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.youxin.app.utils.KSessionUtil;
//import com.youxin.app.utils.redis.RedisUtil;
//
//@RestController
//public class TestController {
//
//	private static RedisUtil redis;
//
//	@Autowired
//	public TestController(RedisUtil redis) {
//		TestController.redis = redis;
//	}
//
//	@RequestMapping("/console")
//	public Object savegetredis(String value) {
//		redis.saveString(value, value);
//		String key = redis.getKey(value);
//		return key;
//	}
//	
//	@RequestMapping("/console/ksessionutil")
//	public Object ksessionutil(String value) {
//		return KSessionUtil.getUserByUserId(value);
//	}
//
//}
