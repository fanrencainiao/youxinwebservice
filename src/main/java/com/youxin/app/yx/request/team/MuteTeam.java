package com.youxin.app.yx.request.team;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年9月6日 下午5:47:38
 */
@Data
public class MuteTeam {

	/**
	 * 是 网易云通信服务器产生，群唯一标识，创建群时会返回
	 */
	private String tid;
	/**
	 * 是 要操作的群成员accid
	 */
	private String accid;
	/**
	 * 是 1：关闭消息提醒，2：打开消息提醒，其他值无效
	 * 
	 */
	private int ope;
}
