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
 * 日期格式表达式类，用于解析和格式化日期时间。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DatePattern {

    /**
     * 时区显示名称缓存，用于提升性能
     */
    private static final ConcurrentMap<TimeZoneDisplayKey, String> C_TIME_ZONE_DISPLAY_CACHE = new ConcurrentHashMap<>(7);

    /**
     * 格式化规则列表
     */
    private final Rule[] rules;

    /**
     * 估算格式化后字符串长度
     */
    private int estimateLength;

    /**
     * 构造函数，初始化日期格式表达式。
     *
     * @param patternStr 日期表达式字符串
     * @param locale     地域信息
     * @param timeZone   时区
     */
    public DatePattern(final String patternStr, final Locale locale, final TimeZone timeZone) {
        this.rules = parsePattern(patternStr, locale, timeZone).toArray(new Rule[0]);
    }

    /**
     * 解析日期格式表达式中的标记。
     *
     * @param pattern  格式表达式字符串
     * @param indexRef 索引引用数组
     * @return 解析出的标记
     */
    protected static String parseToken(final String pattern, final int[] indexRef) {
        final StringBuilder buf = new StringBuilder();
        int i = indexRef[0];
        final int length = pattern.length();
        char c = pattern.charAt(i);

        if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {
            buf.append(c);
            while (i + 1 < length) {
                final char peek = pattern.charAt(i + 1);
                if (peek == c) {
                    buf.append(c);
                    i++;
                } else {
                    break;
                }
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
                } else if (!inLiteral && (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z')) {
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
     * 根据字段和填充长度选择合适的数字规则。
     *
     * @param field   日期字段
     * @param padding 填充长度
     * @return 数字规则
     */
    protected static NumberRule selectNumberRule(final int field, final int padding) {
        return switch (padding) {
            case 1 -> new UnpaddedNumberField(field);
            case 2 -> new TwoDigitNumberField(field);
            default -> new PaddedNumberField(field, padding);
        };
    }

    /**
     * 将两位数字追加到输出缓冲区。
     *
     * @param buffer 输出缓冲区
     * @param value  要追加的数字
     * @throws IOException 如果发生I/O错误
     */
    private static void appendDigits(final Appendable buffer, final int value) throws IOException {
        buffer.append((char) (value / 10 + '0'));
        buffer.append((char) (value % 10 + '0'));
    }

    /**
     * 将完整数字追加到输出缓冲区，支持填充。
     *
     * @param buffer        输出缓冲区
     * @param value         要追加的数字
     * @param minFieldWidth 最小字段宽度
     * @throws IOException 如果发生I/O错误
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
                case 4:
                    buffer.append((char) (value / 1000 + '0'));
                    value %= 1000;
                case 3:
                    if (value >= 100) {
                        buffer.append((char) (value / 100 + '0'));
                        value %= 100;
                    } else {
                        buffer.append('0');
                    }
                case 2:
                    if (value >= 10) {
                        buffer.append((char) (value / 10 + '0'));
                        value %= 10;
                    } else {
                        buffer.append('0');
                    }
                case 1:
                    buffer.append((char) (value + '0'));
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
     * 获取时区显示名称，使用缓存提升性能。
     *
     * @param tz       时区
     * @param daylight 是否为夏令时
     * @param style    显示样式（TimeZone.LONG或TimeZone.SHORT）
     * @param locale   地域
     * @return 时区显示名称
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
     * 获取估算的格式化后字符串长度。
     *
     * @return 估算长度
     */
    public int getEstimateLength() {
        return this.estimateLength;
    }

    /**
     * 根据指定格式，将日期时间格式化到输出缓冲区。
     *
     * @param calendar 日历对象
     * @param buf      输出缓冲区
     * @param <B>      Appendable实现类（如StringBuilder或StringBuffer）
     * @return 格式化后的Appendable对象
     * @throws DateException 如果发生I/O错误
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
     * 解析日期表达式字符串，返回规则列表。
     *
     * @param patternStr 日期表达式字符串
     * @param locale     地域
     * @param timeZone   时区
     * @return 规则列表
     * @throws IllegalArgumentException 如果表达式无效
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
                case 'G':
                    rule = new TextField(Calendar.ERA, ERAs);
                    break;
                case 'y':
                case 'Y':
                    if (tokenLen == 2) {
                        rule = TwoDigitYearField.INSTANCE;
                    } else {
                        rule = selectNumberRule(Calendar.YEAR, Math.max(tokenLen, 4));
                    }
                    if (c == 'Y') {
                        rule = new WeekYear((NumberRule) rule);
                    }
                    break;
                case 'M':
                    if (tokenLen >= 4) {
                        rule = new TextField(Calendar.MONTH, months);
                    } else if (tokenLen == 3) {
                        rule = new TextField(Calendar.MONTH, shortMonths);
                    } else if (tokenLen == 2) {
                        rule = TwoDigitMonthField.INSTANCE;
                    } else {
                        rule = UnpaddedMonthField.INSTANCE;
                    }
                    break;
                case 'd':
                    rule = selectNumberRule(Calendar.DAY_OF_MONTH, tokenLen);
                    break;
                case 'h':
                    rule = new TwelveHourField(selectNumberRule(Calendar.HOUR, tokenLen));
                    break;
                case 'H':
                    rule = selectNumberRule(Calendar.HOUR_OF_DAY, tokenLen);
                    break;
                case 'm':
                    rule = selectNumberRule(Calendar.MINUTE, tokenLen);
                    break;
                case 's':
                    rule = selectNumberRule(Calendar.SECOND, tokenLen);
                    break;
                case 'S':
                    rule = selectNumberRule(Calendar.MILLISECOND, tokenLen);
                    break;
                case 'E':
                    rule = new TextField(Calendar.DAY_OF_WEEK, tokenLen < 4 ? shortWeekdays : weekdays);
                    break;
                case 'u':
                    rule = new DayInWeekField(selectNumberRule(Calendar.DAY_OF_WEEK, tokenLen));
                    break;
                case 'D':
                    rule = selectNumberRule(Calendar.DAY_OF_YEAR, tokenLen);
                    break;
                case 'F':
                    rule = selectNumberRule(Calendar.DAY_OF_WEEK_IN_MONTH, tokenLen);
                    break;
                case 'w':
                    rule = selectNumberRule(Calendar.WEEK_OF_YEAR, tokenLen);
                    break;
                case 'W':
                    rule = selectNumberRule(Calendar.WEEK_OF_MONTH, tokenLen);
                    break;
                case 'a':
                    rule = new TextField(Calendar.AM_PM, AmPmStrings);
                    break;
                case 'k':
                    rule = new TwentyFourHourField(selectNumberRule(Calendar.HOUR_OF_DAY, tokenLen));
                    break;
                case 'K':
                    rule = selectNumberRule(Calendar.HOUR, tokenLen);
                    break;
                case 'X':
                    rule = Iso8601_Rule.getRule(tokenLen);
                    break;
                case 'z':
                    if (tokenLen >= 4) {
                        rule = new TimeZoneNameRule(timeZone, locale, TimeZone.LONG);
                    } else {
                        rule = new TimeZoneNameRule(timeZone, locale, TimeZone.SHORT);
                    }
                    break;
                case 'Z':
                    if (tokenLen == 1) {
                        rule = TimeZoneNumberRule.INSTANCE_NO_COLON;
                    } else if (tokenLen == 2) {
                        rule = Iso8601_Rule.ISO8601_HOURS_COLON_MINUTES;
                    } else {
                        rule = TimeZoneNumberRule.INSTANCE_COLON;
                    }
                    break;
                case '\'':
                    final String sub = token.substring(1);
                    if (sub.length() == 1) {
                        rule = new CharacterLiteral(sub.charAt(0));
                    } else {
                        rule = new StringLiteral(sub);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Illegal pattern component: " + token);
            }

            this.estimateLength += rule.estimateLength();
            rules.add(rule);
        }

        return rules;
    }

    /**
     * 日期格式化规则接口。
     */
    public interface Rule {
        /**
         * 估算格式化结果的长度。
         *
         * @return 估算长度
         */
        int estimateLength();

        /**
         * 根据规则将日历值追加到输出缓冲区。
         *
         * @param buf      输出缓冲区
         * @param calendar 日历对象
         * @throws IOException 如果发生I/O错误
         */
        void appendTo(Appendable buf, Calendar calendar) throws IOException;
    }

    /**
     * 数字格式化规则接口。
     */
    public interface NumberRule extends Rule {
        /**
         * 将指定数字值追加到输出缓冲区。
         *
         * @param buffer 输出缓冲区
         * @param value  要追加的数字
         * @throws IOException 如果发生I/O错误
         */
        void appendTo(Appendable buffer, int value) throws IOException;
    }

    /**
     * 输出单个字符的规则。
     */
    private static class CharacterLiteral implements Rule {
        /**
         * 字符值
         */
        private final char mValue;

        /**
         * 构造CharacterLiteral实例。
         *
         * @param value 字符值
         */
        CharacterLiteral(final char value) {
            mValue = value;
        }

        @Override
        public int estimateLength() {
            return 1;
        }

        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            buffer.append(mValue);
        }
    }

    /**
     * 输出固定字符串的规则。
     */
    private static class StringLiteral implements Rule {
        /**
         * 字符串值
         */
        private final String mValue;

        /**
         * 构造StringLiteral实例。
         *
         * @param value 字符串值
         */
        StringLiteral(final String value) {
            mValue = value;
        }

        @Override
        public int estimateLength() {
            return mValue.length();
        }

        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            buffer.append(mValue);
        }
    }

    /**
     * 输出一组值之一的规则。
     */
    private static class TextField implements Rule {
        /**
         * 日历字段
         */
        private final int mField;
        /**
         * 字段值数组
         */
        private final String[] mValues;

        /**
         * 构造TextField实例。
         *
         * @param field  日历字段
         * @param values 字段值数组
         */
        TextField(final int field, final String[] values) {
            mField = field;
            mValues = values;
        }

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

        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            buffer.append(mValues[calendar.get(mField)]);
        }
    }

    /**
     * 输出无填充数字的规则。
     */
    private static class UnpaddedNumberField implements NumberRule {
        /**
         * 日历字段
         */
        private final int mField;

        /**
         * 构造UnpaddedNumberField实例。
         *
         * @param field 日历字段
         */
        UnpaddedNumberField(final int field) {
            mField = field;
        }

        @Override
        public int estimateLength() {
            return 4;
        }

        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            appendTo(buffer, calendar.get(mField));
        }

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
     * 输出无填充月份的规则。
     */
    private static class UnpaddedMonthField implements NumberRule {
        /**
         * 单例实例
         */
        static final UnpaddedMonthField INSTANCE = new UnpaddedMonthField();

        /**
         * 构造UnpaddedMonthField实例。
         */
        UnpaddedMonthField() {
        }

        @Override
        public int estimateLength() {
            return 2;
        }

        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            appendTo(buffer, calendar.get(Calendar.MONTH) + 1);
        }

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
     * 输出填充数字的规则。
     */
    private static class PaddedNumberField implements NumberRule {
        /**
         * 日历字段
         */
        private final int mField;
        /**
         * 输出字段大小
         */
        private final int mSize;

        /**
         * 构造PaddedNumberField实例。
         *
         * @param field 日历字段
         * @param size  输出字段大小
         */
        PaddedNumberField(final int field, final int size) {
            if (size < 3) {
                throw new IllegalArgumentException();
            }
            mField = field;
            mSize = size;
        }

        @Override
        public int estimateLength() {
            return mSize;
        }

        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            appendTo(buffer, calendar.get(mField));
        }

        @Override
        public final void appendTo(final Appendable buffer, final int value) throws IOException {
            appendFullDigits(buffer, value, mSize);
        }
    }

    /**
     * 输出两位数字的规则。
     */
    private static class TwoDigitNumberField implements NumberRule {
        /**
         * 日历字段
         */
        private final int mField;

        /**
         * 构造TwoDigitNumberField实例。
         *
         * @param field 日历字段
         */
        TwoDigitNumberField(final int field) {
            mField = field;
        }

        @Override
        public int estimateLength() {
            return 2;
        }

        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            appendTo(buffer, calendar.get(mField));
        }

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
     * 输出两位年份的规则。
     */
    private static class TwoDigitYearField implements NumberRule {
        /**
         * 单例实例
         */
        static final TwoDigitYearField INSTANCE = new TwoDigitYearField();

        /**
         * 构造TwoDigitYearField实例。
         */
        TwoDigitYearField() {
        }

        @Override
        public int estimateLength() {
            return 2;
        }

        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            appendTo(buffer, calendar.get(Calendar.YEAR) % 100);
        }

        @Override
        public final void appendTo(final Appendable buffer, final int value) throws IOException {
            appendDigits(buffer, value);
        }
    }

    /**
     * 输出两位月份的规则。
     */
    private static class TwoDigitMonthField implements NumberRule {
        /**
         * 单例实例
         */
        static final TwoDigitMonthField INSTANCE = new TwoDigitMonthField();

        /**
         * 构造TwoDigitMonthField实例。
         */
        TwoDigitMonthField() {
        }

        @Override
        public int estimateLength() {
            return 2;
        }

        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            appendTo(buffer, calendar.get(Calendar.MONTH) + 1);
        }

        @Override
        public final void appendTo(final Appendable buffer, final int value) throws IOException {
            appendDigits(buffer, value);
        }
    }

    /**
     * 输出12小时制字段的规则。
     */
    private static class TwelveHourField implements NumberRule {
        /**
         * 数字规则
         */
        private final NumberRule mRule;

        /**
         * 构造TwelveHourField实例。
         *
         * @param rule 数字规则
         */
        TwelveHourField(final NumberRule rule) {
            mRule = rule;
        }

        @Override
        public int estimateLength() {
            return mRule.estimateLength();
        }

        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            int value = calendar.get(Calendar.HOUR);
            if (value == 0) {
                value = calendar.getLeastMaximum(Calendar.HOUR) + 1;
            }
            mRule.appendTo(buffer, value);
        }

        @Override
        public void appendTo(final Appendable buffer, final int value) throws IOException {
            mRule.appendTo(buffer, value);
        }
    }

    /**
     * 输出24小时制字段的规则。
     */
    private static class TwentyFourHourField implements NumberRule {
        /**
         * 数字规则
         */
        private final NumberRule mRule;

        /**
         * 构造TwentyFourHourField实例。
         *
         * @param rule 数字规则
         */
        TwentyFourHourField(final NumberRule rule) {
            mRule = rule;
        }

        @Override
        public int estimateLength() {
            return mRule.estimateLength();
        }

        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            int value = calendar.get(Calendar.HOUR_OF_DAY);
            if (value == 0) {
                value = calendar.getMaximum(Calendar.HOUR_OF_DAY) + 1;
            }
            mRule.appendTo(buffer, value);
        }

        @Override
        public void appendTo(final Appendable buffer, final int value) throws IOException {
            mRule.appendTo(buffer, value);
        }
    }

    /**
     * 输出星期数字的规则。
     */
    private static class DayInWeekField implements NumberRule {
        /**
         * 数字规则
         */
        private final NumberRule mRule;

        /**
         * 构造DayInWeekField实例。
         *
         * @param rule 数字规则
         */
        DayInWeekField(final NumberRule rule) {
            mRule = rule;
        }

        @Override
        public int estimateLength() {
            return mRule.estimateLength();
        }

        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            final int value = calendar.get(Calendar.DAY_OF_WEEK);
            mRule.appendTo(buffer, value != Calendar.SUNDAY ? value - 1 : 7);
        }

        @Override
        public void appendTo(final Appendable buffer, final int value) throws IOException {
            mRule.appendTo(buffer, value);
        }
    }

    /**
     * 输出周年份的规则。
     */
    private static class WeekYear implements NumberRule {
        /**
         * 数字规则
         */
        private final NumberRule mRule;

        /**
         * 构造WeekYear实例。
         *
         * @param rule 数字规则
         */
        WeekYear(final NumberRule rule) {
            mRule = rule;
        }

        @Override
        public int estimateLength() {
            return mRule.estimateLength();
        }

        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            int weekYear = calendar.getWeekYear();
            if (mRule instanceof TwoDigitYearField) {
                weekYear %= 100;
            }
            mRule.appendTo(buffer, weekYear);
        }

        @Override
        public void appendTo(final Appendable buffer, final int value) throws IOException {
            mRule.appendTo(buffer, value);
        }
    }

    /**
     * 输出时区名称的规则。
     */
    private static class TimeZoneNameRule implements Rule {
        /**
         * 地域
         */
        private final Locale mLocale;
        /**
         * 显示样式
         */
        private final int mStyle;
        /**
         * 标准时区名称
         */
        private final String mStandard;
        /**
         * 夏令时区名称
         */
        private final String mDaylight;

        /**
         * 构造TimeZoneNameRule实例。
         *
         * @param timeZone 时区
         * @param locale   地域
         * @param style    显示样式
         */
        TimeZoneNameRule(final TimeZone timeZone, final Locale locale, final int style) {
            mLocale = locale;
            mStyle = style;
            mStandard = getTimeZoneDisplay(timeZone, false, style, locale);
            mDaylight = getTimeZoneDisplay(timeZone, true, style, locale);
        }

        @Override
        public int estimateLength() {
            return Math.max(mStandard.length(), mDaylight.length());
        }

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
     * 输出时区数字的规则（如+/-HHMM或+/-HH:MM）。
     */
    private static class TimeZoneNumberRule implements Rule {
        /**
         * 带冒号的时区格式实例
         */
        static final TimeZoneNumberRule INSTANCE_COLON = new TimeZoneNumberRule(true);
        /**
         * 无冒号的时区格式实例
         */
        static final TimeZoneNumberRule INSTANCE_NO_COLON = new TimeZoneNumberRule(false);
        /**
         * 是否在小时和分钟之间添加冒号
         */
        final boolean mColon;

        /**
         * 构造TimeZoneNumberRule实例。
         *
         * @param colon 是否在小时和分钟之间添加冒号
         */
        TimeZoneNumberRule(final boolean colon) {
            mColon = colon;
        }

        @Override
        public int estimateLength() {
            return 5;
        }

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
     * 输出ISO 8601时区格式的规则。
     */
    private static class Iso8601_Rule implements Rule {
        /**
         * 仅小时格式实例
         */
        static final Iso8601_Rule ISO8601_HOURS = new Iso8601_Rule(3);
        /**
         * 小时和分钟格式实例
         */
        static final Iso8601_Rule ISO8601_HOURS_MINUTES = new Iso8601_Rule(5);
        /**
         * 带冒号的小时和分钟格式实例
         */
        static final Iso8601_Rule ISO8601_HOURS_COLON_MINUTES = new Iso8601_Rule(6);
        /**
         * 输出字符长度
         */
        final int length;

        /**
         * 构造Iso8601_Rule实例。
         *
         * @param length 输出字符长度（除非输出Z）
         */
        Iso8601_Rule(final int length) {
            this.length = length;
        }

        /**
         * 获取指定长度的ISO 8601规则。
         *
         * @param tokenLen 时区字符串长度标记
         * @return Iso8601_Rule实例
         * @throws IllegalArgumentException 如果长度无效
         */
        static Iso8601_Rule getRule(final int tokenLen) {
            return switch (tokenLen) {
                case 1 -> Iso8601_Rule.ISO8601_HOURS;
                case 2 -> Iso8601_Rule.ISO8601_HOURS_MINUTES;
                case 3 -> Iso8601_Rule.ISO8601_HOURS_COLON_MINUTES;
                default -> throw new IllegalArgumentException("invalid number of X");
            };
        }

        @Override
        public int estimateLength() {
            return length;
        }

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
     * 时区显示名称的复合键类。
     */
    private static class TimeZoneDisplayKey {
        /**
         * 时区
         */
        private final TimeZone mTimeZone;
        /**
         * 时区样式
         */
        private final int mStyle;
        /**
         * 地域
         */
        private final Locale mLocale;

        /**
         * 构造TimeZoneDisplayKey实例。
         *
         * @param timeZone 时区
         * @param daylight 是否为夏令时
         * @param style    时区样式
         * @param locale   地域
         */
        TimeZoneDisplayKey(final TimeZone timeZone, final boolean daylight, final int style, final Locale locale) {
            mTimeZone = timeZone;
            if (daylight) {
                mStyle = style | 0x80000000;
            } else {
                mStyle = style;
            }
            mLocale = locale;
        }

        @Override
        public int hashCode() {
            return (mStyle * 31 + mLocale.hashCode()) * 31 + mTimeZone.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof TimeZoneDisplayKey other) {
                return mTimeZone.equals(other.mTimeZone) && mStyle == other.mStyle && mLocale.equals(other.mLocale);
            }
            return false;
        }
    }

}