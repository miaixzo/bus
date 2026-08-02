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
package org.miaixz.bus.starter.wrapper.body;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.spring.web.wrapper.BodyCacheOptions;
import org.miaixz.bus.spring.web.wrapper.CachedBodyFilter;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Configures wrapper request and response body caching.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BodyCacheProperties.class)
@ConditionalOnProperty(prefix = GeniusBuilder.WRAPPER_BODY_CACHE, name = "enabled", havingValue = "true", matchIfMissing = false)
public class BodyCacheConfiguration {

    /**
     * Bound body cache configuration properties.
     */
    private final BodyCacheProperties properties;

    /**
     * Stores the bounded request and response cache policy used to create Servlet components.
     *
     * @param properties bound configuration properties
     */
    public BodyCacheConfiguration(BodyCacheProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the body cache options.
     *
     * @return immutable request and response body-cache options
     */
    @Bean
    @ConditionalOnMissingBean(BodyCacheOptions.class)
    public BodyCacheOptions bodyCacheOptions() {
        return new BodyCacheOptions(true, this.properties.isCacheResponse(),
                this.properties.getMaxRequestSize().toBytes(), this.properties.getMaxResponseSize().toBytes(),
                this.properties.isIncludeMultipart());
    }

    /**
     * Creates the cached body filter.
     *
     * @param options request and response body-cache limits
     * @return the Servlet filter that exposes repeatable request and response bodies
     */
    @Bean
    @ConditionalOnMissingBean(CachedBodyFilter.class)
    public CachedBodyFilter cachedBodyFilter(BodyCacheOptions options) {
        return new CachedBodyFilter(options);
    }

}
