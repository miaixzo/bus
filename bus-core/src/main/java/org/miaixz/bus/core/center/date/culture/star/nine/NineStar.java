/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.date.culture.star.nine;

import org.miaixz.bus.core.center.date.culture.Direction;
import org.miaixz.bus.core.center.date.culture.Element;
import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.lang.Normal;

/**
 * Represents the Nine Stars in Chinese metaphysics, often associated with Feng Shui and other divinatory
 * practices. This class extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 21+
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
     * Gets the corresponding {@link Element} for this Nine Star.
     *
     * @return The {@link Element} associated with this Nine Star.
     */
    public Element getElement() {
        return getDirection().getElement();
    }

    /**
     * Gets the corresponding {@link Dipper} for this Nine Star.
     *
     * @return The {@link Dipper} associated with this Nine Star.
     */
    public Dipper getDipper() {
        return Dipper.fromIndex(index);
    }

    /**
     * Gets the corresponding {@link Direction} for this Nine Star.
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
