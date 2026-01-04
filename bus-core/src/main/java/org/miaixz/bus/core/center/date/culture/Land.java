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
 * Represents the Nine Lands (九野) in Chinese traditional culture, often associated with directions. This class extends
 * {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Land extends Samsara {

    /**
     * Array of names for the Nine Lands.
     */
    public static final String[] NAMES = { "玄天", "朱天", "苍天", "阳天", "钧天", "幽天", "颢天", "变天", "炎天" };

    /**
     * Constructs a {@code Land} instance with the specified index.
     *
     * @param index The index of the Land in the {@link #NAMES} array.
     */
    public Land(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Land} instance with the specified name.
     *
     * @param name The name of the Land.
     */
    public Land(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Land} instance from its index.
     *
     * @param index The index of the Land.
     * @return A new {@code Land} instance.
     */
    public static Land fromIndex(int index) {
        return new Land(index);
    }

    /**
     * Creates a {@code Land} instance from its name.
     *
     * @param name The name of the Land.
     * @return A new {@code Land} instance.
     */
    public static Land fromName(String name) {
        return new Land(name);
    }

    /**
     * Gets the next {@code Land} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Land} instance.
     */
    public Land next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Gets the corresponding {@link Direction} for this Land.
     *
     * @return The {@link Direction} associated with this Land.
     */
    public Direction getDirection() {
        return Direction.fromIndex(index);
    }

}
