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
package org.miaixz.bus.core.center.date;

import java.io.Serial;
import java.util.Date;

import org.miaixz.bus.core.lang.range.Range;
import org.miaixz.bus.core.xyz.DateKit;

/**
 * Represents a date range, providing an iterable sequence of dates between a start and end point. This class extends
 * {@link Range} to specifically handle date and time intervals.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Boundary extends Range<DateTime> {

    @Serial
    private static final long serialVersionUID = 2852233385529L;

    /**
     * Constructs a {@code Boundary} with inclusive start and end dates, and a specified step unit. The iteration step
     * defaults to 1 unit.
     *
     * @param start The inclusive starting date and time.
     * @param end   The inclusive ending date and time.
     * @param unit  The step unit for iterating through the date range.
     */
    public Boundary(final Date start, final Date end, final Various unit) {
        this(start, end, unit, 1);
    }

    /**
     * Constructs a {@code Boundary} with inclusive start and end dates, a specified step unit, and a step count.
     *
     * @param start The inclusive starting date and time.
     * @param end   The inclusive ending date and time.
     * @param unit  The step unit for iterating through the date range.
     * @param step  The number of units to step by for each iteration. Must be a positive integer.
     */
    public Boundary(final Date start, final Date end, final Various unit, final int step) {
        this(start, end, unit, step, true, true);
    }

    /**
     * Constructs a {@code Boundary} with specified start and end dates, step unit, step count, and inclusivity flags.
     *
     * @param start          The starting date and time.
     * @param end            The ending date and time.
     * @param unit           The step unit for iterating through the date range.
     * @param step           The number of units to step by for each iteration. Must be a positive integer.
     * @param isIncludeStart {@code true} if the start date should be included in the range, {@code false} otherwise.
     * @param isIncludeEnd   {@code true} if the end date should be included in the range, {@code false} otherwise.
     */
    public Boundary(final Date start, final Date end, final Various unit, final int step, final boolean isIncludeStart,
            final boolean isIncludeEnd) {
        super(DateKit.date(start), DateKit.date(end), (current, end1, index) -> {
            if (step <= 0) {
                return null;
            }
            final DateTime dt = DateKit.date(start).offsetNew(unit, (index + 1) * step);
            if (dt.isAfter(end1)) {
                return null;
            }
            return dt;
        }, isIncludeStart, isIncludeEnd);
    }

}
