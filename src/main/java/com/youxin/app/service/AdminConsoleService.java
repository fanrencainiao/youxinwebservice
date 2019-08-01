package com.youxin.app.service;

import java.util.Map;

import com.youxin.app.entity.User;


public interface AdminConsoleService {
	
	User login(String userId,String pwd);
	
}
