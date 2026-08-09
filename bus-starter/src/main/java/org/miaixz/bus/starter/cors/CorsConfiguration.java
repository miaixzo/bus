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
package org.miaixz.bus.starter.cors;

import java.util.Arrays;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableCors;

/**
 * Configures CORS (Cross-Origin Resource Sharing) support. This class enables and configures the CORS filter based on
 * the properties defined in {@link CorsProperties}.
 *
 * @author Kimi Liu
 */
@EnableConfigurationProperties(value = { CorsProperties.class })
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.springframework.web.cors.CorsConfigurationSource")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnEnabled(annotation = EnableCors.class, prefix = GeniusBuilder.CORS)
public class CorsConfiguration {

    /**
     * Bound cors configuration properties.
     */
    private final CorsProperties properties;

    /**
     * Stores the origin, method, header, and credential policy used by the CORS filter.
     *
     * @param properties bound configuration properties
     */
    public CorsConfiguration(CorsProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates and configures the {@link CorsFilter} bean. This bean is conditional and will only be created if no other
     * {@link CorsFilter} bean is present in the context.
     *
     * @return The configured {@link CorsFilter}.
     */
    @Bean
    @ConditionalOnMissingBean(CorsFilter.class)
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(this.properties.getPath(), buildConfig());
        return new CorsFilter(source);
    }

    /**
     * Builds the Spring {@link org.springframework.web.cors.CorsConfiguration} object from the custom
     * {@link CorsProperties}.
     *
     * @return The configured {@link org.springframework.web.cors.CorsConfiguration} instance.
     */
    private org.springframework.web.cors.CorsConfiguration buildConfig() {
        if (Boolean.TRUE.equals(this.properties.getAllowCredentials())
                && Arrays.asList(this.properties.getAllowedOrigins()).contains(Symbol.STAR)) {
            throw new IllegalStateException(
                    "bus.cors.allowed-origins must not contain '*' when bus.cors.allow-credentials=true");
        }
        org.springframework.web.cors.CorsConfiguration corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList(this.properties.getAllowedOrigins()));
        corsConfiguration.setAllowedHeaders(Arrays.asList(this.properties.getAllowedHeaders()));
        corsConfiguration.setAllowedMethods(Arrays.asList(this.properties.getAllowedMethods()));
        // Whether to send cookie information
        corsConfiguration.setAllowCredentials(this.properties.getAllowCredentials());
        if (ObjectKit.isNotNull(this.properties.getMaxAge())) {
            corsConfiguration.setMaxAge(this.properties.getMaxAge());
        }
        if (ArrayKit.isNotEmpty(this.properties.getExposedHeaders())) {
            corsConfiguration.setExposedHeaders(Arrays.asList(this.properties.getExposedHeaders()));
        }
        return corsConfiguration;
    }

}
