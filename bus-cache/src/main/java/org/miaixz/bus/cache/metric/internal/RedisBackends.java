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
package org.miaixz.bus.cache.metric.internal;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.Factory;
import org.miaixz.bus.cache.Options;
import org.miaixz.bus.cache.metric.RedisCache;
import org.miaixz.bus.cache.metric.RedisClusterCache;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis backend builder isolated from {@link Factory} so Jedis is loaded only when a Redis backend is selected.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class RedisBackends {

    /**
     * Hidden constructor for utility class.
     */
    private RedisBackends() {
        // No initialization required.
    }

    /**
     * Creates a single-node Redis backend with a pooled Jedis client.
     *
     * @param options cache options
     * @return Redis cache
     */
    public static CacheX<String, Object> redisCache(Options options) {
        Options.Redis redis = redis(options);
        Logger.info(
                true,
                "Cache",
                "Redis cache initialization started: hostPresent={}, port={}, timeoutMs={}, passwordPresent={}",
                StringKit.isNotBlank(redis.getHost()),
                redis.getPort(),
                redis.getTimeout(),
                StringKit.isNotBlank(redis.getPassword()));
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(redis.getMaxActive());
        config.setMaxIdle(redis.getMaxIdle());
        config.setMinIdle(redis.getMinIdle());
        CacheX<String, Object> cache = new RedisCache<>(
                new JedisPool(config, redis.getHost(), redis.getPort(), redis.getTimeout(), redis.getPassword()));
        Logger.info(
                false,
                "Cache",
                "Redis cache initialization completed: hostPresent={}, port={}, timeoutMs={}, maxActive={}, maxIdle={}, minIdle={}",
                StringKit.isNotBlank(redis.getHost()),
                redis.getPort(),
                redis.getTimeout(),
                redis.getMaxActive(),
                redis.getMaxIdle(),
                redis.getMinIdle());
        return cache;
    }

    /**
     * Creates a Redis cluster backend from the configured cluster node list.
     *
     * @param options cache options
     * @return Redis cluster cache
     */
    public static CacheX<String, Object> redisClusterCache(Options options) {
        Options.Redis redis = redis(options);
        String nodes = StringKit.isNotBlank(redis.getNodes()) ? redis.getNodes() : options.getNodes();
        if (StringKit.isBlank(nodes)) {
            Logger.warn(false, "Cache", "Redis cluster cache initialization rejected: nodeCount=0");
            throw new IllegalArgumentException("cache.redis.nodes is required for redis-cluster cache");
        }
        long nodeCount = Arrays.stream(nodes.split(",")).map(String::trim).filter(StringKit::isNotBlank).count();
        Logger.info(true, "Cache", "Redis cluster cache initialization started: nodeCount={}", nodeCount);
        Set<HostAndPort> hostAndPorts = Arrays.stream(nodes.split(",")).map(String::trim).filter(StringKit::isNotBlank)
                .map(RedisBackends::hostAndPort).collect(Collectors.toSet());
        CacheX<String, Object> cache = new RedisClusterCache<>(new JedisCluster(hostAndPorts));
        Logger.info(false, "Cache", "Redis cluster cache initialization completed: nodeCount={}", hostAndPorts.size());
        return cache;
    }

    /**
     * Parses one {@code host:port} entry into a Jedis node descriptor.
     *
     * @param value node text in {@code host:port} format
     * @return parsed node
     */
    private static HostAndPort hostAndPort(String value) {
        String[] parts = value.split(":");
        if (parts.length != 2) {
            Logger.warn(
                    false,
                    "Cache",
                    "Cache node parsing rejected: partCount={}, nodePresent={}",
                    parts.length,
                    StringKit.isNotBlank(value));
            throw new IllegalArgumentException("Invalid cache node format, expected host:port: " + value);
        }
        try {
            return new HostAndPort(parts[0], Integer.parseInt(parts[1]));
        } catch (NumberFormatException e) {
            Logger.warn(
                    false,
                    "Cache",
                    e,
                    "Cache node parsing failed: hostPresent={}, portPresent={}, exception={}",
                    StringKit.isNotBlank(parts[0]),
                    StringKit.isNotBlank(parts[1]),
                    e.getClass().getSimpleName());
            throw new IllegalArgumentException("Invalid port in cache node: " + value, e);
        }
    }

    /**
     * Returns the Redis-specific options block, creating a default one when absent.
     *
     * @param options cache options
     * @return Redis options
     */
    private static Options.Redis redis(Options options) {
        return options.getRedis() == null ? new Options.Redis() : options.getRedis();
    }

}
