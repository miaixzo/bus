package org.aoju.bus.office.metric;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 可以挂起的线程池执行程序,池中只允许有一个线程
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public class SuspendableThread extends ThreadPoolExecutor {

    private final ReentrantLock suspendLock = new ReentrantLock();
    private final Condition availableCondition = suspendLock.newCondition();
    private boolean available;

    public SuspendableThread(final ThreadFactory threadFactory) {
        super(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
    }

    @Override
    protected void beforeExecute(final Thread thread, final Runnable task) {
        super.beforeExecute(thread, task);

        suspendLock.lock();
        try {
            while (!available) {
                availableCondition.await();
            }
        } catch (InterruptedException interruptedEx) {
            thread.interrupt();
        } finally {
            suspendLock.unlock();
        }
    }

    /**
     * 设置此执行程序的可用性.
     *
     * @param available 如果执行器可以执行任务{@code true}，否则{@code false} .
     */
    public void setAvailable(final boolean available) {
        suspendLock.lock();
        try {
            this.available = available;
            if (available) {
                availableCondition.signalAll();
            }
        } finally {
            suspendLock.unlock();
        }
    }

}
