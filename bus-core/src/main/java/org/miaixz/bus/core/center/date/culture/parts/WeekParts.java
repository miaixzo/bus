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
package org.miaixz.bus.core.center.date.culture.parts;

import org.miaixz.bus.core.center.date.culture.Week;

/**
 * Abstract base class for date components containing week information.
 *
 * <p>
 * This class extends {@link MonthParts} and adds week-related fields, representing a specific week within a month. The
 * week definition can vary based on the starting weekday and the week index.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class WeekParts extends MonthParts {

    /**
     * The week index within the month (0-5).
     */
    protected int index;

    /**
     * The starting weekday (1-7 for Monday-Sunday, or 0 for Sunday).
     */
    protected int start;

    /**
     * Gets the week index.
     *
     * @return the week index (0-5)
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the starting weekday of the week.
     *
     * @return the starting weekday as a {@link Week} enum value
     */
    public Week getStart() {
        return Week.fromIndex(start);
    }

    /**
     * Validates the week components.
     *
     * @param index the week index to validate (0-5)
     * @param start the starting weekday to validate (0-6)
     * @throws IllegalArgumentException if index or start is out of valid range
     */
    public static void validate(int index, int start) {
        if (index < 0 || index > 5) {
            throw new IllegalArgumentException(String.format("illegal week index: %d", index));
        }
        if (start < 0 || start > 6) {
            throw new IllegalArgumentException(String.format("illegal week start: %d", start));
        }
    }

}
