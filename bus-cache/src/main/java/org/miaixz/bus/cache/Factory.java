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
import java.util.Set;
import java.util.stream.Collectors;

import org.miaixz.bus.cache.metric.*;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Shared cache backend factory used by both the cache starter and downstream modules.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Factory {

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
            throw new IllegalArgumentException("Cache backend type is required");
        }
        return switch (type) {
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
            default -> throw new IllegalArgumentException("Unknown cache backend type: " + type);
        };
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
            Logger.warn(true, "Cache", "Failed to initialize CaffeineCache, falling back to MemoryCache");
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
            Logger.warn(true, "Cache", "Failed to initialize GuavaCache, falling back to MemoryCache");
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
            throw new IllegalArgumentException("cache.nodes is required for memcached cache");
        }
        try {
            return new Backend(new MemcachedCache<>(nodes), !extendedMode, !extendedMode, extendedMode,
                    maxSize(options), expireMs(options));
        } catch (Exception e) {
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
     * Creates a single-node Redis backend with a pooled Jedis client.
     *
     * @param options cache options
     * @return Redis cache
     */
    private CacheX<String, Object> redisCache(Options options) {
        Options.Redis redis = redis(options);
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(redis.getMaxActive());
        config.setMaxIdle(redis.getMaxIdle());
        config.setMinIdle(redis.getMinIdle());
        return new RedisCache<>(
                new JedisPool(config, redis.getHost(), redis.getPort(), redis.getTimeout(), redis.getPassword()));
    }

    /**
     * Creates a Redis cluster backend from the configured cluster node list.
     *
     * @param options cache options
     * @return Redis cluster cache
     */
    private CacheX<String, Object> redisClusterCache(Options options) {
        Options.Redis redis = redis(options);
        String nodes = StringKit.isNotBlank(redis.getNodes()) ? redis.getNodes() : options.getNodes();
        if (StringKit.isBlank(nodes)) {
            throw new IllegalArgumentException("cache.redis.nodes is required for redis-cluster cache");
        }
        Set<HostAndPort> hostAndPorts = Arrays.stream(nodes.split(",")).map(String::trim).filter(StringKit::isNotBlank)
                .map(this::hostAndPort).collect(Collectors.toSet());
        return new RedisClusterCache<>(new JedisCluster(hostAndPorts));
    }

    /**
     * Parses one {@code host:port} entry into a Jedis node descriptor.
     *
     * @param value node text in {@code host:port} format
     * @return parsed node
     */
    private HostAndPort hostAndPort(String value) {
        String[] parts = value.split(":");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid cache node format, expected host:port: " + value);
        }
        try {
            return new HostAndPort(parts[0], Integer.parseInt(parts[1]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port in cache node: " + value, e);
        }
    }

    /**
     * Returns the Redis-specific options block, creating a default one when absent.
     *
     * @param options cache options
     * @return Redis options
     */
    private Options.Redis redis(Options options) {
        return options.getRedis() == null ? new Options.Redis() : options.getRedis();
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
     */
    private record Backend(CacheX<String, Object> cache, boolean scan, boolean counter, boolean mirror,
            long maximumSize, long expireMs) {
    }

}
