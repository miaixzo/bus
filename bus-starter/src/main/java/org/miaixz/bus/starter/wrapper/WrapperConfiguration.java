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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.CollKit;
import org.miaixz.bus.core.xyz.MapKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.http.MutableRequestWrapper;
import org.miaixz.bus.spring.http.MutableResponseWrapper;
import org.miaixz.bus.spring.http.SentinelRequestHandler;
import org.miaixz.bus.spring.http.AwareWebMvcConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.webmvc.autoconfigure.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * XSS防护和请求/响应内容缓存配置类，用于配置Web请求的包装器和过滤器。
 *
 * 该类主要功能包括：
 * <ul>
 * <li>配置请求和响应包装器，实现XSS防护和内容缓存</li>
 * <li>自定义请求映射处理器，支持基于包名的URL前缀自动生成</li>
 * <li>注册过滤器，对特定HTTP方法的请求进行包装处理</li>
 * <li>配置Web MVC相关组件，如请求处理器和MVC配置器</li>
 * </ul>
 * 
 * <pre>
 * // 在application.yml中配置
 * bus:
 *   wrapper:
 *     enabled: true  # 启用包装器
 *     order: -100   # 过滤器顺序
 *     base-packages:  # 需要自动生成URL前缀的包名
 *       - com.example.controller
 *
 * // 控制器类
 * &#64;RestController
 * &#64;RequestMapping("/user")
 * public class UserController {
 *     // 实际访问路径会自动加上包名前缀，如 /example/user/info
 *     &#64;GetMapping("/info")
 *     public UserInfo getUserInfo() {
 *         return userService.getUserInfo();
 *     }
 * }
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@EnableConfigurationProperties(value = { WrapperProperties.class })
public class WrapperConfiguration implements WebMvcRegistrations {

    /**
     * 包装器配置属性，包含各种包装器相关的配置信息。 通过{@link EnableConfigurationProperties}注解自动注入。
     */
    @Resource
    WrapperProperties properties;

    /**
     * 获取自定义的请求映射处理器。
     *
     * <p>
     * 该方法返回一个自定义的{@link RequestMappingHandlerMapping}实例， 用于处理请求映射的生成，支持基于包名的URL前缀自动生成。
     * </p>
     *
     * @return 自定义的请求映射处理器
     */
    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new RequestMappingHandler();
    }

    /**
     * 注册请求体缓存过滤器。
     *
     * <p>
     * 该方法创建并配置一个{@link BodyCacheFilter}过滤器，用于对特定HTTP方法的请求进行包装处理， 实现XSS防护和请求/响应内容缓存。
     * </p>
     *
     * @return 配置好的过滤器注册Bean
     */
    @Bean("registrationBodyCacheFilter")
    public FilterRegistrationBean registrationBodyCacheFilter() {
        FilterRegistrationBean<BodyCacheFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setEnabled(this.properties.isEnabled());
        registrationBean.setOrder(this.properties.getOrder());
        registrationBean.setFilter(new BodyCacheFilter());
        if (!StringKit.isEmpty(this.properties.getName())) {
            registrationBean.setName(this.properties.getName());
        }
        if (MapKit.isNotEmpty(this.properties.getInitParameters())) {
            registrationBean.setInitParameters(this.properties.getInitParameters());
        }
        if (ObjectKit.isNotEmpty(this.properties.getServletRegistrationBeans())) {
            registrationBean.setServletRegistrationBeans(this.properties.getServletRegistrationBeans());
        }
        if (!CollKit.isEmpty(this.properties.getServletNames())) {
            registrationBean.setServletNames(this.properties.getServletNames());
        }
        return registrationBean;
    }

    /**
     * 创建请求处理器。
     *
     * <p>
     * 该方法创建一个{@link SentinelRequestHandler}实例，用于处理请求。
     * </p>
     *
     * @return 请求处理器实例
     */
    @Bean("supportRequestHandler")
    public SentinelRequestHandler requestHandler() {
        return new SentinelRequestHandler();
    }

    /**
     * 创建Web MVC配置器。
     *
     * <p>
     * 该方法创建一个{@link org.springframework.web.servlet.config.annotation.WebMvcConfigurer}实例， 用于配置Web MVC相关组件。
     * </p>
     *
     * @return Web MVC配置器实例
     */
    @Bean("supportWebMvcConfigurer")
    public org.springframework.web.servlet.config.annotation.WebMvcConfigurer supportWebMvcConfigurer() {
        return new AwareWebMvcConfigurer(this.properties.getAutoType(), this.properties.getPrefix(), requestHandler());
    }

    /**
     * 自定义的请求映射处理器，支持基于包名的URL前缀自动生成。
     */
    class RequestMappingHandler extends RequestMappingHandlerMapping {

        /**
         * 获取方法的请求映射信息。
         *
         * <p>
         * 该方法在生成请求映射信息时，会检查控制器类所在的包是否在配置的基础包中， 如果是，则自动添加包名前缀到URL路径中。
         * </p>
         *
         * @param method      控制器方法
         * @param handlerType 控制器类
         * @return 请求映射信息
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
                        Logger.debug("Create a URL request mapping '" + prefix
                                + Arrays.toString(requestMappingInfo.getPathPatternsCondition().getPatterns().toArray())
                                + "' for " + packName + Symbol.C_DOT + handlerType.getSimpleName());
                        requestMappingInfo = RequestMappingInfo.paths(prefix).options(getBuilderConfiguration()).build()
                                .combine(requestMappingInfo);
                    }
                }
            }
            return requestMappingInfo;
        }
    }

    /**
     * 请求体缓存过滤器，用于对特定HTTP方法的请求进行包装处理。
     */
    class BodyCacheFilter extends OncePerRequestFilter {

        /**
         * 执行过滤操作。
         *
         * <p>
         * 该方法对POST、PATCH、PUT等HTTP方法的请求进行包装处理， 实现XSS防护和请求/响应内容缓存。
         * </p>
         *
         * @param request     HTTP请求对象
         * @param response    HTTP响应对象
         * @param filterChain 过滤器链
         * @throws ServletException 如果发生Servlet异常
         * @throws IOException      如果发生I/O异常
         */
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {
            final String method = request.getMethod();
            // 如果不是 POST PATCH PUT 等有流的接口则无需进行类型转换,提高性能
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