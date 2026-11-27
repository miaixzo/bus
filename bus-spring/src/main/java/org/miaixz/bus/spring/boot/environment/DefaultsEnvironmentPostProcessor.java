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
package org.miaixz.bus.spring.boot.environment;

import java.util.Map;
import java.util.Properties;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertiesPropertySource;

import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.cortex.registry.RegistryEndpoint;

/**
 * An {@link EnvironmentPostProcessor} implementation that sets up some default properties for the application
 * environment.
 * <p>
 * This post-processor adds a property source named {@code BootEnvironmentPropertyRegistry.BUS_PROPERTY_SOURCE}
 * containing the application version and a deterministic application-name fallback.
 *
 * @author Kimi Liu
 */
public class DefaultsEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    /**
     * Initializes the post-processor that contributes Bus version and application-name defaults.
     */
    public DefaultsEnvironmentPostProcessor() {
        // No initialization required.
    }

    /**
     * Post-processes the environment to add default properties.
     * <p>
     * When {@code BootEnvironmentPropertyRegistry.BUS_PROPERTY_SOURCE} is absent, this adds the application version and
     * supplies a default value for {@code BootEnvironmentPropertyRegistry.APPLICATION_NAME}.
     * </p>
     *
     * @param environment The configurable environment.
     * @param application The Spring application instance.
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String registerUrl = environment.getProperty(RegistryEndpoint.REGISTER_URL);
        if (registerUrl != null && !registerUrl.isBlank()) {
            environment.getPropertySources().addFirst(
                    new MapPropertySource("busRegistryEndpoint",
                            Map.of(RegistryEndpoint.PATH_PROPERTY, RegistryEndpoint.path(registerUrl))));
        }
        if (environment.getPropertySources().get(BootEnvironmentPropertyRegistry.BUS_PROPERTY_SOURCE) != null) {
            return;
        }

        // Version configuration
        Properties properties = new Properties();
        properties.setProperty(Keys.VERSION, Version._VERSION);
        if (!environment.containsProperty(BootEnvironmentPropertyRegistry.APPLICATION_NAME)) {
            Class<?> mainClass = application.getMainApplicationClass();
            properties.setProperty(
                    BootEnvironmentPropertyRegistry.APPLICATION_NAME,
                    mainClass == null ? "application" : mainClass.getSimpleName());
        }

        // Default configuration properties
        PropertiesPropertySource propertySource = new PropertiesPropertySource(
                BootEnvironmentPropertyRegistry.BUS_PROPERTY_SOURCE, properties);
        environment.getPropertySources().addLast(propertySource);
    }

    /**
     * Returns the order value for this post-processor.
     * <p>
     * This ensures that this post-processor runs with a very low precedence, allowing other post-processors to run
     * first.
     * </p>
     *
     * @return the precedence used to contribute Bus defaults during environment preparation
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }

}
