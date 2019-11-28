package com.youxin.app.service;


import com.youxin.app.entity.BankRecord;
import com.youxin.app.entity.User;
import com.youxin.app.utils.PageResult;


public interface AdminConsoleService {
	
	User login(String userId,String pwd);

	PageResult<BankRecord> getBankRecordList(String bankCard, int userId, int pageIndex, int limit);
	
}
