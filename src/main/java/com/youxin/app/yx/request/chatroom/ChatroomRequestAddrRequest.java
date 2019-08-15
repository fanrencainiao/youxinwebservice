package com.youxin.app.yx.request.chatroom;

import lombok.Data;
/**
 * 请求聊天室地址
 * @author cf
 * @date 2019年8月14日 上午10:24:39
 */
@Data
public class ChatroomRequestAddrRequest {

	/**
	 * 是	聊天室id
	 */
	private long roomid;
	/**
	 * accid	String	是	进入聊天室的账号
	 */
	private String accid;
	/**
	 * clienttype	int	否	1:weblink（客户端为web端时使用）; 2:commonlink（客户端为非web端时使用）;3:wechatlink(微信小程序使用), 默认1
	 */
	private int clienttype;
	/**
	 * clientip	String	否	客户端ip，传此参数时，会根据用户ip所在地区，返回合适的地址
	 */
	private String clientip;
	
	
}
