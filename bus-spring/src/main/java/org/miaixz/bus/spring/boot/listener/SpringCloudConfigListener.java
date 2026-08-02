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

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.StreamSupport;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.support.EnvironmentPostProcessorApplicationListener;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.*;

import org.miaixz.bus.core.xyz.ClassKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.spring.boot.environment.CloudEnvironment;
import org.miaixz.bus.spring.boot.environment.EnvironmentKeys;

/**
 * An {@link ApplicationListener} for {@link ApplicationEnvironmentPreparedEvent} that adapts to Spring Cloud
 * environments.
 * <p>
 * This listener is responsible for registering logging properties into the Spring Cloud bootstrap environment. It
 * ensures that logging configurations are correctly propagated when running within a Spring Cloud setup.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SpringCloudConfigListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    /**
     * Initializes the listener that exposes remote Spring Cloud configuration as a deterministic property source.
     */
    public SpringCloudConfigListener() {
        // No initialization required.
    }

    /**
     * Guards one-time registration of the remote configuration property source.
     */
    private final AtomicBoolean processed = new AtomicBoolean();

    /**
     * Returns the order value for this listener.
     * <p>
     * This listener is set to have the highest precedence to ensure it runs very early in the startup process.
     * </p>
     *
     * @return The order value, {@code Ordered.HIGHEST_PRECEDENCE}.
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * Handles the {@link ApplicationEnvironmentPreparedEvent} by exposing eligible remote configuration properties.
     * <p>
     * This method checks if the application is running in a Spring Cloud environment. If it is, and if a bootstrap
     * context is present, it adds a high-priority property source. Otherwise, it builds a bootstrap application context
     * to process environment post-processors and then configures logging and required properties.
     * </p>
     *
     * @param event The environment prepared event.
     */
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();
        if (!environment.getProperty(EnvironmentKeys.CLOUD_CONFIG_ENABLED, Boolean.class, false)
                || !processed.compareAndSet(false, true)) {
            return;
        }
        MapPropertySource highPriorityConfig = new MapPropertySource(EnvironmentKeys.BUS_HIGH_PRIORITY,
                new HashMap<>());
        // only work in spring cloud application
        if (new CloudEnvironment(environment, getClass().getClassLoader()).isEnabled()) {
            if (environment.getPropertySources().contains(EnvironmentKeys.CLOUD_BOOTSTRAP)) {
                // in bootstrap application context, add high priority config
                environment.getPropertySources().addLast(highPriorityConfig);
            } else {
                // in application context, build high priority config
                SpringApplication application = event.getSpringApplication();
                StandardEnvironment bootstrapEnvironment = new StandardEnvironment();
                StreamSupport.stream(environment.getPropertySources().spliterator(), false)
                        .filter(source -> !(source instanceof PropertySource.StubPropertySource))
                        .forEach(source -> bootstrapEnvironment.getPropertySources().addLast(source));

                List<Class<?>> sources = new ArrayList<>();
                for (Object s : application.getAllSources()) {
                    if (s instanceof Class) {
                        sources.add((Class<?>) s);
                    } else if (s instanceof String) {
                        sources.add(ClassKit.forName((String) s, null));
                    }
                }

                SpringApplication bootstrapApplication = new SpringApplicationBuilder()
                        .profiles(environment.getActiveProfiles()).bannerMode(Banner.Mode.OFF)
                        .environment(bootstrapEnvironment).sources(sources.toArray(new Class[] {}))
                        .registerShutdownHook(false).logStartupInfo(false).web(WebApplicationType.NONE).listeners()
                        .initializers().build(event.getArgs());

                ConfigurableBootstrapContext bootstrapContext = event.getBootstrapContext();
                ApplicationEnvironmentPreparedEvent bootstrapEvent = new ApplicationEnvironmentPreparedEvent(
                        bootstrapContext, bootstrapApplication, event.getArgs(), bootstrapEnvironment);

                application.getListeners().stream()
                        .filter(listener -> listener instanceof EnvironmentPostProcessorApplicationListener).forEach(
                                listener -> ((EnvironmentPostProcessorApplicationListener) listener)
                                        .onApplicationEvent(bootstrapEvent));

                logSetting(bootstrapEnvironment, highPriorityConfig);
                requireProperties(bootstrapEnvironment, highPriorityConfig);
                environment.getPropertySources().addLast(highPriorityConfig);
            }
        }
    }

    /**
     * Configures logging-related properties.
     * <p>
     * It iterates through the environment's property sources, filters for logging configurations, and adds them to the
     * high-priority configuration map.
     * </p>
     *
     * @param environment        The configurable environment.
     * @param highPriorityConfig high priority config
     */
    private void logSetting(ConfigurableEnvironment environment, MapPropertySource highPriorityConfig) {
        StreamSupport.stream(environment.getPropertySources().spliterator(), false)
                .filter(propertySource -> propertySource instanceof EnumerablePropertySource)
                .map(propertySource -> Arrays.asList(((EnumerablePropertySource<?>) propertySource).getPropertyNames()))
                .flatMap(Collection::stream).filter(this::isLoggingConfig)
                .forEach((key) -> highPriorityConfig.getSource().put(key, environment.getProperty(key)));
    }

    /**
     * Tests whether the supplied property name belongs to Spring's logging configuration namespace.
     *
     * @param key lookup key
     * @return whether logging config
     */
    private boolean isLoggingConfig(String key) {
        return key != null
                && (key.equals(EnvironmentKeys.LOGGING_PATH) || key.startsWith(EnvironmentKeys.LOGGING_LEVEL_PREFIX)
                        || key.startsWith(EnvironmentKeys.LOGGING_PATH_PREFIX));
    }

    /**
     * Configures required properties.
     * <p>
     * If the application name is present in the environment, it is added to the high-priority configuration map.
     * </p>
     *
     * @param environment        The configurable environment.
     * @param highPriorityConfig high priority config
     */
    private void requireProperties(ConfigurableEnvironment environment, MapPropertySource highPriorityConfig) {
        if (StringKit.hasText(environment.getProperty(EnvironmentKeys.APPLICATION_NAME))) {
            highPriorityConfig.getSource()
                    .put(EnvironmentKeys.APPLICATION_NAME, environment.getProperty(EnvironmentKeys.APPLICATION_NAME));
        }
    }

}
