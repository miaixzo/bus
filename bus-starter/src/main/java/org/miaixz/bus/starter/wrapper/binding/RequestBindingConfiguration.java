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
package org.miaixz.bus.starter.wrapper.binding;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.spring.web.resolver.AutoBindingTypeMatcher;
import org.miaixz.bus.spring.web.resolver.RequestBindingOptions;
import org.miaixz.bus.spring.web.resolver.RequestObjectArgumentResolver;
import org.miaixz.bus.spring.web.resolver.RequestWebMvcConfigurer;
import org.miaixz.bus.starter.GeniusBuilder;

/**
 * Configures request-object binding for Servlet web applications.
 *
 * @author Kimi Liu
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(RequestBindingProperties.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = GeniusBuilder.WRAPPER_REQUEST_BINDING, name = "enabled", havingValue = "true", matchIfMissing = true)
public class RequestBindingConfiguration {

    /**
     * Initializes automatic request-object binding when the wrapper binding feature is enabled.
     */
    public RequestBindingConfiguration() {
        // No initialization required.
    }

    /**
     * Creates the request binding options.
     *
     * @return immutable options controlling request-object binding
     */
    @Bean
    @ConditionalOnMissingBean(RequestBindingOptions.class)
    public RequestBindingOptions requestBindingOptions() {
        return new RequestBindingOptions();
    }

    /**
     * Creates the request object argument resolver.
     *
     * @param options request-binding options used by the resolver
     * @return the Spring MVC resolver for request objects
     */
    @Bean
    @ConditionalOnMissingBean(RequestObjectArgumentResolver.class)
    public RequestObjectArgumentResolver requestObjectArgumentResolver(RequestBindingOptions options) {
        return new RequestObjectArgumentResolver(new AutoBindingTypeMatcher(), options);
    }

    /**
     * Registers the unified request-object resolver with Spring MVC.
     *
     * @param options  request binding options
     * @param resolver unified request-object resolver
     * @return MVC configurer that installs the resolver
     */
    @Bean
    @ConditionalOnMissingBean(RequestWebMvcConfigurer.class)
    public RequestWebMvcConfigurer requestWebMvcConfigurer(
            RequestBindingOptions options,
            RequestObjectArgumentResolver resolver) {
        return new RequestWebMvcConfigurer(options, resolver);
    }

}
