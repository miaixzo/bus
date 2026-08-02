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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.Collector;
import org.miaixz.bus.cache.Context;
import org.miaixz.bus.cache.Factory;
import org.miaixz.bus.cache.Module;
import org.miaixz.bus.cache.collect.*;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.metrics.builtin.CacheMetricsAdapter;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Configures annotation-driven cache interception and cache providers.
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
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.miaixz.bus.cache.CacheX")
@ConditionalOnProperty(prefix = GeniusBuilder.CACHE, name = "enabled", havingValue = "true", matchIfMissing = false)
public class CacheConfiguration {

    /**
     * Bound cache configuration properties.
     */
    private final CacheProperties properties;

    /**
     * Stores the cache provider and interception policy used by the Bean factories.
     *
     * @param properties bound configuration properties
     */
    public CacheConfiguration(CacheProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the context-owned cache factory.
     *
     * @return the cache factory selected from the configured cache type
     */
    @Bean
    @ConditionalOnMissingBean(Factory.class)
    public Factory factory() {
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
    @ConditionalOnMissingBean(name = "defaultCache")
    public CacheX<String, Object> defaultCache(Factory factory) {
        return factory.initialize(this.properties);
    }

    /**
     * Creates the {@link AspectjCacheProxy} bean.
     * <p>
     * Uses the named default backend when no explicit cache map is supplied.
     * </p>
     *
     * @param factory      cache factory
     * @param defaultCache named default cache backend
     * @return configured proxy
     * @throws IllegalArgumentException on unknown type values or missing required properties
     */
    @Bean
    @ConditionalOnMissingBean(AspectjCacheProxy.class)
    public AspectjCacheProxy cacheConfigurer(
            Factory factory,
            @Qualifier("defaultCache") CacheX<String, Object> defaultCache) {
        try {
            Map<String, CacheX> caches = this.properties.getMap();
            if (MapKit.isEmpty(caches)) {
                caches = Map.of("default", defaultCache);
            }

            CacheProperties.Collector providerCfg = this.properties.getProvider();
            boolean hasCollector = providerCfg != null && StringKit.isNotEmpty(providerCfg.getKey());

            Context context = Context.newConfig(caches);
            if (hasCollector) {
                context.setCollector(createCollector(providerCfg));
            }
            return new AspectjCacheProxy(Module.instance(context));
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Starter",
                    e,
                    "Cache auto configuration failed: propertiesPresent={}, providerPresent={}, exception={}",
                    this.properties != null,
                    this.properties != null && this.properties.getProvider() != null,
                    e.getClass().getSimpleName());
            throw new IllegalArgumentException("Failed to configure cache: " + e.getMessage(), e);
        }
    }

    /**
     * Collects non-null cache backend specifications in stable order.
     *
     * @param cfg cache configuration to adapt
     * @return the cache collector backed by the selected factory
     */
    private Collector createCollector(CacheProperties.Collector cfg) {
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
