/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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

import org.aoju.bus.http.Header;
import org.aoju.bus.http.Internal;
import org.aoju.bus.http.Request;
import org.aoju.bus.http.Response;
import org.aoju.bus.http.metric.http.HttpDate;
import org.aoju.bus.http.metric.http.HttpHeaders;
import org.aoju.bus.http.metric.http.StatusLine;

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * Given a request and cached response, this figures out whether to use the network, the cache, or
 * both.
 *
 * <p>Selecting a cache strategy may add conditions to the request (like the "If-Modified-Since"
 * header for conditional GETs) or warnings to the cached response (if the cached data is
 * potentially stale).
 *
 * @author Kimi Liu
 * @version 5.3.6
 * @since JDK 1.8+
 */
public final class CacheStrategy {
    /**
     * The request to send on the network, or null if this call doesn't use the network.
     */
    public final Request networkRequest;

    /**
     * The cached response to return or validate; or null if this call doesn't use a cache.
     */
    public final Response cacheResponse;

    CacheStrategy(Request networkRequest, Response cacheResponse) {
        this.networkRequest = networkRequest;
        this.cacheResponse = cacheResponse;
    }

    public static boolean isCacheable(Response response, Request request) {
        switch (response.code()) {
            case HttpURLConnection.HTTP_OK:
            case HttpURLConnection.HTTP_NOT_AUTHORITATIVE:
            case HttpURLConnection.HTTP_NO_CONTENT:
            case HttpURLConnection.HTTP_MULT_CHOICE:
            case HttpURLConnection.HTTP_MOVED_PERM:
            case HttpURLConnection.HTTP_NOT_FOUND:
            case HttpURLConnection.HTTP_BAD_METHOD:
            case HttpURLConnection.HTTP_GONE:
            case HttpURLConnection.HTTP_REQ_TOO_LONG:
            case HttpURLConnection.HTTP_NOT_IMPLEMENTED:
            case StatusLine.HTTP_PERM_REDIRECT:
                // These codes can be cached unless headers forbid it.
                break;

            case HttpURLConnection.HTTP_MOVED_TEMP:
            case StatusLine.HTTP_TEMP_REDIRECT:
                if (response.header("Expires") != null
                        || response.cacheControl().maxAgeSeconds() != -1
                        || response.cacheControl().isPublic()
                        || response.cacheControl().isPrivate()) {
                    break;
                }

            default:
                return false;
        }

        return !response.cacheControl().noStore() && !request.cacheControl().noStore();
    }

    public static class Factory {
        final long nowMillis;
        final Request request;
        final Response cacheResponse;

        /**
         * The server's time when the cached response was served, if known.
         */
        private Date servedDate;
        private String servedDateString;

        /**
         * The last modified date of the cached response, if known.
         */
        private Date lastModified;
        private String lastModifiedString;

        /**
         * The expiration date of the cached response, if known. If both this field and the max age are
         * set, the max age is preferred.
         */
        private Date expires;

        /**
         * Extension header set by httpClient specifying the timestamp when the cached HTTP request was
         * first initiated.
         */
        private long sentRequestMillis;

        /**
         * Extension header set by httpClient specifying the timestamp when the cached HTTP response was
         * first received.
         */
        private long receivedResponseMillis;

        /**
         * Etag of the cached response.
         */
        private String etag;

        /**
         * Age of the cached response.
         */
        private int ageSeconds = -1;

        public Factory(long nowMillis, Request request, Response cacheResponse) {
            this.nowMillis = nowMillis;
            this.request = request;
            this.cacheResponse = cacheResponse;

            if (cacheResponse != null) {
                this.sentRequestMillis = cacheResponse.sentRequestAtMillis();
                this.receivedResponseMillis = cacheResponse.receivedResponseAtMillis();
                Header headers = cacheResponse.headers();
                for (int i = 0, size = headers.size(); i < size; i++) {
                    String fieldName = headers.name(i);
                    String value = headers.value(i);
                    if ("Date".equalsIgnoreCase(fieldName)) {
                        servedDate = HttpDate.parse(value);
                        servedDateString = value;
                    } else if ("Expires".equalsIgnoreCase(fieldName)) {
                        expires = HttpDate.parse(value);
                    } else if ("Last-Modified".equalsIgnoreCase(fieldName)) {
                        lastModified = HttpDate.parse(value);
                        lastModifiedString = value;
                    } else if ("ETag".equalsIgnoreCase(fieldName)) {
                        etag = value;
                    } else if ("Age".equalsIgnoreCase(fieldName)) {
                        ageSeconds = HttpHeaders.parseSeconds(value, -1);
                    }
                }
            }
        }

        private static boolean hasConditions(Request request) {
            return request.header("If-Modified-Since") != null || request.header("If-None-Match") != null;
        }

        public CacheStrategy get() {
            CacheStrategy candidate = getCandidate();

            if (candidate.networkRequest != null && request.cacheControl().onlyIfCached()) {
                // We're forbidden from using the network and the cache is insufficient.
                return new CacheStrategy(null, null);
            }

            return candidate;
        }

        private CacheStrategy getCandidate() {
            if (cacheResponse == null) {
                return new CacheStrategy(request, null);
            }

            if (request.isHttps() && cacheResponse.handshake() == null) {
                return new CacheStrategy(request, null);
            }

            if (!isCacheable(cacheResponse, request)) {
                return new CacheStrategy(request, null);
            }

            CacheControl requestCaching = request.cacheControl();
            if (requestCaching.noCache() || hasConditions(request)) {
                return new CacheStrategy(request, null);
            }

            CacheControl responseCaching = cacheResponse.cacheControl();

            long ageMillis = cacheResponseAge();
            long freshMillis = computeFreshnessLifetime();

            if (requestCaching.maxAgeSeconds() != -1) {
                freshMillis = Math.min(freshMillis, TimeUnit.SECONDS.toMillis(requestCaching.maxAgeSeconds()));
            }

            long minFreshMillis = 0;
            if (requestCaching.minFreshSeconds() != -1) {
                minFreshMillis = TimeUnit.SECONDS.toMillis(requestCaching.minFreshSeconds());
            }

            long maxStaleMillis = 0;
            if (!responseCaching.mustRevalidate() && requestCaching.maxStaleSeconds() != -1) {
                maxStaleMillis = TimeUnit.SECONDS.toMillis(requestCaching.maxStaleSeconds());
            }

            if (!responseCaching.noCache() && ageMillis + minFreshMillis < freshMillis + maxStaleMillis) {
                Response.Builder builder = cacheResponse.newBuilder();
                if (ageMillis + minFreshMillis >= freshMillis) {
                    builder.addHeader("Warning", "110 HttpURLConnection \"Response is stale\"");
                }
                long oneDayMillis = 24 * 60 * 60 * 1000L;
                if (ageMillis > oneDayMillis && isFreshnessLifetimeHeuristic()) {
                    builder.addHeader("Warning", "113 HttpURLConnection \"Heuristic expiration\"");
                }
                return new CacheStrategy(null, builder.build());
            }

            String conditionName;
            String conditionValue;
            if (etag != null) {
                conditionName = "If-None-Match";
                conditionValue = etag;
            } else if (lastModified != null) {
                conditionName = "If-Modified-Since";
                conditionValue = lastModifiedString;
            } else if (servedDate != null) {
                conditionName = "If-Modified-Since";
                conditionValue = servedDateString;
            } else {
                return new CacheStrategy(request, null); // No condition! Make a regular request.
            }

            Header.Builder conditionalRequestHeaders = request.headers().newBuilder();
            Internal.instance.addLenient(conditionalRequestHeaders, conditionName, conditionValue);

            Request conditionalRequest = request.newBuilder()
                    .headers(conditionalRequestHeaders.build())
                    .build();
            return new CacheStrategy(conditionalRequest, cacheResponse);
        }

        private long computeFreshnessLifetime() {
            CacheControl responseCaching = cacheResponse.cacheControl();
            if (responseCaching.maxAgeSeconds() != -1) {
                return TimeUnit.SECONDS.toMillis(responseCaching.maxAgeSeconds());
            } else if (expires != null) {
                long servedMillis = servedDate != null
                        ? servedDate.getTime()
                        : receivedResponseMillis;
                long delta = expires.getTime() - servedMillis;
                return delta > 0 ? delta : 0;
            } else if (lastModified != null
                    && cacheResponse.request().url().query() == null) {
                long servedMillis = servedDate != null
                        ? servedDate.getTime()
                        : sentRequestMillis;
                long delta = servedMillis - lastModified.getTime();
                return delta > 0 ? (delta / 10) : 0;
            }
            return 0;
        }

        private long cacheResponseAge() {
            long apparentReceivedAge = servedDate != null
                    ? Math.max(0, receivedResponseMillis - servedDate.getTime())
                    : 0;
            long receivedAge = ageSeconds != -1
                    ? Math.max(apparentReceivedAge, TimeUnit.SECONDS.toMillis(ageSeconds))
                    : apparentReceivedAge;
            long responseDuration = receivedResponseMillis - sentRequestMillis;
            long residentDuration = nowMillis - receivedResponseMillis;
            return receivedAge + responseDuration + residentDuration;
        }

        private boolean isFreshnessLifetimeHeuristic() {
            return cacheResponse.cacheControl().maxAgeSeconds() == -1 && expires == null;
        }

    }

}
