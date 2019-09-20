package com.youxin.app.yx.request;


import lombok.Data;

@Data
public class Msg {

	
	private String msgid;
	
	/**
	 * 发送者accid，用户帐号，最大32字符，必须保证一个APP内唯一
	 */
	private String from;
	/**
	 * 0：点对点个人消息，1：群消息（高级群），其他返回414
	 */
	private int ope;
	/**
	 * ope==0是表示accid即用户id，ope==1表示tid即群id
	 */
	private String to;
	/**
	 * 0 表示文本消息,1 表示图片，2 表示语音，3 表示视频，4 表示地理位置信息，6 表示文件，7:表示点对点消息撤回，8:表示群消息撤回，其它为参数错误 100 自定义消息类型（特别注意，对于未对接易盾反垃圾功能的应用，该类型的消息不会提交反垃圾系统检测）
	 */
	private int type;
	/**
	 * 0：点对点自定义通知，1：群消息自定义通知，其他返回414
	 */
	private int msgtype;
	/**
	 * 自定义通知内容，第三方组装的字符串，建议是JSON串，最大长度4096字符
	 */
	private String attach;
	/**
	 * 如果有指定推送，此属性指定为客户端本地的声音文件名，长度不要超过30个字符，如果不指定，会使用默认声音
	 */
	private String sound;
	/**
	 * 1表示只发在线，2表示会存离线，其他会报414错误。默认会存离线
	 */
	private int save;
	/**
	 * 最大长度5000字符，JSON格式。
	 */
	private String body;
	/**
	 * 对于对接了易盾反垃圾功能的应用，本消息是否需要指定经由易盾检测的内容（antispamCustom）。true或false, 默认false。只对消息类型为：100 自定义消息类型 的消息生效。
	 */
	private String antispam;
	/**
	 * 在antispam参数为true时生效。
	 *	自定义的反垃圾检测内容, JSON格式，长度限制同body字段，不能超过5000字符，要求antispamCustom格式如下：
	 *	
	 *	{"type":1,"data":"custom content"}
	 *	
  	 *	字段说明：
	 *	1. type: 1：文本，2：图片。
	 *	2. data: 文本内容or图片地址。
	 */
	private String antispamCustom;
	
	/**
	 * 发消息时特殊指定的行为选项,JSON格式，可用于指定消息的漫游，存云端历史，发送方多端同步，推送，消息抄送等特殊行为;option中字段不填时表示默认值 ，option示例:

	 * {"push":false,"roam":true,"history":false,"sendersync":true,"route":false,"badge":false,"needPushNick":true}
		
	 *	字段说明：
	 *	1. roam: 该消息是否需要漫游，默认true（需要app开通漫游消息功能）；
	 *	2. history: 该消息是否存云端历史，默认true；
	 *	3. sendersync: 该消息是否需要发送方多端同步，默认true；
	 *	4. push: 该消息是否需要APNS推送或安卓系统通知栏推送，默认true；
	 *	5. route: 该消息是否需要抄送第三方；默认true (需要app开通消息抄送功能);
	 *	6. badge:该消息是否需要计入到未读计数中，默认true;
	 *	7. needPushNick: 推送文案是否需要带上昵称，不设置该参数时默认true;
	 *	8. persistent: 是否需要存离线消息，不设置该参数时默认true。
	 */
	private String option;
	/**
	 * 推送文案，android以此为推送显示文案；ios若未填写payload，显示文案以pushcontent为准。超过500字符后，会对文本进行截断。
	 */
	private String pushcontent;
	/**
	 * ios 推送对应的payload,必须是JSON,不能超过2k字符
	 */
	private String payload;
	/**
	 * 开发者扩展字段，长度限制1024字符
	 */
	private String ext;
	/**
	 * 发送群消息时的强推（@操作）用户列表，格式为JSONArray，如["accid1","accid2"]。若forcepushall为true，则forcepushlist为除发送者外的所有有效群成员
	 */
	private String forcepushlist;
	/**
	 * 发送群消息时，针对强推（@操作）列表forcepushlist中的用户，强制推送的内容
	 */
	private String forcepushcontent;
	/**
	 * 发送群消息时，强推（@操作）列表是否为群里除发送者外的所有有效成员，true或false，默认为false
	 */
	private String forcepushall;
	/**
	 * 可选，反垃圾业务ID，实现“单条消息配置对应反垃圾”，若不填则使用原来的反垃圾配置
	 */
	private String bid;
	/**
	 * 可选，单条消息是否使用易盾反垃圾，可选值为0。 
	 *	0：（在开通易盾的情况下）不使用易盾反垃圾而是使用通用反垃圾，包括自定义消息。
		
	 *  若不填此字段，即在默认情况下，若应用开通了易盾反垃圾功能，则使用易盾反垃圾来进行垃圾消息的判断
	 */
	private int useYidun;
	/**
	 * 可选，群消息是否需要已读业务（仅对群消息有效），0:不需要，1:需要
	 */
	private int markRead;
	/**
	 * 是否为好友关系才发送消息，默认否
	 * 注：使用该参数需要先开通功能服务
	 */
	private String checkFriend="false";
	
	/**
	 * 是否需要返回消息ID
	*	false：不返回消息ID（默认值）
	*	true：返回消息ID（toAccids包含的账号数量不可以超过100个）
	 */
	private String returnMsgid="false";
	/**
	 * 1表示忽略撤回时间检测，其它为非法参数，如果需要撤回时间检测，不填即可
	 */
	private String ignoreTime;
	/**
	 * 要撤回消息的创建时间
	 */
	private Long timetag;
	/**
	 * 撤回消息描述
	 */
	private String msg;
}
