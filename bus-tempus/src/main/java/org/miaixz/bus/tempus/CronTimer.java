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
package org.miaixz.bus.tempus;

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.center.date.Chrono;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.logger.Logger;

/**
 * The timer thread for the cron scheduler. This thread checks the task list every minute (or second, depending on
 * configuration) and executes any matching tasks.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CronTimer extends Thread implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852286896673L;

    /**
     * Timer unit in milliseconds for a second.
     */
    private final long TIMER_UNIT_SECOND = Chrono.SECOND.getMillis();
    /**
     * Timer unit in milliseconds for a minute.
     */
    private final long TIMER_UNIT_MINUTE = Chrono.MINUTE.getMillis();
    /**
     * The scheduler instance for managing cron tasks.
     */
    private final Scheduler scheduler;
    /**
     * A flag indicating whether the timer has been forcibly stopped.
     */
    private volatile boolean isStop;

    /**
     * Constructs a new CronTimer.
     *
     * @param scheduler The {@link Scheduler}.
     */
    public CronTimer(final Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Checks if the sleep time is valid. A valid sleep time must be:
     * 
     * <pre>
     * 1. Greater than 0, to prevent issues if the system time is moved forward.
     * 2. Less than twice the timer unit, to prevent long sleeps if the system time is moved backward.
     * </pre>
     *
     * @param millis    The sleep time in milliseconds.
     * @param timerUnit The timer unit (milliseconds for a second or a minute).
     * @return {@code true} if the sleep time is valid, {@code false} otherwise.
     */
    private static boolean isValidSleepMillis(final long millis, final long timerUnit) {
        return millis > 0 &&
        // Prevent long sleeps due to system time being moved backward.
                millis < (2 * timerUnit);
    }

    /**
     * Executes the timer thread, checking for tasks at regular intervals. This method continuously checks the task list
     * every minute (or second, depending on configuration) and executes any matching tasks. It handles edge cases such
     * as system time changes (forward or backward) and ensures proper task execution timing.
     * <p>
     * The timer loop:
     * <ol>
     * <li>Spawns a launcher to check for matching tasks at the current time</li>
     * <li>Calculates the next execution time point</li>
     * <li>Handles edge cases like time rollbacks and catching up with missed executions</li>
     * <li>Sleeps until the next scheduled time point</li>
     * </ol>
     * <p>
     * <b>Note:</b> This method is designed to be called by the thread's start mechanism and should not be invoked
     * directly. The timer will continue running until {@link #stopTimer()} is called.
     * </p>
     */
    @Override
    public void run() {
        final long timerUnit = this.scheduler.config.isMatchSecond() ? TIMER_UNIT_SECOND : TIMER_UNIT_MINUTE;
        final long doubleTimeUnit = 2 * timerUnit;

        long thisTime = System.currentTimeMillis();
        while (!isStop) {
            spawnLauncher(thisTime);

            // The next time calculation is based on the previous execution point's start time
            // Dividing by the timer unit here is to clear the parts below the unit,
            // for example, if the unit is minute, seconds and milliseconds are cleared
            long nextTime = ((thisTime / timerUnit) + 1) * timerUnit;
            final long sleep = nextTime - System.currentTimeMillis();
            if (sleep < 0) {
                // Possible slow loop execution causing time points to lag behind system time,
                // catch up with system time and execute the intermediate time points
                thisTime = System.currentTimeMillis();
                while (nextTime <= thisTime) {
                    // Catch up with system time and run execution points
                    spawnLauncher(nextTime);
                    nextTime = ((thisTime / timerUnit) + 1) * timerUnit;
                }
                continue;
            } else if (sleep > doubleTimeUnit) {
                // Time rollback, possibly the user turned back time or the system automatically corrected time,
                // recalculate
                thisTime = System.currentTimeMillis();
                continue;
            } else if (!ThreadKit.safeSleep(sleep)) {
                // Wait until the next time point, exit Timer directly if interrupted by user
                break;
            }

            // Use additive method to ensure exactly 1 minute or 1 second, avoiding late wake-up issues
            // No validation needed here, as each loop calculates the time difference between sleep and the last trigger
            // point.
            // When the previous wake-up is late, this time will reduce the sleep time, ensuring the error is within one
            // unit and continuously correcting.
            thisTime = nextTime;
        }
        Logger.debug("Cron timer stopped.");
    }

    /**
     * Stops the timer thread gracefully.
     */
    synchronized public void stopTimer() {
        this.isStop = true;
        ThreadKit.interrupt(this, true);
    }

    /**
     * Spawns a launcher to check for matching tasks at the given time.
     *
     * @param millis The current time in milliseconds.
     */
    private void spawnLauncher(final long millis) {
        this.scheduler.manager.spawnLauncher(millis);
    }

}
