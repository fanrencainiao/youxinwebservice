package com.youxin.app.yx.request.history;

import lombok.Data;
/**
 * 
 * @author cf
 * @date 2019年9月3日 上午11:05:23
 */
@Data
public class QueryUserEvents {

	
	/**
	 * 是 查询用户对应的accid.
	 */
	private String accid;
	
	/**
	 * 是 开始时间，ms
	 */
	private String begintime;
	/**
	 * 是 截止时间，ms
	 */
	private String endtime;
	/**
	 * 是 本次查询的消息条数上限(最多100条),小于等于0，或者大于100，会提示参数错误
	 */
	private int limit;
	/**
	 * 否 1按时间正序排列，2按时间降序排列。其它返回参数414错误.默认是按降序排列
	 */
	private int reverse;
}
