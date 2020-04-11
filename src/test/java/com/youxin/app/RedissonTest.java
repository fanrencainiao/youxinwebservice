//package com.youxin.app;
//
//import java.util.concurrent.TimeUnit;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mongodb.morphia.Datastore;
//import org.redisson.api.RLock;
//import org.redisson.api.RedissonClient;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import com.youxin.app.utils.ThreadUtil;
//import com.youxin.app.utils.alipay.util.AliPayUtil;
//import com.youxin.app.utils.supper.Callback;
//
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes=YouxinApplication.class)
//public class RedissonTest {
//
//	@Autowired
//	@Qualifier("get")
//	private Datastore dfds;
//	@Autowired
//	private RedissonClient rc;
//
//
//	@Test
//	public void rcfailLockTest() {
//		for (int i = 0; i < 100; i++) {
//			int j=i;
//			System.out.println(j);
//		
//			ThreadUtil.executeInThread(new Callback() {
//				@Override
//				public void execute(Object obj) {
//
//					RLock lock = rc.getFairLock("testlock");
//					try {
//						boolean tryLock = lock.tryLock(5, 30, TimeUnit.SECONDS);
//						if(tryLock) {
//							System.out.println("获得"+j);
//						}else {
//							System.out.println("失败");
//						}
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//						System.out.println("异常");
//					}finally {
//						System.out.println("finnal"+j);
//						lock.unlock();
//					}
//					
//				}
//				
//			});
//			
//		}
//		System.out.println("循环结束,保持服务启动状态");
//		try {
//			Thread.sleep(1000);
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//	}
//
//	private void locktest() {
//		RLock lock = rc.getFairLock("testlock");
//		try {
//			boolean tryLock = lock.tryLock(5, 30, TimeUnit.SECONDS);
//			if(tryLock) {
//				System.out.println("获得");
//				Thread.sleep(6000);
//			}else {
//				System.out.println("失败");
//			}
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			System.out.println("异常");
//		}finally {
//			lock.unlock();
//		}
//	}
//
//}
