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
package org.miaixz.bus.core.center.date;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import org.miaixz.bus.core.center.date.format.FormatPeriod;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.DateKit;

/**
 * Represents the interval between two dates.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Between implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852233282300L;

    /**
     * The beginning date of the interval.
     */
    private final long begin;
    /**
     * The ending date of the interval.
     */
    private final long end;

    /**
     * Constructs a {@code Between} object. The earlier date is set as the beginning time, and the later date as the
     * ending time. The interval always retains a positive absolute value.
     *
     * @param begin The starting date.
     * @param end   The ending date.
     */
    public Between(final Date begin, final Date end) {
        this(begin, end, true);
    }

    /**
     * Constructs a {@code Between} object.
     *
     * @param begin The starting date.
     * @param end   The ending date.
     * @param isAbs If {@code true}, the date interval will only retain a positive absolute value (swapping begin and
     *              end if begin > end).
     */
    public Between(final Date begin, final Date end, final boolean isAbs) {
        Assert.notNull(begin, "Begin date is null !");
        Assert.notNull(end, "End date is null !");

        if (isAbs && begin.after(end)) {
            // If the interval should be positive, and the begin date is after the end date, swap them.
            this.begin = end.getTime();
            this.end = begin.getTime();
        } else {
            this.begin = begin.getTime();
            this.end = end.getTime();
        }
    }

    /**
     * Calculates the duration between the two dates held by this object in the specified {@link Chrono}.
     *
     * @param chrono The chronological unit for measuring the time difference (e.g., {@link Chrono#MILLISECOND},
     *               {@link Chrono#SECOND}, {@link Chrono#MINUTE}, {@link Chrono#HOUR}, {@link Chrono#DAY}).
     * @return The duration difference in the specified unit.
     */
    public long between(final Chrono chrono) {
        final long diff = end - begin;
        return diff / chrono.getMillis();
    }

    /**
     * Calculates the number of months between the two dates. If {@code isReset} is {@code false} and the day of the
     * beginning date is greater than the day of the ending date, the month count will be decremented by one (indicating
     * less than a full month).
     *
     * @param isReset Whether to reset the time of the dates to the beginning of the day (resetting day, hour, minute,
     *                second).
     * @return The number of months between the dates.
     */
    public long betweenMonth(final boolean isReset) {
        final java.util.Calendar beginCal = Calendar.calendar(begin);
        final java.util.Calendar endCal = Calendar.calendar(end);

        final int betweenYear = endCal.get(java.util.Calendar.YEAR) - beginCal.get(java.util.Calendar.YEAR);
        final int betweenMonthOfYear = endCal.get(java.util.Calendar.MONTH) - beginCal.get(java.util.Calendar.MONTH);

        final int result = betweenYear * 12 + betweenMonthOfYear;
        if (!isReset) {
            endCal.set(java.util.Calendar.YEAR, beginCal.get(java.util.Calendar.YEAR));
            endCal.set(java.util.Calendar.MONTH, beginCal.get(java.util.Calendar.MONTH));
            final long between = endCal.getTimeInMillis() - beginCal.getTimeInMillis();
            if (between < 0) {
                return result - 1;
            }
        }
        return result;
    }

    /**
     * Calculates the number of years between the two dates. If {@code isReset} is {@code false} and the month of the
     * beginning date is greater than the month of the ending date, the year count will be decremented by one
     * (indicating less than a full year).
     *
     * @param isReset Whether to reset the time of the dates to the beginning of the year (resetting month, day, hour,
     *                minute, second).
     * @return The number of years between the dates.
     */
    public long betweenYear(final boolean isReset) {
        final java.util.Calendar beginCal = Calendar.calendar(begin);
        final java.util.Calendar endCal = Calendar.calendar(end);

        final int result = endCal.get(java.util.Calendar.YEAR) - beginCal.get(java.util.Calendar.YEAR);
        if (isReset) {
            return result;
        }
        final int beginMonthBase0 = beginCal.get(java.util.Calendar.MONTH);
        final int endMonthBase0 = endCal.get(java.util.Calendar.MONTH);
        if (beginMonthBase0 < endMonthBase0) {
            return result;
        } else if (beginMonthBase0 > endMonthBase0) {
            return result - 1;
        } else if (java.util.Calendar.FEBRUARY == beginMonthBase0 && Calendar.isLastDayOfMonth(beginCal)
                && Calendar.isLastDayOfMonth(endCal)) {
            // Handle February leap year case.
            // If both dates are on the last day of February, treat months as equal. Both are set to the 1st.
            beginCal.set(java.util.Calendar.DAY_OF_MONTH, 1);
            endCal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        }

        endCal.set(java.util.Calendar.YEAR, beginCal.get(java.util.Calendar.YEAR));
        final long between = endCal.getTimeInMillis() - beginCal.getTimeInMillis();
        return between < 0 ? result - 1 : result;
    }

    /**
     * Retrieves the beginning date.
     *
     * @return The beginning date.
     */
    public Date getBeginDate() {
        return DateKit.date(begin);
    }

    /**
     * Retrieves the ending date.
     *
     * @return The ending date.
     */
    public Date getEndDate() {
        return DateKit.date(end);
    }

    /**
     * Formats and outputs the time difference as a string.
     *
     * @param chrono The chronological unit for calculating and formatting the difference (e.g.,
     *               {@link Chrono#MILLISECOND}, {@link Chrono#SECOND}, {@link Chrono#MINUTE}, {@link Chrono#HOUR},
     *               {@link Chrono#DAY}).
     * @return The formatted time difference string.
     */
    public String toString(final Chrono chrono) {
        return FormatPeriod.of(between(chrono), chrono).format();
    }

    /**
     * Returns a string representation of the time difference, formatted with millisecond level detail.
     *
     * @return A string representation of the object.
     */
    @Override
    public String toString() {
        return toString(Chrono.MILLISECOND);
    }

}
