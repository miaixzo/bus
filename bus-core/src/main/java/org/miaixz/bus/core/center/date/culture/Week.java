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

import org.miaixz.bus.core.center.date.culture.star.seven.SevenStar;

/**
 * Represents the days of the week (星期) in Chinese culture. This class extends {@link Samsara} to manage a cyclical list
 * of these entities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Week extends Samsara {

    /**
     * Array of names for the days of the week, retrieved from the English Week enumeration.
     */
    public static final String[] NAMES = org.miaixz.bus.core.center.date.Week.get("name");

    /**
     * Array representing the ordinal number of the week (e.g., "First Week", "Second Week").
     */
    public static final String[] WHICH = { "第一周", "第二周", "第三周", "第四周", "第五周", "第六周" };

    /**
     * Constructs a {@code Week} instance with the specified index.
     *
     * @param index The index of the day of the week in the {@link #NAMES} array.
     */
    public Week(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Week} instance with the specified name.
     *
     * @param name The name of the day of the week.
     */
    public Week(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Week} instance from its index.
     *
     * @param index The index of the day of the week.
     * @return A new {@code Week} instance.
     */
    public static Week fromIndex(int index) {
        return new Week(index);
    }

    /**
     * Creates a {@code Week} instance from its name.
     *
     * @param name The name of the day of the week.
     * @return A new {@code Week} instance.
     */
    public static Week fromName(String name) {
        return new Week(name);
    }

    /**
     * Gets the next {@code Week} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Week} instance.
     */
    public Week next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Gets the corresponding {@link SevenStar} for this day of the week.
     *
     * @return The {@link SevenStar} associated with this day.
     */
    public SevenStar getSevenStar() {
        return SevenStar.fromIndex(index);
    }

}
