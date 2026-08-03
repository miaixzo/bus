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
package org.miaixz.bus.spring.web.routing;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.logger.Logger;

/**
 * Applies an explicit prefix only to controllers matching configured package rules.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RoutePrefixHandlerMapping extends RequestMappingHandlerMapping {

    /**
     * Bound route prefix handler mapping configuration properties.
     */
    private final RoutePrefixProperties properties;
    /**
     * Path matcher used to compose route prefixes with controller mappings.
     */
    private final AntPathMatcher packageMatcher = new AntPathMatcher(Symbol.DOT);

    /**
     * Creates the route prefix handler mapping.
     *
     * @param properties bound configuration properties
     */
    public RoutePrefixHandlerMapping(RoutePrefixProperties properties) {
        this.properties = Objects.requireNonNull(properties, "properties");
    }

    /**
     * Publishes the finalized handler mapping set for route-prefix diagnostics.
     *
     * @param handlerMethods initialized request mappings
     */
    @Override
    protected void handlerMethodsInitialized(Map<RequestMappingInfo, HandlerMethod> handlerMethods) {
        if (properties.isInStorage()) {
            Logger.debug(false, "Starter", "Request mappings initialized: count={}", handlerMethods.size());
        }
    }

    /**
     * Prepends the configured application prefix to one controller method mapping.
     *
     * @param method      controller method
     * @param handlerType controller type
     * @return prefixed mapping, original mapping, or {@code null} when the method is not mapped
     */
    @Override
    protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
        RequestMappingInfo mapping = super.getMappingForMethod(method, handlerType);
        if (mapping == null || !isController(handlerType)) {
            return mapping;
        }
        for (String basePackage : properties.getBasePackages()) {
            String packageName = handlerType.getPackageName();
            if (!packageMatcher.matchStart(basePackage, packageName)
                    && !packageMatcher.matchStart(packageName, basePackage)) {
                continue;
            }
            String routePrefix = properties.getPrefix() + packageSuffix(basePackage, packageName);
            if (routePrefix.isEmpty()) {
                return mapping;
            }
            return RequestMappingInfo.paths(routePrefix).options(getBuilderConfiguration()).build().combine(mapping);
        }
        return mapping;
    }

    /**
     * Tests whether a handler type is a controller eligible for automatic route prefixing.
     *
     * @param handlerType handler type
     * @return whether controller
     */
    private boolean isController(Class<?> handlerType) {
        return handlerType.isAnnotationPresent(Controller.class)
                || handlerType.isAnnotationPresent(RestController.class);
    }

    /**
     * Extracts the package suffix following the configured base package.
     *
     * @param basePackage base package
     * @param packageName package name
     * @return the relative package suffix, or an empty string for the base package
     */
    private String packageSuffix(String basePackage, String packageName) {
        if (basePackage.indexOf(Symbol.C_STAR) >= 0 || !packageName.startsWith(basePackage)) {
            return Normal.EMPTY;
        }
        String suffix = packageName.substring(basePackage.length()).replace(Symbol.C_DOT, Symbol.C_SLASH);
        return suffix.isEmpty() ? Normal.EMPTY : (suffix.startsWith(Symbol.SLASH) ? suffix : Symbol.SLASH + suffix);
    }

}
