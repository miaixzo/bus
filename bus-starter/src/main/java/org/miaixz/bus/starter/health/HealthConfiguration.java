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

import org.miaixz.bus.core.xyz.AnnoKit;
import org.miaixz.bus.health.Provider;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.starter.annotation.EnableHealth;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.availability.ApplicationAvailabilityBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

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
     * @return {@link Provider}
     */
    @Bean
    @Conditional(EnableHealthCondition.class)
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
     * @return {@link HealthProviderService}
     */
    @Bean
    @Conditional(EnableHealthCondition.class)
    public HealthProviderService healthProviderService(ApplicationEventPublisher publisher,
            ApplicationAvailability availability, Provider provider) {
        return new HealthProviderService(properties, provider, publisher, availability);
    }

    /**
     * Spring 应用可用性接口，用于获取和检查当前的存活状态和就绪状态。
     *
     * @return {@link ApplicationAvailability}
     */
    @Bean
    @Conditional(EnableHealthCondition.class)
    public ApplicationAvailability availability() {
        return new ApplicationAvailabilityBean();
    }

    /**
     * Spring 应用事件发布器，用于发布可用性状态变更事件。
     *
     * @param applicationContext Spring 应用上下文
     * @return {@link ApplicationEventPublisher}
     */
    @Bean
    @Conditional(EnableHealthCondition.class)
    public ApplicationEventPublisher publisher(ApplicationContext applicationContext) {
        return event -> {
            if (event != null) {
                applicationContext.publishEvent(event);
            } else {
                Logger.warn("Null event received");
            }
        };
    }

    /**
     * 定义 HealthController Bean，并手动注册REST端点。
     *
     * @param healthService  健康状态服务
     * @param handlerMapping 用于注册端点
     * @return {@link HealthController}
     */
    @Bean
    @Conditional(EnableHealthCondition.class)
    public HealthController healthController(HealthProviderService healthService,
            RequestMappingHandlerMapping handlerMapping) {
        HealthController controller = new HealthController(healthService);
        try {
            // 注册/healthz端点（支持GET和POST）
            RequestMappingInfo healthzMapping = RequestMappingInfo.paths("/healthz")
                    .methods(RequestMethod.GET, RequestMethod.POST).build();
            handlerMapping.registerMapping(healthzMapping, controller,
                    HealthController.class.getMethod("healthz", String.class));

            // 注册/broken端点
            RequestMappingInfo brokenMapping = RequestMappingInfo.paths("/broken")
                    .methods(RequestMethod.GET, RequestMethod.POST).build();
            handlerMapping.registerMapping(brokenMapping, controller, HealthController.class.getMethod("broken"));

            // 注册/correct端点
            RequestMappingInfo correctMapping = RequestMappingInfo.paths("/correct")
                    .methods(RequestMethod.GET, RequestMethod.POST).build();
            handlerMapping.registerMapping(correctMapping, controller, HealthController.class.getMethod("correct"));

            // 注册/accept端点
            RequestMappingInfo acceptMapping = RequestMappingInfo.paths("/accept")
                    .methods(RequestMethod.GET, RequestMethod.POST).build();
            handlerMapping.registerMapping(acceptMapping, controller, HealthController.class.getMethod("accept"));

            // 注册/refuse端点
            RequestMappingInfo refuseMapping = RequestMappingInfo.paths("/refuse")
                    .methods(RequestMethod.GET, RequestMethod.POST).build();
            handlerMapping.registerMapping(refuseMapping, controller, HealthController.class.getMethod("refuse"));

        } catch (NoSuchMethodException e) {
            Logger.error("Failed to register HealthController mappings: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to register mapping", e);
        }
        return controller;
    }

    /**
     * 条件类：检查是否应用了 @EnableHealth 注解
     */
    static class EnableHealthCondition implements Condition {
        /**
         * 检查 Spring 上下文中是否存在 @EnableHealth 注解的 Bean
         *
         * @param context  Spring 条件上下文
         * @param metadata 注解元数据（未使用）
         * @return the true/false
         */
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            try {
                for (String beanName : context.getBeanFactory().getBeanDefinitionNames()) {
                    String beanClassName = context.getBeanFactory().getBeanDefinition(beanName).getBeanClassName();
                    if (beanClassName != null) {
                        Class<?> beanClass = Class.forName(beanClassName);
                        if (AnnoKit.hasAnnotation(beanClass, EnableHealth.class)) {
                            return true;
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                Logger.error("Failed to check EnableHealth annotation: {}", e.getMessage(), e);
            }
            return false;
        }
    }

}