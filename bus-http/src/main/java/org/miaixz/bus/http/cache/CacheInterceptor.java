/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
import org.miaixz.bus.logger.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * An interceptor that serves requests from the cache and writes responses to the cache.
 *
 * @author Kimi Liu
 * @since Java 21+
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
            if (HTTP.WARNING.equalsIgnoreCase(fieldName) && value.startsWith(Symbol.ONE)) {
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

    /**
     * Intercepts the request to serve from cache and/or update the cache.
     * <p>
     * This method implements a complete HTTP caching strategy as defined in RFC 7234. It attempts to satisfy requests
     * from the cache when possible, and validates cached responses with the server when needed. Fresh responses are
     * written to the cache for future use.
     * </p>
     *
     * @param chain The interceptor chain containing the request to process.
     * @return The response, either from cache or network, according to HTTP caching semantics.
     * @throws IOException if an I/O error occurs during network communication or cache operations.
     */
    @Override
    public Response intercept(NewChain chain) throws IOException {
        Response cacheCandidate = cache != null ? cache.get(chain.request()) : null;

        long now = System.currentTimeMillis();
        Logger.debug(
                true,
                "Http",
                "protocol=http, Cache strategy evaluation started: method={}, url={}, cacheCandidate={}",
                chain.request().method(),
                chain.request().url().redact(),
                cacheCandidate != null);

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
            Logger.debug(
                    false,
                    "Http",
                    "protocol=http, Cache strategy rejected network and cache: method={}, url={}, status=504",
                    chain.request().method(),
                    chain.request().url().redact());
            return new Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(504)
                    .message("Unsatisfiable Request (only-if-cached)").body(Builder.EMPTY_RESPONSE)
                    .sentRequestAtMillis(-1L).receivedResponseAtMillis(System.currentTimeMillis()).build();
        }

        // If we don't need the network, we're done.
        if (networkRequest == null) {
            Logger.debug(
                    false,
                    "Http",
                    "protocol=http, Cache hit served without network: method={}, url={}, status={}",
                    chain.request().method(),
                    chain.request().url().redact(),
                    cacheResponse.code());
            return cacheResponse.newBuilder().cacheResponse(stripBody(cacheResponse)).build();
        }

        Response networkResponse = null;
        try {
            Logger.debug(
                    true,
                    "Http",
                    "protocol=http, Cache strategy forwarding to network: method={}, url={}, conditionalCache={}",
                    networkRequest.method(),
                    networkRequest.url().redact(),
                    cacheResponse != null);
            networkResponse = chain.proceed(networkRequest);
        } finally {
            // If we crashed on I/O or otherwise, don't leak the cache body.
            if (networkResponse == null && cacheCandidate != null) {
                Logger.warn(
                        false,
                        "Http",
                        "protocol=http, Network response missing; closing cache candidate: method={}, url={}",
                        chain.request().method(),
                        chain.request().url().redact());
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
                Logger.debug(
                        false,
                        "Http",
                        "protocol=http, Conditional cache hit: method={}, url={}, status={}",
                        networkRequest.method(),
                        networkRequest.url().redact(),
                        response.code());
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
                Logger.debug(
                        false,
                        "Http",
                        "protocol=http, Network response offered to cache: method={}, url={}, status={}",
                        networkRequest.method(),
                        networkRequest.url().redact(),
                        response.code());
                return cacheWritingResponse(cacheRequest, response);
            }

            if (HTTP.invalidatesCache(networkRequest.method())) {
                try {
                    cache.remove(networkRequest);
                } catch (IOException ignored) {
                    Logger.warn(
                            false,
                            "Http",
                            "protocol=http, " + (ignored),
                            "Cache invalidation failed: method={}, url={}, exception={}",
                            networkRequest.method(),
                            networkRequest.url().redact(),
                            ignored.getMessage());
                }
            }
        }

        Logger.debug(
                false,
                "Http",
                "protocol=http, Cache strategy completed with network response: method={}, url={}, status={}",
                networkRequest.method(),
                networkRequest.url().redact(),
                response.code());
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

            /**
             * Reads bytes from the source and writes them to both the sink and the cache.
             * <p>
             * This method tees the data stream, ensuring that all bytes read from the response are also written to the
             * cache. If an I/O error occurs, the cache request is aborted.
             * </p>
             *
             * @param sink      The buffer to read bytes into.
             * @param byteCount The maximum number of bytes to read.
             * @return The number of bytes read, or -1 if the end of the stream has been reached.
             * @throws IOException if an I/O error occurs.
             */
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long bytesRead;
                try {
                    bytesRead = source.read(sink, byteCount);
                } catch (IOException e) {
                    Logger.warn(
                            false,
                            "Http",
                            e,
                            "HTTP cache operation failed: component=cache, provider={}, recoverable={}, exception={}",
                            "CacheInterceptor",
                            true,
                            e.getClass().getSimpleName());
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

            /**
             * Returns the timeout for the underlying source.
             *
             * @return The timeout configuration.
             */
            @Override
            public Timeout timeout() {
                return source.timeout();
            }

            /**
             * Closes the source and ensures the cache is properly updated.
             * <p>
             * Attempts to discard any remaining unread bytes before closing. If this fails, the cache request is
             * aborted to prevent incomplete cache entries.
             * </p>
             *
             * @throws IOException if an I/O error occurs during closing.
             */
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

        String contentType = response.header(HTTP.CONTENT_TYPE);
        long contentLength = response.body().contentLength();
        return response.newBuilder()
                .body(new RealResponseBody(contentType, contentLength, IoKit.buffer(cacheWritingSource))).build();
    }

}
