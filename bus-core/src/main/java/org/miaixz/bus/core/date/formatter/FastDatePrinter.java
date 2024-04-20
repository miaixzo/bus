/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.date.formatter;

import org.miaixz.bus.core.exception.InternalException;
import org.miaixz.bus.core.lang.Symbol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DateFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * {@link java.text.SimpleDateFormat} 的线程安全版本,用于将 {@link Date} 格式化输出
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FastDatePrinter extends AbstractMotd implements DatePrinter {

    private static final long serialVersionUID = 1L;
    private static final int MAX_DIGITS = 10; // log10(Integer.MAX_VALUE) ~= 9.3
    private static final ConcurrentMap<TimeZoneDisplayKey, String> C_TIME_ZONE_DISPLAY_CACHE = new ConcurrentHashMap<>(7);
    /**
     * 规则列表.
     */
    private transient Rule[] rules;
    /**
     * 估算最大长度.
     */
    private transient int mMaxLengthEstimate;

    /**
     * 构造,内部使用
     *
     * @param pattern  使用{@link java.text.SimpleDateFormat} 相同的日期格式
     * @param timeZone 非空时区{@link TimeZone}
     * @param locale   非空{@link Locale} 日期地理位置
     */
    public FastDatePrinter(final String pattern, TimeZone timeZone, final Locale locale) {
        super(pattern, timeZone, locale);
        init();
    }

    /**
     * 向给定缓冲区追加两个数字
     *
     * @param buffer 要追加的缓冲区
     * @param value  要附加数字的值
     */
    private static void appendDigits(final Appendable buffer, final int value) throws IOException {
        buffer.append((char) (value / 10 + '0'));
        buffer.append((char) (value % 10 + '0'));
    }

    /**
     * 将所有数字追加到给定的缓冲区
     *
     * @param buffer 要追加的缓冲区
     * @param value  要附加数字的值
     */
    private static void appendFullDigits(final Appendable buffer, int value, int minFieldWidth) throws IOException {
        // 1到4位数字的专用路径——>避免从临时工作数组分配内存(参见LANG-1248)
        if (value < 10000) {
            // 更少的内存分配路径适用于4位或更少的数字
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
            // 左零垫
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
            // 更多的内存分配路径适用于任何数字
            // 以相反的方式建立十进制表示法
            final char[] work = new char[MAX_DIGITS];
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
     * 获取时区显示名称，使用缓存以获得性能
     *
     * @param tz       要查询的区域
     * @param daylight 适用于夏时制
     * @param style    使用{@code时区的样式。长}或{@code TimeZone.SHORT}
     * @param locale   要使用的语言环境
     * @return 时区的文本名称
     */
    static String getTimeZoneDisplay(final TimeZone tz, final boolean daylight, final int style, final Locale locale) {
        final TimeZoneDisplayKey key = new TimeZoneDisplayKey(tz, daylight, style, locale);
        String value = C_TIME_ZONE_DISPLAY_CACHE.get(key);
        if (null == value) {
            // This is a very slow call, so cache the results.
            value = tz.getDisplayName(daylight, style, locale);
            final String prior = C_TIME_ZONE_DISPLAY_CACHE.putIfAbsent(key, value);
            if (null != prior) {
                value = prior;
            }
        }
        return value;
    }

    /**
     * 初始化
     */
    private void init() {
        final List<Rule> rulesList = parsePattern();
        rules = rulesList.toArray(new Rule[rulesList.size()]);

        int len = 0;
        for (int i = rules.length; --i >= 0; ) {
            len += rules[i].estimateLength();
        }

        mMaxLengthEstimate = len;
    }

    /**
     * 返回给定模式的规则列表
     *
     * @return 规则对象的{@code List}
     * @throws IllegalArgumentException 如果模式无效
     */
    protected List<Rule> parsePattern() {
        final DateFormatSymbols symbols = new DateFormatSymbols(locale);
        final List<Rule> rules = new ArrayList<>();

        final String[] ERAs = symbols.getEras();
        final String[] months = symbols.getMonths();
        final String[] shortMonths = symbols.getShortMonths();
        final String[] weekdays = symbols.getWeekdays();
        final String[] shortWeekdays = symbols.getShortWeekdays();
        final String[] AmPmStrings = symbols.getAmPmStrings();

        final int length = pattern.length();
        final int[] indexRef = new int[1];

        for (int i = 0; i < length; i++) {
            indexRef[0] = i;
            final String token = parseToken(pattern, indexRef);
            i = indexRef[0];

            final int tokenLen = token.length();
            if (tokenLen == 0) {
                break;
            }

            Rule rule;
            final char c = token.charAt(0);

            switch (c) {
                case 'G': // 时区 (文本)
                    rule = new TextField(Calendar.ERA, ERAs);
                    break;
                case 'y': // 年 (数字)
                case 'Y': // 周年
                    if (tokenLen == 2) {
                        rule = TwoDigitYearField.INSTANCE;
                    } else {
                        rule = selectNumberRule(Calendar.YEAR, Math.max(tokenLen, 4));
                    }
                    if (c == 'Y') {
                        rule = new WeekYear((NumberRule) rule);
                    }
                    break;
                case 'M': // 月年 (文本和数字)
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
                case 'd': // 天月 (数字)
                    rule = selectNumberRule(Calendar.DAY_OF_MONTH, tokenLen);
                    break;
                case 'h': // 小时 上午/下午 (数字, 1..12)
                    rule = new TwelveHourField(selectNumberRule(Calendar.HOUR, tokenLen));
                    break;
                case 'H': // 小时天 (数字, 0..23)
                    rule = selectNumberRule(Calendar.HOUR_OF_DAY, tokenLen);
                    break;
                case 'm': // 分钟小时 (数字)
                    rule = selectNumberRule(Calendar.MINUTE, tokenLen);
                    break;
                case 's': // 秒分 (数字)
                    rule = selectNumberRule(Calendar.SECOND, tokenLen);
                    break;
                case 'S': // 毫秒 (数字)
                    rule = selectNumberRule(Calendar.MILLISECOND, tokenLen);
                    break;
                case 'E': // 天周 (文本)
                    rule = new TextField(Calendar.DAY_OF_WEEK, tokenLen < 4 ? shortWeekdays : weekdays);
                    break;
                case 'u': // 天周 (数字)
                    rule = new DayInWeekField(selectNumberRule(Calendar.DAY_OF_WEEK, tokenLen));
                    break;
                case 'D': // 天年 (数字)
                    rule = selectNumberRule(Calendar.DAY_OF_YEAR, tokenLen);
                    break;
                case 'F': // 天周月 (数字)
                    rule = selectNumberRule(Calendar.DAY_OF_WEEK_IN_MONTH, tokenLen);
                    break;
                case 'w': // 周年 (数字)
                    rule = selectNumberRule(Calendar.WEEK_OF_YEAR, tokenLen);
                    break;
                case 'W': // 周月 (数字)
                    rule = selectNumberRule(Calendar.WEEK_OF_MONTH, tokenLen);
                    break;
                case 'a': // 上午/下午 (文本)
                    rule = new TextField(Calendar.AM_PM, AmPmStrings);
                    break;
                case 'k': // 小时天 (1..24)
                    rule = new TwentyFourHourField(selectNumberRule(Calendar.HOUR_OF_DAY, tokenLen));
                    break;
                case 'K': // 小时 上午/下午 (0..11)
                    rule = selectNumberRule(Calendar.HOUR, tokenLen);
                    break;
                case 'X': // ISO 8601
                    rule = Iso8601_Rule.getRule(tokenLen);
                    break;
                case 'z': // 时区 (文本)
                    if (tokenLen >= 4) {
                        rule = new TimeZoneNameRule(timeZone, locale, TimeZone.LONG);
                    } else {
                        rule = new TimeZoneNameRule(timeZone, locale, TimeZone.SHORT);
                    }
                    break;
                case 'Z': // 时区 (值)
                    if (tokenLen == 1) {
                        rule = TimeZoneNumberRule.INSTANCE_NO_COLON;
                    } else if (tokenLen == 2) {
                        rule = Iso8601_Rule.ISO8601_HOURS_COLON_MINUTES;
                    } else {
                        rule = TimeZoneNumberRule.INSTANCE_COLON;
                    }
                    break;
                case Symbol.C_SINGLE_QUOTE:
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

            rules.add(rule);
        }

        return rules;
    }

    /**
     * 执行令牌解析
     *
     * @param pattern  该模式
     * @param indexRef 索引的引用
     * @return 解析令牌
     */
    protected String parseToken(final String pattern, final int[] indexRef) {
        final StringBuilder buf = new StringBuilder();

        int i = indexRef[0];
        final int length = pattern.length();

        char c = pattern.charAt(i);
        if (c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {
            // 扫描相同字符的运行，这表示时间模式
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
            // 这将标识令牌为文本
            buf.append(Symbol.C_SINGLE_QUOTE);

            boolean inLiteral = false;

            for (; i < length; i++) {
                c = pattern.charAt(i);

                if (c == Symbol.C_SINGLE_QUOTE) {
                    if (i + 1 < length && pattern.charAt(i + 1) == Symbol.C_SINGLE_QUOTE) {
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
     * 获取所需填充的适当规则
     *
     * @param field   得到一个字段的规则
     * @param padding 所需的填充
     * @return 一个正确填充的新规则
     */
    protected NumberRule selectNumberRule(final int field, final int padding) {
        switch (padding) {
            case 1:
                return new UnpaddedNumberField(field);
            case 2:
                return new TwoDigitNumberField(field);
            default:
                return new PaddedNumberField(field, padding);
        }
    }

    /**
     * 格式化一个{@code Date}、{@code Calendar}或{@code Long}(毫秒)对象
     *
     * @param object 要格式化的对象
     * @return 格式化的值
     */
    String format(final Object object) {
        if (object instanceof Date) {
            return format((Date) object);
        } else if (object instanceof Calendar) {
            return format((Calendar) object);
        } else if (object instanceof Long) {
            return format(((Long) object).longValue());
        } else {
            throw new IllegalArgumentException("Unknown class: " + (null == object ? "<null>" : object.getClass().getName()));
        }
    }

    @Override
    public String format(final long millis) {
        final Calendar c = Calendar.getInstance(timeZone, locale);
        c.setTimeInMillis(millis);
        return applyRulesToString(c);
    }

    @Override
    public String format(final Date date) {
        final Calendar c = Calendar.getInstance(timeZone, locale);
        c.setTime(date);
        return applyRulesToString(c);
    }

    @Override
    public String format(final Calendar calendar) {
        return format(calendar, new StringBuilder(mMaxLengthEstimate)).toString();
    }

    @Override
    public <B extends Appendable> B format(final long millis, final B buf) {
        final Calendar c = Calendar.getInstance(timeZone, locale);
        c.setTimeInMillis(millis);
        return applyRules(c, buf);
    }

    @Override
    public <B extends Appendable> B format(final Date date, final B buf) {
        final Calendar c = Calendar.getInstance(timeZone, locale);
        c.setTime(date);
        return applyRules(c, buf);
    }

    @Override
    public <B extends Appendable> B format(Calendar calendar, final B buf) {
        // do not pass in calendar directly, this will cause TimeZone of FastDatePrinter to be ignored
        if (!calendar.getTimeZone().equals(timeZone)) {
            calendar = (Calendar) calendar.clone();
            calendar.setTimeZone(timeZone);
        }
        return applyRules(calendar, buf);
    }

    /**
     * 通过应用此打印机的规则创建给定日历的字符串表示形式
     *
     * @param c 把这些规则应用到日历上的人
     * @return 给定日历的字符串表示形式
     */
    private String applyRulesToString(final Calendar c) {
        return applyRules(c, new StringBuilder(mMaxLengthEstimate)).toString();
    }

    /**
     * 通过将规则应用于指定的日历来执行格式化
     *
     * @param calendar 要格式化的日历
     * @param buf      要格式化为的缓冲区
     * @param <B>      附加类类型，通常是StringBuilder或StringBuffer
     * @return 指定的字符串缓冲区
     */
    private <B extends Appendable> B applyRules(final Calendar calendar, final B buf) {
        try {
            for (final Rule rule : this.rules) {
                rule.appendTo(buf, calendar);
            }
        } catch (final IOException e) {
            throw new InternalException(e);
        }
        return buf;
    }

    /**
     * 估算生成的日期字符串长度
     * 实际生成的字符串长度小于或等于此值
     *
     * @return 日期字符串长度
     */
    public int getMaxLengthEstimate() {
        return mMaxLengthEstimate;
    }

    /**
     * 便捷获取 DateTimeFormatter
     * 由于 {@link FormatBuilder} 很大一部分的格式没有提供 {@link DateTimeFormatter},因此这里提供快捷获取方式
     *
     * @return DateTimeFormatter
     */
    public DateTimeFormatter getDateTimeFormatter() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(this.getPattern());
        if (this.getLocale() != null) {
            formatter = formatter.withLocale(this.getLocale());
        }
        if (this.getTimeZone() != null) {
            formatter = formatter.withZone(this.getTimeZone().toZoneId());
        }
        return formatter;
    }

    /**
     * 序列化后创建对象。此实现重新初始化瞬态属性
     *
     * @param in 对象被反序列化的ObjectInputStream
     * @throws IOException            如果有IO问题
     * @throws ClassNotFoundException 如果找不到类
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        init();
    }

    /**
     * 规则
     */
    private interface Rule {
        /**
         * 返回结果的估计长度
         *
         * @return 估计的长度
         */
        int estimateLength();

        /**
         * 根据规则实现将指定日历的值附加到输出缓冲区
         *
         * @param buf      输出缓冲区
         * @param calendar 日历追加
         * @throws IOException 如果发生I/O错误
         */
        void appendTo(Appendable buf, Calendar calendar) throws IOException;
    }

    /**
     * 定义数字规则的内部类
     */
    private interface NumberRule extends Rule {
        /**
         * 根据规则实现将指定的值附加到输出缓冲区
         *
         * @param buffer 输出缓冲区
         * @param value  要追加的值
         * @throws IOException 如果发生I/O错误
         */
        void appendTo(Appendable buffer, int value) throws IOException;
    }

    /**
     * 内部类来输出一个常量单个字符
     */
    private static class CharacterLiteral implements Rule {
        private final char mValue;

        /**
         * 构造一个新的{@code CharacterLiteral}实例来保存指定的值
         *
         * @param value 字符文字
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
     * 内部类来输出consts字符串
     */
    private static class StringLiteral implements Rule {
        private final String mValue;

        /**
         * 构造一个新的{@code StringLiteral}实例来保存指定的值
         *
         * @param value 字符串文字
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
     * 内部类来输出一组值中的一个
     */
    private static class TextField implements Rule {
        private final int mField;
        private final String[] mValues;

        /**
         * 使用指定的字段和值构造{@code TextField}的实例
         *
         * @param field  字段
         * @param values 字段值
         */
        TextField(final int field, final String[] values) {
            mField = field;
            mValues = values;
        }

        @Override
        public int estimateLength() {
            int max = 0;
            for (int i = mValues.length; --i >= 0; ) {
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
     * 内部类来输出未填充的数字
     */
    private static class UnpaddedNumberField implements NumberRule {
        private final int mField;

        /**
         * 使用指定的字段构造{@code UnpadedNumberField}的实例
         *
         * @param field 字段
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
                buffer.append((char) (value + Symbol.C_ZERO));
            } else if (value < 100) {
                appendDigits(buffer, value);
            } else {
                appendFullDigits(buffer, value, 1);
            }
        }
    }

    /**
     * 内部类来输出未填充的月份
     */
    private static class UnpaddedMonthField implements NumberRule {
        static final UnpaddedMonthField INSTANCE = new UnpaddedMonthField();

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
                buffer.append((char) (value + Symbol.C_ZERO));
            } else {
                appendDigits(buffer, value);
            }
        }
    }

    /**
     * 内部类来输出填充后的数字
     */
    private static class PaddedNumberField implements NumberRule {
        private final int mField;
        private final int mSize;

        /**
         * 构造{@code PaddedNumberField}的实例
         *
         * @param field 字段
         * @param size  输出字段的大小
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
     * 内部类来输出两位数的数字
     */
    private static class TwoDigitNumberField implements NumberRule {
        private final int mField;

        /**
         * 使用指定的字段构造{@code TwoDigitNumberField}的实例
         *
         * @param field 字段
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
     * 内部类来输出两位数的年份
     */
    private static class TwoDigitYearField implements NumberRule {
        static final TwoDigitYearField INSTANCE = new TwoDigitYearField();

        /**
         * 构造一个{@code TwoDigitYearField}的实例
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
     * 内部类来输出两位数的月份
     */
    private static class TwoDigitMonthField implements NumberRule {
        static final TwoDigitMonthField INSTANCE = new TwoDigitMonthField();

        /**
         * 构造{@code TwoDigitMonthField}的实例
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
     * 内部类来输出12小时字段
     */
    private static class TwelveHourField implements NumberRule {
        private final NumberRule mRule;

        /**
         * 使用指定的{@code NumberRule}构造{@code TwelveHourField}的实例
         *
         * @param rule 规则
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
     * 内部类来输出24小时字段
     */
    private static class TwentyFourHourField implements NumberRule {
        private final NumberRule mRule;

        /**
         * 使用指定的{@code NumberRule}构造{@code TwentyFourHourField}的实例
         *
         * @param rule 规则
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
     * 内部类来输出以周为单位的数字天
     */
    private static class DayInWeekField implements NumberRule {
        private final NumberRule mRule;

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
     * 内部类来输出以周为单位的数字天
     */
    private static class WeekYear implements NumberRule {
        private final NumberRule mRule;

        WeekYear(final NumberRule rule) {
            mRule = rule;
        }

        @Override
        public int estimateLength() {
            return mRule.estimateLength();
        }

        @Override
        public void appendTo(final Appendable buffer, final Calendar calendar) throws IOException {
            mRule.appendTo(buffer, calendar.getWeekYear());
        }

        @Override
        public void appendTo(final Appendable buffer, final int value) throws IOException {
            mRule.appendTo(buffer, value);
        }
    }

    /**
     * 内部类来输出时区名称
     */
    private static class TimeZoneNameRule implements Rule {
        private final Locale mLocale;
        private final int mStyle;
        private final String mStandard;
        private final String mDaylight;

        /**
         * 使用指定的属性构造{@code TimeZoneNameRule}的实例
         *
         * @param timeZone 时区
         * @param locale   语言环境
         * @param style    格式
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
     * 内部类以数字{@code +/-HHMM}或{@code +/-HH:MM}的形式输出时区
     */
    private static class TimeZoneNumberRule implements Rule {
        static final TimeZoneNumberRule INSTANCE_COLON = new TimeZoneNumberRule(true);
        static final TimeZoneNumberRule INSTANCE_NO_COLON = new TimeZoneNumberRule(false);

        final boolean mColon;

        /**
         * 使用指定的属性构造{@code TimeZoneNumberRule}的实例
         *
         * @param colon 如果{@code true}在输出中在HH和MM之间添加冒号
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
                buffer.append(Symbol.C_MINUS);
                offset = -offset;
            } else {
                buffer.append(Symbol.C_PLUS);
            }

            final int hours = offset / (60 * 60 * 1000);
            appendDigits(buffer, hours);

            if (mColon) {
                buffer.append(Symbol.C_COLON);
            }

            final int minutes = offset / (60 * 1000) - 60 * hours;
            appendDigits(buffer, minutes);
        }
    }

    /**
     * 内部类以数字{@code +/-HHMM}或{@code +/-HH:MM}的形式输出时区
     */
    private static class Iso8601_Rule implements Rule {

        static final Iso8601_Rule ISO8601_HOURS = new Iso8601_Rule(3);
        static final Iso8601_Rule ISO8601_HOURS_MINUTES = new Iso8601_Rule(5);
        static final Iso8601_Rule ISO8601_HOURS_COLON_MINUTES = new Iso8601_Rule(6);
        final int length;

        /**
         * 使用指定的属性构造{@code Iso8601_Rule}的实例
         *
         * @param length 输出中的字符数
         */
        Iso8601_Rule(final int length) {
            this.length = length;
        }

        /**
         * 工厂方法的Iso8601_Rules
         *
         * @param tokenLen 表示要格式化的时区字符串长度的令牌
         * @return 可以格式化长度为{@code tokenLen}的时区字符串的Iso8601_Rule
         * 如果不存在这样的规则，则抛出IllegalArgumentException
         */
        static Iso8601_Rule getRule(final int tokenLen) {
            switch (tokenLen) {
                case 1:
                    return Iso8601_Rule.ISO8601_HOURS;
                case 2:
                    return Iso8601_Rule.ISO8601_HOURS_MINUTES;
                case 3:
                    return Iso8601_Rule.ISO8601_HOURS_COLON_MINUTES;
                default:
                    throw new IllegalArgumentException("invalid number of X");
            }
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
                buffer.append(Symbol.C_MINUS);
                offset = -offset;
            } else {
                buffer.append(Symbol.C_PLUS);
            }

            final int hours = offset / (60 * 60 * 1000);
            appendDigits(buffer, hours);

            if (length < 5) {
                return;
            }

            if (length == 6) {
                buffer.append(Symbol.C_COLON);
            }

            final int minutes = offset / (60 * 1000) - 60 * hours;
            appendDigits(buffer, minutes);
        }
    }

    /**
     * 作为时区名称的复合键的内部类
     */
    private static class TimeZoneDisplayKey {
        private final TimeZone mTimeZone;
        private final int mStyle;
        private final Locale mLocale;

        /**
         * 使用指定的属性构造{@code TimeZoneDisplayKey}的实例
         *
         * @param timeZone 时区
         * @param daylight 如果{@code true}调整夏令时的样式
         * @param style    时区的格式
         * @param locale   时区的语言环境
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
        public boolean equals(final Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof TimeZoneDisplayKey) {
                final TimeZoneDisplayKey other = (TimeZoneDisplayKey) object;
                return mTimeZone.equals(other.mTimeZone) && mStyle == other.mStyle && mLocale.equals(other.mLocale);
            }
            return false;
        }
    }

}