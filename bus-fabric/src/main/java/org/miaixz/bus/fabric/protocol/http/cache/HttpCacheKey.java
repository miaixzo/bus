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

import java.util.ArrayList;
import java.util.Locale;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.protocol.http.HttpHeaders;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Cache key and vary matching rules for HTTP cache entries.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class HttpCacheKey {

    /**
     * Hidden constructor for cache key helpers.
     */
    private HttpCacheKey() {
        // No initialization required.
    }

    /**
     * Builds a full cache key.
     *
     * @param request request
     * @param vary    vary header
     * @return key
     */
    static String key(final HttpRequest request, final String vary) {
        return baseKey(request) + Symbol.HASH + varyDigest(request, vary);
    }

    /**
     * Builds a base cache key without vary values.
     *
     * @param request request
     * @return key prefix
     */
    static String baseKey(final HttpRequest request) {
        Assert.isTrue(
                request.method() != null && request.url() != null,
                () -> new ValidateException("HTTP request must contain method and URL"));
        return request.method().value() + Symbol.AT + request.url().toUri();
    }

    /**
     * Returns whether vary headers in the cached response match the current request.
     *
     * @param cached  cached response
     * @param request current request
     * @return true when the cached response can satisfy the request
     */
    static boolean varyMatches(final HttpResponse cached, final HttpRequest request) {
        return HttpHeaders.varyMatches(cached.request().headers(), request.headers(), cached.headers());
    }

    /**
     * Returns whether a Vary header contains the wildcard marker.
     *
     * @param vary Vary header value
     * @return true when wildcard Vary is present
     */
    static boolean varyStar(final String vary) {
        if (vary == null || vary.isBlank()) {
            return false;
        }
        for (final String name : vary.split(Symbol.COMMA)) {
            if (Symbol.STAR.equals(name.trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Encodes the request headers named by {@code Vary} into the cache key suffix.
     *
     * @param request request being cached or looked up
     * @param vary    response Vary header
     * @return deterministic vary suffix
     */
    private static String varyDigest(final HttpRequest request, final String vary) {
        if (vary == null || vary.isBlank()) {
            return Normal.EMPTY;
        }
        final ArrayList<String> parts = new ArrayList<>();
        for (final String name : vary.split(Symbol.COMMA)) {
            final String normalized = name.trim().toLowerCase(Locale.ROOT);
            if (Symbol.STAR.equals(normalized)) {
                throw new ProtocolException("Vary star is not cacheable");
            }
            parts.add(normalized + Symbol.EQUAL + request.headers().values(normalized));
        }
        return String.join(Symbol.AND, parts);
    }

}
