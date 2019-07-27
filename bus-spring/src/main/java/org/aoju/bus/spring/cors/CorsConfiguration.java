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
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
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
