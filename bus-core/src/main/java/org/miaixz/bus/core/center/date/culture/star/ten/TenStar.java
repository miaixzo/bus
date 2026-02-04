/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.date.culture.star.ten;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents the Ten Gods (十神) in Chinese Bazi (Four Pillars of Destiny) astrology. This class extends {@link Samsara}
 * to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TenStar extends Samsara {

    /**
     * Array of names for the Ten Gods.
     */
    public static final String[] NAMES = { "比肩", "劫财", "食神", "伤官", "偏财", "正财", "七杀", "正官", "偏印", "正印" };

    /**
     * Constructs a {@code TenStar} instance with the specified index.
     *
     * @param index The index of the Ten God in the {@link #NAMES} array.
     */
    public TenStar(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code TenStar} instance with the specified name.
     *
     * @param name The name of the Ten God.
     */
    public TenStar(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code TenStar} instance from its index.
     *
     * @param index The index of the Ten God.
     * @return A new {@code TenStar} instance.
     */
    public static TenStar fromIndex(int index) {
        return new TenStar(index);
    }

    /**
     * Creates a {@code TenStar} instance from its name.
     *
     * @param name The name of the Ten God.
     * @return A new {@code TenStar} instance.
     */
    public static TenStar fromName(String name) {
        return new TenStar(name);
    }

    /**
     * Gets the next {@code TenStar} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code TenStar} instance.
     */
    public TenStar next(int n) {
        return fromIndex(nextIndex(n));
    }

}
