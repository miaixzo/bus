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

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.bootstrap.ConfigurableBootstrapContext;
import org.springframework.boot.logging.LoggerConfiguration;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.boot.environment.EnvironmentKeys;
import org.miaixz.bus.spring.boot.startup.SpringStartupCollector;
import org.miaixz.bus.spring.boot.startup.SpringStartupPublisher;
import org.miaixz.bus.spring.boot.startup.SpringStartupSummary;
import org.miaixz.bus.spring.boot.startup.SpringStartupSummary.Stage;

/**
 * Collects startup statistics after the environment explicitly enables the feature.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SpringApplicationRunListener implements org.springframework.boot.SpringApplicationRunListener, Ordered {

    /**
     * Bean name for the startup collector.
     */
    private static final String COLLECTOR_BEAN_NAME = "busSpringStartupCollector";
    /**
     * Bean name for the startup lifecycle component.
     */
    private static final String LIFECYCLE_BEAN_NAME = "busStartupSmartLifecycle";

    /**
     * Guards the one-time evaluation of the startup reporting switch.
     */
    private final AtomicBoolean activationChecked = new AtomicBoolean();
    /**
     * Guards the one-time registration of startup reporting components.
     */
    private final AtomicBoolean componentsRegistered = new AtomicBoolean();
    /**
     * Guards completion of the startup report.
     */
    private final AtomicBoolean reportCompleted = new AtomicBoolean();

    /**
     * Collector allocated after startup metrics are enabled.
     */
    private SpringStartupCollector startupCollector;
    /**
     * Timestamp at which the prepared environment enabled startup metrics.
     */
    private long environmentPreparedTime;
    /**
     * Timestamp at which application-context preparation completed.
     */
    private long contextPreparedTime;
    /**
     * Timestamp at which application-context loading completed.
     */
    private long contextLoadedTime;

    /**
     * Creates a listener without allocating startup statistics.
     *
     * @param application Spring Boot application
     */
    public SpringApplicationRunListener(org.springframework.boot.SpringApplication application) {
        // Spring Boot requires this constructor signature for run-listener discovery.
        // No initialization required.
    }

    /**
     * Performs no work because the environment and the feature switch are not available yet.
     *
     * @param bootstrapContext bootstrap context
     */
    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        // Startup collection is intentionally deferred until environmentPrepared.
    }

    /**
     * Activates startup reporting after configuration data has supplied the feature switch.
     *
     * @param bootstrapContext bootstrap context used to expose the reporter
     * @param environment      prepared application environment
     */
    @Override
    public void environmentPrepared(
            ConfigurableBootstrapContext bootstrapContext,
            ConfigurableEnvironment environment) {
        if (!activationChecked.compareAndSet(false, true)
                || !environment.getProperty(EnvironmentKeys.METRICS_ENABLED, Boolean.class, false)
                || !environment.getProperty(EnvironmentKeys.STARTUP_METRICS_ENABLED, Boolean.class, false)) {
            return;
        }

        long applicationBootTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        long enabledAt = System.currentTimeMillis();
        SpringStartupCollector collector = new SpringStartupCollector(
                environment.getProperty(EnvironmentKeys.APPLICATION_NAME),
                applicationBootTime);
        startupCollector = collector;
        environmentPreparedTime = enabledAt;
        collector.addStage(Stage.between(Stage.JVM_STARTING, applicationBootTime, enabledAt));
        collector.addStage(Stage.between(Stage.ENVIRONMENT_PREPARE, enabledAt, enabledAt));
        bootstrapContext.registerIfAbsent(SpringStartupCollector.class, key -> collector);
    }

    /**
     * Opens the application-context preparation stage for an enabled report.
     *
     * @param context prepared application context
     */
    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        if (!isEnabled()) {
            return;
        }
        contextPreparedTime = System.currentTimeMillis();
        startupCollector.addStage(Stage.between(
                Stage.APPLICATION_CONTEXT_PREPARE,
                environmentPreparedTime,
                contextPreparedTime));
    }

    /**
     * Closes context preparation, records context loading, and registers reporting components.
     *
     * @param context loaded application context
     */
    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        if (!isEnabled() || contextPreparedTime == 0) {
            return;
        }
        contextLoadedTime = System.currentTimeMillis();
        startupCollector.addStage(Stage.between(
                Stage.APPLICATION_CONTEXT_LOAD,
                contextPreparedTime,
                contextLoadedTime));
        registerComponents(context);
    }

    /**
     * Completes and logs the startup report after the application context has started.
     *
     * @param context   started application context
     * @param timeTaken total Spring Boot startup duration
     */
    @Override
    public void started(ConfigurableApplicationContext context, Duration timeTaken) {
        if (!isEnabled() || contextLoadedTime == 0 || !reportCompleted.compareAndSet(false, true)) {
            return;
        }

        long applicationBootEndTime = System.currentTimeMillis();
        if (!startupCollector.containsStage(Stage.APPLICATION_CONTEXT_REFRESH)) {
            startupCollector.addStage(Stage.between(
                    Stage.APPLICATION_CONTEXT_REFRESH,
                    contextLoadedTime,
                    applicationBootEndTime));
        }

        publishStartupMetrics(context, startupCollector.complete(applicationBootEndTime));
        Logger.info(false, "Starter", "Spring " + getStartedMessage(context, timeTaken));
    }

    /**
     * Runs startup reporting near the end of Spring Boot listener processing.
     *
     * @return listener order
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }

    /**
     * Indicates whether startup metric collection was enabled by the prepared environment.
     *
     * @return whether enabled
     */
    private boolean isEnabled() {
        return startupCollector != null;
    }

    /**
     * Publishes the completed startup summary to optional application-context publishers.
     *
     * @param context started application context
     * @param summary completed startup summary
     */
    private void publishStartupMetrics(ConfigurableApplicationContext context, SpringStartupSummary summary) {
        List<SpringStartupPublisher> publishers = List
                .copyOf(context.getBeansOfType(SpringStartupPublisher.class).values());
        if (publishers.isEmpty()) {
            return;
        }
        for (SpringStartupPublisher publisher : publishers) {
            try {
                publisher.publish(summary);
            } catch (RuntimeException exception) {
                Logger.warn(
                        false,
                        "Starter",
                        "Startup metrics publication failed: publisher={}, exception={}",
                        publisher.getClass().getName(),
                        exception.getClass().getSimpleName());
            }
        }
    }

    /**
     * Registers the components.
     *
     * @param context owning application context
     */
    private void registerComponents(ConfigurableApplicationContext context) {
        if (!componentsRegistered.compareAndSet(false, true)) {
            return;
        }
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        if (!contains(beanFactory, COLLECTOR_BEAN_NAME, SpringStartupCollector.class)) {
            beanFactory.registerSingleton(COLLECTOR_BEAN_NAME, startupCollector);
        }
        if (!contains(beanFactory, LIFECYCLE_BEAN_NAME, SpringSmartLifecycle.class)) {
            SpringSmartLifecycle lifecycle = new SpringSmartLifecycle(startupCollector, contextLoadedTime);
            beanFactory.registerSingleton(LIFECYCLE_BEAN_NAME, lifecycle);
        }
    }

    /**
     * Returns whether a compatible component is already registered.
     *
     * @param beanFactory target Bean factory
     * @param name        expected Bean name
     * @param type        required component type
     * @return {@code true} when the name or type is already registered
     */
    private boolean contains(ConfigurableListableBeanFactory beanFactory, String name, Class<?> type) {
        return beanFactory.containsBean(name) || beanFactory.getBeanNamesForType(type, false, false).length > 0;
    }

    /**
     * Returns the started message.
     *
     * @param context   owning application context
     * @param timeTaken time taken
     * @return the started message
     */
    private String getStartedMessage(ConfigurableApplicationContext context, Duration timeTaken) {
        StringBuilder message = new StringBuilder("Started");
        ConfigurableEnvironment environment = context.getEnvironment();
        message.append(" - App Name: ")
                .append(StringKit.defaultIfEmpty(environment.getProperty(EnvironmentKeys.APPLICATION_NAME), "unknown"));
        message.append(" - Config Name: ")
                .append(StringKit.defaultIfEmpty(environment.getProperty("spring.config.name"), "application"));
        String[] activeProfiles = environment.getActiveProfiles();
        message.append(" - Active Profiles: ")
                .append(activeProfiles.length > 0 ? String.join(", ", activeProfiles) : "none");

        String logging = environment.getProperty(EnvironmentKeys.LOGGING_LEVEL);
        if (!StringKit.hasText(logging)) {
            LoggingSystem loggingSystem = context.getBean(LoggingSystem.class);
            for (LoggerConfiguration config : loggingSystem.getLoggerConfigurations()) {
                if ("org.miaixz".equalsIgnoreCase(config.getName())) {
                    logging = config.getEffectiveLevel().name();
                    break;
                }
            }
        }
        if (StringKit.hasText(logging)) {
            message.append(" with [").append(logging).append(']');
        }
        return message.append(" in ").append(timeTaken.toMillis() / 1000.0).append(" seconds").toString();
    }

}
