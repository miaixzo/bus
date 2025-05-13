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
package org.miaixz.bus.starter.health;

import org.miaixz.bus.health.Provider;
import org.miaixz.bus.logger.Logger;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.ApplicationAvailabilityBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.Resource;

/**
 * 健康状态
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@EnableConfigurationProperties(value = { HealthProperties.class })
public class HealthConfiguration {

    @Resource
    HealthProperties properties;

    /**
     * 定义 Provider Bean，延迟初始化并捕获异常
     *
     * @return Provider 实例
     */
    @Bean
    public Provider provider() {
        try {
            return new Provider();
        } catch (Exception e) {
            Logger.error("Failed to initialize Provider: {}", e.getMessage(), e);
            throw new IllegalStateException("Failed to initialize Provider: " + e.getMessage(), e);
        }
    }

    /**
     * 定义 HealthProviderService Bean。
     *
     * @param publisher    Spring 应用事件发布器
     * @param availability Spring 应用可用性接口
     * @param provider     系统信息提供者
     * @return HealthProviderService 实例
     */
    @Bean
    public HealthProviderService healthProviderService(ApplicationEventPublisher publisher,
            ApplicationAvailability availability, Provider provider) {
        return new HealthProviderService(properties, provider, publisher, availability);
    }

    /**
     * 定义 HealthController Bean。
     *
     * @param healthService 健康状态服务
     * @return HealthController 实例
     */
    @Bean
    public HealthController healthController(HealthProviderService healthService) {
        return new HealthController(healthService);
    }

    /**
     * Spring 应用可用性接口，用于获取和检查当前的存活状态和就绪状态。
     *
     * @return ApplicationAvailability 实例
     */
    @Bean
    public ApplicationAvailability availability() {
        return new ApplicationAvailabilityBean();
    }

    /**
     * Spring 应用事件发布器，用于发布可用性状态变更事件。
     *
     * @param applicationContext Spring 应用上下文
     * @return ApplicationEventPublisher 实例
     */
    @Bean
    public ApplicationEventPublisher publisher(ApplicationContext applicationContext) {
        return applicationContext;
    }

}