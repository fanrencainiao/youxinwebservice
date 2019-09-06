package com.youxin.app.yx.request.team;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年9月6日 下午5:09:18
 */
@Data
public class AddOrUpdateManager {

	/**
	 * 是 网易云通信服务器产生，群唯一标识，创建群时会返回，最大长度128字符
	 */
	private String tid;
	/**
	 * 是 群主用户帐号，最大长度32字符
	 */
	private String owner;
	/**
	 * 是 ["aaa","bbb"](JSONArray对应的accid，如果解析出错会报414)，长度最大1024字符（一次添加最多10个管理员）
	 */
	private String members;
}
