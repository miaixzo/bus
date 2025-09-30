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

import org.miaixz.bus.spring.GeniusBuilder;
import org.miaixz.bus.spring.metrics.ChildrenMetrics;
import org.miaixz.bus.spring.metrics.ModuleMetrics;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.SmartLifecycle;

/**
 * 实现{@link SmartLifecycle}计算应用程序上下文刷新时间。
 * <p>
 * 该类用于监控和记录Spring应用程序上下文刷新过程中的性能指标。 通过实现SmartLifecycle接口，在应用程序启动过程中自动触发上下文刷新时间的统计， 并将统计信息添加到启动报告器中，为应用程序启动性能分析提供数据支持。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpringSmartLifecycle implements SmartLifecycle, ApplicationContextAware {

    /**
     * 根模块名称常量
     */
    public static final String ROOT_MODULE_NAME = "ROOT_APPLICATION_CONTEXT";

    /**
     * 收集和报告启动成本的基本组件
     */
    private final StartupReporter startupReporter;

    /**
     * 应用程序上下文
     */
    private ConfigurableApplicationContext applicationContext;

    /**
     * 构造函数，初始化SpringSmartLifecycle
     *
     * @param startupReporter 启动报告器实例
     */
    public SpringSmartLifecycle(StartupReporter startupReporter) {
        this.startupReporter = startupReporter;
    }

    /**
     * 设置应用程序上下文
     *
     * @param applicationContext 应用程序上下文
     * @throws BeansException 如果设置上下文时发生异常
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    /**
     * 启动生命周期组件
     * <p>
     * 在应用程序启动过程中调用此方法，用于计算应用程序上下文刷新时间。 创建并初始化上下文刷新阶段统计信息和根模块统计信息，并将统计信息添加到启动报告器中。
     * </p>
     */
    @Override
    public void start() {
        // 初始化上下文刷新阶段统计信息
        ChildrenMetrics<ModuleMetrics> stat = new ChildrenMetrics<>();
        stat.setName(GeniusBuilder.APPLICATION_CONTEXT_REFRESH_STAGE);
        stat.setEndTime(System.currentTimeMillis());

        // 构建根模块统计信息
        ModuleMetrics rootModuleStat = new ModuleMetrics();
        rootModuleStat.setName(ROOT_MODULE_NAME);
        rootModuleStat.setEndTime(stat.getEndTime());
        rootModuleStat.setThreadName(Thread.currentThread().getName());

        // 从ApplicationStartup获取Bean统计列表
        rootModuleStat.setChildren(startupReporter.generateBeanStats(applicationContext));

        // 将根模块添加到上下文刷新阶段统计信息中
        stat.addChild(rootModuleStat);

        // 将上下文刷新阶段统计信息添加到启动报告器中
        startupReporter.addCommonStartupStat(stat);
    }

    /**
     * 停止生命周期组件
     * <p>
     * 此方法为空实现，因为不需要在停止时执行任何操作
     * </p>
     */
    @Override
    public void stop() {
        // 空实现，不需要在停止时执行任何操作
    }

    /**
     * 检查生命周期组件是否正在运行
     *
     * @return 始终返回false，表示此组件不需要持续运行
     */
    @Override
    public boolean isRunning() {
        return false;
    }

    /**
     * 获取生命周期组件的执行阶段
     *
     * @return 返回Integer.MIN_VALUE，表示此组件应该在最早阶段执行
     */
    @Override
    public int getPhase() {
        return Integer.MIN_VALUE;
    }

}
