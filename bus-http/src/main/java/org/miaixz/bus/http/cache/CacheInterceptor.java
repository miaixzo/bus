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
package org.miaixz.bus.http.cache;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Header;
import org.miaixz.bus.core.lang.Http;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.*;
import org.miaixz.bus.http.bodys.RealResponseBody;
import org.miaixz.bus.http.metric.Interceptor;
import org.miaixz.bus.http.metric.Internal;
import org.miaixz.bus.http.metric.NewChain;
import org.miaixz.bus.http.metric.http.HttpCodec;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 服务来自缓存的请求并将响应写入缓存。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheInterceptor implements Interceptor {

    final InternalCache cache;

    public CacheInterceptor(InternalCache cache) {
        this.cache = cache;
    }

    private static Response stripBody(Response response) {
        return null != response && null != response.body()
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
            if (isContentSpecificHeader(fieldName)
                    || !isEndToEnd(fieldName)
                    || null == networkHeaders.get(fieldName)) {
                Internal.instance.addLenient(result, fieldName, value);
            }
        }

        for (int i = 0, size = networkHeaders.size(); i < size; i++) {
            String fieldName = networkHeaders.name(i);
            if (!isContentSpecificHeader(fieldName) && isEndToEnd(fieldName)) {
                Internal.instance.addLenient(result, fieldName, networkHeaders.value(i));
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
        return !Header.CONNECTION.equalsIgnoreCase(fieldName)
                && !Header.KEEP_ALIVE.equalsIgnoreCase(fieldName)
                && !Header.PROXY_AUTHENTICATE.equalsIgnoreCase(fieldName)
                && !Header.PROXY_AUTHORIZATION.equalsIgnoreCase(fieldName)
                && !Header.TE.equalsIgnoreCase(fieldName)
                && !Header.TRAILERS.equalsIgnoreCase(fieldName)
                && !Header.TRANSFER_ENCODING.equalsIgnoreCase(fieldName)
                && !Header.UPGRADE.equalsIgnoreCase(fieldName);
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
    public Response intercept(NewChain chain) throws IOException {
        Response cacheCandidate = null != cache
                ? cache.get(chain.request())
                : null;

        long now = System.currentTimeMillis();

        CacheStrategy strategy = new CacheStrategy.Factory(now, chain.request(), cacheCandidate).get();
        Request networkRequest = strategy.networkRequest;
        Response cacheResponse = strategy.cacheResponse;

        if (null != cache) {
            cache.trackResponse(strategy);
        }

        if (null != cacheCandidate && null == cacheResponse) {
            // 缓存候选不适用关闭它
            IoKit.close(cacheCandidate.body());
        }

        // 如果我们被禁止使用网络且缓存不足，则失败
        if (null == networkRequest && null == cacheResponse) {
            return new Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(504)
                    .message("Unsatisfiable Request (only-if-cached)")
                    .body(Builder.EMPTY_RESPONSE)
                    .sentRequestAtMillis(-1L)
                    .receivedResponseAtMillis(System.currentTimeMillis())
                    .build();
        }

        // 如果没有网络就完大了
        if (null == networkRequest) {
            return cacheResponse.newBuilder()
                    .cacheResponse(stripBody(cacheResponse))
                    .build();
        }

        Response networkResponse = null;
        try {
            networkResponse = chain.proceed(networkRequest);
        } finally {
            // 如果我们在I/O或其他方面崩溃，不要泄漏缓存体
            if (null == networkResponse && null != cacheCandidate) {
                IoKit.close(cacheCandidate.body());
            }
        }

        // 如果我们也有缓存响应，那么在做一个条件get
        if (null != cacheResponse) {
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
                IoKit.close(cacheResponse.body());
            }
        }

        Response response = networkResponse.newBuilder()
                .cacheResponse(stripBody(cacheResponse))
                .networkResponse(stripBody(networkResponse))
                .build();

        if (null != cache) {
            if (Headers.hasBody(response) && CacheStrategy.isCacheable(response, networkRequest)) {
                // 将此请求提供给缓存
                CacheRequest cacheRequest = cache.put(response);
                return cacheWritingResponse(cacheRequest, response);
            }

            if (Http.invalidatesCache(networkRequest.method())) {
                try {
                    cache.remove(networkRequest);
                } catch (IOException ignored) {
                    // 无法写入缓存
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
        if (null == cacheRequest) {
            return response;
        }
        Sink cacheBodyUnbuffered = cacheRequest.body();
        if (null == cacheBodyUnbuffered) {
            return response;
        }

        final BufferSource source = response.body().source();
        final BufferSink cacheBody = IoKit.buffer(cacheBodyUnbuffered);

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

        String mediaType = response.header(Header.CONTENT_TYPE);
        long contentLength = response.body().length();
        return response.newBuilder()
                .body(new RealResponseBody(mediaType, contentLength, IoKit.buffer(cacheWritingSource)))
                .build();
    }

}
