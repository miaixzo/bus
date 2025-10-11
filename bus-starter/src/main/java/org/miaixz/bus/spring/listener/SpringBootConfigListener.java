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
