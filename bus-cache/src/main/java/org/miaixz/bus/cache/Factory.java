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
package org.miaixz.bus.cache;

import java.util.Arrays;

import org.miaixz.bus.cache.metric.*;
import org.miaixz.bus.cache.metric.internal.RedisBackends;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

/**
 * Shared cache backend factory used by both the cache starter and downstream modules.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Factory {

    /**
     * Constructs a new Factory instance.
     */
    public Factory() {
        // No initialization required.
    }

    /**
     * Default maximum cache size defined by {@link Options}.
     */
    private static final long DEFAULT_CACHE_MAX_SIZE = new Options().getMaxSize();

    /**
     * Default expiration defined by {@link Options}.
     */
    private static final long DEFAULT_CACHE_EXPIRE_MS = new Options().getExpire();

    /**
     * Returns whether the supplied options describe a concrete cache backend.
     *
     * @param options cache options
     * @return {@code true} when a backend type is configured
     */
    public boolean hasBackend(Options options) {
        return options != null && StringKit.isNotBlank(options.getType());
    }

    /**
     * Initializes a regular cache backend from {@link Options}.
     *
     * @param options cache options
     * @return initialized backend
     */
    public CacheX<String, Object> initialize(Options options) {
        return createBackend(options, false).cache();
    }

    /**
     * Initializes a cache backend plus the extra runtime semantics required by modules such as Cortex.
     *
     * @param options cache options
     * @return initialized cache view
     */
    public CacheX<String, Object> initializeExtended(Options options) {
        Backend backend = createBackend(options, true);
        CacheX<String, Object> mirror = backend.mirror() ? new MemoryCache<>(backend.maximumSize(), backend.expireMs())
                : null;
        return new Hybrid(backend.cache(), backend.scan(), backend.counter(), mirror);
    }

    /**
     * Builds the backend descriptor for the supplied options and runtime mode.
     *
     * @param options      cache options
     * @param extendedMode whether capability compensation is required
     * @return backend descriptor
     */
    private Backend createBackend(Options options, boolean extendedMode) {
        Options effective = options == null ? new Options() : options;
        String type = normalizeType(effective.getType());
        if (StringKit.isBlank(type)) {
            Logger.warn(
                    false,
                    "Cache",
                    "Cache backend creation rejected: typePresent=false, extendedMode={}",
                    extendedMode);
            throw new IllegalArgumentException("Cache backend type is required");
        }
        Logger.info(
                true,
                "Cache",
                "Cache backend creation started: type={}, extendedMode={}, maxSize={}, expireMs={}",
                type,
                extendedMode,
                maxSize(effective),
                expireMs(effective));
        Backend backend = switch (type) {
            case "memory" -> new Backend(memoryCache(effective), true, true, false, maxSize(effective),
                    expireMs(effective));
            case "noop" -> new Backend(new NoOpCache<>(), false, false, false, maxSize(effective), expireMs(effective));
            case "caffeine" -> caffeineBackend(effective, extendedMode);
            case "guava" -> guavaBackend(effective, extendedMode);
            case "redis" -> new Backend(redisCache(effective), true, true, false, maxSize(effective),
                    expireMs(effective));
            case "redis-cluster" -> new Backend(redisClusterCache(effective), true, true, false, maxSize(effective),
                    expireMs(effective));
            case "memcached" -> memcachedBackend(effective, extendedMode);
            default -> {
                Logger.warn(
                        false,
                        "Cache",
                        "Cache backend creation rejected: type={}, extendedMode={}",
                        type,
                        extendedMode);
                throw new IllegalArgumentException("Unknown cache backend type: " + type);
            }
        };
        Logger.info(
                false,
                "Cache",
                "Cache backend creation completed: type={}, backend={}, scan={}, counter={}, mirror={}, extendedMode={}",
                type,
                backend.cache().getClass().getSimpleName(),
                backend.scan(),
                backend.counter(),
                backend.mirror(),
                extendedMode);
        return backend;
    }

    /**
     * Creates a Caffeine backend and falls back to memory when the optional dependency is unavailable.
     *
     * @param options      cache options
     * @param extendedMode whether capability compensation is required
     * @return backend descriptor
     */
    private Backend caffeineBackend(Options options, boolean extendedMode) {
        try {
            return new Backend(new CaffeineCache<>(maxSize(options), expireMs(options)), !extendedMode, !extendedMode,
                    extendedMode, maxSize(options), expireMs(options));
        } catch (Throwable e) {
            Logger.warn(
                    false,
                    "Cache",
                    e,
                    "Caffeine cache initialization failed; falling back to memory: maxSize={}, expireMs={}, extendedMode={}, exception={}",
                    maxSize(options),
                    expireMs(options),
                    extendedMode,
                    e.getClass().getSimpleName());
            return new Backend(memoryCache(options), true, true, false, maxSize(options), expireMs(options));
        }
    }

    /**
     * Creates a Guava backend and falls back to memory when the optional dependency is unavailable.
     *
     * @param options      cache options
     * @param extendedMode whether capability compensation is required
     * @return backend descriptor
     */
    private Backend guavaBackend(Options options, boolean extendedMode) {
        try {
            return new Backend(new GuavaCache<>(maxSize(options), expireMs(options)), !extendedMode, !extendedMode,
                    extendedMode, maxSize(options), expireMs(options));
        } catch (Throwable e) {
            Logger.warn(
                    false,
                    "Cache",
                    e,
                    "Guava cache initialization failed; falling back to memory: maxSize={}, expireMs={}, extendedMode={}, exception={}",
                    maxSize(options),
                    expireMs(options),
                    extendedMode,
                    e.getClass().getSimpleName());
            return new Backend(memoryCache(options), true, true, false, maxSize(options), expireMs(options));
        }
    }

    /**
     * Creates a Memcached backend from the configured node list.
     *
     * @param options      cache options
     * @param extendedMode whether capability compensation is required
     * @return backend descriptor
     */
    private Backend memcachedBackend(Options options, boolean extendedMode) {
        String nodes = options.getNodes();
        if (StringKit.isBlank(nodes)) {
            Logger.warn(
                    false,
                    "Cache",
                    "Memcached cache initialization rejected: nodeCount=0, extendedMode={}",
                    extendedMode);
            throw new IllegalArgumentException("cache.nodes is required for memcached cache");
        }
        Logger.info(
                true,
                "Cache",
                "Memcached cache initialization started: nodeCount={}, maxSize={}, expireMs={}, extendedMode={}",
                Arrays.stream(nodes.split(",")).map(String::trim).filter(StringKit::isNotBlank).count(),
                maxSize(options),
                expireMs(options),
                extendedMode);
        try {
            Backend backend = new Backend(new MemcachedCache<>(nodes), !extendedMode, !extendedMode, extendedMode,
                    maxSize(options), expireMs(options));
            Logger.info(
                    false,
                    "Cache",
                    "Memcached cache initialization completed: nodeCount={}, scan={}, counter={}, mirror={}, extendedMode={}",
                    Arrays.stream(nodes.split(",")).map(String::trim).filter(StringKit::isNotBlank).count(),
                    backend.scan(),
                    backend.counter(),
                    backend.mirror(),
                    extendedMode);
            return backend;
        } catch (NoClassDefFoundError e) {
            throw missingOptionalDependency("memcached", "com.googlecode.xmemcached:xmemcached", e);
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Cache",
                    e,
                    "Memcached cache initialization failed: nodeCount={}, maxSize={}, expireMs={}, extendedMode={}, exception={}",
                    Arrays.stream(nodes.split(",")).map(String::trim).filter(StringKit::isNotBlank).count(),
                    maxSize(options),
                    expireMs(options),
                    extendedMode,
                    e.getClass().getSimpleName());
            throw new IllegalArgumentException("Failed to initialize Memcached cache: " + e.getMessage(), e);
        }
    }

    /**
     * Creates the in-memory backend used by both direct memory mode and fallback paths.
     *
     * @param options cache options
     * @return in-memory cache
     */
    private CacheX<String, Object> memoryCache(Options options) {
        return new MemoryCache<>(maxSize(options), expireMs(options));
    }

    /**
     * Creates a single-node Redis backend without loading Jedis unless Redis is selected.
     *
     * @param options cache options
     * @return Redis cache
     */
    private CacheX<String, Object> redisCache(Options options) {
        try {
            return RedisBackends.redisCache(options);
        } catch (NoClassDefFoundError e) {
            throw missingOptionalDependency("redis", "redis.clients:jedis", e);
        }
    }

    /**
     * Creates a Redis cluster backend without loading Jedis unless Redis Cluster is selected.
     *
     * @param options cache options
     * @return Redis cluster cache
     */
    private CacheX<String, Object> redisClusterCache(Options options) {
        try {
            return RedisBackends.redisClusterCache(options);
        } catch (NoClassDefFoundError e) {
            throw missingOptionalDependency("redis-cluster", "redis.clients:jedis", e);
        }
    }

    /**
     * Creates an exception for a selected backend whose optional client library is absent.
     *
     * @param type       backend type
     * @param dependency required dependency coordinate
     * @param cause      missing class error
     * @return configuration exception
     */
    private IllegalArgumentException missingOptionalDependency(String type, String dependency, NoClassDefFoundError cause) {
        Logger.error(
                false,
                "Cache",
                cause,
                "Cache backend creation failed: type={}, missingDependency={}",
                type,
                dependency);
        return new IllegalArgumentException(
                "Cache backend '" + type + "' requires optional dependency " + dependency, cause);
    }

    /**
     * Normalizes backend type names to the internal lowercase hyphenated form.
     *
     * @param type raw backend type
     * @return normalized backend type
     */
    private String normalizeType(String type) {
        return StringKit.isBlank(type) ? null : type.trim().toLowerCase().replace('_', '-');
    }

    /**
     * Returns the effective maximum size, falling back to the module default when absent or invalid.
     *
     * @param options cache options
     * @return effective maximum size
     */
    private long maxSize(Options options) {
        return options.getMaxSize() > 0L ? options.getMaxSize() : DEFAULT_CACHE_MAX_SIZE;
    }

    /**
     * Returns the effective expiration time, falling back to the module default when absent or invalid.
     *
     * @param options cache options
     * @return effective expiration in milliseconds
     */
    private long expireMs(Options options) {
        return options.getExpire() > 0L ? options.getExpire() : DEFAULT_CACHE_EXPIRE_MS;
    }

    /**
     * Backend descriptor used internally by the factory.
     *
     * @param cache       primary backend
     * @param scan        whether scan can use the primary backend
     * @param counter     whether increment can use the primary backend
     * @param mirror      whether an in-memory mirror is required
     * @param maximumSize mirror capacity
     * @param expireMs    mirror TTL
     * @author Kimi Liu
     * @since Java 21+
     */
    private record Backend(CacheX<String, Object> cache, boolean scan, boolean counter, boolean mirror,
            long maximumSize, long expireMs) {

    }

}
