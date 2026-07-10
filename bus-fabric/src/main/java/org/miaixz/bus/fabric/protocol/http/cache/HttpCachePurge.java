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

import java.util.function.Consumer;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.fabric.cache.CacheStore;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;

/**
 * Cache invalidation helpers for HTTP cache entries.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class HttpCachePurge {

    /**
     * Hidden constructor for cache invalidation helpers.
     */
    private HttpCachePurge() {
        // No initialization required.
    }

    /**
     * Removes all vary variants for one request.
     *
     * @param store   cache store
     * @param request request
     * @param removed removed key callback
     */
    static void remove(final CacheStore store, final HttpRequest request, final Consumer<String> removed) {
        final String prefix = HttpCacheKey.baseKey(request);
        final String uri = request.url().toUri().toString();
        final boolean unsafe = request.method() != HTTP.Method.GET && request.method() != HTTP.Method.HEAD;
        final var keys = store.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            if (key.startsWith(prefix) || (unsafe && sameUri(key, uri))) {
                removeKey(store, key, removed);
            }
        }
    }

    /**
     * Removes one cache key.
     *
     * @param store   cache store
     * @param key     cache key
     * @param removed removed key callback
     */
    static void removeKey(final CacheStore store, final String key, final Consumer<String> removed) {
        store.remove(key);
        removed.accept(key);
    }

    /**
     * Returns whether a cache key belongs to the supplied URI regardless of method.
     *
     * @param key cache key
     * @param uri request URI
     * @return true when matching
     */
    private static boolean sameUri(final String key, final String uri) {
        final int method = key.indexOf(Symbol.C_AT);
        final int vary = key.indexOf(Symbol.C_HASH, method + 1);
        return method >= 0 && vary > method && uri.equals(key.substring(method + 1, vary));
    }

}
