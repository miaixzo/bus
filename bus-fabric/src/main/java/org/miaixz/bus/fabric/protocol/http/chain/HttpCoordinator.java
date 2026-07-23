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
package org.miaixz.bus.fabric.protocol.http.chain;

import java.util.Locale;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;
import org.miaixz.bus.fabric.protocol.http.body.PayloadBody;
import org.miaixz.bus.fabric.protocol.http.cache.HttpCache;
import org.miaixz.bus.logger.Logger;

/**
 * HTTP chain coordinator that resolves cache hits and writes cacheable network responses.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class HttpCoordinator implements HttpStage {

    /**
     * Normalized identifier exposed to the HTTP stage chain.
     */
    private final String name;

    /**
     * Cache used for lookup, validation, statistics, and writes, or {@code null} when disabled.
     */
    private final HttpCache cache;

    /**
     * Runtime clock used to evaluate cached response freshness.
     */
    private final Clock clock;

    /**
     * Creates a cache coordinator.
     *
     * @param cache cache implementation, or {@code null} for transparent disabled behavior
     * @param clock runtime time source used for freshness calculations
     */
    private HttpCoordinator(final HttpCache cache, final Clock clock) {
        this.name = normalizeName("http-cache");
        this.cache = cache;
        this.clock = require(clock, "Runtime clock");
    }

    /**
     * Creates a cache-enabled coordinator.
     *
     * @param cache non-null HTTP cache implementation
     * @param clock runtime time source used for freshness calculations
     * @return cache-enabled coordinator stage
     * @throws ValidateException if the cache or clock is {@code null}
     */
    public static HttpCoordinator create(final HttpCache cache, final Clock clock) {
        return new HttpCoordinator(require(cache, "HTTP cache"), clock);
    }

    /**
     * Creates a transparent disabled cache coordinator.
     *
     * @param clock runtime time source retained for consistent stage construction
     * @return coordinator that bypasses storage while honoring {@code only-if-cached}
     * @throws ValidateException if {@code clock} is {@code null}
     */
    public static HttpCoordinator disabled(final Clock clock) {
        return new HttpCoordinator(null, clock);
    }

    /**
     * Resolves cache before proceeding to the network stage.
     *
     * @param request request used for cache lookup and possible conditional validation
     * @param chain   remaining exchange chain used on a cache miss or revalidation
     * @return fresh cached response, synthetic 504, merged 304 result, or network response
     * @throws ValidateException if the request or chain is {@code null}
     */
    @Override
    public HttpResponse execute(final HttpRequest request, final HttpChain chain) {
        final HttpRequest current = require(request, "HTTP request");
        final HttpChain next = require(chain, "HTTP chain");
        if (cache == null) {
            if (!current.headers().contains(Http.Header.CACHE_CONTROL)
                    && !current.headers().contains(Http.Header.PRAGMA)) {
                return next.proceed(current);
            }
            if (current.cacheControl().onlyIfCached()) {
                return unsatisfiable(current);
            }
            return next.proceed(current);
        }
        final var cacheControl = current.cacheControl();
        final boolean debug = Logger.isDebugEnabled();
        cache.recordRequest();
        if (debug) {
            Logger.debug(
                    true,
                    "Fabric",
                    "HTTP cache coordinator lookup started: method={}, host={}, port={}, path={}",
                    current.method().value(),
                    current.url().host(),
                    current.url().port(),
                    current.url().path());
        }
        final HttpResponse cached = cache.get(current);
        if (cached != null && cache.fresh(current, cached, clock)) {
            cache.recordHit();
            if (debug) {
                Logger.debug(
                        false,
                        "Fabric",
                        "HTTP cache coordinator fresh hit: code={}, method={}, host={}, port={}",
                        cached.code(),
                        current.method().value(),
                        current.url().host(),
                        current.url().port());
            }
            return cached.toBuilder().request(current).cacheResponse(cached).build();
        }
        if (cacheControl.onlyIfCached()) {
            if (cached != null) {
                cached.close();
            }
            if (debug) {
                Logger.debug(
                        false,
                        "Fabric",
                        "HTTP cache coordinator returning unsatisfiable response: reason=only-if-cached-miss");
            }
            return unsatisfiable(current);
        }
        final HttpRequest networkRequest = cached == null || cacheControl.noCache() ? current
                : cache.conditional(current, cached);
        if (debug) {
            Logger.debug(
                    false,
                    "Fabric",
                    "HTTP cache coordinator proceeding to network: cachedPresent={}, conditional={}, noCache={}, "
                            + "method={}, host={}, port={}",
                    cached != null,
                    networkRequest != current,
                    cacheControl.noCache(),
                    current.method().value(),
                    current.url().host(),
                    current.url().port());
        }
        cache.recordNetwork();
        final HttpResponse network = next.proceed(networkRequest);
        if (debug) {
            Logger.debug(
                    false,
                    "Fabric",
                    "HTTP cache coordinator network response: code={}, conditional={}",
                    network.code(),
                    networkRequest != current);
        }
        if (cached != null && network.code() == Http.Status.NOT_MODIFIED) {
            final HttpResponse merged = cache.update(cached, network);
            final HttpResponse updated = merged.request() == current ? merged
                    : merged.toBuilder().request(current).build();
            network.close();
            if (debug) {
                Logger.debug(
                        false,
                        "Fabric",
                        "HTTP cache coordinator merged conditional response: code={}",
                        updated.code());
            }
            return write(current, updated, debug);
        }
        if (cached != null) {
            final HttpResponse response = network.toBuilder().request(current).cacheResponse(cached)
                    .networkResponse(network).build();
            cached.close();
            if (debug) {
                Logger.debug(
                        false,
                        "Fabric",
                        "HTTP cache coordinator replaced stale response: code={}",
                        response.code());
            }
            return write(current, response, debug);
        }
        return write(current, network.toBuilder().request(current).networkResponse(network).build(), debug);
    }

    /**
     * Returns a 504 response for unsatisfied only-if-cached requests.
     *
     * @param request request that cannot be satisfied without network access
     * @return synthetic empty-body 504 response associated with the request
     */
    private static HttpResponse unsatisfiable(final HttpRequest request) {
        return HttpResponse.builder().request(request).code(Http.Status.GATEWAY_TIMEOUT)
                .message("Unsatisfiable Request (only-if-cached)").body(PayloadBody.empty()).build();
    }

    /**
     * Returns stage name.
     *
     * @return normalized cache-coordinator stage identifier
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Writes a cacheable response while preserving a readable response for callers.
     *
     * @param request  original request used as the cache key
     * @param response caller-visible response considered for storage
     * @param debug    whether cache-decision diagnostics are logged
     * @return original response when uncacheable, otherwise the readable response returned by the cache writer
     */
    private HttpResponse write(final HttpRequest request, final HttpResponse response, final boolean debug) {
        if (!cache.cacheable(request, response)) {
            cache.remove(request);
            if (debug) {
                Logger.debug(
                        false,
                        "Fabric",
                        "HTTP cache coordinator write skipped: code={}, cacheable={}",
                        response.code(),
                        false);
            }
            return response;
        }
        if (debug) {
            Logger.debug(
                    false,
                    "Fabric",
                    "HTTP cache coordinator write delegated: code={}, cacheable={}",
                    response.code(),
                    true);
        }
        return cache.write(request, response);
    }

    /**
     * Normalizes a stage name.
     *
     * @param value stage identifier to validate and normalize
     * @return trimmed lowercase stage identifier
     * @throws ValidateException if the identifier is blank or contains a line break
     */
    private static String normalizeName(final String value) {
        Assert.isFalse(
                StringKit.isBlank(value) || StringKit.containsAny(value, Symbol.C_CR, Symbol.C_LF),
                () -> new ValidateException("HTTP cache name must be non-blank and single-line"));
        return StringKit.trim(value).toLowerCase(Locale.ROOT);
    }

    /**
     * Validates required references.
     *
     * @param value reference to validate
     * @param name  logical field name included in the validation error
     * @param <T>   reference type
     * @return validated non-null reference
     * @throws ValidateException if {@code value} is {@code null}
     */
    private static <T> T require(final T value, final String name) {
        if (value == null) {
            throw new ValidateException(name + " must not be null");
        }
        return value;
    }

}
