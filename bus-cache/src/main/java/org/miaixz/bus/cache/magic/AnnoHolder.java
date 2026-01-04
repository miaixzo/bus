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
package org.miaixz.bus.cache.magic;

import java.lang.reflect.Method;
import java.util.Map;
import org.miaixz.bus.cache.magic.annotation.CacheKey;

/**
 * An immutable container for caching-related annotation information extracted from a method.
 * <p>
 * This class stores details such as the cache name, key prefix, expiration time, and mappings of parameters to cache
 * keys. It is instantiated using a fluent {@link Builder}.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnnoHolder {

    /**
     * The annotated method.
     */
    private final Method method;

    /**
     * The name of the cache to be used.
     */
    private final String cache;

    /**
     * The prefix for the cache key.
     */
    private final String prefix;

    /**
     * The cache expiration time in milliseconds.
     */
    private final int expire;

    /**
     * A map from parameter index to the corresponding {@link CacheKey} annotation.
     */
    private final Map<Integer, CacheKey> cacheKeyMap;

    /**
     * The index of the parameter used for multi-key caching. A value of -1 indicates it is not a multi-key cache.
     */
    private final int multiIndex;

    /**
     * The identifier for the cache, used in multi-key scenarios to map results back.
     */
    private final String id;

    /**
     * Private constructor to be used by the internal {@link Builder}.
     *
     * @param method      The annotated method.
     * @param cache       The name of the cache.
     * @param prefix      The prefix for the cache key.
     * @param expire      The cache expiration time in milliseconds.
     * @param cacheKeyMap A map of parameter indices to {@link CacheKey} annotations.
     * @param multiIndex  The index of the parameter for multi-key caching.
     * @param id          The identifier for the cache.
     */
    private AnnoHolder(Method method, String cache, String prefix, int expire, Map<Integer, CacheKey> cacheKeyMap,
            int multiIndex, String id) {
        this.method = method;
        this.cache = cache;
        this.prefix = prefix;
        this.expire = expire;
        this.cacheKeyMap = cacheKeyMap;
        this.multiIndex = multiIndex;
        this.id = id;
    }

    /**
     * Gets the annotated method.
     *
     * @return The {@link Method} object.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Gets the name of the cache.
     *
     * @return The cache name.
     */
    public String getCache() {
        return cache;
    }

    /**
     * Gets the prefix for the cache key.
     *
     * @return The cache key prefix.
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Gets the cache expiration time in milliseconds.
     *
     * @return The expiration time in milliseconds.
     */
    public int getExpire() {
        return expire;
    }

    /**
     * Gets the map of parameter indices to {@link CacheKey} annotations.
     *
     * @return A map where the key is the parameter index and the value is the {@link CacheKey} annotation.
     */
    public Map<Integer, CacheKey> getCacheKeyMap() {
        return cacheKeyMap;
    }

    /**
     * Gets the index of the parameter used for multi-key caching.
     *
     * @return The parameter index, or -1 if this is not a multi-key cache.
     */
    public int getMultiIndex() {
        return multiIndex;
    }

    /**
     * Checks if this holder represents a multi-key caching operation.
     *
     * @return {@code true} if it is a multi-key cache, otherwise {@code false}.
     */
    public boolean isMulti() {
        return multiIndex != -1;
    }

    /**
     * Gets the identifier for the cache.
     *
     * @return The cache identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * A builder for creating {@link AnnoHolder} instances using a fluent API.
     */
    public static class Builder {

        /**
         * The method for which the holder is being built.
         */
        private final Method method;

        /**
         * The cache name to use.
         */
        private String cache;

        /**
         * The key prefix for cache entries.
         */
        private String prefix;

        /**
         * The expiration time in milliseconds.
         */
        private int expire;

        /**
         * A map of parameter indices to their {@link CacheKey} annotations.
         */
        private Map<Integer, CacheKey> cacheKeyMap;

        /**
         * The index of the parameter to use as a multi-cache key, or -1 if not set.
         */
        private int multiIndex = -1;

        /**
         * The identifier for the cache annotation.
         */
        private String id;

        /**
         * Private constructor for the builder.
         *
         * @param method The method for which the holder is being built.
         */
        private Builder(Method method) {
            this.method = method;
        }

        /**
         * Creates a new builder instance for the given method.
         *
         * @param method The method object.
         * @return A new {@link Builder} instance.
         */
        public static Builder newBuilder(Method method) {
            return new Builder(method);
        }

        /**
         * Sets the cache name.
         *
         * @param cache The name of the cache.
         * @return This builder instance for chaining.
         */
        public Builder setCache(String cache) {
            this.cache = cache;
            return this;
        }

        /**
         * Sets the cache key prefix.
         *
         * @param prefix The prefix for the cache key.
         * @return This builder instance for chaining.
         */
        public Builder setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * Sets the cache expiration time.
         *
         * @param expire The expiration time in milliseconds.
         * @return This builder instance for chaining.
         */
        public Builder setExpire(int expire) {
            this.expire = expire;
            return this;
        }

        /**
         * Sets the index of the parameter for multi-key caching.
         *
         * @param multiIndex The parameter index.
         * @return This builder instance for chaining.
         */
        public Builder setMultiIndex(int multiIndex) {
            this.multiIndex = multiIndex;
            return this;
        }

        /**
         * Sets the cache identifier.
         *
         * @param id The cache identifier.
         * @return This builder instance for chaining.
         */
        public Builder setId(String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the map of parameter indices to {@link CacheKey} annotations.
         *
         * @param cacheKeyMap The map of cache key annotations.
         * @return This builder instance for chaining.
         */
        public Builder setCacheKeyMap(Map<Integer, CacheKey> cacheKeyMap) {
            this.cacheKeyMap = cacheKeyMap;
            return this;
        }

        /**
         * Builds and returns the final, immutable {@link AnnoHolder} instance.
         *
         * @return A new {@link AnnoHolder} instance.
         */
        public AnnoHolder build() {
            return new AnnoHolder(method, cache, prefix, expire, cacheKeyMap, multiIndex, id);
        }
    }

}
