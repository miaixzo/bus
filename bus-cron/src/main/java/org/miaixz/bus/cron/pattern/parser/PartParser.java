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
package org.miaixz.bus.cron.pattern.parser;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.en.Month;
import org.miaixz.bus.core.center.date.culture.en.Week;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.CrontabException;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.xyz.ListKit;
import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.cron.pattern.Part;
import org.miaixz.bus.cron.pattern.matcher.*;

/**
 * A parser for a single field of a cron expression. This class parses a string representation of a cron field (e.g.,
 * for minutes, hours) into a {@link PartMatcher} that can be used to check if a given time value matches the field's
 * constraints.
 * <p>
 * Each field supports the following formats:
 * <ul>
 * <li><strong>*</strong>: Matches all valid values for the field.</li>
 * <li><strong>?</strong>: Matches any value (same as '*').</li>
 * <li><strong>L</strong>: Represents the maximum allowed value for the field.</li>
 * <li><strong>*&#47;2</strong>: An interval (e.g., every 2 minutes).</li>
 * <li><strong>2-8</strong>: A continuous range (e.g., minutes 2 through 8).</li>
 * <li><strong>2,3,5,8</strong>: A list of specific values.</li>
 * <li><strong>wed</strong>: An alias for the day of the week.</li>
 * <li><strong>jan</strong>: An alias for the month.</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PartParser {

    private final Part part;

    /**
     * Constructs a new parser for a specific part of the cron expression.
     *
     * @param part The cron expression part to be parsed.
     */
    public PartParser(final Part part) {
        this.part = part;
    }

    /**
     * Creates a new parser for a specific part of the cron expression.
     *
     * @param part The cron expression part to be parsed.
     * @return A new {@link PartParser} instance.
     */
    public static PartParser of(final Part part) {
        return new PartParser(part);
    }

    /**
     * Checks if the given value is a wildcard character ('*' or '?').
     *
     * @param value The string value to check.
     * @return {@code true} if the value is a wildcard, {@code false} otherwise.
     */
    private static boolean isMatchAllString(final String value) {
        return (1 == value.length()) && (Symbol.STAR.equals(value) || "?".equals(value));
    }

    /**
     * Parses the given expression string into a {@link PartMatcher}.
     * <ul>
     * <li>"*" or "?" returns {@link AlwaysTrueMatcher}.</li>
     * <li>For {@link Part#DAY_OF_MONTH}, returns {@link DayOfMonthMatcher}.</li>
     * <li>For {@link Part#YEAR}, returns {@link YearValueMatcher}.</li>
     * <li>For all other parts, returns {@link BoolArrayMatcher}.</li>
     * </ul>
     *
     * @param value The expression string for the part.
     * @return A {@link PartMatcher} that can evaluate the expression.
     */
    public PartMatcher parse(final String value) {
        if (isMatchAllString(value)) {
            // In Quartz, '?' is used to prevent conflicts, but here it has the same effect as '*'.
            return new AlwaysTrueMatcher();
        }

        final List<Integer> values = parseArray(value);
        if (values.isEmpty()) {
            throw new CrontabException("Invalid part value: [{}]", value);
        }

        switch (this.part) {
            case DAY_OF_MONTH:
                return new DayOfMonthMatcher(values);

            case YEAR:
                return new YearValueMatcher(values);

            default:
                return new BoolArrayMatcher(values);
        }
    }

    /**
     * Parses a comma-separated list of values. e.g., "a,b,c,d" or "a"
     *
     * @param value The expression part string.
     * @return A list of integer values.
     */
    private List<Integer> parseArray(final String value) {
        final List<Integer> values = new ArrayList<>();
        final List<String> parts = CharsBacker.split(value, Symbol.COMMA);
        for (final String part : parts) {
            ListKit.addAllIfNotContains(values, parseStep(part));
        }
        return values;
    }

    /**
     * Parses an expression that may contain a step value.
     * <ul>
     * <li><strong>a</strong> 或 <strong>*</strong></li>
     * <li><strong>a&#47;b</strong> 或 <strong>*&#47;b</strong></li>
     * <li><strong>a-b/2</strong></li>
     * </ul>
     * 
     * @param value The expression part string.
     * @return A list of integer values.
     */
    private List<Integer> parseStep(final String value) {
        final List<String> parts = CharsBacker.split(value, Symbol.SLASH);
        final int size = parts.size();
        final List<Integer> results;

        if (size == 1) { // No step value
            results = parseRange(value, -1);
        } else if (size == 2) { // With step value
            final int step = parseNumber(parts.get(1), false); // Step value is not checked against min/max
            if (step < 1) {
                throw new CrontabException("Non positive divisor for field: [{}]", value);
            }
            results = parseRange(parts.get(0), step);
        } else {
            throw new CrontabException("Invalid syntax of field: [{}]", value);
        }
        return results;
    }

    /**
     * Parses a range expression. e.g., "*", "2", "3-8", "8-3", "3-3"
     *
     * @param value The range expression string.
     * @param step  The step value, or -1 if no step is specified.
     * @return A list of integer values.
     */
    private List<Integer> parseRange(final String value, int step) {
        final List<Integer> results = new ArrayList<>();

        // Handle wildcard and single-number cases
        if (value.length() <= 2) {
            int minValue = part.getMin();
            if (!isMatchAllString(value)) {
                minValue = Math.max(minValue, parseNumber(value, true));
            } else {
                // If it's a wildcard and no step is defined, default to a step of 1.
                if (step < 1) {
                    step = 1;
                }
            }
            if (step > 0) {
                final int maxValue = part.getMax();
                if (minValue > maxValue) {
                    throw new CrontabException("Invalid value {} > {}", minValue, maxValue);
                }
                // Generate values with step
                for (int i = minValue; i <= maxValue; i += step) {
                    results.add(i);
                }
            } else {
                // A single fixed value
                results.add(minValue);
            }
            return results;
        }

        // Handle range "a-b"
        final List<String> parts = CharsBacker.split(value, Symbol.MINUS);
        final int size = parts.size();
        if (size == 1) { // A single value, possibly with a step (e.g., "20/2")
            final int v1 = parseNumber(value, true);
            if (step > 0) {
                MathKit.appendRange(v1, part.getMax(), step, results);
            } else {
                results.add(v1);
            }
        } else if (size == 2) { // A range value
            final int v1 = parseNumber(parts.get(0), true);
            final int v2 = parseNumber(parts.get(1), true);
            if (step < 1) {
                // If no step is defined in a range, default to a step of 1.
                step = 1;
            }
            if (v1 <= v2) { // Normal range, e.g., 2-5
                MathKit.appendRange(v1, v2, step, results);
            } else { // Reversed range, e.g., 5-2 (means from 5 to max, and from min to 2)
                MathKit.appendRange(v1, part.getMax(), step, results);
                MathKit.appendRange(part.getMin(), v2, step, results);
            }
        } else {
            throw new CrontabException("Invalid syntax of field: [{}]", value);
        }
        return results;
    }

    /**
     * Parses a string into an integer, supporting aliases.
     *
     * @param value      The string to parse.
     * @param checkValue Whether to validate that the parsed value is within the part's allowed range.
     * @return The parsed integer.
     * @throws CrontabException if the value is not a valid number or alias.
     */
    private int parseNumber(final String value, final boolean checkValue) throws CrontabException {
        int i;
        try {
            i = Integer.parseInt(value);
        } catch (final NumberFormatException ignore) {
            i = parseAlias(value);
        }

        // Support negative numbers as offsets from the max value
        if (i < 0) {
            i += part.getMax();
        }

        // Normalize Sunday (7) to 0
        if (Part.DAY_OF_WEEK.equals(this.part) && Week.SUNDAY.getIsoValue() == i) {
            i = Week.SUNDAY.ordinal();
        }

        return checkValue ? part.checkValue(i) : i;
    }

    /**
     * Parses special aliases:
     * <ul>
     * <li>'L' for the maximum value of the field.</li>
     * <li>Month and Day-of-Week name aliases (e.g., "JAN", "SUN").</li>
     * </ul>
     *
     * @param name The alias string.
     * @return The parsed integer value.
     * @throws CrontabException if the alias is not recognized.
     */
    private int parseAlias(final String name) throws CrontabException {
        if ("L".equalsIgnoreCase(name)) {
            // 'L' represents the maximum value for the part.
            return part.getMax();
        }

        switch (this.part) {
            case MONTH:
                // Month is 1-based
                return Month.of(name).getIsoValue();

            case DAY_OF_WEEK:
                // Day of week is 0-based (0=Sunday)
                return Week.of(name).ordinal();
        }

        throw new CrontabException("Invalid alias value: [{}]", name);
    }

}
