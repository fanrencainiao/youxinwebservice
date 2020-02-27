package com.youxin.app.controller;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KSessionUtil;
import com.youxin.app.utils.applicationBean.WxConfig;

@RestController
@RequestMapping("")
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
	
//	@RequestMapping("/console/wxConfig")
//	public Object savegetredis(String value) {
//		return wxConfig;
//	}
	@RequestMapping("/info/getRandomMoney")
	public Object getRandomMoneys(Integer num,Double money) {
		Double randomMoney = getRandomMoney(num, money);
		if(randomMoney>money-0.04)
			return "超过总金额";
		else if(randomMoney<0.01)
			return "低于最低金额";
		else
			return randomMoney;
	}
	public static void main(String[] args) {
//		for (int i = 0; i < 200; i++) {
//			Double rm = getRandomMoney(5, 100d);
//			System.out.println(new Random().nextDouble());
//			System.out.println(i);
//			if(rm<0.01||rm>200) {
//				System.out.println(i);
//				return;
//			}
				
//		}
//		System.out.println(splitRedPackets(200, 100));   	
		
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
		   moeny=moeny*2.0/num;
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
	 
	 
	 
	 private static final float MINMONEY = 0.01f;
	 private static final float MAXMONEY = 200f;
	 private static boolean isRight(float money,int count)
	 {
	     double avg = money/count;
	       if(avg<MINMONEY){
	         return false;
	       }
	       else if(avg>MAXMONEY)
	       {
	         return false;
	       }
	       return true;
	 }
	 private static float randomRedPacket(float money,float mins,float maxs,int count)
	 {
	   if(count==1)
	   {
	     return (float)(Math.round(money*100))/100;
	   }
	   if(mins == maxs)
	   {
	     return mins;//如果最大值和最小值一样，就返回mins
	   }
	   float max = maxs>money?money:maxs;
	   float one = ((float)Math.random()*(max-mins)+mins);
	   one = (float)(Math.round(one*100))/100;
	   float moneyOther = money - one;
	   if(isRight(moneyOther,count-1))
	   {
	     return one;
	   }
	   else{
	     //重新分配
	     float avg = moneyOther / (count-1);
	     if(avg<MINMONEY)
	     {
	       return randomRedPacket(money,mins,one,count);
	     }else if(avg>MAXMONEY)
	     {
	       return randomRedPacket(money,one,maxs,count);
	     }
	   }
	   return one;
	 }
	 
	 private static final float TIMES = 2.1f;

	 public static List<Float> splitRedPackets(float money,int count)
	 {
	   if(!isRight(money,count))
	   {
	     return null;
	   }
	   List<Float> list = new ArrayList<Float>();
	   float max = (float)(money*TIMES/count);

	   max = max>MAXMONEY?MAXMONEY:max;
	   for(int i=0;i<count;i++)
	   {
	     float one = randomRedPacket(money,MINMONEY,max,count-i);
	     list.add(one);
	     money-=one;
	   }
	   return list;
	 }


}
