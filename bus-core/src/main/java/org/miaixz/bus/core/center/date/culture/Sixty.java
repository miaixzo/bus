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
 * Represents the Sixty-year Cycle (元), a larger temporal unit in Chinese traditional calendar. Each cycle is composed
 * of 60 years, and there are three such cycles: Upper, Middle, and Lower. This class extends {@link Samsara} to manage
 * a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Sixty extends Samsara {

    /**
     * Array of names for the Sixty-year Cycles.
     */
    public static final String[] NAMES = { "上元", "中元", "下元" };

    /**
     * Constructs a {@code Sixty} instance with the specified index.
     *
     * @param index The index of the Sixty-year Cycle in the {@link #NAMES} array.
     */
    public Sixty(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Sixty} instance with the specified name.
     *
     * @param name The name of the Sixty-year Cycle.
     */
    public Sixty(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Sixty} instance from its index.
     *
     * @param index The index of the Sixty-year Cycle.
     * @return A new {@code Sixty} instance.
     */
    public static Sixty fromIndex(int index) {
        return new Sixty(index);
    }

    /**
     * Creates a {@code Sixty} instance from its name.
     *
     * @param name The name of the Sixty-year Cycle.
     * @return A new {@code Sixty} instance.
     */
    public static Sixty fromName(String name) {
        return new Sixty(name);
    }

    /**
     * Gets the next {@code Sixty} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Sixty} instance.
     */
    public Sixty next(int n) {
        return fromIndex(nextIndex(n));
    }

}
