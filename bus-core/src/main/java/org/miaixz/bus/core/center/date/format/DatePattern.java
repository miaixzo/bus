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
package org.miaixz.bus.core.center.date.format;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.DateException;

/**
 * A class for parsing and formatting date-time strings based on a pattern. This class is the core of the
 * {@link org.miaixz.bus.core.center.date.printer.FastDatePrinter} and handles the conversion of a format pattern into a
 * series of formatting rules.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DatePattern {

    /**
     * A cache for timezone display names to improve performance.
     */
    private static final ConcurrentMap<TimeZoneDisplayKey, String> C_TIME_ZONE_DISPLAY_CACHE = new ConcurrentHashMap<>(
            7);
    /**
     * The list of formatting rules derived from the pattern.
     */
    private final Rule[] rules;
    /**
     * An estimated length of the formatted string.
     */
    private int estimateLength;

    /**
     * Constructs a new {@code DatePattern}.
     *
     * @param pattern  The date format pattern string.
     * @param locale   The locale.
     * @param timeZone The timezone.
     */
    public DatePattern(final String pattern, final Locale locale, final TimeZone timeZone) {
        this.rules = parsePattern(pattern, locale, timeZone).toArray(new Rule[0]);
    }

    /**
     * Parses a token (a sequence of the same character or a literal) from the format pattern.
     *
     * @param pattern  The format pattern string.
     * @param indexRef An array containing the current parsing index.
     * @return The parsed token.
     */
    protected static String parseToken(final String pattern, final int[] indexRef) {
        final StringBuilder buf = new StringBuilder();
        int i = indexRef[0];
        final int length = pattern.length();
        char c = pattern.charAt(i);

        if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
            buf.append(c);
            while (i + 1 < length && pattern.charAt(i + 1) == c) {
                buf.append(c);
                i++;
            }
        } else {
            buf.append('\'');
            boolean inLiteral = false;
            for (; i < length; i++) {
                c = pattern.charAt(i);
                if (c == '\'') {
                    if (i + 1 < length && pattern.charAt(i + 1) == '\'') {
                        i++;
                        buf.append(c);
                    } else {
                        inLiteral = !inLiteral;
                    }
                } else if (!inLiteral && ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z'))) {
                    i--;
                    break;
                } else {
                    buf.append(c);
                }
            }
        }

        indexRef[0] = i;
        return buf.toString();
    }

    /**
     * Selects the appropriate {@code NumberRule} based on the field and padding length.
     *
     * @param field   The calendar field.
     * @param padding The padding length.
     * @return The corresponding number rule.
     */
    protected static NumberRule selectNumberRule(final int field, final int padding) {
        return switch (padding) {
            case 1 -> new UnpaddedNumberField(field);
            case 2 -> new TwoDigitNumberField(field);
            default -> new PaddedNumberField(field, padding);
        };
    }

    /**
     * Appends a two-digit number to the buffer.
     *
     * @param buffer The output buffer.
     * @param value  The number to append.
     * @throws IOException if an I/O error occurs.
     */
    private static void appendDigits(final Appendable buffer, final int value) throws IOException {
        buffer.append((char) (value / 10 + '0'));
        buffer.append((char) (value % 10 + '0'));
    }

    /**
     * Appends a number to the buffer, padding with leading zeros to meet the minimum field width.
     *
     * @param buffer        The output buffer.
     * @param value         The number to append.
     * @param minFieldWidth The minimum field width.
     * @throws IOException if an I/O error occurs.
     */
    private static void appendFullDigits(final Appendable buffer, int value, int minFieldWidth) throws IOException {
        if (value < 10000) {
            int nDigits = 4;
            if (value < 1000) {
                --nDigits;
                if (value < 100) {
                    --nDigits;
                    if (value < 10) {
                        --nDigits;
                    }
                }
            }
            for (int i = minFieldWidth - nDigits; i > 0; --i) {
                buffer.append('0');
            }
            switch (nDigits) {
                case 4 -> {
                    buffer.append((char) (value / 1000 + '0'));
                    value %= 1000;
                }
                case 3 -> {
                    if (value >= 100) {
                        buffer.append((char) (value / 100 + '0'));
                        value %= 100;
                    } else {
                        buffer.append('0');
                    }
                }
                case 2 -> {
                    if (value >= 10) {
                        buffer.append((char) (value / 10 + '0'));
                        value %= 10;
                    } else {
                        buffer.append('0');
                    }
                }
                case 1 -> buffer.append((char) (value + '0'));
            }
        } else {
            char[] work = new char[Normal._10];
            int digit = 0;
            while (value != 0) {
                work[digit++] = (char) (value % 10 + '0');
                value = value / 10;
            }
            while (digit < minFieldWidth) {
                buffer.append('0');
                --minFieldWidth;
            }
            while (--digit >= 0) {
                buffer.append(work[digit]);
            }
        }
    }

    /**
     * Gets the display name for a timezone, using a cache to improve performance.
     *
     * @param tz       The timezone.
     * @param daylight Whether it is daylight saving time.
     * @param style    The display style (TimeZone.LONG or TimeZone.SHORT).
     * @param locale   The locale.
     * @return The display name of the timezone.
     */
    static String getTimeZoneDisplay(final TimeZone tz, final boolean daylight, final int style, final Locale locale) {
        final TimeZoneDisplayKey key = new TimeZoneDisplayKey(tz, daylight, style, locale);
        String value = C_TIME_ZONE_DISPLAY_CACHE.get(key);
        if (value == null) {
            value = tz.getDisplayName(daylight, style, locale);
            final String prior = C_TIME_ZONE_DISPLAY_CACHE.putIfAbsent(key, value);
            if (prior != null) {
                value = prior;
            }
        }
        return value;
    }

    /**
     * Gets an estimate of the maximum length of the formatted date string.
     *
     * @return The estimated length.
     */
    public int getEstimateLength() {
        return this.estimateLength;
    }

    /**
     * Applies the stored formatting rules to the given {@link Calendar} instance, appending the result to the buffer.
     *
     * @param calendar The calendar object to format.
     * @param buf      The output buffer.
     * @param <B>      The type of the Appendable (e.g., StringBuilder).
     * @return The buffer with the formatted date.
     * @throws DateException if an I/O error occurs.
     */
    public <B extends Appendable> B applyRules(final Calendar calendar, final B buf) {
        try {
            for (final Rule rule : this.rules) {
                rule.appendTo(buf, calendar);
            }
        } catch (final IOException e) {
            throw new DateException(e);
        }
        return buf;
    }

    /**
     * Parses a date format pattern string into a list of formatting rules.
     *
     * @param patternStr The date format pattern string.
     * @param locale     The locale.
     * @param timeZone   The timezone.
     * @return A list of formatting rules.
     * @throws IllegalArgumentException if the pattern is invalid.
     */
    private List<Rule> parsePattern(final String patternStr, final Locale locale, final TimeZone timeZone) {
        final DateFormatSymbols symbols = new DateFormatSymbols(locale);
        final List<Rule> rules = new ArrayList<>();

        final String[] ERAs = symbols.getEras();
        final String[] months = symbols.getMonths();
        final String[] shortMonths = symbols.getShortMonths();
        final String[] weekdays = symbols.getWeekdays();
        final String[] shortWeekdays = symbols.getShortWeekdays();
        final String[] AmPmStrings = symbols.getAmPmStrings();

        final int length = patternStr.length();
        final int[] indexRef = new int[1];

        for (int i = 0; i < length; i++) {
            indexRef[0] = i;
            final String token = parseToken(patternStr, indexRef);
            i = indexRef[0];

            final int tokenLen = token.length();
            if (tokenLen == 0) {
                break;
            }

            Rule rule;
            final char c = token.charAt(0);

            switch (c) {
                case 'G' -> rule = new TextField(Calendar.ERA, ERAs);
                case 'y', 'Y' -> {
                    if (tokenLen == 2) {
                        rule = TwoDigitYearField.INSTANCE;
                    } else {
                        rule = selectNumberRule(Calendar.YEAR, Math.max(tokenLen, 4));
                    }
                    if (c == 'Y') {
                        rule = new WeekYear((NumberRule) rule);
                    }
                }
                case 'M' -> {
                    if (tokenLen >= 4) {
                        rule = new TextField(Calendar.MONTH, months);
                    } else if (tokenLen == 3) {
                        rule = new TextField(Calendar.MONTH, shortMonths);
                    } else if (tokenLen == 2) {
                        rule = TwoDigitMonthField.INSTANCE;
                    } else {
                        rule = UnpaddedMonthField.INSTANCE;
                    }
                }
                case 'd' -> rule = selectNumberRule(Calendar.DAY_OF_MONTH, tokenLen);
                case 'h' -> rule = new TwelveHourField(selectNumberRule(Calendar.HOUR, tokenLen));
                case 'H' -> rule = selectNumberRule(Calendar.HOUR_OF_DAY, tokenLen);
                case 'm' -> rule = selectNumberRule(Calendar.MINUTE, tokenLen);
                case 's' -> rule = selectNumberRule(Calendar.SECOND, tokenLen);
                case 'S' -> rule = selectNumberRule(Calendar.MILLISECOND, tokenLen);
                case 'E' -> rule = new TextField(Calendar.DAY_OF_WEEK, tokenLen < 4 ? shortWeekdays : weekdays);
                case 'u' -> rule = new DayInWeekField(selectNumberRule(Calendar.DAY_OF_WEEK, tokenLen));
                case 'D' -> rule = selectNumberRule(Calendar.DAY_OF_YEAR, tokenLen);
                case 'F' -> rule = selectNumberRule(Calendar.DAY_OF_WEEK_IN_MONTH, tokenLen);
                case 'w' -> rule = selectNumberRule(Calendar.WEEK_OF_YEAR, tokenLen);
                case 'W' -> rule = selectNumberRule(Calendar.WEEK_OF_MONTH, tokenLen);
                case 'a' -> rule = new TextField(Calendar.AM_PM, AmPmStrings);
                case 'k' -> rule = new TwentyFourHourField(selectNumberRule(Calendar.HOUR_OF_DAY, tokenLen));
                case 'K' -> rule = selectNumberRule(Calendar.HOUR, tokenLen);
                case 'X' -> rule = Iso8601_Rule.getRule(tokenLen);
                case 'z' -> rule = new TimeZoneNameRule(timeZone, locale,
                        tokenLen < 4 ? TimeZone.SHORT : TimeZone.LONG);
                case 'Z' -> {
                    if (tokenLen == 1) {
                        rule = TimeZoneNumberRule.INSTANCE_NO_COLON;
                    } else if (tokenLen == 2) {
                        rule = Iso8601_Rule.ISO8601_HOURS_COLON_MINUTES;
                    } else {
                        rule = TimeZoneNumberRule.INSTANCE_COLON;
                    }
                }
                case '\'' -> {
                    final String sub = token.substring(1);
                    if (sub.length() == 1) {
                        rule = new CharacterLiteral(sub.charAt(0));
                    } else {
                        rule = new StringLiteral(sub);
                    }
                }
                default -> throw new IllegalArgumentException("Illegal pattern component: " + token);
            }
            this.estimateLength += rule.estimateLength();
            rules.add(rule);
        }
        return rules;
    }

    /**
     * An interface for a single formatting rule.
     */
    public interface Rule {

        /**
         * Estimates the length of the formatted output.
         *
         * @return The estimated length.
         */
        int estimateLength();

        /**
         * Appends the formatted value to the buffer.
         *
         * @param buf      The buffer to append to.
         * @param calendar The calendar containing the date-time information.
         * @throws IOException if an I/O error occurs.
         */
        void appendTo(Appendable buf, Calendar calendar) throws IOException;
    }

    /**
     * An extension of {@link Rule} for formatting numeric fields.
     */
    public interface NumberRule extends Rule {

        /**
         * Appends a formatted numeric value to the buffer.
         *
         * @param buffer The buffer to append to.
         * @param value  The integer value to format.
         * @throws IOException if an I/O error occurs.
         */
        void appendTo(Appendable buffer, int value) throws IOException;
    }

    /**
     * A rule to output a literal character.
     */
    private static class CharacterLiteral implements Rule {

        private final char mValue;

        CharacterLiteral(final char value) {
            mValue = value;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Returns the estimated length of the formatted output for this character literal.
         *
         * @return {@code 1} as a character literal has a fixed length
         */
        @Override
        public int estimateLength() {
            return 1;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the character literal to the buffer.
         *
         * @param buffer   the buffer to append to
         * @param calendar the calendar (ignored for character literals)
         */
        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            buffer.append(mValue);
        }
    }

    /**
     * A rule to output a literal string.
     */
    private static class StringLiteral implements Rule {

        private final String mValue;

        StringLiteral(final String value) {
            mValue = value;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Returns the length of this string literal.
         *
         * @return the length of the string literal
         */
        @Override
        public int estimateLength() {
            return mValue.length();
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the string literal to the buffer.
         *
         * @param buffer   the buffer to append to
         * @param calendar the calendar (ignored for string literals)
         */
        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            buffer.append(mValue);
        }
    }

    /**
     * A rule to output a text field from a set of values (e.g., month name, day of the week).
     */
    private static class TextField implements Rule {

        private final int mField;
        private final String[] mValues;

        TextField(final int field, final String[] values) {
            mField = field;
            mValues = values;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Returns the maximum length of any value in the text field array.
         *
         * @return the maximum length of the text values
         */
        @Override
        public int estimateLength() {
            int max = 0;
            for (int i = mValues.length; --i >= 0;) {
                final int len = mValues[i].length();
                if (len > max) {
                    max = len;
                }
            }
            return max;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the text value for the specified calendar field to the buffer.
         *
         * @param buffer   the buffer to append to
         * @param calendar the calendar containing the field value
         */
        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            buffer.append(mValues[calendar.get(mField)]);
        }
    }

    /**
     * A rule for formatting a number field without padding.
     */
    private static class UnpaddedNumberField implements NumberRule {

        private final int mField;

        UnpaddedNumberField(final int field) {
            mField = field;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Returns the estimated maximum length for an unpadded number field.
         *
         * @return the estimated maximum length (4 digits)
         */
        @Override
        public int estimateLength() {
            return 4;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the unpadded numeric value of the specified field to the buffer.
         *
         * @param buffer   the buffer to append to
         * @param calendar the calendar containing the field value
         */
        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            appendTo(buffer, calendar.get(mField));
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the unpadded numeric value to the buffer.
         *
         * @param buffer the buffer to append to
         * @param value  the value to append
         */
        @Override
        public final void appendTo(final Appendable buffer, final int value) throws IOException {
            if (value < 10) {
                buffer.append((char) (value + '0'));
            } else if (value < 100) {
                appendDigits(buffer, value);
            } else {
                appendFullDigits(buffer, value, 1);
            }
        }
    }

    /**
     * A rule for formatting the month field without padding (1-12).
     */
    private static class UnpaddedMonthField implements NumberRule {

        static final UnpaddedMonthField INSTANCE = new UnpaddedMonthField();

        UnpaddedMonthField() {
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Returns the estimated maximum length for an unpadded month field.
         *
         * @return the estimated maximum length (2 digits)
         */
        @Override
        public int estimateLength() {
            return 2;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the unpadded month value (1-12) to the buffer.
         *
         * @param buffer   the buffer to append to
         * @param calendar the calendar containing the month value
         */
        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            appendTo(buffer, calendar.get(Calendar.MONTH) + 1);
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the unpadded month value to the buffer.
         *
         * @param buffer the buffer to append to
         * @param value  the month value (1-12)
         */
        @Override
        public final void appendTo(final Appendable buffer, final int value) throws IOException {
            if (value < 10) {
                buffer.append((char) (value + '0'));
            } else {
                appendDigits(buffer, value);
            }
        }
    }

    /**
     * A rule for formatting a number field with padding.
     */
    private static class PaddedNumberField implements NumberRule {

        private final int mField;
        private final int mSize;

        PaddedNumberField(final int field, final int size) {
            if (size < 3) {
                throw new IllegalArgumentException();
            }
            mField = field;
            mSize = size;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Returns the padded size of this number field.
         *
         * @return the padded size
         */
        @Override
        public int estimateLength() {
            return mSize;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the padded numeric value of the specified field to the buffer.
         *
         * @param buffer   the buffer to append to
         * @param calendar the calendar containing the field value
         */
        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            appendTo(buffer, calendar.get(mField));
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the padded numeric value to the buffer.
         *
         * @param buffer the buffer to append to
         * @param value  the value to append with padding
         */
        @Override
        public final void appendTo(final Appendable buffer, final int value) throws IOException {
            appendFullDigits(buffer, value, mSize);
        }
    }

    /**
     * A rule for formatting a number field with two digits.
     */
    private static class TwoDigitNumberField implements NumberRule {

        private final int mField;

        TwoDigitNumberField(final int field) {
            mField = field;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Returns the length of a two-digit number field.
         *
         * @return {@code 2} for two-digit fields
         */
        @Override
        public int estimateLength() {
            return 2;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the two-digit numeric value of the specified field to the buffer.
         *
         * @param buffer   the buffer to append to
         * @param calendar the calendar containing the field value
         */
        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            appendTo(buffer, calendar.get(mField));
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the value as a two-digit number to the buffer.
         *
         * @param buffer the buffer to append to
         * @param value  the value to append (will be formatted to 2 digits)
         */
        @Override
        public final void appendTo(final Appendable buffer, final int value) throws IOException {
            if (value < 100) {
                appendDigits(buffer, value);
            } else {
                appendFullDigits(buffer, value, 2);
            }
        }
    }

    /**
     * A rule for formatting the year with two digits.
     */
    private static class TwoDigitYearField implements NumberRule {

        static final TwoDigitYearField INSTANCE = new TwoDigitYearField();

        TwoDigitYearField() {
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Returns the length of a two-digit year field.
         *
         * @return {@code 2} for two-digit years
         */
        @Override
        public int estimateLength() {
            return 2;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the two-digit year value to the buffer.
         *
         * @param buffer   the buffer to append to
         * @param calendar the calendar containing the year value
         */
        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            appendTo(buffer, calendar.get(Calendar.YEAR) % 100);
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the two-digit value to the buffer.
         *
         * @param buffer the buffer to append to
         * @param value  the value to append as two digits
         */
        @Override
        public final void appendTo(final Appendable buffer, final int value) throws IOException {
            appendDigits(buffer, value);
        }
    }

    /**
     * A rule for formatting the month with two digits (01-12).
     */
    private static class TwoDigitMonthField implements NumberRule {

        static final TwoDigitMonthField INSTANCE = new TwoDigitMonthField();

        TwoDigitMonthField() {
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Returns the length of a two-digit month field.
         *
         * @return {@code 2} for two-digit months
         */
        @Override
        public int estimateLength() {
            return 2;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the two-digit month value (01-12) to the buffer.
         *
         * @param buffer   the buffer to append to
         * @param calendar the calendar containing the month value
         */
        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            appendTo(buffer, calendar.get(Calendar.MONTH) + 1);
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the value as a two-digit month to the buffer.
         *
         * @param buffer the buffer to append to
         * @param value  the month value to append (1-12)
         */
        @Override
        public final void appendTo(final Appendable buffer, final int value) throws IOException {
            appendDigits(buffer, value);
        }
    }

    /**
     * A rule for formatting the hour in 12-hour format (1-12).
     */
    private static class TwelveHourField implements NumberRule {

        private final NumberRule mRule;

        TwelveHourField(final NumberRule rule) {
            mRule = rule;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Returns the estimated length of the 12-hour field.
         *
         * @return the estimated length
         */
        @Override
        public int estimateLength() {
            return mRule.estimateLength();
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the 12-hour format value (1-12) to the buffer.
         *
         * @param buffer   the buffer to append to
         * @param calendar the calendar containing the hour value
         */
        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            int value = calendar.get(Calendar.HOUR);
            if (value == 0) {
                value = calendar.getLeastMaximum(Calendar.HOUR) + 1;
            }
            mRule.appendTo(buffer, value);
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the 12-hour format value to the buffer.
         *
         * @param buffer the buffer to append to
         * @param value  the value to append
         */
        @Override
        public void appendTo(final Appendable buffer, final int value) throws IOException {
            mRule.appendTo(buffer, value);
        }
    }

    /**
     * A rule for formatting the hour in 24-hour format (1-24).
     */
    private static class TwentyFourHourField implements NumberRule {

        private final NumberRule mRule;

        TwentyFourHourField(final NumberRule rule) {
            mRule = rule;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Returns the estimated length of the 24-hour field.
         *
         * @return the estimated length
         */
        @Override
        public int estimateLength() {
            return mRule.estimateLength();
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the 24-hour format value (1-24) to the buffer.
         *
         * @param buffer   the buffer to append to
         * @param calendar the calendar containing the hour value
         */
        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            int value = calendar.get(Calendar.HOUR_OF_DAY);
            if (value == 0) {
                value = calendar.getMaximum(Calendar.HOUR_OF_DAY) + 1;
            }
            mRule.appendTo(buffer, value);
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the 24-hour format value to the buffer.
         *
         * @param buffer the buffer to append to
         * @param value  the value to append
         */
        @Override
        public void appendTo(final Appendable buffer, final int value) throws IOException {
            mRule.appendTo(buffer, value);
        }
    }

    /**
     * A rule for formatting the day of the week as a number (1=Monday...7=Sunday).
     */
    private static class DayInWeekField implements NumberRule {

        private final NumberRule mRule;

        DayInWeekField(final NumberRule rule) {
            mRule = rule;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Returns the estimated length of the day-in-week field.
         *
         * @return the estimated length
         */
        @Override
        public int estimateLength() {
            return mRule.estimateLength();
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the day of week as a number (1=Monday...7=Sunday) to the buffer.
         *
         * @param buffer   the buffer to append to
         * @param calendar the calendar containing the day of week value
         */
        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            final int value = calendar.get(Calendar.DAY_OF_WEEK);
            mRule.appendTo(buffer, value != Calendar.SUNDAY ? value - 1 : 7);
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the day-in-week value to the buffer.
         *
         * @param buffer the buffer to append to
         * @param value  the value to append
         */
        @Override
        public void appendTo(final Appendable buffer, final int value) throws IOException {
            mRule.appendTo(buffer, value);
        }
    }

    /**
     * A rule for formatting the week-based year.
     */
    private static class WeekYear implements NumberRule {

        private final NumberRule mRule;

        WeekYear(final NumberRule rule) {
            mRule = rule;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Returns the estimated length of the week-based year field.
         *
         * @return the estimated length
         */
        @Override
        public int estimateLength() {
            return mRule.estimateLength();
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the week-based year value to the buffer.
         *
         * @param buffer   the buffer to append to
         * @param calendar the calendar containing the week year value
         */
        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            int weekYear = calendar.getWeekYear();
            if (mRule instanceof TwoDigitYearField) {
                weekYear %= 100;
            }
            mRule.appendTo(buffer, weekYear);
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the week year value to the buffer.
         *
         * @param buffer the buffer to append to
         * @param value  the week year value to append
         */
        @Override
        public void appendTo(final Appendable buffer, final int value) throws IOException {
            mRule.appendTo(buffer, value);
        }
    }

    /**
     * A rule for formatting the timezone name.
     */
    private static class TimeZoneNameRule implements Rule {

        private final Locale mLocale;
        private final int mStyle;
        private final String mStandard;
        private final String mDaylight;

        TimeZoneNameRule(final TimeZone timeZone, final Locale locale, final int style) {
            mLocale = locale;
            mStyle = style;
            mStandard = getTimeZoneDisplay(timeZone, false, style, locale);
            mDaylight = getTimeZoneDisplay(timeZone, true, style, locale);
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Returns the maximum length of the standard or daylight timezone name.
         *
         * @return the maximum length of the timezone names
         */
        @Override
        public int estimateLength() {
            return Math.max(mStandard.length(), mDaylight.length());
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the timezone name (standard or daylight) to the buffer based on the calendar's DST offset.
         *
         * @param buffer   the buffer to append to
         * @param calendar the calendar containing the timezone information
         */
        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            final TimeZone zone = calendar.getTimeZone();
            if (calendar.get(Calendar.DST_OFFSET) != 0) {
                buffer.append(getTimeZoneDisplay(zone, true, mStyle, mLocale));
            } else {
                buffer.append(getTimeZoneDisplay(zone, false, mStyle, mLocale));
            }
        }
    }

    /**
     * A rule for formatting the timezone as a numeric offset (e.g., +/-HHMM or +/-HH:mm).
     */
    private static class TimeZoneNumberRule implements Rule {

        static final TimeZoneNumberRule INSTANCE_COLON = new TimeZoneNumberRule(true);
        static final TimeZoneNumberRule INSTANCE_NO_COLON = new TimeZoneNumberRule(false);
        final boolean mColon;

        TimeZoneNumberRule(final boolean colon) {
            mColon = colon;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Returns the length of the timezone offset format.
         *
         * @return {@code 5} for the +/-HHMM or +/-HH:mm format
         */
        @Override
        public int estimateLength() {
            return 5;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the timezone offset to the buffer in +/-HHMM or +/-HH:mm format.
         *
         * @param buffer   the buffer to append to
         * @param calendar the calendar containing the timezone offset
         */
        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            int offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
            if (offset < 0) {
                buffer.append('-');
                offset = -offset;
            } else {
                buffer.append('+');
            }
            final int hours = offset / (60 * 60 * 1000);
            appendDigits(buffer, hours);
            if (mColon) {
                buffer.append(':');
            }
            final int minutes = offset / (60 * 1000) - 60 * hours;
            appendDigits(buffer, minutes);
        }
    }

    /**
     * A rule for formatting the timezone in ISO 8601 format.
     */
    private static class Iso8601_Rule implements Rule {

        static final Iso8601_Rule ISO8601_HOURS = new Iso8601_Rule(3);
        static final Iso8601_Rule ISO8601_HOURS_MINUTES = new Iso8601_Rule(5);
        static final Iso8601_Rule ISO8601_HOURS_COLON_MINUTES = new Iso8601_Rule(6);
        final int length;

        Iso8601_Rule(final int length) {
            this.length = length;
        }

        static Iso8601_Rule getRule(final int tokenLen) {
            return switch (tokenLen) {
                case 1 -> Iso8601_Rule.ISO8601_HOURS;
                case 2 -> Iso8601_Rule.ISO8601_HOURS_MINUTES;
                case 3 -> Iso8601_Rule.ISO8601_HOURS_COLON_MINUTES;
                default -> throw new IllegalArgumentException("invalid number of X");
            };
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Returns the length of the ISO 8601 timezone format.
         *
         * @return the length of the format
         */
        @Override
        public int estimateLength() {
            return length;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Appends the timezone offset in ISO 8601 format to the buffer.
         *
         * @param buffer   the buffer to append to
         * @param calendar the calendar containing the timezone offset
         */
        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            int offset = calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET);
            if (offset == 0) {
                buffer.append("Z");
                return;
            }
            if (offset < 0) {
                buffer.append('-');
                offset = -offset;
            } else {
                buffer.append('+');
            }
            final int hours = offset / (60 * 60 * 1000);
            appendDigits(buffer, hours);
            if (length < 5) {
                return;
            }
            if (length == 6) {
                buffer.append(':');
            }
            final int minutes = offset / (60 * 1000) - 60 * hours;
            appendDigits(buffer, minutes);
        }
    }

    /**
     * A composite key for caching timezone display names.
     */
    private static class TimeZoneDisplayKey {

        private final TimeZone mTimeZone;
        private final int mStyle;
        private final Locale mLocale;

        TimeZoneDisplayKey(final TimeZone timeZone, final boolean daylight, final int style, final Locale locale) {
            mTimeZone = timeZone;
            mStyle = daylight ? style | 0x80000000 : style;
            mLocale = locale;
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Computes the hash code for this timezone display key.
         *
         * @return the hash code
         */
        @Override
        public int hashCode() {
            return (mStyle * 31 + mLocale.hashCode()) * 31 + mTimeZone.hashCode();
        }

        /**
         * Description inherited from parent class or interface.
         * <p>
         * Checks if this key is equal to another object.
         *
         * @param object the object to compare with
         * @return {@code true} if the objects are equal
         */
        @Override
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof TimeZoneDisplayKey other) {
                return mTimeZone.equals(other.mTimeZone) && mStyle == other.mStyle && mLocale.equals(other.mLocale);
            }
            return false;
        }
    }

}
