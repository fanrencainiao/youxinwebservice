package com.youxin.app.yx.request.subscribe;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年9月3日 上午11:17:00
 */
@Data
public class SubscribeAdd {
	/**
	 * 是 事件订阅人账号
	 */
	private String accid;
	/**
	 * 是 事件类型，固定设置为1，即 eventType=1
	 */
	private int eventType;
	/**
	 * 是 被订阅人的账号列表，最多100个账号，JSONArray格式。示例：["pub_user1","pub_user2"]
	 */
	private String publisherAccids;
	/**
	 * 是 有效期，单位：秒。取值范围：60～2592000（即60秒到30天）
	 */
	private long ttl;
}
