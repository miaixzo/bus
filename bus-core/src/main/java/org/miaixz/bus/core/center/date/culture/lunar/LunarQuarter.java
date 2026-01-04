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
package org.miaixz.bus.core.center.date.culture.lunar;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents a quarter in the Lunar calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LunarQuarter extends Samsara {

    /**
     * Names of lunar quarters.
     */
    public static final String[] NAMES = { "孟春", "仲春", "季春", "孟夏", "仲夏", "季夏", "孟秋", "仲秋", "季秋", "孟冬", "仲冬", "季冬" };

    /**
     * Constructs a {@code LunarQuarter} with the given index.
     *
     * @param index The index of the quarter.
     */
    public LunarQuarter(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code LunarQuarter} with the given name.
     *
     * @param name The name of the quarter.
     */
    public LunarQuarter(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a LunarQuarter instance from the given index.
     *
     * @param index The index of the quarter.
     * @return A new {@link LunarQuarter} instance.
     */
    public static LunarQuarter fromIndex(int index) {
        return new LunarQuarter(index);
    }

    /**
     * Creates a {@code LunarQuarter} instance from the given name.
     *
     * @param name The name of the quarter.
     * @return A new {@link LunarQuarter} instance.
     */
    public static LunarQuarter fromName(String name) {
        return new LunarQuarter(name);
    }

    /**
     * Gets the next lunar quarter after a specified number of quarters.
     *
     * @param n The number of quarters to add.
     * @return The {@link LunarQuarter} after {@code n} quarters.
     */
    public LunarQuarter next(int n) {
        return fromIndex(nextIndex(n));
    }

}
