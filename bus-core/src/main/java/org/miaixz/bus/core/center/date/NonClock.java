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
package org.miaixz.bus.core.center.date;

import java.sql.Timestamp;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * System clock optimization for high-concurrency scenarios. Optimizes the performance issue of
 * System.currentTimeMillis() in high-concurrency scenarios. Calling System.currentTimeMillis() is more time-consuming
 * than creating a new ordinary object (some say it's about 100 times slower). System.currentTimeMillis() is slow
 * because it interacts with the system. This class updates the clock in the background periodically. When the JVM
 * exits, the thread is automatically reclaimed. See:
 * <a href="http://git.oschina.net/yu120/sequence">http://git.oschina.net/yu120/sequence</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NonClock {

    /**
     * Clock update interval, in milliseconds.
     */
    private final long period;
    /**
     * The current time in milliseconds.
     */
    private volatile long now;

    /**
     * Constructs a {@code NonClock} instance.
     *
     * @param period The clock update interval, in milliseconds.
     */
    public NonClock(final long period) {
        this.period = period;
        this.now = System.currentTimeMillis();
        scheduleClockUpdating();
    }

    /**
     * Gets the current time in milliseconds.
     *
     * @return The current time in milliseconds.
     */
    public static long now() {
        return InstanceHolder.INSTANCE.currentTimeMillis();
    }

    /**
     * Gets the current time as a string.
     *
     * @return The current time in string representation.
     */
    public static String nowDate() {
        return new Timestamp(InstanceHolder.INSTANCE.currentTimeMillis()).toString();
    }

    /**
     * Starts the clock update thread.
     */
    private void scheduleClockUpdating() {
        final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            final Thread thread = new Thread(runnable, "System Clock");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(() -> now = System.currentTimeMillis(), period, period, TimeUnit.MILLISECONDS);
    }

    /**
     * Gets the current time in milliseconds.
     *
     * @return The current time in milliseconds.
     */
    private long currentTimeMillis() {
        return now;
    }

    /**
     * Singleton holder for {@code NonClock}.
     */
    private static class InstanceHolder {

        /**
         * The singleton instance of {@code NonClock} with a 1ms update period.
         */
        public static final NonClock INSTANCE = new NonClock(1);
    }

}
