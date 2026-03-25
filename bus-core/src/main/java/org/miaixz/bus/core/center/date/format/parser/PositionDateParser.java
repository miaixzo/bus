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

import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;

import org.miaixz.bus.core.center.date.printer.DatePrinter;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.exception.DateException;

/**
 * An interface for date parsers that use a {@link ParsePosition} to track the parsing progress, similar to
 * {@link java.text.DateFormat}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface PositionDateParser extends DateParser, DatePrinter {

    /**
     * Parses a date string into a {@link Date} object, updating the given {@link ParsePosition}. This is equivalent to
     * {@link java.text.DateFormat#parse(String, ParsePosition)}.
     *
     * @param source The date string to parse.
     * @param pos    The {@link ParsePosition} object that tracks the parsing index.
     * @return The parsed {@link Date}, or null if parsing fails.
     */
    default Date parse(final CharSequence source, final ParsePosition pos) {
        return parseCalendar(source, pos, Keys.getBoolean(Keys.DATE_LENIENT, false)).getTime();
    }

    /**
     * Parses a date string, updating the fields of the given {@link Calendar}. After successful parsing,
     * {@link ParsePosition#getIndex()} is updated to the position after the last character used. If parsing fails,
     * {@link ParsePosition#getErrorIndex()} is updated to the position of the error.
     *
     * @param source   The date string to be parsed.
     * @param pos      Defines the starting position for parsing and is updated upon completion.
     * @param calendar The {@link Calendar} to be updated with the parsed date.
     * @return {@code true} if parsing was successful, {@code false} otherwise.
     */
    boolean parse(CharSequence source, ParsePosition pos, Calendar calendar);

    /**
     * Parses a date string into a new {@link Calendar} object.
     *
     * @param source  The date string to parse.
     * @param pos     The {@link ParsePosition} object.
     * @param lenient Whether parsing should be lenient.
     * @return The parsed {@link Calendar}.
     * @throws DateException if parsing fails.
     */
    default Calendar parseCalendar(final CharSequence source, final ParsePosition pos, final boolean lenient) {
        Assert.notBlank(source, "Date string must not be blank!");
        final Calendar calendar = Calendar.getInstance(getTimeZone(), getLocale());
        calendar.clear();
        calendar.setLenient(lenient);

        if (parse(source.toString(), pos, calendar)) {
            return calendar;
        }

        throw new DateException("Parse [{}] with format [{}] error, at: {}", source, getPattern(), pos.getErrorIndex());
    }

}
