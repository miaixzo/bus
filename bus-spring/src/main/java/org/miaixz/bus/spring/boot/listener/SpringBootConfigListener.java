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
package org.miaixz.bus.spring.boot.listener;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.spring.boot.environment.EnvironmentKeys;

/**
 * An {@link ApplicationListener} for the {@link ApplicationEnvironmentPreparedEvent}. It registers a custom property
 * source during the environment preparation phase.
 *
 * @author Kimi Liu
 */
public class SpringBootConfigListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    /**
     * Name of the Bus configuration property source.
     */
    private static final String PROPERTY_SOURCE = "busConfig";

    /**
     * Initializes the listener that registers Bus configuration and prints the banner once per application startup.
     */
    public SpringBootConfigListener() {
        // No initialization required.
    }

    /**
     * A flag to ensure that configuration registration and banner printing are performed only once.
     */
    private final AtomicBoolean registered = new AtomicBoolean();

    /**
     * Handles the {@link ApplicationEnvironmentPreparedEvent} and triggers configuration registration once.
     *
     * @param event The environment prepared event, containing the application and environment.
     */
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        if (event.getEnvironment().getProperty(EnvironmentKeys.CONFIG_ENABLED, Boolean.class, false)
                && registered.compareAndSet(false, true)) {
            registerConfigs(event.getEnvironment());
        }
    }

    /**
     * Defines the listener's priority, ensuring it executes early in the environment preparation phase.
     *
     * @return The order value, set to {@code HIGHEST_PRECEDENCE + 13}.
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 13;
    }

    /**
     * Registers the {@code bus.version} property source.
     *
     * @param environment The Spring environment configuration.
     */
    public void registerConfigs(ConfigurableEnvironment environment) {
        // Register the bus.version property.
        Properties props = new Properties();
        props.setProperty(Keys.VERSION, Version._VERSION);
        MutablePropertySources sources = environment.getPropertySources();
        if (!sources.contains(PROPERTY_SOURCE)) {
            sources.addLast(new PropertiesPropertySource(PROPERTY_SOURCE, props));
        }
    }

}
