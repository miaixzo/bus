/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.wrapper;

import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.http.AwareWebMvcConfigurer;
import org.miaixz.bus.spring.http.MutableRequestWrapper;
import org.miaixz.bus.spring.http.MutableResponseWrapper;
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
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.lang.reflect.Method;

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
 *     order: -100         # Filter order
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
 * @since Java 17+
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
     * This method creates and configures a {@link BodyCacheFilter} to wrap requests for specific HTTP methods, enabling
     * XSS protection and request/response content caching.
     * </p>
     *
     * @return A configured {@link FilterRegistrationBean} for the body cache filter.
     */
    @Bean("registrationBodyCacheFilter")
    public FilterRegistrationBean<BodyCacheFilter> registrationBodyCacheFilter() {
        FilterRegistrationBean<BodyCacheFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setEnabled(this.properties.isEnabled());
        registrationBean.setOrder(this.properties.getOrder());
        registrationBean.setFilter(new BodyCacheFilter());
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
    public org.springframework.web.servlet.config.annotation.WebMvcConfigurer supportWebMvcConfigurer() {
        return new AwareWebMvcConfigurer(this.properties.getAutoType(), this.properties.getPrefix(), requestHandler());
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

    /**
     * A filter that caches the request body by wrapping the {@link HttpServletRequest}. This allows the request body to
     * be read multiple times, which is necessary for features like XSS filtering and logging.
     */
    class BodyCacheFilter extends OncePerRequestFilter {

        /**
         * Wraps the request and response if necessary.
         * <p>
         * This method wraps the {@link HttpServletRequest} in a {@link MutableRequestWrapper} for HTTP methods that
         * typically contain a body (POST, PATCH, PUT) to allow for repeatable reads. The {@link HttpServletResponse} is
         * also wrapped to allow for response body manipulation or caching.
         * </p>
         *
         * @param request     The original HTTP request.
         * @param response    The original HTTP response.
         * @param filterChain The filter chain.
         * @throws ServletException if a servlet-specific error occurs.
         * @throws IOException      if an I/O error occurs.
         */
        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {
            final String method = request.getMethod();
            // Only wrap requests with a body to improve performance.
            if (HTTP.POST.equals(method) || HTTP.PATCH.equals(method) || HTTP.PUT.equals(method)) {
                if (!(request instanceof MutableRequestWrapper)) {
                    request = new MutableRequestWrapper(request);
                }
            }
            if (!(response instanceof MutableResponseWrapper)) {
                response = new MutableResponseWrapper(response);
            }
            filterChain.doFilter(request, response);
        }
    }

}
