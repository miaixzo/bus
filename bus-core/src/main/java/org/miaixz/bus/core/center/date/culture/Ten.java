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
package org.miaixz.bus.core.center.date.culture;

/**
 * Represents the Ten-day Cycle (旬) in Chinese traditional calendar. This class extends {@link Samsara} to manage a
 * cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Ten extends Samsara {

    /**
     * Array of names for the Ten-day Cycles.
     */
    public static final String[] NAMES = { "甲子", "甲戌", "甲申", "甲午", "甲辰", "甲寅" };

    /**
     * Constructs a {@code Ten} instance with the specified index.
     *
     * @param index The index of the Ten-day Cycle in the {@link #NAMES} array.
     */
    public Ten(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Ten} instance with the specified name.
     *
     * @param name The name of the Ten-day Cycle.
     */
    public Ten(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Ten} instance from its index.
     *
     * @param index The index of the Ten-day Cycle.
     * @return A new {@code Ten} instance.
     */
    public static Ten fromIndex(int index) {
        return new Ten(index);
    }

    /**
     * Creates a {@code Ten} instance from its name.
     *
     * @param name The name of the Ten-day Cycle.
     * @return A new {@code Ten} instance.
     */
    public static Ten fromName(String name) {
        return new Ten(name);
    }

    /**
     * Gets the next {@code Ten} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Ten} instance.
     */
    public Ten next(int n) {
        return fromIndex(nextIndex(n));
    }

}
