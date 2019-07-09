package com.youxin.app.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
/**
 * 页面跳转控制类
 * @author cf
 *
 */
@Controller
public class TopageController {

	@RequestMapping(value = "/toPage", method = RequestMethod.GET)
	public String toPage(HttpServletRequest request) {
		String url = request.getParameter("url");
		return url;
	}

}
