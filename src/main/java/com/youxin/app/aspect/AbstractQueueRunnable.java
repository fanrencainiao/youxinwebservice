package com.youxin.app.aspect;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 */
public abstract class AbstractQueueRunnable<T> extends BaseRunnable {
	

	/** The msg queue. */
	protected ConcurrentLinkedQueue<T> msgQueue = new ConcurrentLinkedQueue<>();
	
	protected long sleep=1000;
	/**
	 * @param sleep the sleep to set
	 */
	public void setSleep(long sleep) {
		this.sleep = sleep;
	}
	
	protected int batchSize=1;
	/**
	 * @param batchSize the batchSize to set
	 */
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	
	/**
	 * 
	 * @author tanyaowu
	 */
	public abstract void runTask();
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while(true) {
			try {
				if(msgQueue.isEmpty()&&0<sleep) {
					Thread.sleep(sleep);
				}else {
					loopCount.set(0);
					runTask();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	
	
	/**
	 * @return
	 *
	 */
	public boolean addMsg(T t) {
		return msgQueue.offer(t);
	}
	
	/**
	 * 清空处理的队列消息
	 */
	public void clearMsgQueue() {
		msgQueue.clear();
	}

	public boolean isNeededExecute() {
		return !msgQueue.isEmpty();
	}
	
}
