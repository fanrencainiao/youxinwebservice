package com.youxin.app.yx.request.history;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年9月3日 上午11:13:05
 */
@Data
public class QueryBroadcastMsgById {

	/**
	 *  是	广播消息ID，应用内唯一。
	 */
	private long broadcastId;
}
