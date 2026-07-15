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
package org.miaixz.bus.fabric.protocol.http.cache;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Pure HTTP cache policy.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class CachePolicy {

    /**
     * Creates a policy.
     */
    private CachePolicy() {
        // No initialization required.
    }

    /**
     * Returns the default policy.
     *
     * @return policy
     */
    public static CachePolicy defaults() {
        return Instances.get(CachePolicy.class.getName() + ".defaults", CachePolicy::new);
    }

    /**
     * Returns whether a response may be cached.
     *
     * @param request  request
     * @param response response
     * @return true when cacheable
     */
    public boolean cacheable(final HttpRequest request, final HttpResponse response) {
        require(request, "HTTP request");
        require(response, "HTTP response");
        final HttpCacheControl requestControl = request.cacheControl();
        final HttpCacheControl responseControl = response.cacheControl();
        if (request.method() != HTTP.Method.GET && request.method() != HTTP.Method.HEAD) {
            return false;
        }
        if (!Builder.CACHE_POLICY_CACHEABLE.contains(response.code())
                && !explicitlyCacheable(response, responseControl)) {
            return false;
        }
        if (HttpCacheKey.varyStar(response.headers().get(HTTP.VARY))) {
            return false;
        }
        return !requestControl.noStore() && !responseControl.noStore();
    }

    /**
     * Returns whether a response is fresh.
     *
     * @param response response
     * @param clock    clock
     * @return true when fresh
     */
    public boolean fresh(final HttpResponse response, final Clock clock) {
        require(response, "HTTP response");
        return fresh(response.request(), response, clock);
    }

    /**
     * Returns whether a cached response is fresh enough for a request.
     *
     * @param request  request
     * @param response response
     * @param clock    clock
     * @return true when fresh
     */
    public boolean fresh(final HttpRequest request, final HttpResponse response, final Clock clock) {
        require(request, "HTTP request");
        require(response, "HTTP response");
        require(clock, "Runtime clock");
        final HttpCacheControl requestControl = request.cacheControl();
        final HttpCacheControl responseControl = response.cacheControl();
        if (requestControl.noCache() || responseControl.noCache() || responseControl.noStore()) {
            return false;
        }
        final Instant now = clock.now();
        final Instant date = headerInstant(response.headers(), HTTP.DATE);
        final long age = currentAgeSeconds(response.headers(), date, now);
        long lifetime = freshnessLifetime(response, responseControl, date, now);
        if (responseControl.immutable()) {
            return true;
        }
        if (requestControl.maxAgeSeconds() >= 0) {
            lifetime = Math.min(lifetime, requestControl.maxAgeSeconds());
        }
        final long minFresh = Math.max(0L, requestControl.minFreshSeconds());
        final long maxStale = responseControl.mustRevalidate() || requestControl.maxStaleSeconds() < 0 ? 0L
                : requestControl.maxStaleSeconds();
        return age + minFresh < lifetime + maxStale;
    }

    /**
     * Returns whether response headers explicitly allow caching.
     *
     * @param response response
     * @param control  cache control
     * @return true when explicit
     */
    private static boolean explicitlyCacheable(final HttpResponse response, final HttpCacheControl control) {
        return control.isPublic() || control.isPrivate() || control.maxAgeSeconds() >= 0
                || control.sMaxAgeSeconds() >= 0 || control.immutable() || response.headers().get(HTTP.EXPIRES) != null;
    }

    /**
     * Returns response freshness lifetime.
     *
     * @param response response
     * @param control  cache control
     * @param date     Date header instant or null
     * @param now      current time
     * @return lifetime seconds
     */
    private static long freshnessLifetime(
            final HttpResponse response,
            final HttpCacheControl control,
            final Instant date,
            final Instant now) {
        if (control.sMaxAgeSeconds() >= 0) {
            return control.sMaxAgeSeconds();
        }
        if (control.maxAgeSeconds() >= 0) {
            return control.maxAgeSeconds();
        }
        final Instant expires = headerInstant(response.headers(), HTTP.EXPIRES);
        if (expires == null) {
            return 0L;
        }
        final Instant base = date == null ? now : date;
        return Math.max(0L, Duration.between(base, expires).getSeconds());
    }

    /**
     * Creates a conditional request.
     *
     * @param request request
     * @param cached  cached response
     * @return request
     */
    public HttpRequest conditional(final HttpRequest request, final HttpResponse cached) {
        require(request, "HTTP request");
        require(cached, "Cached response");
        final String etag = cached.headers().get(HTTP.ETAG);
        if (etag != null) {
            return copy(request, request.headers().with(HTTP.IF_NONE_MATCH, etag));
        }
        final String modified = cached.headers().get(HTTP.LAST_MODIFIED);
        if (modified != null) {
            return copy(request, request.headers().with(HTTP.IF_MODIFIED_SINCE, modified));
        }
        return request;
    }

    /**
     * Copies a request with headers.
     *
     * @param request request
     * @param headers headers
     * @return copy
     */
    private static HttpRequest copy(final HttpRequest request, final Headers headers) {
        return request.toBuilder().headers(headers).build();
    }

    /**
     * Returns header instant.
     *
     * @param headers headers
     * @param name    name
     * @return instant or null
     */
    private static Instant headerInstant(final Headers headers, final String name) {
        final String value = headers.get(name);
        if (value == null) {
            return null;
        }
        try {
            return ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
        } catch (final DateTimeParseException e) {
            throw new ProtocolException("Invalid HTTP cache date", e);
        }
    }

    /**
     * Returns age seconds.
     *
     * @param headers headers
     * @return age seconds
     */
    private static long ageSeconds(final Headers headers) {
        final String value = headers.get(HTTP.AGE);
        if (value == null) {
            return 0L;
        }
        try {
            final long seconds = Long.parseLong(value);
            if (seconds < 0 || Duration.ofSeconds(seconds).isNegative()) {
                throw new ProtocolException("HTTP Age must be non-negative");
            }
            return seconds;
        } catch (final NumberFormatException e) {
            throw new ProtocolException("Invalid HTTP Age", e);
        }
    }

    /**
     * Returns current response age.
     *
     * @param headers headers
     * @param date    Date header instant or null
     * @param now     current time
     * @return age seconds
     */
    private static long currentAgeSeconds(final Headers headers, final Instant date, final Instant now) {
        final long apparentAge = date == null ? 0L : Math.max(0L, Duration.between(date, now).getSeconds());
        return Math.max(apparentAge, ageSeconds(headers));
    }

    /**
     * Validates required value.
     *
     * @param value value
     * @param name  name
     * @param <T>   type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
