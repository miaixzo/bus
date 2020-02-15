/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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

import org.aoju.bus.Version;
import org.aoju.bus.core.io.Buffer;
import org.aoju.bus.core.utils.DateUtils;
import org.aoju.bus.http.*;
import org.aoju.bus.http.accord.platform.Platform;
import org.aoju.bus.http.bodys.BufferedBody;
import org.aoju.bus.http.bodys.OutputStreamBody;
import org.aoju.bus.http.bodys.StreamedBody;
import org.aoju.bus.http.metric.Dispatcher;
import org.aoju.bus.http.metric.Handshake;
import org.aoju.bus.http.metric.Interceptor;
import org.aoju.bus.logger.Logger;

import java.io.*;
import java.net.*;
import java.security.Permission;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 此实现使用{@linkplain NewCall}发送请求和接收响应
 *
 * @author Kimi Liu
 * @version 5.6.1
 * @since JDK 1.8+
 */
public final class HttpURLConnection extends java.net.HttpURLConnection implements Callback {

    /**
     * 合成响应头:选择(@link Protocol)(“spdy/3.1”、“http/1.1”等)
     */
    public static final String SELECTED_PROTOCOL = Platform.get().getPrefix() + "-Selected-Protocol";

    /**
     * 合成响应标头:加载响应的位置
     */
    public static final String RESPONSE_SOURCE = Platform.get().getPrefix() + "-Response-Source";

    private static final Set<String> METHODS = new LinkedHashSet<>(
            Arrays.asList("OPTIONS", "GET", "HEAD", "POST", "PUT", "DELETE", "TRACE", "PATCH"));
    private static final Comparator<String> FIELD_NAME_COMPARATOR = (a, b) -> {
        if (a == b) {
            return 0;
        } else if (a == null) {
            return -1;
        } else if (b == null) {
            return 1;
        } else {
            return String.CASE_INSENSITIVE_ORDER.compare(a, b);
        }
    };
    private final NetworkInterceptor networkInterceptor = new NetworkInterceptor();
    private final Object lock = new Object();
    Httpd client;
    NewCall call;
    Interceptor urlFilter;
    Response networkResponse;
    boolean connectPending = true;
    Proxy proxy;
    Handshake handshake;
    private Headers.Builder requestHeaders = new Headers.Builder();
    private boolean executed;
    /**
     * 在第一次调用getHeaders()时惰性地创建(使用合成头信息)
     */
    private Headers responseHeaders;
    /**
     * 类似于同名的超类字段，但很长，可在所有平台上使用
     */
    private long fixedContentLength = -1L;
    private Response response;
    private Throwable callFailure;

    public HttpURLConnection(URL url, Httpd client) {
        super(url);
        this.client = client;
    }

    public HttpURLConnection(URL url, Httpd client, Interceptor urlFilter) {
        this(url, client);
        this.urlFilter = urlFilter;
    }

    private static String responseSourceHeader(Response response) {
        if (response.networkResponse() == null) {
            if (response.cacheResponse() == null) {
                return "NONE";
            }
            return "CACHE " + response.code();
        }
        if (response.cacheResponse() == null) {
            return "NETWORK " + response.code();
        }
        return "CONDITIONAL_CACHE " + response.networkResponse().code();
    }

    private static String toHumanReadableAscii(String s) {
        for (int i = 0, length = s.length(), c; i < length; i += Character.charCount(c)) {
            c = s.codePointAt(i);
            if (c > '\u001f' && c < '\u007f') continue;

            Buffer buffer = new Buffer();
            buffer.writeUtf8(s, 0, i);
            buffer.writeUtf8CodePoint('?');
            for (int j = i + Character.charCount(c); j < length; j += Character.charCount(c)) {
                c = s.codePointAt(j);
                buffer.writeUtf8CodePoint(c > '\u001f' && c < '\u007f' ? c : '?');
            }
            return buffer.readUtf8();
        }
        return s;
    }

    private static IOException propagate(Throwable throwable) throws IOException {
        if (throwable instanceof IOException) throw (IOException) throwable;
        if (throwable instanceof Error) throw (Error) throwable;
        if (throwable instanceof RuntimeException) throw (RuntimeException) throwable;
        throw new AssertionError();
    }

    public static Map<String, List<String>> toMultimap(Headers headers, String valueForNullKey) {
        Map<String, List<String>> result = new TreeMap<>(FIELD_NAME_COMPARATOR);
        for (int i = 0, size = headers.size(); i < size; i++) {
            String fieldName = headers.name(i);
            String value = headers.value(i);

            List<String> allValues = new ArrayList<>();
            List<String> otherValues = result.get(fieldName);
            if (otherValues != null) {
                allValues.addAll(otherValues);
            }
            allValues.add(value);
            result.put(fieldName, Collections.unmodifiableList(allValues));
        }
        if (valueForNullKey != null) {
            result.put(null, Collections.unmodifiableList(Collections.singletonList(valueForNullKey)));
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public void connect() throws IOException {
        if (executed) return;

        NewCall call = buildCall();
        executed = true;
        call.enqueue(this);

        synchronized (lock) {
            try {
                while (connectPending && response == null && callFailure == null) {
                    // 等待直到网络拦截器到达或调用失败
                    lock.wait();
                }
                if (callFailure != null) {
                    throw propagate(callFailure);
                }
            } catch (InterruptedException e) {
                // 保留中断状态
                Thread.currentThread().interrupt();
                throw new InterruptedIOException();
            }
        }
    }

    @Override
    public void disconnect() {
        // 在连接存在之前调用disconnect()应该没有效果
        if (call == null) return;
        // 取消阻塞任何正在等待的异步线程
        networkInterceptor.proceed();
        call.cancel();
    }

    @Override
    public InputStream getErrorStream() {
        try {
            Response response = getResponse(true);
            if (HttpHeaders.hasBody(response) && response.code() >= HTTP_BAD_REQUEST) {
                return response.body().byteStream();
            }
            return null;
        } catch (IOException e) {
            return null;
        }
    }

    private Headers getHeaders() throws IOException {
        if (responseHeaders == null) {
            Response response = getResponse(true);
            Headers headers = response.headers();
            responseHeaders = headers.newBuilder()
                    .add(SELECTED_PROTOCOL, response.protocol().toString())
                    .add(RESPONSE_SOURCE, responseSourceHeader(response))
                    .build();
        }
        return responseHeaders;
    }

    @Override
    public String getHeaderField(int position) {
        try {
            Headers headers = getHeaders();
            if (position < 0 || position >= headers.size()) return null;
            return headers.value(position);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String getHeaderField(String fieldName) {
        try {
            return fieldName == null
                    ? StatusLine.get(getResponse(true)).toString()
                    : getHeaders().get(fieldName);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public String getHeaderFieldKey(int position) {
        try {
            Headers headers = getHeaders();
            if (position < 0 || position >= headers.size()) return null;
            return headers.name(position);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Map<String, List<String>> getHeaderFields() {
        try {
            return toMultimap(getHeaders(),
                    StatusLine.get(getResponse(true)).toString());
        } catch (IOException e) {
            return Collections.emptyMap();
        }
    }

    @Override
    public Map<String, List<String>> getRequestProperties() {
        if (connected) {
            throw new IllegalStateException(
                    "Cannot access request header fields after connection is set");
        }

        return toMultimap(requestHeaders.build(), null);
    }

    @Override
    public InputStream getInputStream() throws IOException {
        if (!doInput) {
            throw new ProtocolException("This protocol does not support input");
        }

        Response response = getResponse(false);

        if (response.code() >= HTTP_BAD_REQUEST) {
            throw new FileNotFoundException(url.toString());
        }

        return response.body().byteStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        OutputStreamBody requestBody = (OutputStreamBody) buildCall().request().body();
        if (requestBody == null) {
            throw new ProtocolException("method does not support a request body: " + method);
        }

        // 如果此请求需要向服务器传输字节，则立即构建一个物理连接，并开始通过该连接传输这些字节
        if (requestBody instanceof StreamedBody) {
            connect();
            networkInterceptor.proceed();
        }

        if (requestBody.isClosed()) {
            throw new ProtocolException("cannot write request body after response has been read");
        }

        return requestBody.outputStream();
    }

    @Override
    public Permission getPermission() {
        URL url = getURL();
        String hostname = url.getHost();
        int hostPort = url.getPort() != -1
                ? url.getPort()
                : UnoUrl.defaultPort(url.getProtocol());
        if (usingProxy()) {
            InetSocketAddress proxyAddress = (InetSocketAddress) client.proxy().address();
            hostname = proxyAddress.getHostName();
            hostPort = proxyAddress.getPort();
        }
        return new SocketPermission(hostname + ":" + hostPort, "connect, resolve");
    }

    @Override
    public String getRequestProperty(String field) {
        if (field == null) return null;
        return requestHeaders.get(field);
    }

    @Override
    public boolean getInstanceFollowRedirects() {
        return client.followRedirects();
    }

    @Override
    public void setInstanceFollowRedirects(boolean followRedirects) {
        client = client.newBuilder()
                .followRedirects(followRedirects)
                .build();
    }

    @Override
    public int getConnectTimeout() {
        return client.connectTimeoutMillis();
    }

    @Override
    public void setConnectTimeout(int timeoutMillis) {
        client = client.newBuilder()
                .connectTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public int getReadTimeout() {
        return client.readTimeoutMillis();
    }

    @Override
    public void setReadTimeout(int timeoutMillis) {
        client = client.newBuilder()
                .readTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
                .build();
    }

    private NewCall buildCall() throws IOException {
        if (call != null) {
            return call;
        }

        connected = true;
        if (doOutput) {
            if (method.equals("GET")) {
                // 他们请求一个流来写。这意味着POST方法
                method = "POST";
            } else if (!HttpMethod.permitsRequestBody(method)) {
                throw new ProtocolException(method + " does not support writing");
            }
        }

        if (requestHeaders.get("User-Agent") == null) {
            requestHeaders.add("User-Agent", defaultUserAgent());
        }

        OutputStreamBody requestBody = null;
        if (HttpMethod.permitsRequestBody(method)) {
            // 如果还没有内容类型，则为请求主体添加内容类型
            String contentType = requestHeaders.get("Content-Type");
            if (contentType == null) {
                contentType = "application/x-www-form-urlencoded";
                requestHeaders.add("Content-Type", contentType);
            }

            boolean stream = fixedContentLength != -1L || chunkLength > 0;

            long contentLength = -1L;
            String contentLengthString = requestHeaders.get("Content-Length");
            if (fixedContentLength != -1L) {
                contentLength = fixedContentLength;
            } else if (contentLengthString != null) {
                contentLength = Long.parseLong(contentLengthString);
            }

            requestBody = stream
                    ? new StreamedBody(contentLength)
                    : new BufferedBody(contentLength);
            requestBody.timeout().timeout(client.writeTimeoutMillis(), TimeUnit.MILLISECONDS);
        }

        UnoUrl url;
        try {
            url = UnoUrl.get(getURL().toString());
        } catch (IllegalArgumentException e) {
            if (Builder.instance.isInvalidHttpUrlHost(e)) {
                UnknownHostException unknownHost = new UnknownHostException();
                unknownHost.initCause(e);
                throw unknownHost;
            }
            MalformedURLException malformedUrl = new MalformedURLException();
            malformedUrl.initCause(e);
            throw malformedUrl;
        }

        Request request = new Request.Builder()
                .url(url)
                .headers(requestHeaders.build())
                .method(method, requestBody)
                .build();

        Httpd.Builder clientBuilder = client.newBuilder();
        clientBuilder.interceptors().clear();
        clientBuilder.interceptors().add(UnexpectedException.INTERCEPTOR);
        clientBuilder.networkInterceptors().clear();
        clientBuilder.networkInterceptors().add(networkInterceptor);

        // 使用单独的分配器，这样就不会影响限制。但是使用相同的执行服务!
        clientBuilder.dispatcher(new Dispatcher(client.dispatcher().executorService()));

        // 如果我们目前没有使用缓存，请确保引擎的客户端没有缓存
        if (!getUseCaches()) {
            clientBuilder.cache(null);
        }

        return call = clientBuilder.build().newCall(request);
    }

    private String defaultUserAgent() {
        String agent = System.getProperty("http.agent");
        return agent != null ? toHumanReadableAscii(agent) : Version.all();
    }

    private Response getResponse(boolean networkResponseOnError) throws IOException {
        synchronized (lock) {
            if (response != null) return response;
            if (callFailure != null) {
                if (networkResponseOnError && networkResponse != null) return networkResponse;
                throw propagate(callFailure);
            }
        }

        NewCall call = buildCall();
        networkInterceptor.proceed();

        OutputStreamBody requestBody = (OutputStreamBody) call.request().body();
        if (requestBody != null) requestBody.outputStream().close();

        if (executed) {
            synchronized (lock) {
                try {
                    while (response == null && callFailure == null) {
                        lock.wait(); // Wait until the response is returned or the call fails.
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Retain interrupted status.
                    throw new InterruptedIOException();
                }
            }
        } else {
            executed = true;
            try {
                onResponse(call, call.execute());
            } catch (IOException e) {
                onFailure(call, e);
            }
        }

        synchronized (lock) {
            if (callFailure != null) throw propagate(callFailure);
            if (response != null) return response;
        }

        throw new AssertionError();
    }

    @Override
    public boolean usingProxy() {
        if (proxy != null) return true;
        Proxy clientProxy = client.proxy();
        return clientProxy != null && clientProxy.type() != Proxy.Type.DIRECT;
    }

    @Override
    public String getResponseMessage() throws IOException {
        return getResponse(true).message();
    }

    @Override
    public int getResponseCode() throws IOException {
        return getResponse(true).code();
    }

    @Override
    public void setRequestProperty(String field, String newValue) {
        if (connected) {
            throw new IllegalStateException("Cannot set request property after connection is made");
        }
        if (field == null) {
            throw new NullPointerException("field == null");
        }
        if (newValue == null) {
            Logger.warn("Ignoring header " + field + " because its value was null.", null);
            return;
        }

        requestHeaders.set(field, newValue);
    }

    @Override
    public void setIfModifiedSince(long newValue) {
        super.setIfModifiedSince(newValue);
        if (ifModifiedSince != 0) {
            requestHeaders.set("If-Modified-Since", DateUtils.format(new Date(ifModifiedSince)));
        } else {
            requestHeaders.removeAll("If-Modified-Since");
        }
    }

    @Override
    public void addRequestProperty(String field, String value) {
        if (connected) {
            throw new IllegalStateException("Cannot add request property after connection is made");
        }
        if (field == null) {
            throw new NullPointerException("field == null");
        }
        if (value == null) {
            Logger.warn("Ignoring header " + field + " because its value was null.", null);
            return;
        }

        requestHeaders.add(field, value);
    }

    @Override
    public void setRequestMethod(String method) throws ProtocolException {
        if (!METHODS.contains(method)) {
            throw new ProtocolException("Expected one of " + METHODS + " but was " + method);
        }
        this.method = method;
    }

    @Override
    public void setFixedLengthStreamingMode(int contentLength) {
        setFixedLengthStreamingMode((long) contentLength);
    }

    @Override
    public void setFixedLengthStreamingMode(long contentLength) {
        if (super.connected) throw new IllegalStateException("Already connected");
        if (chunkLength > 0) throw new IllegalStateException("Already in chunked mode");
        if (contentLength < 0) throw new IllegalArgumentException("contentLength < 0");
        this.fixedContentLength = contentLength;
        super.fixedContentLength = (int) Math.min(contentLength, Integer.MAX_VALUE);
    }

    @Override
    public void onFailure(NewCall call, IOException e) {
        synchronized (lock) {
            this.callFailure = (e instanceof UnexpectedException) ? e.getCause() : e;
            lock.notifyAll();
        }
    }

    @Override
    public void onResponse(NewCall call, Response response) {
        synchronized (lock) {
            this.response = response;
            this.handshake = response.handshake();
            this.url = response.request().url().url();
            lock.notifyAll();
        }
    }

    static final class UnexpectedException extends IOException {
        static final Interceptor INTERCEPTOR = chain -> {
            try {
                return chain.proceed(chain.request());
            } catch (Error | RuntimeException e) {
                throw new UnexpectedException(e);
            }
        };

        UnexpectedException(Throwable cause) {
            super(cause);
        }
    }

    final class NetworkInterceptor implements Interceptor {
        private boolean proceed;

        public void proceed() {
            synchronized (lock) {
                this.proceed = true;
                lock.notifyAll();
            }
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            synchronized (lock) {
                connectPending = false;
                proxy = chain.connection().route().proxy();
                handshake = chain.connection().handshake();
                lock.notifyAll();

                try {
                    while (!proceed) {
                        lock.wait();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new InterruptedIOException();
                }
            }

            if (request.body() instanceof OutputStreamBody) {
                OutputStreamBody requestBody = (OutputStreamBody) request.body();
                request = requestBody.prepareToSendRequest(request);
            }

            Response response = chain.proceed(request);

            synchronized (lock) {
                networkResponse = response;
                url = response.request().url().url();
            }

            return response;
        }
    }

}