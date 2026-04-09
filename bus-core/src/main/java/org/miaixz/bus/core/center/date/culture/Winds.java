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
 * Represents a cardinal direction or "Zone" (宫) in Chinese traditional culture. This class extends {@link Samsara} to
 * manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Winds extends Samsara {

    /**
     * Array of names for the cardinal directions/zones.
     */
    public static final String[] NAMES = { "东", "北", "西", "南" };

    /**
     * Constructs a {@code Zone} instance with the specified index.
     *
     * @param index The index of the Zone in the {@link #NAMES} array.
     */
    public Winds(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Zone} instance with the specified name.
     *
     * @param name The name of the Zone.
     */
    public Winds(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Zone} instance from its index.
     *
     * @param index The index of the Zone.
     * @return A new {@code Zone} instance.
     */
    public static Winds fromIndex(int index) {
        return new Winds(index);
    }

    /**
     * Creates a {@code Zone} instance from its name.
     *
     * @param name The name of the Zone.
     * @return A new {@code Zone} instance.
     */
    public static Winds fromName(String name) {
        return new Winds(name);
    }

    /**
     * Gets the next {@code Zone} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Zone} instance.
     */
    public Winds next(int n) {
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
     * Gets the corresponding {@link Beast} for this zone.
     *
     * @return The {@link Beast} associated with this Zone.
     */
    public Beast getBeast() {
        return Beast.fromIndex(index);
    }

}
