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
package org.miaixz.bus.core.center.date.culture.star.twelve;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.Luck;

/**
 * Represents the Ecliptic (黄道) and Black Path (黑道) concepts in Chinese traditional calendar, indicating auspicious or
 * inauspicious days. This class extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Ecliptic extends Samsara {

    /**
     * Array of names for the Ecliptic and Black Path.
     */
    public static final String[] NAMES = { "黄道", "黑道" };

    /**
     * Constructs an {@code Ecliptic} instance with the specified index.
     *
     * @param index The index of the path (0 for Ecliptic, 1 for Black Path) in the {@link #NAMES} array.
     */
    public Ecliptic(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs an {@code Ecliptic} instance with the specified name.
     *
     * @param name The name of the path.
     */
    public Ecliptic(String name) {
        super(NAMES, name);
    }

    /**
     * Creates an {@code Ecliptic} instance from its index.
     *
     * @param index The index of the path.
     * @return A new {@code Ecliptic} instance.
     */
    public static Ecliptic fromIndex(int index) {
        return new Ecliptic(index);
    }

    /**
     * Creates an {@code Ecliptic} instance from its name.
     *
     * @param name The name of the path.
     * @return A new {@code Ecliptic} instance.
     */
    public static Ecliptic fromName(String name) {
        return new Ecliptic(name);
    }

    /**
     * Gets the next {@code Ecliptic} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Ecliptic} instance.
     */
    public Ecliptic next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Determines the auspiciousness (luck) of the current Ecliptic or Black Path.
     *
     * @return The {@link Luck} associated with this path.
     */
    public Luck getLuck() {
        return Luck.fromIndex(index);
    }

}
