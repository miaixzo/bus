/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.offers;

import org.aoju.bus.http.Call;
import org.aoju.bus.http.Internal;
import org.aoju.bus.http.RealCall;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Policy on when async requests are executed.
 *
 * <p>Each dispatcher uses an {@link ExecutorService} to run calls internally. If you supply your
 * own executor, it should be able to run {@linkplain #getMaxRequests the configured maximum} number
 * of calls concurrently.
 *
 * @author Kimi Liu
 * @version 5.1.0
 * @since JDK 1.8+
 */
public final class Dispatcher {

    /**
     * Ready async calls in the order they'll be run.
     */
    private final Deque<RealCall.AsyncCall> readyAsyncCalls = new ArrayDeque<>();
    /**
     * Running asynchronous calls. Includes canceled calls that haven't finished yet.
     */
    private final Deque<RealCall.AsyncCall> runningAsyncCalls = new ArrayDeque<>();
    /**
     * Running synchronous calls. Includes canceled calls that haven't finished yet.
     */
    private final Deque<RealCall> runningSyncCalls = new ArrayDeque<>();
    private int maxRequests = 64;
    private int maxRequestsPerHost = 5;
    private Runnable idleCallback;
    /**
     * Executes calls. Created lazily.
     */
    private ExecutorService executorService;

    public Dispatcher(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public Dispatcher() {
    }

    public synchronized ExecutorService executorService() {
        if (executorService == null) {
            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), Internal.threadFactory("HttpClient Dispatcher", false));
        }
        return executorService;
    }

    public synchronized int getMaxRequests() {
        return maxRequests;
    }

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

    public void setMaxRequestsPerHost(int maxRequestsPerHost) {
        if (maxRequestsPerHost < 1) {
            throw new IllegalArgumentException("max < 1: " + maxRequestsPerHost);
        }
        synchronized (this) {
            this.maxRequestsPerHost = maxRequestsPerHost;
        }
        promoteAndExecute();
    }

    public synchronized void setIdleCallback(Runnable idleCallback) {
        this.idleCallback = idleCallback;
    }

    public void enqueue(RealCall.AsyncCall call) {
        synchronized (this) {
            readyAsyncCalls.add(call);
        }
        promoteAndExecute();
    }

    public synchronized void cancelAll() {
        for (RealCall.AsyncCall call : readyAsyncCalls) {
            call.get().cancel();
        }

        for (RealCall.AsyncCall call : runningAsyncCalls) {
            call.get().cancel();
        }

        for (RealCall call : runningSyncCalls) {
            call.cancel();
        }
    }

    private boolean promoteAndExecute() {
        assert (!Thread.holdsLock(this));

        List<RealCall.AsyncCall> executableCalls = new ArrayList<>();
        boolean isRunning;
        synchronized (this) {
            for (Iterator<RealCall.AsyncCall> i = readyAsyncCalls.iterator(); i.hasNext(); ) {
                RealCall.AsyncCall asyncCall = i.next();

                if (runningAsyncCalls.size() >= maxRequests) break; // Max capacity.
                if (runningCallsForHost(asyncCall) >= maxRequestsPerHost) continue; // Host max capacity.

                i.remove();
                executableCalls.add(asyncCall);
                runningAsyncCalls.add(asyncCall);
            }
            isRunning = runningCallsCount() > 0;
        }

        for (int i = 0, size = executableCalls.size(); i < size; i++) {
            RealCall.AsyncCall asyncCall = executableCalls.get(i);
            asyncCall.executeOn(executorService());
        }

        return isRunning;
    }

    private int runningCallsForHost(RealCall.AsyncCall call) {
        int result = 0;
        for (RealCall.AsyncCall c : runningAsyncCalls) {
            if (c.get().forWebSocket) continue;
            if (c.host().equals(call.host())) result++;
        }
        return result;
    }

    public synchronized void executed(RealCall call) {
        runningSyncCalls.add(call);
    }

    public void finished(RealCall.AsyncCall call) {
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

        if (!isRunning && idleCallback != null) {
            idleCallback.run();
        }
    }

    public synchronized List<Call> queuedCalls() {
        List<Call> result = new ArrayList<>();
        for (RealCall.AsyncCall asyncCall : readyAsyncCalls) {
            result.add(asyncCall.get());
        }
        return Collections.unmodifiableList(result);
    }

    public synchronized List<Call> runningCalls() {
        List<Call> result = new ArrayList<>();
        result.addAll(runningSyncCalls);
        for (RealCall.AsyncCall asyncCall : runningAsyncCalls) {
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
