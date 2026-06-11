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
package org.miaixz.bus.starter.tempus;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.SmartLifecycle;

import org.miaixz.bus.tempus.temporal.Subscriber;

/**
 * Spring lifecycle bridge for Temporal subscriber startup and shutdown.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TempusLifecycle implements SmartLifecycle {

    /**
     * Phase used to start after regular infrastructure beans.
     */
    private static final int PHASE = Integer.MAX_VALUE - 100;

    /**
     * Subscriber managed by this lifecycle.
     */
    private final Subscriber subscriber;

    /**
     * Lifecycle running flag.
     */
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Creates a Temporal lifecycle bridge.
     *
     * @param subscriber Temporal subscriber
     */
    public TempusLifecycle(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    /**
     * Starts the subscriber once.
     */
    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            subscriber.start();
        }
    }

    /**
     * Stops the subscriber once.
     */
    @Override
    public void stop() {
        if (running.compareAndSet(true, false)) {
            subscriber.shutdown();
        }
    }

    /**
     * Checks whether this lifecycle is running.
     *
     * @return {@code true} when running
     */
    @Override
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Starts automatically with the Spring context.
     *
     * @return {@code true}
     */
    @Override
    public boolean isAutoStartup() {
        return true;
    }

    /**
     * Returns the lifecycle phase.
     *
     * @return lifecycle phase
     */
    @Override
    public int getPhase() {
        return PHASE;
    }

}
