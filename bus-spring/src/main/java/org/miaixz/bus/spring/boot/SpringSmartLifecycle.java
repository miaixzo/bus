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

import org.springframework.context.SmartLifecycle;

import org.miaixz.bus.spring.boot.startup.SpringStartupCollector;
import org.miaixz.bus.spring.boot.startup.SpringStartupSummary.Stage;

/**
 * Captures application-context refresh duration once during startup.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SpringSmartLifecycle implements SmartLifecycle {

    /**
     * Reporter receiving application-context refresh metrics.
     */
    private final SpringStartupCollector startupCollector;
    /**
     * Application-context refresh start timestamp in milliseconds.
     */
    private final long refreshStartTime;
    /**
     * Tracks whether this lifecycle has recorded its refresh metrics.
     */
    private final AtomicBoolean running = new AtomicBoolean();

    /**
     * Creates the lifecycle for one startup report.
     *
     * @param startupCollector current startup collector
     * @param refreshStartTime refresh start time
     */
    public SpringSmartLifecycle(SpringStartupCollector startupCollector, long refreshStartTime) {
        this.startupCollector = startupCollector;
        this.refreshStartTime = refreshStartTime;
    }

    /**
     * Records context-refresh metrics once the lifecycle processor starts this component.
     */
    @Override
    public void start() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        startupCollector.addStage(Stage.between(
                Stage.APPLICATION_CONTEXT_REFRESH,
                refreshStartTime,
                System.currentTimeMillis()));
    }

    /**
     * Stops metric collection.
     */
    @Override
    public void stop() {
        running.set(false);
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
