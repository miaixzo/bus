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
 * Represents a "Yun" (运), a 20-year cycle in Chinese traditional calendar. Three "Yun" cycles constitute one "Yuan"
 * (元). This class extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Twenty extends Samsara {

    /**
     * Array of names for the Twenty-year Cycles (Yun).
     */
    public static final String[] NAMES = { "一运", "二运", "三运", "四运", "五运", "六运", "七运", "八运", "九运" };

    /**
     * Constructs a {@code Twenty} instance with the specified index.
     *
     * @param index The index of the Twenty-year Cycle in the {@link #NAMES} array.
     */
    public Twenty(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Twenty} instance with the specified name.
     *
     * @param name The name of the Twenty-year Cycle.
     */
    public Twenty(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Twenty} instance from its index.
     *
     * @param index The index of the Twenty-year Cycle.
     * @return A new {@code Twenty} instance.
     */
    public static Twenty fromIndex(int index) {
        return new Twenty(index);
    }

    /**
     * Creates a {@code Twenty} instance from its name.
     *
     * @param name The name of the Twenty-year Cycle.
     * @return A new {@code Twenty} instance.
     */
    public static Twenty fromName(String name) {
        return new Twenty(name);
    }

    /**
     * Gets the next {@code Twenty} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Twenty} instance.
     */
    public Twenty next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Gets the corresponding {@link Sixty} (元) for this Twenty-year Cycle.
     *
     * @return The {@link Sixty} associated with this Twenty-year Cycle.
     */
    public Sixty getSixty() {
        return Sixty.fromIndex(index / 3);
    }

}
