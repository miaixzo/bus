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
package org.miaixz.bus.core.center.date.culture.star.nine;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents the Nine Stars of the Northern Dipper (北斗九星) in Chinese astrology. This class extends {@link Samsara} to
 * manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Dipper extends Samsara {

    /**
     * Array of names for the Nine Stars of the Northern Dipper.
     */
    public static final String[] NAMES = { "天枢", "天璇", "天玑", "天权", "玉衡", "开阳", "摇光", "洞明", "隐元" };

    /**
     * Constructs a {@code Dipper} instance with the specified index.
     *
     * @param index The index of the star in the {@link #NAMES} array.
     */
    public Dipper(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Dipper} instance with the specified name.
     *
     * @param name The name of the star.
     */
    public Dipper(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Dipper} instance from its index.
     *
     * @param index The index of the star.
     * @return A new {@code Dipper} instance.
     */
    public static Dipper fromIndex(int index) {
        return new Dipper(index);
    }

    /**
     * Creates a {@code Dipper} instance from its name.
     *
     * @param name The name of the star.
     * @return A new {@code Dipper} instance.
     */
    public static Dipper fromName(String name) {
        return new Dipper(name);
    }

    /**
     * Gets the next {@code Dipper} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Dipper} instance.
     */
    public Dipper next(int n) {
        return fromIndex(nextIndex(n));
    }

}
