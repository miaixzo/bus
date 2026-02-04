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
package org.miaixz.bus.spring.listener;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.miaixz.bus.core.Version;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.spring.banner.BannerPrinter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

/**
 * An {@link ApplicationListener} for the {@link ApplicationEnvironmentPreparedEvent}. It registers a custom property
 * source and handles the printing of the Spring Boot banner via {@link BannerPrinter}. This ensures that the
 * configuration and banner printing occur early in the environment preparation phase and are executed only once.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpringBootConfigListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    /**
     * A flag to ensure that configuration registration and banner printing are performed only once.
     */
    private final AtomicBoolean registered = new AtomicBoolean();

    /**
     * The {@link SpringApplication} instance, used for banner configuration.
     */
    public SpringApplication application;

    /**
     * Handles the {@link ApplicationEnvironmentPreparedEvent} by initializing the {@link SpringApplication} instance
     * and triggering the configuration registration. This logic is guaranteed to run only once.
     *
     * @param event The environment prepared event, containing the application and environment.
     */
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        this.application = event.getSpringApplication();
        if (registered.compareAndSet(false, true)) {
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
     * Registers custom configurations and triggers the banner printing. This method adds a property source for
     * {@code bus.version} and then calls the {@link BannerPrinter}.
     *
     * @param environment The Spring environment configuration.
     */
    public void registerConfigs(ConfigurableEnvironment environment) {
        // Register the bus.version property.
        Properties props = new Properties();
        props.setProperty(Keys.VERSION, Version._VERSION);
        MutablePropertySources sources = environment.getPropertySources();
        sources.addLast(new PropertiesPropertySource("bus", props));

        // Set and print the banner.
        new BannerPrinter().printBanner(this.application, environment);
        // Other configuration logic can be added here.
    }

}
