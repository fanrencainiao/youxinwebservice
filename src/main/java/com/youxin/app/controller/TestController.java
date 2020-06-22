package com.youxin.app.controller;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import org.mongodb.morphia.Key;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.youxin.app.entity.User;
import com.youxin.app.ex.ServiceException;
import com.youxin.app.repository.UserRepository;
import com.youxin.app.service.UserService;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KSessionUtil;
import com.youxin.app.utils.Result;
import com.youxin.app.utils.applicationBean.WxConfig;
import com.youxin.app.yx.SDKService;

@RestController
@RequestMapping("")
public class TestController  {
/*	 @Value("${hello.text}")
	 private String text;*/
	private static WxConfig wxConfig;
	@Autowired
	public TestController(WxConfig wxConfig) {
	TestController.wxConfig = wxConfig;
	}
	
	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository repository;
	
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
	public static Object getRandomMoneys(Integer num,Double money) {
		Map<String, Object> result=new HashMap<String, Object>();
		List<Double> redList=new ArrayList<>();
		DecimalFormat df = new DecimalFormat("#.00");
		df.setRoundingMode(RoundingMode.HALF_EVEN);
		double randomMoney=0;
		Long st=DateUtil.currentTimeMilliSeconds();
		for (int i = 0; i < num; i++) {
			randomMoney = getRandomMoney(num-i, money);
			if(randomMoney==0.0)
				return "无法分配，或异常";
			redList.add(randomMoney);
			money=Double.valueOf(df.format(money-randomMoney));
		}
		result.put("红包耗时(毫秒)", DateUtil.currentTimeMilliSeconds()-st);
		result.put("红包列表", redList);
		result.put("红包列表总和",Double.valueOf(redList.stream().mapToDouble(r->r).sum()));
		return result;
	}

	@RequestMapping("/info/synExData")
	public Object synExData() {
		List<User> asList = repository.createQuery().asList();
		asList.stream().forEach(u->{
			u.setExs();
			com.youxin.app.yx.request.User.User yuser=new com.youxin.app.yx.request.User.User();
			BeanUtils.copyProperties(u, yuser);
			JSONObject json = SDKService.updateUinfo(yuser);
			if(json.getIntValue("code")==200) {
				//维护数据
				KSessionUtil.deleteUserByUserId(u.getId());
				System.out.println("修改用户ex更新到云信成功");
			}else
				System.out.println("修改disableuser更新到云信失败");
		});
		return Result.success("处理完毕");
		
	}
	

//	    @GetMapping("/info/say")
//	    public String sayHello(){
//	        return text;
//	    }

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
		
//		queueTest();
		System.out.println(getRandomMoneys(2, 0.1));
		
	}

	private static void queueTest() {
		//add()和remove()方法在失败的时候会抛出异常(不推荐)
        Queue<String> queue = new LinkedList<String>();
        //添加元素
        queue.offer("a");
        queue.offer("b");
        queue.offer("c");
        queue.offer("d");
        queue.offer("e");
        for(String q : queue){
            System.out.println(q);
        }
        System.out.println("===");
        System.out.println("poll="+queue.poll()); //返回第一个元素，并在队列中删除
        for(String q : queue){
            System.out.println(q);
        }
        System.out.println("===");
        System.out.println("element="+queue.element()); //返回第一个元素 
        for(String q : queue){
            System.out.println(q);
        }
        System.out.println("===");
        System.out.println("peek="+queue.peek()); //返回第一个元素 
        for(String q : queue){
            System.out.println(q);
        }
	}
	
	/**
	  * 
	  * @param num 红包个数
	  * @param moeny 红包金额
	  * @return
	  */
	 private static synchronized Double getRandomMoney(int num,Double moeny) {
		 double max = 200d;// 最大金额
			double min = 0.01d;// 最小金额
			double regMaxMoney = 0d;// 最小金额
			DecimalFormat df = new DecimalFormat("#.00");
			df.setRoundingMode(RoundingMode.HALF_EVEN);
			Double resultMoney = 0.0d;
			Random r = new Random();
			long currentTimeMilliSeconds = DateUtil.currentTimeMilliSeconds();
			if (num == 1) {
				System.out.println(moeny);
				resultMoney = moeny;
			} else {
				if (Double.valueOf(moeny / num) > max || Double.valueOf(moeny / num) < min) {
					System.out.println("无法分配");
				} else if (Double.valueOf(moeny / num) == max) {
					System.out.println(max);
					resultMoney = max;
				} else if (Double.valueOf(moeny / num) == min) {
					System.out.println(min);
					resultMoney = min;
				} else {
				regMaxMoney=moeny;
					moeny = moeny * 2 / num;
					double nd = r.nextDouble();
					Double rmoney = 0.0d;
					Double lesMoney = Double.valueOf(df.format(regMaxMoney - (num - 1) * max));
					if (lesMoney > 0) {
						System.out.println("1:" + (max - lesMoney) * nd );
						rmoney = (max - lesMoney) * nd + lesMoney;
						rmoney=Double.valueOf(df.format(rmoney))>=max?(rmoney-0.01):rmoney;
						System.out.println("1:" + rmoney);
					}else {
//						rmoney=moeny* nd;
						// 计算此次最大分配金额，保证此次之后剩余金额与数量足够最小分配
						if (moeny < max) {
//							rmoney = (moeny - (num - 1) * min) * nd;
							//减去最小分配之后的金额
							Double nm=regMaxMoney - (num - 1) * min;
							//若金额小于系数金额，则使用当前金额进行计算,否则用系数金额计算，防止单个金额过大问题
							if(nm<moeny)
								rmoney=nm* nd;
							else
								rmoney=moeny* nd;
							if(rmoney<0.02)
								rmoney=0.01;
								
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
			System.out.println("抢红包耗时：" + (DateUtil.currentTimeMilliSeconds() - currentTimeMilliSeconds));
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
