package com.youxin.app.service;


import com.youxin.app.entity.MessageReceive;
import com.youxin.app.utils.PageResult;

public interface MessageReceiveService {
	
	PageResult<MessageReceive> getList(String fromAccount,String to,String eventType,String convType,Integer pageSize,Integer pageNum,Long startTime,Long endTime);

	void delMessage(String fromAccount, String to, String eventType, String convType, Long startTime, Long endTime);
	
}
