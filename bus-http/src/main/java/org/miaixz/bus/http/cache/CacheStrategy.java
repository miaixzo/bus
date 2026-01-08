/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.http.Builder;
import org.miaixz.bus.http.Headers;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.metric.Internal;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Given a request and a cached response, this class determines whether to use the network, the cache, or both.
 * Selecting a cache strategy may add conditions to the request (like the "If-Modified-Since" header for conditional
 * GETs) or warnings to the cached response (if the cached data is potentially stale).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheStrategy {

    /**
     * The request to send on the network, or null if the network is not used.
     */
    public final Request networkRequest;

    /**
     * The cached response to return or validate; null if this call does not use the cache.
     */
    public final Response cacheResponse;

    CacheStrategy(Request networkRequest, Response cacheResponse) {
        this.networkRequest = networkRequest;
        this.cacheResponse = cacheResponse;
    }

    /**
     * Returns true if {@code response} can be stored to later serve another request.
     *
     * @param response The response to check.
     * @param request  The request that resulted in the response.
     * @return {@code true} if the response is cacheable.
     */
    public static boolean isCacheable(Response response, Request request) {
        // Always go to network for non-cacheable response codes (RFC 7231 section 6.1).
        switch (response.code()) {
            case HTTP.HTTP_OK:
            case HTTP.HTTP_NOT_AUTHORITATIVE:
            case HTTP.HTTP_NO_CONTENT:
            case HTTP.HTTP_MULT_CHOICE:
            case HTTP.HTTP_MOVED_PERM:
            case HTTP.HTTP_NOT_FOUND:
            case HTTP.HTTP_BAD_METHOD:
            case HTTP.HTTP_GONE:
            case HTTP.HTTP_REQ_TOO_LONG:
            case HTTP.HTTP_NOT_IMPLEMENTED:
            case HTTP.HTTP_PERM_REDIRECT:
                // These codes can be cached unless headers forbid it.
                break;

            case HTTP.HTTP_MOVED_TEMP:
            case HTTP.HTTP_TEMP_REDIRECT:
                // These codes can only be cached if explicitly allowed by headers.
                if (response.header(HTTP.EXPIRES) != null || response.cacheControl().maxAgeSeconds() != -1
                        || response.cacheControl().isPublic() || response.cacheControl().isPrivate()) {
                    break;
                }
                // Fall-through.
            default:
                // All other codes cannot be cached.
                return false;
        }

        // A 'no-store' directive on either the request or the response prevents the response from being cached.
        return !response.cacheControl().noStore() && !request.cacheControl().noStore();
    }

    /**
     * A factory for creating {@link CacheStrategy} instances.
     */
    public static class Factory {

        final long nowMillis;
        final Request request;
        final Response cacheResponse;

        /**
         * The server-provided date of the cached response.
         */
        private Date servedDate;
        private String servedDateString;

        /**
         * The last modified date of the cached response.
         */
        private Date lastModified;
        private String lastModifiedString;

        /**
         * The expiration date of the cached response. If both this field and the max age are set, the max age is
         * preferred.
         */
        private Date expires;

        /**
         * Extension header set by Httpd specifying the timestamp when the cached HTTP request was first initiated.
         */
        private long sentRequestMillis;

        /**
         * Extension header set by Httpd specifying the timestamp when the cached HTTP response was first received.
         */
        private long receivedResponseMillis;

        /**
         * The ETag of the cached response.
         */
        private String etag;

        /**
         * The age of the cached response.
         */
        private int ageSeconds = -1;

        public Factory(long nowMillis, Request request, Response cacheResponse) {
            this.nowMillis = nowMillis;
            this.request = request;
            this.cacheResponse = cacheResponse;

            if (cacheResponse != null) {
                this.sentRequestMillis = cacheResponse.sentRequestAtMillis();
                this.receivedResponseMillis = cacheResponse.receivedResponseAtMillis();
                Headers headers = cacheResponse.headers();
                for (int i = 0, size = headers.size(); i < size; i++) {
                    String fieldName = headers.name(i);
                    String value = headers.value(i);
                    if ("Date".equalsIgnoreCase(fieldName)) {
                        servedDate = Builder.parse(value);
                        servedDateString = value;
                    } else if ("Expires".equalsIgnoreCase(fieldName)) {
                        expires = Builder.parse(value);
                    } else if ("Last-Modified".equalsIgnoreCase(fieldName)) {
                        lastModified = Builder.parse(value);
                        lastModifiedString = value;
                    } else if ("ETag".equalsIgnoreCase(fieldName)) {
                        etag = value;
                    } else if ("Age".equalsIgnoreCase(fieldName)) {
                        ageSeconds = Headers.parseSeconds(value, -1);
                    }
                }
            }
        }

        /**
         * Returns true if the request contains conditions that save the server from sending a response that the client
         * already has locally. When a request is enqueued with its own conditions, the built-in response cache won't be
         * used.
         *
         * @param request The request to check for conditions.
         * @return {@code true} if the request has conditions.
         */
        private static boolean hasConditions(Request request) {
            return request.header(HTTP.IF_MODIFIED_SINCE) != null || request.header(HTTP.IF_NONE_MATCH) != null;
        }

        /**
         * Returns a strategy to satisfy {@code request} using the a cached response {@code response}.
         *
         * @return The computed cache strategy.
         */
        public CacheStrategy get() {
            CacheStrategy candidate = getCandidate();

            if (candidate.networkRequest != null && request.cacheControl().onlyIfCached()) {
                // We're forbidden from using the network and the cache is insufficient.
                return new CacheStrategy(null, null);
            }

            return candidate;
        }

        /**
         * Returns a strategy to use assuming the request can use the network.
         *
         * @return The candidate cache strategy.
         */
        private CacheStrategy getCandidate() {
            // No cached response.
            if (cacheResponse == null) {
                return new CacheStrategy(request, null);
            }

            // Drop the cached response if the handshake is missing.
            if (request.isHttps() && cacheResponse.handshake() == null) {
                return new CacheStrategy(request, null);
            }

            // If this response shouldn't have been stored, it shouldn't be used as a response source.
            // This check should be redundant as long as the persistence store is well-behaved and the rules
            // haven't changed since the response was cached.
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

            // Find a condition to add to the request. If the condition is satisfied, the response body will not be
            // transmitted.
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

            Headers.Builder conditionalRequestHeaders = request.headers().newBuilder();
            Internal.instance.addLenient(conditionalRequestHeaders, conditionName, conditionValue);

            Request conditionalRequest = request.newBuilder().headers(conditionalRequestHeaders.build()).build();
            return new CacheStrategy(conditionalRequest, cacheResponse);
        }

        /**
         * Returns the number of milliseconds that the response is fresh for, from the served date.
         *
         * @return The freshness lifetime in milliseconds.
         */
        private long computeFreshnessLifetime() {
            CacheControl responseCaching = cacheResponse.cacheControl();
            if (responseCaching.maxAgeSeconds() != -1) {
                return TimeUnit.SECONDS.toMillis(responseCaching.maxAgeSeconds());
            } else if (expires != null) {
                long servedMillis = servedDate != null ? servedDate.getTime() : receivedResponseMillis;
                long delta = expires.getTime() - servedMillis;
                return delta > 0 ? delta : 0;
            } else if (lastModified != null && cacheResponse.request().url().query() == null) {
                // As recommended by the HTTP RFC and implemented in Firefox, the max age of a document should be
                // defaulted to 10% of the document's age at the time it was served. Default expiration dates aren't
                // used for URIs containing a query.
                long servedMillis = servedDate != null ? servedDate.getTime() : sentRequestMillis;
                long delta = servedMillis - lastModified.getTime();
                return delta > 0 ? (delta / 10) : 0;
            }
            return 0;
        }

        /**
         * Returns the current age of the response, in milliseconds. The calculation is specified by RFC 7234, 4.2.3
         * Calculating Age.
         *
         * @return The age of the response in milliseconds.
         */
        private long cacheResponseAge() {
            long apparentReceivedAge = servedDate != null ? Math.max(0, receivedResponseMillis - servedDate.getTime())
                    : 0;
            long receivedAge = ageSeconds != -1 ? Math.max(apparentReceivedAge, TimeUnit.SECONDS.toMillis(ageSeconds))
                    : apparentReceivedAge;
            long responseDuration = receivedResponseMillis - sentRequestMillis;
            long residentDuration = nowMillis - receivedResponseMillis;
            return receivedAge + responseDuration + residentDuration;
        }

        /**
         * Returns true if computeFreshnessLifetime used a heuristic. If we use a heuristic to serve a cached response,
         * we must attach a warning header.
         *
         * @return {@code true} if a heuristic was used.
         */
        private boolean isFreshnessLifetimeHeuristic() {
            return cacheResponse.cacheControl().maxAgeSeconds() == -1 && expires == null;
        }
    }

}
