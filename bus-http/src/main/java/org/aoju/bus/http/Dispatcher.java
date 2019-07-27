package org.aoju.bus.http;

import org.aoju.bus.http.internal.Internal;

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
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
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

    /**
     * Set the maximum number of requests to execute concurrently. Above this requests queue in
     * memory, waiting for the running calls to complete.
     *
     * <p>If more than {@code maxRequests} requests are in flight when this is invoked, those requests
     * will remain in flight.
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
     * Set the maximum number of requests for each host to execute concurrently. This limits requests
     * by the URL's host name. Note that concurrent requests to a single IP address may still exceed
     * this limit: multiple hostnames may share an IP address or be routed through the same HTTP
     * proxy.
     *
     * <p>If more than {@code maxRequestsPerHost} requests are in flight when this is invoked, those
     * requests will remain in flight.
     *
     * <p>WebSocket connections to hosts <b>do not</b> count against this limit.
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
     * Set a callback to be invoked each time the dispatcher becomes idle (when the number of running
     * calls returns to zero).
     *
     * <p>Note: The time at which a {@linkplain Call call} is considered idle is different depending
     * on whether it was run {@linkplain Call#enqueue(Callback) asynchronously} or
     * {@linkplain Call#execute() synchronously}. Asynchronous calls become idle after the
     * {@link Callback#onResponse onResponse} or {@link Callback#onFailure onFailure} callback has
     * returned. Synchronous calls become idle once {@link Call#execute() execute()} returns. This
     * means that if you are doing synchronous calls the network layer will not truly be idle until
     * every returned {@link Response} has been closed.
     */
    public synchronized void setIdleCallback(Runnable idleCallback) {
        this.idleCallback = idleCallback;
    }

    void enqueue(RealCall.AsyncCall call) {
        synchronized (this) {
            readyAsyncCalls.add(call);
        }
        promoteAndExecute();
    }

    /**
     * Cancel all calls currently enqueued or executing. Includes calls executed both {@linkplain
     * Call#execute() synchronously} and {@linkplain Call#enqueue asynchronously}.
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
     * Promotes eligible calls from {@link #readyAsyncCalls} to {@link #runningAsyncCalls} and runs
     * them on the executor service. Must not be called with synchronization because executing calls
     * can call into user code.
     *
     * @return true if the dispatcher is currently running calls.
     */
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

    /**
     * Returns the number of running calls that share a host with {@code call}.
     */
    private int runningCallsForHost(RealCall.AsyncCall call) {
        int result = 0;
        for (RealCall.AsyncCall c : runningAsyncCalls) {
            if (c.get().forWebSocket) continue;
            if (c.host().equals(call.host())) result++;
        }
        return result;
    }

    /**
     * Used by {@code Call#execute} to signal it is in-flight.
     */
    synchronized void executed(RealCall call) {
        runningSyncCalls.add(call);
    }

    /**
     * Used by {@code AsyncCall#run} to signal completion.
     */
    void finished(RealCall.AsyncCall call) {
        finished(runningAsyncCalls, call);
    }

    /**
     * Used by {@code Call#execute} to signal completion.
     */
    void finished(RealCall call) {
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

    /**
     * Returns a snapshot of the calls currently awaiting execution.
     */
    public synchronized List<Call> queuedCalls() {
        List<Call> result = new ArrayList<>();
        for (RealCall.AsyncCall asyncCall : readyAsyncCalls) {
            result.add(asyncCall.get());
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns a snapshot of the calls currently being executed.
     */
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
