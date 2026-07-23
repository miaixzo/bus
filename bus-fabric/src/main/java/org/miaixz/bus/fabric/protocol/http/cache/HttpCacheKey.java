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
     * @param request request whose method, URL, and selected header values identify the entry
     * @param vary    response Vary header value, or null or blank when no request headers vary the entry
     * @return base key followed by a hash separator and the encoded Vary-selected request headers
     */
    static String key(final HttpRequest request, final String vary) {
        return baseKey(request) + Symbol.HASH + varyDigest(request, vary);
    }

    /**
     * Builds a base cache key without vary values.
     *
     * @param request request containing a non-null method and URL
     * @return method value and request URI joined with an at-sign separator
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
     * @param cached  cached response carrying the original request and Vary response headers
     * @param request current request whose headers are compared with the cached request
     * @return true when all header fields selected by the cached response's Vary metadata match
     */
    static boolean varyMatches(final HttpResponse cached, final HttpRequest request) {
        return HttpHeaders.varyMatches(cached.request().headers(), request.headers(), cached.headers());
    }

    /**
     * Returns whether a Vary header contains the wildcard marker.
     *
     * @param vary Vary header value, or null
     * @return true when a comma-delimited field is exactly the wildcard marker after trimming
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
     * @param vary    response Vary header value, or null or blank for an empty suffix
     * @return ordered, ampersand-delimited lowercase field names and corresponding request-header value lists
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
