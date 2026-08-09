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
package org.miaixz.bus.starter.wrapper.routing;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.spring.web.routing.RoutePrefixHandlerMapping;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Configures controller route prefixes.
 *
 * @author Kimi Liu
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RoutePrefixProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = "org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping")
@ConditionalOnProperty(prefix = GeniusBuilder.WRAPPER_ROUTE_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = false)
public class RoutePrefixConfiguration {

    /**
     * Bound route prefix configuration properties.
     */
    private final RoutePrefixProperties properties;

    /**
     * Stores the normalized route-prefix policy used by the handler mapping.
     *
     * @param properties bound configuration properties
     */
    public RoutePrefixConfiguration(RoutePrefixProperties properties) {
        this.properties = properties;
    }

    /**
     * Creates the route prefix handler mapping.
     *
     * @return the handler mapping that applies configured route prefixes
     */
    @Bean
    @ConditionalOnMissingBean(RoutePrefixHandlerMapping.class)
    public RoutePrefixHandlerMapping routePrefixHandlerMapping() {
        return new RoutePrefixHandlerMapping(this.properties.toOptions());
    }

}
