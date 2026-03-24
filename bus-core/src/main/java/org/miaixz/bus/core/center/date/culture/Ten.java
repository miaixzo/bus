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
 * Represents the Ten-day Cycle (旬) in Chinese traditional calendar. This class extends {@link Samsara} to manage a
 * cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Ten extends Samsara {

    /**
     * Array of names for the Ten-day Cycles.
     */
    public static final String[] NAMES = { "甲子", "甲戌", "甲申", "甲午", "甲辰", "甲寅" };

    /**
     * Constructs a {@code Ten} instance with the specified index.
     *
     * @param index The index of the Ten-day Cycle in the {@link #NAMES} array.
     */
    public Ten(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Ten} instance with the specified name.
     *
     * @param name The name of the Ten-day Cycle.
     */
    public Ten(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Ten} instance from its index.
     *
     * @param index The index of the Ten-day Cycle.
     * @return A new {@code Ten} instance.
     */
    public static Ten fromIndex(int index) {
        return new Ten(index);
    }

    /**
     * Creates a {@code Ten} instance from its name.
     *
     * @param name The name of the Ten-day Cycle.
     * @return A new {@code Ten} instance.
     */
    public static Ten fromName(String name) {
        return new Ten(name);
    }

    /**
     * Gets the next {@code Ten} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Ten} instance.
     */
    public Ten next(int n) {
        return fromIndex(nextIndex(n));
    }

}
