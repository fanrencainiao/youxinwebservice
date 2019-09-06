package com.youxin.app.yx.request.team;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年9月6日 下午2:37:47
 */
@Data
public class Create {
	/**
	 * 		是	群名称，最大长度64字符
	 */
	private String tname;
	/**
	 * 				是	群主用户帐号，最大长度32字符
	 */
	private String owner;
	/**
	 * 				是	["aaa","bbb"](JSONArray对应的accid，如果解析出错会报414)，一次最多拉200个成员
	 */
	private String members;
	/**
	 * 				否	群公告，最大长度1024字符
	 */
	private String announcement;
	/**
	 * 				否	群描述，最大长度512字符
	 */
	private String intro;
	/**
	 * 				是	邀请发送的文字，最大长度150字符
	 */
	private String msg;
	/**
	 * 				是	管理后台建群时，0不需要被邀请人同意加入群，1需要被邀请人同意才可以加入群。其它会返回414
	 */
	private int magree;
	/**
	 * 				是	群建好后，sdk操作时，0不用验证，1需要验证,2不允许任何人加入。其它返回414
	 */
	private int joinmode;
	/**
	 * 				否	自定义高级群扩展属性，第三方可以跟据此属性自定义扩展自己的群属性。（建议为json）,最大长度1024字符
	 */
	private String custom;
	/**
	 * 				否	群头像，最大长度1024字符
	 */
	private String icon;
	/**
	 * 				否	被邀请人同意方式，0-需要同意(默认),1-不需要同意。其它返回414
	 */
	private int beinvitemode;
	/**
	 * 				否	谁可以邀请他人入群，0-管理员(默认),1-所有人。其它返回414
	 */
	private int invitemode;
	/**
	 * 				否	谁可以修改群资料，0-管理员(默认),1-所有人。其它返回414
	 */
	private int uptinfomode;
	/**
	 * 				否	谁可以更新群自定义属性，0-管理员(默认),1-所有人。其它返回414
	 */
	private int upcustommode;
	/**
	 * 				否	该群最大人数(包含群主)，范围：2至应用定义的最大群人数(默认:200)。其它返回414
	 */
	private int teamMemberLimit;

}
