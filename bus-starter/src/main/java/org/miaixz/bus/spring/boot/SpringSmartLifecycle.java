/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
 * Implements {@link SmartLifecycle} to calculate application context refresh time.
 * <p>
 * This class is used to monitor and record performance metrics during the Spring application context refresh process.
 * By implementing the SmartLifecycle interface, it automatically triggers the collection of context refresh time
 * statistics during application startup and adds these statistics to the {@link StartupReporter}, providing data
 * support for application startup performance analysis.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SpringSmartLifecycle implements SmartLifecycle, ApplicationContextAware {

    /**
     * Constant for the root module name.
     */
    public static final String ROOT_MODULE_NAME = "ROOT_APPLICATION_CONTEXT";

    /**
     * The component responsible for collecting and reporting startup costs.
     */
    private final StartupReporter startupReporter;

    /**
     * The application context.
     */
    private ConfigurableApplicationContext applicationContext;

    /**
     * Constructs a new {@code SpringSmartLifecycle} instance.
     *
     * @param startupReporter The {@link StartupReporter} instance to which startup statistics will be added.
     */
    public SpringSmartLifecycle(StartupReporter startupReporter) {
        this.startupReporter = startupReporter;
    }

    /**
     * Sets the application context.
     *
     * @param applicationContext The application context.
     * @throws BeansException if setting the context fails.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    /**
     * Starts the lifecycle component.
     * <p>
     * This method is called during application startup to calculate the application context refresh time. It creates
     * and initializes context refresh stage statistics and root module statistics, then adds these statistics to the
     * {@link StartupReporter}.
     * </p>
     */
    @Override
    public void start() {
        // Initialize context refresh stage statistics
        ChildrenMetrics<ModuleMetrics> stat = new ChildrenMetrics<>();
        stat.setName(GeniusBuilder.APPLICATION_CONTEXT_REFRESH_STAGE);
        stat.setEndTime(System.currentTimeMillis());

        // Build root module statistics
        ModuleMetrics rootModuleStat = new ModuleMetrics();
        rootModuleStat.setName(ROOT_MODULE_NAME);
        rootModuleStat.setEndTime(stat.getEndTime());
        rootModuleStat.setThreadName(Thread.currentThread().getName());

        // Get Bean statistics list from ApplicationStartup
        rootModuleStat.setChildren(startupReporter.generateBeanStats(applicationContext));

        // Add root module to context refresh stage statistics
        stat.addChild(rootModuleStat);

        // Add context refresh stage statistics to the startup reporter
        startupReporter.addCommonStartupStat(stat);
    }

    /**
     * Stops the lifecycle component.
     * <p>
     * This method is an empty implementation as no specific actions are required during stopping.
     * </p>
     */
    @Override
    public void stop() {
        // Empty implementation, no operations needed on stop.
    }

    /**
     * Checks if the lifecycle component is currently running.
     *
     * @return Always returns {@code false}, indicating that this component does not require continuous running.
     */
    @Override
    public boolean isRunning() {
        return false;
    }

    /**
     * Returns the phase in which this lifecycle component should be executed.
     *
     * @return {@code Integer.MIN_VALUE}, indicating that this component should be executed at the earliest possible
     *         phase.
     */
    @Override
    public int getPhase() {
        return Integer.MIN_VALUE;
    }

}
