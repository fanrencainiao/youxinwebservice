package com.youxin.app.utils;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.youxin.app.controller.TestController;
import com.youxin.app.utils.applicationBean.WxConfig;


/**
 * 获取微信 用户信息  工具类
 * @author lidaye
 *
 */
@Component
public class WXUserUtils{
	
	
	private static WxConfig wxConfig;
	@Autowired
	public WXUserUtils(WxConfig wxConfig) {
		WXUserUtils.wxConfig = wxConfig;
	}
	
	private final static String GETOPENIDURL=
			"https://api.weixin.qq.com/sns/oauth2/access_token";
	

	private final static String GETTOKEN=
			"https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential";
	
	private final static String GETUSERURL=
			"https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=OPENID";
	

	
	//获取小程序连接
	private final static String GETWXACODEUNLIMIT=
			"https://api.weixin.qq.com/wxa/getwxacodeunlimit";
	
	
	/*private static WXConfig wxConfig=null;
	static {
		wxConfig=new WXConfig();
		wxConfig.setAppid("wx373339ef4f3cd807");
		wxConfig.setSecret("ec6e99350b0fdb428cf50a5be403b268");
	}*/
	/**
	 * 获取 微信用户 openId
	 * @param code
	 * @return
	 */
	public static JSONObject  getWxOpenId(String code) {
		Map<String, String> params=new HashMap<>();
		params.put("grant_type","authorization_code");
		params.put("appid", wxConfig.getAppid());
		params.put("secret",wxConfig.getSecret());
		params.put("code", code);
		String result=HttpUtil.URLGet(GETOPENIDURL, params);
	
		System.out.println("\n\n getWxOpenId ===> "+result);
		return JSONObject.parseObject(result);
		
	}

	
	/**
	 * 获取微信Token
	 * @param openId
	 * @return
	 */
	public static JSONObject getWxToken(){
		Map<String, String> params=new HashMap<>();
		params.put("appid", wxConfig.getAppid());
		params.put("secret", wxConfig.getSecret());
		
		String result=HttpUtil.URLGet(GETTOKEN, params);
		return JSONObject.parseObject(result);
	}
	
	/**
	 * 获取微信小程序Token
	 * @param openId
	 * @return
	 */
	public static JSONObject getWxXcxToken(){
		Map<String, String> params=new HashMap<>();
		params.put("appid","wxcb84ab70c0f39747");
		params.put("secret", "785239ff49f6da9d93d5b7400ca3e500");
		
		String result=HttpUtil.URLGet(GETTOKEN, params);
		return JSONObject.parseObject(result);
	}
	/**
	 * 获取二维码
	 * @return
	 */
	public static InputStream getWxXcxCodeLimit(int toUserId,String inviteCode){
		Map<String, Object> params=new HashMap<>();
		params.put("scene", "toUserId="+toUserId+"&inviteCode="+inviteCode);
		InputStream result=null;
		return result;
	}
	  public static void main(String[] args) {
//	        String access_token = (String) WeChatApplet.getToken().get("access_token");
//	        Map<String, Object> data = WeChatApplet.getMiniQrCode(access_token, "pages/index/index",
//	                "btype=1&border=xxx",60);
		  Map<String, Object> params=new HashMap<>();
			params.put("scene", "toUserId="+123+"&inviteCode="+"sd");
		  Map<String, Object> data = HttpUtil.URLPostBuffer(GETWXACODEUNLIMIT+"?access_token="+getWxXcxToken().getString("access_token"), params);
			
	        String qrBytesEncoder = (String) data.get("qrBytesEncoder");
	        Integer qrLength = (Integer) data.get("qrLength");
	        System.out.println(qrBytesEncoder);
	        System.out.println(qrLength);
	        byte[] qrBytes = (byte[]) data.get("qrBytes");
	        saveToImg("d://qrcode.png",qrBytes);
	    }
	 
	    /**
	     * @param imgPath
	     * @param bytes
	     * @return
	     */
	    public static int saveToImg(String imgPath, byte[] bytes) {
	        int stateInt = 1;
	        try {
	            FileOutputStream fos = new FileOutputStream(imgPath);
	            fos.write(bytes);
	            fos.flush();
	            fos.close();
	        } catch (Exception e) {
	            stateInt = 0;
	            e.printStackTrace();
	        } finally {
	        }
	        return stateInt;
	    }
	
	
	/**
	 * 获取微信 用户资料
	 * @param token
	 * @param openid
	 * @return
	 */
	public static JSONObject  getWxUserInfo(String token,String openid) {
		Map<String, String> params=new HashMap<>();
		params.put("access_token",token);
		params.put("openid", openid);
		
		String result=HttpUtil.URLGet(GETUSERURL, params);
	
		System.out.println("\n\n getWxUserInfo ===> "+result);
		return JSONObject.parseObject(result);
		
	}



	
}
