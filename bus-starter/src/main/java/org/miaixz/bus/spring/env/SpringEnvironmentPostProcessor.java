/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.spring.env;

import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.spring.GeniusBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;

import java.util.Properties;

/**
 * An {@link org.springframework.boot.env.EnvironmentPostProcessor} implementation that sets up some default properties
 * for the application environment.
 * <p>
 * This post-processor adds a property source named {@code GeniusBuilder.BUS_PROPERTY_SOURCE} to the environment,
 * containing properties like the application version. It also sets {@code GeniusBuilder.APP_NAME} as a required
 * property.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpringEnvironmentPostProcessor implements org.springframework.boot.env.EnvironmentPostProcessor, Ordered {

    /**
     * Post-processes the environment to add default properties.
     * <p>
     * If the property source {@code GeniusBuilder.BUS_PROPERTY_SOURCE} is not already present, it adds the application
     * version and sets {@code GeniusBuilder.APP_NAME} as a required property.
     * </p>
     *
     * @param environment The configurable environment.
     * @param application The Spring application instance.
     */
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (environment.getPropertySources().get(GeniusBuilder.BUS_PROPERTY_SOURCE) != null) {
            return;
        }

        // Version configuration
        Properties properties = new Properties();
        properties.setProperty(Keys.VERSION, Version._VERSION);

        // Default configuration properties
        PropertiesPropertySource propertySource = new PropertiesPropertySource(GeniusBuilder.BUS_PROPERTY_SOURCE,
                properties);
        environment.getPropertySources().addLast(propertySource);

        // Set application name as a required property
        environment.setRequiredProperties(GeniusBuilder.APP_NAME);
    }

    /**
     * Returns the order value for this post-processor.
     * <p>
     * This ensures that this post-processor runs with a very low precedence, allowing other post-processors to run
     * first.
     * </p>
     *
     * @return The order value.
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 100;
    }

}
