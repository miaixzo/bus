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
package org.miaixz.bus.spring.web.resolver;

import java.util.List;
import java.util.Objects;

import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Registers the unified application request-object argument resolver.
 *
 * @author Kimi Liu
 */
public class RequestWebMvcConfigurer implements WebMvcConfigurer {

    /**
     * Request-binding options shared with the argument resolver.
     */
    private final RequestBindingOptions options;
    /**
     * Argument resolver registered with Spring MVC.
     */
    private final RequestObjectArgumentResolver resolver;

    /**
     * Creates a resolver-only MVC configurer.
     *
     * @param options  request-binding options shared with Spring MVC
     * @param resolver request-object argument resolver to register
     */
    public RequestWebMvcConfigurer(RequestBindingOptions options, RequestObjectArgumentResolver resolver) {
        this.options = Objects.requireNonNull(options, "options");
        this.resolver = Objects.requireNonNull(resolver, "resolver");
    }

    /**
     * Registers the request-object resolver when request binding is enabled.
     *
     * @param resolvers mutable MVC argument resolver list
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        Objects.requireNonNull(this.options, "options");
        if (resolvers.stream().noneMatch(RequestObjectArgumentResolver.class::isInstance)) {
            resolvers.add(this.resolver);
        }
    }

}
