package com.youxin.app.aspect;

import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public abstract class BaseRunnable implements Runnable{

	/**
	 * 被循环执行的次数
	 */
	public AtomicInteger loopCount = new AtomicInteger();
	
	
	
	
	
}
