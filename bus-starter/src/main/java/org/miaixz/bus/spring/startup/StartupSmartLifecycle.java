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

import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.spring.startup.statics.ChildrenStatics;
import org.miaixz.bus.spring.startup.statics.ModuleStatics;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.SmartLifecycle;

/**
 * 实现{@link SmartLifecycle}计算应用程序上下文刷新时间
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StartupSmartLifecycle implements SmartLifecycle, ApplicationContextAware {

    public static final String ROOT_MODULE_NAME = "ROOT_APPLICATION_CONTEXT";
    /**
     * 收集和启动报告成本的基本组件
     */
    private final StartupReporter startupReporter;

    /**
     * 应用程序上下文
     */
    private ConfigurableApplicationContext applicationContext;

    public StartupSmartLifecycle(StartupReporter startupReporter) {
        this.startupReporter = startupReporter;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    @Override
    public void start() {
        // 初始化 ContextRefreshStageStat
        ChildrenStatics<ModuleStatics> stat = new ChildrenStatics<>();
        stat.setName(GeniusBuilder.APPLICATION_CONTEXT_REFRESH_STAGE);
        stat.setEndTime(System.currentTimeMillis());

        // 构建根模块
        ModuleStatics rootModuleStat = new ModuleStatics();
        rootModuleStat.setName(ROOT_MODULE_NAME);
        rootModuleStat.setEndTime(stat.getEndTime());
        rootModuleStat.setThreadName(Thread.currentThread().getName());

        // 从ApplicationStartup获取beanstatlist
        rootModuleStat.setChildren(startupReporter.generateBeanStats(applicationContext));

        // 报告ContextRefreshStageStat
        stat.addChild(rootModuleStat);
        startupReporter.addCommonStartupStat(stat);
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public int getPhase() {
        return Integer.MIN_VALUE;
    }

}
