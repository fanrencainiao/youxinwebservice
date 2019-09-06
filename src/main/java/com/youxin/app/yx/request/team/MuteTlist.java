package com.youxin.app.yx.request.team;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年9月6日 下午5:50:04
 */
@Data
public class MuteTlist {

	/**
	 * 是 网易云通信服务器产生，群唯一标识，创建群时会返回
	 */
	private String tid;
	/**
	 * 是 群主accid
	 */
	private String owner;
	/**
	 * 是 禁言对象的accid
	 */
	private String accid;
	/**
	 * 是 1-禁言，0-解禁
	 */
	private int mute;

}
