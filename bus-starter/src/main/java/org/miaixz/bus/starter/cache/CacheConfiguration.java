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
package org.miaixz.bus.starter.cache;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.Context;
import org.miaixz.bus.cache.Collector;
import org.miaixz.bus.cache.metric.*;
import org.miaixz.bus.cache.collect.*;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.metrics.builtin.CacheMetricsAdapter;
import org.miaixz.bus.starter.jdbc.JdbcProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.Resource;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Auto-configuration for the cache system.
 * <p>
 * Reads {@link CacheProperties} and wires two independent components:
 * <ol>
 * <li><b>Cache storage backend</b> — selected by {@code bus.cache.type}.</li>
 * <li><b>Collector backend</b> — selected by {@code bus.cache.provider.type}.</li>
 * </ol>
 * The resulting {@link AspectjCacheProxy} intercepts {@code @Cached}, {@code @CachedGet}, and {@code @Invalid}
 * annotations to provide transparent AOP-based caching.
 * </p>
 * <p>
 * Per-entry expiration is controlled by {@code @Cached(expire = ...)}; {@code bus.cache.expire} sets the default TTL
 * only for in-process backends (memory, caffeine, guava).
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@EnableConfigurationProperties(value = { CacheProperties.class })
public class CacheConfiguration {

    /**
     * Injected cache configuration properties.
     */
    @Resource
    CacheProperties properties;

    /**
     * Creates the {@link AspectjCacheProxy} bean.
     * <p>
     * Returns {@code null} when neither a backend type nor a metrics type is configured, leaving caching inactive.
     * </p>
     *
     * @return configured proxy, or {@code null} if no configuration is present
     * @throws IllegalArgumentException on unknown type values or missing required properties
     */
    @Bean
    public AspectjCacheProxy cacheConfigurer() {
        try {
            Map<String, CacheX> caches = this.properties.getMap();
            if (MapKit.isEmpty(caches)) {
                CacheX<?, ?> backend = createBackend();
                if (backend != null) {
                    caches = Map.of("default", backend);
                }
            }

            JdbcProperties providerCfg = this.properties.getProvider();
            boolean hasCollector = providerCfg != null && StringKit.isNotEmpty(providerCfg.getKey());

            if (MapKit.isEmpty(caches) && !hasCollector) {
                return null;
            }

            Context context = Context.newConfig(caches);
            context.setCollector(createCollector(providerCfg));
            return new AspectjCacheProxy(context);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to configure cache: " + e.getMessage(), e);
        }
    }

    private CacheX<?, ?> createBackend() throws Exception {
        String type = this.properties.getType();
        if (StringKit.isEmpty(type)) {
            return null;
        }
        CacheProperties.Redis redisCfg = this.properties.getRedis();
        return switch (type.toLowerCase()) {
            case "memory" -> new MemoryCache<>();
            case "noop" -> new NoOpCache<>();
            case "caffeine" -> new CaffeineCache<>(this.properties.getMaxSize(), this.properties.getExpire());
            case "guava" -> new GuavaCache<>(this.properties.getMaxSize(), this.properties.getExpire());
            case "redis" -> {
                JedisPoolConfig cfg = new JedisPoolConfig();
                cfg.setMaxTotal(redisCfg.getMaxActive());
                cfg.setMaxIdle(redisCfg.getMaxIdle());
                cfg.setMinIdle(redisCfg.getMinIdle());
                yield new RedisCache<>(new JedisPool(cfg, redisCfg.getHost(), redisCfg.getPort(), redisCfg.getTimeout(),
                        redisCfg.getPassword()));
            }
            case "redis-cluster" -> {
                Set<HostAndPort> nodes = Arrays.stream(redisCfg.getNodes().split(",")).map(s -> {
                    String[] p = s.trim().split(":");
                    if (p.length != 2) {
                        throw new IllegalArgumentException(
                                "Invalid Redis cluster node format (expected host:port): " + s.trim());
                    }
                    try {
                        return new HostAndPort(p[0], Integer.parseInt(p[1]));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Invalid port in Redis cluster node: " + s.trim(), e);
                    }
                }).collect(Collectors.toSet());
                yield new RedisClusterCache<>(new JedisCluster(nodes));
            }
            case "memcached" -> new MemcachedCache<>(this.properties.getNodes());
            default -> throw new IllegalArgumentException("Unknown cache backend type: " + type);
        };
    }

    private Collector createCollector(JdbcProperties cfg) {
        if (cfg == null || StringKit.isEmpty(cfg.getKey())) {
            return null;
        }
        return switch (cfg.getKey().toLowerCase()) {
            case "memory" -> new MemoryCollector();
            case "h2" -> new H2Collector(cfg.getUrl(), cfg.getUsername(), cfg.getPassword());
            case "mysql" -> new MySQLCollector(cfg.getUrl(), cfg.getUsername(), cfg.getPassword());
            case "postgresql" -> new PostgreSQLCollector(cfg.getUrl(), cfg.getUsername(), cfg.getPassword());
            case "sqlite" -> new SqliteCollector(cfg.getUrl(), cfg.getUsername(), cfg.getPassword());
            case "bus" -> new CacheMetricsAdapter();
            default -> throw new IllegalArgumentException("Unknown metrics provider type: " + cfg.getKey());
        };
    }

}
