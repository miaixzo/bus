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
package org.miaixz.bus.core.center.date.culture.cn.star.ten;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents the Ten Gods (十神) in Chinese Bazi (Four Pillars of Destiny) astrology. This class extends {@link Samsara}
 * to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TenStar extends Samsara {

    /**
     * Array of names for the Ten Gods.
     */
    public static final String[] NAMES = { "比肩", "劫财", "食神", "伤官", "偏财", "正财", "七杀", "正官", "偏印", "正印" };

    /**
     * Constructs a {@code TenStar} instance with the specified index.
     *
     * @param index The index of the Ten God in the {@link #NAMES} array.
     */
    public TenStar(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code TenStar} instance with the specified name.
     *
     * @param name The name of the Ten God.
     */
    public TenStar(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code TenStar} instance from its index.
     *
     * @param index The index of the Ten God.
     * @return A new {@code TenStar} instance.
     */
    public static TenStar fromIndex(int index) {
        return new TenStar(index);
    }

    /**
     * Creates a {@code TenStar} instance from its name.
     *
     * @param name The name of the Ten God.
     * @return A new {@code TenStar} instance.
     */
    public static TenStar fromName(String name) {
        return new TenStar(name);
    }

    /**
     * Gets the next {@code TenStar} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code TenStar} instance.
     */
    public TenStar next(int n) {
        return fromIndex(nextIndex(n));
    }

}
