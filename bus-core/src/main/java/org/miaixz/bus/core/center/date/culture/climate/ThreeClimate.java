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
package org.miaixz.bus.core.center.date.culture.climate;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents a "Sanhou" (三候) or Three Climates, which are three five-day periods within a solar term. This class
 * extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ThreeClimate extends Samsara {

    /**
     * Array of names for the Three Climates.
     */
    public static final String[] NAMES = { "初候", "二候", "三候" };

    /**
     * Constructs a {@code ThreeClimate} instance with the specified name.
     *
     * @param name The name of the Climate.
     */
    public ThreeClimate(String name) {
        super(NAMES, name);
    }

    /**
     * Constructs a {@code ThreeClimate} instance with the specified index.
     *
     * @param index The index of the Climate in the {@link #NAMES} array.
     */
    public ThreeClimate(int index) {
        super(NAMES, index);
    }

    /**
     * Creates a {@code ThreeClimate} instance from its name.
     *
     * @param name The name of the Climate.
     * @return A new {@code ThreeClimate} instance.
     */
    public static ThreeClimate fromName(String name) {
        return new ThreeClimate(name);
    }

    /**
     * Creates a {@code ThreeClimate} instance from its index.
     *
     * @param index The index of the Climate.
     * @return A new {@code ThreeClimate} instance.
     */
    public static ThreeClimate fromIndex(int index) {
        return new ThreeClimate(index);
    }

    /**
     * Gets the next {@code ThreeClimate} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code ThreeClimate} instance.
     */
    public ThreeClimate next(int n) {
        return fromIndex(nextIndex(n));
    }

}
