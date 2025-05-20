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
package org.miaixz.bus.http;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.accord.ConnectInterceptor;
import org.miaixz.bus.http.accord.Transmitter;
import org.miaixz.bus.http.cache.CacheInterceptor;
import org.miaixz.bus.http.metric.Interceptor;
import org.miaixz.bus.http.metric.NamedRunnable;
import org.miaixz.bus.http.metric.NewChain;
import org.miaixz.bus.http.metric.http.BridgeInterceptor;
import org.miaixz.bus.http.metric.http.CallServerInterceptor;
import org.miaixz.bus.http.metric.http.RealInterceptorChain;
import org.miaixz.bus.http.metric.http.RetryAndFollowUp;
import org.miaixz.bus.logger.Logger;

/**
 * HTTP 请求的实际执行者
 * <p>
 * 负责执行同步和异步 HTTP 请求，通过拦截器链处理请求和响应。 支持 WebSocket 连接、请求取消、超时管理和重试机制。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RealCall implements NewCall {

    /**
     * HTTP 客户端
     */
    public final Httpd client;
    /**
     * 原始请求
     */
    public final Request originalRequest;
    /**
     * 是否为 WebSocket 请求
     */
    public final boolean forWebSocket;
    /**
     * 请求传输器
     */
    public Transmitter transmitter;
    /**
     * 是否已执行
     */
    public boolean executed;

    /**
     * 构造函数，初始化 RealCall 实例
     *
     * @param client          HTTP 客户端
     * @param originalRequest 原始请求
     * @param forWebSocket    是否为 WebSocket 请求
     */
    private RealCall(Httpd client, Request originalRequest, boolean forWebSocket) {
        this.client = client;
        this.originalRequest = originalRequest;
        this.forWebSocket = forWebSocket;
    }

    /**
     * 创建 RealCall 实例
     *
     * @param client          HTTP 客户端
     * @param originalRequest 原始请求
     * @param forWebSocket    是否为 WebSocket 请求
     * @return RealCall 实例
     */
    static RealCall newRealCall(Httpd client, Request originalRequest, boolean forWebSocket) {
        RealCall call = new RealCall(client, originalRequest, forWebSocket);
        call.transmitter = new Transmitter(client, call);
        return call;
    }

    /**
     * 获取原始请求
     *
     * @return 原始请求
     */
    @Override
    public Request request() {
        return originalRequest;
    }

    /**
     * 同步执行请求
     *
     * @return 响应
     * @throws IOException           如果执行失败
     * @throws IllegalStateException 如果请求已执行
     */
    @Override
    public Response execute() throws IOException {
        synchronized (this) {
            if (executed)
                throw new IllegalStateException("Already Executed");
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

    /**
     * 异步执行请求
     *
     * @param responseCallback 响应回调
     * @throws IllegalStateException 如果请求已执行
     */
    @Override
    public void enqueue(Callback responseCallback) {
        synchronized (this) {
            if (executed)
                throw new IllegalStateException("Already Executed");
            executed = true;
        }
        transmitter.callStart();
        client.dispatcher().enqueue(new AsyncCall(responseCallback));
    }

    /**
     * 取消请求
     */
    @Override
    public void cancel() {
        transmitter.cancel();
    }

    /**
     * 获取超时配置
     *
     * @return 超时配置
     */
    @Override
    public Timeout timeout() {
        return transmitter.timeout();
    }

    /**
     * 检查是否已执行
     *
     * @return true 如果已执行
     */
    @Override
    public synchronized boolean isExecuted() {
        return executed;
    }

    /**
     * 检查是否已取消
     *
     * @return true 如果已取消
     */
    @Override
    public boolean isCanceled() {
        return transmitter.isCanceled();
    }

    /**
     * 克隆 RealCall 实例
     *
     * @return 新的 RealCall 实例
     */
    @Override
    public RealCall clone() {
        return RealCall.newRealCall(client, originalRequest, forWebSocket);
    }

    /**
     * 获取可记录的字符串表示
     *
     * @return 描述调用的字符串（不包含完整 URL）
     */
    public String toLoggableString() {
        return (isCanceled() ? "canceled " : Normal.EMPTY) + (forWebSocket ? "web socket" : "call") + " to "
                + redactedUrl();
    }

    /**
     * 获取隐藏敏感信息的 URL
     *
     * @return 隐藏敏感信息的 URL
     */
    public String redactedUrl() {
        return originalRequest.url().redact();
    }

    /**
     * 通过拦截器链获取响应
     *
     * @return 响应
     * @throws IOException 如果执行失败
     */
    public Response getResponseWithInterceptorChain() throws IOException {
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

        NewChain chain = new RealInterceptorChain(interceptors, transmitter, null, 0, originalRequest, this,
                client.connectTimeoutMillis(), client.readTimeoutMillis(), client.writeTimeoutMillis());

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

    /**
     * 异步调用类
     */
    public class AsyncCall extends NamedRunnable {

        /**
         * 响应回调
         */
        private final Callback responseCallback;
        /**
         * 主机调用计数
         */
        private volatile AtomicInteger callsPerHost = new AtomicInteger(0);

        /**
         * 构造函数，初始化异步调用
         *
         * @param responseCallback 响应回调
         */
        AsyncCall(Callback responseCallback) {
            super("Http %s", redactedUrl());
            this.responseCallback = responseCallback;
        }

        /**
         * 获取主机调用计数
         *
         * @return 主机调用计数
         */
        public AtomicInteger callsPerHost() {
            return callsPerHost;
        }

        /**
         * 重用其他异步调用的主机计数
         *
         * @param other 其他异步调用
         */
        public void reuseCallsPerHostFrom(AsyncCall other) {
            this.callsPerHost = other.callsPerHost;
        }

        /**
         * 获取主机名
         *
         * @return 主机名
         */
        public String host() {
            return originalRequest.url().host();
        }

        /**
         * 获取请求
         *
         * @return 请求
         */
        Request request() {
            return originalRequest;
        }

        /**
         * 获取 RealCall 实例
         *
         * @return RealCall 实例
         */
        public RealCall get() {
            return RealCall.this;
        }

        /**
         * 在执行器服务上执行异步调用
         *
         * @param executorService 执行器服务
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
                    client.dispatcher().finished(RealCall.this);
                }
            }
        }

        /**
         * 执行异步调用
         */
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