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
 * Represents the concept of auspiciousness (吉) and inauspiciousness (凶) in Chinese traditional culture. This class
 * extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Luck extends Samsara {

    /**
     * Array of names for Luck, typically "Auspicious" (吉) and "Inauspicious" (凶).
     */
    public static final String[] NAMES = { "吉", "凶" };

    /**
     * Constructs a {@code Luck} instance with the specified index.
     *
     * @param index The index of the Luck in the {@link #NAMES} array.
     */
    public Luck(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Luck} instance with the specified name.
     *
     * @param name The name of the Luck.
     */
    public Luck(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Luck} instance from its index.
     *
     * @param index The index of the Luck.
     * @return A new {@code Luck} instance.
     */
    public static Luck fromIndex(int index) {
        return new Luck(index);
    }

    /**
     * Creates a {@code Luck} instance from its name.
     *
     * @param name The name of the Luck.
     * @return A new {@code Luck} instance.
     */
    public static Luck fromName(String name) {
        return new Luck(name);
    }

    /**
     * Gets the next {@code Luck} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Luck} instance.
     */
    public Luck next(int n) {
        return fromIndex(nextIndex(n));
    }

}
