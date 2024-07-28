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
package org.miaixz.bus.http.metric.http;

import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.*;
import org.miaixz.bus.http.accord.Exchange;
import org.miaixz.bus.http.accord.RouteException;
import org.miaixz.bus.http.accord.Transmitter;
import org.miaixz.bus.http.bodys.RequestBody;
import org.miaixz.bus.http.metric.Interceptor;
import org.miaixz.bus.http.metric.Internal;
import org.miaixz.bus.http.metric.NewChain;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;

/**
 * 该拦截器从失败中恢复，并根据需要进行重定向 如果调用被取消，它可能会抛出{@link IOException}
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RetryAndFollowUp implements Interceptor {

    /**
     * 我们应该尝试多少次重定向和验证挑战?Chrome遵循21重定向; Firefox、curl和wget遵循20;Safari是16;HTTP/1.0建议5
     */
    private static final int MAX_FOLLOW_UPS = 20;

    private final Httpd httpd;

    public RetryAndFollowUp(Httpd httpd) {
        this.httpd = httpd;
    }

    @Override
    public Response intercept(NewChain chain) throws IOException {
        Request request = chain.request();
        RealInterceptorChain realChain = (RealInterceptorChain) chain;
        Transmitter transmitter = realChain.transmitter();

        int followUpCount = 0;
        Response priorResponse = null;
        while (true) {
            transmitter.prepareToConnect(request);

            if (transmitter.isCanceled()) {
                throw new IOException("Canceled");
            }

            Response response;
            boolean success = false;
            try {
                response = realChain.proceed(request, transmitter, null);
                success = true;
            } catch (RouteException e) {
                // The attempt to connect via a route failed. The request will not have been sent.
                if (!recover(e.getLastConnectException(), transmitter, false, request)) {
                    throw e.getFirstConnectException();
                }
                continue;
            } catch (IOException e) {
                // An attempt to communicate with a server failed. The request may have been sent.
                boolean requestSendStarted = !(e instanceof IOException);
                if (!recover(e, transmitter, requestSendStarted, request))
                    throw e;
                continue;
            } finally {
                // The network call threw an exception. Release any resources.
                if (!success) {
                    transmitter.exchangeDoneDueToException();
                }
            }

            // Attach the prior response if it exists. Such responses never have a body.
            if (priorResponse != null) {
                response = response.newBuilder().priorResponse(priorResponse.newBuilder().body(null).build()).build();
            }

            Exchange exchange = Internal.instance.exchange(response);
            Route route = exchange != null ? exchange.connection().route() : null;
            Request followUp = followUpRequest(response, route);

            if (followUp == null) {
                if (exchange != null && exchange.isDuplex()) {
                    transmitter.timeoutEarlyExit();
                }
                return response;
            }

            RequestBody followUpBody = followUp.body();
            if (followUpBody != null && followUpBody.isOneShot()) {
                return response;
            }

            IoKit.close(response.body());
            if (transmitter.hasExchange()) {
                exchange.detachWithViolence();
            }

            if (++followUpCount > MAX_FOLLOW_UPS) {
                throw new ProtocolException("Too many follow-up requests: " + followUpCount);
            }

            request = followUp;
            priorResponse = response;
        }
    }

    /**
     * Report and attempt to recover from a failure to communicate with a server. Returns true if {@code e} is
     * recoverable, or false if the failure is permanent. Requests with a body can only be recovered if the body is
     * buffered or if the failure occurred before the request has been sent.
     */
    private boolean recover(IOException e, Transmitter transmitter, boolean requestSendStarted, Request userRequest) {
        // 应用层禁止重试
        if (!httpd.retryOnConnectionFailure())
            return false;

        // 我们不能再发送请求体了
        if (requestSendStarted && requestIsOneShot(e, userRequest))
            return false;

        // 这个异常是致命的
        if (!isRecoverable(e, requestSendStarted))
            return false;

        /// 没有更多的路线可以尝试
        if (!transmitter.canRetry())
            return false;

        // 对于故障恢复，使用与新连接相同的路由选择器
        return true;
    }

    private boolean requestIsOneShot(IOException e, Request userRequest) {
        RequestBody requestBody = userRequest.body();
        return (requestBody != null && requestBody.isOneShot()) || e instanceof FileNotFoundException;
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

    /**
     * Figures out the HTTP request to make in response to receiving {@code userResponse}. This will either add
     * authentication headers, follow redirects or handle a client request timeout. If a follow-up is either unnecessary
     * or not applicable, this returns null.
     */
    private Request followUpRequest(Response userResponse, Route route) throws IOException {
        if (userResponse == null)
            throw new IllegalStateException();
        int responseCode = userResponse.code();

        final String method = userResponse.request().method();
        switch (responseCode) {
        case HTTP.HTTP_PROXY_AUTH:
            Proxy selectedProxy = route != null ? route.proxy() : httpd.proxy();
            if (selectedProxy.type() != Proxy.Type.HTTP) {
                throw new ProtocolException("Received HTTP_PROXY_AUTH (407) code while not using proxy");
            }
            return httpd.proxyAuthenticator().authenticate(route, userResponse);

        case HTTP.HTTP_UNAUTHORIZED:
            return httpd.authenticator().authenticate(route, userResponse);

        case HTTP.HTTP_PERM_REDIRECT:
        case HTTP.HTTP_TEMP_REDIRECT:
            // "If the 307 or 308 status code is received in response to a request other than GET
            // or HEAD, the user agent MUST NOT automatically redirect the request"
            if (!method.equals("GET") && !method.equals("HEAD")) {
                return null;
            }
            // fall-through
        case HTTP.HTTP_MULT_CHOICE:
        case HTTP.HTTP_MOVED_PERM:
        case HTTP.HTTP_MOVED_TEMP:
        case HTTP.HTTP_SEE_OTHER:
            // Does the client allow redirects?
            if (!httpd.followRedirects())
                return null;

            String location = userResponse.header(HTTP.LOCATION);
            if (null == location)
                return null;
            UnoUrl url = userResponse.request().url().resolve(location);

            // Don't follow redirects to unsupported protocols.
            if (url == null)
                return null;

            // If configured, don't follow redirects between SSL and non-SSL.
            boolean sameScheme = url.scheme().equals(userResponse.request().url().scheme());
            if (!sameScheme && !httpd.followSslRedirects())
                return null;

            // Most redirects don't include a request body.
            Request.Builder requestBuilder = userResponse.request().newBuilder();
            if (HTTP.permitsRequestBody(method)) {
                final boolean maintainBody = HTTP.redirectsWithBody(method);
                if (HTTP.redirectsToGet(method)) {
                    requestBuilder.method("GET", null);
                } else {
                    RequestBody requestBody = maintainBody ? userResponse.request().body() : null;
                    requestBuilder.method(method, requestBody);
                }
                if (!maintainBody) {
                    requestBuilder.removeHeader(HTTP.TRANSFER_ENCODING);
                    requestBuilder.removeHeader(HTTP.CONTENT_LENGTH);
                    requestBuilder.removeHeader(HTTP.CONTENT_TYPE);
                }
            }

            // When redirecting across hosts, drop all authentication headers. This
            // is potentially annoying to the application layer since they have no
            // way to retain them.
            if (!Builder.sameConnection(userResponse.request().url(), url)) {
                requestBuilder.removeHeader("Authorization");
            }

            return requestBuilder.url(url).build();

        case HTTP.HTTP_CLIENT_TIMEOUT:
            // 408's are rare in practice, but some servers like HAProxy use this response code. The
            // spec says that we may repeat the request without modifications. Modern browsers also
            // repeat the request (even non-idempotent ones.)
            if (!httpd.retryOnConnectionFailure()) {
                // The application layer has directed us not to retry the request.
                return null;
            }

            RequestBody requestBody = userResponse.request().body();
            if (requestBody != null && requestBody.isOneShot()) {
                return null;
            }

            if (userResponse.priorResponse() != null
                    && userResponse.priorResponse().code() == HTTP.HTTP_CLIENT_TIMEOUT) {
                // We attempted to retry and got another timeout. Give up.
                return null;
            }

            if (retryAfter(userResponse, 0) > 0) {
                return null;
            }

            return userResponse.request();

        case HTTP.HTTP_UNAVAILABLE:
            if (userResponse.priorResponse() != null && userResponse.priorResponse().code() == HTTP.HTTP_UNAVAILABLE) {
                // We attempted to retry and got another timeout. Give up.
                return null;
            }

            if (retryAfter(userResponse, Integer.MAX_VALUE) == 0) {
                // specifically received an instruction to retry without delay
                return userResponse.request();
            }

            return null;

        default:
            return null;
        }
    }

    private int retryAfter(Response userResponse, int defaultDelay) {
        String header = userResponse.header("Retry-After");

        if (null == header) {
            return defaultDelay;
        }

        if (header.matches("\\d+")) {
            return Integer.valueOf(header);
        }

        return Integer.MAX_VALUE;
    }

}
