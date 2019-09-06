package com.youxin.app.yx.request.team;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年9月6日 下午2:42:15
 */
@Data
public class Add {

	/**
	 * 是 网易云通信服务器产生，群唯一标识，创建群时会返回，最大长度128字符
	 */
	private String tid;
	/**
	 * 是 群主用户帐号，最大长度32字符
	 */
	private String owner;
	/**
	 * 是 ["aaa","bbb"](JSONArray对应的accid，如果解析出错会报414)，一次最多拉200个成员
	 */
	private String members;
	/**
	 * 是 管理后台建群时，0不需要被邀请人同意加入群，1需要被邀请人同意才可以加入群。其它会返回414
	 */
	private int magree;
	/**
	 * 是 邀请发送的文字，最大长度150字符
	 */
	private String msg;
	/**
	 * 否 自定义扩展字段，最大长度512
	 */
	private String attach;
}
