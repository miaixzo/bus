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
 * Represents a cardinal or intercardinal direction (方位) in Chinese traditional culture. This class extends
 * {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 21+
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
