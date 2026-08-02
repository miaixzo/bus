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
import org.miaixz.bus.spring.boot.startup.BaseMetrics;
import org.miaixz.bus.spring.boot.startup.ChildrenMetrics;
import org.miaixz.bus.spring.boot.startup.ModuleMetrics;
import org.miaixz.bus.spring.boot.startup.StartupReporter;
import org.miaixz.bus.spring.boot.startup.StartupReporterProcessor;
import org.miaixz.bus.spring.boot.startup.StartupStages;

/**
 * Collects startup statistics after the environment explicitly enables the feature.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SpringApplicationRunListener implements org.springframework.boot.SpringApplicationRunListener, Ordered {

    /**
     * Bean name for the startup reporter.
     */
    private static final String REPORTER_BEAN_NAME = "busStartupReporter";
    /**
     * Bean name for the startup reporter processor.
     */
    private static final String PROCESSOR_BEAN_NAME = "busStartupReporterProcessor";
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
     * Reporter allocated after startup metrics are enabled.
     */
    private StartupReporter startupReporter;
    /**
     * Metrics for JVM startup before Spring Boot begins.
     */
    private BaseMetrics jvmStartingStage;
    /**
     * Metrics for Spring environment preparation.
     */
    private BaseMetrics environmentPrepareStage;
    /**
     * Metrics for application-context preparation and its child phases.
     */
    private ChildrenMetrics<BaseMetrics> applicationContextPrepareStage;
    /**
     * Metrics for application-context loading.
     */
    private BaseMetrics applicationContextLoadStage;

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

    @Override
    public void environmentPrepared(
            ConfigurableBootstrapContext bootstrapContext,
            ConfigurableEnvironment environment) {
        if (!activationChecked.compareAndSet(false, true)
                || !environment.getProperty(EnvironmentKeys.STARTUP_ENABLED, Boolean.class, false)) {
            return;
        }

        long enabledAt = System.currentTimeMillis();
        StartupReporter reporter = new StartupReporter(ManagementFactory.getRuntimeMXBean().getStartTime());
        reporter.setAppName(environment.getProperty(EnvironmentKeys.APPLICATION_NAME));
        reporter.bindToStartupReporter(environment);
        startupReporter = reporter;

        jvmStartingStage = stage(
                StartupStages.JVM_STARTING_STAGE,
                ManagementFactory.getRuntimeMXBean().getStartTime(),
                enabledAt);
        environmentPrepareStage = stage(StartupStages.ENVIRONMENT_PREPARE_STAGE, enabledAt, enabledAt);
        bootstrapContext.registerIfAbsent(StartupReporter.class, key -> reporter);
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        if (!isEnabled()) {
            return;
        }
        applicationContextPrepareStage = new ChildrenMetrics<>(StartupStages.APPLICATION_CONTEXT_PREPARE_STAGE,
                environmentPrepareStage.getEndTime(), System.currentTimeMillis(), List.of());
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        if (!isEnabled() || applicationContextPrepareStage == null) {
            return;
        }
        applicationContextLoadStage = stage(
                StartupStages.APPLICATION_CONTEXT_LOAD_STAGE,
                applicationContextPrepareStage.getEndTime(),
                System.currentTimeMillis());
        registerComponents(context);
    }

    @Override
    public void started(ConfigurableApplicationContext context, Duration timeTaken) {
        if (!isEnabled() || applicationContextLoadStage == null || !reportCompleted.compareAndSet(false, true)) {
            return;
        }

        BaseMetrics refreshStage = startupReporter.getStageByName(StartupStages.APPLICATION_CONTEXT_REFRESH_STAGE);
        ChildrenMetrics<ModuleMetrics> applicationRefreshStage;
        if (refreshStage instanceof ChildrenMetrics<?> childrenMetrics) {
            ChildrenMetrics<ModuleMetrics> typedStage = (ChildrenMetrics<ModuleMetrics>) childrenMetrics;
            applicationRefreshStage = typedStage;
        } else {
            long refreshEndTime = System.currentTimeMillis();
            ModuleMetrics rootModule = new ModuleMetrics(SpringSmartLifecycle.ROOT_MODULE_NAME,
                    applicationContextLoadStage.getEndTime(), refreshEndTime, Thread.currentThread().getName(),
                    List.of());
            applicationRefreshStage = new ChildrenMetrics<>(StartupStages.APPLICATION_CONTEXT_REFRESH_STAGE,
                    applicationContextLoadStage.getEndTime(), refreshEndTime, List.of(rootModule));
            startupReporter.addCommonStartupStat(applicationRefreshStage);
        }

        startupReporter.addCommonStartupStat(jvmStartingStage);
        startupReporter.addCommonStartupStat(environmentPrepareStage);
        startupReporter.addCommonStartupStat(applicationContextPrepareStage);
        startupReporter.addCommonStartupStat(applicationContextLoadStage);
        startupReporter.applicationBootFinish();
        Logger.info(false, "Starter", "Spring " + getStartedMessage(context, timeTaken));
    }

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
        return startupReporter != null;
    }

    /**
     * Creates timing metrics for one startup stage.
     *
     * @param name      stage name
     * @param startTime stage start time in milliseconds
     * @param endTime   stage end time in milliseconds
     * @return startup stage metrics
     */
    private BaseMetrics stage(String name, long startTime, long endTime) {
        return new BaseMetrics(name, startTime, endTime);
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
        if (!contains(beanFactory, REPORTER_BEAN_NAME, StartupReporter.class)) {
            beanFactory.registerSingleton(REPORTER_BEAN_NAME, startupReporter);
        }
        if (!contains(beanFactory, PROCESSOR_BEAN_NAME, StartupReporterProcessor.class)) {
            StartupReporterProcessor processor = new StartupReporterProcessor(startupReporter);
            beanFactory.addBeanPostProcessor(processor);
            beanFactory.registerSingleton(PROCESSOR_BEAN_NAME, processor);
        }
        if (!contains(beanFactory, LIFECYCLE_BEAN_NAME, SpringSmartLifecycle.class)) {
            SpringSmartLifecycle lifecycle = new SpringSmartLifecycle(startupReporter,
                    applicationContextLoadStage.getEndTime());
            lifecycle.setApplicationContext(context);
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
