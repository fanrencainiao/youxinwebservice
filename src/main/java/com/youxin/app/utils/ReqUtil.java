package com.youxin.app.utils;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;



public class ReqUtil {

	private static final String name = "LOGIN_USER_ID";

	public static void setLoginedUserId(String userId) {
		try {
			RequestContextHolder.getRequestAttributes().setAttribute(name, userId, RequestAttributes.SCOPE_REQUEST);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Integer getUserId() {
		// 获取AuthorizationFilter通过查询令牌用户映射设置的userId
		Object obj = RequestContextHolder.getRequestAttributes().getAttribute(name, RequestAttributes.SCOPE_REQUEST);

		return null == obj ? 0 : Integer.parseInt(obj.toString());
	}


	public HttpServletRequest getRequest() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	}

	
}
