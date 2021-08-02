/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.http.metric;

import org.aoju.bus.http.Builder;
import org.aoju.bus.http.NewCall;
import org.aoju.bus.http.RealCall;
import org.aoju.bus.http.RealCall.AsyncCall;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 关于何时执行异步请求的策略
 * 每个dispatcher使用一个{@link ExecutorService}在内部运行调用。
 * 如果您提供自己的执行程序，它应该能够并发地运行{@linkplain #getMaxRequests 配置的最大调用数}
 *
 * @author Kimi Liu
 * @version 6.2.6
 * @since JDK 1.8+
 */
public final class Dispatcher {

    /**
     * 准备异步调用的顺序，他们将被运行
     */
    private final Deque<AsyncCall> readyAsyncCalls = new ArrayDeque<>();
    /**
     * 运行异步调用。包括尚未结束的已取消调用
     */
    private final Deque<AsyncCall> runningAsyncCalls = new ArrayDeque<>();
    /**
     * 运行同步调用。包括尚未结束的已取消调用
     */
    private final Deque<RealCall> runningSyncCalls = new ArrayDeque<>();
    private int maxRequests = 64;
    private int maxRequestsPerHost = 5;
    private Runnable idleCallback;
    /**
     * 执行调用
     */
    private ExecutorService executorService;

    public Dispatcher(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public Dispatcher() {
    }

    public synchronized ExecutorService executorService() {
        if (null == executorService) {
            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new SynchronousQueue<>(), Builder.threadFactory("Httpd Dispatcher", false));
        }
        return executorService;
    }

    public synchronized int getMaxRequests() {
        return maxRequests;
    }

    /**
     * 设置并发执行的最大请求数。上述请求在内存中排队，等待正在运行的调用完成
     * 如果在调用它时有超过{@code maxRequests}的请求在运行，那么这些请求将保持运行状态
     *
     * @param maxRequests 最大请求数
     */
    public void setMaxRequests(int maxRequests) {
        if (maxRequests < 1) {
            throw new IllegalArgumentException("max < 1: " + maxRequests);
        }
        synchronized (this) {
            this.maxRequests = maxRequests;
        }
        promoteAndExecute();
    }

    public synchronized int getMaxRequestsPerHost() {
        return maxRequestsPerHost;
    }

    /**
     * 设置每个主机并发执行的最大请求数。这将根据URL的主机名限制请求。
     * 注意，对单个IP地址的并发请求仍然可能超过这个限制:多个主机名可能共享一个IP地址，或者通过相同的HTTP代理进行路由
     * 如果在调用它时有超过{@code maxRequestsPerHost}的请求在运行，那么这些请求将保持运行状态
     *
     * @param maxRequestsPerHost 最大请求数
     */
    public void setMaxRequestsPerHost(int maxRequestsPerHost) {
        if (maxRequestsPerHost < 1) {
            throw new IllegalArgumentException("max < 1: " + maxRequestsPerHost);
        }
        synchronized (this) {
            this.maxRequestsPerHost = maxRequestsPerHost;
        }
        promoteAndExecute();
    }


    /**
     * 设置一个回调，以便每次调度程序变为空闲时调用(当运行的调用数量返回零时)
     *
     * @param idleCallback 回调
     */
    public synchronized void setIdleCallback(Runnable idleCallback) {
        this.idleCallback = idleCallback;
    }

    public void enqueue(AsyncCall call) {
        synchronized (this) {
            readyAsyncCalls.add(call);
        }
        promoteAndExecute();
    }

    /**
     * 取消当前排队或执行的所有调用。包括同步执行的
     * {@linkplain NewCall#execute()}和异步
     * 执行的{@linkplain NewCall#enqueue}。
     */
    public synchronized void cancelAll() {
        for (AsyncCall call : readyAsyncCalls) {
            call.get().cancel();
        }

        for (AsyncCall call : runningAsyncCalls) {
            call.get().cancel();
        }

        for (RealCall call : runningSyncCalls) {
            call.cancel();
        }
    }

    /**
     * 将符合条件的调用从{@link #readyAsyncCalls}提升到{@link #runningAsyncCalls}，
     * 并在executor服务上运行它们。必须不与同步调用，因为执行调用可以调用到用户代码
     *
     * @return 如果调度程序当前正在运行调用，则为true
     */
    private boolean promoteAndExecute() {
        assert (!Thread.holdsLock(this));

        List<AsyncCall> executableCalls = new ArrayList<>();
        boolean isRunning;
        synchronized (this) {
            for (Iterator<AsyncCall> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
                AsyncCall asyncCall = i.next();

                if (runningAsyncCalls.size() >= maxRequests) break; // Max capacity.
                if (runningCallsForHost(asyncCall) >= maxRequestsPerHost) continue; // Host max capacity.

                i.remove();
                executableCalls.add(asyncCall);
                runningAsyncCalls.add(asyncCall);
            }
            isRunning = runningCallsCount() > 0;
        }

        for (int i = 0, size = executableCalls.size(); i < size; i++) {
            AsyncCall asyncCall = executableCalls.get(i);
            asyncCall.executeOn(executorService());
        }

        return isRunning;
    }

    /**
     * @param call 回调信息
     * @return 与{@code call}共享主机的运行调用的数量
     */
    private int runningCallsForHost(AsyncCall call) {
        int result = 0;
        for (AsyncCall c : runningAsyncCalls) {
            if (c.get().forWebSocket) continue;
            if (c.host().equals(call.host())) result++;
        }
        return result;
    }

    public synchronized void executed(RealCall call) {
        runningSyncCalls.add(call);
    }

    public void finished(AsyncCall call) {
        finished(runningAsyncCalls, call);
    }

    public void finished(RealCall call) {
        finished(runningSyncCalls, call);
    }

    private <T> void finished(Deque<T> calls, T call) {
        Runnable idleCallback;
        synchronized (this) {
            if (!calls.remove(call)) throw new AssertionError("Call wasn't in-flight!");
            idleCallback = this.idleCallback;
        }

        boolean isRunning = promoteAndExecute();

        if (!isRunning && null != idleCallback) {
            idleCallback.run();
        }
    }

    public synchronized List<NewCall> queuedCalls() {
        List<NewCall> result = new ArrayList<>();
        for (AsyncCall asyncCall : readyAsyncCalls) {
            result.add(asyncCall.get());
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized List<NewCall> runningCalls() {
        List<NewCall> result = new ArrayList<>();
        result.addAll(runningSyncCalls);
        for (AsyncCall asyncCall : runningAsyncCalls) {
            result.add(asyncCall.get());
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized int queuedCallsCount() {
        return readyAsyncCalls.size();
    }

    public synchronized int runningCallsCount() {
        return runningAsyncCalls.size() + runningSyncCalls.size();
    }

}
