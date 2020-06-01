/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.starter.wrapper;

import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.toolkit.CollKit;
import org.aoju.bus.core.toolkit.MapKit;
import org.aoju.bus.core.toolkit.ObjectKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Xss/重复读取等配置
 *
 * @author Kimi Liu
 * @version 5.9.6
 * @since JDK 1.8+
 */
@EnableConfigurationProperties({WrapperProperties.class})
public class WrapperConfiguration {

    @Autowired
    WrapperProperties properties;

    @Bean("registrationBodyCacheFilter")
    public FilterRegistrationBean registrationBodyCacheFilter() {
        FilterRegistrationBean<BodyCacheFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setEnabled(this.properties.getEnabled());
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

    private static class BodyCacheFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
            final String method = request.getMethod();
            // 如果不是 POST PATCH PUT 等有流的接口则无需进行类型转换,提高性能
            if (Http.POST.equals(method) || Http.PATCH.equals(method) || Http.PUT.equals(method)) {
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
