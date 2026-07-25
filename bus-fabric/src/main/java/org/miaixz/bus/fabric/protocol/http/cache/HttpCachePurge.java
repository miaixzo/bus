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

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
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
     * Removes every key with the request base-key prefix and, for unsafe methods, every method variant for the same
     * serialized URI.
     *
     * @param store   cache store whose key snapshot is scanned
     * @param request request defining the base key, URI, and safe-method behavior
     * @param removed callback invoked after each selected key is removed
     * @throws ValidateException if a required argument is {@code null}
     */
    static void remove(final CacheStore store, final HttpRequest request, final Consumer<String> removed) {
        final CacheStore cacheStore = require(store, "Cache store");
        final HttpRequest targetRequest = require(request, "HTTP request");
        final Consumer<String> removeListener = require(removed, "Removed key callback");
        final String prefix = HttpCacheKey.baseKey(targetRequest);
        final String uri = targetRequest.url().toUri().toString();
        final boolean unsafe = targetRequest.method() != Http.Method.GET && targetRequest.method() != Http.Method.HEAD;
        final var keys = cacheStore.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            if (key.startsWith(prefix) || (unsafe && sameUri(key, uri))) {
                removeKey(cacheStore, key, removeListener);
            }
        }
    }

    /**
     * Removes one cache key and then reports that key to a callback.
     *
     * @param store   cache store from which the key is removed
     * @param key     exact cache key passed to the store and callback
     * @param removed callback invoked after the store removal returns
     * @throws ValidateException if a required argument is {@code null}
     */
    static void removeKey(final CacheStore store, final String key, final Consumer<String> removed) {
        final CacheStore cacheStore = require(store, "Cache store");
        final String cacheKey = require(key, "Cache key");
        final Consumer<String> removeListener = require(removed, "Removed key callback");
        cacheStore.remove(cacheKey);
        removeListener.accept(cacheKey);
    }

    /**
     * Returns whether a cache key belongs to the supplied URI regardless of method.
     *
     * @param key serialized cache key containing method and vary separators
     * @param uri serialized request URI compared with the key's URI segment
     * @return {@code true} when the substring between the first {@code @} and following {@code #} equals the URI
     */
    private static boolean sameUri(final String key, final String uri) {
        final int method = key.indexOf(Symbol.C_AT);
        final int vary = key.indexOf(Symbol.C_HASH, method + 1);
        return method >= 0 && vary > method && uri.equals(key.substring(method + 1, vary));
    }

    /**
     * Validates and returns a required reference.
     *
     * @param value reference to validate
     * @param name  logical reference name used in the validation message
     * @param <T>   reference type
     * @return the validated non-null reference
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
