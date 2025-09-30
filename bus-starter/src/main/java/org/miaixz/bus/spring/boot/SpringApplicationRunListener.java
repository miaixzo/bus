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

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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

/**
 * 实现 {@link org.springframework.boot.SpringApplicationRunListener} 和 {@link ApplicationListener}，计算启动阶段时间。
 * <p>
 * 该类用于监控和记录Spring应用程序启动过程中的各个阶段耗时，支持动态加载和性能统计。 它记录JVM启动、环境准备、上下文初始化等关键阶段的性能指标，为应用程序启动性能分析提供数据支持。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpringApplicationRunListener implements org.springframework.boot.SpringApplicationRunListener,
        ApplicationListener<ApplicationStartedEvent>, Ordered {

    /**
     * Spring Boot主引导和启动Spring应用程序的实例
     */
    private final org.springframework.boot.SpringApplication application;

    /**
     * 收集和报告启动成本的基本组件
     */
    private final StartupReporter startupReporter;

    /**
     * JVM启动后的运行阶段统计信息
     * <p>
     * 记录从JVM启动到SpringApplicationRunListener.starting方法执行完成的时间
     * </p>
     */
    private BaseMetrics jvmStartingStage;

    /**
     * 环境准备阶段的运行阶段统计信息
     * <p>
     * 记录从started方法执行到environmentPrepared方法执行完成的时间
     * </p>
     */
    private BaseMetrics environmentPrepareStage;

    /**
     * 应用程序上下文准备阶段的运行阶段统计信息
     * <p>
     * 记录从environmentPrepared方法执行到contextPrepared方法执行完成的时间
     * </p>
     */
    private ChildrenMetrics<BaseMetrics> applicationContextPrepareStage;

    /**
     * 应用程序上下文加载阶段的运行阶段统计信息
     * <p>
     * 记录从contextPrepared方法执行到contextLoaded方法执行完成的时间
     * </p>
     */
    private BaseMetrics applicationContextLoadStage;

    /**
     * 构造函数，初始化SpringApplicationRunListener
     *
     * @param springApplication Spring应用程序实例
     */
    public SpringApplicationRunListener(org.springframework.boot.SpringApplication springApplication) {
        this.application = springApplication;
        this.startupReporter = new StartupReporter();
        Logger.debug(
                "Initialized SpringApplicationRunListener for application: {}",
                springApplication.getMainApplicationClass());
    }

    /**
     * 应用程序启动开始时的回调方法
     * <p>
     * 记录JVM启动阶段的开始和结束时间，计算JVM启动耗时
     * </p>
     *
     * @param bootstrapContext 可配置的引导上下文
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
     * 环境准备完成时的回调方法
     * <p>
     * 记录环境准备阶段的开始和结束时间，计算环境准备耗时，并设置应用程序名称
     * </p>
     *
     * @param bootstrapContext 可配置的引导上下文
     * @param environment      可配置的环境
     */
    @Override
    public void environmentPrepared(
            ConfigurableBootstrapContext bootstrapContext,
            ConfigurableEnvironment environment) {
        environmentPrepareStage = new BaseMetrics();
        environmentPrepareStage.setName(GeniusBuilder.ENVIRONMENT_PREPARE_STAGE);
        environmentPrepareStage.setStartTime(jvmStartingStage.getEndTime());
        environmentPrepareStage.setEndTime(System.currentTimeMillis());

        // 设置应用程序名称并绑定到启动报告器
        startupReporter.setAppName(environment.getProperty(GeniusBuilder.APP_NAME));
        startupReporter.bindToStartupReporter(environment);

        // 注册启动报告器到引导上下文
        bootstrapContext.register(StartupReporter.class, key -> startupReporter);

        // 尝试设置BufferingApplicationStartup（如果可用）
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
     * 应用程序上下文准备完成时的回调方法
     * <p>
     * 记录应用程序上下文准备阶段的开始和结束时间，计算上下文准备耗时， 并添加SpringApplication中收集的初始化器统计信息
     * </p>
     *
     * @param context 可配置的应用程序上下文
     */
    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        applicationContextPrepareStage = new ChildrenMetrics<>();
        applicationContextPrepareStage.setName(GeniusBuilder.APPLICATION_CONTEXT_PREPARE_STAGE);
        applicationContextPrepareStage.setStartTime(environmentPrepareStage.getEndTime());
        applicationContextPrepareStage.setEndTime(System.currentTimeMillis());

        // 如果是自定义的SpringApplication，获取初始化器统计信息
        if (application instanceof SpringApplication springApplication) {
            List<BaseMetrics> statisticsList = springApplication.getInitializerStartupStatList();
            applicationContextPrepareStage.setChildren(new ArrayList<>(statisticsList));
            statisticsList.clear();
        }

        Logger.debug(
                "Application context preparation stage completed in {} ms",
                applicationContextPrepareStage.getCost());
    }

    /**
     * 应用程序上下文加载完成时的回调方法
     * <p>
     * 记录应用程序上下文加载阶段的开始和结束时间，计算上下文加载耗时， 并注册启动报告器和生命周期处理器
     * </p>
     *
     * @param context 可配置的应用程序上下文
     */
    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        applicationContextLoadStage = new BaseMetrics();
        applicationContextLoadStage.setName(GeniusBuilder.APPLICATION_CONTEXT_LOAD_STAGE);
        applicationContextLoadStage.setStartTime(applicationContextPrepareStage.getEndTime());
        applicationContextLoadStage.setEndTime(System.currentTimeMillis());

        // 注册启动报告器处理器和启动报告器
        context.getBeanFactory().addBeanPostProcessor(new StartupReporterProcessor(startupReporter));
        context.getBeanFactory().registerSingleton("STARTUP_REPORTER_BEAN", startupReporter);

        // 注册智能生命周期处理器
        SpringSmartLifecycle springSmartLifecycle = new SpringSmartLifecycle(startupReporter);
        springSmartLifecycle.setApplicationContext(context);
        context.getBeanFactory().registerSingleton("STARTUP_SMART_LIFECYCLE", springSmartLifecycle);

        Logger.debug("Application context loading stage completed in {} ms", applicationContextLoadStage.getCost());
    }

    /**
     * 应用程序启动完成时的回调方法
     * <p>
     * 记录应用程序刷新阶段的开始和结束时间，计算刷新耗时， 添加所有阶段的统计信息到启动报告器，并标记应用程序启动完成
     * </p>
     *
     * @param context   可配置的应用程序上下文
     * @param timeTaken 启动所花费的时间
     */
    @Override
    public void started(ConfigurableApplicationContext context, Duration timeTaken) {
        // 获取应用程序刷新阶段统计信息
        ChildrenMetrics<ModuleMetrics> applicationRefreshStage = (ChildrenMetrics<ModuleMetrics>) startupReporter
                .getStageNyName(GeniusBuilder.APPLICATION_CONTEXT_REFRESH_STAGE);

        // 设置刷新阶段的时间信息
        applicationRefreshStage.setStartTime(applicationContextLoadStage.getEndTime());
        applicationRefreshStage.setEndTime(System.currentTimeMillis());
        applicationRefreshStage.setCost(applicationRefreshStage.getEndTime() - applicationRefreshStage.getStartTime());

        // 设置根模块的时间信息
        ModuleMetrics rootModule = applicationRefreshStage.getChildren().get(0);
        rootModule.setStartTime(applicationRefreshStage.getStartTime());
        rootModule.setCost(rootModule.getEndTime() - rootModule.getStartTime());

        // 添加所有阶段的统计信息到启动报告器
        startupReporter.addCommonStartupStat(jvmStartingStage);
        startupReporter.addCommonStartupStat(environmentPrepareStage);
        startupReporter.addCommonStartupStat(applicationContextPrepareStage);
        startupReporter.addCommonStartupStat(applicationContextLoadStage);

        // 标记应用程序启动完成
        startupReporter.applicationBootFinish();

        // 输出启动完成消息
        Logger.info(getStartedMessage(context, timeTaken));
    }

    /**
     * 处理应用程序启动事件
     * <p>
     * 当接收到ApplicationStartedEvent事件时调用此方法
     * </p>
     *
     * @param event 应用程序启动事件
     */
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        Logger.debug("Received ApplicationStartedEvent for application: {}", application.getMainApplicationClass());
    }

    /**
     * 获取此监听器的顺序
     *
     * @return 顺序值，值越小优先级越高
     */
    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }

    /**
     * 生成应用程序启动完成消息
     * <p>
     * 构建包含应用程序名称、配置名称、活动配置文件、日志级别和启动时间的消息
     * </p>
     *
     * @param context            可配置的应用程序上下文
     * @param timeTakenToStartup 启动所花费的时间
     * @return 启动完成消息字符串
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

        // 获取日志级别
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
