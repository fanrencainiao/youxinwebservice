package com.youxin.app.service.impl;


import java.math.BigDecimal;
import java.util.List;

import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.youxin.app.entity.ConsumeRecord;
import com.youxin.app.repository.ConsumeRecordRepository;
import com.youxin.app.service.UserService;
import com.youxin.app.utils.DateUtil;
import com.youxin.app.utils.KConstants;
import com.youxin.app.utils.MongoUtil;
import com.youxin.app.utils.PageResult;
import com.youxin.app.utils.PageVO;
import com.youxin.app.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
public class ConsumeRecordManagerImpl {
	
	@Autowired
	private ConsumeRecordRepository repository;
	@Autowired
	private UserService userService;
	public void saveConsumeRecord(ConsumeRecord entity){
		repository.save(entity);
	}
	public ConsumeRecord getConsumeRecordByNo(String tradeNo){
		Query<ConsumeRecord> q=repository.createQuery();
		if(!StringUtil.isEmpty(tradeNo))
			q.filter("tradeNo", tradeNo);
		return q.get();
	}
	public Object reChargeList(Integer userId ,int pageIndex,int pageSize){
		Query<ConsumeRecord> q=repository.createQuery();
		q.filter("type", KConstants.MOENY_ADD);
		if(0!=userId)
			q.filter("userId", userId);
		List<ConsumeRecord> pageData = q.asList(MongoUtil.pageFindOption(pageIndex, pageSize));
		long total=q.count();
		return new PageVO(pageData, total,pageIndex, pageSize);
	}
	
	public PageResult<ConsumeRecord> consumeRecordList(Integer userId,int page,int limit){
	
		PageResult<ConsumeRecord>  result = new PageResult<ConsumeRecord>();
		Query<ConsumeRecord> q = repository.createQuery().order("-time");
		
		if(0!=userId)
			q.filter("userId", userId);
//			q.field("money").greaterThan(0);
			q.filter("status", KConstants.OrderStatus.END);
			
			result.setData(q.asList(MongoUtil.pageFindOption(page, limit)));
			
			result.setCount(q.count());
			return result;
	}
	
	public PageResult<ConsumeRecord> consumeRecordListByVip(Integer userId,int page,int limit){
		
		PageResult<ConsumeRecord>  result = new PageResult<ConsumeRecord>();
		Query<ConsumeRecord> q = repository.createQuery().order("-time");
		
		if(0!=userId)
			q.filter("userId", userId);
			q.field("money").greaterThan(0);
			q.filter("status", KConstants.OrderStatus.END);
			q.filter("type", KConstants.ConsumeType.VIP_COMMISSION);
			
			result.setData(q.asList(MongoUtil.pageFindOption(page, limit)));
			
			result.setCount(q.count());
			return result;
	}
	
	/** @Description:（用户充值记录） 
	* @param userId
	* @param type
	 * @param desc 
	* @param page
	* @param limit
	* @return
	**/ 
	public PageResult<ConsumeRecord> recharge(int userId,int type,int payType,String desc, int page,int limit,String startDate,String endDate){
		double totalMoney = 0;
		PageResult<ConsumeRecord> result = new PageResult<ConsumeRecord>();
		Query<ConsumeRecord> query = repository.createQuery().order("-time");
		if(0 != type)
			query.filter("type", type);
		else 
			query.or(query.criteria("type").equal(1),query.criteria("type").equal(3));// 过滤用户充值和后台
		if(0 != payType)
			query.filter("payType", payType);
		if(0 != userId)
			query.filter("userId", userId);
		if(!StringUtil.isEmpty(desc))
			query.field("desc").contains(desc);
		if(!StringUtil.isEmpty(startDate) && !StringUtil.isEmpty(endDate)){
			long startTime = 0; //开始时间（秒）
			long endTime = 0; //结束时间（秒）,默认为当前时间
			startTime = StringUtil.isEmpty(startDate) ? 0 :DateUtil.toDate(startDate).getTime()/1000;
//			DateUtil.getTodayNight();
			endTime = StringUtil.isEmpty(endDate) ? DateUtil.currentTimeSeconds() : DateUtil.toDate(endDate).getTime()/1000;
			long formateEndtime = DateUtil.getOnedayNextDay(endTime,1,0);
			query.field("time").greaterThan(startTime).field("time").lessThanOrEq(formateEndtime);
		}
		List<ConsumeRecord> recordList = query.asList(MongoUtil.pageFindOption(page, limit));
		for(ConsumeRecord record : recordList){
			record.setUserName(userService.getUserName(record.getUserId()));
		}
		List<ConsumeRecord> allList = query.asList();
		for (ConsumeRecord consumeRecord : allList) {
			//交易完成或者支付完成
			if(consumeRecord.getStatus()==1||consumeRecord.getStatus()==2) {
				BigDecimal bd1 = new BigDecimal(Double.toString(totalMoney)); 
		        BigDecimal bd2 = new BigDecimal(Double.toString(consumeRecord.getMoney())); 
				totalMoney =  bd1.add(bd2).doubleValue();
			}
		}
		result.setCount(query.count());
		log.info("当前总金额："+totalMoney);
		result.setTotal(totalMoney);
		result.setData(recordList);
		return result;
	}
	
	/**
	 * 用户付款记录
	 * @param userId
	 * @param type
	 * @param page
	 * @param limit
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public PageResult<ConsumeRecord> payment(int userId,int type,int page,int limit,String startDate,String endDate){
		double totalMoney = 0;
		PageResult<ConsumeRecord> result = new PageResult<ConsumeRecord>();
		Query<ConsumeRecord> query = repository.createQuery().order("-time");
		if(0 != type)
			query.filter("type", type);
		else 
			query.or(query.criteria("type").equal(10),query.criteria("type").equal(12));// 过滤用户付款码付款和二维码付款
		if(0 != userId)
			query.filter("userId", userId);
		if(!StringUtil.isEmpty(startDate) && !StringUtil.isEmpty(endDate)){
			long startTime = 0; //开始时间（秒）
			long endTime = 0; //结束时间（秒）,默认为当前时间
			startTime = StringUtil.isEmpty(startDate) ? 0 :DateUtil.toDate(startDate).getTime()/1000;
//			DateUtil.getTodayNight();
			endTime = StringUtil.isEmpty(endDate) ? DateUtil.currentTimeSeconds() : DateUtil.toDate(endDate).getTime()/1000;
			long formateEndtime = DateUtil.getOnedayNextDay(endTime,1,0);
			query.field("time").greaterThan(startTime).field("time").lessThanOrEq(formateEndtime);
		}
		List<ConsumeRecord> recordList = query.asList(MongoUtil.pageFindOption(page, limit));
		for(ConsumeRecord record : recordList){
			BigDecimal bd1 = new BigDecimal(Double.toString(totalMoney)); 
	        BigDecimal bd2 = new BigDecimal(Double.toString(record.getMoney())); 
			totalMoney =  bd1.add(bd2).doubleValue();
			record.setUserName(userService.getUserName(record.getUserId()));
		}
		result.setCount(query.count());
		log.info("当前总金额："+totalMoney);
		result.setTotal(totalMoney);
		result.setData(recordList);
		return result;
	}
	
}
