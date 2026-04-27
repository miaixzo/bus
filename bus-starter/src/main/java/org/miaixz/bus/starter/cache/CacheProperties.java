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

import org.miaixz.bus.cache.Options;
import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.starter.jdbc.JdbcProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Starter-side configuration properties for the cache system.
 * <p>
 * Binds to the {@code bus.cache.*} namespace in {@code application.yml}. Two independent concerns are configured here:
 * </p>
 * <ol>
 * <li><b>Cache storage backend</b> — where data is stored (inherited from {@link Options}, including
 * {@link Options.Redis}).</li>
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
public class CacheProperties extends Options {

    /**
     * Named {@link CacheX} instances for advanced multi-cache scenarios.
     * <p>
     * Because {@link CacheX} is an interface, this map <strong>cannot</strong> be bound from YAML. Provide it
     * programmatically via {@code @Bean}. When present, it takes precedence over the auto-configured backend defined by
     * {@link Options#getType()}.
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
     * {@code bus.metrics.provider} configuration</li>
     * </ul>
     * Connection pool fields ({@code url}, {@code username}, {@code password}, {@code maxActive}, etc.) apply to
     * JDBC-backed metrics types.
     * </p>
     */
    private JdbcProperties provider = new JdbcProperties();

}
