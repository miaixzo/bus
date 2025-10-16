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
package org.miaixz.bus.core.center.date.culture.cn;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents the Sixty-year Cycle (元), a larger temporal unit in Chinese traditional calendar. Each cycle is composed
 * of 60 years, and there are three such cycles: Upper, Middle, and Lower. This class extends {@link Samsara} to manage
 * a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Sixty extends Samsara {

    /**
     * Array of names for the Sixty-year Cycles.
     */
    public static final String[] NAMES = { "上元", "中元", "下元" };

    /**
     * Constructs a {@code Sixty} instance with the specified index.
     *
     * @param index The index of the Sixty-year Cycle in the {@link #NAMES} array.
     */
    public Sixty(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Sixty} instance with the specified name.
     *
     * @param name The name of the Sixty-year Cycle.
     */
    public Sixty(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Sixty} instance from its index.
     *
     * @param index The index of the Sixty-year Cycle.
     * @return A new {@code Sixty} instance.
     */
    public static Sixty fromIndex(int index) {
        return new Sixty(index);
    }

    /**
     * Creates a {@code Sixty} instance from its name.
     *
     * @param name The name of the Sixty-year Cycle.
     * @return A new {@code Sixty} instance.
     */
    public static Sixty fromName(String name) {
        return new Sixty(name);
    }

    /**
     * Gets the next {@code Sixty} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Sixty} instance.
     */
    public Sixty next(int n) {
        return fromIndex(nextIndex(n));
    }

}
