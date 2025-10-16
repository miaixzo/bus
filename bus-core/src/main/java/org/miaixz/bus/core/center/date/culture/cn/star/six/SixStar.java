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
package org.miaixz.bus.core.center.date.culture.cn.star.six;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents the Rokuyo (六曜) or Six-day Cycle, also known as Koumei Rokuyo-sei (孔明六曜星), a system of divination used in
 * Japan. This class extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SixStar extends Samsara {

    /**
     * Array of names for the Rokuyo days.
     */
    public static final String[] NAMES = { "先胜", "友引", "先负", "佛灭", "大安", "赤口" };

    /**
     * Constructs a {@code SixStar} instance with the specified index.
     *
     * @param index The index of the Rokuyo day in the {@link #NAMES} array.
     */
    public SixStar(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code SixStar} instance with the specified name.
     *
     * @param name The name of the Rokuyo day.
     */
    public SixStar(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code SixStar} instance from its index.
     *
     * @param index The index of the Rokuyo day.
     * @return A new {@code SixStar} instance.
     */
    public static SixStar fromIndex(int index) {
        return new SixStar(index);
    }

    /**
     * Creates a {@code SixStar} instance from its name.
     *
     * @param name The name of the Rokuyo day.
     * @return A new {@code SixStar} instance.
     */
    public static SixStar fromName(String name) {
        return new SixStar(name);
    }

    /**
     * Gets the next {@code SixStar} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code SixStar} instance.
     */
    public SixStar next(int n) {
        return fromIndex(nextIndex(n));
    }

}
