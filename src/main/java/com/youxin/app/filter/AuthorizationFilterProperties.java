package com.youxin.app.filter;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component("afp")
@ConfigurationProperties(prefix = "authorizationfilter")
public class AuthorizationFilterProperties {

	private List<String> requestUriList;
	private String url;
	

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<String> getRequestUriList() {
		return requestUriList;
	}

	public void setRequestUriList(List<String> requestUriList) {
		this.requestUriList = requestUriList;
	}

}
