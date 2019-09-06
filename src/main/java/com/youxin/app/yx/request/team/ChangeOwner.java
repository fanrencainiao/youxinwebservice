package com.youxin.app.yx.request.team;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年9月6日 下午5:05:30
 */
@Data
public class ChangeOwner {

	/**
	 * 是 网易云通信服务器产生，群唯一标识，创建群时会返回，最大长度128字符
	 */
	private String tid;
	/**
	 * 是 群主用户帐号，最大长度32字符
	 */
	private String owner;
	/**
	 * 是 新群主帐号，最大长度32字符
	 */
	private String newowner;
	/**
	 * 1:群主解除群主后离开群，2：群主解除群主后成为普通成员。其它414
	 */
	private int leave;
}
