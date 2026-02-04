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
package org.miaixz.bus.core.center.date.culture.star.twelve;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents the Twelve Yellow and Black Path Deities (黄道黑道十二神) in Chinese traditional calendar. These deities are used
 * to determine the auspiciousness of a day. This class extends {@link Samsara} to manage a cyclical list of these
 * entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TwelveStar extends Samsara {

    /**
     * Array of names for the Twelve Yellow and Black Path Deities.
     */
    public static final String[] NAMES = { "青龙", "明堂", "天刑", "朱雀", "金匮", "天德", "白虎", "玉堂", "天牢", "玄武", "司命", "勾陈" };

    /**
     * Constructs a {@code TwelveStar} instance with the specified index.
     *
     * @param index The index of the deity in the {@link #NAMES} array.
     */
    public TwelveStar(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code TwelveStar} instance with the specified name.
     *
     * @param name The name of the deity.
     */
    public TwelveStar(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code TwelveStar} instance from its index.
     *
     * @param index The index of the deity.
     * @return A new {@code TwelveStar} instance.
     */
    public static TwelveStar fromIndex(int index) {
        return new TwelveStar(index);
    }

    /**
     * Creates a {@code TwelveStar} instance from its name.
     *
     * @param name The name of the deity.
     * @return A new {@code TwelveStar} instance.
     */
    public static TwelveStar fromName(String name) {
        return new TwelveStar(name);
    }

    /**
     * Gets the next {@code TwelveStar} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code TwelveStar} instance.
     */
    public TwelveStar next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Gets the corresponding {@link Ecliptic} (黄道黑道) for this Twelve Star.
     *
     * @return The {@link Ecliptic} associated with this Twelve Star.
     */
    public Ecliptic getEcliptic() {
        return Ecliptic.fromIndex(new int[] { 0, 0, 1, 1, 0, 0, 1, 0, 1, 1, 0, 1 }[index]);
    }

}
