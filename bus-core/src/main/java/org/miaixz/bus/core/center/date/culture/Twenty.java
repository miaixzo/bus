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
 * Represents a "Yun" (运), a 20-year cycle in Chinese traditional calendar. Three "Yun" cycles constitute one "Yuan"
 * (元). This class extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Twenty extends Samsara {

    /**
     * Array of names for the Twenty-year Cycles (Yun).
     */
    public static final String[] NAMES = { "一运", "二运", "三运", "四运", "五运", "六运", "七运", "八运", "九运" };

    /**
     * Constructs a {@code Twenty} instance with the specified index.
     *
     * @param index The index of the Twenty-year Cycle in the {@link #NAMES} array.
     */
    public Twenty(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Twenty} instance with the specified name.
     *
     * @param name The name of the Twenty-year Cycle.
     */
    public Twenty(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Twenty} instance from its index.
     *
     * @param index The index of the Twenty-year Cycle.
     * @return A new {@code Twenty} instance.
     */
    public static Twenty fromIndex(int index) {
        return new Twenty(index);
    }

    /**
     * Creates a {@code Twenty} instance from its name.
     *
     * @param name The name of the Twenty-year Cycle.
     * @return A new {@code Twenty} instance.
     */
    public static Twenty fromName(String name) {
        return new Twenty(name);
    }

    /**
     * Gets the next {@code Twenty} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Twenty} instance.
     */
    public Twenty next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Gets the corresponding {@link Sixty} (元) for this Twenty-year Cycle.
     *
     * @return The {@link Sixty} associated with this Twenty-year Cycle.
     */
    public Sixty getSixty() {
        return Sixty.fromIndex(index / 3);
    }

}
