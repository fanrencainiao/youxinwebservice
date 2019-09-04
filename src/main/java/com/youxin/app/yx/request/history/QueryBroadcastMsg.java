package com.youxin.app.yx.request.history;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年9月3日 上午11:06:57
 */
@Data
public class QueryBroadcastMsg {

	/**
	 * 否 查询的起始ID，0表示查询最近的limit条。默认0。
	 */
	private long broadcastId;
	/**
	 * 否 查询的条数，最大100。默认100。
	 */
	private int limit;
	/**
	 * 否 查询的类型，1表示所有，2表示查询存离线的，3表示查询不存离线的。默认1。
	 * 
	 */
	private long type;
}
