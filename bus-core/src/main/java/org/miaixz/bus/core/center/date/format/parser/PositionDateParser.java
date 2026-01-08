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
 * @since Java 17+
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
