package com.youxin.app.yx.request.team;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年9月6日 下午4:47:53
 */
@Data
public class Query {

	/**
	 * 		是	群id列表，如["3083","3084"]
	 */
	private String tids;
	/**
	 * 		是	1表示带上群成员列表，0表示不带群成员列表，只返回群信息
	 */
	private int ope;
	
}
