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
import org.miaixz.bus.spring.banner.TextBanner;
import org.miaixz.bus.spring.boot.statics.BaseStatics;
import org.miaixz.bus.spring.boot.statics.ChildrenStatics;
import org.miaixz.bus.spring.boot.statics.ModuleStatics;
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
 * 实现 {@link SpringApplicationRunListener} 和 {@link ApplicationListener}，计算启动阶段时间。 支持动态加载和性能统计，记录 JVM 启动、环境准备、上下文初始化等阶段。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpringApplicationRunListener implements org.springframework.boot.SpringApplicationRunListener,
        ApplicationListener<ApplicationStartedEvent>, Ordered {

    /**
     * Spring boot 主引导和启动Spring应用程序
     */
    private final org.springframework.boot.SpringApplication application;

    /**
     * 收集和启动报告成本的基本组件
     */
    private final StartupReporter startupReporter;

    /**
     * JVM启动后的运行阶段{@link org.springframework.boot.SpringApplicationRunListener#started(ConfigurableApplicationContext, Duration)}
     * ()}
     */
    private BaseStatics jvmStartingStage;

    /**
     * 从{@link org.springframework.boot.SpringApplicationRunListener#started(ConfigurableApplicationContext, Duration)}()}
     * 到{@link org.springframework.boot.SpringApplicationRunListener#environmentPrepared(ConfigurableBootstrapContext, ConfigurableEnvironment)}
     * (ConfigurableEnvironment)}}的运行阶段
     */
    private BaseStatics environmentPrepareStage;

    /**
     * 从{@link org.springframework.boot.SpringApplicationRunListener#environmentPrepared(ConfigurableBootstrapContext, ConfigurableEnvironment)}
     * (ConfigurableEnvironment)}
     * 到{@link org.springframework.boot.SpringApplicationRunListener#contextPrepared(ConfigurableApplicationContext)}}的运行阶段
     */
    private ChildrenStatics<BaseStatics> applicationContextPrepareStage;

    /**
     * 从{@link org.springframework.boot.SpringApplicationRunListener#contextPrepared(ConfigurableApplicationContext)}
     * 到{@link org.springframework.boot.SpringApplicationRunListener#contextLoaded(ConfigurableApplicationContext)}}的运行阶段
     */
    private BaseStatics applicationContextLoadStage;

    public SpringApplicationRunListener(org.springframework.boot.SpringApplication springApplication) {
        this.application = springApplication;
        this.startupReporter = new StartupReporter();
        Logger.debug("Initialized SpringApplicationRunListener for application: {}",
                springApplication.getMainApplicationClass());
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        jvmStartingStage = new BaseStatics();
        jvmStartingStage.setName(GeniusBuilder.JVM_STARTING_STAGE);
        jvmStartingStage.setStartTime(ManagementFactory.getRuntimeMXBean().getStartTime());
        jvmStartingStage.setEndTime(System.currentTimeMillis());
        Logger.debug("JVM starting stage completed in {} ms", jvmStartingStage.getCost());
    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext,
            ConfigurableEnvironment environment) {
        application.setBanner(new TextBanner());
        environmentPrepareStage = new BaseStatics();
        environmentPrepareStage.setName(GeniusBuilder.ENVIRONMENT_PREPARE_STAGE);
        environmentPrepareStage.setStartTime(jvmStartingStage.getEndTime());
        environmentPrepareStage.setEndTime(System.currentTimeMillis());
        startupReporter.setAppName(environment.getProperty(GeniusBuilder.APP_NAME));
        startupReporter.bindToStartupReporter(environment);
        bootstrapContext.register(StartupReporter.class, key -> startupReporter);

        try {
            Class.forName("org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup");
            application.setApplicationStartup(
                    new org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup(
                            startupReporter.bufferSize));
        } catch (ClassNotFoundException e) {
            Logger.debug("BufferingApplicationStartup not available, skipping startup metrics");
        }
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        applicationContextPrepareStage = new ChildrenStatics<>();
        applicationContextPrepareStage.setName(GeniusBuilder.APPLICATION_CONTEXT_PREPARE_STAGE);
        applicationContextPrepareStage.setStartTime(environmentPrepareStage.getEndTime());
        applicationContextPrepareStage.setEndTime(System.currentTimeMillis());
        if (application instanceof SpringApplication springApplication) {
            List<BaseStatics> statisticsList = springApplication.getInitializerStartupStatList();
            applicationContextPrepareStage.setChildren(new ArrayList<>(statisticsList));
            statisticsList.clear();
        }
        Logger.debug("Application context preparation stage completed in {} ms",
                applicationContextPrepareStage.getCost());
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        applicationContextLoadStage = new BaseStatics();
        applicationContextLoadStage.setName(GeniusBuilder.APPLICATION_CONTEXT_LOAD_STAGE);
        applicationContextLoadStage.setStartTime(applicationContextPrepareStage.getEndTime());
        applicationContextLoadStage.setEndTime(System.currentTimeMillis());
        context.getBeanFactory().addBeanPostProcessor(new StartupReporterProcessor(startupReporter));
        context.getBeanFactory().registerSingleton("STARTUP_REPORTER_BEAN", startupReporter);
        SpringSmartLifecycle springSmartLifecycle = new SpringSmartLifecycle(startupReporter);
        springSmartLifecycle.setApplicationContext(context);
        context.getBeanFactory().registerSingleton("STARTUP_SMART_LIFECYCLE", springSmartLifecycle);
        Logger.debug("Application context loading stage completed in {} ms", applicationContextLoadStage.getCost());
    }

    @Override
    public void started(ConfigurableApplicationContext context, Duration timeTaken) {
        ChildrenStatics<ModuleStatics> applicationRefreshStage = (ChildrenStatics<ModuleStatics>) startupReporter
                .getStageNyName(GeniusBuilder.APPLICATION_CONTEXT_REFRESH_STAGE);
        applicationRefreshStage.setStartTime(applicationContextLoadStage.getEndTime());
        applicationRefreshStage.setEndTime(System.currentTimeMillis());
        applicationRefreshStage.setCost(applicationRefreshStage.getEndTime() - applicationRefreshStage.getStartTime());

        ModuleStatics rootModule = applicationRefreshStage.getChildren().get(0);
        rootModule.setStartTime(applicationRefreshStage.getStartTime());
        rootModule.setCost(rootModule.getEndTime() - rootModule.getStartTime());

        startupReporter.addCommonStartupStat(jvmStartingStage);
        startupReporter.addCommonStartupStat(environmentPrepareStage);
        startupReporter.addCommonStartupStat(applicationContextPrepareStage);
        startupReporter.addCommonStartupStat(applicationContextLoadStage);
        startupReporter.applicationBootFinish();

        Logger.info(getStartedMessage(context, timeTaken));
    }

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        Logger.debug("Received ApplicationStartedEvent for application: {}", application.getMainApplicationClass());
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }

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