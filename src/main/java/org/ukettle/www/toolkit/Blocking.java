package org.ukettle.www.toolkit;

import org.ukettle.widget.kettle.entity.KettleSpoon;

public class Blocking {

	/**
	 * 控制整个kettle有多少个job执行，如果超过设置的最大值，则加入该队列。
	 */
	private static ListQueue<KettleSpoon> jobQueue = new ListQueue<KettleSpoon>();

	public static final int DEFAULT_KETTLE_WORKER_THREAD_NUMS = 6;

	public static final int DEFAULT_EKETTLE_JOB_RUNNING_TIME = 4 * 60 * 1000;

	public static void addToWaitingQueue(KettleSpoon obj) {
		jobQueue.offer(obj);
	}

	public static KettleSpoon getObjectFromWaitingQueue()
			throws InterruptedException {
		return jobQueue.take();
	}

	public static void clean() {
		jobQueue.clear();
	}

	public static int jobSize() {
		return jobQueue.size();
	}

}