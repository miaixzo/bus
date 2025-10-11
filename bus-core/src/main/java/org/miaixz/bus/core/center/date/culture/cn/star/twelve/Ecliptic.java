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
package org.miaixz.bus.core.center.date.culture.cn.star.twelve;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.cn.Luck;

/**
 * Represents the Ecliptic (黄道) and Black Path (黑道) concepts in Chinese traditional calendar, indicating auspicious or
 * inauspicious days. This class extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Ecliptic extends Samsara {

    /**
     * Array of names for the Ecliptic and Black Path.
     */
    public static final String[] NAMES = { "黄道", "黑道" };

    /**
     * Constructs an {@code Ecliptic} instance with the specified index.
     *
     * @param index The index of the path (0 for Ecliptic, 1 for Black Path) in the {@link #NAMES} array.
     */
    public Ecliptic(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs an {@code Ecliptic} instance with the specified name.
     *
     * @param name The name of the path.
     */
    public Ecliptic(String name) {
        super(NAMES, name);
    }

    /**
     * Creates an {@code Ecliptic} instance from its index.
     *
     * @param index The index of the path.
     * @return A new {@code Ecliptic} instance.
     */
    public static Ecliptic fromIndex(int index) {
        return new Ecliptic(index);
    }

    /**
     * Creates an {@code Ecliptic} instance from its name.
     *
     * @param name The name of the path.
     * @return A new {@code Ecliptic} instance.
     */
    public static Ecliptic fromName(String name) {
        return new Ecliptic(name);
    }

    /**
     * Gets the next {@code Ecliptic} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Ecliptic} instance.
     */
    public Ecliptic next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Determines the auspiciousness (luck) of the current Ecliptic or Black Path.
     *
     * @return The {@link Luck} associated with this path.
     */
    public Luck getLuck() {
        return Luck.fromIndex(index);
    }

}
