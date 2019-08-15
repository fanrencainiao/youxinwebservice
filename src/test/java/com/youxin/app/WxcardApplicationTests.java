package com.youxin.app;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WxcardApplicationTests {

	@Test
	public void contextLoads() {
	}
	
	
	@Test
	public void luaTest() {
		String luaFileName = null;
		try {
			luaFileName  = "D:\\workspace\\youxinservers\\src\\test\\java\\com\\youxin\\app\\hello.lua";
	
			Globals globals = JsePlatform.standardGlobals();
			LuaValue transcoderObj = globals.loadfile(luaFileName).call();
			LuaValue func = transcoderObj.get(LuaValue.valueOf("main"));
			String result = func.call().toString();
			System.out.println("result---"+result);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("result---" + e.getMessage());
		}
	}

}
