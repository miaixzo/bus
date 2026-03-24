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
 * Represents the Four Auspicious Beasts (神兽) in Chinese mythology, associated with cardinal directions. This class
 * extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Beast extends Samsara {

    /**
     * Array of names for the Four Auspicious Beasts.
     */
    public static final String[] NAMES = { "青龙", "玄武", "白虎", "朱雀" };

    /**
     * Constructs a {@code Beast} instance with the specified index.
     *
     * @param index The index of the Beast in the {@link #NAMES} array.
     */
    public Beast(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Beast} instance with the specified name.
     *
     * @param name The name of the Beast.
     */
    public Beast(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Beast} instance from its index.
     *
     * @param index The index of the Beast.
     * @return A new {@code Beast} instance.
     */
    public static Beast fromIndex(int index) {
        return new Beast(index);
    }

    /**
     * Creates a {@code Beast} instance from its name.
     *
     * @param name The name of the Beast.
     * @return A new {@code Beast} instance.
     */
    public static Beast fromName(String name) {
        return new Beast(name);
    }

    /**
     * Gets the next {@code Beast} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Beast} instance.
     */
    public Beast next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Gets the corresponding {@link Winds} (宫) for this Beast.
     *
     * @return The {@link Winds} associated with this Beast.
     */
    public Winds getZone() {
        return Winds.fromIndex(index);
    }

}
