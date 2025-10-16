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
package org.miaixz.bus.http.cache;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.io.timout.Timeout;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.Builder;
import org.miaixz.bus.http.Headers;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.bodys.RealResponseBody;
import org.miaixz.bus.http.metric.Interceptor;
import org.miaixz.bus.http.metric.Internal;
import org.miaixz.bus.http.metric.NewChain;
import org.miaixz.bus.http.metric.http.HttpCodec;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * An interceptor that serves requests from the cache and writes responses to the cache.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheInterceptor implements Interceptor {

    final InternalCache cache;

    public CacheInterceptor(InternalCache cache) {
        this.cache = cache;
    }

    /**
     * Strips the body from a response.
     *
     * @param response The response to strip.
     * @return A new response with a null body.
     */
    private static Response stripBody(Response response) {
        return response != null && response.body() != null ? response.newBuilder().body(null).build() : response;
    }

    /**
     * Combines cached headers with network headers as defined by RFC 7234, 4.3.4.
     *
     * @param cachedHeaders  The cached headers.
     * @param networkHeaders The network headers.
     * @return The combined headers.
     */
    private static Headers combine(Headers cachedHeaders, Headers networkHeaders) {
        Headers.Builder result = new Headers.Builder();

        for (int i = 0, size = cachedHeaders.size(); i < size; i++) {
            String fieldName = cachedHeaders.name(i);
            String value = cachedHeaders.value(i);
            if ("Warning".equalsIgnoreCase(fieldName) && value.startsWith(Symbol.ONE)) {
                continue; // Drop 100-level freshness warnings.
            }
            if (isContentSpecificHeader(fieldName) || !isEndToEnd(fieldName) || networkHeaders.get(fieldName) == null) {
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
     * Returns true if {@code fieldName} is an end-to-end HTTP header as defined by RFC 2616.
     *
     * @param fieldName The field name to check.
     * @return {@code true} if the header is end-to-end.
     */
    static boolean isEndToEnd(String fieldName) {
        return !HTTP.CONNECTION.equalsIgnoreCase(fieldName) && !HTTP.KEEP_ALIVE.equalsIgnoreCase(fieldName)
                && !HTTP.PROXY_AUTHENTICATE.equalsIgnoreCase(fieldName)
                && !HTTP.PROXY_AUTHORIZATION.equalsIgnoreCase(fieldName) && !HTTP.TE.equalsIgnoreCase(fieldName)
                && !HTTP.TRAILERS.equalsIgnoreCase(fieldName) && !HTTP.TRANSFER_ENCODING.equalsIgnoreCase(fieldName)
                && !HTTP.UPGRADE.equalsIgnoreCase(fieldName);
    }

    /**
     * Returns true if {@code fieldName} is content-specific and should always be used from the cached headers.
     *
     * @param fieldName The field name to check.
     * @return {@code true} if the header is content-specific.
     */
    static boolean isContentSpecificHeader(String fieldName) {
        return HTTP.CONTENT_LENGTH.equalsIgnoreCase(fieldName) || HTTP.CONTENT_ENCODING.equalsIgnoreCase(fieldName)
                || HTTP.CONTENT_TYPE.equalsIgnoreCase(fieldName);
    }

    @Override
    public Response intercept(NewChain chain) throws IOException {
        Response cacheCandidate = cache != null ? cache.get(chain.request()) : null;

        long now = System.currentTimeMillis();

        CacheStrategy strategy = new CacheStrategy.Factory(now, chain.request(), cacheCandidate).get();
        Request networkRequest = strategy.networkRequest;
        Response cacheResponse = strategy.cacheResponse;

        if (cache != null) {
            cache.trackResponse(strategy);
        }

        if (cacheCandidate != null && cacheResponse == null) {
            IoKit.close(cacheCandidate.body()); // The cache candidate wasn't applicable. Close it.
        }

        // If we're forbidden from using the network and the cache is insufficient, fail.
        if (networkRequest == null && cacheResponse == null) {
            return new Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(504)
                    .message("Unsatisfiable Request (only-if-cached)").body(Builder.EMPTY_RESPONSE)
                    .sentRequestAtMillis(-1L).receivedResponseAtMillis(System.currentTimeMillis()).build();
        }

        // If we don't need the network, we're done.
        if (networkRequest == null) {
            return cacheResponse.newBuilder().cacheResponse(stripBody(cacheResponse)).build();
        }

        Response networkResponse = null;
        try {
            networkResponse = chain.proceed(networkRequest);
        } finally {
            // If we crashed on I/O or otherwise, don't leak the cache body.
            if (networkResponse == null && cacheCandidate != null) {
                IoKit.close(cacheCandidate.body());
            }
        }

        // If we have a cache response, we're doing a conditional GET.
        if (cacheResponse != null) {
            if (networkResponse.code() == HTTP.HTTP_NOT_MODIFIED) {
                Response response = cacheResponse.newBuilder()
                        .headers(combine(cacheResponse.headers(), networkResponse.headers()))
                        .sentRequestAtMillis(networkResponse.sentRequestAtMillis())
                        .receivedResponseAtMillis(networkResponse.receivedResponseAtMillis())
                        .cacheResponse(stripBody(cacheResponse)).networkResponse(stripBody(networkResponse)).build();
                networkResponse.body().close();

                // Update the cache after combining headers but before stripping the content-encoding header.
                cache.trackConditionalCacheHit();
                cache.update(cacheResponse, response);
                return response;
            } else {
                IoKit.close(cacheResponse.body());
            }
        }

        Response response = networkResponse.newBuilder().cacheResponse(stripBody(cacheResponse))
                .networkResponse(stripBody(networkResponse)).build();

        if (cache != null) {
            if (Headers.hasBody(response) && CacheStrategy.isCacheable(response, networkRequest)) {
                // Offer this request to the cache.
                CacheRequest cacheRequest = cache.put(response);
                return cacheWritingResponse(cacheRequest, response);
            }

            if (HTTP.invalidatesCache(networkRequest.method())) {
                try {
                    cache.remove(networkRequest);
                } catch (IOException ignored) {
                    // Unable to invalidates cache.
                }
            }
        }

        return response;
    }

    /**
     * Returns a new source that writes to {@code cacheRequest} as bytes are read by the source consumer. The returned
     * source will be careful to discard the remaining bytes if the stream is closed before it is exhausted; otherwise
     * we may not exhaust the source stream and therefore not complete the cached response.
     *
     * @param cacheRequest The cache request.
     * @param response     The response.
     * @return The response body.
     * @throws IOException if an I/O error occurs.
     */
    private Response cacheWritingResponse(final CacheRequest cacheRequest, Response response) throws IOException {
        // Some apps return a null body; for compatibility we treat that as a null cache request.
        if (cacheRequest == null)
            return response;
        Sink cacheBodyUnbuffered = cacheRequest.body();
        if (cacheBodyUnbuffered == null)
            return response;

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
                        cacheRequest.abort(); // Failed to write a complete cache response.
                    }
                    throw e;
                }

                if (bytesRead == -1) {
                    if (!cacheRequestClosed) {
                        cacheRequestClosed = true;
                        cacheBody.close(); // The cache response is complete!
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

        String contentType = response.header("Content-Type");
        long contentLength = response.body().contentLength();
        return response.newBuilder()
                .body(new RealResponseBody(contentType, contentLength, IoKit.buffer(cacheWritingSource))).build();
    }

}
