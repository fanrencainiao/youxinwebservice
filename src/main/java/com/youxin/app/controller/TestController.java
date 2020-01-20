package com.youxin.app.controller;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KSessionUtil;
import com.youxin.app.utils.applicationBean.WxConfig;

@RestController
public class TestController  {
	
	private static WxConfig wxConfig;
	@Autowired
	public TestController(WxConfig wxConfig) {
	TestController.wxConfig = wxConfig;
	}
	
//	private static RedisUtil redis;

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
	
	@RequestMapping("/console/wxConfig")
	public Object savegetredis(String value) {
		return wxConfig;
	}
	
	public static void main(String[] args) {
		for (int i = 0; i < 200; i++) {
			Double rm = getRandomMoney(50, 1d);
			System.out.println(i);
			if(rm<0.01||rm>200) {
				System.out.println(i);
				return;
			}
				
		}
			
		
	}
	
	/**
	  * 
	  * @param num 红包个数
	  * @param moeny 红包金额
	  * @return
	  */
	 private static synchronized Double getRandomMoney(int num,Double moeny) {
	  Double max=200d;//最大金额
	  Double min=0.01d;//最小金额
	  DecimalFormat df = new DecimalFormat("#.00");
	  df.setRoundingMode(RoundingMode.HALF_EVEN);
	  Double resultMoney=0.0d;
	  Random r = new Random();
	  long currentTimeMilliSeconds = DateUtil.currentTimeMilliSeconds();
	  if (num == 1) {
	   System.out.println(moeny);
	   resultMoney = moeny;
	  } else {
	   if (Double.valueOf(df.format(moeny / num)) == max) {
	    System.out.println(max);
	    resultMoney = max;
	   } else if (Double.valueOf(df.format(moeny / num)) == min) {
	    System.out.println(min);
	    resultMoney = min;
	   } else if (Double.valueOf(df.format(moeny / num)) > max || Double.valueOf(df.format(moeny / num)) < min) {
	    System.out.println("无法分配");
	   } else {

	    double nd = r.nextDouble();
	    Double rmoney = 0.0d;

	    Double lesMoney = Double.valueOf(df.format(moeny - (num - 1) * max));
	    if (lesMoney > 0) {
	     rmoney = (max - lesMoney) * nd + lesMoney;
	     System.out.println("1:" + rmoney);
	    } else {
	     // 计算此次最大分配金额，保证此次之后剩余金额与数量足够最小分配
	     if ((moeny - (num - 1) * min) < max) {
	      rmoney = (moeny - (num - 1) * min) * nd;
	      System.out.println("2:" + rmoney);
	     } else {
	      rmoney = (max - (num - 1) * min) * nd;
	      System.out.println("3:" + rmoney);
	     }
	    }
	    rmoney = Double.valueOf(df.format(rmoney));
	    if (rmoney == 0.0)
	     rmoney = 0.01;
	    System.out.println("结果：" + rmoney);
	    resultMoney = rmoney;
	   }
	  }
	  System.out.println("抢红包耗时："+(DateUtil.currentTimeMilliSeconds() - currentTimeMilliSeconds));
	  return resultMoney;
	 }

}
