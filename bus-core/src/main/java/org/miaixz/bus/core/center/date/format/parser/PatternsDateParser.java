/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
