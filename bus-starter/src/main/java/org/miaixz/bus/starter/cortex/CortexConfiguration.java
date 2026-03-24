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
package org.miaixz.bus.starter.cortex;

import org.miaixz.bus.cache.CacheX;
import org.miaixz.bus.cache.metric.MemoryCache;
import org.miaixz.bus.cortex.Config;
import org.miaixz.bus.cortex.Cortex;
import org.miaixz.bus.cortex.config.ConfigPublisher;
import org.miaixz.bus.cortex.config.DefaultConfig;
import org.miaixz.bus.cortex.registry.WatchManager;
import org.miaixz.bus.cortex.registry.api.ApiRegistry;
import org.miaixz.bus.cortex.registry.mcp.McpRegistry;
import org.miaixz.bus.cortex.registry.prompt.PromptRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for Bus Cortex starter wiring.
 * <p>
 * The starter now exposes real bus-cortex components backed by a shared local {@link CacheX}, then initializes the
 * static {@link Cortex} facade so application code and Spring-managed beans resolve the same handles.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(CortexProperties.class)
@ConditionalOnProperty(prefix = "bus.cortex", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CortexConfiguration {

    /**
     * Creates the starter-local shared cache.
     *
     * @param properties bound Cortex properties
     * @return local cache used by starter-backed Cortex services
     */
    @Bean
    @ConditionalOnMissingBean(name = "cortexCache")
    public CacheX<String, Object> cortexCache(CortexProperties properties) {
        return new MemoryCache<>(1024, properties.getCacheExpireMs());
    }

    /**
     * Creates the watch manager used by registries and config subscriptions.
     *
     * @param properties bound Cortex properties
     * @return watch manager instance
     */
    @Bean
    @ConditionalOnMissingBean
    public WatchManager cortexWatchManager(CortexProperties properties) {
        return new WatchManager(properties.getMaxWatchesPerNamespace(), properties.getWatchExpireMs());
    }

    /**
     * Creates the config publisher responsible for versioned config persistence.
     *
     * @param cacheX       shared starter cache
     * @param watchManager watch manager for publish notifications
     * @param properties   bound Cortex properties
     * @return config publisher instance
     */
    @Bean
    @ConditionalOnMissingBean
    public ConfigPublisher cortexConfigPublisher(
            CacheX<String, Object> cacheX,
            WatchManager watchManager,
            CortexProperties properties) {
        return new ConfigPublisher(cacheX, watchManager, properties.getMaxConfigVersions());
    }

    /**
     * Creates the API registry bean.
     *
     * @param cacheX       shared starter cache
     * @param watchManager watch manager for registry subscriptions
     * @return API registry
     */
    @Bean
    @ConditionalOnMissingBean
    public ApiRegistry apiRegistry(CacheX<String, Object> cacheX, WatchManager watchManager) {
        return new ApiRegistry(cacheX, watchManager);
    }

    /**
     * Creates the MCP registry bean.
     *
     * @param cacheX       shared starter cache
     * @param watchManager watch manager for registry subscriptions
     * @return MCP registry
     */
    @Bean
    @ConditionalOnMissingBean
    public McpRegistry mcpRegistry(CacheX<String, Object> cacheX, WatchManager watchManager) {
        return new McpRegistry(cacheX, watchManager);
    }

    /**
     * Creates the prompt registry bean.
     *
     * @param cacheX       shared starter cache
     * @param watchManager watch manager for registry subscriptions
     * @return prompt registry
     */
    @Bean
    @ConditionalOnMissingBean
    public PromptRegistry promptRegistry(CacheX<String, Object> cacheX, WatchManager watchManager) {
        return new PromptRegistry(cacheX, watchManager);
    }

    /**
     * Creates the config-center bean.
     *
     * @param configPublisher versioned config publisher
     * @param cacheX          shared starter cache
     * @param watchManager    watch manager for config subscriptions
     * @param properties      bound Cortex properties
     * @return config center
     */
    @Bean
    @ConditionalOnMissingBean
    public Config cortexConfig(
            ConfigPublisher configPublisher,
            CacheX<String, Object> cacheX,
            WatchManager watchManager,
            CortexProperties properties) {
        return new DefaultConfig(configPublisher, cacheX, watchManager, properties.requireNamespace());
    }

}
