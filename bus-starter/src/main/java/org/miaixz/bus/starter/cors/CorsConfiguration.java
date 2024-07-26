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
package org.miaixz.bus.starter.cors;

import jakarta.annotation.Resource;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.ObjectKit;
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
 * @since Java 17+
 */
@EnableConfigurationProperties(value = { CorsProperties.class })
public class CorsConfiguration {

    @Resource
    CorsProperties properties;

    /**
     * 跨域过滤器
     *
     * @return Cors过滤器
     */
    @Bean
    @ConditionalOnMissingBean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration(this.properties.getPath(), buildConfig());
        return new CorsFilter(source);
    }

    private org.springframework.web.cors.CorsConfiguration buildConfig() {
        org.springframework.web.cors.CorsConfiguration corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList(this.properties.getAllowedOrigins()));
        corsConfiguration.setAllowedHeaders(Arrays.asList(this.properties.getAllowedHeaders()));
        corsConfiguration.setAllowedMethods(Arrays.asList(this.properties.getAllowedMethods()));
        // 是否发送 Cookie 信息
        corsConfiguration.setAllowCredentials(this.properties.getAllowCredentials());
        if (ObjectKit.isNotNull(this.properties.getMaxAge())) {
            corsConfiguration.setMaxAge(this.properties.getMaxAge());
        }
        if (ArrayKit.isNotEmpty(this.properties.getExposedHeaders())) {
            corsConfiguration.setExposedHeaders(Arrays.asList(this.properties.getExposedHeaders()));
        }
        return corsConfiguration;
    }

}
