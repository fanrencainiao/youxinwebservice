package com.youxin.app.yx.request.history;

import lombok.Data;

/**
 * 
 * @author cf
 * @date 2019年9月3日 上午10:59:00
 */
@Data
public class QueryTeamMsg {

	/**
	 * 是 群id
	 */
	private String tid;
	/**
	 * 是 查询用户对应的accid.
	 */
	private String accid;
	
	/**
	 * 是 开始时间，ms
	 */
	private String begintime;
	/**
	 * 是 截止时间，ms
	 */
	private String endtime;
	/**
	 * 是 本次查询的消息条数上限(最多100条),小于等于0，或者大于100，会提示参数错误
	 */
	private int limit;
	/**
	 * 否 1按时间正序排列，2按时间降序排列。其它返回参数414错误.默认是按降序排列
	 */
	private int reverse;
	/**
	 * 否 查询指定的多个消息类型，类型之间用","分割，不设置该参数则查询全部类型消息格式示例： 0,1,2,3 类型支持：
	 * 1:图片，2:语音，3:视频，4:地理位置，5:通知，6:文件，10:提示，11:Robot，100:自定义
	 */
	private String type;

}
