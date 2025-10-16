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
package org.miaixz.bus.core.center.date.culture.cn.plumrain;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents the "Plum Rain" (梅雨) season, a period of continuous rain in East Asia. This class extends {@link Samsara}
 * to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PlumRain extends Samsara {

    /**
     * Array of names for the Plum Rain periods: "入梅" (Entering Plum Rain) and "出梅" (Exiting Plum Rain).
     */
    public static final String[] NAMES = { "入梅", "出梅" };

    /**
     * Constructs a {@code PlumRain} instance with the specified name.
     *
     * @param name The name of the Plum Rain period.
     */
    public PlumRain(String name) {
        super(NAMES, name);
    }

    /**
     * Constructs a {@code PlumRain} instance with the specified index.
     *
     * @param index The index of the Plum Rain period in the {@link #NAMES} array.
     */
    public PlumRain(int index) {
        super(NAMES, index);
    }

    /**
     * Creates a {@code PlumRain} instance from its name.
     *
     * @param name The name of the Plum Rain period.
     * @return A new {@code PlumRain} instance.
     */
    public static PlumRain fromName(String name) {
        return new PlumRain(name);
    }

    /**
     * Creates a {@code PlumRain} instance from its index.
     *
     * @param index The index of the Plum Rain period.
     * @return A new {@code PlumRain} instance.
     */
    public static PlumRain fromIndex(int index) {
        return new PlumRain(index);
    }

    /**
     * Gets the next {@code PlumRain} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code PlumRain} instance.
     */
    public PlumRain next(int n) {
        return fromIndex(nextIndex(n));
    }

}
