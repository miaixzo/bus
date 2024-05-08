/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.toolkit;

import org.miaixz.bus.core.lang.Console;
import org.miaixz.bus.core.lock.AtomicNoLock;
import org.miaixz.bus.core.thread.*;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Supplier;

/**
 * 线程池工具
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ThreadKit {

    private static final AtomicNoLock NO_LOCK = new AtomicNoLock();

    /**
     * 新建一个线程池
     * <pre>
     *    1. 初始线程数为corePoolSize指定的大小
     *    2. 没有最大线程数限制
     *    3. 默认使用LinkedBlockingQueue，默认队列大小为1024
     * </pre>
     *
     * @param corePoolSize 同时执行的线程数大小
     * @return ExecutorService
     */
    public static ExecutorService newExecutor(int corePoolSize) {
        return ExecutorBuilder.of().setCorePoolSize(corePoolSize).build();
    }

    /**
     * 获得一个新的线程池
     * <pre>
     *    1. 初始线程数为 0
     *    2. 最大线程数为Integer.MAX_VALUE
     *    3. 使用SynchronousQueue
     *    4. 任务直接提交给线程而不保持它们
     * </pre>
     *
     * @return ExecutorService
     */
    public static ExecutorService newExecutor() {
        return ExecutorBuilder.of().setWorkQueue(new SynchronousQueue<>()).build();
    }

    /**
     * 获得一个新的线程池
     * 如果maximumPoolSize &gt;= corePoolSize，在没有新任务加入的情况下，多出的线程将最多保留60s
     *
     * @param corePoolSize    初始线程池大小
     * @param maximumPoolSize 最大线程池大小
     * @return {@link ThreadPoolExecutor}
     */
    public static ThreadPoolExecutor newExecutor(int corePoolSize, int maximumPoolSize) {
        return ExecutorBuilder.of()
                .setCorePoolSize(corePoolSize)
                .setMaxPoolSize(maximumPoolSize)
                .build();
    }

    /**
     * 获得一个新的线程池，并指定最大任务队列大小
     * 如果maximumPoolSize &gt;= corePoolSize，在没有新任务加入的情况下，多出的线程将最多保留60s
     *
     * @param corePoolSize     初始线程池大小
     * @param maximumPoolSize  最大线程池大小
     * @param maximumQueueSize 最大任务队列大小
     * @return {@link ThreadPoolExecutor}
     */
    public static ExecutorService newExecutor(int corePoolSize, int maximumPoolSize, int maximumQueueSize) {
        return ExecutorBuilder.of()
                .setCorePoolSize(corePoolSize)
                .setMaxPoolSize(maximumPoolSize)
                .setWorkQueue(new LinkedBlockingQueue<>(maximumQueueSize))
                .build();
    }

    /**
     * 获得一个新的线程池,只有单个线程
     * <pre>
     *    1. 初始线程数为 1
     *    2. 最大线程数为 1
     *    3. 默认使用LinkedBlockingQueue，默认队列大小为1024
     *    4. 同时只允许一个线程工作，剩余放入队列等待，等待数超过1024报错
     * </pre>
     *
     * @return ExecutorService
     */
    public static ExecutorService newSingleExecutor() {
        return ExecutorBuilder.of()
                .setCorePoolSize(1)
                .setMaxPoolSize(1)
                .setKeepAliveTime(0)
                .buildFinalizable();
    }

    /**
     * 获得一个新的线程池
     * 传入阻塞系数,线程池的大小计算公式为：CPU可用核心数 / (1 - 阻塞因子)
     * Blocking Coefficient(阻塞系数) = 阻塞时间／(阻塞时间+使用CPU的时间)
     * 计算密集型任务的阻塞系数为0,而IO密集型任务的阻塞系数则接近于1
     * <p>
     * see: http://blog.csdn.net/partner4java/article/details/9417663
     *
     * @param blockingCoefficient 阻塞系数,阻塞因子介于0~1之间的数,阻塞因子越大,线程池中的线程数越多
     * @return {@link ThreadPoolExecutor}
     */
    public static ThreadPoolExecutor newExecutorByBlockingCoefficient(float blockingCoefficient) {
        if (blockingCoefficient >= 1 || blockingCoefficient < 0) {
            throw new IllegalArgumentException("[blockingCoefficient] must between 0 and 1, or equals 0.");
        }

        // 最佳的线程数 = CPU可用核心数 / (1 - 阻塞系数)
        int poolSize = (int) (RuntimeKit.getProcessorCount() / (1 - blockingCoefficient));
        return ExecutorBuilder.of().setCorePoolSize(poolSize).setMaxPoolSize(poolSize).setKeepAliveTime(0L).build();
    }

    /**
     * 获取一个新的线程池，默认的策略如下
     * <pre>
     *     1. 核心线程数与最大线程数为nThreads指定的大小
     *     2. 默认使用{@link LinkedBlockingQueue}，默认队列大小为1024
     *     3. 如果isBlocked为{code true}，当执行拒绝策略的时候会处于阻塞状态，直到能添加到队列中或者被{@link Thread#interrupt()}中断
     * </pre>
     *
     * @param nThreads         线程池大小
     * @param threadNamePrefix 线程名称前缀
     * @param isBlocked        是否使用{@link BlockPolicy}策略
     * @return {@link ExecutorService}
     */
    public static ExecutorService newFixedExecutor(int nThreads, String threadNamePrefix, boolean isBlocked) {
        return newFixedExecutor(nThreads, 1024, threadNamePrefix, isBlocked);
    }

    /**
     * 获取一个新的线程池，默认的策略如下
     * <pre>
     *     1. 核心线程数与最大线程数为nThreads指定的大小
     *     2. 默认使用{@link LinkedBlockingQueue}
     *     3. 如果isBlocked为{code true}，当执行拒绝策略的时候会处于阻塞状态，直到能添加到队列中或者被{@link Thread#interrupt()}中断
     * </pre>
     *
     * @param nThreads         线程池大小
     * @param maximumQueueSize 队列大小
     * @param threadNamePrefix 线程名称前缀
     * @param isBlocked        是否使用{@link BlockPolicy}策略
     * @return {@link ExecutorService}
     */
    public static ExecutorService newFixedExecutor(int nThreads, int maximumQueueSize, String threadNamePrefix, boolean isBlocked) {
        return newFixedExecutor(nThreads, maximumQueueSize, threadNamePrefix,
                (isBlocked ? RejectPolicy.BLOCK : RejectPolicy.ABORT).getValue());
    }

    /**
     * 获得一个新的线程池，默认策略如下
     * <pre>
     *     1. 核心线程数与最大线程数为nThreads指定的大小
     *     2. 默认使用 {@link LinkedBlockingQueue}
     * </pre>
     *
     * @param nThreads         线程池大小
     * @param maximumQueueSize 队列大小
     * @param threadNamePrefix 线程名称前缀
     * @param handler          拒绝策略
     * @return {@link ExecutorService}
     */
    public static ExecutorService newFixedExecutor(int nThreads,
                                                   int maximumQueueSize,
                                                   String threadNamePrefix,
                                                   RejectedExecutionHandler handler) {
        return ExecutorBuilder.of()
                .setCorePoolSize(nThreads).setMaxPoolSize(nThreads)
                .setWorkQueue(new LinkedBlockingQueue<>(maximumQueueSize))
                .setThreadFactory(createThreadFactory(threadNamePrefix))
                .setHandler(handler)
                .build();
    }

    /**
     * 直接在公共线程池中执行线程
     *
     * @param runnable 可运行对象
     */
    public static void execute(Runnable runnable) {
        GlobalThread.execute(runnable);
    }

    /**
     * 执行异步方法
     *
     * @param runnable 需要执行的方法体
     * @param isDaemon 是否守护线程 守护线程会在主线程结束后自动结束
     * @return 执行的方法体
     */
    public static Runnable excAsync(final Runnable runnable, boolean isDaemon) {
        Thread thread = new Thread(() -> runnable.run());
        thread.setDaemon(isDaemon);
        thread.start();

        return runnable;
    }

    /**
     * 执行有返回值的异步方法
     * Future代表一个异步执行的操作,通过get()方法可以获得操作的结果,如果异步操作还没有完成,则,get()会使当前线程阻塞
     *
     * @param <T>  回调对象类型
     * @param task {@link Callable}
     * @return Future
     */
    public static <T> Future<T> execAsync(Callable<T> task) {
        return GlobalThread.submit(task);
    }

    /**
     * 执行有返回值的异步方法
     * Future代表一个异步执行的操作,通过get()方法可以获得操作的结果,如果异步操作还没有完成,则,get()会使当前线程阻塞
     *
     * @param runnable 可运行对象
     * @return {@link Future}
     */
    public static Future<?> execAsync(Runnable runnable) {
        return GlobalThread.submit(runnable);
    }

    /**
     * 执行异步方法
     *
     * @param runnable 需要执行的方法体
     * @param isDaemon 是否守护线程。守护线程会在主线程结束后自动结束
     * @return 执行的方法体
     */
    public static Runnable execAsync(Runnable runnable, boolean isDaemon) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(isDaemon);
        thread.start();

        return runnable;
    }

    /**
     * 新建一个CompletionService,调用其submit方法可以异步执行多个任务,最后调用take方法按照完成的顺序获得其结果
     * 若未完成,则会阻塞
     *
     * @param <T> 回调对象类型
     * @return CompletionService
     */
    public static <T> CompletionService<T> newCompletionService() {
        return new ExecutorCompletionService<>(GlobalThread.getExecutor());
    }

    /**
     * 新建一个CompletionService,调用其submit方法可以异步执行多个任务,最后调用take方法按照完成的顺序获得其结果
     * 若未完成,则会阻塞
     *
     * @param <T>      回调对象类型
     * @param executor 执行器 {@link ExecutorService}
     * @return CompletionService
     */
    public static <T> CompletionService<T> newCompletionService(ExecutorService executor) {
        return new ExecutorCompletionService<>(executor);
    }

    /**
     * 新建一个CountDownLatch,一个同步辅助类,在完成一组正在其他线程中执行的操作之前,它允许一个或多个线程一直等待
     *
     * @param threadCount 线程数量
     * @return CountDownLatch
     */
    public static CountDownLatch newCountDownLatch(int threadCount) {
        return new CountDownLatch(threadCount);
    }

    /**
     * 创建新线程,非守护线程,正常优先级,线程组与当前线程的线程组一致
     *
     * @param runnable {@link Runnable}
     * @param name     线程名
     * @return {@link Thread}
     */
    public static Thread newThread(Runnable runnable, String name) {
        final Thread t = newThread(runnable, name, false);
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }

    /**
     * 创建新线程
     *
     * @param runnable {@link Runnable}
     * @param name     线程名
     * @param isDaemon 是否守护线程
     * @return {@link Thread}
     */
    public static Thread newThread(Runnable runnable, String name, boolean isDaemon) {
        final Thread t = new Thread(null, runnable, name);
        t.setDaemon(isDaemon);
        return t;
    }

    /**
     * 挂起当前线程
     *
     * @param timeout  挂起的时长
     * @param timeUnit 时长单位
     * @return 被中断返回false, 否则true
     */
    public static boolean sleep(Number timeout, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout.longValue());
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    /**
     * 挂起当前线程
     *
     * @param millis 挂起的毫秒数
     * @return 被中断返回false，否则true
     */
    public static boolean sleep(Number millis) {
        if (null == millis) {
            return true;
        }
        return sleep(millis.longValue());
    }

    /**
     * 挂起当前线程
     *
     * @param millis 挂起的毫秒数
     * @return 被中断返回false，否则true
     */
    public static boolean sleep(long millis) {
        if (millis > 0) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * 考虑{@link Thread#sleep(long)}方法有可能时间不足给定毫秒数,此方法保证sleep时间不小于给定的毫秒数
     *
     * @param millis 给定的sleep时间
     * @return 被中断返回false, 否则true
     * @see ThreadKit#sleep(Number)
     */
    public static boolean safeSleep(Number millis) {
        if (null == millis) {
            return true;
        }

        return safeSleep(millis.longValue());
    }

    /**
     * 考虑{@link Thread#sleep(long)}方法有可能时间不足给定毫秒数，此方法保证sleep时间不小于给定的毫秒数
     *
     * @param millis 给定的sleep时间
     * @return 被中断返回false，否则true
     * @see ThreadKit#sleep(Number)
     */
    public static boolean safeSleep(long millis) {
        long done = 0;
        long before;
        long spendTime;
        while (done >= 0 && done < millis) {
            before = System.currentTimeMillis();
            if (false == sleep(millis - done)) {
                return false;
            }
            spendTime = System.currentTimeMillis() - before;
            if (spendTime <= 0) {
                // Sleep花费时间为0或者负数，说明系统时间被拨动
                break;
            }
            done += spendTime;
        }
        return true;
    }

    /**
     * @return 获得堆栈列表
     */
    public static StackTraceElement[] getStackTrace() {
        return Thread.currentThread().getStackTrace();
    }

    /**
     * 获得堆栈项
     *
     * @param i 第几个堆栈项
     * @return 堆栈项
     */
    public static StackTraceElement getStackTraceElement(int i) {
        StackTraceElement[] stackTrace = getStackTrace();
        if (i < 0) {
            i += stackTrace.length;
        }
        return stackTrace[i];
    }

    /**
     * 创建本地线程对象
     *
     * @param <T>           持有对象类型
     * @param isInheritable 是否为子线程提供从父线程那里继承的值
     * @return 本地线程
     */
    public static <T> ThreadLocal<T> createThreadLocal(boolean isInheritable) {
        if (isInheritable) {
            return new InheritableThreadLocal<>();
        } else {
            return new ThreadLocal<>();
        }
    }

    /**
     * 创建本地线程对象
     *
     * @param <T>      持有对象类型
     * @param supplier 初始化线程对象函数
     * @return 本地线程
     * @see ThreadLocal#withInitial(Supplier)
     */
    public static <T> ThreadLocal<T> createThreadLocal(Supplier<? extends T> supplier) {
        return ThreadLocal.withInitial(supplier);
    }

    /**
     * 创建ThreadFactoryBuilder
     *
     * @return ThreadFactoryBuilder
     * @see ThreadBuilder#build()
     */
    public static ThreadBuilder createThreadFactoryBuilder() {
        return ThreadBuilder.of();
    }

    /**
     * 创建自定义线程名称前缀的{@link ThreadFactory}
     *
     * @param threadNamePrefix 线程名称前缀
     * @return {@link ThreadFactory}
     * @see ThreadBuilder#build()
     */
    public static ThreadFactory createThreadFactory(String threadNamePrefix) {
        return ThreadBuilder.of().setNamePrefix(threadNamePrefix).build();
    }

    /**
     * 结束线程,调用此方法后,线程将抛出 {@link InterruptedException}异常
     *
     * @param thread 线程
     * @param isJoin 是否等待结束
     */
    public static void interrupt(Thread thread, boolean isJoin) {
        if (null != thread && false == thread.isInterrupted()) {
            thread.interrupt();
            if (isJoin) {
                waitFor(thread);
            }
        }
    }

    /**
     * 等待当前线程结束. 调用 {@link Thread#join()} 并忽略 {@link InterruptedException}
     */
    public static void waitFor() {
        waitFor(Thread.currentThread());
    }

    /**
     * 等待线程结束. 调用 {@link Thread#join()} 并忽略 {@link InterruptedException}
     *
     * @param thread 线程
     */
    public static void waitFor(Thread thread) {
        boolean dead = false;
        do {
            try {
                thread.join();
                dead = true;
            } catch (InterruptedException e) {
                // ignore
            }
        } while (!dead);
    }

    /**
     * 获取JVM中与当前线程同组的所有线程
     *
     * @return 线程对象数组
     */
    public static Thread[] getThreads() {
        return getThreads(Thread.currentThread().getThreadGroup().getParent());
    }

    /**
     * 获取JVM中与当前线程同组的所有线程
     * 使用数组二次拷贝方式,防止在线程列表获取过程中线程终止
     * from Voovan
     *
     * @param group 线程组
     * @return 线程对象数组
     */
    public static Thread[] getThreads(ThreadGroup group) {
        final Thread[] slackList = new Thread[group.activeCount() * 2];
        final int actualSize = group.enumerate(slackList);
        final Thread[] result = new Thread[actualSize];
        System.arraycopy(slackList, 0, result, 0, actualSize);
        return result;
    }

    /**
     * 获取进程的主线程
     *
     * @return 进程的主线程
     */
    public static Thread getMainThread() {
        for (Thread thread : getThreads()) {
            if (thread.getId() == 1) {
                return thread;
            }
        }
        return null;
    }

    /**
     * 创建线程工厂
     *
     * @param prefix   线程名前缀
     * @param isDaemon 是否守护线程
     * @return the object
     */
    public static ThreadFactory newNamedThreadFactory(String prefix, boolean isDaemon) {
        return new NamedThreadFactory(prefix, isDaemon);
    }

    /**
     * 创建线程工厂
     *
     * @param prefix      线程名前缀
     * @param threadGroup 线程组,可以为null
     * @param isDaemon    是否守护线程
     * @return the object
     */
    public static ThreadFactory newNamedThreadFactory(String prefix, ThreadGroup threadGroup, boolean isDaemon) {
        return new NamedThreadFactory(prefix, threadGroup, isDaemon);
    }

    /**
     * 创建线程工厂
     *
     * @param prefix      线程名前缀
     * @param threadGroup 线程组,可以为null
     * @param isDaemon    是否守护线程
     * @param handler     未捕获异常处理
     * @return the object
     */
    public static ThreadFactory newNamedThreadFactory(String prefix, ThreadGroup threadGroup, boolean isDaemon, UncaughtExceptionHandler handler) {
        return new NamedThreadFactory(prefix, threadGroup, isDaemon, handler);
    }

    /**
     * 阻塞当前线程,保证在main方法中执行不被退出
     *
     * @param object 对象所在线程
     */
    public static void sync(Object object) {
        synchronized (object) {
            try {
                object.wait();
            } catch (InterruptedException e) {
                Console.error(e.getMessage());
            }
        }
    }

    /**
     * 创建{@link ScheduledThreadPoolExecutor}
     *
     * @param corePoolSize 初始线程池大小
     * @return {@link ScheduledThreadPoolExecutor}
     */
    public static ScheduledThreadPoolExecutor createScheduledExecutor(int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize);
    }

    /**
     * 开始执行一个定时任务，执行方式分fixedRate模式和fixedDelay模式。<br>
     * 注意：此方法的延迟和周期的单位均为毫秒。
     *
     * <ul>
     *     <li>fixedRate 模式：以固定的频率执行。每period的时刻检查，如果上个任务完成，启动下个任务，否则等待上个任务结束后立即启动。</li>
     *     <li>fixedDelay模式：以固定的延时执行。上次任务结束后等待period再执行下个任务。</li>
     * </ul>
     *
     * @param executor              定时任务线程池，{@code null}新建一个默认线程池
     * @param command               需要定时执行的逻辑
     * @param initialDelay          初始延迟，单位毫秒
     * @param period                执行周期，单位毫秒
     * @param fixedRateOrFixedDelay {@code true}表示fixedRate模式，{@code false}表示fixedDelay模式
     * @return {@link ScheduledExecutorService}
     * @since 5.5.8
     */
    public static ScheduledExecutorService schedule(final ScheduledExecutorService executor,
                                                    final Runnable command,
                                                    final long initialDelay,
                                                    final long period,
                                                    final boolean fixedRateOrFixedDelay) {
        return schedule(executor, command, initialDelay, period, TimeUnit.MILLISECONDS, fixedRateOrFixedDelay);
    }

    /**
     * 开始执行一个定时任务，执行方式分fixedRate模式和fixedDelay模式。
     *
     * <ul>
     *     <li>fixedRate 模式：以固定的频率执行。每period的时刻检查，如果上个任务完成，启动下个任务，否则等待上个任务结束后立即启动。</li>
     *     <li>fixedDelay模式：以固定的延时执行。上次任务结束后等待period再执行下个任务。</li>
     * </ul>
     *
     * @param executor              定时任务线程池，{@code null}新建一个默认线程池
     * @param command               需要定时执行的逻辑
     * @param initialDelay          初始延迟
     * @param period                执行周期
     * @param timeUnit              时间单位
     * @param fixedRateOrFixedDelay {@code true}表示fixedRate模式，{@code false}表示fixedDelay模式
     * @return {@link ScheduledExecutorService}
     */
    public static ScheduledExecutorService schedule(ScheduledExecutorService executor,
                                                    final Runnable command,
                                                    final long initialDelay,
                                                    final long period,
                                                    final TimeUnit timeUnit,
                                                    final boolean fixedRateOrFixedDelay) {
        if (null == executor) {
            executor = createScheduledExecutor(2);
        }
        if (fixedRateOrFixedDelay) {
            executor.scheduleAtFixedRate(command, initialDelay, period, timeUnit);
        } else {
            executor.scheduleWithFixedDelay(command, initialDelay, period, timeUnit);
        }

        return executor;
    }

    /**
     * 创建{@link StampedLock}锁
     *
     * @return {@link StampedLock}锁
     */
    public static StampedLock createStampLock() {
        return new StampedLock();
    }

    /**
     * 创建{@link ReentrantReadWriteLock}锁
     *
     * @param fair 是否公平锁
     * @return {@link ReentrantReadWriteLock}锁
     */
    public static ReentrantReadWriteLock createReadWriteLock(boolean fair) {
        return new ReentrantReadWriteLock(fair);
    }

    /**
     * 获取单例的无锁对象
     *
     * @return {@link AtomicNoLock}
     */
    public static AtomicNoLock getNoLock() {
        return NO_LOCK;
    }

    public static final class FastBufferThread extends Thread {

        private int pageIndex;

        public FastBufferThread(Runnable target, String name) {
            super(target, name);
        }

        public int getPageIndex() {
            return pageIndex;
        }

        public void setPageIndex(int pageIndex) {
            this.pageIndex = pageIndex;
        }
    }

}
