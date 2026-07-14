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
import org.miaixz.bus.core.net.HTTP;
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
     * Logger tag used by the fabric runtime.
     */
    private static final String LOG_TAG = "Fabric";

    /**
     * Stage name.
     */
    private final String name;

    /**
     * Optional cache.
     */
    private final HttpCache cache;

    /**
     * Runtime clock.
     */
    private final Clock clock;

    /**
     * Creates a cache coordinator.
     *
     * @param cache cache or null when disabled
     * @param clock runtime clock
     */
    private HttpCoordinator(final HttpCache cache, final Clock clock) {
        this.name = normalizeName("http-cache");
        this.cache = cache;
        this.clock = require(clock, "Runtime clock");
    }

    /**
     * Creates a cache-enabled coordinator.
     *
     * @param cache cache
     * @param clock runtime clock
     * @return stage
     */
    public static HttpCoordinator create(final HttpCache cache, final Clock clock) {
        return new HttpCoordinator(require(cache, "HTTP cache"), clock);
    }

    /**
     * Creates a transparent disabled cache coordinator.
     *
     * @param clock runtime clock
     * @return stage
     */
    public static HttpCoordinator disabled(final Clock clock) {
        return new HttpCoordinator(null, clock);
    }

    /**
     * Resolves cache before proceeding to the network stage.
     *
     * @param request request
     * @param chain   chain
     * @return response
     */
    @Override
    public HttpResponse execute(final HttpRequest request, final HttpChain chain) {
        final HttpRequest current = require(request, "HTTP request");
        final HttpChain next = require(chain, "HTTP chain");
        if (cache == null) {
            Logger.debug(
                    false,
                    LOG_TAG,
                    "HTTP cache coordinator disabled: method={}, host={}, port={}, path={}",
                    current.method().value(),
                    current.url().host(),
                    current.url().port(),
                    current.url().path());
            if (current.cacheControl().onlyIfCached()) {
                Logger.debug(
                        false,
                        LOG_TAG,
                        "HTTP cache coordinator returning unsatisfiable response: "
                                + "reason=cache-disabled-only-if-cached");
                return unsatisfiable(current);
            }
            return next.proceed(current);
        }
        cache.recordRequest();
        Logger.debug(
                true,
                LOG_TAG,
                "HTTP cache coordinator lookup started: method={}, host={}, port={}, path={}",
                current.method().value(),
                current.url().host(),
                current.url().port(),
                current.url().path());
        final HttpResponse cached = cache.get(current);
        if (cached != null && cache.fresh(current, cached, clock)) {
            cache.recordHit();
            Logger.debug(
                    false,
                    LOG_TAG,
                    "HTTP cache coordinator fresh hit: code={}, method={}, host={}, port={}",
                    cached.code(),
                    current.method().value(),
                    current.url().host(),
                    current.url().port());
            return cached.toBuilder().request(current).cacheResponse(cached).build();
        }
        if (current.cacheControl().onlyIfCached()) {
            if (cached != null) {
                cached.close();
            }
            Logger.debug(
                    false,
                    LOG_TAG,
                    "HTTP cache coordinator returning unsatisfiable response: reason=only-if-cached-miss");
            return unsatisfiable(current);
        }
        final HttpRequest networkRequest = cached == null || current.cacheControl().noCache() ? current
                : cache.conditional(current, cached);
        Logger.debug(
                false,
                LOG_TAG,
                "HTTP cache coordinator proceeding to network: cachedPresent={}, conditional={}, noCache={}, "
                        + "method={}, host={}, port={}",
                cached != null,
                networkRequest != current,
                current.cacheControl().noCache(),
                current.method().value(),
                current.url().host(),
                current.url().port());
        cache.recordNetwork();
        final HttpResponse network = next.proceed(networkRequest);
        Logger.debug(
                false,
                LOG_TAG,
                "HTTP cache coordinator network response: code={}, conditional={}",
                network.code(),
                networkRequest != current);
        if (cached != null && network.code() == HTTP.HTTP_NOT_MODIFIED) {
            final HttpResponse updated = cache.update(cached, network).toBuilder().request(current)
                    .cacheResponse(cached).networkResponse(network).build();
            network.close();
            Logger.debug(false, LOG_TAG, "HTTP cache coordinator merged conditional response: code={}", updated.code());
            return write(current, updated);
        }
        if (cached != null) {
            final HttpResponse response = network.toBuilder().request(current).cacheResponse(cached)
                    .networkResponse(network).build();
            cached.close();
            Logger.debug(false, LOG_TAG, "HTTP cache coordinator replaced stale response: code={}", response.code());
            return write(current, response);
        }
        return write(current, network.toBuilder().request(current).networkResponse(network).build());
    }

    /**
     * Returns a 504 response for unsatisfied only-if-cached requests.
     *
     * @param request request
     * @return response
     */
    private static HttpResponse unsatisfiable(final HttpRequest request) {
        return HttpResponse.builder().request(request).code(HTTP.HTTP_GATEWAY_TIMEOUT)
                .message("Unsatisfiable Request (only-if-cached)").body(PayloadBody.empty()).build();
    }

    /**
     * Returns stage name.
     *
     * @return stage name
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Writes a cacheable response while preserving a readable response for callers.
     *
     * @param request  request
     * @param response network response
     * @return caller response
     */
    private HttpResponse write(final HttpRequest request, final HttpResponse response) {
        if (!cache.cacheable(request, response)) {
            cache.remove(request);
            Logger.debug(
                    false,
                    LOG_TAG,
                    "HTTP cache coordinator write skipped: code={}, cacheable={}",
                    response.code(),
                    false);
            return response;
        }
        Logger.debug(
                false,
                LOG_TAG,
                "HTTP cache coordinator write delegated: code={}, cacheable={}",
                response.code(),
                true);
        return cache.write(request, response);
    }

    /**
     * Normalizes a stage name.
     *
     * @param value value
     * @return normalized name
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
     * @param value value
     * @param name  field name
     * @param <T>   type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
