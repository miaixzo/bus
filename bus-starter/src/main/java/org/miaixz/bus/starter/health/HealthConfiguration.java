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

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.miaixz.bus.health.Provider;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.boot.condition.ConditionalOnEnabled;
import org.miaixz.bus.starter.GeniusBuilder;
import org.miaixz.bus.starter.annotation.EnableHealth;

/**
 * Configures application health indicators and availability monitoring.
 * <p>
 * This configuration class sets up all the necessary beans for the health monitoring feature, including the data
 * provider and the Spring Boot health indicator. The entire configuration is conditional on the health feature being
 * explicitly enabled.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@EnableConfigurationProperties(value = { HealthProperties.class })
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = { "org.miaixz.bus.health.Provider",
        "org.springframework.boot.health.contributor.HealthIndicator" })
@ConditionalOnEnabled(annotation = EnableHealth.class, prefix = GeniusBuilder.HEALTH)
public class HealthConfiguration {

    /**
     * Bound health configuration properties.
     */
    private final HealthProperties properties;

    /**
     * Stores the health-reporting policy used by the indicator and availability listener.
     *
     * @param properties bound configuration properties
     */
    public HealthConfiguration(HealthProperties properties) {
        this.properties = properties;
    }

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
     * Creates the {@link Provider} bean, which is responsible for gathering raw system and hardware information.
     *
     * @return A new {@link Provider} instance.
     * @throws IllegalStateException if the provider fails to initialize.
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.health.contributor.HealthIndicator")
    @ConditionalOnMissingBean(Provider.class)
    public Provider provider() {
        try {
            return new Provider();
        } catch (Exception e) {
            Logger.error(false, "Starter", "Health failed to initialize Provider: {}", e.getClass().getSimpleName(), e);
            throw new IllegalStateException("Failed to initialize Provider: " + e.getMessage(), e);
        }
    }

    /**
     * Creates the read-only system health indicator when the Boot health SPI is available.
     *
     * @param provider provider instance
     * @return the health indicator backed by the selected health Provider
     */
    @Bean
    @ConditionalOnClass(name = "org.springframework.boot.health.contributor.HealthIndicator")
    @ConditionalOnMissingBean(SystemHealthIndicator.class)
    public SystemHealthIndicator systemHealthIndicator(Provider provider) {
        return new SystemHealthIndicator(provider, this.properties.getDetails());
    }

}
