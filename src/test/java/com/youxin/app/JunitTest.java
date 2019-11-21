package com.youxin.app;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.youxin.app.entity.RedPacket;
import com.youxin.app.utils.StringUtil;
import com.youxin.app.yx.SDKService;
import com.youxin.app.yx.request.Msg;
import com.youxin.app.yx.request.MsgRequest;


@RunWith(SpringRunner.class)
@SpringBootTest(classes=YouxinApplication.class)
public class JunitTest {
	

	/**
	 * 计算两个数字相乘的绝对值
	 */
	@Test
	public void TestAbs() {
		try {
			int a=getA(-23);
			int b=getB(-34);
			System.out.println(a*b);
		} catch (Exception e) {
			System.out.println("程序异常");
		}
	}
	
	public int getA(int a){
		
		return Math.abs(a);
	}
	public int getB(int b){
			
		return b;
	}
	public int getC(int c){
		
		return Math.abs(c);
	}
//	@Test
	public void sendMsgTest() {
		MsgRequest messageBean = new MsgRequest();
		messageBean.setFrom("d515adcd7b3c3d05b5b9f6ebeb94ae51");
		messageBean.setOpe(1);// 群消息
		messageBean.setType(0);// 群消息自定义通知
		messageBean.setTo("2705420735");
		messageBean.setBody("{\"msg\":\"sdsdsdssds\"}");
		try {
			SDKService.sendMsg(messageBean);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

}
