/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.xyz;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.*;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.thread.*;

/**
 * Thread pool utility class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ThreadKit {

    /**
     * Gets a new cached thread pool.
     *
     * @return An {@link ExecutorService}.
     */
    public static ExecutorService newExecutor() {
        return ExecutorBuilder.of().useSynchronousQueue().build();
    }

    /**
     * Gets a new single-threaded executor.
     *
     * @return An {@link ExecutorService}.
     */
    public static ExecutorService newSingleExecutor() {
        return ExecutorBuilder.of().setCorePoolSize(1).setMaxPoolSize(1).setKeepAliveTime(0).buildFinalizable();
    }

    /**
     * Creates a new fixed-size thread pool.
     *
     * @param poolSize The number of threads in the pool.
     * @return A {@link ThreadPoolExecutor}.
     */
    public static ThreadPoolExecutor newExecutor(final int poolSize) {
        return newExecutor(poolSize, poolSize);
    }

    /**
     * Gets a new thread pool.
     *
     * @param corePoolSize    The initial pool size.
     * @param maximumPoolSize The maximum pool size.
     * @return A {@link ThreadPoolExecutor}.
     */
    public static ThreadPoolExecutor newExecutor(final int corePoolSize, final int maximumPoolSize) {
        return ExecutorBuilder.of().setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize).build();
    }

    /**
     * Gets a new thread pool with a specified queue size.
     *
     * @param corePoolSize     The initial pool size.
     * @param maximumPoolSize  The maximum pool size.
     * @param maximumQueueSize The maximum queue size.
     * @return A {@link ThreadPoolExecutor}.
     */
    public static ExecutorService newExecutor(
            final int corePoolSize,
            final int maximumPoolSize,
            final int maximumQueueSize) {
        return ExecutorBuilder.of().setCorePoolSize(corePoolSize).setMaxPoolSize(maximumPoolSize)
                .useLinkedBlockingQueue(maximumQueueSize).build();
    }

    /**
     * Gets a new thread pool sized based on the blocking coefficient. The formula is: `poolSize = availableProcessors /
     * (1 - blockingCoefficient)`.
     *
     * @param blockingCoefficient The blocking coefficient (between 0 and 1).
     * @return A {@link ThreadPoolExecutor}.
     */
    public static ThreadPoolExecutor newExecutorByBlockingCoefficient(final float blockingCoefficient) {
        if (blockingCoefficient >= 1 || blockingCoefficient < 0) {
            throw new IllegalArgumentException("[blockingCoefficient] must be between 0 and 1.");
        }
        final int poolSize = (int) (RuntimeKit.getProcessorCount() / (1 - blockingCoefficient));
        return ExecutorBuilder.of().setCorePoolSize(poolSize).setMaxPoolSize(poolSize).setKeepAliveTime(0L).build();
    }

    /**
     * Gets a new fixed-size thread pool.
     *
     * @param nThreads         The number of threads.
     * @param threadNamePrefix The prefix for thread names.
     * @param isBlocked        If `true`, uses a `BlockPolicy` for rejected tasks.
     * @return An {@link ExecutorService}.
     */
    public static ExecutorService newFixedExecutor(
            final int nThreads,
            final String threadNamePrefix,
            final boolean isBlocked) {
        return newFixedExecutor(nThreads, 1024, threadNamePrefix, isBlocked);
    }

    /**
     * Gets a new fixed-size thread pool with a specified queue size.
     *
     * @param nThreads         The number of threads.
     * @param maximumQueueSize The queue size.
     * @param threadNamePrefix The prefix for thread names.
     * @param isBlocked        If `true`, uses a `BlockPolicy`.
     * @return A {@link ThreadPoolExecutor}.
     */
    public static ThreadPoolExecutor newFixedExecutor(
            final int nThreads,
            final int maximumQueueSize,
            final String threadNamePrefix,
            final boolean isBlocked) {
        return newFixedExecutor(
                nThreads,
                maximumQueueSize,
                threadNamePrefix,
                (isBlocked ? RejectPolicy.BLOCK : RejectPolicy.ABORT).getValue());
    }

    /**
     * Gets a new fixed-size thread pool with a specified rejection policy.
     *
     * @param nThreads         The number of threads.
     * @param maximumQueueSize The queue size.
     * @param threadNamePrefix The prefix for thread names.
     * @param handler          The rejection handler.
     * @return A {@link ThreadPoolExecutor}.
     */
    public static ThreadPoolExecutor newFixedExecutor(
            final int nThreads,
            final int maximumQueueSize,
            final String threadNamePrefix,
            final RejectedExecutionHandler handler) {
        return ExecutorBuilder.of().setCorePoolSize(nThreads).setMaxPoolSize(nThreads)
                .setWorkQueue(new LinkedBlockingQueue<>(maximumQueueSize))
                .setThreadFactory(createThreadFactory(threadNamePrefix)).setHandler(handler).build();
    }

    /**
     * Executes a task in the global thread pool.
     *
     * @param runnable The `Runnable` task.
     */
    public static void execute(final Runnable runnable) {
        GlobalThreadPool.execute(runnable);
    }

    /**
     * Executes a task asynchronously in a new thread.
     *
     * @param runnable The task.
     * @param isDaemon If `true`, the thread is a daemon thread.
     * @return The `Runnable` task.
     */
    public static Runnable execAsync(final Runnable runnable, final boolean isDaemon) {
        final Thread thread = new Thread(runnable);
        thread.setDaemon(isDaemon);
        thread.start();
        return runnable;
    }

    /**
     * Executes an asynchronous task that returns a value.
     *
     * @param <T>  The result type.
     * @param task The `Callable` task.
     * @return A {@link Future} representing the pending result.
     */
    public static <T> Future<T> execAsync(final Callable<T> task) {
        return GlobalThreadPool.submit(task);
    }

    /**
     * Executes an asynchronous task.
     *
     * @param runnable The `Runnable` task.
     * @return A {@link Future}.
     */
    public static Future<?> execAsync(final Runnable runnable) {
        return GlobalThreadPool.submit(runnable);
    }

    /**
     * Creates a new `CompletionService` using the global thread pool.
     *
     * @param <T> The result type.
     * @return A {@link CompletionService}.
     */
    public static <T> CompletionService<T> newCompletionService() {
        return new ExecutorCompletionService<>(GlobalThreadPool.getExecutor());
    }

    /**
     * Creates a new `CompletionService` with a specified executor.
     *
     * @param <T>      The result type.
     * @param executor The {@link ExecutorService}.
     * @return A {@link CompletionService}.
     */
    public static <T> CompletionService<T> newCompletionService(final ExecutorService executor) {
        return new ExecutorCompletionService<>(executor);
    }

    /**
     * Creates a new `CountDownLatch`.
     *
     * @param taskCount The number of times `countDown()` must be invoked.
     * @return A {@link CountDownLatch}.
     */
    public static CountDownLatch newCountDownLatch(final int taskCount) {
        return new CountDownLatch(taskCount);
    }

    /**
     * Creates a new `CyclicBarrier`.
     *
     * @param taskCount The number of parties.
     * @return A {@link CyclicBarrier}.
     */
    public static CyclicBarrier newCyclicBarrier(final int taskCount) {
        return new CyclicBarrier(taskCount);
    }

    /**
     * Creates a new `Phaser`.
     *
     * @param taskCount The number of parties.
     * @return A {@link Phaser}.
     */
    public static Phaser newPhaser(final int taskCount) {
        return new Phaser(taskCount);
    }

    /**
     * Creates a new non-daemon thread with normal priority.
     *
     * @param runnable The `Runnable`.
     * @param name     The thread name.
     * @return A {@link Thread}.
     */
    public static Thread newThread(final Runnable runnable, final String name) {
        final Thread t = newThread(runnable, name, false);
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }

    /**
     * Creates a new thread.
     *
     * @param runnable The `Runnable`.
     * @param name     The thread name.
     * @param isDaemon If `true`, creates a daemon thread.
     * @return A {@link Thread}.
     */
    public static Thread newThread(final Runnable runnable, final String name, final boolean isDaemon) {
        final Thread t = new Thread(null, runnable, name);
        t.setDaemon(isDaemon);
        return t;
    }

    /**
     * Suspends the current thread for a specified duration.
     *
     * @param timeout  The duration.
     * @param timeUnit The time unit.
     * @return `false` if interrupted, `true` otherwise.
     */
    public static boolean sleep(final Number timeout, final TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout.longValue());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }

    /**
     * Suspends the current thread for a specified number of milliseconds.
     *
     * @param millis The duration in milliseconds.
     * @return `false` if interrupted, `true` otherwise.
     */
    public static boolean sleep(final Number millis) {
        if (millis == null) {
            return true;
        }
        return sleep(millis.longValue());
    }

    /**
     * Suspends the current thread for a specified number of milliseconds.
     *
     * @param millis The duration in milliseconds.
     * @return `false` if interrupted, `true` otherwise.
     */
    public static boolean sleep(final long millis) {
        if (millis > 0) {
            try {
                Thread.sleep(millis);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return true;
    }

    /**
     * Suspends the current thread for at least the specified number of milliseconds.
     *
     * @param millis The duration in milliseconds.
     * @return `false` if interrupted, `true` otherwise.
     */
    public static boolean safeSleep(final Number millis) {
        if (millis == null) {
            return true;
        }
        return safeSleep(millis.longValue());
    }

    /**
     * Suspends the current thread for at least the specified number of milliseconds.
     *
     * @param millis The duration in milliseconds.
     * @return `false` if interrupted, `true` otherwise.
     */
    public static boolean safeSleep(final long millis) {
        long done = 0;
        long before;
        while (done < millis) {
            before = System.nanoTime();
            if (!sleep(millis - done)) {
                return false;
            }
            done += (System.nanoTime() - before) / 1_000_000;
        }
        return true;
    }

    /**
     * @return The current thread's stack trace.
     */
    public static StackTraceElement[] getStackTrace() {
        return Thread.currentThread().getStackTrace();
    }

    /**
     * Gets a specific `StackTraceElement` by its depth.
     *
     * @param i The depth.
     * @return The `StackTraceElement`.
     */
    public static StackTraceElement getStackTraceElement(int i) {
        final StackTraceElement[] stackTrace = getStackTrace();
        if (i < 0) {
            i += stackTrace.length;
        }
        return stackTrace[i];
    }

    /**
     * Creates a `ThreadLocal` object.
     *
     * @param <T>           The type of the object.
     * @param isInheritable If `true`, child threads will inherit the value from the parent thread.
     * @return The `ThreadLocal`.
     */
    public static <T> ThreadLocal<T> createThreadLocal(final boolean isInheritable) {
        if (isInheritable) {
            return new InheritableThreadLocal<>();
        } else {
            return new ThreadLocal<>();
        }
    }

    /**
     * Creates a `ThreadLocal` object with an initial value supplier.
     *
     * @param <T>      The type of the object.
     * @param supplier The supplier for the initial value.
     * @return The `ThreadLocal`.
     */
    public static <T> ThreadLocal<T> createThreadLocal(final Supplier<? extends T> supplier) {
        return ThreadLocal.withInitial(supplier);
    }

    /**
     * Creates a new `ThreadFactoryBuilder`.
     *
     * @return A `ThreadFactoryBuilder`.
     */
    public static ThreadFactoryBuilder createThreadFactoryBuilder() {
        return ThreadFactoryBuilder.of();
    }

    /**
     * Creates a `ThreadFactory` with a custom thread name prefix.
     *
     * @param threadNamePrefix The prefix for thread names.
     * @return A {@link ThreadFactory}.
     */
    public static ThreadFactory createThreadFactory(final String threadNamePrefix) {
        return ThreadFactoryBuilder.of().setNamePrefix(threadNamePrefix).build();
    }

    /**
     * Interrupts a thread.
     *
     * @param thread The thread to interrupt.
     * @param isJoin If `true`, waits for the thread to die after interrupting.
     */
    public static void interrupt(final Thread thread, final boolean isJoin) {
        if (null != thread && !thread.isInterrupted()) {
            thread.interrupt();
            if (isJoin) {
                waitForDie(thread);
            }
        }
    }

    /**
     * Waits for the current thread to die.
     */
    public static void waitForDie() {
        waitForDie(Thread.currentThread());
    }

    /**
     * Waits for a thread to die.
     *
     * @param thread The thread.
     */
    public static void waitForDie(final Thread thread) {
        if (null == thread) {
            return;
        }
        boolean dead = false;
        do {
            try {
                thread.join();
                dead = true;
            } catch (final InterruptedException e) {
                // ignore and retry
            }
        } while (!dead);
    }

    /**
     * Gets all threads in the current thread's parent group.
     *
     * @return An array of threads.
     */
    public static Thread[] getThreads() {
        return getThreads(Thread.currentThread().getThreadGroup().getParent());
    }

    /**
     * Gets all threads in a specific thread group.
     *
     * @param group The thread group.
     * @return An array of threads.
     */
    public static Thread[] getThreads(final ThreadGroup group) {
        final Thread[] slackList = new Thread[group.activeCount() * 2];
        final int actualSize = group.enumerate(slackList);
        final Thread[] result = new Thread[actualSize];
        System.arraycopy(slackList, 0, result, 0, actualSize);
        return result;
    }

    /**
     * Gets the main thread of the current process.
     *
     * @return The main thread.
     */
    public static Thread getMainThread() {
        for (final Thread thread : getThreads()) {
            if (thread.getId() == 1) {
                return thread;
            }
        }
        return null;
    }

    /**
     * Gets the current thread's thread group.
     *
     * @return The thread group.
     */
    public static ThreadGroup currentThreadGroup() {
        return Thread.currentThread().getThreadGroup();
    }

    /**
     * Gets the current thread's ID.
     *
     * @return The thread ID.
     */
    public static long currentThreadId() {
        return Thread.currentThread().getId();
    }

    /**
     * Creates a named `ThreadFactory`.
     *
     * @param prefix   The thread name prefix.
     * @param isDaemon If `true`, created threads will be daemon threads.
     * @return A {@link ThreadFactory}.
     */
    public static ThreadFactory newNamedThreadFactory(final String prefix, final boolean isDaemon) {
        return new NamedThreadFactory(prefix, isDaemon);
    }

    /**
     * Creates a named `ThreadFactory`.
     *
     * @param prefix      The thread name prefix.
     * @param threadGroup The thread group.
     * @param isDaemon    If `true`, created threads will be daemon threads.
     * @return A {@link ThreadFactory}.
     */
    public static ThreadFactory newNamedThreadFactory(
            final String prefix,
            final ThreadGroup threadGroup,
            final boolean isDaemon) {
        return new NamedThreadFactory(prefix, threadGroup, isDaemon);
    }

    /**
     * Creates a named `ThreadFactory`.
     *
     * @param prefix      The thread name prefix.
     * @param threadGroup The thread group.
     * @param isDaemon    If `true`, created threads will be daemon threads.
     * @param handler     The handler for uncaught exceptions.
     * @return A {@link ThreadFactory}.
     */
    public static ThreadFactory newNamedThreadFactory(
            final String prefix,
            final ThreadGroup threadGroup,
            final boolean isDaemon,
            final UncaughtExceptionHandler handler) {
        return new NamedThreadFactory(prefix, threadGroup, isDaemon, handler);
    }

    /**
     * Blocks the current thread, typically used to keep a main method from exiting.
     *
     * @param object The object to lock on.
     */
    public static void sync(final Object object) {
        synchronized (object) {
            try {
                object.wait();
            } catch (final InterruptedException e) {
                // ignore
            }
        }
    }

    /**
     * Performs a concurrency test.
     *
     * @param threadSize The number of concurrent threads.
     * @param runnable   The task to execute.
     * @return A {@link ConcurrencyTester} with the results.
     */
    public static ConcurrencyTester concurrencyTest(final int threadSize, final Runnable runnable) {
        try (ConcurrencyTester tester = new ConcurrencyTester(threadSize)) {
            return tester.test(runnable);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates a {@link ScheduledThreadPoolExecutor}.
     *
     * @param corePoolSize The core pool size.
     * @return A {@link ScheduledThreadPoolExecutor}.
     */
    public static ScheduledThreadPoolExecutor createScheduledExecutor(final int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize);
    }

    /**
     * Schedules a task to run periodically.
     *
     * @param executor              The `ScheduledExecutorService`.
     * @param command               The task.
     * @param initialDelay          The initial delay in milliseconds.
     * @param period                The period in milliseconds.
     * @param fixedRateOrFixedDelay If `true`, uses fixed-rate execution; otherwise, fixed-delay.
     * @return The `ScheduledExecutorService`.
     */
    public static ScheduledExecutorService schedule(
            final ScheduledExecutorService executor,
            final Runnable command,
            final long initialDelay,
            final long period,
            final boolean fixedRateOrFixedDelay) {
        return schedule(executor, command, initialDelay, period, TimeUnit.MILLISECONDS, fixedRateOrFixedDelay);
    }

    /**
     * Schedules a task to run periodically.
     *
     * @param executor              The `ScheduledExecutorService`.
     * @param command               The task.
     * @param initialDelay          The initial delay.
     * @param period                The period.
     * @param timeUnit              The time unit for delay and period.
     * @param fixedRateOrFixedDelay If `true`, uses fixed-rate execution; otherwise, fixed-delay.
     * @return The `ScheduledExecutorService`.
     */
    public static ScheduledExecutorService schedule(
            ScheduledExecutorService executor,
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

}
