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
package org.miaixz.bus.image.galaxy.data;

import java.util.Calendar;

/**
 * Represents the precision of a date/time value and whether timezone information is included. This class is used to
 * specify desired precision for formatting dates and to retrieve the actual precision of parsed dates.
 * 
 * @author Kimi Liu
 * @since Java 17+
 */
public class DatePrecision {

    /**
     * Specifies the precision of a date value (e.g., {@link Calendar#MILLISECOND} for millisecond precision). For
     * methods that format a date (e.g., {@link Attributes#setDate}), this acts as an input to specify the precision
     * that should be stored. For methods that parse a date (e.g., {@link Attributes#getDate}), this field will be used
     * as a return value, indicating the precision of the parsed date.
     */
    public int lastField;
    /**
     * Specifies whether a formatted date includes a timezone in the stored value itself. This is only used for values
     * of {@link VR#DT}. For methods that format a DT date time, this acts as an input to specify whether the timezone
     * offset should be appended to the formatted date (e.g., "+0100"). For methods that parse a DT date time, this
     * field will be used as a return value, indicating whether the parsed date included a timezone offset.
     */
    public boolean includeTimezone;

    /**
     * An array of {@code DatePrecision} objects, typically used when dealing with multiple date values where each might
     * have its own precision.
     */
    public DatePrecision[] precisions;

    /**
     * Constructs a new {@code DatePrecision} with default values: millisecond precision and no timezone inclusion.
     */
    public DatePrecision() {
        this(Calendar.MILLISECOND, false);
    }

    /**
     * Constructs a new {@code DatePrecision} with the specified last field precision and no timezone inclusion.
     * 
     * @param lastField The last field of precision (e.g., {@link Calendar#SECOND}, {@link Calendar#MILLISECOND}).
     */
    public DatePrecision(int lastField) {
        this(lastField, false);
    }

    /**
     * Constructs a new {@code DatePrecision} with the specified last field precision and timezone inclusion setting.
     * 
     * @param lastField       The last field of precision (e.g., {@link Calendar#SECOND}, {@link Calendar#MILLISECOND}).
     * @param includeTimezone {@code true} if timezone offset should be included in formatted output, {@code false}
     *                        otherwise.
     */
    public DatePrecision(int lastField, boolean includeTimezone) {
        this.lastField = lastField;
        this.includeTimezone = includeTimezone;
    }

}
