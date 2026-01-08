/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import jakarta.annotation.Resource;
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

/**
 * Auto-configuration for application health status and monitoring.
 * <p>
 * This configuration class sets up all the necessary beans for the health monitoring feature, including the data
 * provider, the service layer, and the controller with its endpoints. The entire configuration is conditional on the
 * presence of the {@link EnableHealth} annotation on a user-defined configuration class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@EnableConfigurationProperties(value = { HealthProperties.class })
public class HealthConfiguration {

    @Resource
    private HealthProperties properties;

    /**
     * Creates the {@link Provider} bean, which is responsible for gathering raw system and hardware information.
     *
     * @return A new {@link Provider} instance.
     * @throws IllegalStateException if the provider fails to initialize.
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
     * Creates the {@link HealthService} bean, which orchestrates health data retrieval and state changes.
     *
     * @param publisher    The application event publisher.
     * @param availability The application availability manager.
     * @param provider     The system information provider.
     * @return A new {@link HealthService} instance.
     */
    @Bean
    @Conditional(EnableHealthCondition.class)
    public HealthService healthProviderService(
            ApplicationEventPublisher publisher,
            ApplicationAvailability availability,
            Provider provider) {
        return new HealthService(properties, provider, publisher, availability);
    }

    /**
     * Creates the {@link ApplicationAvailability} bean, which tracks and provides the application's liveness and
     * readiness states.
     *
     * @return A new {@link ApplicationAvailabilityBean} instance.
     */
    @Bean
    @Conditional(EnableHealthCondition.class)
    public ApplicationAvailability availability() {
        return new ApplicationAvailabilityBean();
    }

    /**
     * Creates the {@link ApplicationEventPublisher} bean, used to broadcast availability state changes.
     *
     * @param applicationContext The Spring application context.
     * @return An {@link ApplicationEventPublisher} that delegates to the application context.
     */
    @Bean
    @Conditional(EnableHealthCondition.class)
    public ApplicationEventPublisher publisher(ApplicationContext applicationContext) {
        return applicationContext::publishEvent;
    }

    /**
     * Creates the {@link HealthController} bean and manually registers its REST endpoints.
     * <p>
     * Manual registration is used here to provide more control over the endpoint paths and methods, independent of a
     * class-level {@code @RequestMapping}.
     * </p>
     *
     * @param healthService  The health service to be used by the controller.
     * @param handlerMapping The Spring request mapping handler for registering endpoints.
     * @return The configured {@link HealthController} instance.
     * @throws RuntimeException if the controller methods cannot be found for mapping.
     */
    @Bean
    @Conditional(EnableHealthCondition.class)
    public HealthController healthController(HealthService healthService, RequestMappingHandlerMapping handlerMapping) {
        HealthController controller = new HealthController(healthService);
        try {
            // Register /healthz endpoint (supports GET and POST)
            registerMapping(handlerMapping, "/healthz", controller, "healthz", String.class);
            // Register state-changing endpoints
            registerMapping(handlerMapping, "/broken", controller, "broken");
            registerMapping(handlerMapping, "/correct", controller, "correct");
            registerMapping(handlerMapping, "/accept", controller, "accept");
            registerMapping(handlerMapping, "/refuse", controller, "refuse");
        } catch (NoSuchMethodException e) {
            Logger.error("Failed to register HealthController mappings: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to register health mappings", e);
        }
        return controller;
    }

    /**
     * Helper method to register a request mapping for the health controller.
     */
    private void registerMapping(
            RequestMappingHandlerMapping handlerMapping,
            String path,
            Object handler,
            String methodName,
            Class<?>... parameterTypes) throws NoSuchMethodException {
        RequestMappingInfo mappingInfo = RequestMappingInfo.paths(path).methods(RequestMethod.GET, RequestMethod.POST)
                .build();
        handlerMapping.registerMapping(mappingInfo, handler, handler.getClass().getMethod(methodName, parameterTypes));
    }

    /**
     * A {@link Condition} that checks for the presence of the {@link EnableHealth} annotation.
     * <p>
     * This condition ensures that the health monitoring beans are only created when the feature is explicitly enabled
     * by the user.
     */
    static class EnableHealthCondition implements Condition {

        /**
         * Determines if the condition matches.
         *
         * @param context  The condition context.
         * @param metadata The metadata of the class or method being checked.
         * @return {@code true} if a bean with the {@link EnableHealth} annotation is found, {@code false} otherwise.
         */
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            // The condition matches if any bean with the @EnableHealth annotation exists.
            return !context.getBeanFactory().getBeansWithAnnotation(EnableHealth.class).isEmpty();
        }
    }

}
