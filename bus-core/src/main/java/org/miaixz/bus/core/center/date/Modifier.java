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
package org.miaixz.bus.core.center.date;

import org.miaixz.bus.core.center.date.culture.en.Modify;
import org.miaixz.bus.core.xyz.ArrayKit;

/**
 * Date modifier for adjusting specific date fields. This class provides methods to:
 *
 * <pre>
 * 1. Get the start time of a specified field.
 * 2. Get the rounded time of a specified field.
 * 3. Get the end time of a specified field.
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Modifier {

    /**
     * Fields to be ignored during calculation.
     */
    private static final int[] IGNORE_FIELDS = new int[] { java.util.Calendar.HOUR_OF_DAY, // Same as HOUR
            java.util.Calendar.AM_PM, // This field is handled separately and does not participate in start/end
                                      // calculations
            java.util.Calendar.DAY_OF_WEEK_IN_MONTH, // Not involved in calculation
            java.util.Calendar.DAY_OF_YEAR, // Represented by DAY_OF_MONTH
            java.util.Calendar.WEEK_OF_MONTH, // Special handling
            java.util.Calendar.WEEK_OF_YEAR // Represented by WEEK_OF_MONTH
    };

    /**
     * Modifies the date based on the specified field and modification type.
     *
     * @param calendar  The {@link java.util.Calendar} object to modify.
     * @param dateField The date field to retain, e.g., {@link java.util.Calendar#DAY_OF_MONTH}.
     * @param modify    The modification type, including truncate, round, and ceiling.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar modify(
            final java.util.Calendar calendar,
            final int dateField,
            final Modify modify) {
        return modify(calendar, dateField, modify, false);
    }

    /**
     * Modifies the date, taking the start or end value. Optionally, milliseconds can be truncated to zero.
     *
     * <p>
     * In {@link Modify#TRUNCATE} mode, milliseconds are always truncated to zero. However, in {@link Modify#CEILING}
     * and {@link Modify#ROUND} modes, sometimes the millisecond part must be zero (e.g., in MySQL databases), so this
     * option is added.
     * 
     *
     * @param calendar  The {@link java.util.Calendar} object to modify.
     * @param dateField The date field to retain, e.g., {@link java.util.Calendar#DAY_OF_MONTH}.
     * @param modify    The modification type, including truncate, round, and ceiling.
     * @param truncate  {@code true} to truncate milliseconds to zero, {@code false} otherwise.
     * @return The modified {@link java.util.Calendar} object.
     */
    public static java.util.Calendar modify(
            final java.util.Calendar calendar,
            final int dateField,
            final Modify modify,
            final boolean truncate) {
        // Special handling for AM_PM
        if (java.util.Calendar.AM_PM == dateField) {
            final boolean isAM = Calendar.isAM(calendar);
            switch (modify) {
                case TRUNCATE:
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, isAM ? 0 : 12);
                    break;

                case CEILING:
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, isAM ? 11 : 23);
                    break;

                case ROUND:
                    final int min = isAM ? 0 : 12;
                    final int max = isAM ? 11 : 23;
                    final int href = min + (max - min) / 2 + 1;
                    final int value = calendar.get(java.util.Calendar.HOUR_OF_DAY);
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, (value < href) ? min : max);
                    break;
            }
            // Process the next level field
            return modify(calendar, dateField + 1, modify, truncate);
        }

        final int endField = truncate ? java.util.Calendar.SECOND : java.util.Calendar.MILLISECOND;
        // Loop through and process each field level, accurate to milliseconds
        for (int i = dateField + 1; i <= endField; i++) {
            if (ArrayKit.contains(IGNORE_FIELDS, i)) {
                // Ignore irrelevant fields (WEEK_OF_MONTH) and do not modify them
                continue;
            }

            // When calculating the start and end days of the week, month-related fields are ignored.
            if (java.util.Calendar.WEEK_OF_MONTH == dateField || java.util.Calendar.WEEK_OF_YEAR == dateField) {
                if (java.util.Calendar.DAY_OF_MONTH == i) {
                    continue;
                }
            } else {
                // In other cases, ignore week-related field calculations
                if (java.util.Calendar.DAY_OF_WEEK == i) {
                    continue;
                }
            }

            modifyField(calendar, i, modify);
        }

        if (truncate) {
            calendar.set(java.util.Calendar.MILLISECOND, 0);
        }

        return calendar;
    }

    /**
     * Modifies the value of a specific date field.
     *
     * @param calendar The {@link java.util.Calendar} object.
     * @param field    The field to modify, see {@link java.util.Calendar}.
     * @param modify   The {@link Modify} type.
     */
    private static void modifyField(final java.util.Calendar calendar, int field, final Modify modify) {
        if (java.util.Calendar.HOUR == field) {
            // Correct hour. HOUR is 12-hour format, the end time of AM is 12:00, here changed to HOUR_OF_DAY: 23:59
            field = java.util.Calendar.HOUR_OF_DAY;
        }

        switch (modify) {
            case TRUNCATE:
                calendar.set(field, Calendar.getBeginValue(calendar, field));
                break;

            case CEILING:
                calendar.set(field, Calendar.getEndValue(calendar, field));
                break;

            case ROUND:
                final int min = Calendar.getBeginValue(calendar, field);
                final int max = Calendar.getEndValue(calendar, field);
                final int href;
                if (java.util.Calendar.DAY_OF_WEEK == field) {
                    // Special handling for week, assuming Monday is the first day, the middle is Thursday
                    href = (min + 3) % 7;
                } else {
                    href = (max - min) / 2 + 1;
                }
                final int value = calendar.get(field);
                calendar.set(field, (value < href) ? min : max);
                break;
        }
    }

}
