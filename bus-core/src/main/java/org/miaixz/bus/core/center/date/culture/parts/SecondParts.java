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
 * @since Java 17+
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
