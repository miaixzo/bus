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
package org.miaixz.bus.core.center.date.culture.dog;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents the "Sanfu" (三伏), also known as Dog Days, which are three periods of hot weather in the Chinese calendar.
 * This class extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Dog extends Samsara {

    /**
     * Array of names for the Sanfu periods: "初伏" (Chufu - initial fu), "中伏" (Zhongfu - middle fu), "末伏" (Mofu - final
     * fu).
     */
    public static final String[] NAMES = { "初伏", "中伏", "末伏" };

    /**
     * Constructs a {@code Dog} instance with the specified name.
     *
     * @param name The name of the Sanfu period.
     */
    public Dog(String name) {
        super(NAMES, name);
    }

    /**
     * Constructs a {@code Dog} instance with the specified index.
     *
     * @param index The index of the Sanfu period in the {@link #NAMES} array.
     */
    public Dog(int index) {
        super(NAMES, index);
    }

    /**
     * Creates a {@code Dog} instance from its name.
     *
     * @param name The name of the Sanfu period.
     * @return A new {@code Dog} instance.
     */
    public static Dog fromName(String name) {
        return new Dog(name);
    }

    /**
     * Creates a {@code Dog} instance from its index.
     *
     * @param index The index of the Sanfu period.
     * @return A new {@code Dog} instance.
     */
    public static Dog fromIndex(int index) {
        return new Dog(index);
    }

    /**
     * Gets the next {@code Dog} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Dog} instance.
     */
    public Dog next(int n) {
        return fromIndex(nextIndex(n));
    }

}
