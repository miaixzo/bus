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
package org.miaixz.bus.starter.wrapper;

import java.lang.reflect.Method;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.options.WrapperRuntimeOptions;
import org.miaixz.bus.spring.http.AwareWebMvcConfigurer;
import org.miaixz.bus.spring.http.RuntimeContextBindingFilter;
import org.miaixz.bus.spring.http.SentinelRequestHandler;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.ForwardedHeaderFilter;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import jakarta.annotation.Resource;

/**
 * Configuration class for XSS protection and request/response content caching. This class configures web request
 * wrappers and filters.
 * <p>
 * Its main functions include:
 * <ul>
 * <li>Configuring request and response wrappers for XSS protection and content caching.</li>
 * <li>Customizing the request mapping handler to support automatic URL prefix generation based on package names.</li>
 * <li>Registering filters to wrap requests for specific HTTP methods.</li>
 * <li>Configuring Web MVC components like request handlers and MVC configurers.</li>
 * </ul>
 *
 * <pre>{@code
 * // In application.yml:
 * bus:
 *   wrapper:
 *     enabled: true       # Enable the wrapper
 *     order: -100         # Vector order
 *     base-packages:      # Packages for automatic URL prefix generation
 *       - com.example.controller
 *
 * // Controller class:
 * &#64;RestController
 * &#64;RequestMapping("/user")
 * public class UserController {
 *     // The actual access path will be prefixed with the package name,
 *     // e.g., /example/user/info
 *     &#64;GetMapping("/info")
 *     public UserInfo getUserInfo() {
 *         return userService.getUserInfo();
 *     }
 * }
 * }</pre>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(value = { WrapperProperties.class })
public class WrapperConfiguration implements WebMvcRegistrations {

    /**
     * Injected wrapper configuration properties.
     */
    @Resource
    private WrapperProperties properties;

    /**
     * Provides a custom {@link RequestMappingHandlerMapping}.
     * <p>
     * This custom handler supports automatic URL prefix generation based on the controller's package name.
     * </p>
     *
     * @return A custom {@link RequestMappingHandlerMapping} instance.
     */
    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new RequestMappingHandler();
    }

    /**
     * Registers the request body caching filter.
     * <p>
     * This method creates and configures a {@link RuntimeContextBindingFilter} to wrap requests for specific HTTP
     * methods, enabling XSS protection and request/response content caching.
     * </p>
     *
     * @return A configured {@link FilterRegistrationBean} for the body cache filter.
     */
    @Bean("registrationBodyCacheFilter")
    public FilterRegistrationBean<RuntimeContextBindingFilter> registrationBodyCacheFilter(
            WrapperRuntimeOptions options) {
        FilterRegistrationBean<RuntimeContextBindingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setEnabled(this.properties.isEnabled());
        registrationBean.setOrder(this.properties.getOrder());
        registrationBean.setFilter(new RuntimeContextBindingFilter(options));
        if (StringKit.isNotEmpty(this.properties.getName())) {
            registrationBean.setName(this.properties.getName());
        }
        if (MapKit.isNotEmpty(this.properties.getInitParameters())) {
            registrationBean.setInitParameters(this.properties.getInitParameters());
        }
        if (ObjectKit.isNotEmpty(this.properties.getServletRegistrationBeans())) {
            registrationBean.setServletRegistrationBeans(this.properties.getServletRegistrationBeans());
        }
        if (CollKit.isNotEmpty(this.properties.getServletNames())) {
            registrationBean.setServletNames(this.properties.getServletNames());
        }
        return registrationBean;
    }

    /**
     * Creates a {@link SentinelRequestHandler} bean.
     *
     * @return A new {@link SentinelRequestHandler} instance.
     */
    @Bean("supportRequestHandler")
    public SentinelRequestHandler requestHandler() {
        return new SentinelRequestHandler();
    }

    /**
     * Creates a {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurer} bean.
     *
     * @return A new {@link AwareWebMvcConfigurer} instance.
     */
    @Bean("supportWebMvcConfigurer")
    public org.springframework.web.servlet.config.annotation.WebMvcConfigurer supportWebMvcConfigurer(
            SentinelRequestHandler requestHandler,
            WrapperRuntimeOptions options) {
        return new AwareWebMvcConfigurer(this.properties.getAutoType(), this.properties.getPrefix(), requestHandler,
                options);
    }

    /**
     * Publishes the runtime wrapper compatibility snapshot.
     * <p>
     * The snapshot is derived from {@link WrapperProperties} and then stored as the shared runtime instance so that
     * filter, wrapper, resolver, and request-context helpers observe the same settings.
     *
     * @return The shared {@link WrapperRuntimeOptions} runtime snapshot.
     */
    @Bean("wrapperRuntimeOptions")
    public WrapperRuntimeOptions wrapperRuntimeOptions() {
        WrapperRuntimeOptions options = this.properties.runtimeOptions();
        WrapperRuntimeOptions.update(options);
        return options;
    }

    /**
     * Registers the {@link ForwardedHeaderFilter} bean.
     * <p>
     * This standard Spring Framework filter processes {@code Forwarded} and {@code X-Forwarded-*} headers, which is
     * essential when the application runs behind a reverse proxy.
     * </p>
     *
     * @return A {@link FilterRegistrationBean} for the {@link ForwardedHeaderFilter}.
     */
    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new ForwardedHeaderFilter());
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    /**
     * A custom {@link RequestMappingHandlerMapping} that supports automatic URL prefix generation based on package
     * names.
     */
    class RequestMappingHandler extends RequestMappingHandlerMapping {

        /**
         * Derives the mapping for a given handler method.
         * <p>
         * It checks if the controller's package matches any of the configured base packages. If it does, a URL prefix
         * is automatically generated from the package structure and prepended to the method's mapping.
         * </p>
         *
         * @param method      The method to map.
         * @param handlerType The class of the handler.
         * @return The derived {@link RequestMappingInfo}, or {@code null}.
         */
        @Override
        protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
            RequestMappingInfo requestMappingInfo = super.getMappingForMethod(method, handlerType);
            if (null != requestMappingInfo
                    && (handlerType.isAnnotationPresent(Controller.class)
                            || handlerType.isAnnotationPresent(RestController.class))
                    && ObjectKit.isNotEmpty(properties.getBasePackages())) {
                AntPathMatcher antPathMatcher = new AntPathMatcher(Symbol.DOT);
                for (String basePackage : properties.getBasePackages()) {
                    String packName = handlerType.getPackageName();
                    if (antPathMatcher.matchStart(packName, basePackage)
                            || antPathMatcher.matchStart(basePackage, packName)) {
                        String[] arrays = StringKit.splitToArray(basePackage, Symbol.DOT);
                        String prefix = StringKit.splitToArray(packName, arrays[arrays.length - 1])[1]
                                .replace(Symbol.C_DOT, Symbol.C_SLASH);
                        Logger.debug(
                                "Create a URL request mapping '{}{}' for {}.{}",
                                prefix,
                                requestMappingInfo.getPathPatternsCondition().getPatterns(),
                                packName,
                                handlerType.getSimpleName());
                        requestMappingInfo = RequestMappingInfo.paths(prefix).options(getBuilderConfiguration()).build()
                                .combine(requestMappingInfo);
                    }
                }
            }
            return requestMappingInfo;
        }
    }

}
