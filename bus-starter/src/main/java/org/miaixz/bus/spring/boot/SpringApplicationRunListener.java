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
package org.miaixz.bus.spring.boot;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.spring.metrics.BaseMetrics;
import org.miaixz.bus.spring.metrics.ChildrenMetrics;
import org.miaixz.bus.spring.metrics.ModuleMetrics;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.boot.logging.LoggerConfiguration;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements {@link org.springframework.boot.SpringApplicationRunListener} and {@link ApplicationListener} to calculate
 * and report startup stage times.
 * <p>
 * This class monitors and records the time taken for various stages during the Spring application startup process,
 * including JVM startup, environment preparation, and context initialization. It provides performance metrics for
 * analyzing application startup performance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpringApplicationRunListener implements org.springframework.boot.SpringApplicationRunListener,
        ApplicationListener<ApplicationStartedEvent>, Ordered {

    /**
     * The Spring Boot application instance.
     */
    private final org.springframework.boot.SpringApplication application;

    /**
     * The component responsible for collecting and reporting startup costs.
     */
    private final StartupReporter startupReporter;

    /**
     * Startup metrics for the JVM starting stage. Records the time from JVM launch until the {@code starting} method of
     * this listener completes.
     */
    private BaseMetrics jvmStartingStage;

    /**
     * Startup metrics for the environment preparation stage. Records the time from the completion of the
     * {@code started} method until the {@code environmentPrepared} method completes.
     */
    private BaseMetrics environmentPrepareStage;

    /**
     * Startup metrics for the application context preparation stage. Records the time from the completion of the
     * {@code environmentPrepared} method until the {@code contextPrepared} method completes.
     */
    private ChildrenMetrics<BaseMetrics> applicationContextPrepareStage;

    /**
     * Startup metrics for the application context loading stage. Records the time from the completion of the
     * {@code contextPrepared} method until the {@code contextLoaded} method completes.
     */
    private BaseMetrics applicationContextLoadStage;

    /**
     * Constructs a new {@code SpringApplicationRunListener}.
     *
     * @param springApplication The Spring application instance.
     */
    public SpringApplicationRunListener(org.springframework.boot.SpringApplication springApplication) {
        this.application = springApplication;
        this.startupReporter = new StartupReporter();
        Logger.debug(
                "Initialized SpringApplicationRunListener for application: {}",
                springApplication.getMainApplicationClass());
    }

    /**
     * Callback method invoked when the application is starting.
     * <p>
     * Records the start and end times of the JVM starting stage and calculates the elapsed time.
     * </p>
     *
     * @param bootstrapContext The configurable bootstrap context.
     */
    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        jvmStartingStage = new BaseMetrics();
        jvmStartingStage.setName(GeniusBuilder.JVM_STARTING_STAGE);
        jvmStartingStage.setStartTime(ManagementFactory.getRuntimeMXBean().getStartTime());
        jvmStartingStage.setEndTime(System.currentTimeMillis());
        Logger.debug("JVM starting stage completed in {} ms", jvmStartingStage.getCost());
    }

    /**
     * Callback method invoked when the environment is prepared.
     * <p>
     * Records the start and end times of the environment preparation stage, calculates the elapsed time, and sets the
     * application name. It also binds the {@link StartupReporter} to the environment and registers it in the bootstrap
     * context.
     * </p>
     *
     * @param bootstrapContext The configurable bootstrap context.
     * @param environment      The configurable environment.
     */
    @Override
    public void environmentPrepared(
            ConfigurableBootstrapContext bootstrapContext,
            ConfigurableEnvironment environment) {
        environmentPrepareStage = new BaseMetrics();
        environmentPrepareStage.setName(GeniusBuilder.ENVIRONMENT_PREPARE_STAGE);
        environmentPrepareStage.setStartTime(jvmStartingStage.getEndTime());
        environmentPrepareStage.setEndTime(System.currentTimeMillis());

        // Set the application name and bind to the startup reporter
        startupReporter.setAppName(environment.getProperty(GeniusBuilder.APP_NAME));
        startupReporter.bindToStartupReporter(environment);

        // Register the startup reporter to the bootstrap context
        bootstrapContext.register(StartupReporter.class, key -> startupReporter);

        // Attempt to set BufferingApplicationStartup (if available)
        try {
            Class.forName("org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup");
            application.setApplicationStartup(
                    new org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup(
                            startupReporter.bufferSize));
        } catch (ClassNotFoundException e) {
            Logger.debug("BufferingApplicationStartup not available, skipping startup metrics");
        }
    }

    /**
     * Callback method invoked when the application context is prepared.
     * <p>
     * Records the start and end times of the application context preparation stage, calculates the elapsed time, and
     * adds initializer statistics collected from {@link org.miaixz.bus.spring.boot.SpringApplication}.
     * </p>
     *
     * @param context The configurable application context.
     */
    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        applicationContextPrepareStage = new ChildrenMetrics<>();
        applicationContextPrepareStage.setName(GeniusBuilder.APPLICATION_CONTEXT_PREPARE_STAGE);
        applicationContextPrepareStage.setStartTime(environmentPrepareStage.getEndTime());
        applicationContextPrepareStage.setEndTime(System.currentTimeMillis());

        // If it's a custom SpringApplication, get initializer statistics
        if (application instanceof org.miaixz.bus.spring.boot.SpringApplication springApplication) {
            List<BaseMetrics> statisticsList = springApplication.getInitializerStartupStatList();
            applicationContextPrepareStage.setChildren(new ArrayList<>(statisticsList));
            statisticsList.clear();
        }

        Logger.debug(
                "Application context preparation stage completed in {} ms",
                applicationContextPrepareStage.getCost());
    }

    /**
     * Callback method invoked when the application context is loaded.
     * <p>
     * Records the start and end times of the application context loading stage, calculates the elapsed time, and
     * registers the {@link StartupReporterProcessor} and {@link SpringSmartLifecycle} beans.
     * </p>
     *
     * @param context The configurable application context.
     */
    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        applicationContextLoadStage = new BaseMetrics();
        applicationContextLoadStage.setName(GeniusBuilder.APPLICATION_CONTEXT_LOAD_STAGE);
        applicationContextLoadStage.setStartTime(applicationContextPrepareStage.getEndTime());
        applicationContextLoadStage.setEndTime(System.currentTimeMillis());

        // Register StartupReporterProcessor and StartupReporter
        context.getBeanFactory().addBeanPostProcessor(new StartupReporterProcessor(startupReporter));
        context.getBeanFactory().registerSingleton("STARTUP_REPORTER_BEAN", startupReporter);

        // Register SpringSmartLifecycle processor
        SpringSmartLifecycle springSmartLifecycle = new SpringSmartLifecycle(startupReporter);
        springSmartLifecycle.setApplicationContext(context);
        context.getBeanFactory().registerSingleton("STARTUP_SMART_LIFECYCLE", springSmartLifecycle);

        Logger.debug("Application context loading stage completed in {} ms", applicationContextLoadStage.getCost());
    }

    /**
     * Callback method invoked when the application has started.
     * <p>
     * Records the start and end times of the application refresh stage, calculates the elapsed time, adds all stage
     * statistics to the {@link StartupReporter}, and marks the application boot as finished.
     * </p>
     *
     * @param context   The configurable application context.
     * @param timeTaken The total time taken for the application to start.
     */
    @Override
    public void started(ConfigurableApplicationContext context, Duration timeTaken) {
        // Get application refresh stage statistics
        ChildrenMetrics<ModuleMetrics> applicationRefreshStage = (ChildrenMetrics<ModuleMetrics>) startupReporter
                .getStageNyName(GeniusBuilder.APPLICATION_CONTEXT_REFRESH_STAGE);

        // Set time information for the refresh stage
        applicationRefreshStage.setStartTime(applicationContextLoadStage.getEndTime());
        applicationRefreshStage.setEndTime(System.currentTimeMillis());
        applicationRefreshStage.setCost(applicationRefreshStage.getEndTime() - applicationRefreshStage.getStartTime());

        // Set time information for the root module
        ModuleMetrics rootModule = applicationRefreshStage.getChildren().get(0);
        rootModule.setStartTime(applicationRefreshStage.getStartTime());
        rootModule.setCost(rootModule.getEndTime() - rootModule.getStartTime());

        // Add all stage statistics to the startup reporter
        startupReporter.addCommonStartupStat(jvmStartingStage);
        startupReporter.addCommonStartupStat(environmentPrepareStage);
        startupReporter.addCommonStartupStat(applicationContextPrepareStage);
        startupReporter.addCommonStartupStat(applicationContextLoadStage);

        // Mark application boot as finished
        startupReporter.applicationBootFinish();

        // Log the application started message
        Logger.info(getStartedMessage(context, timeTaken));
    }

    /**
     * Handles {@link ApplicationStartedEvent} events.
     *
     * @param event The application started event.
     */
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        Logger.debug("Received ApplicationStartedEvent for application: {}", application.getMainApplicationClass());
    }

    /**
     * Returns the order value for this listener.
     *
     * @return The order value, where a lower value indicates higher priority.
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }

    /**
     * Generates a message indicating that the application has started.
     * <p>
     * Constructs a message containing the application name, config name, active profiles, effective logging level, and
     * total startup time.
     * </p>
     *
     * @param context            The configurable application context.
     * @param timeTakenToStartup The total time taken for the application to start.
     * @return The formatted application started message string.
     */
    private String getStartedMessage(ConfigurableApplicationContext context, Duration timeTakenToStartup) {
        StringBuilder message = new StringBuilder();
        message.append("Started");

        ConfigurableEnvironment environment = context.getEnvironment();
        String appName = StringKit.defaultIfEmpty(environment.getProperty("spring.application.name"), "unknown");
        message.append(" - App Name: ").append(appName);

        String configName = StringKit.defaultIfEmpty(environment.getProperty("spring.config.name"), "application");
        message.append(" - Config Name: ").append(configName);

        String[] activeProfiles = environment.getActiveProfiles();
        message.append(" - Active Profiles: ")
                .append(activeProfiles.length > 0 ? String.join(", ", activeProfiles) : "none");

        // Get logging level
        String logging = environment.getProperty(GeniusBuilder.LOGGING_LEVEL);
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
            message.append(" with [").append(logging).append("]");
        }

        message.append(" in ");
        message.append(timeTakenToStartup.toMillis() / 1000.0);
        message.append(" seconds");

        return message.toString();
    }

}
