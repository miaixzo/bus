/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Xss/重复读取等WEB封装配置
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@EnableConfigurationProperties(value = { WrapperProperties.class })
public class WrapperConfiguration implements WebMvcRegistrations {

    @Resource
    WrapperProperties properties;

    @Override
    public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
        return new RequestMappingHandler();
    }

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

    @Bean("supportWebMvcConfigurer")
    public WebMvcConfigurer supportWebMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new GenieWrapperHandler());
            }
        };
    }

    class RequestMappingHandler extends RequestMappingHandlerMapping {

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

    class BodyCacheFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                FilterChain filterChain) throws ServletException, IOException {
            final String method = request.getMethod();
            // 如果不是 POST PATCH PUT 等有流的接口则无需进行类型转换,提高性能
            if (HTTP.POST.equals(method) || HTTP.PATCH.equals(method) || HTTP.PUT.equals(method)) {
                if (!(request instanceof CacheRequestWrapper)) {
                    request = new CacheRequestWrapper(request);
                }
            }
            if (!(response instanceof CacheResponseWrapper)) {
                response = new CacheResponseWrapper(response);
            }
            filterChain.doFilter(request, response);
        }
    }

}
