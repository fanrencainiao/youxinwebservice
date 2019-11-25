//package com.youxin.app;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.youxin.app.entity.RedPacket;
//import com.youxin.app.entity.msgbody.MsgBody;
//import com.youxin.app.entity.msgbody.MsgBody.ID;
//import com.youxin.app.utils.KConstants;
//import com.youxin.app.utils.StringUtil;
//import com.youxin.app.yx.SDKService;
//import com.youxin.app.yx.request.Msg;
//import com.youxin.app.yx.request.MsgRequest;
//
//
//@RunWith(SpringRunner.class)
//@SpringBootTest(classes=YouxinApplication.class)
//public class JunitTest {
//	
//
//	/**
//	 * 计算两个数字相乘的绝对值
//	 */
////	@Test
//	public void TestAbs() {
//		try {
//			int a=getA(-23);
//			int b=getB(-34);
//			System.out.println(a*b);
//		} catch (Exception e) {
//			System.out.println("程序异常");
//		}
//	}
//	
//	public int getA(int a){
//		
//		return Math.abs(a);
//	}
//	public int getB(int b){
//			
//		return b;
//	}
//	public int getC(int c){
//		
//		return Math.abs(c);
//	}
////	@Test
//	public void sendMsgTest() {
//		//1100  1e6e0a04d20f50967c64dac2d639a577
//		MsgRequest messageBean = new MsgRequest();
//		messageBean.setType(100);// 自定义
//		messageBean.setOpe(0);// 个人消息
////		if (toUser!=null) {
////			messageBean.setFrom(toUser.getAccid());
////		}else {
//			messageBean.setFrom("1e6e0a04d20f50967c64dac2d639a577");
////		}
//		messageBean.setTo("971b71a3fd88a7088267deb89bd36d60");
//		ID id=new ID();
//		id.setId("5dd7aaa212e8e35d83d2c2a3");
//		System.out.println(new MsgBody(0, KConstants.MsgType.BACKREDPACKET, "5dd7aaa212e8e35d83d2c2a3"));
//		messageBean.setBody(JSON.toJSONString(new MsgBody(0, KConstants.MsgType.BACKREDPACKET, id)));
//		try {
//			SDKService.sendMsg(messageBean);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	
//
//}
