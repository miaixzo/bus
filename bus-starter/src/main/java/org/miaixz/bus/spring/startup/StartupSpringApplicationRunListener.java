/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.spring.startup;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.spring.banner.TextBanner;
import org.miaixz.bus.spring.startup.statics.BaseStatics;
import org.miaixz.bus.spring.startup.statics.ChildrenStatics;
import org.miaixz.bus.spring.startup.statics.ModuleStatics;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.metrics.ApplicationStartup;

/**
 * 实现{@link SpringApplicationRunListener}来计算启动阶段需要花费时间
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StartupSpringApplicationRunListener implements SpringApplicationRunListener, Ordered {

    /**
     * Spring boot 主引导和启动Spring应用程序
     */
    private final SpringApplication application;

    /**
     * 收集和启动报告成本的基本组件
     */
    private final StartupReporter startupReporter;

    /**
     * JVM启动后的运行阶段{@link SpringApplicationRunListener#started(ConfigurableApplicationContext, Duration)} ()}
     */
    private BaseStatics jvmStartingStage;

    /**
     * 从{@link SpringApplicationRunListener#started(ConfigurableApplicationContext, Duration)}()}
     * 到{@link SpringApplicationRunListener#environmentPrepared(ConfigurableBootstrapContext, ConfigurableEnvironment)}
     * (ConfigurableEnvironment)}}的运行阶段
     */
    private BaseStatics environmentPrepareStage;

    /**
     * 从{@link SpringApplicationRunListener#environmentPrepared(ConfigurableBootstrapContext, ConfigurableEnvironment)}
     * (ConfigurableEnvironment)}
     * 到{@link SpringApplicationRunListener#contextPrepared(ConfigurableApplicationContext)}}的运行阶段
     */
    private ChildrenStatics<BaseStatics> applicationContextPrepareStage;

    /**
     * 从{@link SpringApplicationRunListener#contextPrepared(ConfigurableApplicationContext)}
     * 到{@link SpringApplicationRunListener#contextLoaded(ConfigurableApplicationContext)}}的运行阶段
     */
    private BaseStatics applicationContextLoadStage;

    public StartupSpringApplicationRunListener(SpringApplication springApplication) {
        this.application = springApplication;
        this.startupReporter = new StartupReporter();
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        jvmStartingStage = new BaseStatics();
        jvmStartingStage.setName(GeniusBuilder.JVM_STARTING_STAGE);
        jvmStartingStage.setStartTime(ManagementFactory.getRuntimeMXBean().getStartTime());
        jvmStartingStage.setEndTime(System.currentTimeMillis());
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

        ApplicationStartup userApplicationStartup = application.getApplicationStartup();
        if (ApplicationStartup.DEFAULT == userApplicationStartup || userApplicationStartup == null) {
            application.setApplicationStartup(new BufferingApplicationStartup(startupReporter.bufferSize));
        }
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        applicationContextPrepareStage = new ChildrenStatics<>();
        applicationContextPrepareStage.setName(GeniusBuilder.APPLICATION_CONTEXT_PREPARE_STAGE);
        applicationContextPrepareStage.setStartTime(environmentPrepareStage.getEndTime());
        applicationContextPrepareStage.setEndTime(System.currentTimeMillis());
        if (application instanceof StartupSpringApplication startupSpringApplication) {
            List<BaseStatics> statisticsList = startupSpringApplication.getInitializerStartupStatList();
            applicationContextPrepareStage.setChildren(new ArrayList<>(statisticsList));
            statisticsList.clear();
        }
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        applicationContextLoadStage = new BaseStatics();
        applicationContextLoadStage.setName(GeniusBuilder.APPLICATION_CONTEXT_LOAD_STAGE);
        applicationContextLoadStage.setStartTime(applicationContextPrepareStage.getEndTime());
        applicationContextLoadStage.setEndTime(System.currentTimeMillis());
        context.getBeanFactory().addBeanPostProcessor(new StartupReporterProcessor(startupReporter));
        context.getBeanFactory().registerSingleton("STARTUP_REPORTER_BEAN", startupReporter);
        StartupSmartLifecycle startupSmartLifecycle = new StartupSmartLifecycle(startupReporter);
        startupSmartLifecycle.setApplicationContext(context);
        context.getBeanFactory().registerSingleton("STARTUP_SMART_LIfE_CYCLE", startupSmartLifecycle);
    }

    @Override
    public void started(ConfigurableApplicationContext context, Duration timeTaken) {
        ChildrenStatics<ModuleStatics> applicationRefreshStage = (ChildrenStatics<ModuleStatics>) startupReporter
                .getStageNyName(GeniusBuilder.APPLICATION_CONTEXT_REFRESH_STAGE);
        applicationRefreshStage.setStartTime(applicationContextLoadStage.getEndTime());
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
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 10;
    }

    private String getStartedMessage(ConfigurableApplicationContext context, Duration timeTakenToStartup) {
        ConfigurableEnvironment environment = context.getEnvironment();
        StringBuilder message = new StringBuilder();
        message.append("Started ");

        String name = environment.getProperty(GeniusBuilder.APP_NAME);
        if (StringKit.hasText(name)) {
            message.append(name);
        }

        String logging = environment.getProperty(GeniusBuilder.BUS_STARTUP_LOGGING);
        if (StringKit.hasText(logging)) {
            message.append(" with [");
            message.append(logging);
            message.append("]");
        }

        message.append(" in ");
        message.append(timeTakenToStartup.toMillis() / 1000.0);
        message.append(" seconds");
        return message.toString();
    }

}
