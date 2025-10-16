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
 * Represents a cardinal direction or "Zone" (宫) in Chinese traditional culture. This class extends {@link Samsara} to
 * manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Zone extends Samsara {

    /**
     * Array of names for the cardinal directions/zones.
     */
    public static final String[] NAMES = { "东", "北", "西", "南" };

    /**
     * Constructs a {@code Zone} instance with the specified index.
     *
     * @param index The index of the Zone in the {@link #NAMES} array.
     */
    public Zone(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Zone} instance with the specified name.
     *
     * @param name The name of the Zone.
     */
    public Zone(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Zone} instance from its index.
     *
     * @param index The index of the Zone.
     * @return A new {@code Zone} instance.
     */
    public static Zone fromIndex(int index) {
        return new Zone(index);
    }

    /**
     * Creates a {@code Zone} instance from its name.
     *
     * @param name The name of the Zone.
     * @return A new {@code Zone} instance.
     */
    public static Zone fromName(String name) {
        return new Zone(name);
    }

    /**
     * Gets the next {@code Zone} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Zone} instance.
     */
    public Zone next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Gets the corresponding {@link Direction} for this Zone.
     *
     * @return The {@link Direction} associated with this Zone.
     */
    public Direction getDirection() {
        return Direction.fromName(getName());
    }

    /**
     * Gets the corresponding {@link Beast} (神兽) for this Zone.
     *
     * @return The {@link Beast} associated with this Zone.
     */
    public Beast getBeast() {
        return Beast.fromIndex(index);
    }

}
