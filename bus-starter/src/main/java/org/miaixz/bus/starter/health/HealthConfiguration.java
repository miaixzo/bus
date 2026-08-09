/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.starter.health;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.availability.ApplicationAvailability;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import org.miaixz.bus.health.Collector;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableHealth;

/**
 * Configures application availability monitoring and its lightweight HTTP endpoint.
 * <p>
 * The configuration reuses the Bus Health collector for explicitly selected details while the endpoint itself reports
 * Spring Boot's in-memory liveness and readiness states without requiring Actuator.
 *
 * @author Kimi Liu
 */
@EnableConfigurationProperties(value = { HealthProperties.class })
@Configuration(proxyBeanMethods = false)
@ConditionalOnEnabled(annotation = EnableHealth.class, prefix = GeniusBuilder.HEALTH)
public class HealthConfiguration {

    /**
     * Creates the observer for Spring Boot availability state changes.
     *
     * @return availability event observer
     */
    @Bean
    @ConditionalOnMissingBean(AvailabilityListener.class)
    public AvailabilityListener availabilityListener() {
        return new AvailabilityListener();
    }

    /**
     * Creates the collector responsible for gathering system and hardware information.
     *
     * @return system and hardware information collector
     * @throws IllegalStateException if the collector fails to initialize
     */
    @Bean
    @ConditionalOnMissingBean(Collector.class)
    public Collector collector() {
        try {
            return new Collector();
        } catch (Exception e) {
            Logger.error(
                    false,
                    "Starter",
                    "Health failed to initialize Collector: {}",
                    e.getClass().getSimpleName(),
                    e);
            throw new IllegalStateException("Failed to initialize Collector: " + e.getMessage(), e);
        }
    }

    /**
     * Creates the service that combines application availability with selected Bus Health details.
     *
     * @param properties   health endpoint properties
     * @param collector    system and hardware information collector
     * @param publisher    application event publisher
     * @param availability current application availability state
     * @return health query service
     */
    @Bean
    @ConditionalOnMissingBean(HealthService.class)
    public HealthService healthService(
            HealthProperties properties,
            Collector collector,
            ApplicationEventPublisher publisher,
            ApplicationAvailability availability) {
        return new HealthService(properties, collector, publisher, availability);
    }

    /**
     * Creates and registers the lightweight health endpoint handler dynamically.
     *
     * @param service        health query service
     * @param handlerMapping Spring MVC request mapping registry
     * @return dynamically registered health endpoint handler
     */
    @Bean
    @ConditionalOnMissingBean(HealthEndpointHandler.class)
    public HealthEndpointHandler healthEndpointHandler(
            HealthService service,
            RequestMappingHandlerMapping handlerMapping) {
        HealthEndpointHandler handler = new HealthEndpointHandler(service);
        try {
            registerMapping(handlerMapping, "/healthz", handler, "healthz", String.class);
            registerMapping(handlerMapping, "/broken", handler, "broken");
            registerMapping(handlerMapping, "/correct", handler, "correct");
            registerMapping(handlerMapping, "/accept", handler, "accept");
            registerMapping(handlerMapping, "/refuse", handler, "refuse");
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Failed to register health mapping", e);
        }
        return handler;
    }

    /**
     * Registers a GET endpoint against a dynamically created handler.
     *
     * @param handlerMapping Spring MVC request mapping registry
     * @param path           endpoint path
     * @param handler        handler instance
     * @param methodName     handler method name
     * @param parameterTypes handler method parameter types
     * @throws NoSuchMethodException when the handler method cannot be resolved
     */
    private void registerMapping(
            RequestMappingHandlerMapping handlerMapping,
            String path,
            Object handler,
            String methodName,
            Class<?>... parameterTypes) throws NoSuchMethodException {
        RequestMappingInfo mapping = RequestMappingInfo.paths(path).methods(RequestMethod.GET, RequestMethod.POST)
                .build();
        handlerMapping.registerMapping(mapping, handler, handler.getClass().getMethod(methodName, parameterTypes));
    }

}
