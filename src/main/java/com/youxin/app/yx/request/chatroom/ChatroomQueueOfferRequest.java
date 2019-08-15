package com.youxin.app.yx.request.chatroom;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年8月14日 上午11:31:19
 */
@Data
public class ChatroomQueueOfferRequest {

	/**
	 * 是 聊天室id
	 */
	private long roomid;
	/**
	 * 是 elementKey,新元素的UniqKey,长度限制128字符
	 */
	private String key;
	/**
	 * 是 elementValue,新元素内容，长度限制4096字符
	 * 
	 */
	private String value;
	/**
	 * 否 提交这个新元素的操作者accid，默认为该聊天室的创建者，若operator对应的帐号不存在，会返回404错误。
	 * 若指定的operator不在线，则添加元素成功后的通知事件中的操作者默认为聊天室的创建者；若指定的operator在线，则通知事件的操作者为operator。
	 */
	private String operator;
	/**
	 * 否 这个新元素的提交者operator的所有聊天室连接在从该聊天室掉线或者离开该聊天室的时候，提交的元素是否需要删除。
	 * true：需要删除；false：不需要删除。默认false。 当指定该参数为true时，若operator当前不在该聊天室内，则会返回403错误。
	 */
//	private  String  transient="false";

}
