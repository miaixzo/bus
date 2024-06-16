/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.http;

import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.accord.ConnectInterceptor;
import org.miaixz.bus.http.accord.Transmitter;
import org.miaixz.bus.http.cache.CacheInterceptor;
import org.miaixz.bus.http.metric.Interceptor;
import org.miaixz.bus.http.metric.NamedRunnable;
import org.miaixz.bus.http.metric.http.BridgeInterceptor;
import org.miaixz.bus.http.metric.http.CallServerInterceptor;
import org.miaixz.bus.http.metric.http.RealInterceptorChain;
import org.miaixz.bus.http.metric.http.RetryAndFollowUp;
import org.miaixz.bus.logger.Logger;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 实际调用准备执行的请求
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RealCall implements NewCall {

    /**
     * 调用客户端
     */
    public final Httpd client;
    /**
     * 应用程序的原始请求未掺杂重定向或验证标头.
     */
    public final Request originalRequest;
    public final boolean forWebSocket;
    /**
     * 在{@link NewCall}和{@link Transmitter}之间有一个循环
     * 这是在创建调用实例之后立即设置的
     */
    private Transmitter transmitter;
    /**
     * 进程守护
     */
    private boolean executed;

    private RealCall(Httpd client, Request originalRequest, boolean forWebSocket) {
        this.client = client;
        this.originalRequest = originalRequest;
        this.forWebSocket = forWebSocket;
    }

    static RealCall newRealCall(Httpd client, Request originalRequest, boolean forWebSocket) {
        // 安全地将Call实例发布到EventListener
        RealCall call = new RealCall(client, originalRequest, forWebSocket);
        call.transmitter = new Transmitter(client, call);
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
        transmitter.timeoutEnter();
        transmitter.callStart();
        try {
            client.dispatcher().executed(this);
            return getResponseWithInterceptorChain();
        } finally {
            client.dispatcher().finished(this);
        }
    }

    @Override
    public void enqueue(Callback responseCallback) {
        synchronized (this) {
            if (executed) throw new IllegalStateException("Already Executed");
            executed = true;
        }
        transmitter.callStart();
        client.dispatcher().enqueue(new AsyncCall(responseCallback));
    }

    @Override
    public void cancel() {
        transmitter.cancel();
    }

    @Override
    public Timeout timeout() {
        return transmitter.timeout();
    }

    @Override
    public synchronized boolean isExecuted() {
        return executed;
    }

    @Override
    public boolean isCanceled() {
        return transmitter.isCanceled();
    }

    @Override
    public RealCall clone() {
        return RealCall.newRealCall(client, originalRequest, forWebSocket);
    }

    /**
     * 返回描述此调用的字符串
     * 不包含完整的URL，因为它可能包含敏感信息
     */
    public String toLoggableString() {
        return (isCanceled() ? "canceled " : Normal.EMPTY)
                + (forWebSocket ? "web socket" : "call")
                + " to " + redactedUrl();
    }

    public String redactedUrl() {
        return originalRequest.url().redact();
    }

    public Response getResponseWithInterceptorChain() throws IOException {
        // Build a full stack of interceptors.
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.addAll(client.interceptors());
        interceptors.add(new RetryAndFollowUp(client));
        interceptors.add(new BridgeInterceptor(client.cookieJar()));
        interceptors.add(new CacheInterceptor(client.internalCache()));
        interceptors.add(new ConnectInterceptor(client));
        if (!forWebSocket) {
            interceptors.addAll(client.networkInterceptors());
        }
        interceptors.add(new CallServerInterceptor(forWebSocket));

        Interceptor.Chain chain = new RealInterceptorChain(interceptors, transmitter, null, 0,
                originalRequest, this, client.connectTimeoutMillis(),
                client.readTimeoutMillis(), client.writeTimeoutMillis());

        boolean calledNoMoreExchanges = false;
        try {
            Response response = chain.proceed(originalRequest);
            if (transmitter.isCanceled()) {
                IoKit.close(response);
                throw new IOException("Canceled");
            }
            return response;
        } catch (IOException e) {
            calledNoMoreExchanges = true;
            throw transmitter.noMoreExchanges(e);
        } finally {
            if (!calledNoMoreExchanges) {
                transmitter.noMoreExchanges(null);
            }
        }
    }

    public class AsyncCall extends NamedRunnable {

        private final Callback responseCallback;
        private volatile AtomicInteger callsPerHost = new AtomicInteger(0);

        AsyncCall(Callback responseCallback) {
            super("Http %s", redactedUrl());
            this.responseCallback = responseCallback;
        }

        public AtomicInteger callsPerHost() {
            return callsPerHost;
        }

        public void reuseCallsPerHostFrom(AsyncCall other) {
            this.callsPerHost = other.callsPerHost;
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

        /**
         * 尝试在{@code executorService}上排队这个异步调用
         * 如果执行程序已经关闭，则将尝试通过调用失败来进行清理
         */
        public void executeOn(ExecutorService executorService) {
            assert (!Thread.holdsLock(client.dispatcher()));
            boolean success = false;
            try {
                executorService.execute(this);
                success = true;
            } catch (RejectedExecutionException e) {
                InterruptedIOException ioException = new InterruptedIOException("executor rejected");
                ioException.initCause(e);
                transmitter.noMoreExchanges(ioException);
                responseCallback.onFailure(RealCall.this, ioException);
            } finally {
                if (!success) {
                    // 这个回调不再运行
                    client.dispatcher().finished(RealCall.this);
                }
            }
        }

        @Override
        protected void execute() {
            boolean signalledCallback = false;
            transmitter.timeoutEnter();
            try {
                Response response = getResponseWithInterceptorChain();
                signalledCallback = true;
                responseCallback.onResponse(RealCall.this, response);
            } catch (IOException e) {
                if (signalledCallback) {
                    // 不要第二次发送回调信号
                    Logger.info("Callback failure for " + toLoggableString(), e);
                } else {
                    responseCallback.onFailure(RealCall.this, e);
                }
            } catch (Throwable t) {
                cancel();
                if (!signalledCallback) {
                    IOException canceledException = new IOException("canceled due to " + t);
                    canceledException.addSuppressed(t);
                    responseCallback.onFailure(RealCall.this, canceledException);
                }
                throw t;
            } finally {
                client.dispatcher().finished(RealCall.this);
            }
        }
    }

}
