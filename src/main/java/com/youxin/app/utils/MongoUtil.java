package com.youxin.app.utils;

import org.mongodb.morphia.query.FindOptions;

public class MongoUtil {
	/**
	 * 分页查询参数
	 * @param page   页码
	 * @param limit  每页数量
	 * @param start  0  : 页码从0开始   1 :页码从1开始
	 * @return
	 */
	public static FindOptions pageFindOption(int page,int limit) {
		FindOptions findOptions = new FindOptions();
		findOptions.skip(page * limit).limit(limit);
		return findOptions;
	}

}
