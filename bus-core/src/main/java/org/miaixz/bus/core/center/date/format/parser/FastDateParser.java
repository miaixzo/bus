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
 * Thread-safe date parser, replacing {@link java.text.SimpleDateFormat}, used to convert date strings to {@link Date}
 * objects.
 *
 * @author Kimi Liu
 * @see FastDatePrinter
 * @since Java 17+
 */
public class FastDateParser extends SimpleDatePrinter implements PositionDateParser {

    @Serial
    private static final long serialVersionUID = 2852256079661L;

    /**
     * Locale for Japanese Imperial calendar.
     */
    private static final Locale JAPANESE_IMPERIAL = new Locale("ja", "JP", "JP");

    /**
     * Comparator for sorting strings by length in descending order, for regular expression options.
     */
    private static final Comparator<String> LONGER_FIRST_LOWERCASE = Comparator.reverseOrder();

    /**
     * Strategy cache array, indexed by calendar field.
     */
    private static final ConcurrentMap<Locale, Strategy>[] CACHES = new ConcurrentMap[Calendar.FIELD_COUNT];

    /**
     * Strategy for parsing two-digit years, automatically adjusting to four-digit years.
     */
    private static final Strategy ABBREVIATED_YEAR_STRATEGY = new NumberStrategy(Calendar.YEAR) {

        @Override
        int modify(final FastDateParser parser, final int iValue) {
            return iValue < 100 ? parser.adjustYear(iValue) : iValue;
        }
    };

    /**
     * Strategy for parsing month numbers, adjusting the value by subtracting 1 to conform to Calendar standards.
     */
    private static final Strategy NUMBER_MONTH_STRATEGY = new NumberStrategy(Calendar.MONTH) {

        @Override
        int modify(final FastDateParser parser, final int iValue) {
            return iValue - 1;
        }
    };

    /**
     * Strategy for parsing full years.
     */
    private static final Strategy LITERAL_YEAR_STRATEGY = new NumberStrategy(Calendar.YEAR);

    /**
     * Strategy for parsing week of year.
     */
    private static final Strategy WEEK_OF_YEAR_STRATEGY = new NumberStrategy(Calendar.WEEK_OF_YEAR);

    /**
     * Strategy for parsing week of month.
     */
    private static final Strategy WEEK_OF_MONTH_STRATEGY = new NumberStrategy(Calendar.WEEK_OF_MONTH);

    /**
     * Strategy for parsing day of year.
     */
    private static final Strategy DAY_OF_YEAR_STRATEGY = new NumberStrategy(Calendar.DAY_OF_YEAR);

    /**
     * Strategy for parsing day of month.
     */
    private static final Strategy DAY_OF_MONTH_STRATEGY = new NumberStrategy(Calendar.DAY_OF_MONTH);

    /**
     * Strategy for parsing day of week, adjusting the value to Calendar standards.
     */
    private static final Strategy DAY_OF_WEEK_STRATEGY = new NumberStrategy(Calendar.DAY_OF_WEEK) {

        @Override
        int modify(final FastDateParser parser, final int iValue) {
            return iValue != 7 ? iValue + 1 : Calendar.SUNDAY;
        }
    };

    /**
     * Strategy for parsing day of week in month.
     */
    private static final Strategy DAY_OF_WEEK_IN_MONTH_STRATEGY = new NumberStrategy(Calendar.DAY_OF_WEEK_IN_MONTH);

    /**
     * Strategy for parsing 24-hour format (0-23).
     */
    private static final Strategy HOUR_OF_DAY_STRATEGY = new NumberStrategy(Calendar.HOUR_OF_DAY);

    /**
     * Strategy for parsing 24-hour format (1-24), where 24 is converted to 0.
     */
    private static final Strategy HOUR24_OF_DAY_STRATEGY = new NumberStrategy(Calendar.HOUR_OF_DAY) {

        @Override
        int modify(final FastDateParser parser, final int iValue) {
            return iValue == 24 ? 0 : iValue;
        }
    };

    /**
     * Strategy for parsing 12-hour format (1-12), where 12 is converted to 0.
     */
    private static final Strategy HOUR12_STRATEGY = new NumberStrategy(Calendar.HOUR) {

        @Override
        int modify(final FastDateParser parser, final int iValue) {
            return iValue == 12 ? 0 : iValue;
        }
    };

    /**
     * Strategy for parsing 12-hour format (0-11).
     */
    private static final Strategy HOUR_STRATEGY = new NumberStrategy(Calendar.HOUR);

    /**
     * Strategy for parsing minutes.
     */
    private static final Strategy MINUTE_STRATEGY = new NumberStrategy(Calendar.MINUTE);

    /**
     * Strategy for parsing seconds.
     */
    private static final Strategy SECOND_STRATEGY = new NumberStrategy(Calendar.SECOND);

    /**
     * Strategy for parsing milliseconds.
     */
    private static final Strategy MILLISECOND_STRATEGY = new NumberStrategy(Calendar.MILLISECOND);

    /**
     * The current century value (e.g., 19 for years before 2000, 20 for years after).
     */
    private final int century;

    /**
     * The starting year of the century.
     */
    private final int startYear;

    /**
     * List of parsing strategies and their widths.
     */
    private transient List<StrategyAndWidth> list;

    /**
     * Constructs a {@code FastDateParser} instance. It is recommended to use the factory methods of
     * {@link FormatBuilder} to obtain cached instances.
     *
     * @param pattern  The non-null {@link java.text.SimpleDateFormat} compatible format.
     * @param timeZone The non-null time zone.
     * @param locale   The non-null locale.
     */
    public FastDateParser(final String pattern, final TimeZone timeZone, final Locale locale) {
        this(pattern, timeZone, locale, null);
    }

    /**
     * Constructs a {@code FastDateParser} instance, allowing specification of the century start time.
     *
     * @param pattern      The non-null {@link java.text.SimpleDateFormat} compatible format.
     * @param timeZone     The non-null time zone.
     * @param locale       The non-null locale.
     * @param centuryStart The century start time for two-digit year parsing.
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
     * Checks if a character is a format letter (A-Z or a-z).
     *
     * @param c The character to check.
     * @return {@code true} if it's a format letter, {@code false} otherwise.
     */
    private static boolean isFormatLetter(final char c) {
        return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z';
    }

    /**
     * Appends a string to a regular expression, handling special character escaping.
     *
     * @param sb    The regular expression builder.
     * @param value The string to append.
     * @return The regular expression builder.
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
     * Retrieves short and long names for a calendar field.
     *
     * @param cal    The calendar object.
     * @param locale The locale.
     * @param field  The calendar field.
     * @param regex  The regular expression builder.
     * @return A map of names to field values.
     */
    private static Map<String, Integer> appendDisplayNames(
            final Calendar cal,
            final Locale locale,
            final int field,
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
     * Retrieves the strategy cache for a specified calendar field.
     *
     * @param field The calendar field.
     * @return The cache mapping locales to strategies.
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
     * Initializes derived fields, called by the constructor and deserialization.
     *
     * @param definingCalendar The calendar object used for initialization.
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
     * Creates the object after deserialization, re-initializing transient properties.
     *
     * @param in The input stream.
     * @throws IOException            if an I/O error occurs.
     * @throws ClassNotFoundException if the class is not found.
     */
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        final Calendar definingCalendar = Calendar.getInstance(timeZone, locale);
        init(definingCalendar);
    }

    /**
     * Parses a date string.
     *
     * @param source The date string to parse.
     * @return The parsed date object.
     * @throws DateException if parsing fails.
     */
    @Override
    public Date parse(final CharSequence source) throws DateException {
        final ParsePosition pp = new ParsePosition(0);
        final Date date = parse(source, pp);
        if (date == null) {
            if (locale.equals(JAPANESE_IMPERIAL)) {
                throw new DateException("(The " + locale + " locale does not support dates before 1868 AD) "
                        + "Unparseable date: \"" + source, pp.getErrorIndex());
            }
            throw new DateException("Unparseable date: " + source, pp.getErrorIndex());
        }
        return date;
    }

    /**
     * Parses a date string into a calendar object.
     *
     * @param source   The date string.
     * @param pos      The parse position.
     * @param calendar The calendar object.
     * @return {@code true} if parsing is successful, {@code false} otherwise.
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
     * Adjusts a two-digit year to a four-digit year using the current century.
     *
     * @param twoDigitYear The two-digit year.
     * @return The adjusted four-digit year.
     */
    private int adjustYear(final int twoDigitYear) {
        final int trial = century + twoDigitYear;
        return twoDigitYear >= startYear ? trial : trial + 100;
    }

    /**
     * Retrieves a parsing strategy based on the format character and field width.
     *
     * @param f                The format character.
     * @param width            The field width.
     * @param definingCalendar The calendar object.
     * @return The parsing strategy.
     * @throws IllegalArgumentException if the format character is not supported.
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
     * Retrieves a locale-specific parsing strategy.
     *
     * @param field            The calendar field.
     * @param definingCalendar The calendar object.
     * @return The parsing strategy.
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
     * Interface for date field parsing strategies.
     */
    interface Strategy {

        /**
         * Parses a date field.
         *
         * @param parser   The parser.
         * @param calendar The calendar object.
         * @param source   The source string.
         * @param pos      The parse position.
         * @param maxWidth The maximum width.
         * @return {@code true} if parsing is successful, {@code false} otherwise.
         */
        boolean parse(FastDateParser parser, Calendar calendar, CharSequence source, ParsePosition pos, int maxWidth);

        /**
         * Checks if the field is a number field.
         *
         * @return {@code true} if it's a number field, {@code false} otherwise.
         */
        default boolean isNumber() {
            return false;
        }
    }

    /**
     * Class to store parsing strategy and field width.
     */
    private static class StrategyAndWidth {

        /**
         * The parsing strategy.
         */
        final Strategy strategy;
        /**
         * The field width.
         */
        final int width;

        /**
         * Constructs a {@code StrategyAndWidth} instance.
         *
         * @param strategy The parsing strategy.
         * @param width    The field width.
         */
        StrategyAndWidth(final Strategy strategy, final int width) {
            this.strategy = strategy;
            this.width = width;
        }

        /**
         * Gets the maximum width.
         *
         * @param lt The strategy iterator.
         * @return The maximum width.
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
     * Strategy for parsing a single field.
     */
    private static abstract class PatternStrategy implements Strategy {

        /**
         * The regular expression pattern.
         */
        private Pattern pattern;

        /**
         * Creates a regular expression pattern.
         *
         * @param regex The regular expression builder.
         */
        void createPattern(final StringBuilder regex) {
            createPattern(regex.toString());
        }

        /**
         * Creates a regular expression pattern.
         *
         * @param regex The regular expression string.
         */
        void createPattern(final String regex) {
            this.pattern = Pattern.compile(regex);
        }

        /**
         * Parses a field value.
         *
         * @param parser   The parser.
         * @param calendar The calendar object.
         * @param source   The source string.
         * @param pos      The parse position.
         * @param maxWidth The maximum width.
         * @return {@code true} if parsing is successful, {@code false} otherwise.
         */
        @Override
        public boolean parse(
                final FastDateParser parser,
                final Calendar calendar,
                final CharSequence source,
                final ParsePosition pos,
                final int maxWidth) {
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
         * Sets the calendar field value.
         *
         * @param parser The parser.
         * @param cal    The calendar object.
         * @param value  The field value.
         */
        abstract void setCalendar(FastDateParser parser, Calendar cal, String value);
    }

    /**
     * Strategy for copying static or quoted fields in the format pattern.
     */
    private static class CopyQuotedStrategy implements Strategy {

        /**
         * The literal text of the format field.
         */
        final private String formatField;

        /**
         * Constructs a {@code CopyQuotedStrategy} instance.
         *
         * @param formatField The literal text to match.
         */
        CopyQuotedStrategy(final String formatField) {
            this.formatField = formatField;
        }

        /**
         * Parses the literal text field.
         *
         * @param parser   The parser.
         * @param calendar The calendar object.
         * @param source   The source string.
         * @param pos      The parse position.
         * @param maxWidth The maximum width.
         * @return {@code true} if parsing is successful, {@code false} otherwise.
         */
        @Override
        public boolean parse(
                final FastDateParser parser,
                final Calendar calendar,
                final CharSequence source,
                final ParsePosition pos,
                final int maxWidth) {
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
     * Strategy for parsing text fields (case-insensitive).
     */
    private static class CaseInsensitiveTextStrategy extends PatternStrategy {

        /**
         * The calendar field.
         */
        final int field;
        /**
         * The locale.
         */
        final Locale locale;
        /**
         * Map of field values (lowercase keys).
         */
        private final Map<String, Integer> lKeyValues;

        /**
         * Constructs a {@code CaseInsensitiveTextStrategy} instance.
         *
         * @param field            The calendar field.
         * @param definingCalendar The calendar object.
         * @param locale           The locale.
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
         * Sets the calendar field value.
         *
         * @param parser The parser.
         * @param cal    The calendar object.
         * @param value  The field value.
         */
        @Override
        void setCalendar(final FastDateParser parser, final Calendar cal, final String value) {
            final Integer iVal = lKeyValues.get(value.toLowerCase(locale));
            cal.set(field, iVal);
        }
    }

    /**
     * Strategy for parsing number fields.
     */
    private static class NumberStrategy implements Strategy {

        /**
         * The calendar field.
         */
        private final int field;

        /**
         * Constructs a {@code NumberStrategy} instance.
         *
         * @param field The calendar field.
         */
        NumberStrategy(final int field) {
            this.field = field;
        }

        /**
         * Checks if the field is a number field.
         *
         * @return Always returns {@code true}.
         */
        @Override
        public boolean isNumber() {
            return true;
        }

        /**
         * Parses a number field.
         *
         * @param parser   The parser.
         * @param calendar The calendar object.
         * @param source   The source string.
         * @param pos      The parse position.
         * @param maxWidth The maximum width.
         * @return {@code true} if parsing is successful, {@code false} otherwise.
         */
        @Override
        public boolean parse(
                final FastDateParser parser,
                final Calendar calendar,
                final CharSequence source,
                final ParsePosition pos,
                final int maxWidth) {
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
         * Modifies the parsed number value.
         *
         * @param parser The parser.
         * @param iValue The parsed number.
         * @return The modified value.
         */
        int modify(final FastDateParser parser, final int iValue) {
            return iValue;
        }
    }

    /**
     * Strategy for parsing time zone fields.
     */
    static class TimeZoneStrategy extends PatternStrategy {

        /**
         * Regular expression for RFC 822 time zone format.
         */
        private static final String RFC_822_TIME_ZONE = "[+-]\\d{4}";
        /**
         * Regular expression for UTC time zone with offset format.
         */
        private static final String UTC_TIME_ZONE_WITH_OFFSET = "[+-]\\d{2}:\\d{2}";
        /**
         * Regular expression for GMT option.
         */
        private static final String GMT_OPTION = "GMT[+-]\\d{1,2}:\\d{2}";
        /**
         * Index for time zone ID.
         */
        private static final int ID = 0;
        /**
         * The locale.
         */
        private final Locale locale;
        /**
         * Map of time zone names to information.
         */
        private final Map<String, TzInfo> tzNames = new HashMap<>();

        /**
         * Constructs a {@code TimeZoneStrategy} instance.
         *
         * @param locale The locale.
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
         * Sets the calendar time zone.
         *
         * @param parser The parser.
         * @param cal    The calendar object.
         * @param value  The time zone value.
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
         * Time zone information class.
         */
        private static class TzInfo {

            /**
             * The time zone object.
             */
            TimeZone zone;
            /**
             * Daylight saving time offset.
             */
            int dstOffset;

            /**
             * Constructs a {@code TzInfo} instance.
             *
             * @param tz     The time zone.
             * @param useDst Whether to use daylight saving time.
             */
            TzInfo(final TimeZone tz, final boolean useDst) {
                zone = tz;
                dstOffset = useDst ? tz.getDSTSavings() : 0;
            }
        }
    }

    /**
     * Strategy for parsing ISO 8601 time zone formats.
     */
    private static class ISO8601TimeZoneStrategy extends PatternStrategy {

        /**
         * ISO 8601 single hour strategy.
         */
        private static final Strategy ISO_8601_1_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}))");
        /**
         * ISO 8601 hour and minute strategy.
         */
        private static final Strategy ISO_8601_2_STRATEGY = new ISO8601TimeZoneStrategy("(Z|(?:[+-]\\d{2}\\d{2}))");
        /**
         * ISO 8601 hour and minute strategy with colon.
         */
        private static final Strategy ISO_8601_3_STRATEGY = new ISO8601TimeZoneStrategy(
                "(Z|(?:[+-]\\d{2}(?::)\\d{2}))");

        /**
         * Constructs an {@code ISO8601TimeZoneStrategy} instance.
         *
         * @param pattern The regular expression pattern.
         */
        ISO8601TimeZoneStrategy(final String pattern) {
            createPattern(pattern);
        }

        /**
         * Gets the ISO 8601 rule for the specified length.
         *
         * @param tokenLen The length of the time zone string token.
         * @return The {@code Iso8601_Rule} instance.
         * @throws IllegalArgumentException if the length is invalid.
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
         * Sets the calendar time zone.
         *
         * @param parser The parser.
         * @param cal    The calendar object.
         * @param value  The time zone value.
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
     * Strategy parser for parsing format strings.
     */
    private class StrategyParser {

        /**
         * The defining calendar.
         */
        final private Calendar definingCalendar;
        /**
         * The current parsing index.
         */
        private int currentIdx;

        /**
         * Constructs a {@code StrategyParser} instance.
         *
         * @param definingCalendar The defining calendar.
         */
        StrategyParser(final Calendar definingCalendar) {
            this.definingCalendar = definingCalendar;
        }

        /**
         * Gets the next parsing strategy.
         *
         * @return The strategy and width object, or {@code null} if no more strategies.
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
         * Parses a letter format field.
         *
         * @param c The format character.
         * @return The strategy and width object.
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
         * Parses a literal field.
         *
         * @return The strategy and width object.
         * @throws IllegalArgumentException if a quote is unterminated.
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
