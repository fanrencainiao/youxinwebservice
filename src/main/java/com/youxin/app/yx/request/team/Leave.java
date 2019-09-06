package com.youxin.app.yx.request.team;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年9月6日 下午5:53:33
 */
@Data
public class Leave {

	/**
	 * 是 网易云通信服务器产生，群唯一标识，创建群时会返回
	 */
	private String tid;
	/**
	 * 是 退群的accid
	 */
	private String accid;
}
