package com.youxin.app.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.youxin.app.utils.StringUtil;
/**
 * 页面跳转控制类
 * @author cf
 *
 */
@Controller
public class TopageController {

	@RequestMapping(value = "/toPage/{url}", method = RequestMethod.GET)
	public String toPage(@PathVariable String url) {
		if(StringUtil.isEmpty(url)||url.contains("404")) 
			url="404";
		else
			url="console/"+url;
		return url;
	}

}
