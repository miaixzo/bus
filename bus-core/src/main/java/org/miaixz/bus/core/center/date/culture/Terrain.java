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
 * Represents the Twelve Stages of Life (地势/长生十二神), a concept in Chinese metaphysics describing the life cycle of
 * elements. This class extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Terrain extends Samsara {

    /**
     * Array of names for the Twelve Stages of Life.
     */
    public static final String[] NAMES = { "长生", "沐浴", "冠带", "临官", "帝旺", "衰", "病", "死", "墓", "绝", "胎", "养" };

    /**
     * Constructs a {@code Terrain} instance with the specified index.
     *
     * @param index The index of the stage in the {@link #NAMES} array.
     */
    public Terrain(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Terrain} instance with the specified name.
     *
     * @param name The name of the stage.
     */
    public Terrain(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Terrain} instance from its index.
     *
     * @param index The index of the stage.
     * @return A new {@code Terrain} instance.
     */
    public static Terrain fromIndex(int index) {
        return new Terrain(index);
    }

    /**
     * Creates a {@code Terrain} instance from its name.
     *
     * @param name The name of the stage.
     * @return A new {@code Terrain} instance.
     */
    public static Terrain fromName(String name) {
        return new Terrain(name);
    }

    /**
     * Gets the next {@code Terrain} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Terrain} instance.
     */
    public Terrain next(int n) {
        return fromIndex(nextIndex(n));
    }

}
