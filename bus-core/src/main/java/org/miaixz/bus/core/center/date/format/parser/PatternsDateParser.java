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
package org.miaixz.bus.core.center.date.format.parser;

import java.io.Serial;
import java.io.Serializable;
import java.util.Locale;

import org.miaixz.bus.core.center.date.Calendar;
import org.miaixz.bus.core.center.date.DateTime;
import org.miaixz.bus.core.lang.exception.DateException;

/**
 * Parses a date-time string using a given array of date formats. It attempts each format until parsing succeeds,
 * returning a {@link DateTime} object. If all attempts fail, a {@link DateException} is thrown.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PatternsDateParser implements DateParser, Serializable {

    @Serial
    private static final long serialVersionUID = 2852256632611L;

    /**
     * Array of date format patterns.
     */
    private String[] patterns;

    /**
     * Locale setting.
     */
    private Locale locale;

    /**
     * Constructs a {@code PatternsDateParser} instance, initializing the date format patterns.
     *
     * @param args Multiple date format patterns.
     */
    public PatternsDateParser(final String... args) {
        this.patterns = args;
    }

    /**
     * Creates a {@code PatternsDateParser} instance.
     *
     * @param args Multiple date format patterns.
     * @return A {@code PatternsDateParser} instance.
     */
    public static PatternsDateParser of(final String... args) {
        return new PatternsDateParser(args);
    }

    /**
     * Sets the array of date format patterns.
     *
     * @param patterns The list of date format patterns.
     * @return This instance.
     */
    public PatternsDateParser setPatterns(final String... patterns) {
        this.patterns = patterns;
        return this;
    }

    /**
     * Gets the locale setting.
     *
     * @return The locale setting.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale.
     *
     * @param locale The locale setting.
     * @return This instance.
     */
    public PatternsDateParser setLocale(final Locale locale) {
        this.locale = locale;
        return this;
    }

    /**
     * Parses a date string.
     *
     * @param source The date string to parse.
     * @return The parsed {@link DateTime} object.
     * @throws DateException if parsing fails.
     */
    @Override
    public DateTime parse(final CharSequence source) {
        return new DateTime(Calendar.parseByPatterns(source, this.locale, this.patterns));
    }

}
