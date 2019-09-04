package com.youxin.app.yx.request.subscribe;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年9月3日 上午11:29:22
 */
@Data
public class SubscribeBatchdel {
	/**
	 * 是 事件订阅人账号
	 */
	private String accid;
	/**
	 * 是 事件类型，固定设置为1，即 eventType=1
	 */
	private int eventType;


}
