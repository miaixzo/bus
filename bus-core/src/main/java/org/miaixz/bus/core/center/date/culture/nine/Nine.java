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
package org.miaixz.bus.core.center.date.culture.nine;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents the "Shu Jiu" (数九) or Counting Nine periods, which are nine nine-day periods following the Winter
 * Solstice. This class extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Nine extends Samsara {

    /**
     * Array of names for the Counting Nine periods.
     */
    public static final String[] NAMES = { "一九", "二九", "三九", "四九", "五九", "六九", "七九", "八九", "九九" };

    /**
     * Constructs a {@code Nine} instance with the specified name.
     *
     * @param name The name of the Counting Nine period.
     */
    public Nine(String name) {
        super(NAMES, name);
    }

    /**
     * Constructs a {@code Nine} instance with the specified index.
     *
     * @param index The index of the Counting Nine period in the {@link #NAMES} array.
     */
    public Nine(int index) {
        super(NAMES, index);
    }

    /**
     * Creates a {@code Nine} instance from its name.
     *
     * @param name The name of the Counting Nine period.
     * @return A new {@code Nine} instance.
     */
    public static Nine fromName(String name) {
        return new Nine(name);
    }

    /**
     * Creates a {@code Nine} instance from its index.
     *
     * @param index The index of the Counting Nine period.
     * @return A new {@code Nine} instance.
     */
    public static Nine fromIndex(int index) {
        return new Nine(index);
    }

    /**
     * Gets the next {@code Nine} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Nine} instance.
     */
    public Nine next(int n) {
        return fromIndex(nextIndex(n));
    }

}
