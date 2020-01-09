/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.starter.metric;

import org.aoju.bus.logger.Logger;
import org.aoju.bus.metric.ApiConfig;
import org.aoju.bus.metric.ApiHandler;
import org.aoju.bus.metric.process.ConfigClient;
import org.aoju.bus.metric.support.ApiController;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * API网关配置
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8++
 */
@EnableConfigurationProperties(MetricProperties.class)
public class MetricConfiguration {

    @Autowired
    MetricProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public ApiConfig apiConfig() {
        return new ApiConfig();
    }

    /**
     * 默认入口
     */
    @Controller
    @RequestMapping("api")
    public class EayopenIndexController extends ApiController {

        @Autowired
        private ApiConfig config;

        @Override
        protected void initApiConfig(ApiConfig apiConfig) {
            BeanUtils.copyProperties(properties, apiConfig);
            this.initInterceptor(properties, apiConfig);
            this.initOpenClient(properties, apiConfig);
        }

        private void initInterceptor(MetricProperties properties, ApiConfig apiConfig) {
            if (!CollectionUtils.isEmpty(properties.getInterceptors())) {
                List<String> interceptors = properties.getInterceptors();
                ApiHandler[] apiInterceptor = new ApiHandler[interceptors.size()];
                for (int i = 0; i < interceptors.size(); i++) {
                    String interceptorClassName = interceptors.get(i);
                    try {
                        apiInterceptor[i] = (ApiHandler) Class.forName(interceptorClassName).newInstance();
                    } catch (Exception e) {
                        Logger.error("Class.forName({}).newInstance() error", interceptorClassName, e);
                        throw new RuntimeException(e);
                    }
                }
                apiConfig.setInterceptors(apiInterceptor);
            }
        }

        private void initOpenClient(MetricProperties properties, ApiConfig apiConfig) {
            String ip = properties.getConfigServerIp();
            String port = properties.getConfigServerPort();
            if (StringUtils.hasText(ip) && StringUtils.hasText(port)) {
                // appName 应用名称
                // host    配置中心ip
                // port    配置中心端口
                String docUrl = properties.getDocUrl();
                ConfigClient configClient = new ConfigClient(properties.getAppName(), ip, Integer.valueOf(port), docUrl);
                apiConfig.setConfigClient(configClient);
            }
        }

        @Override
        protected ApiConfig newApiConfig() {
            return config;
        }

    }

}
