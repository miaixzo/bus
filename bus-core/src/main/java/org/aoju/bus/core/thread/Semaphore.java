package org.aoju.bus.core.thread;

/**
 * 带有信号量控制的{@link Runnable} 接口抽象实现
 *
 * <p>
 * 通过设置信号量,可以限制可以访问某些资源（物理或逻辑的）线程数目
 * 例如：设置信号量为2,表示最多有两个线程可以同时执行方法逻辑,
 * 其余线程等待,直到此线程逻辑执行完毕
 * </p>
 *
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public class Semaphore implements Runnable {

    /**
     * 实际执行的逻辑
     */
    private Runnable runnable;
    /**
     * 信号量
     */
    private java.util.concurrent.Semaphore semaphore;

    /**
     * 构造
     *
     * @param runnable  实际执行的线程逻辑
     * @param semaphore 信号量,多个线程必须共享同一信号量
     */
    public Semaphore(Runnable runnable, java.util.concurrent.Semaphore semaphore) {
        this.runnable = runnable;
        this.semaphore = semaphore;
    }

    @Override
    public void run() {
        if (null != this.semaphore) {
            try {
                semaphore.acquire();
                this.runnable.run();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                semaphore.release();
            }
        }
    }

}
