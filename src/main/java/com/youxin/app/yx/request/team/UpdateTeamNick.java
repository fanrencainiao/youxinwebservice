package com.youxin.app.yx.request.team;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年9月6日 下午5:38:43
 */
@Data
public class UpdateTeamNick {
	
	/**
	 * 是	群唯一标识，创建群时网易云通信服务器产生并返回
	 */
	private String tid;
	/**
	 * 		是	群主 accid
	 */
	private String owner;
	/**
	 * 		是	要修改群昵称的群成员 accid
	 */
	private String accid;
	/**
	 * 		否	accid 对应的群昵称，最大长度32字符
	 */
	private String nick;
	/**
	 * 		否	自定义扩展字段，最大长度1024字节
	 */
	private String custom;
}
