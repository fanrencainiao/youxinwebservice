package com.youxin.app.yx.request.chatroom;

import lombok.Data;

/**
 * 发送聊天室消息
 * 
 * @author cf
 *
 */
@Data
public class ChatroomSendMsgRequest {

	/**
	 * 是 聊天室id
	 */
	private long roomid;

	/**
	 * 是 客户端消息id，使用uuid等随机串，msgId相同的消息会被客户端去重
	 */
	private String msgId;
	/**
	 * 是 消息发出者的账号accid
	 */
	private String fromAccid;
	/**
	 * 是 消息类型： 0: 表示文本消息， 1: 表示图片， 2: 表示语音， 3: 表示视频， 4: 表示地理位置信息， 6: 表示文件， 10:
	 * 表示Tips消息， 100: 自定义消息类型（特别注意，对于未对接易盾反垃圾功能的应用，该类型的消息不会提交反垃圾系统检测）
	 */
	private int msgType;
	/**
	 * 否 重发消息标记，0：非重发消息，1：重发消息，如重发消息会按照msgid检查去重逻辑
	 */
	private int resendFlag;
	/**
	 * 否 消息内容，格式同消息格式示例中的body字段,长度限制4096字符
	 */
	private String attach;
	/**
	 * 否 消息扩展字段，内容可自定义，请使用JSON格式，长度限制4096字符
	 */
	private String ext;

	/**
	 * 否 对于对接了易盾反垃圾功能的应用，本消息是否需要指定经由易盾检测的内容（antispamCustom）。true或false,
	 * 默认false。只对消息类型为：100 自定义消息类型 的消息生效。
	 */
	private String antispam;
	/**
	 * 否 在antispam参数为true时生效。 自定义的反垃圾检测内容,
	 * JSON格式，长度限制同body字段，不能超过5000字符，要求antispamCustom格式如下： {"type":1,"data":"custom
	 * content"} 字段说明： 1. type: 1：文本，2：图片。 2. data: 文本内容or图片地址。
	 */
	private String antispamCustom;
	/**
	 * 否 是否跳过存储云端历史，0：不跳过，即存历史消息；1：跳过，即不存云端历史；默认0
	 */
	private int skipHistory;
	/**
	 * 否 可选，反垃圾业务ID，实现“单条消息配置对应反垃圾”，若不填则使用原来的反垃圾配置
	 */
	private String bid;
	/**
	 * Boolean 否 可选，true表示是高优先级消息，云信会优先保障投递这部分消息；false表示低优先级消息。默认false。
	 * 强烈建议应用恰当选择参数，以便在必要时，优先保障应用内的高优先级消息的投递。若全部设置为高优先级，则等于没有设置。
	 */
	private String highPriority;
	/**
	 * 否 可选，单条消息是否使用易盾反垃圾，可选值为0。 0：（在开通易盾的情况下）不使用易盾反垃圾而是使用通用反垃圾，包括自定义消息。
	 * 
	 * 若不填此字段，即在默认情况下，若应用开通了易盾反垃圾功能，则使用易盾反垃圾来进行垃圾消息的判断
	 */
	private int useYidun;
	/**
	 * Boolean 否 可选，true表示会重发消息，false表示不会重发消息。默认true
	 */
	private String needHighPriorityMsgResend;
	/**
	 * 可选，消息丢弃的概率。取值范围[0-9999]； 其中0代表不丢弃消息，9999代表99.99%的概率丢弃消息，默认不丢弃；
	 * 注意如果填写了此参数，highPriority参数则会无效； 此参数可用于流控特定业务类型的消息。
	 */
	private int abandonRatio;

}
