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
package org.miaixz.bus.core.center.date.culture.nine;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents the "Shu Jiu" (数九) or Counting Nine periods, which are nine nine-day periods following the Winter
 * Solstice. This class extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Nine extends Samsara {

    /**
     * Array of names for the Counting Nine periods.
     */
    public static final String[] NAMES = { "一九", "二九", "三九", "四九", "五九", "六九", "七九", "八九", "九九" };

    /**
     * Constructs a {@code Nine} instance with the specified name.
     *
     * @param name The name of the Counting Nine period.
     */
    public Nine(String name) {
        super(NAMES, name);
    }

    /**
     * Constructs a {@code Nine} instance with the specified index.
     *
     * @param index The index of the Counting Nine period in the {@link #NAMES} array.
     */
    public Nine(int index) {
        super(NAMES, index);
    }

    /**
     * Creates a {@code Nine} instance from its name.
     *
     * @param name The name of the Counting Nine period.
     * @return A new {@code Nine} instance.
     */
    public static Nine fromName(String name) {
        return new Nine(name);
    }

    /**
     * Creates a {@code Nine} instance from its index.
     *
     * @param index The index of the Counting Nine period.
     * @return A new {@code Nine} instance.
     */
    public static Nine fromIndex(int index) {
        return new Nine(index);
    }

    /**
     * Gets the next {@code Nine} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Nine} instance.
     */
    public Nine next(int n) {
        return fromIndex(nextIndex(n));
    }

}
