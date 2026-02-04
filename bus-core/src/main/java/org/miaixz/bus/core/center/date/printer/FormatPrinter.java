/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
