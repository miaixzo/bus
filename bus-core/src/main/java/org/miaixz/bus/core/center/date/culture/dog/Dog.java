/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.date.culture.dog;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents the "Sanfu" (三伏), also known as Dog Days, which are three periods of hot weather in the Chinese calendar.
 * This class extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Dog extends Samsara {

    /**
     * Array of names for the Sanfu periods: "初伏" (Chufu - initial fu), "中伏" (Zhongfu - middle fu), "末伏" (Mofu - final
     * fu).
     */
    public static final String[] NAMES = { "初伏", "中伏", "末伏" };

    /**
     * Constructs a {@code Dog} instance with the specified name.
     *
     * @param name The name of the Sanfu period.
     */
    public Dog(String name) {
        super(NAMES, name);
    }

    /**
     * Constructs a {@code Dog} instance with the specified index.
     *
     * @param index The index of the Sanfu period in the {@link #NAMES} array.
     */
    public Dog(int index) {
        super(NAMES, index);
    }

    /**
     * Creates a {@code Dog} instance from its name.
     *
     * @param name The name of the Sanfu period.
     * @return A new {@code Dog} instance.
     */
    public static Dog fromName(String name) {
        return new Dog(name);
    }

    /**
     * Creates a {@code Dog} instance from its index.
     *
     * @param index The index of the Sanfu period.
     * @return A new {@code Dog} instance.
     */
    public static Dog fromIndex(int index) {
        return new Dog(index);
    }

    /**
     * Gets the next {@code Dog} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Dog} instance.
     */
    public Dog next(int n) {
        return fromIndex(nextIndex(n));
    }

}
