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
import org.miaixz.bus.cache.Collector;
import org.miaixz.bus.cache.Context;
import org.miaixz.bus.cache.Factory;
import org.miaixz.bus.cache.collect.*;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.metrics.builtin.CacheMetricsAdapter;
import org.miaixz.bus.starter.jdbc.JdbcProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.Resource;

/**
 * Auto-configuration for the cache system.
 *
 * <p>
 * Reads {@link CacheProperties} and wires two independent components:
 * <ol>
 * <li><b>Cache storage backend</b> - selected by {@code bus.cache.type}.</li>
 * <li><b>Collector backend</b> - selected by {@code bus.cache.provider.type}.</li>
 * </ol>
 *
 * <p>
 * The resulting {@link AspectjCacheProxy} intercepts {@code @Cached}, {@code @CachedGet}, and {@code @Invalid}
 * annotations to provide transparent AOP-based caching.
 *
 * <p>
 * Per-entry expiration is controlled by {@code @Cached(expire = ...)}; {@code bus.cache.expire} sets the default TTL
 * only for in-process backends (memory, caffeine, guava).
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(value = { CacheProperties.class })
public class CacheConfiguration {

    /**
     * Injected cache configuration properties.
     */
    @Resource
    CacheProperties properties;

    /**
     * Exposes the shared cache factory used by both cache and Cortex auto-configuration.
     *
     * @return cache factory
     */
    @Bean
    @ConditionalOnMissingBean(Factory.class)
    public Factory cacheFactory() {
        return new Factory();
    }

    /**
     * Creates the default cache backend from {@code bus.cache.*}.
     *
     * @param factory shared cache factory
     * @return default cache backend
     */
    @Bean("defaultCache")
    @Primary
    @ConditionalOnProperty(prefix = "bus.cache", name = "type")
    @ConditionalOnMissingBean(name = "defaultCache")
    public CacheX<String, Object> defaultCache(Factory factory) {
        return factory.initialize(this.properties);
    }

    /**
     * Creates the {@link AspectjCacheProxy} bean.
     * <p>
     * Returns {@code null} when no cache backend is configured, leaving caching inactive.
     * </p>
     *
     * @return configured proxy, or {@code null} if no configuration is present
     * @throws IllegalArgumentException on unknown type values or missing required properties
     */
    @Bean
    public AspectjCacheProxy cacheConfigurer(
            Factory factory,
            @Qualifier("defaultCache") ObjectProvider<CacheX<String, Object>> defaultCacheProvider) {
        try {
            Map<String, CacheX> caches = this.properties.getMap();
            if (MapKit.isEmpty(caches)) {
                CacheX<String, Object> backend = defaultCacheProvider.getIfAvailable();
                if (backend == null && factory.hasBackend(this.properties)) {
                    backend = factory.initialize(this.properties);
                }
                if (backend != null) {
                    caches = Map.of("default", backend);
                }
            }

            JdbcProperties providerCfg = this.properties.getProvider();
            boolean hasCollector = providerCfg != null && StringKit.isNotEmpty(providerCfg.getKey());

            if (MapKit.isEmpty(caches)) {
                return null;
            }

            Context context = Context.newConfig(caches);
            if (hasCollector) {
                context.setCollector(createCollector(providerCfg));
            }
            return new AspectjCacheProxy(context);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to configure cache: " + e.getMessage(), e);
        }
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
