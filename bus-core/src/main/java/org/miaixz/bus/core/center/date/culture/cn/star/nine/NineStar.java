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
package org.miaixz.bus.core.center.date.culture.cn.star.nine;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.cn.Direction;
import org.miaixz.bus.core.center.date.culture.cn.Element;
import org.miaixz.bus.core.lang.Normal;

/**
 * Represents the Nine Stars (九星) in Chinese metaphysics, often associated with Feng Shui and other divinatory
 * practices. This class extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NineStar extends Samsara {

    /**
     * Array of names for the Nine Stars.
     */
    public static final String[] NAMES = { "一", "二", "三", "四", "五", "六", "七", "八", "九" };

    /**
     * Constructs a {@code NineStar} instance with the specified index.
     *
     * @param index The index of the star in the {@link #NAMES} array.
     */
    public NineStar(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code NineStar} instance with the specified name.
     *
     * @param name The name of the star.
     */
    public NineStar(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code NineStar} instance from its index.
     *
     * @param index The index of the star.
     * @return A new {@code NineStar} instance.
     */
    public static NineStar fromIndex(int index) {
        return new NineStar(index);
    }

    /**
     * Creates a {@code NineStar} instance from its name.
     *
     * @param name The name of the star.
     * @return A new {@code NineStar} instance.
     */
    public static NineStar fromName(String name) {
        return new NineStar(name);
    }

    /**
     * Gets the next {@code NineStar} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code NineStar} instance.
     */
    public NineStar next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Gets the color associated with this Nine Star.
     *
     * @return The color as a string.
     */
    public String getColor() {
        return Normal.COLOR[index];
    }

    /**
     * Gets the corresponding {@link Element} (五行) for this Nine Star.
     *
     * @return The {@link Element} associated with this Nine Star.
     */
    public Element getElement() {
        return Element.fromIndex(new int[] { 4, 2, 0, 0, 2, 3, 3, 2, 1 }[index]);
    }

    /**
     * Gets the corresponding {@link Dipper} (北斗九星) for this Nine Star.
     *
     * @return The {@link Dipper} associated with this Nine Star.
     */
    public Dipper getDipper() {
        return Dipper.fromIndex(index);
    }

    /**
     * Gets the corresponding {@link Direction} (方位) for this Nine Star.
     *
     * @return The {@link Direction} associated with this Nine Star.
     */
    public Direction getDirection() {
        return Direction.fromIndex(index);
    }

    /**
     * Returns a string representation of this Nine Star, including its name, color, and element.
     *
     * @return A string representation of the Nine Star.
     */
    @Override
    public String toString() {
        return getName() + getColor() + getElement();
    }

}
