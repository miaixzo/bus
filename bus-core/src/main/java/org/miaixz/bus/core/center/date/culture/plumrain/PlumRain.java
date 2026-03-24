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
package org.miaixz.bus.core.center.date.culture.plumrain;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents the "Plum Rain" (梅雨) Quarter, a period of continuous rain in East Asia. This class extends {@link Samsara}
 * to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PlumRain extends Samsara {

    /**
     * Array of names for the Plum Rain periods: "入梅" (Entering Plum Rain) and "出梅" (Exiting Plum Rain).
     */
    public static final String[] NAMES = { "入梅", "出梅" };

    /**
     * Constructs a {@code PlumRain} instance with the specified name.
     *
     * @param name The name of the Plum Rain period.
     */
    public PlumRain(String name) {
        super(NAMES, name);
    }

    /**
     * Constructs a {@code PlumRain} instance with the specified index.
     *
     * @param index The index of the Plum Rain period in the {@link #NAMES} array.
     */
    public PlumRain(int index) {
        super(NAMES, index);
    }

    /**
     * Creates a {@code PlumRain} instance from its name.
     *
     * @param name The name of the Plum Rain period.
     * @return A new {@code PlumRain} instance.
     */
    public static PlumRain fromName(String name) {
        return new PlumRain(name);
    }

    /**
     * Creates a {@code PlumRain} instance from its index.
     *
     * @param index The index of the Plum Rain period.
     * @return A new {@code PlumRain} instance.
     */
    public static PlumRain fromIndex(int index) {
        return new PlumRain(index);
    }

    /**
     * Gets the next {@code PlumRain} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code PlumRain} instance.
     */
    public PlumRain next(int n) {
        return fromIndex(nextIndex(n));
    }

}
