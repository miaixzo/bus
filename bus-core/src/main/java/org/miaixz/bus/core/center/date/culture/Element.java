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
package org.miaixz.bus.core.center.date.culture;

/**
 * Represents the Five Elements (五行: Wood, Fire, Earth, Metal, Water) in Chinese philosophy. This class extends
 * {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Element extends Samsara {

    /**
     * Array of names for the Five Elements.
     */
    public static final String[] NAMES = { "木", "火", "土", "金", "水" };

    /**
     * Constructs an {@code Element} instance with the specified index.
     *
     * @param index The index of the element in the {@link #NAMES} array.
     */
    public Element(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs an {@code Element} instance with the specified name.
     *
     * @param name The name of the element.
     */
    public Element(String name) {
        super(NAMES, name);
    }

    /**
     * Creates an {@code Element} instance from its index.
     *
     * @param index The index of the element.
     * @return A new {@code Element} instance.
     */
    public static Element fromIndex(int index) {
        return new Element(index);
    }

    /**
     * Creates an {@code Element} instance from its name.
     *
     * @param name The name of the element.
     * @return A new {@code Element} instance.
     */
    public static Element fromName(String name) {
        return new Element(name);
    }

    /**
     * Gets the next {@code Element} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Element} instance.
     */
    public Element next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Gets the element that this element reinforces (我生者).
     *
     * @return The reinforcing {@link Element}.
     */
    public Element getReinforce() {
        return next(1);
    }

    /**
     * Gets the element that this element restrains (我克者).
     *
     * @return The restraining {@link Element}.
     */
    public Element getRestrain() {
        return next(2);
    }

    /**
     * Gets the element that reinforces this element (生我者).
     *
     * @return The reinforced {@link Element}.
     */
    public Element getReinforced() {
        return next(-1);
    }

    /**
     * Gets the element that restrains this element (克我者).
     *
     * @return The restrained {@link Element}.
     */
    public Element getRestrained() {
        return next(-2);
    }

    /**
     * Gets the corresponding {@link Direction} (方位) for this Element.
     *
     * @return The {@link Direction} associated with this Element.
     */
    public Direction getDirection() {
        return Direction.fromIndex(new int[] { 2, 8, 4, 6, 0 }[index]);
    }

}
