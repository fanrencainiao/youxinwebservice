package com.youxin.app.yx.request.team;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年9月6日 下午6:04:15
 */
@Data
public class MuteTlistAll {

	/**
	 * 是 网易云通信服务器产生，群唯一标识，创建群时会返回
	 */
	private String tid;
	/**
	 * 	是	群主的accid
	 */
	private String owner;
	/**
	 * 		否	true:禁言，false:解禁(mute和muteType至少提供一个，都提供时按mute处理)
	 */
	private String mute;
	/**
	 * 		否	禁言类型 0:解除禁言，1:禁言普通成员 3:禁言整个群(包括群主)
	 */
	private int muteType;

}
