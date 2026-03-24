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
 * Represents the Nine Lands (九野) in Chinese traditional culture, often associated with directions. This class extends
 * {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 21+
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
