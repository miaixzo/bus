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
package org.miaixz.bus.core.center.date.culture.ren;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.Element;
import org.miaixz.bus.core.center.date.culture.Luck;

/**
 * Represents the Minor Liu Ren (小六壬) divination system, which consists of six states. This class extends
 * {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MinorRen extends Samsara {

    /**
     * Array of names for the six states of Minor Liu Ren.
     */
    public static final String[] NAMES = { "大安", "留连", "速喜", "赤口", "小吉", "空亡" };

    /**
     * Constructs a {@code MinorRen} instance with the specified index.
     *
     * @param index The index of the state in the {@link #NAMES} array.
     */
    public MinorRen(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code MinorRen} instance with the specified name.
     *
     * @param name The name of the state.
     */
    public MinorRen(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code MinorRen} instance from its index.
     *
     * @param index The index of the state.
     * @return A new {@code MinorRen} instance.
     */
    public static MinorRen fromIndex(int index) {
        return new MinorRen(index);
    }

    /**
     * Creates a {@code MinorRen} instance from its name.
     *
     * @param name The name of the state.
     * @return A new {@code MinorRen} instance.
     */
    public static MinorRen fromName(String name) {
        return new MinorRen(name);
    }

    /**
     * Gets the next {@code MinorRen} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code MinorRen} instance.
     */
    public MinorRen next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Determines the auspiciousness (luck) of the current Minor Liu Ren state.
     *
     * @return The {@link Luck} associated with this state. Returns {@code Luck.GOOD} for even indices, and
     *         {@code Luck.BAD} for odd indices.
     */
    public Luck getLuck() {
        return Luck.fromIndex(index % 2);
    }

    /**
     * Gets the corresponding {@link Element} (五行) for this Minor Liu Ren state.
     *
     * @return The {@link Element} associated with this state.
     */
    public Element getElement() {
        return Element.fromIndex(new int[] { 0, 4, 1, 3, 0, 2 }[index]);
    }

}
