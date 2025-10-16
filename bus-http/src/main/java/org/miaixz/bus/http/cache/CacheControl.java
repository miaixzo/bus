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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.http.Headers;

import java.util.concurrent.TimeUnit;

/**
 * A cache control header with caching directives from a server or client. These directives set the policy for which
 * responses can be stored, and which requests can be satisfied by a stored response.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheControl {

    /**
     * A cache control request directive that requires a network validation for the response. Note that the cache may
     * assist these requests with conditional GET requests.
     */
    public static final CacheControl FORCE_NETWORK = new Builder().noCache().build();

    /**
     * A cache control request directive that uses only the cache, even if the cached response is stale. If the response
     * is not available in the cache or requires server validation, the call will fail.
     */
    public static final CacheControl FORCE_CACHE = new Builder().onlyIfCached()
            .maxStale(Integer.MAX_VALUE, TimeUnit.SECONDS).build();

    /**
     * In a request, this means that a cache should not be used to satisfy the request.
     */
    private final boolean noCache;
    /**
     * If true, this response should not be cached.
     */
    private final boolean noStore;
    /**
     * The duration past the response's served date that it can be served without validation.
     */
    private final int maxAgeSeconds;
    /**
     * The "s-maxage" directive is the max age for shared caches. Not to be confused with "max-age" for non-shared
     * caches, this directive is not honored by this cache.
     */
    private final int sMaxAgeSeconds;
    private final boolean isPrivate;
    private final boolean isPublic;
    private final boolean mustRevalidate;
    private final int maxStaleSeconds;
    private final int minFreshSeconds;
    /**
     * The "only-if-cached" directive is misleading. It actually means "do not use the network". It is set by a client
     * who only wants to be served from the cache. Cached responses will require validation (i.e. conditional GETs are
     * not permitted if this header is set).
     */
    private final boolean onlyIfCached;
    private final boolean noTransform;
    private final boolean immutable;

    String headerValue;

    private CacheControl(boolean noCache, boolean noStore, int maxAgeSeconds, int sMaxAgeSeconds, boolean isPrivate,
            boolean isPublic, boolean mustRevalidate, int maxStaleSeconds, int minFreshSeconds, boolean onlyIfCached,
            boolean noTransform, boolean immutable, String headerValue) {
        this.noCache = noCache;
        this.noStore = noStore;
        this.maxAgeSeconds = maxAgeSeconds;
        this.sMaxAgeSeconds = sMaxAgeSeconds;
        this.isPrivate = isPrivate;
        this.isPublic = isPublic;
        this.mustRevalidate = mustRevalidate;
        this.maxStaleSeconds = maxStaleSeconds;
        this.minFreshSeconds = minFreshSeconds;
        this.onlyIfCached = onlyIfCached;
        this.noTransform = noTransform;
        this.immutable = immutable;
        this.headerValue = headerValue;
    }

    CacheControl(Builder builder) {
        this.noCache = builder.noCache;
        this.noStore = builder.noStore;
        this.maxAgeSeconds = builder.maxAgeSeconds;
        this.sMaxAgeSeconds = -1;
        this.isPrivate = false;
        this.isPublic = false;
        this.mustRevalidate = false;
        this.maxStaleSeconds = builder.maxStaleSeconds;
        this.minFreshSeconds = builder.minFreshSeconds;
        this.onlyIfCached = builder.onlyIfCached;
        this.noTransform = builder.noTransform;
        this.immutable = builder.immutable;
    }

    /**
     * Returns the cache directives of {@code headers}. If both Cache-Control and Pragma headers are present, they are
     * merged.
     *
     * @param headers The headers to parse.
     * @return The cache control header.
     */
    public static CacheControl parse(Headers headers) {
        boolean noCache = false;
        boolean noStore = false;
        int maxAgeSeconds = -1;
        int sMaxAgeSeconds = -1;
        boolean isPrivate = false;
        boolean isPublic = false;
        boolean mustRevalidate = false;
        int maxStaleSeconds = -1;
        int minFreshSeconds = -1;
        boolean onlyIfCached = false;
        boolean noTransform = false;
        boolean immutable = false;

        boolean canUseHeaderValue = true;
        String headerValue = null;

        for (int i = 0, size = headers.size(); i < size; i++) {
            String name = headers.name(i);
            String value = headers.value(i);

            if (name.equalsIgnoreCase(HTTP.CACHE_CONTROL)) {
                if (headerValue != null) {
                    // Multiple Cache-Control headers means we can't use the raw value.
                    canUseHeaderValue = false;
                } else {
                    headerValue = value;
                }
            } else if (name.equalsIgnoreCase("Pragma")) {
                // Might specify additional cache-control parameters. We invalidate the raw header value.
                canUseHeaderValue = false;
            } else {
                continue;
            }

            int pos = 0;
            while (pos < value.length()) {
                int tokenStart = pos;
                pos = Headers.skipUntil(value, pos, "=,;");
                String directive = value.substring(tokenStart, pos).trim();
                String parameter;

                if (pos == value.length() || value.charAt(pos) == Symbol.C_COMMA
                        || value.charAt(pos) == Symbol.C_SEMICOLON) {
                    pos++; // Consume ',' or ';' (if necessary).
                    parameter = null;
                } else {
                    pos++; // Consume '='.
                    pos = Headers.skipWhitespace(value, pos);

                    // Quoted string.
                    if (pos < value.length() && value.charAt(pos) == '\"') {
                        pos++; // Consume '"' open quote.
                        int parameterStart = pos;
                        pos = Headers.skipUntil(value, pos, "\"");
                        parameter = value.substring(parameterStart, pos);
                        pos++; // Consume '"' close quote (if necessary).
                    } else {
                        // Unquoted string.
                        int parameterStart = pos;
                        pos = Headers.skipUntil(value, pos, ",;");
                        parameter = value.substring(parameterStart, pos).trim();
                    }
                }

                if ("no-cache".equalsIgnoreCase(directive)) {
                    noCache = true;
                } else if ("no-store".equalsIgnoreCase(directive)) {
                    noStore = true;
                } else if ("max-age".equalsIgnoreCase(directive)) {
                    maxAgeSeconds = Headers.parseSeconds(parameter, -1);
                } else if ("s-maxage".equalsIgnoreCase(directive)) {
                    sMaxAgeSeconds = Headers.parseSeconds(parameter, -1);
                } else if ("private".equalsIgnoreCase(directive)) {
                    isPrivate = true;
                } else if ("public".equalsIgnoreCase(directive)) {
                    isPublic = true;
                } else if ("must-revalidate".equalsIgnoreCase(directive)) {
                    mustRevalidate = true;
                } else if ("max-stale".equalsIgnoreCase(directive)) {
                    maxStaleSeconds = Headers.parseSeconds(parameter, Integer.MAX_VALUE);
                } else if ("min-fresh".equalsIgnoreCase(directive)) {
                    minFreshSeconds = Headers.parseSeconds(parameter, -1);
                } else if ("only-if-cached".equalsIgnoreCase(directive)) {
                    onlyIfCached = true;
                } else if ("no-transform".equalsIgnoreCase(directive)) {
                    noTransform = true;
                } else if ("immutable".equalsIgnoreCase(directive)) {
                    immutable = true;
                }
            }
        }

        if (!canUseHeaderValue) {
            headerValue = null;
        }
        return new CacheControl(noCache, noStore, maxAgeSeconds, sMaxAgeSeconds, isPrivate, isPublic, mustRevalidate,
                maxStaleSeconds, minFreshSeconds, onlyIfCached, noTransform, immutable, headerValue);
    }

    /**
     * Returns true if this cache control forbids caching of any kind.
     *
     * @return {@code true} if caching is forbidden.
     */
    public boolean noCache() {
        return noCache;
    }

    /**
     * Returns true if this cache control forbids storing the response in any cache.
     *
     * @return {@code true} if storing is forbidden.
     */
    public boolean noStore() {
        return noStore;
    }

    /**
     * Returns the maximum age of a cached response in seconds.
     *
     * @return The max age in seconds.
     */
    public int maxAgeSeconds() {
        return maxAgeSeconds;
    }

    /**
     * Returns the "s-maxage" directive, which is the max age for shared caches.
     *
     * @return The s-maxage in seconds.
     */
    public int sMaxAgeSeconds() {
        return sMaxAgeSeconds;
    }

    /**
     * Returns true if this response should not be cached by a shared cache.
     *
     * @return {@code true} if this response is private.
     */
    public boolean isPrivate() {
        return isPrivate;
    }

    /**
     * Returns true if this response may be cached by any cache.
     *
     * @return {@code true} if this response is public.
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * Returns true if the cache must revalidate the response with the origin server before using it.
     *
     * @return {@code true} if revalidation is required.
     */
    public boolean mustRevalidate() {
        return mustRevalidate;
    }

    /**
     * Returns the maximum staleness of a cached response in seconds.
     *
     * @return The max stale in seconds.
     */
    public int maxStaleSeconds() {
        return maxStaleSeconds;
    }

    /**
     * Returns the minimum freshness of a cached response in seconds.
     *
     * @return The min fresh in seconds.
     */
    public int minFreshSeconds() {
        return minFreshSeconds;
    }

    /**
     * Returns true if the cache should only use the cached response and not use the network.
     *
     * @return {@code true} if only cached responses should be used.
     */
    public boolean onlyIfCached() {
        return onlyIfCached;
    }

    /**
     * Returns true if the cache should not transform the response.
     *
     * @return {@code true} if transformations are forbidden.
     */
    public boolean noTransform() {
        return noTransform;
    }

    /**
     * Returns true if the response is immutable.
     *
     * @return {@code true} if the response is immutable.
     */
    public boolean immutable() {
        return immutable;
    }

    @Override
    public String toString() {
        String result = headerValue;
        return null != result ? result : (headerValue = headerValue());
    }

    private String headerValue() {
        StringBuilder result = new StringBuilder();
        if (noCache)
            result.append("no-cache, ");
        if (noStore)
            result.append("no-store, ");
        if (maxAgeSeconds != -1)
            result.append("max-age=").append(maxAgeSeconds).append(", ");
        if (sMaxAgeSeconds != -1)
            result.append("s-maxage=").append(sMaxAgeSeconds).append(", ");
        if (isPrivate)
            result.append("private, ");
        if (isPublic)
            result.append("public, ");
        if (mustRevalidate)
            result.append("must-revalidate, ");
        if (maxStaleSeconds != -1)
            result.append("max-stale=").append(maxStaleSeconds).append(", ");
        if (minFreshSeconds != -1)
            result.append("min-fresh=").append(minFreshSeconds).append(", ");
        if (onlyIfCached)
            result.append("only-if-cached, ");
        if (noTransform)
            result.append("no-transform, ");
        if (immutable)
            result.append("immutable, ");
        if (result.length() == 0)
            return Normal.EMPTY;
        result.delete(result.length() - 2, result.length());
        return result.toString();
    }

    /**
     * A builder for creating {@code Cache-Control} request headers.
     */
    public static final class Builder {

        boolean noCache;
        boolean noStore;
        int maxAgeSeconds = -1;
        int maxStaleSeconds = -1;
        int minFreshSeconds = -1;
        boolean onlyIfCached;
        boolean noTransform;
        boolean immutable;

        /**
         * Do not accept a cached response without validation.
         *
         * @return this builder.
         */
        public Builder noCache() {
            this.noCache = true;
            return this;
        }

        /**
         * Do not store the server's response in any cache.
         *
         * @return this builder.
         */
        public Builder noStore() {
            this.noStore = true;
            return this;
        }

        /**
         * Sets the maximum age of a cached response. If the cached response is older than {@code maxAge}, it will not
         * be used and a network request will be made.
         *
         * @param maxAge   a non-negative integer. It is stored and transmitted with {@link TimeUnit#SECONDS} precision;
         *                 finer precision will be lost.
         * @param timeUnit the unit of {@code maxAge}.
         * @return this builder.
         */
        public Builder maxAge(int maxAge, TimeUnit timeUnit) {
            if (maxAge < 0)
                throw new IllegalArgumentException("maxAge < 0: " + maxAge);
            long maxAgeSecondsLong = timeUnit.toSeconds(maxAge);
            this.maxAgeSeconds = maxAgeSecondsLong > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) maxAgeSecondsLong;
            return this;
        }

        /**
         * Accept cached responses that have exceeded their freshness lifetime by at most {@code maxStale}. If
         * unspecified, stale cached responses will not be used.
         *
         * @param maxStale a non-negative integer. It is stored and transmitted with {@link TimeUnit#SECONDS} precision;
         *                 finer precision will be lost.
         * @param timeUnit the unit of {@code maxStale}.
         * @return this builder.
         */
        public Builder maxStale(int maxStale, TimeUnit timeUnit) {
            if (maxStale < 0)
                throw new IllegalArgumentException("maxStale < 0: " + maxStale);
            long maxStaleSecondsLong = timeUnit.toSeconds(maxStale);
            this.maxStaleSeconds = maxStaleSecondsLong > Integer.MAX_VALUE ? Integer.MAX_VALUE
                    : (int) maxStaleSecondsLong;
            return this;
        }

        /**
         * Sets the minimum number of seconds that a response will continue to be fresh for. If the response will be
         * stale by the time it is received, a network request will be made.
         *
         * @param minFresh a non-negative integer. It is stored and transmitted with {@link TimeUnit#SECONDS} precision;
         *                 finer precision will be lost.
         * @param timeUnit the unit of {@code minFresh}.
         * @return this builder.
         */
        public Builder minFresh(int minFresh, TimeUnit timeUnit) {
            if (minFresh < 0)
                throw new IllegalArgumentException("minFresh < 0: " + minFresh);
            long minFreshSecondsLong = timeUnit.toSeconds(minFresh);
            this.minFreshSeconds = minFreshSecondsLong > Integer.MAX_VALUE ? Integer.MAX_VALUE
                    : (int) minFreshSecondsLong;
            return this;
        }

        /**
         * Only accept the response from the cache.
         *
         * @return this builder.
         */
        public Builder onlyIfCached() {
            this.onlyIfCached = true;
            return this;
        }

        /**
         * Do not accept a transformed response.
         *
         * @return this builder.
         */
        public Builder noTransform() {
            this.noTransform = true;
            return this;
        }

        /**
         * Indicates that the response will not be updated while it's fresh.
         *
         * @return this builder.
         */
        public Builder immutable() {
            this.immutable = true;
            return this;
        }

        /**
         * Builds a new {@link CacheControl} instance.
         *
         * @return a new {@link CacheControl} instance.
         */
        public CacheControl build() {
            return new CacheControl(this);
        }
    }

}
