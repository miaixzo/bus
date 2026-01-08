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
package org.miaixz.bus.core.center.date.culture.star.nine;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents the Nine Stars of the Northern Dipper (北斗九星) in Chinese astrology. This class extends {@link Samsara} to
 * manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Dipper extends Samsara {

    /**
     * Array of names for the Nine Stars of the Northern Dipper.
     */
    public static final String[] NAMES = { "天枢", "天璇", "天玑", "天权", "玉衡", "开阳", "摇光", "洞明", "隐元" };

    /**
     * Constructs a {@code Dipper} instance with the specified index.
     *
     * @param index The index of the star in the {@link #NAMES} array.
     */
    public Dipper(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Dipper} instance with the specified name.
     *
     * @param name The name of the star.
     */
    public Dipper(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Dipper} instance from its index.
     *
     * @param index The index of the star.
     * @return A new {@code Dipper} instance.
     */
    public static Dipper fromIndex(int index) {
        return new Dipper(index);
    }

    /**
     * Creates a {@code Dipper} instance from its name.
     *
     * @param name The name of the star.
     * @return A new {@code Dipper} instance.
     */
    public static Dipper fromName(String name) {
        return new Dipper(name);
    }

    /**
     * Gets the next {@code Dipper} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Dipper} instance.
     */
    public Dipper next(int n) {
        return fromIndex(nextIndex(n));
    }

}
