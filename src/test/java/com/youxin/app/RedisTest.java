//package com.youxin.app;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import com.youxin.app.utils.KSessionUtil;
//import com.youxin.app.utils.jedis.RedisCRUD;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes=YouxinApplication.class)
//public class RedisTest {
//	@Autowired
//	private RedisCRUD redisCRUD;
//	
////	public KSessionUtil(RedisCRUD redisCRUD) {
////		KSessionUtil.redisCRUD=redisCRUD;
////	}
//	/**
//	 * 获取用户token
//	 */
//	@Test
//	public void getToken() {
//		System.out.println(redisCRUD.get("loginToken:token:10011560"));
//	}
//}
