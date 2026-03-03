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
package org.miaixz.bus.core.center.date.culture.star.six;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents the Rokuyo (六曜) or Six-day Cycle, also known as Koumei Rokuyo-sei (孔明六曜星), a system of divination used in
 * Japan. This class extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SixStar extends Samsara {

    /**
     * Array of names for the Rokuyo days.
     */
    public static final String[] NAMES = { "先胜", "友引", "先负", "佛灭", "大安", "赤口" };

    /**
     * Constructs a {@code SixStar} instance with the specified index.
     *
     * @param index The index of the Rokuyo day in the {@link #NAMES} array.
     */
    public SixStar(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code SixStar} instance with the specified name.
     *
     * @param name The name of the Rokuyo day.
     */
    public SixStar(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code SixStar} instance from its index.
     *
     * @param index The index of the Rokuyo day.
     * @return A new {@code SixStar} instance.
     */
    public static SixStar fromIndex(int index) {
        return new SixStar(index);
    }

    /**
     * Creates a {@code SixStar} instance from its name.
     *
     * @param name The name of the Rokuyo day.
     * @return A new {@code SixStar} instance.
     */
    public static SixStar fromName(String name) {
        return new SixStar(name);
    }

    /**
     * Gets the next {@code SixStar} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code SixStar} instance.
     */
    public SixStar next(int n) {
        return fromIndex(nextIndex(n));
    }

}
