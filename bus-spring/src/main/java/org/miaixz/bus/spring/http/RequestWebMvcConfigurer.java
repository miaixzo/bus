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
package org.miaixz.bus.spring.http;

import org.miaixz.bus.spring.options.WrapperRuntimeOptions;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Configures request object argument resolution for Spring MVC.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class RequestWebMvcConfigurer implements WebMvcConfigurer {

    /**
     * Runtime request-wrapper options shared with argument resolvers.
     */
    private final WrapperRuntimeOptions options;

    /**
     * Creates the request MVC configurer.
     *
     * @param prefix  optional URL prefix for controllers
     * @param handler Sentinel request interceptor
     * @param options runtime request-wrapper options
     */
    public RequestWebMvcConfigurer(String prefix, SentinelRequestHandler handler, WrapperRuntimeOptions options) {
        this.options = options == null ? WrapperRuntimeOptions.of() : options;
    }

    /**
     * Registers the request-wrapper-aware controller argument resolver.
     *
     * @param resolvers argument resolver list
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        boolean alreadyRegistered = resolvers.stream().anyMatch(RequestObjectArgumentResolver.class::isInstance);
        if (!alreadyRegistered) {
            resolvers.add(new RequestObjectArgumentResolver(this.options));
        }
    }

}
