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
package org.miaixz.bus.cron.pattern;

import org.miaixz.bus.core.center.date.Month;
import org.miaixz.bus.core.center.date.Week;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.CrontabException;

import java.util.Calendar;

/**
 * Enumeration of the parts of a cron expression. This enum defines the position and valid value range for each field in
 * a cron expression. The {@link #ordinal()} of each constant represents its position in the expression.
 * <p>
 * The order of the parts is as follows:
 * 
 * <pre>
 *         0       1    2        3         4       5         6
 *     [SECOND] MINUTE HOUR DAY_OF_MONTH MONTH DAY_OF_WEEK [YEAR]
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Part {

    /**
     * The second part of the expression (0-59).
     */
    SECOND(Calendar.SECOND, 0, 59),
    /**
     * The minute part of the expression (0-59).
     */
    MINUTE(Calendar.MINUTE, 0, 59),
    /**
     * The hour part of the expression (0-23).
     */
    HOUR(Calendar.HOUR_OF_DAY, 0, 23),
    /**
     * The day of the month part of the expression (1-31). Note: 32 is used internally to represent the last day of the
     * month.
     */
    DAY_OF_MONTH(Calendar.DAY_OF_MONTH, 1, 32),
    /**
     * The month part of the expression (1-12).
     */
    MONTH(Calendar.MONTH, Month.JANUARY.getIsoValue(), Month.DECEMBER.getIsoValue()),
    /**
     * The day of the week part of the expression (0-6, where Sunday is 0).
     */
    DAY_OF_WEEK(Calendar.DAY_OF_WEEK, Week.SUNDAY.ordinal(), Week.SATURDAY.ordinal()),
    /**
     * The optional year part of the expression (1970-2099).
     */
    YEAR(Calendar.YEAR, 1970, 2099);

    /**
     * Array of all Part enum constants for efficient iteration.
     */
    private static final Part[] ENUMS = Part.values();

    /**
     * The corresponding {@link Calendar} field constant for this part.
     */
    private final int calendarField;
    /**
     * The minimum allowed value for this part (inclusive).
     */
    private final int min;
    /**
     * The maximum allowed value for this part (inclusive).
     */
    private final int max;

    /**
     * Constructs a new Part.
     *
     * @param calendarField The corresponding field constant from {@link Calendar}.
     * @param min           The minimum allowed value (inclusive).
     * @param max           The maximum allowed value (inclusive).
     */
    Part(final int calendarField, final int min, final int max) {
        this.calendarField = calendarField;
        if (min > max) {
            this.min = max;
            this.max = min;
        } else {
            this.min = min;
            this.max = max;
        }
    }

    /**
     * Gets the {@code Part} for the given position index.
     *
     * @param i The position index (0-based).
     * @return The corresponding {@code Part}.
     */
    public static Part of(final int i) {
        return ENUMS[i];
    }

    /**
     * Gets the corresponding {@link Calendar} field constant.
     *
     * @return The {@link Calendar} field constant.
     */
    public int getCalendarField() {
        return this.calendarField;
    }

    /**
     * Gets the minimum allowed value for this part.
     *
     * @return The minimum value.
     */
    public int getMin() {
        return this.min;
    }

    /**
     * Gets the maximum allowed value for this part.
     *
     * @return The maximum value.
     */
    public int getMax() {
        return this.max;
    }

    /**
     * Checks if a given value is within the valid range for this part.
     *
     * @param value The value to check.
     * @return The validated value.
     * @throws CrontabException if the value is out of range.
     */
    public int checkValue(final int value) throws CrontabException {
        Assert.checkBetween(
                value,
                min,
                max,
                () -> new CrontabException("{} value {} out of range: [{} , {}]", this.name(), value, min, max));
        return value;
    }

}
