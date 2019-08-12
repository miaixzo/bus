package org.aoju.bus.tracer.thread.executor;

import java.util.concurrent.*;

/**
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public class TraceExecutors {

    private TraceExecutors() {
    }

    public static ExecutorService newFixedThreadPool(int nThreads) {
        return new TraceExecutorService(Executors.newFixedThreadPool(nThreads));
    }

    public static ExecutorService newWorkStealingPool(int parallelism) {
        return Executors.newWorkStealingPool(parallelism);
    }

    public static ExecutorService newWorkStealingPool() {
        return Executors.newWorkStealingPool();
    }

    public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
        return new TraceExecutorService(Executors.newFixedThreadPool(nThreads, threadFactory));
    }

    public static ExecutorService newSingleThreadExecutor() {
        return new TraceExecutorService(Executors.newSingleThreadExecutor());
    }

    public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory) {
        return new TraceExecutorService(Executors.newSingleThreadExecutor(threadFactory));
    }


    public static ExecutorService newCachedThreadPool() {
        return new TraceExecutorService(Executors.newCachedThreadPool());
    }

    public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory) {
        return new TraceExecutorService(Executors.newCachedThreadPool(threadFactory));
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
        return Executors.newSingleThreadScheduledExecutor();
    }

    public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
        return Executors.newSingleThreadScheduledExecutor(threadFactory);
    }

    public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
        return Executors.newScheduledThreadPool(corePoolSize);
    }

    public static ScheduledExecutorService newScheduledThreadPool(
            int corePoolSize, ThreadFactory threadFactory) {
        return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
    }
    
}
