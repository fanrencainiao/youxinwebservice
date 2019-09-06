package com.youxin.app.yx.request.team;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年9月6日 下午4:26:03
 */
@Data
public class Remove {

	/**
	 * 是 网易云通信服务器产生，群唯一标识，创建群时会返回，最大长度128字符
	 */
	private String tid;
	/**
	 * 是 群主用户帐号，最大长度32字符
	 */
	private String owner;
	
}
