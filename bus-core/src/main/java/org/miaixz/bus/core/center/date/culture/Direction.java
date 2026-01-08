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
package org.miaixz.bus.core.center.date.culture;

/**
 * Represents a cardinal or intercardinal direction (方位) in Chinese traditional culture. This class extends
 * {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Direction extends Samsara {

    /**
     * Array of names for the directions, ordered according to the Later Heaven Bagua (后天八卦): (0-North, 1-Southwest,
     * 2-East, 3-Southeast, 4-Center, 5-Northwest, 6-West, 7-Northeast, 8-South).
     */
    public static final String[] NAMES = { "北", "西南", "东", "东南", "中", "西北", "西", "东北", "南" };

    /**
     * Constructs a {@code Direction} instance with the specified index.
     *
     * @param index The index of the direction in the {@link #NAMES} array.
     */
    public Direction(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Direction} instance with the specified name.
     *
     * @param name The name of the direction.
     */
    public Direction(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Direction} instance from its index.
     *
     * @param index The index of the direction.
     * @return A new {@code Direction} instance.
     */
    public static Direction fromIndex(int index) {
        return new Direction(index);
    }

    /**
     * Creates a {@code Direction} instance from its name.
     *
     * @param name The name of the direction.
     * @return A new {@code Direction} instance.
     */
    public static Direction fromName(String name) {
        return new Direction(name);
    }

    /**
     * Gets the next {@code Direction} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Direction} instance.
     */
    public Direction next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Gets the corresponding {@link Land} (九野) for this Direction.
     *
     * @return The {@link Land} associated with this Direction.
     */
    public Land getLand() {
        return Land.fromIndex(index);
    }

    /**
     * Gets the corresponding {@link Element} (五行) for this Direction.
     *
     * @return The {@link Element} associated with this Direction.
     */
    public Element getElement() {
        return Element.fromIndex(new int[] { 4, 2, 0, 0, 2, 3, 3, 2, 1 }[index]);
    }

}
