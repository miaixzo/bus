/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.http.metric;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.http.Builder;
import org.miaixz.bus.http.NewCall;
import org.miaixz.bus.http.RealCall;

/**
 * Policy on when async requests are executed.
 *
 * <p>
 * Each dispatcher uses an {@link ExecutorService} to run calls internally. If you supply your own executor, it should
 * be able to run {@linkplain #getMaxRequests the configured maximum} calls concurrently.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Dispatcher {

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
    /**
     * Maximum number of simultaneous requests.
     */
    private int maxRequests = Normal._64;
    /**
     * Maximum number of simultaneous requests per host.
     */
    private int maxRequestsPerHost = 5;
    /**
     * A callback to be invoked when the dispatcher is idle.
     */
    private Runnable idleCallback;
    /**
     * The executor service that runs calls.
     */
    private ExecutorService executorService;

    /**
     * Constructor that takes an ExecutorService.
     *
     * @param executorService The executor service to use for running calls.
     */
    public Dispatcher(ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Default constructor.
     */
    public Dispatcher() {
    }

    /**
     * Returns the executor service that runs calls. Creates one if none exists.
     *
     * @return The executor service.
     */
    public synchronized ExecutorService executorService() {
        if (null == executorService) {
            executorService = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new SynchronousQueue<>(), Builder.threadFactory("Http Dispatcher", false));
        }
        return executorService;
    }

    /**
     * Returns the maximum number of simultaneous requests.
     *
     * @return The maximum number of requests.
     */
    public synchronized int getMaxRequests() {
        return maxRequests;
    }

    /**
     * Set the maximum number of requests to execute concurrently. Above this requests stay in memory, waiting for
     * naming calls to finish. If more than {@code maxRequests} requests are in flight when this is called, they'll
     * remain in flight.
     *
     * @param maxRequests The maximum number of requests.
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

    /**
     * Returns the maximum number of simultaneous requests per host.
     *
     * @return The maximum number of requests per host.
     */
    public synchronized int getMaxRequestsPerHost() {
        return maxRequestsPerHost;
    }

    /**
     * Set the maximum number of requests for each host to execute concurrently. This limits requests by the URL's host
     * name. Note that concurrent requests to a single IP address may still exceed this limit: multiple hostnames may
     * share an IP address or be routed through the same HTTP proxy. If more than {@code maxRequestsPerHost} requests
     * are in flight when this is called, they'll remain in flight.
     *
     * @param maxRequestsPerHost The maximum number of requests per host.
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
     * Set a callback to be invoked each time the dispatcher becomes idle (when the number of running calls returns to
     * zero).
     *
     * @param idleCallback The callback.
     */
    public synchronized void setIdleCallback(Runnable idleCallback) {
        this.idleCallback = idleCallback;
    }

    /**
     * Schedules {@code call} to be executed at some point in the future.
     *
     * @param call The asynchronous call to enqueue.
     */
    public void enqueue(RealCall.AsyncCall call) {
        synchronized (this) {
            readyAsyncCalls.add(call);
            if (!call.get().forWebSocket) {
                RealCall.AsyncCall existingCall = findExistingCallWithHost(call.host());
                if (existingCall != null)
                    call.reuseCallsPerHostFrom(existingCall);
            }
        }
        promoteAndExecute();
    }

    /**
     * Finds an existing call (either running or ready) with the same host.
     *
     * @param host The host to search for.
     * @return An existing async call, or null if none is found.
     */
    public RealCall.AsyncCall findExistingCallWithHost(String host) {
        for (RealCall.AsyncCall existingCall : runningAsyncCalls) {
            if (existingCall.host().equals(host))
                return existingCall;
        }
        for (RealCall.AsyncCall existingCall : readyAsyncCalls) {
            if (existingCall.host().equals(host))
                return existingCall;
        }
        return null;
    }

    /**
     * Cancel all queued and running calls. Includes calls executed synchronously {@linkplain NewCall#execute()} and
     * asynchronously {@linkplain NewCall#enqueue}.
     */
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

    /**
     * Promotes eligible calls from {@link #readyAsyncCalls} to {@link #runningAsyncCalls} and runs them on the executor
     * service. Must not be called with synchronization because executing calls can call into user code.
     *
     * @return true if the dispatcher is currently running calls.
     */
    public boolean promoteAndExecute() {
        assert (!Thread.holdsLock(this));

        List<RealCall.AsyncCall> executableCalls = new ArrayList<>();
        boolean isRunning;
        synchronized (this) {
            for (Iterator<RealCall.AsyncCall> i = readyAsyncCalls.iterator(); i.hasNext();) {
                RealCall.AsyncCall asyncCall = i.next();

                if (runningAsyncCalls.size() >= maxRequests)
                    break; // Max capacity.
                if (asyncCall.callsPerHost().get() >= maxRequestsPerHost)
                    continue; // Host max capacity.

                i.remove();
                asyncCall.callsPerHost().incrementAndGet();
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

    /**
     * Used by {@code Call#execute} to signal it is in-flight.
     *
     * @param call the call that has been executed.
     */
    public synchronized void executed(RealCall call) {
        runningSyncCalls.add(call);
    }

    /**
     * Used by {@code AsyncCall#run} to signal completion.
     *
     * @param call the async call that has finished.
     */
    public void finished(RealCall.AsyncCall call) {
        call.callsPerHost().decrementAndGet();
        finished(runningAsyncCalls, call);
    }

    /**
     * Used by {@code Call#execute} to signal completion.
     *
     * @param call the call that has finished.
     */
    public void finished(RealCall call) {
        finished(runningSyncCalls, call);
    }

    /**
     * A general-purpose function to finish a call, either synchronous or asynchronous.
     *
     * @param calls The deque of calls to remove from.
     * @param call  The call to remove.
     * @param <T>   The type of the call.
     */
    public <T> void finished(Deque<T> calls, T call) {
        Runnable idleCallback;
        synchronized (this) {
            if (!calls.remove(call)) {
                throw new AssertionError("Call wasn't in-flight!");
            }
            idleCallback = this.idleCallback;
        }

        boolean isRunning = promoteAndExecute();

        if (!isRunning && null != idleCallback) {
            idleCallback.run();
        }
    }

    /**
     * Returns a snapshot of the calls currently awaiting execution.
     *
     * @return A list of queued calls.
     */
    public synchronized List<NewCall> queuedCalls() {
        List<NewCall> result = new ArrayList<>();
        for (RealCall.AsyncCall asyncCall : readyAsyncCalls) {
            result.add(asyncCall.get());
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns a snapshot of the calls currently being executed.
     *
     * @return A list of running calls.
     */
    public synchronized List<NewCall> runningCalls() {
        List<NewCall> result = new ArrayList<>();
        result.addAll(runningSyncCalls);
        for (RealCall.AsyncCall asyncCall : runningAsyncCalls) {
            result.add(asyncCall.get());
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns the number of calls that are waiting to be executed.
     *
     * @return The count of queued calls.
     */
    public synchronized int queuedCallsCount() {
        return readyAsyncCalls.size();
    }

    /**
     * Returns the number of calls that are currently being executed.
     *
     * @return The count of running calls.
     */
    public synchronized int runningCallsCount() {
        return runningAsyncCalls.size() + runningSyncCalls.size();
    }

}
