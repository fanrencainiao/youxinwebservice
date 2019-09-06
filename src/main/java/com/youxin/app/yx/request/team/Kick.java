package com.youxin.app.yx.request.team;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年9月6日 下午2:50:28
 */
@Data
public class Kick {
	/**
	 * 是 网易云通信服务器产生，群唯一标识，创建群时会返回，最大长度128字符
	 */
	private String tid;
	/**
	 * 是 群主用户帐号，最大长度32字符
	 */
	private String owner;
	/**
	 * 否 ["aaa","bbb"]（JSONArray对应的accid，如果解析出错，会报414）一次最多操作200个accid;
	 * 注：member或members任意提供一个，优先使用member参数
	 */
	private String members;
	/**
	 * 否 被移除人的accid，用户账号，最大长度32字符;注：member或members任意提供一个，优先使用member参数
	 */
	private String member;

	/**
	 * 否 自定义扩展字段，最大长度512
	 */
	private String attach;
}
