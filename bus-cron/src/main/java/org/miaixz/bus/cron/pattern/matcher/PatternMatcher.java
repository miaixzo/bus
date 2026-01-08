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
package org.miaixz.bus.cron.pattern.matcher;

import org.miaixz.bus.core.xyz.DateKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.cron.pattern.Part;

import java.time.Year;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * A matcher for a single cron expression. A {@code PatternMatcher} consists of seven {@link PartMatcher} instances,
 * each representing one of the seven fields in a cron expression:
 * 
 * <pre>
 *    0      1     2        3         4       5        6
 * SECOND MINUTE HOUR DAY_OF_MONTH MONTH DAY_OF_WEEK YEAR
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PatternMatcher {

    private final PartMatcher[] matchers;

    /**
     * Constructs a new PatternMatcher.
     *
     * @param secondMatcher     The matcher for the second field.
     * @param minuteMatcher     The matcher for the minute field.
     * @param hourMatcher       The matcher for the hour field.
     * @param dayOfMonthMatcher The matcher for the day-of-month field.
     * @param monthMatcher      The matcher for the month field.
     * @param dayOfWeekMatcher  The matcher for the day-of-week field.
     * @param yearMatcher       The matcher for the year field.
     */
    public PatternMatcher(final PartMatcher secondMatcher, final PartMatcher minuteMatcher,
            final PartMatcher hourMatcher, final PartMatcher dayOfMonthMatcher, final PartMatcher monthMatcher,
            final PartMatcher dayOfWeekMatcher, final PartMatcher yearMatcher) {

        matchers = new PartMatcher[] { secondMatcher, minuteMatcher, hourMatcher, dayOfMonthMatcher, monthMatcher,
                dayOfWeekMatcher, yearMatcher };
    }

    /**
     * Checks if the day-of-month field matches, with special handling for 'L' (last day).
     *
     * @param matcher    The {@link PartMatcher} for the day-of-month.
     * @param dayOfMonth The day of the month to check.
     * @param month      The month (1-12).
     * @param isLeapYear Whether the year is a leap year.
     * @return {@code true} if it matches, {@code false} otherwise.
     */
    private static boolean matchDayOfMonth(
            final PartMatcher matcher,
            final int dayOfMonth,
            final int month,
            final boolean isLeapYear) {
        return ((matcher instanceof DayOfMonthMatcher)
                ? ((DayOfMonthMatcher) matcher).match(dayOfMonth, month, isLeapYear)
                : matcher.test(dayOfMonth));
    }

    /**
     * Gets the {@link PartMatcher} for the specified cron expression part.
     *
     * @param part The cron expression part.
     * @return The corresponding {@link PartMatcher}.
     */
    public PartMatcher get(final Part part) {
        return matchers[part.ordinal()];
    }

    /**
     * Checks if a given set of time fields matches this cron pattern.
     *
     * @param fields An array of time fields: {second, minute, hour, dayOfMonth, month, dayOfWeek, year}.
     * @return {@code true} if it matches, {@code false} otherwise.
     */
    public boolean match(final int[] fields) {
        return match(fields[0], fields[1], fields[2], fields[3], fields[4], fields[5], fields[6]);
    }

    /**
     * Checks if a given day-of-week value matches the corresponding part of the cron expression.
     *
     * @param dayOfWeekValue The day-of-week value (0=Sunday, 7=Sunday).
     * @return {@code true} if it matches, {@code false} otherwise.
     */
    public boolean matchWeek(final int dayOfWeekValue) {
        return get(Part.DAY_OF_WEEK).test(dayOfWeekValue);
    }

    /**
     * Checks if the given time components match this cron pattern.
     *
     * @param second     The second (or -1 to ignore).
     * @param minute     The minute.
     * @param hour       The hour.
     * @param dayOfMonth The day of the month.
     * @param month      The month (1-based).
     * @param dayOfWeek  The day of the week (0-based, 0 or 7 is Sunday).
     * @param year       The year.
     * @return {@code true} if it matches, {@code false} otherwise.
     */
    private boolean match(
            final int second,
            final int minute,
            final int hour,
            final int dayOfMonth,
            final int month,
            final int dayOfWeek,
            final int year) {
        return ((second < 0) || get(Part.SECOND).test(second)) // Match second (always true if second matching is off)
                && get(Part.MINUTE).test(minute) // Match minute
                && get(Part.HOUR).test(hour) // Match hour
                && matchDayOfMonth(get(Part.DAY_OF_MONTH), dayOfMonth, month, Year.isLeap(year)) // Match day of month
                && get(Part.MONTH).test(month) // Match month
                && get(Part.DAY_OF_WEEK).test(dayOfWeek) // Match day of week
                && get(Part.YEAR).test(year); // Match year
    }

    /**
     * Calculates the next matching date and time. The algorithm starts from the highest field (year) and works its way
     * down:
     * <ul>
     * <li>If a field's next value is the same, move to the next lower field.</li>
     * <li>If a field's next value is greater than the current value, reset all lower fields to their minimums.</li>
     * <li>If a field's next value is less than the current value (a rollover), go back to the previous higher field and
     * get its next value.</li>
     * </ul>
     * 
     * <pre>
     *        SECOND MINUTE HOUR DAY_OF_MONTH MONTH DAY_OF_WEEK YEAR
     *     Lower &lt;------------------------------------------&gt; Higher
     * </pre>
     *
     * @param values The current time fields: {second, minute, hour, dayOfMonth, month, dayOfWeek, year}. This array
     *               will be modified.
     * @param zone   The time zone.
     * @return A {@link Calendar} instance for the next match, with milliseconds set to 0.
     */
    public Calendar nextMatchAfter(final int[] values, final TimeZone zone) {
        final Calendar calendar = Calendar.getInstance(zone);
        calendar.set(Calendar.MILLISECOND, 0);

        final int[] newValues = nextMatchValuesAfter(values);
        for (int i = 0; i < newValues.length; i++) {
            // Day of week is not set directly, it's a result of the other fields.
            if (i != Part.DAY_OF_WEEK.ordinal()) {
                setValue(calendar, Part.of(i), newValues[i]);
            }
        }

        return calendar;
    }

    /**
     * Calculates the next matching time values. The algorithm starts from the highest field (year) and works its way
     * down:
     * <ul>
     * <li>If a field's next value is the same, move to the next lower field.</li>
     * <li>If a field's next value is greater than the current value, reset all lower fields to their minimums.</li>
     * <li>If a field's next value is less than the current value (a rollover), go back to the previous higher field and
     * get its next value.</li>
     * </ul>
     *
     * @param values The current time fields: {second, minute, hour, dayOfMonth, month, dayOfWeek, year}.
     * @return The array of next matching time values.
     */
    private int[] nextMatchValuesAfter(final int[] values) {
        int i = Part.YEAR.ordinal();
        int nextValue = 0;

        // Find the first field from right to left that needs to be changed.
        while (i >= 0) {
            if (i == Part.DAY_OF_WEEK.ordinal()) {
                // Day of week is not part of the calculation.
                i--;
                continue;
            }

            nextValue = getNextMatch(values, i, 0);

            if (nextValue > values[i]) {
                // This field has a new value, so we can stop and reset lower fields.
                values[i] = nextValue;
                i--;
                break;
            } else if (nextValue < values[i]) {
                // The next value for this field has rolled over, so we need to increment the next higher field.
                i++;
                nextValue = -1; // Mark as needing to backtrack.
                break;
            }

            // The value for this field is unchanged, so check the next lower field.
            i--;
        }

        // If we need to backtrack, find the next field to increment.
        if (-1 == nextValue) {
            while (i <= Part.YEAR.ordinal()) {
                if (i == Part.DAY_OF_WEEK.ordinal()) {
                    i++;
                    continue;
                }

                nextValue = getNextMatch(values, i, 1);

                if (nextValue > values[i]) {
                    values[i] = nextValue;
                    i--;
                    break;
                }
                i++;
            }
        }

        // Reset all lower fields to their minimum valid values.
        setToMin(values, i);
        return values;
    }

    /**
     * Gets the next matching value for a specific part. The result can be:
     * <ul>
     * <li>Greater than the original value: The part is updated, and lower parts will be reset to their minimums.</li>
     * <li>Less than the original value: The part has rolled over to its minimum, and the next higher part needs to be
     * incremented.</li>
     * <li>Equal to the original value: The part matches, and the next lower part should be checked.</li>
     * </ul>
     *
     * @param newValues   The time fields array.
     * @param partOrdinal The ordinal of the part to check.
     * @param plusValue   The value to add to the current field value before finding the next match (0 or 1).
     * @return The next matching value.
     */
    private int getNextMatch(final int[] newValues, final int partOrdinal, final int plusValue) {
        final PartMatcher matcher = get(Part.of(partOrdinal));
        final int value = newValues[partOrdinal] + plusValue;

        if (matcher instanceof DayOfMonthMatcher) {
            final boolean isLeapYear = DateKit.isLeapYear(newValues[Part.YEAR.ordinal()]);
            final int month = newValues[Part.MONTH.ordinal()];
            return ((DayOfMonthMatcher) matcher).nextAfter(value, month, isLeapYear);
        }

        return matcher.nextAfter(value);
    }

    /**
     * Sets all fields from SECOND up to (and including) the specified part to their minimum valid values.
     *
     * @param values The values array to modify.
     * @param toPart The ordinal of the highest part to reset.
     */
    private void setToMin(final int[] values, final int toPart) {
        for (int i = toPart; i >= 0; i--) {
            final Part part = Part.of(i);
            if (part == Part.DAY_OF_MONTH) {
                // Special handling for day of month to get the correct minimum.
                final boolean isLeapYear = DateKit.isLeapYear(values[Part.YEAR.ordinal()]);
                final int month = values[Part.MONTH.ordinal()];
                final PartMatcher partMatcher = get(part);
                if (partMatcher instanceof DayOfMonthMatcher) {
                    values[i] = ((DayOfMonthMatcher) partMatcher).getMinValue(month, isLeapYear);
                    continue;
                }
            }

            values[i] = getMin(part);
        }
    }

    /**
     * Gets the minimum value for a given part based on its matcher.
     *
     * @param part The {@link Part}.
     * @return The minimum value.
     */
    private int getMin(final Part part) {
        final PartMatcher matcher = get(part);

        if (matcher instanceof AlwaysTrueMatcher) {
            // If it matches all, the minimum is the field's absolute minimum.
            return part.getMin();
        } else if (matcher instanceof BoolArrayMatcher) {
            // Get the minimum value defined by the user in the expression.
            return ((BoolArrayMatcher) matcher).getMinValue();
        } else {
            // Should not happen with current matcher types.
            throw new IllegalArgumentException("Invalid matcher: " + matcher.getClass().getName());
        }
    }

    /**
     * Sets a value on a {@link Calendar} instance, applying necessary adjustments.
     * <ul>
     * <li>Months are 1-based in cron expressions but 0-based in {@link Calendar}, so they are decremented.</li>
     * <li>Days of the week are 0-based in expressions (0=Sunday) but 1-based in {@link Calendar} (1=Sunday), so they
     * are incremented.</li>
     * </ul>
     *
     * @param calendar The {@link Calendar} to modify.
     * @param part     The cron part to set.
     * @param value    The value to set.
     * @return The modified {@link Calendar}.
     */
    private Calendar setValue(final Calendar calendar, final Part part, int value) {
        switch (part) {
            case MONTH:
                value -= 1; // Adjust for 0-based Calendar month
                break;

            case DAY_OF_WEEK:
                value += 1; // Adjust for 1-based Calendar day of week
                break;
        }
        calendar.set(part.getCalendarField(), value);
        return calendar;
    }

    @Override
    public String toString() {
        return StringKit.format("""
                	SECOND      : {}
                	MINUTE      : {}
                	HOUR        : {}
                	DAY_OF_MONTH: {}
                	MONTH       : {}
                	DAY_OF_WEEK : {}
                	YEAR        : {}
                """, (Object[]) this.matchers);
    }

}
