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
package org.aoju.bus.http.cache;

import org.aoju.bus.core.io.*;
import org.aoju.bus.core.lang.Header;
import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.http.*;
import org.aoju.bus.http.bodys.RealResponseBody;
import org.aoju.bus.http.bodys.ResponseBody;
import org.aoju.bus.http.metric.Interceptor;
import org.aoju.bus.http.metric.http.HttpCodec;
import org.aoju.bus.http.metric.http.HttpHeaders;
import org.aoju.bus.http.metric.http.HttpMethod;
import org.aoju.bus.logger.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 服务来自缓存的请求并将响应写入缓存。
 *
 * @author Kimi Liu
 * @version 5.6.2
 * @since JDK 1.8+
 */
public final class CacheInterceptor implements Interceptor {

    final InternalCache cache;

    public CacheInterceptor(InternalCache cache) {
        this.cache = cache;
    }

    private static Response stripBody(Response response) {
        return response != null && response.body() != null
                ? response.newBuilder().body(null).build()
                : response;
    }

    /**
     * 将缓存的报头与RFC 7234,4.3.4定义的网络报头相结合。
     *
     * @param cachedHeaders  缓存header信息
     * @param networkHeaders 请求header信息
     * @return the header
     */
    private static Headers combine(Headers cachedHeaders, Headers networkHeaders) {
        Headers.Builder result = new Headers.Builder();

        for (int i = 0, size = cachedHeaders.size(); i < size; i++) {
            String fieldName = cachedHeaders.name(i);
            String value = cachedHeaders.value(i);
            if ("Warning".equalsIgnoreCase(fieldName) && value.startsWith(Symbol.ONE)) {
                continue; // Drop 100-level freshness warnings.
            }
            if (isContentSpecificHeader(fieldName) || !isEndToEnd(fieldName)
                    || networkHeaders.get(fieldName) == null) {
                Builder.instance.addLenient(result, fieldName, value);
            }
        }

        for (int i = 0, size = networkHeaders.size(); i < size; i++) {
            String fieldName = networkHeaders.name(i);
            if (!isContentSpecificHeader(fieldName) && isEndToEnd(fieldName)) {
                Builder.instance.addLenient(result, fieldName, networkHeaders.value(i));
            }
        }

        return result.build();
    }

    /**
     * 如果{@code fieldName}是RFC 2616所定义的端到端HTTP标头，则返回true。
     *
     * @param fieldName 属性名称
     * @return the true/false
     */
    static boolean isEndToEnd(String fieldName) {
        return !"Connection".equalsIgnoreCase(fieldName)
                && !"Keep-Alive".equalsIgnoreCase(fieldName)
                && !"Proxy-Authenticate".equalsIgnoreCase(fieldName)
                && !"Proxy-Authorization".equalsIgnoreCase(fieldName)
                && !"TE".equalsIgnoreCase(fieldName)
                && !"Trailers".equalsIgnoreCase(fieldName)
                && !"Transfer-Encoding".equalsIgnoreCase(fieldName)
                && !"Upgrade".equalsIgnoreCase(fieldName);
    }

    /**
     * 如果{@code fieldName}是特定于内容的，则返回true，因此应该始终从缓存的标头中使用
     *
     * @param fieldName 属性名称
     * @return the true/false
     */
    static boolean isContentSpecificHeader(String fieldName) {
        return Header.CONTENT_LENGTH.equalsIgnoreCase(fieldName)
                || Header.CONTENT_ENCODING.equalsIgnoreCase(fieldName)
                || Header.CONTENT_TYPE.equalsIgnoreCase(fieldName);
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Response cacheCandidate = cache != null
                ? cache.get(chain.request())
                : null;

        long now = System.currentTimeMillis();

        CacheStrategy strategy = new CacheStrategy.Factory(now, chain.request(), cacheCandidate).get();
        Request networkRequest = strategy.networkRequest;
        Response cacheResponse = strategy.cacheResponse;

        if (cache != null) {
            cache.trackResponse(strategy);
        }

        if (cacheCandidate != null && cacheResponse == null) {
            // 缓存候选不适用。关闭它
            IoUtils.close(cacheCandidate.body());
        }

        // 如果我们被禁止使用网络且缓存不足，则失败
        if (networkRequest == null && cacheResponse == null) {
            return new Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(504)
                    .message("Unsatisfiable Request (only-if-cached)")
                    .body(ResponseBody.create(null, Normal.EMPTY_BYTE_ARRAY))
                    .sentRequestAtMillis(-1L)
                    .receivedResponseAtMillis(System.currentTimeMillis())
                    .build();
        }

        // 如果没有网络就完大了
        if (networkRequest == null) {
            return cacheResponse.newBuilder()
                    .cacheResponse(stripBody(cacheResponse))
                    .build();
        }

        Response networkResponse = null;
        try {
            networkResponse = chain.proceed(networkRequest);
        } finally {
            // 如果我们在I/O或其他方面崩溃，不要泄漏缓存体
            if (networkResponse == null && cacheCandidate != null) {
                IoUtils.close(cacheCandidate.body());
            }
        }

        // 如果我们也有缓存响应，那么在做一个条件get
        if (cacheResponse != null) {
            if (networkResponse.code() == Http.HTTP_NOT_MODIFIED) {
                Response response = cacheResponse.newBuilder()
                        .headers(combine(cacheResponse.headers(), networkResponse.headers()))
                        .sentRequestAtMillis(networkResponse.sentRequestAtMillis())
                        .receivedResponseAtMillis(networkResponse.receivedResponseAtMillis())
                        .cacheResponse(stripBody(cacheResponse))
                        .networkResponse(stripBody(networkResponse))
                        .build();
                networkResponse.body().close();

                // 在合并报头之后但在剥离内容编码报头之前更新缓存(由initContentStream()执行)
                cache.trackConditionalCacheHit();
                cache.update(cacheResponse, response);
                return response;
            } else {
                IoUtils.close(cacheResponse.body());
            }
        }

        Response response = networkResponse.newBuilder()
                .cacheResponse(stripBody(cacheResponse))
                .networkResponse(stripBody(networkResponse))
                .build();

        if (cache != null) {
            if (HttpHeaders.hasBody(response) && CacheStrategy.isCacheable(response, networkRequest)) {
                // 将此请求提供给缓存
                CacheRequest cacheRequest = cache.put(response);
                return cacheWritingResponse(cacheRequest, response);
            }

            if (HttpMethod.invalidatesCache(networkRequest.method())) {
                try {
                    cache.remove(networkRequest);
                } catch (IOException ignored) {
                    // 无法写入缓存
                    Logger.error(ignored);
                }
            }
        }

        return response;
    }

    /**
     * 当源使用者读取字节时，返回一个向{@code cacheRequest}写入字节的新源。
     * 在关闭流时，要小心地丢弃剩余的字节;否则，我们可能永远不会耗尽源流，因此无法完成缓存的响应
     *
     * @param cacheRequest 缓存请求
     * @param response     相应信息
     * @return 相应体
     * @throws IOException 异常
     */
    private Response cacheWritingResponse(final CacheRequest cacheRequest, Response response)
            throws IOException {
        // 一些应用程序返回一个空体;为了兼容性，我们将其视为空缓存请求
        if (cacheRequest == null) return response;
        Sink cacheBodyUnbuffered = cacheRequest.body();
        if (cacheBodyUnbuffered == null) return response;

        final BufferSource source = response.body().source();
        final BufferSink cacheBody = IoUtils.buffer(cacheBodyUnbuffered);

        Source cacheWritingSource = new Source() {
            boolean cacheRequestClosed;

            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead;
                try {
                    bytesRead = source.read(sink, byteCount);
                } catch (IOException e) {
                    if (!cacheRequestClosed) {
                        cacheRequestClosed = true;
                        // 未能写入完整的缓存响应
                        cacheRequest.abort();
                    }
                    throw e;
                }

                if (bytesRead == -1) {
                    if (!cacheRequestClosed) {
                        cacheRequestClosed = true;
                        // 缓存响应完成
                        cacheBody.close();
                    }
                    return -1;
                }

                sink.copyTo(cacheBody.buffer(), sink.size() - bytesRead, bytesRead);
                cacheBody.emitCompleteSegments();
                return bytesRead;
            }

            @Override
            public Timeout timeout() {
                return source.timeout();
            }

            @Override
            public void close() throws IOException {
                if (!cacheRequestClosed
                        && !Builder.discard(this, HttpCodec.DISCARD_STREAM_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                    cacheRequestClosed = true;
                    cacheRequest.abort();
                }
                source.close();
            }
        };

        String contentType = response.header(Header.CONTENT_TYPE);
        long contentLength = response.body().contentLength();
        return response.newBuilder()
                .body(new RealResponseBody(contentType, contentLength, IoUtils.buffer(cacheWritingSource)))
                .build();
    }

}
