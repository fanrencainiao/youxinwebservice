package com.youxin.app.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.youxin.app.utils.supper.Callback;





public  class ThreadUtil{
	public static final ScheduledExecutorService mThreadPool = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors()*2);
	/**
	* @Description: TODO(立即执行 线程)
	* @param @param callback    参数
	 */
	public static void executeInThread(Callback callback){
		mThreadPool.execute(new Runnable() {
    		@Override
			public void run() {
    			callback.execute(Thread.currentThread().getName());
    		}
		});
	}
	public static void executeInThread(Callback callback,Object obj){
		mThreadPool.execute(new Runnable() {
    		@Override
			public void run() {
    			callback.execute(obj);
    		}
		});
	}
	
	/**
	* @Description: TODO(延时执行线程)
	* @param @param callback 延时 秒钟
	 */
	public static void executeInThread(Callback callback,long delay){
		mThreadPool.schedule(new Runnable() {
    		@Override
			public void run() {
    			callback.execute(Thread.currentThread().getName());
    		}
		}, delay, TimeUnit.SECONDS);
	}

}
