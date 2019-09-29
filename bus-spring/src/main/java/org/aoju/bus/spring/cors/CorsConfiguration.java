/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.spring.cors;

import org.aoju.bus.core.utils.ArrayUtils;
import org.aoju.bus.core.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * Cors 跨域支持
 *
 * @author Kimi Liu
 * @version 3.6.1
 * @since JDK 1.8
 */
@EnableConfigurationProperties(value = {CorsProperties.class})
public class CorsConfiguration {

    @Autowired
    CorsProperties properties;

    private org.springframework.web.cors.CorsConfiguration buildConfig() {
        org.springframework.web.cors.CorsConfiguration corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList(properties.getAllowedOrigins()));
        corsConfiguration.setAllowedHeaders(Arrays.asList(properties.getAllowedHeaders()));
        corsConfiguration.setAllowedMethods(Arrays.asList(properties.getAllowedMethods()));
        // 是否发送 Cookie 信息
        corsConfiguration.setAllowCredentials(properties.getAllowCredentials());
        if (ObjectUtils.isNotNull(properties.getMaxAge())) {
            corsConfiguration.setMaxAge(properties.getMaxAge());
        }
        if (ArrayUtils.isNotEmpty(properties.getExposedHeaders())) {
            corsConfiguration.setExposedHeaders(Arrays.asList(properties.getExposedHeaders()));
        }
        return corsConfiguration;
    }

    /**
     * 跨域过滤器
     *
     * @return Cors过滤器
     */
    @Bean
    @ConditionalOnMissingBean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(properties.getPath(), buildConfig());
        return new CorsFilter(source);
    }

}
