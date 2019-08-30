package com.youxin.app.yx.request.chatroom;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年8月30日 上午10:26:13
 */
@Data
public class ChatroomTopn {
	/**
	 * 否 topn值，可选值 1~500，默认值100
	 */
	private int rootopnmid;
	/**
	 * 否 需要查询的指标所在的时间坐标点，不提供则默认当前时间，单位秒/毫秒皆可
	 */
	private long timestamp;
	/**
	 * 否 统计周期，可选值包括 hour/day, 默认hour
	 */
	private String period;
	/**
	 * 否 取排序值,可选值 active/enter/message,分别表示按日活排序，进入人次排序和消息数排序， 默认active
	 */
	private String orderby;

}
