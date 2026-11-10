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
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.spring.web.routing.RouteErrorResolver;
import org.miaixz.bus.starter.wrapper.routing.RouteErrorProperties.Target;

/**
 * Registers route-specific HTTP error handling for Servlet MVC applications.
 * <p>
 * Registers the adapter when Spring MVC is available. The adapter selects its policy at startup, which allows an AOT
 * build to use deployment settings for {@code enabled} and {@code targets}. An application-supplied
 * {@link RouteErrorResolver} takes precedence over the default bean.
 *
 * @author Kimi Liu
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RouteErrorProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(name = "org.springframework.web.servlet.DispatcherServlet")
public class RouteErrorConfiguration {

    /**
     * Creates the Servlet MVC route error configuration.
     */
    public RouteErrorConfiguration() {
        // The resolver bean receives the bound properties when Spring creates it.
    }

    /**
     * Creates the resolver that runs before the default MVC exception resolver chain.
     *
     * @param properties bound route prefixes, HTTP statuses, and optional cache policy
     * @return resolver that applies the MVC policy when selected
     * @throws IllegalArgumentException if the route error settings are invalid
     */
    @Bean
    @ConditionalOnMissingBean(RouteErrorResolver.class)
    public RouteErrorResolver routeErrorResolver(RouteErrorProperties properties) {
        return new RouteErrorResolver(properties.policy(Target.MVC).orElse(null));
    }

}
