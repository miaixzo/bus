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
package org.miaixz.bus.core.center.date.culture.parts;

/**
 * Abstract base class for date-time components containing time information.
 *
 * <p>
 * This class extends {@link DayParts} and adds hour, minute, and second fields, representing time within a day. It
 * provides the highest precision among the part classes, suitable for implementations that require second-level
 * accuracy.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class SecondParts extends DayParts {

    /**
     * The hour of the day (0-23).
     */
    protected int hour;

    /**
     * The minute of the hour (0-59).
     */
    protected int minute;

    /**
     * The second of the minute (0-59).
     */
    protected int second;

    /**
     * Validates the time components.
     *
     * @param hour   the hour to validate (0-23)
     * @param minute the minute to validate (0-59)
     * @param second the second to validate (0-59)
     * @throws IllegalArgumentException if hour, minute, or second is out of valid range
     */
    public static void validate(int hour, int minute, int second) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException(String.format("illegal hour: %d", hour));
        }
        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException(String.format("illegal minute: %d", minute));
        }
        if (second < 0 || second > 59) {
            throw new IllegalArgumentException(String.format("illegal second: %d", second));
        }
    }

    /**
     * Gets the hour of the day.
     *
     * @return the hour (0-23)
     */
    public int getHour() {
        return hour;
    }

    /**
     * Gets the minute of the hour.
     *
     * @return the minute (0-59)
     */
    public int getMinute() {
        return minute;
    }

    /**
     * Gets the second of the minute.
     *
     * @return the second (0-59)
     */
    public int getSecond() {
        return second;
    }

}
