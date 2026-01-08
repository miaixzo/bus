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
package org.miaixz.bus.core.center.date.format;

import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.xyz.DateKit;

/**
 * Global custom date format manager, used to define mappings between user-specified date formats and formatting/parsing
 * rules.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FormatManager {

    /**
     * Map of date formatting rules.
     */
    private final Map<CharSequence, Function<Date, String>> formatterMap;
    /**
     * Map of date parsing rules.
     */
    private final Map<CharSequence, Function<CharSequence, Date>> parserMap;

    /**
     * Constructs a {@code FormatManager} instance, initializing preset formatting and parsing rules.
     */
    public FormatManager() {
        formatterMap = new ConcurrentHashMap<>();
        parserMap = new ConcurrentHashMap<>();

        // Preset format: seconds timestamp
        registerFormatter(Fields.FORMAT_SECONDS, (date) -> String.valueOf(Math.floorDiv(date.getTime(), 1000L)));
        registerParser(
                Fields.FORMAT_SECONDS,
                (dateStr) -> DateKit.date(Math.multiplyExact(Long.parseLong(dateStr.toString()), 1000L)));

        // Preset format: milliseconds timestamp
        registerFormatter(Fields.FORMAT_MILLISECONDS, (date) -> String.valueOf(date.getTime()));
        registerParser(Fields.FORMAT_MILLISECONDS, (dateStr) -> DateKit.date(Long.parseLong(dateStr.toString())));
    }

    /**
     * Gets the singleton instance of {@code FormatManager}.
     *
     * @return The singleton instance of {@code FormatManager}.
     */
    public static FormatManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Registers a date formatting rule.
     *
     * @param format The format identifier.
     * @param func   The formatting function.
     * @return The current {@code FormatManager} instance.
     * @throws IllegalArgumentException if {@code format} or {@code func} is {@code null}.
     */
    public FormatManager registerFormatter(final String format, final Function<Date, String> func) {
        Assert.notNull(format, "Format must be not null !");
        Assert.notNull(func, "Function must be not null !");
        formatterMap.put(format, func);
        return this;
    }

    /**
     * Registers a date parsing rule.
     *
     * @param format The format identifier.
     * @param func   The parsing function.
     * @return The current {@code FormatManager} instance.
     * @throws IllegalArgumentException if {@code format} or {@code func} is {@code null}.
     */
    public FormatManager registerParser(final String format, final Function<CharSequence, Date> func) {
        Assert.notNull(format, "Format must be not null !");
        Assert.notNull(func, "Function must be not null !");
        parserMap.put(format, func);
        return this;
    }

    /**
     * Checks if the given format is a custom formatting rule.
     *
     * @param format The format identifier.
     * @return {@code true} if it's a custom formatting rule, {@code false} otherwise.
     */
    public boolean isCustomFormat(final String format) {
        return formatterMap != null && formatterMap.containsKey(format);
    }

    /**
     * Checks if the given format is a custom parsing rule.
     *
     * @param format The format identifier.
     * @return {@code true} if it's a custom parsing rule, {@code false} otherwise.
     */
    public boolean isCustomParse(final String format) {
        return parserMap != null && parserMap.containsKey(format);
    }

    /**
     * Formats a date using a custom format.
     *
     * @param date   The date object.
     * @param format The custom format identifier.
     * @return The formatted string, or {@code null} if no corresponding rule is found.
     */
    public String format(final Date date, final CharSequence format) {
        if (formatterMap != null) {
            final Function<Date, String> func = formatterMap.get(format);
            if (func != null) {
                return func.apply(date);
            }
        }
        return null;
    }

    /**
     * Formats a temporal accessor object using a custom format.
     *
     * @param temporalAccessor The temporal accessor object.
     * @param format           The custom format identifier.
     * @return The formatted string, or {@code null} if no corresponding rule is found.
     */
    public String format(final TemporalAccessor temporalAccessor, final CharSequence format) {
        return format(DateKit.date(temporalAccessor), format);
    }

    /**
     * Parses a date string using a custom format.
     *
     * @param date   The date string.
     * @param format The custom format identifier.
     * @return The parsed date object, or {@code null} if no corresponding rule is found.
     */
    public Date parse(final CharSequence date, final String format) {
        if (parserMap != null) {
            final Function<CharSequence, Date> func = parserMap.get(format);
            if (func != null) {
                return func.apply(date);
            }
        }
        return null;
    }

    /**
     * Singleton holder class, implementing lazy loading.
     */
    private static class SingletonHolder {

        /** Static singleton instance, thread-safe guaranteed by JVM. */
        private static final FormatManager INSTANCE = new FormatManager();
    }

}
