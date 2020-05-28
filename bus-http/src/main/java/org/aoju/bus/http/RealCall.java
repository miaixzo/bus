/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.http;

import org.aoju.bus.core.io.AsyncTimeout;
import org.aoju.bus.core.io.Timeout;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.http.accord.ConnectInterceptor;
import org.aoju.bus.http.accord.StreamAllocation;
import org.aoju.bus.http.accord.platform.Platform;
import org.aoju.bus.http.cache.CacheInterceptor;
import org.aoju.bus.http.metric.EventListener;
import org.aoju.bus.http.metric.Interceptor;
import org.aoju.bus.http.metric.NamedRunnable;
import org.aoju.bus.http.metric.http.BridgeInterceptor;
import org.aoju.bus.http.metric.http.CallServerInterceptor;
import org.aoju.bus.http.metric.http.RealInterceptorChain;
import org.aoju.bus.http.metric.http.RetryAndFollowUp;
import org.aoju.bus.logger.Logger;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 实际调用准备执行的请求
 *
 * @author Kimi Liu
 * @version 5.9.5
 * @since JDK 1.8+
 */
public final class RealCall implements NewCall {

    /**
     * 应用程序的原始请求未掺杂重定向或验证标头.
     */
    public final Request originalRequest;
    public final boolean forWebSocket;
    final Httpd client;
    final RetryAndFollowUp retryAndFollowUp;
    final AsyncTimeout timeout;
    /**
     * 在{@link NewCall}和{@link EventListener}之间存在一个循环，这使得
     * 这种情况很尴尬。这将在我们创建call实例之后设置，然后创建事件监听器实例
     */
    private EventListener eventListener;
    private boolean executed;

    private RealCall(Httpd client, Request originalRequest, boolean forWebSocket) {
        this.client = client;
        this.originalRequest = originalRequest;
        this.forWebSocket = forWebSocket;
        this.retryAndFollowUp = new RetryAndFollowUp(client, forWebSocket);
        this.timeout = new AsyncTimeout() {
            @Override
            protected void timedOut() {
                cancel();
            }
        };
        this.timeout.timeout(client.callTimeoutMillis(), TimeUnit.MILLISECONDS);
    }

    static RealCall newRealCall(Httpd client, Request originalRequest, boolean forWebSocket) {
        RealCall call = new RealCall(client, originalRequest, forWebSocket);
        call.eventListener = client.eventListenerFactory().create(call);
        return call;
    }

    @Override
    public Request request() {
        return originalRequest;
    }

    @Override
    public Response execute() throws IOException {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        captureCallStackTrace();
        timeout.enter();
        eventListener.callStart(this);
        try {
            client.dispatcher().executed(this);
            Response result = getResponseWithInterceptorChain();
            if (result == null) throw new IOException("Canceled");
            return result;
        } catch (IOException e) {
            e = timeoutExit(e);
            eventListener.callFailed(this, e);
            throw e;
        } finally {
            client.dispatcher().finished(this);
        }
    }

    IOException timeoutExit(IOException cause) {
        if (!timeout.exit()) return cause;

        InterruptedIOException e = new InterruptedIOException("timeout");
        if (cause != null) {
            e.initCause(cause);
        }
        return e;
    }

    private void captureCallStackTrace() {
        Object callStackTrace = Platform.get().getStackTraceForCloseable("response.body().close()");
        retryAndFollowUp.setCallStackTrace(callStackTrace);
    }

    @Override
    public void enqueue(Callback responseCallback) {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        captureCallStackTrace();
        eventListener.callStart(this);
        client.dispatcher().enqueue(new AsyncCall(responseCallback));
    }

    @Override
    public void cancel() {
        retryAndFollowUp.cancel();
    }

    @Override
    public Timeout timeout() {
        return timeout;
    }

    @Override
    public synchronized boolean isExecuted() {
        return executed;
    }

    @Override
    public boolean isCanceled() {
        return retryAndFollowUp.isCanceled();
    }

    @Override
    public RealCall clone() {
        return RealCall.newRealCall(client, originalRequest, forWebSocket);
    }

    StreamAllocation streamAllocation() {
        return retryAndFollowUp.streamAllocation();
    }

    String toLoggableString() {
        return (isCanceled() ? "canceled " : Normal.EMPTY)
                + (forWebSocket ? "web socket" : "call")
                + " to " + redactedUrl();
    }

    String redactedUrl() {
        return originalRequest.url().redact();
    }

    Response getResponseWithInterceptorChain() throws IOException {
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.addAll(client.interceptors());
        interceptors.add(retryAndFollowUp);
        interceptors.add(new BridgeInterceptor(client.cookieJar()));
        interceptors.add(new CacheInterceptor(client.internalCache()));
        interceptors.add(new ConnectInterceptor(client));
        if (!forWebSocket) {
            interceptors.addAll(client.networkInterceptors());
        }
        interceptors.add(new CallServerInterceptor(forWebSocket));

        Interceptor.Chain chain = new RealInterceptorChain(interceptors, null, null, null, 0,
                originalRequest, this, eventListener, client.connectTimeoutMillis(),
                client.readTimeoutMillis(), client.writeTimeoutMillis());

        Response response = chain.proceed(originalRequest);
        if (retryAndFollowUp.isCanceled()) {
            IoKit.close(response);
            throw new IOException("Canceled");
        }
        return response;
    }

    public final class AsyncCall extends NamedRunnable {
        private final Callback responseCallback;

        AsyncCall(Callback responseCallback) {
            super("Httpd %s", redactedUrl());
            this.responseCallback = responseCallback;
        }

        public String host() {
            return originalRequest.url().host();
        }

        Request request() {
            return originalRequest;
        }

        public RealCall get() {
            return RealCall.this;
        }

        public void executeOn(ExecutorService executorService) {
            assert (!Thread.holdsLock(client.dispatcher()));
            boolean success = false;
            try {
                executorService.execute(this);
                success = true;
            } catch (RejectedExecutionException e) {
                InterruptedIOException ioException = new InterruptedIOException("executor rejected");
                ioException.initCause(e);
                eventListener.callFailed(RealCall.this, ioException);
                responseCallback.onFailure(RealCall.this, ioException);
            } finally {
                if (!success) {
                    client.dispatcher().finished(this);
                }
            }
        }

        @Override
        protected void execute() {
            boolean signalledCallback = false;
            timeout.enter();
            try {
                Response response = getResponseWithInterceptorChain();
                signalledCallback = true;
                responseCallback.onResponse(RealCall.this, response);
            } catch (IOException e) {
                e = timeoutExit(e);
                if (signalledCallback) {
                    Logger.info("Callback failure for " + toLoggableString(), e);
                } else {
                    eventListener.callFailed(RealCall.this, e);
                    responseCallback.onFailure(RealCall.this, e);
                }
            } catch (Throwable t) {
                cancel();
                if (!signalledCallback) {
                    IOException canceledException = new IOException("canceled due to " + t);
                    responseCallback.onFailure(RealCall.this, canceledException);
                }
                throw t;
            } finally {
                client.dispatcher().finished(this);
            }
        }
    }

}
