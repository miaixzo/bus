/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
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
package org.aoju.bus.http.metric.http;

import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.lang.exception.RelevantException;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.http.*;
import org.aoju.bus.http.accord.RouteException;
import org.aoju.bus.http.accord.StreamAllocation;
import org.aoju.bus.http.bodys.RequestBody;
import org.aoju.bus.http.bodys.UnrepeatableBody;
import org.aoju.bus.http.metric.EventListener;
import org.aoju.bus.http.metric.Interceptor;
import org.aoju.bus.http.secure.CertificatePinner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpRetryException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;

/**
 * 该拦截器从失败中恢复，并根据需要进行重定向
 * 如果调用被取消，它可能会抛出{@link IOException}
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8+
 */
public final class RetryAndFollowUp implements Interceptor {

    /**
     * 我们应该尝试多少次重定向和验证挑战?Chrome遵循21重定向;
     * Firefox、curl和wget遵循20;Safari是16;HTTP/1.0建议5
     */
    private static final int MAX_FOLLOW_UPS = 20;

    private final Httpd client;
    private final boolean forWebSocket;
    private volatile StreamAllocation streamAllocation;
    private Object callStackTrace;
    private volatile boolean canceled;

    public RetryAndFollowUp(Httpd client, boolean forWebSocket) {
        this.client = client;
        this.forWebSocket = forWebSocket;
    }

    /**
     * 如果当前持有套接字连接，则立即关闭它。使用它来中断来自任何线程的正在运行的请求
     * 关闭请求体和响应体流是调用方的职责;否则，资源可能会泄露
     * 此方法可以安全地并发调用，但提供了有限的保证。如果已建立传输层连接(如HTTP/2流)，
     * 则终止该连接。否则，如果正在建立套接字连接，则终止该连接.
     */
    public void cancel() {
        canceled = true;
        StreamAllocation streamAllocation = this.streamAllocation;
        if (streamAllocation != null) streamAllocation.cancel();
    }

    public boolean isCanceled() {
        return canceled;
    }

    public void setCallStackTrace(Object callStackTrace) {
        this.callStackTrace = callStackTrace;
    }

    public StreamAllocation streamAllocation() {
        return streamAllocation;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        NewCall call = realChain.call();
        EventListener eventListener = realChain.eventListener();

        StreamAllocation streamAllocation = new StreamAllocation(client.connectionPool(),
                createAddress(request.url()), call, eventListener, callStackTrace);
        this.streamAllocation = streamAllocation;

        int followUpCount = 0;
        Response priorResponse = null;
        while (true) {
            if (canceled) {
                streamAllocation.release();
                throw new IOException("Canceled");
            }

            Response response;
            boolean releaseConnection = true;
            try {
                response = realChain.proceed(request, streamAllocation, null, null);
                releaseConnection = false;
            } catch (RouteException e) {
                // 图通过路由连接失败。请求将不会被发送.
                if (!recover(e.getLastConnectException(), streamAllocation, false, request)) {
                    throw e.getFirstConnectException();
                }
                releaseConnection = false;
                continue;
            } catch (IOException e) {
                // 试图与服务器通信失败。请求可能已经发送.
                boolean requestSendStarted = !(e instanceof RelevantException);
                if (!recover(e, streamAllocation, requestSendStarted, request)) throw e;
                releaseConnection = false;
                continue;
            } finally {
                // 我们抛出了一个未检查的异常。释放任何资源.
                if (releaseConnection) {
                    streamAllocation.streamFailed(null);
                    streamAllocation.release();
                }
            }

            // 如果先前的响应存在，则附加它。这样的反应永远不会有body.
            if (priorResponse != null) {
                response = response.newBuilder()
                        .priorResponse(priorResponse.newBuilder()
                                .body(null)
                                .build())
                        .build();
            }

            Request followUp;
            try {
                followUp = followUpRequest(response, streamAllocation.route());
            } catch (IOException e) {
                streamAllocation.release();
                throw e;
            }

            if (followUp == null) {
                streamAllocation.release();
                return response;
            }

            IoUtils.close(response.body());

            if (++followUpCount > MAX_FOLLOW_UPS) {
                streamAllocation.release();
                throw new ProtocolException("Too many follow-up requests: " + followUpCount);
            }

            if (followUp.body() instanceof UnrepeatableBody) {
                streamAllocation.release();
                throw new HttpRetryException("Cannot retry streamed HTTP body", response.code());
            }

            if (!sameConnection(response, followUp.url())) {
                streamAllocation.release();
                streamAllocation = new StreamAllocation(client.connectionPool(),
                        createAddress(followUp.url()), call, eventListener, callStackTrace);
                this.streamAllocation = streamAllocation;
            } else if (streamAllocation.codec() != null) {
                throw new IllegalStateException("Closing the body of " + response
                        + " didn't close its backing stream. Bad interceptor?");
            }

            request = followUp;
            priorResponse = response;
        }
    }

    private Address createAddress(UnoUrl url) {
        SSLSocketFactory sslSocketFactory = null;
        HostnameVerifier hostnameVerifier = null;
        CertificatePinner certificatePinner = null;
        if (url.isHttps()) {
            sslSocketFactory = client.sslSocketFactory();
            hostnameVerifier = client.hostnameVerifier();
            certificatePinner = client.certificatePinner();
        }

        return new Address(url.host(), url.port(), client.dns(), client.socketFactory(),
                sslSocketFactory, hostnameVerifier, certificatePinner, client.proxyAuthenticator(),
                client.proxy(), client.protocols(), client.connectionSpecs(), client.proxySelector());
    }

    private boolean recover(IOException e, StreamAllocation streamAllocation,
                            boolean requestSendStarted, Request userRequest) {
        streamAllocation.streamFailed(e);

        // 应用层禁止重试.
        if (!client.retryOnConnectionFailure()) return false;

        // 我们不能再发送请求体了
        if (requestSendStarted && requestIsUnrepeatable(e, userRequest)) return false;

        // 这个异常是致命的
        if (!isRecoverable(e, requestSendStarted)) return false;

        // 没有更多的路线可以尝试
        if (!streamAllocation.hasMoreRoutes()) return false;

        // 对于故障恢复，使用与新连接相同的路由选择器
        return true;
    }

    private boolean requestIsUnrepeatable(IOException e, Request userRequest) {
        return userRequest.body() instanceof UnrepeatableBody
                || e instanceof FileNotFoundException;
    }

    private boolean isRecoverable(IOException e, boolean requestSendStarted) {
        // 如果有协议问题，不要恢复
        if (e instanceof ProtocolException) {
            return false;
        }

        // 如果有一个中断不恢复，但如果有一个超时连接到一个路由，我们应该尝试下一个路由(如果有一个).
        if (e instanceof InterruptedIOException) {
            return e instanceof SocketTimeoutException && !requestSendStarted;
        }

        // 查找已知的客户端或协商错误，这些错误不太可能通过再次尝试使用不同的路由来修复.
        if (e instanceof SSLHandshakeException) {
            // 如果问题是来自X509TrustManager的一个证书异常，那么不要重试.
            if (e.getCause() instanceof CertificateException) {
                return false;
            }
        }
        if (e instanceof SSLPeerUnverifiedException) {
            // 例如，证书固定错误.
            return false;
        }

        return true;
    }

    private Request followUpRequest(Response userResponse, Route route) throws IOException {
        if (userResponse == null) throw new IllegalStateException();
        int responseCode = userResponse.code();

        final String method = userResponse.request().method();
        switch (responseCode) {
            case Http.HTTP_PROXY_AUTH:
                Proxy selectedProxy = route.proxy();
                if (selectedProxy.type() != Proxy.Type.HTTP) {
                    throw new ProtocolException("Received HTTP_PROXY_AUTH (407) code while not using proxy");
                }
                return client.proxyAuthenticator().authenticate(route, userResponse);

            case Http.HTTP_UNAUTHORIZED:
                return client.authenticator().authenticate(route, userResponse);

            case Http.HTTP_PERM_REDIRECT:
            case Http.HTTP_TEMP_REDIRECT:
                if (!method.equals("GET") && !method.equals("HEAD")) {
                    return null;
                }
            case Http.HTTP_MULT_CHOICE:
            case Http.HTTP_MOVED_PERM:
            case Http.HTTP_MOVED_TEMP:
            case Http.HTTP_SEE_OTHER:
                if (!client.followRedirects()) return null;

                String location = userResponse.header("Location");
                if (location == null) return null;
                UnoUrl url = userResponse.request().url().resolve(location);

                if (url == null) return null;

                boolean sameScheme = url.scheme().equals(userResponse.request().url().scheme());
                if (!sameScheme && !client.followSslRedirects()) return null;

                Request.Builder requestBuilder = userResponse.request().newBuilder();
                if (HttpMethod.permitsRequestBody(method)) {
                    final boolean maintainBody = HttpMethod.redirectsWithBody(method);
                    if (HttpMethod.redirectsToGet(method)) {
                        requestBuilder.method("GET", null);
                    } else {
                        RequestBody requestBody = maintainBody ? userResponse.request().body() : null;
                        requestBuilder.method(method, requestBody);
                    }
                    if (!maintainBody) {
                        requestBuilder.removeHeader("Transfer-Encoding");
                        requestBuilder.removeHeader("Content-Length");
                        requestBuilder.removeHeader("Content-Type");
                    }
                }

                if (!sameConnection(userResponse, url)) {
                    requestBuilder.removeHeader("Authorization");
                }

                return requestBuilder.url(url).build();

            case Http.HTTP_CLIENT_TIMEOUT:
                if (!client.retryOnConnectionFailure()) {
                    return null;
                }

                if (userResponse.request().body() instanceof UnrepeatableBody) {
                    return null;
                }

                if (userResponse.priorResponse() != null
                        && userResponse.priorResponse().code() == Http.HTTP_CLIENT_TIMEOUT) {
                    return null;
                }

                if (retryAfter(userResponse, 0) > 0) {
                    return null;
                }

                return userResponse.request();

            case Http.HTTP_UNAVAILABLE:
                if (userResponse.priorResponse() != null
                        && userResponse.priorResponse().code() == Http.HTTP_UNAVAILABLE) {
                    return null;
                }

                if (retryAfter(userResponse, Integer.MAX_VALUE) == 0) {
                    return userResponse.request();
                }

                return null;

            default:
                return null;
        }
    }

    private int retryAfter(Response userResponse, int defaultDelay) {
        String header = userResponse.header("Retry-After");

        if (header == null) {
            return defaultDelay;
        }

        if (header.matches("\\d+")) {
            return Integer.valueOf(header);
        }

        return Integer.MAX_VALUE;
    }

    private boolean sameConnection(Response response, UnoUrl followUp) {
        UnoUrl url = response.request().url();
        return url.host().equals(followUp.host())
                && url.port() == followUp.port()
                && url.scheme().equals(followUp.scheme());
    }

}
