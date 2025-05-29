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
package org.miaixz.bus.core.center.date.format.parser;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.text.DateFormatSymbols;
import java.text.ParsePosition;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.center.date.format.FormatBuilder;
import org.miaixz.bus.core.center.date.printer.FastDatePrinter;
import org.miaixz.bus.core.center.date.printer.SimpleDatePrinter;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.DateException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 线程安全的日期解析器，替代 {@link java.text.SimpleDateFormat}，用于将日期字符串转换为 {@link Date} 对象。
 *
 * @author Kimi Liu
 * @see FastDatePrinter
 * @since Java 17+
 */
public class FastDateParser extends SimpleDatePrinter implements PositionDateParser {

    @Serial
    private static final long serialVersionUID = 2852291750285L;

    /**
     * 日本历法地区
     */
    private static final Locale JAPANESE_IMPERIAL = new Locale("ja", "JP", "JP");

    /**
     * 按长度从长到短排序的比较器，用于正则表达式选项
     */
    private static final Comparator<String> LONGER_FIRST_LOWERCASE = Comparator.reverseOrder();

    /**
     * 策略缓存数组，按日历字段索引
     */
    private static final ConcurrentMap<Locale, Strategy>[] CACHES = new ConcurrentMap[Calendar.FIELD_COUNT];

    /**
     * 解析两位年份的策略，自动调整为四位年份
     */
    private static final Strategy ABBREVIATED_YEAR_STRATEGY = new NumberStrategy(Calendar.YEAR) {
        @Override
        int modify(final FastDateParser parser, final int iValue) {
            return iValue < 100 ? parser.adjustYear(iValue) : iValue;
        }
    };

    /**
     * 解析月份数字的策略，月份值减1以符合Calendar标准
     */
    private static final Strategy NUMBER_MONTH_STRATEGY = new NumberStrategy(Calendar.MONTH) {
        @Override
        int modify(final FastDateParser parser, final int iValue) {
            return iValue - 1;
        }
    };

    /**
     * 解析完整年份的策略
     */
    private static final Strategy LITERAL_YEAR_STRATEGY = new NumberStrategy(Calendar.YEAR);

    /**
     * 解析一年中第几周的策略
     */
    private static final Strategy WEEK_OF_YEAR_STRATEGY = new NumberStrategy(Calendar.WEEK_OF_YEAR);

    /**
     * 解析一月中第几周的策略
     */
    private static final Strategy WEEK_OF_MONTH_STRATEGY = new NumberStrategy(Calendar.WEEK_OF_MONTH);

    /**
     * 解析一年中第几天的策略
     */
    private static final Strategy DAY_OF_YEAR_STRATEGY = new NumberStrategy(Calendar.DAY_OF_YEAR);

    /**
     * 解析一个月中第几天的策略
     */
    private static final Strategy DAY_OF_MONTH_STRATEGY = new NumberStrategy(Calendar.DAY_OF_MONTH);

    /**
     * 解析一周中第几天的策略，调整值为Calendar标准
     */
    private static final Strategy DAY_OF_WEEK_STRATEGY = new NumberStrategy(Calendar.DAY_OF_WEEK) {
        @Override
        int modify(final FastDateParser parser, final int iValue) {
            return iValue != 7 ? iValue + 1 : Calendar.SUNDAY;
        }
    };

    /**
     * 解析一月中第几周的策略
     */
    private static final Strategy DAY_OF_WEEK_IN_MONTH_STRATEGY = new NumberStrategy(Calendar.DAY_OF_WEEK_IN_MONTH);

    /**
     * 解析24小时制（0-23）的策略
     */
    private static final Strategy HOUR_OF_DAY_STRATEGY = new NumberStrategy(Calendar.HOUR_OF_DAY);

    /**
     * 解析24小时制（1-24）的策略，24转换为0
     */
    private static final Strategy HOUR24_OF_DAY_STRATEGY = new NumberStrategy(Calendar.HOUR_OF_DAY) {
        @Override
        int modify(final FastDateParser parser, final int iValue) {
            return iValue == 24 ? 0 : iValue;
        }
    };

    /**
     * 解析12小时制（1-12）的策略，12转换为0
     */
    private static final Strategy HOUR12_STRATEGY = new NumberStrategy(Calendar.HOUR) {
        @Override
        int modify(final FastDateParser parser, final int iValue) {
            return iValue == 12 ? 0 : iValue;
        }
    };

    /**
     * 解析12小时制（0-11）的策略
     */
    private static final Strategy HOUR_STRATEGY = new NumberStrategy(Calendar.HOUR);

    /**
     * 解析分钟的策略
     */
    private static final Strategy MINUTE_STRATEGY = new NumberStrategy(Calendar.MINUTE);

    /**
     * 解析秒的策略
     */
    private static final Strategy SECOND_STRATEGY = new NumberStrategy(Calendar.SECOND);

    /**
     * 解析毫秒的策略
     */
    private static final Strategy MILLISECOND_STRATEGY = new NumberStrategy(Calendar.MILLISECOND);

    /**
     * 当前世纪值（如2000年前为19，之后为20）
     */
    private final int century;

    /**
     * 世纪起始年份
     */
    private final int startYear;

    /**
     * 解析策略和宽度的列表
     */
    private transient List<StrategyAndWidth> list;

    /**
     * 构造FastDateParser实例，推荐使用{@link FormatBuilder}的工厂方法获取缓存实例。
     *
     * @param pattern  非空的{@link java.text.SimpleDateFormat}兼容格式
     * @param timeZone 非空时区
     * @param locale   非空地域
     */
    public FastDateParser(final String pattern, final TimeZone timeZone, final Locale locale) {
        this(pattern, timeZone, locale, null);
    }

    /**
     * 构造FastDateParser实例，允许指定世纪起始时间。
     *
     * @param pattern      非空的{@link java.text.SimpleDateFormat}兼容格式
     * @param timeZone     非空时区
     * @param locale       非空地域
     * @param centuryStart 两位年份解析的世纪起始时间
     */
    public FastDateParser(final String pattern, final TimeZone timeZone, final Locale locale, final Date centuryStart) {
        super(pattern, timeZone, locale);
        final Calendar definingCalendar = Calendar.getInstance(timeZone, locale);

        final int centuryStartYear;
        if (centuryStart != null) {
            definingCalendar.setTime(centuryStart);
            centuryStartYear = definingCalendar.get(Calendar.YEAR);
        } else if (locale.equals(JAPANESE_IMPERIAL)) {
            centuryStartYear = 0;
        } else {
            definingCalendar.setTime(new Date());
            centuryStartYear = definingCalendar.get(Calendar.YEAR) - 80;
        }
        century = centuryStartYear / 100 * 100;
        startYear = centuryStartYear - century;

        init(definingCalendar);
    }

    /**
     * 检查字符是否为格式化字母（A-Z或a-z）。
     *
     * @param c 字符
     * @return 是否为格式化字母
     */
    private static boolean isFormatLetter(final char c) {
        return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z';
    }

    /**
     * 将字符串追加到正则表达式中，处理特殊字符转义。
     *
     * @param sb    正则表达式构建器
     * @param value 要追加的字符串
     * @return 正则表达式构建器
     */
    private static StringBuilder simpleQuote(final StringBuilder sb, final String value) {
        for (int i = 0; i < value.length(); ++i) {
            final char c = value.charAt(i);
            switch (c) {
                case '\\':
                case '^':
                case Symbol.C_DOLLAR:
                case '.':
                case '|':
                case '?':
                case Symbol.C_STAR:
                case Symbol.C_PLUS:
                case Symbol.C_PARENTHESE_LEFT:
                case ')':
                case '[':
                case '{':
                    sb.append('\\');
                default:
                    sb.append(c);
            }
        }
        return sb;
    }

    /**
     * 获取日历字段的短名称和长名称。
     *
     * @param cal    日历对象
     * @param locale 地域
     * @param field  日历字段
     * @param regex  正则表达式构建器
     * @return 名称到字段值的映射
     */
    private static Map<String, Integer> appendDisplayNames(final Calendar cal, final Locale locale, final int field,
                                                           final StringBuilder regex) {
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
            simpleQuote(regex, symbol).append('|');
        }
        return values;
    }

    /**
     * 获取指定日历字段的策略缓存。
     *
     * @param field 日历字段
     * @return 地域到策略的缓存
     */
    private static ConcurrentMap<Locale, Strategy> getCache(final int field) {
        synchronized (CACHES) {
            if (CACHES[field] == null) {
                CACHES[field] = new ConcurrentHashMap<>(3);
            }
            return CACHES[field];
        }
    }

    /**
     * 初始化派生字段，由构造函数和反序列化调用。
     *
     * @param definingCalendar 用于初始化的日历对象
     */
    private void init(final Calendar definingCalendar) {
        list = new ArrayList<>();
        final StrategyParser fm = new StrategyParser(definingCalendar);
        for (;;) {
            final StrategyAndWidth field = fm.getNextStrategy();
            if (field == null) {
                break;
            }
            list.add(field);
        }
    }

    /**
     * 反序列化后创建对象，重新初始化瞬态属性。
     *
     * @param in 输入流
     * @throws IOException            如果发生I/O错误
     * @throws ClassNotFoundException 如果类未找到
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        final Calendar definingCalendar = Calendar.getInstance(timeZone, locale);
        init(definingCalendar);
    }

    /**
     * 解析日期字符串。
     *
     * @param source 日期字符串
     * @return 解析后的日期对象
     * @throws DateException 如果解析失败
     */
    @Override
    public Date parse(final CharSequence source) throws DateException {
        final ParsePosition pp = new ParsePosition(0);
        final Date date = parse(source, pp);
        if (date == null) {
            if (locale.equals(JAPANESE_IMPERIAL)) {
                throw new DateException("(The " + locale + " locale does not support dates before 1868 AD) " +
                        "Unparseable date: \"" + source, pp.getErrorIndex());
            }
            throw new DateException("Unparseable date: " + source, pp.getErrorIndex());
        }
        return date;
    }

    /**
     * 解析日期字符串到日历对象。
     *
     * @param source   日期字符串
     * @param pos     解析位置
     * @param calendar 日历对象
     * @return 是否解析成功
     */
    @Override
    public boolean parse(final CharSequence source, ParsePosition pos, final Calendar calendar) {
        if (null == pos) {
            pos = new ParsePosition(0);
        }
        final ListIterator<StrategyAndWidth> lt = list.listIterator();
        while (lt.hasNext()) {
            final StrategyAndWidth strategyAndWidth = lt.next();
            final int maxWidth = strategyAndWidth.getMaxWidth(lt);
            if (!strategyAndWidth.strategy.parse(this, calendar, source, pos, maxWidth)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 使用当前世纪将两位年份调整为四位年份。
     *
     * @param twoDigitYear 两位年份
     * @return 调整后的四位年份
     */
    private int adjustYear(final int twoDigitYear) {
        final int trial = century + twoDigitYear;
        return twoDigitYear >= startYear ? trial : trial + 100;
    }

    /**
     * 根据格式字符获取解析策略。
     *
     * @param f                格式字符
     * @param width            字段宽度
     * @param definingCalendar 日历对象
     * @return 解析策略
     */
    private Strategy getStrategy(final char f, final int width, final Calendar definingCalendar) {
        switch (f) {
            case 'D':
                return DAY_OF_YEAR_STRATEGY;
            case 'E':
                return getLocaleSpecificStrategy(Calendar.DAY_OF_WEEK, definingCalendar);
            case 'F':
                return DAY_OF_WEEK_IN_MONTH_STRATEGY;
            case 'G':
                return getLocaleSpecificStrategy(Calendar.ERA, definingCalendar);
            case 'H':
                return HOUR_OF_DAY_STRATEGY;
            case 'K':
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
            case 'h':
                return HOUR12_STRATEGY;
            case 'k':
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
            default:
                throw new IllegalArgumentException("Format '" + f + "' not supported");
        }
    }

    /**
     * 获取特定地域的解析策略。
     *
     * @param field            日历字段
     * @param definingCalendar 日历对象
     * @return 解析策略
     */
    private Strategy getLocaleSpecificStrategy(final int field, final Calendar definingCalendar) {
        final ConcurrentMap<Locale, Strategy> cache = getCache(field);
        Strategy strategy = cache.get(locale);
        if (strategy == null) {
            strategy = field == Calendar.ZONE_OFFSET ? new TimeZoneStrategy(locale)
                    : new CaseInsensitiveTextStrategy(field, definingCalendar, locale);
            final Strategy inCache = cache.putIfAbsent(locale, strategy);
            if (inCache != null) {
                return inCache;
            }
        }
        return strategy;
    }

    /**
     * 日期字段解析策略接口。
     */
    interface Strategy {
        /**
         * 解析日期字段。
         *
         * @param parser    解析器
         * @param calendar  日历对象
         * @param source    源字符串
         * @param pos       解析位置
         * @param maxWidth  最大宽度
         * @return 是否解析成功
         */
        boolean parse(FastDateParser parser, Calendar calendar, CharSequence source, ParsePosition pos, int maxWidth);

        /**
         * 是否为数字字段。
         *
         * @return 是否为数字字段
         */
        default boolean isNumber() {
            return false;
        }
    }

    /**
     * 存储解析策略和字段宽度的类。
     */
    private static class StrategyAndWidth {
        /**
         * 解析策略
         */
        final Strategy strategy;
        /**
         * 字段宽度
         */
        final int width;

        /**
         * 构造StrategyAndWidth实例。
         *
         * @param strategy 解析策略
         * @param width    字段宽度
         */
        StrategyAndWidth(final Strategy strategy, final int width) {
            this.strategy = strategy;
            this.width = width;
        }

        /**
         * 获取最大宽度。
         *
         * @param lt 策略迭代器
         * @return 最大宽度
         */
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
     * 解析单一字段的策略。
     */
    private static abstract class PatternStrategy implements Strategy {
        /**
         * 正则表达式模式
         */
        private Pattern pattern;

        /**
         * 创建正则表达式模式。
         *
         * @param regex 正则表达式
         */
        void createPattern(final StringBuilder regex) {
            createPattern(regex.toString());
        }

        /**
         * 创建正则表达式模式。
         *
         * @param regex 正则表达式字符串
         */
        void createPattern(final String regex) {
            this.pattern = Pattern.compile(regex);
        }

        /**
         * 解析字段值。
         *
         * @param parser    解析器
         * @param calendar  日历对象
         * @param source    源字符串
         * @param pos       解析位置
         * @param maxWidth  最大宽度
         * @return 是否解析成功
         */
        @Override
        public boolean parse(final FastDateParser parser, final Calendar calendar, final CharSequence source,
                             final ParsePosition pos, final int maxWidth) {
            final Matcher matcher = pattern.matcher(source.subSequence(pos.getIndex(), source.length()));
            if (!matcher.lookingAt()) {
                pos.setErrorIndex(pos.getIndex());
                return false;
            }
            pos.setIndex(pos.getIndex() + matcher.end(1));
            setCalendar(parser, calendar, matcher.group(1));
            return true;
        }

        /**
         * 设置日历字段值。
         *
         * @param parser 解析器
         * @param cal    日历对象
         * @param value  字段值
         */
        abstract void setCalendar(FastDateParser parser, Calendar cal, String value);
    }

    /**
     * 复制格式化模式中的静态或引用字段的策略。
     */
    private static class CopyQuotedStrategy implements Strategy {
        /**
         * 格式化字段的字面文本
         */
        final private String formatField;

        /**
         * 构造CopyQuotedStrategy实例。
         *
         * @param formatField 要匹配的字面文本
         */
        CopyQuotedStrategy(final String formatField) {
            this.formatField = formatField;
        }

        /**
         * 解析字面文本字段。
         *
         * @param parser    解析器
         * @param calendar  日历对象
         * @param source    源字符串
         * @param pos       解析位置
         * @param maxWidth  最大宽度
         * @return 是否解析成功
         */
        @Override
        public boolean parse(final FastDateParser parser, final Calendar calendar, final CharSequence source,
                             final ParsePosition pos, final int maxWidth) {
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
     * 解析文本字段的策略（不区分大小写）。
     */
    private static class CaseInsensitiveTextStrategy extends PatternStrategy {
        /**
         * 日历字段
         */
        final int field;
        /**
         * 地域
         */
        final Locale locale;
        /**
         * 字段值映射（小写键）
         */
        private final Map<String, Integer> lKeyValues;

        /**
         * 构造CaseInsensitiveTextStrategy实例。
         *
         * @param field            日历字段
         * @param definingCalendar 日历对象
         * @param locale           地域
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

        /**
         * 设置日历字段值。
         *
         * @param parser 解析器
         * @param cal    日历对象
         * @param value  字段值
         */
        @Override
        void setCalendar(final FastDateParser parser, final Calendar cal, final String value) {
            final Integer iVal = lKeyValues.get(value.toLowerCase(locale));
            cal.set(field, iVal);
        }
    }

    /**
     * 解析数字字段的策略。
     */
    private static class NumberStrategy implements Strategy {
        /**
         * 日历字段
         */
        private final int field;

        /**
         * 构造NumberStrategy实例。
         *
         * @param field 日历字段
         */
        NumberStrategy(final int field) {
            this.field = field;
        }

        /**
         * 是否为数字字段。
         *
         * @return 始终返回true
         */
        @Override
        public boolean isNumber() {
            return true;
        }

        /**
         * 解析数字字段。
         *
         * @param parser    解析器
         * @param calendar  日历对象
         * @param source    源字符串
         * @param pos       解析位置
         * @param maxWidth  最大宽度
         * @return 是否解析成功
         */
        @Override
        public boolean parse(final FastDateParser parser, final Calendar calendar, final CharSequence source,
                             final ParsePosition pos, final int maxWidth) {
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
            final int value = Integer.parseInt(StringKit.sub(source, pos.getIndex(), idx));
            pos.setIndex(idx);
            calendar.set(field, modify(parser, value));
            return true;
        }

        /**
         * 修改解析的数字值。
         *
         * @param parser 解析器
         * @param iValue 解析的数字
         * @return 修改后的值
         */
        int modify(final FastDateParser parser, final int iValue) {
            return iValue;
        }
    }

    /**
     * 解析时区字段的策略。
     */
    static class TimeZoneStrategy extends PatternStrategy {
        /**
         * RFC 822时区格式正则
         */
        private static final String RFC_822_TIME_ZONE = "[+-]\\d{4}";
        /**
         * UTC带偏移的时区格式正则
         */
        private static final String UTC_TIME_ZONE_WITH_OFFSET = "[+-]\\d{2}:\\d{2}";
        /**
         * GMT选项正则
         */
        private static final String GMT_OPTION = "GMT[+-]\\d{1,2}:\\d{2}";
        /**
         * 时区ID索引
         */
        private static final int ID = 0;
        /**
         * 地域
         */
        private final Locale locale;
        /**
         * 时区名称到信息的映射
         */
        private final Map<String, TzInfo> tzNames = new HashMap<>();

        /**
         * 构造TimeZoneStrategy实例。
         *
         * @param locale 地域
         */
        TimeZoneStrategy(final Locale locale) {
            this.locale = locale;
            final StringBuilder sb = new StringBuilder();
            sb.append("((?iu)" + RFC_822_TIME_ZONE + "|" + UTC_TIME_ZONE_WITH_OFFSET + "|" + GMT_OPTION);
            final Set<String> sorted = new TreeSet<>(LONGER_FIRST_LOWERCASE);
            final String[][] zones = DateFormatSymbols.getInstance(locale).getZoneStrings();
            for (final String[] zoneNames : zones) {
                final String tzId = zoneNames[ID];
                if ("GMT".equalsIgnoreCase(tzId)) {
                    continue;
                }
                final TimeZone tz = TimeZone.getTimeZone(tzId);
                final TzInfo standard = new TzInfo(tz, false);
                TzInfo tzInfo = standard;
                for (int i = 1; i < zoneNames.length; ++i) {
                    tzInfo = switch (i) {
                        case 3 -> new TzInfo(tz, true);
                        case 5 -> standard;
                        default -> tzInfo;
                    };
                    if (zoneNames[i] != null) {
                        final String key = zoneNames[i].toLowerCase(locale);
                        if (sorted.add(key)) {
                            tzNames.put(key, tzInfo);
                        }
                    }
                }
            }
            for (final String zoneName : sorted) {
                simpleQuote(sb.append('|'), zoneName);
            }
            sb.append(Symbol.PARENTHESE_RIGHT);
            createPattern(sb);
        }

        /**
         * 设置日历时区。
         *
         * @param parser 解析器
         * @param cal    日历对象
         * @param value  时区值
         */
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

        /**
         * 时区信息类。
         */
        private static class TzInfo {
            /**
             * 时区对象
             */
            TimeZone zone;
            /**
             * 夏令时偏移量
             */
            int dstOffset;

            /**
             * 构造TzInfo实例。
             *
             * @param tz      时区
             * @param useDst  是否使用夏令时
             */
            TzInfo(final TimeZone tz, final boolean useDst) {
                zone = tz;
                dstOffset = useDst ? tz.getDSTSavings() : 0;
            }
        }
    }

    /**
     * 解析ISO 8601时区格式的策略。
     */
    private static class ISO8601TimeZoneStrategy extends PatternStrategy {
        /**
         * ISO 8601单小时策略
         */
        private static final Strategy ISO_8601_1_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}))");
        /**
         * ISO 8601小时分钟策略
         */
        private static final Strategy ISO_8601_2_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}\\d{2}))");
        /**
         * ISO 8601带冒号小时分钟策略
         */
        private static final Strategy ISO_8601_3_STRATEGY = new ISO8601TimeZoneStrategy(
                "(Z|(?:[+-]\\d{2}(?::)\\d{2}))");

        /**
         * 构造ISO8601TimeZoneStrategy实例。
         *
         * @param pattern 正则表达式模式
         */
        ISO8601TimeZoneStrategy(final String pattern) {
            createPattern(pattern);
        }

        /**
         * 获取指定长度的ISO 8601时区策略。
         *
         * @param tokenLen 时区字符串长度
         * @return ISO 8601时区策略
         * @throws IllegalArgumentException 如果长度无效
         */
        static Strategy getStrategy(final int tokenLen) {
            return switch (tokenLen) {
                case 1 -> ISO_8601_1_STRATEGY;
                case 2 -> ISO_8601_2_STRATEGY;
                case 3 -> ISO_8601_3_STRATEGY;
                default -> throw new IllegalArgumentException("invalid number of X");
            };
        }

        /**
         * 设置日历时区。
         *
         * @param parser 解析器
         * @param cal    日历对象
         * @param value  时区值
         */
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
     * 解析格式字符串的策略解析器。
     */
    private class StrategyParser {
        /**
         * 定义日历
         */
        final private Calendar definingCalendar;
        /**
         * 当前解析索引
         */
        private int currentIdx;

        /**
         * 构造StrategyParser实例。
         *
         * @param definingCalendar 定义日历
         */
        StrategyParser(final Calendar definingCalendar) {
            this.definingCalendar = definingCalendar;
        }

        /**
         * 获取下一个解析策略。
         *
         * @return 策略和宽度对象，若无则返回null
         */
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

        /**
         * 解析字母格式字段。
         *
         * @param c 格式字符
         * @return 策略和宽度对象
         */
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

        /**
         * 解析字面量字段。
         *
         * @return 策略和宽度对象
         */
        private StrategyAndWidth literal() {
            boolean activeQuote = false;
            final StringBuilder sb = new StringBuilder();
            while (currentIdx < pattern.length()) {
                final char c = pattern.charAt(currentIdx);
                if (!activeQuote && isFormatLetter(c)) {
                    break;
                } else if (c == '\'' && (++currentIdx == pattern.length() || pattern.charAt(currentIdx) != '\'')) {
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