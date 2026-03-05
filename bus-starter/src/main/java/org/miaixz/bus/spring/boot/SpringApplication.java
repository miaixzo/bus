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
package org.miaixz.bus.spring.boot;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.metrics.BaseMetrics;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.io.ResourceLoader;

/**
 * An extension of {@link org.springframework.boot.SpringApplication} that calculates the initialization time of each
 * {@link ApplicationContextInitializer}.
 * <p>
 * This class inherits from Spring Boot's {@code SpringApplication} and adds functionality to track the startup time of
 * {@code ApplicationContextInitializer} instances. By recording the start and end times for each initializer, it
 * calculates the time taken and stores the statistics for later analysis.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpringApplication extends org.springframework.boot.SpringApplication {

    /**
     * A list that stores the startup statistics for all {@link ApplicationContextInitializer} instances.
     */
    private final List<BaseMetrics> initializerStartupStatList = new ArrayList<>();

    /**
     * Creates a new {@code SpringApplication} instance from the specified primary source classes.
     *
     * @param primarySources the primary source classes.
     */
    public SpringApplication(Class<?>... primarySources) {
        super(primarySources);
    }

    /**
     * Creates a new {@code SpringApplication} instance with a specific {@link ResourceLoader} and primary source
     * classes.
     *
     * @param resourceLoader the resource loader to use.
     * @param primarySources the primary source classes.
     */
    public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
        super(resourceLoader, primarySources);
    }

    /**
     * A static helper that can be used to run a {@code SpringApplication} from a single primary source class.
     *
     * @param primarySource the primary source class.
     * @param args          the command line arguments.
     * @return the running {@link ConfigurableApplicationContext}.
     */
    public static ConfigurableApplicationContext run(Class<?> primarySource, String... args) {
        return run(new Class<?>[] { primarySource }, args);
    }

    /**
     * A static helper that can be used to run a {@code SpringApplication} from the specified primary source classes.
     *
     * @param primarySources the primary source classes.
     * @param args           the command line arguments.
     * @return the running {@link ConfigurableApplicationContext}.
     */
    public static ConfigurableApplicationContext run(Class<?>[] primarySources, String[] args) {
        return new SpringApplication(primarySources).run(args);
    }

    /**
     * Runs the Spring application, creating and refreshing a new {@link ConfigurableApplicationContext}.
     *
     * @param args the command line arguments.
     * @return the running {@link ConfigurableApplicationContext}.
     */
    @Override
    public ConfigurableApplicationContext run(String... args) {
        return super.run(args);
    }

    /**
     * Applies all registered {@link ApplicationContextInitializer} instances to the given context.
     * <p>
     * This method overrides the parent implementation to add timing statistics for each initializer. It records the
     * start time before an initializer is executed and the end time after it completes, calculating the duration and
     * storing the information.
     * </p>
     *
     * @param context the application context to initialize.
     */
    @Override
    protected void applyInitializers(ConfigurableApplicationContext context) {
        for (ApplicationContextInitializer initializer : getInitializers()) {
            try {
                // Resolve the context type required by the initializer.
                Class<?> requiredType = GenericTypeResolver
                        .resolveTypeArgument(initializer.getClass(), ApplicationContextInitializer.class);
                // Verify that the context is of the required type.
                Assert.isInstanceOf(
                        requiredType,
                        context,
                        "Unable to call initializer: " + initializer.getClass().getName());

                // Create a statistics object and record the start time.
                BaseMetrics stat = new BaseMetrics();
                stat.setName(initializer.getClass().getName());
                stat.setStartTime(System.currentTimeMillis());

                // Execute the initializer.
                initializer.initialize(context);

                // Record the end time and calculate the duration.
                stat.setEndTime(System.currentTimeMillis());
                initializerStartupStatList.add(stat);

                // Log the initialization time.
                Logger.debug("Initialized {} in {} ms", stat.getName(), stat.getCost());
            } catch (Exception e) {
                // Log any exceptions that occur during initialization.
                Logger.warn("Failed to initialize {}: {}", initializer.getClass().getName(), e.getMessage());
            }
        }
    }

    /**
     * Gets the list of startup statistics for all applied {@link ApplicationContextInitializer} instances.
     *
     * @return a list of initialization statistics.
     */
    public List<BaseMetrics> getInitializerStartupStatList() {
        return initializerStartupStatList;
    }

}
