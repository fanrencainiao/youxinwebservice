package com.youxin.app.entity;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.NotSaved;

import lombok.Data;

@Data
@Entity(value = "message_receive", noClassnameStored = true)
public class MessageReceive {
	@NotSaved
	private String icon;	
	private String eventType;		//值为1，表示是会话类型的消息
	private String convType;//	会话具体类型：PERSON（点对点会话内消息）、TEAM（群聊会话内消息）、CUSTOM_PERSON（点对点自定义系统通知及内置好友系统通知）、CUSTOM_TEAM（群聊自定义系统通知及内置群聊系统通知），字符串类型
	private String to		;//若convType为PERSON或CUSTOM_PERSON，则to为消息接收者的用户账号，字符串类型；若convType为TEAM或CUSTOM_TEAM，则to为tid，即群id，可转为Long型数据
	private String fromAccount;//		消息发送者的用户账号，字符串类型
	private String fromClientType;//		发送客户端类型： AOS、IOS、PC、WINPHONE、WEB、REST，字符串类型
	private String fromDeviceId;	//	发送设备id，字符串类型
	private String fromNick;//		发送方昵称，字符串类型
	private Long msgTimestamp;//	消息发送时间，字符串类型
	/**convType为PERSON、TEAM时对应的消息类型：
	 * TEXT //文本消息
	PICTURE //图片消息
	AUDIO //语音消息
	VIDEO //视频消息
	LOCATION //地理位置消息
	NOTIFICATION //群通知消息，如群资料更新通知、群解散通知等。
	FILE //文件消息
	TIPS //提示消息
	CUSTOM //自定义消息

	convType为CUSTOM_PERSON对应的通知消息类型：
	FRIEND_ADD //对方 请求/已经 添加为好友
	FRIEND_DELETE //被对方删除好友
	CUSTOM_P2P_MSG //点对点自定义系统通知

	convType为CUSTOM_TEAM对应的通知消息类型（请注意与NOTIFICATION区分）：
	TEAM_APPLY //申请入群
	TEAM_APPLY_REJECT //拒绝入群申请
	TEAM_INVITE //邀请进群
	TEAM_INVITE_REJECT //拒绝邀请
	CUSTOM_TEAM_MSG //群组自定义系统通知
	 */
	private String msgType;		
	
	private String body;	//消息内容，字符串类型。针对聊天室消息而言，无此字段。内容转由attach承载。
	private String attach;//		附加消息，字符串类型
	private String msgidClient;//		客户端生成的消息id，仅在convType为PERSON或TEAM含此字段，字符串类型
	@Id
	private String msgidServer;//	String	服务端生成的消息id，可转为Long型数据
	private String resendFlag;//	String	重发标记：0不是重发, 1是重发。仅在convType为PERSON或TEAM时含此字段，可转为Integer类型数据
	private String customSafeFlag;//	String	自定义系统通知消息是否存离线:0：不存，1：存。仅在convType为CUSTOM_PERSON或CUSTOM_TEAM时含此字段，可转为Integer类型数据
	private String customApnsText;//
	/**
	 * 当前群成员accid列表。仅在群成员不超过200人，且convType为TEAM或CUSTOM_TEAM时包含此字段，字符串类型。
	 * tMembers格式举例：
	{
	... // 其他字段
	"tMembers":"[123, 456]" //相关的accid为 123 和 456
	}自定义系统通知消息推送文本。仅在convType为CUSTOM_PERSON或CUSTOM_TEAM时含此字段，字符串类型
	 */
	private String tMembers;
	
	private String ext;		//消息扩展字段
	private String antispam	;//	标识是否被反垃圾，仅在被反垃圾时才有此字段，可转为Boolean类型数据
	/**
	 * 该字段中子字段释义如下：
	yidunBusType：0：易盾文本反垃圾业务；1、易盾图片反垃圾业务；2、用户资料反垃圾业务；3、用户头像反垃圾业务。

	action：处理结果：检测结果，0：通过，1：嫌疑，2：不通过。 （只有yidunBusType为0或2时，抄送时才有此字段）

	labels：具体的反垃圾判断细节：
	文本类反垃圾参考：
	http://support.dun.163.com/documents/2018041901?docId=150425947576913920 labels字段的释义
	图片类反垃圾参考：
	http://support.dun.163.com/documents/2018041902?docId=150429557194936320 labels字段的释义
	 */
	private String yidunRes	;//	易盾反垃圾的原始处理细节，只有接入了相关功能易盾反垃圾的应用才会有这个字段。详见以下1.4.5.1、P2P：文本消息 和 1.4.5.2、P2P：图片消息的举例说明。

	
	private String blacklist;//		标识点对点消息是否黑名单，仅在消息发送方被拉黑时才有此字段，可转为Boolean类型数据
	private String ip;//		消息发送方的客户端IP地址(仅SDK发送的消息才有该字段)
	private String port	;//	消息发送方的客户端端口号(仅SDK发送的消息才有该字段)
}
