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
package org.miaixz.bus.core.center.date.culture.ren;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.Element;
import org.miaixz.bus.core.center.date.culture.Luck;

/**
 * Represents the Minor Liu Ren (小六壬) divination system, which consists of six states. This class extends
 * {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MinorRen extends Samsara {

    /**
     * Array of names for the six states of Minor Liu Ren.
     */
    public static final String[] NAMES = { "大安", "留连", "速喜", "赤口", "小吉", "空亡" };

    /**
     * Constructs a {@code MinorRen} instance with the specified index.
     *
     * @param index The index of the state in the {@link #NAMES} array.
     */
    public MinorRen(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code MinorRen} instance with the specified name.
     *
     * @param name The name of the state.
     */
    public MinorRen(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code MinorRen} instance from its index.
     *
     * @param index The index of the state.
     * @return A new {@code MinorRen} instance.
     */
    public static MinorRen fromIndex(int index) {
        return new MinorRen(index);
    }

    /**
     * Creates a {@code MinorRen} instance from its name.
     *
     * @param name The name of the state.
     * @return A new {@code MinorRen} instance.
     */
    public static MinorRen fromName(String name) {
        return new MinorRen(name);
    }

    /**
     * Gets the next {@code MinorRen} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code MinorRen} instance.
     */
    public MinorRen next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Determines the auspiciousness (luck) of the current Minor Liu Ren state.
     *
     * @return The {@link Luck} associated with this state. Returns {@code Luck.GOOD} for even indices, and
     *         {@code Luck.BAD} for odd indices.
     */
    public Luck getLuck() {
        return Luck.fromIndex(index % 2);
    }

    /**
     * Gets the corresponding {@link Element} (五行) for this Minor Liu Ren state.
     *
     * @return The {@link Element} associated with this state.
     */
    public Element getElement() {
        return Element.fromIndex(new int[] { 0, 4, 1, 3, 0, 2 }[index]);
    }

}
