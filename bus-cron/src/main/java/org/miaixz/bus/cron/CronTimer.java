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
package org.miaixz.bus.cron;

import org.miaixz.bus.core.center.date.culture.en.Units;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.logger.Logger;

import java.io.Serial;
import java.io.Serializable;

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
    private final long TIMER_UNIT_SECOND = Units.SECOND.getMillis();
    /**
     * Timer unit in milliseconds for a minute.
     */
    private final long TIMER_UNIT_MINUTE = Units.MINUTE.getMillis();
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

    @Override
    public void run() {
        final long timerUnit = this.scheduler.config.matchSecond ? TIMER_UNIT_SECOND : TIMER_UNIT_MINUTE;

        long thisTime = System.currentTimeMillis();
        long nextTime;
        long sleep;
        while (!isStop) {
            // The next execution time is calculated based on the start time of the previous execution point.
            // Dividing by the timer unit clears the lower parts (e.g., seconds and milliseconds if the unit is
            // minutes).
            nextTime = ((thisTime / timerUnit) + 1) * timerUnit;
            sleep = nextTime - System.currentTimeMillis();
            if (isValidSleepMillis(sleep, timerUnit)) {
                if (!ThreadKit.safeSleep(sleep)) {
                    // If interrupted while sleeping, exit the timer loop.
                    break;
                }

                // Record the execution time as the start of the tick, not the end.
                spawnLauncher(nextTime);

                // Use additive progression to ensure an interval of exactly one minute or one second,
                // avoiding issues with `sleep` waking up late.
                // No validation is needed here, as each loop sleeps for the difference between the next tick and the
                // current time.
                // If the previous wake-up was late, the current sleep time will be reduced, keeping the error within
                // one unit and continuously correcting.
                thisTime = nextTime;
            } else {
                // Recalculate time if it's not a normal interval.
                thisTime = System.currentTimeMillis();
            }
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
        this.scheduler.supervisor.spawnLauncher(millis);
    }

}
