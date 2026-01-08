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
package org.miaixz.bus.core.center.date.printer;

import java.util.Calendar;
import java.util.Date;

/**
 * Interface for date formatting output, defining methods for formatting dates.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface FormatPrinter extends DatePrinter {

    /**
     * Formats a millisecond timestamp into a string.
     *
     * @param millis The millisecond timestamp.
     * @return The formatted string.
     */
    String format(long millis);

    /**
     * Formats a date object into a string, using {@code GregorianCalendar}.
     *
     * @param date The date object.
     * @return The formatted string.
     */
    String format(Date date);

    /**
     * Formats a calendar object into a string.
     *
     * @param calendar The calendar object.
     * @return The formatted string.
     */
    String format(Calendar calendar);

    /**
     * Formats a millisecond timestamp into the specified output buffer.
     *
     * @param millis The millisecond timestamp.
     * @param buf    The output buffer.
     * @param <B>    The type of {@code Appendable}, typically StringBuilder or StringBuffer.
     * @return The formatted buffer.
     */
    <B extends Appendable> B format(long millis, B buf);

    /**
     * Formats a date object into the specified output buffer, using {@code GregorianCalendar}.
     *
     * @param date The date object.
     * @param buf  The output buffer.
     * @param <B>  The type of {@code Appendable}, typically StringBuilder or StringBuffer.
     * @return The formatted buffer.
     */
    <B extends Appendable> B format(Date date, B buf);

    /**
     * Formats a calendar object into the specified output buffer, prioritizing the time zone specified during
     * construction.
     *
     * @param calendar The calendar object.
     * @param buf      The output buffer.
     * @param <B>      The type of {@code Appendable}, typically StringBuilder or StringBuffer.
     * @return The formatted buffer.
     */
    <B extends Appendable> B format(Calendar calendar, B buf);

}
