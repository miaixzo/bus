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

import java.util.Map;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.starter.jdbc.JdbcProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuration properties for the cache system.
 * <p>
 * Binds to the {@code bus.cache.*} namespace in {@code application.yml}. Two independent concerns are configured here:
 * </p>
 * <ol>
 * <li><b>Cache storage backend</b> — where data is stored (top-level fields + {@link Redis}).</li>
 * <li><b>Collector backend</b> — where hit/miss statistics are stored ({@link JdbcProperties}).</li>
 * </ol>
 *
 * <p>
 * Example — Redis single-node with Prometheus metrics:
 * </p>
 * 
 * <pre>{@code
 * bus:
 *   cache:
 *     type: redis
 *     redis:
 *       host: 192.168.1.10
 *       port: 6379
 *       password: secret
 *     provider:
 *       key: bus
 * }</pre>
 *
 * <p>
 * Example — Redis Cluster with MySQL metrics:
 * </p>
 * 
 * <pre>{@code
 * bus:
 *   cache:
 *     type: redis-cluster
 *     redis:
 *       nodes: 192.168.1.1:6379,192.168.1.2:6379,192.168.1.3:6379
 *       password: secret
 *     provider:
 *       type: mysql
 *       url: jdbc:mysql://localhost:3306/cache_db
 *       username: root
 *       password: root123
 * }</pre>
 *
 * <p>
 * Example — Caffeine with in-memory metrics:
 * </p>
 * 
 * <pre>{@code
 * bus:
 *   cache:
 *     type: caffeine
 *     max-size: 5000
 *     expire: 600000
 *     provider:
 *       type: memory
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@ConfigurationProperties(prefix = GeniusBuilder.CACHE)
public class CacheProperties {

    /**
     * Cache storage backend type.
     * <p>
     * Supported values:
     * </p>
     * <ul>
     * <li>{@code memory} — in-process {@link org.miaixz.bus.cache.metric.MemoryCache}</li>
     * <li>{@code noop} — no-op cache for testing {@link org.miaixz.bus.cache.metric.NoOpCache}</li>
     * <li>{@code caffeine} — high-performance in-process {@link org.miaixz.bus.cache.metric.CaffeineCache}</li>
     * <li>{@code guava} — Guava-backed in-process {@link org.miaixz.bus.cache.metric.GuavaCache}</li>
     * <li>{@code redis} — single-node Redis {@link org.miaixz.bus.cache.metric.RedisCache}</li>
     * <li>{@code redis-cluster} — Redis Cluster {@link org.miaixz.bus.cache.metric.RedisClusterCache}</li>
     * <li>{@code memcached} — Memcached {@link org.miaixz.bus.cache.metric.MemcachedCache}</li>
     * </ul>
     */
    private String type;

    /**
     * Maximum number of entries. Applies to {@code memory}, {@code caffeine}, {@code guava}.
     */
    private long maxSize = 10_000;

    /**
     * Default TTL in milliseconds. Applies to {@code memory}, {@code caffeine}, {@code guava}. Per-method expiry is
     * controlled by {@code @Cached(expire = ...)}.
     */
    private long expire = 3_600_000;

    /**
     * Comma-separated {@code host:port} server list for {@code memcached}. Example:
     * {@code 192.168.1.1:11211,192.168.1.2:11211}
     * <p>
     * For Redis cluster nodes, use {@link Redis#nodes} instead.
     * </p>
     */
    private String nodes;

    /**
     * Redis connection configuration. Applies to {@code redis} and {@code redis-cluster}.
     */
    private Redis redis = new Redis();

    /**
     * Named {@link CacheX} instances for advanced multi-cache scenarios.
     * <p>
     * Because {@link CacheX} is an interface, this map <strong>cannot</strong> be bound from YAML. Provide it
     * programmatically via {@code @Bean}. When present, it takes precedence over the auto-configured backend defined by
     * {@link #type}.
     * </p>
     */
    private Map<String, CacheX> map;

    /**
     * Hit/miss statistics backend configuration.
     * <p>
     * {@link JdbcProperties} {@code key} field selects the metrics backend:
     * <ul>
     * <li>{@code memory} — in-process, resets on restart</li>
     * <li>{@code h2} — embedded H2 database</li>
     * <li>{@code mysql} — MySQL via JDBC</li>
     * <li>{@code postgresql} — PostgreSQL via JDBC</li>
     * <li>{@code sqlite} — embedded SQLite database</li>
     * <li>{@code bus} — bridges to bus-metrics (Prometheus / Micrometer / OTel), backend determined by
     * {@code bus.metrics.provider} configuration</li> *
     * </ul>
     * Connection pool fields ({@code url}, {@code username}, {@code password}, {@code maxActive}, etc.) apply to
     * JDBC-backed metrics types.
     * </p>
     */
    private JdbcProperties provider = new JdbcProperties();

    /**
     * Redis connection and pool settings.
     * <p>
     * Shared by both {@code redis} (single-node) and {@code redis-cluster} modes. Single-node uses {@link #host} and
     * {@link #port}; cluster mode uses {@link #nodes}.
     * </p>
     */
    @Getter
    @Setter
    public static class Redis {

        /**
         * Redis server hostname. Applies to single-node ({@code redis}) mode.
         */
        private String host = "localhost";

        /**
         * Redis server port. Applies to single-node ({@code redis}) mode.
         */
        private int port = 6379;

        /**
         * Redis authentication password. Applies to both {@code redis} and {@code redis-cluster}.
         */
        private String password;

        /**
         * Connection and read timeout in milliseconds.
         */
        private int timeout = 2000;

        /**
         * Maximum total connections in the pool.
         */
        private int maxActive = 8;

        /**
         * Maximum idle connections in the pool.
         */
        private int maxIdle = 8;

        /**
         * Minimum idle connections in the pool.
         */
        private int minIdle = 0;

        /**
         * Comma-separated {@code host:port} cluster node list. Applies to {@code redis-cluster} mode. Example:
         * {@code 192.168.1.1:6379,192.168.1.2:6379,192.168.1.3:6379}
         */
        private String nodes;

    }

}
