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

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.SmartLifecycle;

import org.miaixz.bus.spring.boot.startup.ChildrenMetrics;
import org.miaixz.bus.spring.boot.startup.ModuleMetrics;
import org.miaixz.bus.spring.boot.startup.StartupReporter;
import org.miaixz.bus.spring.boot.startup.StartupStages;

/**
 * Captures context refresh statistics once and releases its context reference when stopped.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SpringSmartLifecycle implements SmartLifecycle, ApplicationContextAware {

    /**
     * Root module name used in context refresh statistics.
     */
    public static final String ROOT_MODULE_NAME = "ROOT_APPLICATION_CONTEXT";

    /**
     * Reporter receiving application-context refresh metrics.
     */
    private final StartupReporter startupReporter;
    /**
     * Application-context refresh start timestamp in milliseconds.
     */
    private final long refreshStartTime;
    /**
     * Tracks whether this lifecycle has recorded its refresh metrics.
     */
    private final AtomicBoolean running = new AtomicBoolean();

    /**
     * Configurable application context whose refresh is being measured.
     */
    private volatile ConfigurableApplicationContext applicationContext;

    /**
     * Creates the lifecycle for one startup report.
     *
     * @param startupReporter  current startup reporter
     * @param refreshStartTime refresh start time
     */
    public SpringSmartLifecycle(StartupReporter startupReporter, long refreshStartTime) {
        this.startupReporter = startupReporter;
        this.refreshStartTime = refreshStartTime;
    }

    /**
     * Retains the configurable context whose completed refresh will be measured.
     *
     * @param applicationContext owning application context
     * @throws BeansException if Spring cannot supply the context callback
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (!(applicationContext instanceof ConfigurableApplicationContext configurableContext)) {
            throw new IllegalArgumentException("SpringSmartLifecycle requires a ConfigurableApplicationContext");
        }
        this.applicationContext = configurableContext;
    }

    /**
     * Records context-refresh metrics once the lifecycle processor starts this component.
     */
    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        ConfigurableApplicationContext context = applicationContext;
        try {
            if (context == null || !context.isActive()) {
                throw new IllegalStateException("Application context is not active");
            }

            long refreshEndTime = System.currentTimeMillis();
            ModuleMetrics rootModule = new ModuleMetrics(ROOT_MODULE_NAME, refreshStartTime, refreshEndTime,
                    Thread.currentThread().getName(), startupReporter.generateBeanStats(context));
            ChildrenMetrics<ModuleMetrics> refreshStage = new ChildrenMetrics<>(
                    StartupStages.APPLICATION_CONTEXT_REFRESH_STAGE, refreshStartTime, refreshEndTime,
                    java.util.List.of(rootModule));

            startupReporter.addCommonStartupStat(refreshStage);
        } catch (RuntimeException | Error ex) {
            running.set(false);
            applicationContext = null;
            throw ex;
        }
    }

    /**
     * Stops metric collection and releases the application-context reference.
     */
    @Override
    public void stop() {
        running.set(false);
        applicationContext = null;
    }

    /**
     * Stops this lifecycle component and always signals asynchronous completion.
     *
     * @param callback completion callback supplied by Spring
     */
    @Override
    public void stop(Runnable callback) {
        try {
            stop();
        } finally {
            callback.run();
        }
    }

    /**
     * Returns whether refresh metrics have been recorded for the current start cycle.
     *
     * @return {@code true} while this lifecycle component is running
     */
    @Override
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Starts this component in the earliest lifecycle phase.
     *
     * @return the earliest lifecycle phase value
     */
    @Override
    public int getPhase() {
        return Integer.MIN_VALUE;
    }

}
