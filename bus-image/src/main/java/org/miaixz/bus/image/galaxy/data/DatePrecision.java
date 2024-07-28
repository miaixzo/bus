/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
 * @author Kimi Liu
 * @since Java 17+
 */
public class DatePrecision {

    /**
     * Specifies the precision of a date value (e.g. Calendar.MILLISECOND for millisecond precision). For methods that
     * format a date (e.g. {@link Attributes#setDate}), this acts as an input to specify the precision that should be
     * stored. For methods that parse a date (e.g. {@link Attributes#getDate}), this field will be used as a return
     * value. It returns the precision of the date that was parsed.
     */
    public int lastField;
    /**
     * Specifies whether a formatted date includes a timezone in the stored value itself. This is only used for values
     * of {@link VR#DT}. For methods that format a DT date time, this acts as an input to specify whether the timezone
     * offset should be appended to the formatted date (e.g. "+0100"). For methods that parse a DT date time, this field
     * will be used as a return value. It returns whether the parsed date included a timezone offset.
     */
    public boolean includeTimezone;

    public DatePrecision[] precisions;

    public DatePrecision() {
        this(Calendar.MILLISECOND, false);
    }

    public DatePrecision(int lastField) {
        this(lastField, false);
    }

    public DatePrecision(int lastField, boolean includeTimezone) {
        this.lastField = lastField;
        this.includeTimezone = includeTimezone;
    }

}
