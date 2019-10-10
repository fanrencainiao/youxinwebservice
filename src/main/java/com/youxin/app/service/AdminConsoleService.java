package com.youxin.app.service;


import com.youxin.app.entity.User;


public interface AdminConsoleService {
	
	User login(String userId,String pwd);
	
}
