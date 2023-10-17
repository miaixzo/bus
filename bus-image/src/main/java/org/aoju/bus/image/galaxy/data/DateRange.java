/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.image.galaxy.data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class DateRange implements Serializable {

    private final Date start;
    private final Date end;

    public DateRange(Date start, Date end) {
        this.start = start;
        this.end = end;
    }

    public final Date getStartDate() {
        return start;
    }

    public final Date getEndDate() {
        return end;
    }

    public boolean contains(Date when) {
        return !(null != start && start.after(when)
                || null != end && end.before(when));
    }

    @Override
    public boolean equals(Object object) {
        if (object == this)
            return true;

        if (!(object instanceof DateRange))
            return false;

        DateRange other = (DateRange) object;
        return (null == start
                ? null == other.start
                : start.equals(other.start))
                && (null == end
                ? null == other.end
                : end.equals(other.end));
    }

    @Override
    public int hashCode() {
        int code = 0;
        if (null != start)
            code = start.hashCode();
        if (null != end)
            code ^= start.hashCode();
        return code;
    }

    @Override
    public String toString() {
        return "[" + start + ", " + end + "]";
    }

}
