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
package org.miaixz.bus.core.date.formatter.parser;

import org.miaixz.bus.core.date.formatter.AbstractMotd;
import org.miaixz.bus.core.date.formatter.FormatBuilder;
import org.miaixz.bus.core.lang.Symbol;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link java.text.SimpleDateFormat} 的线程安全版本
 * 用于解析日期字符串并转换为 {@link Date} 对象
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FastDateParser extends AbstractMotd implements PositionDateParser {

    private static final long serialVersionUID = 1L;

    private static final Locale JAPANESE_IMPERIAL = new Locale("ja", "JP", "JP");
    // 用来对正则表达式排序的比较器。('february' 在 'feb'之前)所有条目按区域设置必须是小写的
    private static final Comparator<String> LONGER_FIRST_LOWERCASE = Comparator.reverseOrder();
    private static final ConcurrentMap<Locale, Strategy>[] CACHES = new ConcurrentMap[Calendar.FIELD_COUNT];
    private static final Strategy ABBREVIATED_YEAR_STRATEGY = new NumberStrategy(Calendar.YEAR) {
        @Override
        int modify(final FastDateParser parser, final int iValue) {
            return iValue < 100 ? parser.adjustYear(iValue) : iValue;
        }
    };
    private static final Strategy NUMBER_MONTH_STRATEGY = new NumberStrategy(Calendar.MONTH) {
        @Override
        int modify(final FastDateParser parser, final int iValue) {
            return iValue - 1;
        }
    };
    private static final Strategy LITERAL_YEAR_STRATEGY = new NumberStrategy(Calendar.YEAR);
    private static final Strategy WEEK_OF_YEAR_STRATEGY = new NumberStrategy(Calendar.WEEK_OF_YEAR);
    private static final Strategy WEEK_OF_MONTH_STRATEGY = new NumberStrategy(Calendar.WEEK_OF_MONTH);
    private static final Strategy DAY_OF_YEAR_STRATEGY = new NumberStrategy(Calendar.DAY_OF_YEAR);
    private static final Strategy DAY_OF_MONTH_STRATEGY = new NumberStrategy(Calendar.DAY_OF_MONTH);
    private static final Strategy DAY_OF_WEEK_STRATEGY = new NumberStrategy(Calendar.DAY_OF_WEEK) {
        @Override
        int modify(final FastDateParser parser, final int iValue) {
            return iValue != 7 ? iValue + 1 : Calendar.SUNDAY;
        }
    };
    private static final Strategy DAY_OF_WEEK_IN_MONTH_STRATEGY = new NumberStrategy(Calendar.DAY_OF_WEEK_IN_MONTH);
    private static final Strategy HOUR_OF_DAY_STRATEGY = new NumberStrategy(Calendar.HOUR_OF_DAY);
    private static final Strategy HOUR24_OF_DAY_STRATEGY = new NumberStrategy(Calendar.HOUR_OF_DAY) {
        @Override
        int modify(final FastDateParser parser, final int iValue) {
            return iValue == 24 ? 0 : iValue;
        }
    };
    private static final Strategy HOUR12_STRATEGY = new NumberStrategy(Calendar.HOUR) {
        @Override
        int modify(final FastDateParser parser, final int iValue) {
            return iValue == 12 ? 0 : iValue;
        }
    };
    private static final Strategy HOUR_STRATEGY = new NumberStrategy(Calendar.HOUR);
    private static final Strategy MINUTE_STRATEGY = new NumberStrategy(Calendar.MINUTE);
    private static final Strategy SECOND_STRATEGY = new NumberStrategy(Calendar.SECOND);
    private static final Strategy MILLISECOND_STRATEGY = new NumberStrategy(Calendar.MILLISECOND);
    /**
     * 世纪：2000年前为19， 之后为20
     */
    private final int century;
    private final int startYear;
    // 导出字段
    private transient List<StrategyAndWidth> patterns;

    /**
     * 构造一个新的FastDateParser
     * 使用{@link FormatBuilder#getInstance(String, TimeZone, Locale)}
     * 或{@link FormatBuilder}工厂方法的另一种变体来获取缓存的FastDateParser实例
     *
     * @param pattern  非空{@link java.text.SimpleDateFormat} 兼容模式
     * @param timeZone 非空时区
     * @param locale   非空地区
     */
    public FastDateParser(final String pattern, final TimeZone timeZone, final Locale locale) {
        this(pattern, timeZone, locale, null);
    }

    /**
     * 构造一个新的FastDateParser
     *
     * @param pattern      非空{@link java.text.SimpleDateFormat} 兼容模式
     * @param timeZone     非空时区
     * @param locale       非空地区
     * @param centuryStart 本世纪初为2位数年解析
     */
    public FastDateParser(final String pattern, final TimeZone timeZone, final Locale locale, final Date centuryStart) {
        super(pattern, timeZone, locale);
        final Calendar definingCalendar = Calendar.getInstance(timeZone, locale);

        int centuryStartYear;
        if (null != centuryStart) {
            definingCalendar.setTime(centuryStart);
            centuryStartYear = definingCalendar.get(Calendar.YEAR);
        } else if (locale.equals(JAPANESE_IMPERIAL)) {
            centuryStartYear = 0;
        } else {
            // 从80年前到现在的20年
            definingCalendar.setTime(new Date());
            centuryStartYear = definingCalendar.get(Calendar.YEAR) - 80;
        }
        century = centuryStartYear / 100 * 100;
        startYear = centuryStartYear - century;

        init(definingCalendar);
    }

    private static boolean isFormatLetter(final char c) {
        return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z';
    }

    private static StringBuilder simpleQuote(final StringBuilder sb, final String value) {
        for (int i = 0; i < value.length(); ++i) {
            final char c = value.charAt(i);
            switch (c) {
                case Symbol.C_BACKSLASH:
                case Symbol.C_CARET:
                case Symbol.C_DOLLAR:
                case Symbol.C_DOT:
                case Symbol.C_OR:
                case Symbol.C_QUESTION_MARK:
                case Symbol.C_STAR:
                case Symbol.C_PLUS:
                case Symbol.C_PARENTHESE_LEFT:
                case Symbol.C_PARENTHESE_RIGHT:
                case Symbol.C_BRACKET_LEFT:
                case Symbol.C_BRACE_LEFT:
                    sb.append(Symbol.C_BACKSLASH);
                default:
                    sb.append(c);
            }
        }
        return sb;
    }

    /**
     * 获取显示字段的短值和长值
     *
     * @param cal    获取该日历的短值和长值
     * @param locale 显示名称的区域设置
     * @param field  字段信息
     * @param regex  要构建的正则表达式
     * @return 字符串的映射将名称显示为字段值
     */
    private static Map<String, Integer> appendDisplayNames(final Calendar cal, final Locale locale, final int field, final StringBuilder regex) {
        final Map<String, Integer> values = new HashMap<>();

        final Map<String, Integer> displayNames = cal.getDisplayNames(field, Calendar.ALL_STYLES, locale);
        final TreeSet<String> sorted = new TreeSet<>(LONGER_FIRST_LOWERCASE);
        for (final Map.Entry<String, Integer> displayName : displayNames.entrySet()) {
            final String key = displayName.getKey().toLowerCase(locale);
            if (sorted.add(key)) {
                values.put(key, displayName.getValue());
            }
        }
        for (final String symbol : sorted) {
            simpleQuote(regex, symbol).append(Symbol.C_OR);
        }
        return values;
    }

    /**
     * 获取特定字段的策略缓存
     *
     * @param field 日历字段
     * @return 区域设置到策略的缓存
     */
    private static ConcurrentMap<Locale, Strategy> getCache(final int field) {
        synchronized (CACHES) {
            if (null == CACHES[field]) {
                CACHES[field] = new ConcurrentHashMap<>(3);
            }
            return CACHES[field];
        }
    }

    /**
     * 从定义字段初始化派生字段。这是从构造函数和readObject(反序列化)中调用的
     *
     * @param definingCalendar {@link java.util.Calendar} 用于初始化此FastDateParser的日历
     */
    private void init(final Calendar definingCalendar) {
        patterns = new ArrayList<>();

        final StrategyParser fm = new StrategyParser(definingCalendar);
        for (; ; ) {
            final StrategyAndWidth field = fm.getNextStrategy();
            if (null == field) {
                break;
            }
            patterns.add(field);
        }
    }

    /**
     * 序列化后创建对象。 此实现重新初始化瞬态属性。
     *
     * @param in ObjectInputStream中的@param，将从中反序列化对象。
     * @throws IOException            如果存在IO问题，则抛出IOException。
     * @throws ClassNotFoundException ClassNotFoundException如果找不到一个类     
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        final Calendar definingCalendar = Calendar.getInstance(timeZone, locale);
        init(definingCalendar);
    }

    @Override
    public Date parse(final String source) throws ParseException {
        final ParsePosition pp = new ParsePosition(0);
        final Date date = parse(source, pp);
        if (null == date) {
            if (locale.equals(JAPANESE_IMPERIAL)) {
                throw new ParseException("(The " + locale + " locale does not support dates before 1868 AD)\n" + "Unparseable date: \"" + source, pp.getErrorIndex());
            }
            throw new ParseException("Unparseable date: " + source, pp.getErrorIndex());
        }
        return date;
    }

    @Override
    public Date parse(final String source, final ParsePosition pos) {
        // 时间测试表明获得新实例比克隆快19%
        final Calendar cal = Calendar.getInstance(timeZone, locale);
        cal.clear();

        return parse(source, pos, cal) ? cal.getTime() : null;
    }

    @Override
    public boolean parse(final String source, final ParsePosition pos, final Calendar calendar) {
        final ListIterator<StrategyAndWidth> lt = patterns.listIterator();
        while (lt.hasNext()) {
            final StrategyAndWidth strategyAndWidth = lt.next();
            final int maxWidth = strategyAndWidth.getMaxWidth(lt);
            if (false == strategyAndWidth.strategy.parse(this, calendar, source, pos, maxWidth)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 使用当前的世纪调整两位数年份为四位数年份
     *
     * @param twoDigitYear 两位数年份
     * @return 从centuryStart(inclusive)到centuryStart+100(exclusive)之间的值
     */
    private int adjustYear(final int twoDigitYear) {
        final int trial = century + twoDigitYear;
        return twoDigitYear >= startYear ? trial : trial + 100;
    }

    /**
     * 从SimpleDateFormat模式获取给定字段的策略
     *
     * @param f                SimpleDateFormat模式的子序列
     * @param width            长度
     * @param definingCalendar 获取短值和长值的日历
     * @return 负责现场解析的策略
     */
    private Strategy getStrategy(final char f, final int width, final Calendar definingCalendar) {
        switch (f) {
            default:
                throw new IllegalArgumentException("Format '" + f + "' not supported");
            case 'D':
                return DAY_OF_YEAR_STRATEGY;
            case 'E':
                return getLocaleSpecificStrategy(Calendar.DAY_OF_WEEK, definingCalendar);
            case 'F':
                return DAY_OF_WEEK_IN_MONTH_STRATEGY;
            case 'G':
                return getLocaleSpecificStrategy(Calendar.ERA, definingCalendar);
            case 'H': // Hour in day (0-23)
                return HOUR_OF_DAY_STRATEGY;
            case 'K': // Hour in am/pm (0-11)
                return HOUR_STRATEGY;
            case 'M':
                return width >= 3 ? getLocaleSpecificStrategy(Calendar.MONTH, definingCalendar) : NUMBER_MONTH_STRATEGY;
            case 'S':
                return MILLISECOND_STRATEGY;
            case 'W':
                return WEEK_OF_MONTH_STRATEGY;
            case 'a':
                return getLocaleSpecificStrategy(Calendar.AM_PM, definingCalendar);
            case 'd':
                return DAY_OF_MONTH_STRATEGY;
            case 'h': // Hour in am/pm (1-12), i.e. midday/midnight is 12, not 0
                return HOUR12_STRATEGY;
            case 'k': // Hour in day (1-24), i.e. midnight is 24, not 0
                return HOUR24_OF_DAY_STRATEGY;
            case 'm':
                return MINUTE_STRATEGY;
            case 's':
                return SECOND_STRATEGY;
            case 'u':
                return DAY_OF_WEEK_STRATEGY;
            case 'w':
                return WEEK_OF_YEAR_STRATEGY;
            case 'y':
            case 'Y':
                return width > 2 ? LITERAL_YEAR_STRATEGY : ABBREVIATED_YEAR_STRATEGY;
            case 'X':
                return ISO8601TimeZoneStrategy.getStrategy(width);
            case 'Z':
                if (width == 2) {
                    return ISO8601TimeZoneStrategy.ISO_8601_3_STRATEGY;
                }
            case 'z':
                return getLocaleSpecificStrategy(Calendar.ZONE_OFFSET, definingCalendar);
        }
    }

    /**
     * 构建解析文本字段的策略
     *
     * @param field            日历字段
     * @param definingCalendar 获取短值和长值的日历
     * @return 字段和语言环境的TextStrategy
     */
    private Strategy getLocaleSpecificStrategy(final int field, final Calendar definingCalendar) {
        final ConcurrentMap<Locale, Strategy> cache = getCache(field);
        Strategy strategy = cache.get(locale);
        if (null == strategy) {
            strategy = field == Calendar.ZONE_OFFSET ? new TimeZoneStrategy(locale) : new CaseInsensitiveTextStrategy(field, definingCalendar, locale);
            final Strategy inCache = cache.putIfAbsent(locale, strategy);
            if (null != inCache) {
                return inCache;
            }
        }
        return strategy;
    }

    /**
     * 持有规则和宽度
     */
    private static class StrategyAndWidth {
        final Strategy strategy;
        final int width;

        StrategyAndWidth(final Strategy strategy, final int width) {
            this.strategy = strategy;
            this.width = width;
        }

        int getMaxWidth(final ListIterator<StrategyAndWidth> lt) {
            if (!strategy.isNumber() || !lt.hasNext()) {
                return 0;
            }
            final Strategy nextStrategy = lt.next().strategy;
            lt.previous();
            return nextStrategy.isNumber() ? width : 0;
        }
    }

    /**
     * 单个日期字段的分析策略
     */
    private static abstract class Strategy {
        /**
         * 这个字段是否为数组,默认实现返回false
         *
         * @return 如果字段是一个数字，则为真
         */
        boolean isNumber() {
            return false;
        }

        abstract boolean parse(FastDateParser parser, Calendar calendar, String source, ParsePosition pos, int maxWidth);
    }

    /**
     * 从解析模式解析单个字段的策略
     */
    private static abstract class PatternStrategy extends Strategy {

        private Pattern pattern;

        void createPattern(final StringBuilder regex) {
            createPattern(regex.toString());
        }

        void createPattern(final String regex) {
            this.pattern = Pattern.compile(regex);
        }

        @Override
        boolean isNumber() {
            return false;
        }

        @Override
        boolean parse(final FastDateParser parser, final Calendar calendar, final String source, final ParsePosition pos, final int maxWidth) {
            final Matcher matcher = pattern.matcher(source.substring(pos.getIndex()));
            if (!matcher.lookingAt()) {
                pos.setErrorIndex(pos.getIndex());
                return false;
            }
            pos.setIndex(pos.getIndex() + matcher.end(1));
            setCalendar(parser, calendar, matcher.group(1));
            return true;
        }

        abstract void setCalendar(FastDateParser parser, Calendar cal, String value);
    }

    /**
     * 在解析模式中复制静态字段或带引号字段的策略
     */
    private static class CopyQuotedStrategy extends Strategy {

        final private String formatField;

        /**
         * 构造一个确保formatField具有文本的策略
         *
         * @param formatField 要匹配的文字
         */
        CopyQuotedStrategy(final String formatField) {
            this.formatField = formatField;
        }

        @Override
        boolean isNumber() {
            return false;
        }

        @Override
        boolean parse(final FastDateParser parser, final Calendar calendar, final String source, final ParsePosition pos, final int maxWidth) {
            for (int idx = 0; idx < formatField.length(); ++idx) {
                final int sIdx = idx + pos.getIndex();
                if (sIdx == source.length()) {
                    pos.setErrorIndex(sIdx);
                    return false;
                }
                if (formatField.charAt(idx) != source.charAt(sIdx)) {
                    pos.setErrorIndex(sIdx);
                    return false;
                }
            }
            pos.setIndex(formatField.length() + pos.getIndex());
            return true;
        }
    }

    /**
     * 一种处理解析模式中的文本字段的策略
     */
    private static class CaseInsensitiveTextStrategy extends PatternStrategy {
        final Locale locale;
        private final int field;
        private final Map<String, Integer> lKeyValues;

        /**
         * 构建解析文本字段的策略
         *
         * @param field            日历字段
         * @param definingCalendar 要使用的日历
         * @param locale           要使用的语言环境
         */
        CaseInsensitiveTextStrategy(final int field, final Calendar definingCalendar, final Locale locale) {
            this.field = field;
            this.locale = locale;

            final StringBuilder regex = new StringBuilder();
            regex.append("((?iu)");
            lKeyValues = appendDisplayNames(definingCalendar, locale, field, regex);
            regex.setLength(regex.length() - 1);
            regex.append(Symbol.PARENTHESE_RIGHT);
            createPattern(regex);
        }

        @Override
        void setCalendar(final FastDateParser parser, final Calendar cal, final String value) {
            final Integer iVal = lKeyValues.get(value.toLowerCase(locale));
            cal.set(field, iVal);
        }
    }

    /**
     * 处理解析模式中数字字段的策略
     */
    private static class NumberStrategy extends Strategy {
        private final int field;

        NumberStrategy(final int field) {
            this.field = field;
        }

        @Override
        boolean isNumber() {
            return true;
        }

        @Override
        boolean parse(final FastDateParser parser, final Calendar calendar, final String source, final ParsePosition pos, final int maxWidth) {
            int idx = pos.getIndex();
            int last = source.length();

            if (maxWidth == 0) {
                for (; idx < last; ++idx) {
                    final char c = source.charAt(idx);
                    if (!Character.isWhitespace(c)) {
                        break;
                    }
                }
                pos.setIndex(idx);
            } else {
                final int end = idx + maxWidth;
                if (last > end) {
                    last = end;
                }
            }

            for (; idx < last; ++idx) {
                final char c = source.charAt(idx);
                if (!Character.isDigit(c)) {
                    break;
                }
            }

            if (pos.getIndex() == idx) {
                pos.setErrorIndex(idx);
                return false;
            }

            final int value = Integer.parseInt(source.substring(pos.getIndex(), idx));
            pos.setIndex(idx);

            calendar.set(field, modify(parser, value));
            return true;
        }

        /**
         * 对解析的整数进行任何修改
         *
         * @param parser 解析器
         * @param iValue 解析的整数
         * @return 修改后的值
         */
        int modify(final FastDateParser parser, final int iValue) {
            return iValue;
        }

    }

    /**
     * 在解析模式中处理时区字段的策略
     */
    static class TimeZoneStrategy extends PatternStrategy {
        private static final String RFC_822_TIME_ZONE = "[+-]\\d{4}";
        private static final String UTC_TIME_ZONE_WITH_OFFSET = "[+-]\\d{2}:\\d{2}";
        private static final String GMT_OPTION = "GMT[+-]\\d{1,2}:\\d{2}";

        private static final int ID = 0;
        private final Locale locale;
        private final Map<String, TzInfo> tzNames = new HashMap<>();

        /**
         * 构建一个解析时区的策略
         *
         * @param locale 语言环境
         */
        TimeZoneStrategy(final Locale locale) {
            this.locale = locale;

            final StringBuilder sb = new StringBuilder();
            sb.append("((?iu)" + RFC_822_TIME_ZONE + Symbol.OR + UTC_TIME_ZONE_WITH_OFFSET + Symbol.OR + GMT_OPTION);

            final Set<String> sorted = new TreeSet<>(LONGER_FIRST_LOWERCASE);

            final String[][] zones = DateFormatSymbols.getInstance(locale).getZoneStrings();
            for (final String[] zoneNames : zones) {
                // 偏移量0是时区ID，没有本地化
                final String tzId = zoneNames[ID];
                if ("GMT".equalsIgnoreCase(tzId)) {
                    continue;
                }
                final TimeZone tz = TimeZone.getTimeZone(tzId);
                final TzInfo standard = new TzInfo(tz, false);
                TzInfo tzInfo = standard;
                for (int i = 1; i < zoneNames.length; ++i) {
                    switch (i) {
                        // 偏移量3是夏令时(或夏季)的名称
                        // offset 4是较短的夏季时间名称
                        case 3:
                            tzInfo = new TzInfo(tz, true);
                            break;
                        // 偏移量5开始额外的名称，可能是标准时间
                        case 5:
                            tzInfo = standard;
                            break;
                    }
                    if (null != zoneNames[i]) {
                        final String key = zoneNames[i].toLowerCase(locale);
                        // 忽略附加名称中提供的与重复项关联的数据
                        if (sorted.add(key)) {
                            tzNames.put(key, tzInfo);
                        }
                    }
                }
            }
            // 将具有更长的字符串的regex替换排序，贪婪匹配将确保使用最长的字符串
            for (final String zoneName : sorted) {
                simpleQuote(sb.append(Symbol.C_OR), zoneName);
            }
            sb.append(Symbol.PARENTHESE_RIGHT);
            createPattern(sb);
        }

        @Override
        void setCalendar(final FastDateParser parser, final Calendar cal, final String value) {
            if (value.charAt(0) == Symbol.C_PLUS || value.charAt(0) == Symbol.C_MINUS) {
                final TimeZone tz = TimeZone.getTimeZone("GMT" + value);
                cal.setTimeZone(tz);
            } else if (value.regionMatches(true, 0, "GMT", 0, 3)) {
                final TimeZone tz = TimeZone.getTimeZone(value.toUpperCase());
                cal.setTimeZone(tz);
            } else {
                final TzInfo tzInfo = tzNames.get(value.toLowerCase(locale));
                cal.set(Calendar.DST_OFFSET, tzInfo.dstOffset);
                cal.set(Calendar.ZONE_OFFSET, parser.getTimeZone().getRawOffset());
            }
        }

        private static class TzInfo {
            TimeZone zone;
            int dstOffset;

            TzInfo(final TimeZone tz, final boolean useDst) {
                zone = tz;
                dstOffset = useDst ? tz.getDSTSavings() : 0;
            }
        }
    }

    private static class ISO8601TimeZoneStrategy extends PatternStrategy {

        private static final Strategy ISO_8601_1_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}))");
        private static final Strategy ISO_8601_2_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}\\d{2}))");
        private static final Strategy ISO_8601_3_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}(?::)\\d{2}))");

        /**
         * 构建解析时区的策略
         *
         * @param pattern 正则表达式
         */
        ISO8601TimeZoneStrategy(final String pattern) {
            createPattern(pattern);
        }

        /**
         * SO8601TimeZoneStrategies的工厂方法。
         *
         * @param tokenLen 一个令牌，指示要格式化的TimeZone字符串的长度。
         * @return 可以格式化长度为{@code tokenLen}的TimeZone字符串的ISO8601TimeZoneStrategy。 如果不存在这样的策略，则将引发IllegalArgumentException。
         */
        static Strategy getStrategy(final int tokenLen) {
            switch (tokenLen) {
                case 1:
                    return ISO_8601_1_STRATEGY;
                case 2:
                    return ISO_8601_2_STRATEGY;
                case 3:
                    return ISO_8601_3_STRATEGY;
                default:
                    throw new IllegalArgumentException("invalid number of X");
            }
        }

        @Override
        void setCalendar(final FastDateParser parser, final Calendar cal, final String value) {
            if (Objects.equals(value, "Z")) {
                cal.setTimeZone(TimeZone.getTimeZone("UTC"));
            } else {
                cal.setTimeZone(TimeZone.getTimeZone("GMT" + value));
            }
        }
    }

    /**
     * 将格式解析为策略
     */
    private class StrategyParser {
        final private Calendar definingCalendar;
        private int currentIdx;

        StrategyParser(final Calendar definingCalendar) {
            this.definingCalendar = definingCalendar;
        }

        StrategyAndWidth getNextStrategy() {
            if (currentIdx >= pattern.length()) {
                return null;
            }

            final char c = pattern.charAt(currentIdx);
            if (isFormatLetter(c)) {
                return letterPattern(c);
            }
            return literal();
        }

        private StrategyAndWidth letterPattern(final char c) {
            final int begin = currentIdx;
            while (++currentIdx < pattern.length()) {
                if (pattern.charAt(currentIdx) != c) {
                    break;
                }
            }

            final int width = currentIdx - begin;
            return new StrategyAndWidth(getStrategy(c, width, definingCalendar), width);
        }

        private StrategyAndWidth literal() {
            boolean activeQuote = false;

            final StringBuilder sb = new StringBuilder();
            while (currentIdx < pattern.length()) {
                final char c = pattern.charAt(currentIdx);
                if (!activeQuote && isFormatLetter(c)) {
                    break;
                } else if (c == Symbol.C_SINGLE_QUOTE && (++currentIdx == pattern.length()
                        || pattern.charAt(currentIdx) != Symbol.C_SINGLE_QUOTE)) {
                    activeQuote = !activeQuote;
                    continue;
                }
                ++currentIdx;
                sb.append(c);
            }

            if (activeQuote) {
                throw new IllegalArgumentException("Unterminated quote");
            }

            final String formatField = sb.toString();
            return new StrategyAndWidth(new CopyQuotedStrategy(formatField), formatField.length());
        }
    }

}
