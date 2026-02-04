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
package org.miaixz.bus.core.center.date.culture;

/**
 * Represents the Twelve Day Officers (建除十二值神) in Chinese traditional calendar. These officers indicate the
 * auspiciousness of a day for various activities. This class extends {@link Samsara} to manage a cyclical list of these
 * entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Duty extends Samsara {

    /**
     * Array of names for the Twelve Day Officers.
     */
    public static final String[] NAMES = { "建", "除", "满", "平", "定", "执", "破", "危", "成", "收", "开", "闭" };

    /**
     * Constructs a {@code Duty} instance with the specified index.
     *
     * @param index The index of the Duty Officer in the {@link #NAMES} array.
     */
    public Duty(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Duty} instance with the specified name.
     *
     * @param name The name of the Duty Officer.
     */
    public Duty(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Duty} instance from its index.
     *
     * @param index The index of the Duty Officer.
     * @return A new {@code Duty} instance.
     */
    public static Duty fromIndex(int index) {
        return new Duty(index);
    }

    /**
     * Creates a {@code Duty} instance from its name.
     *
     * @param name The name of the Duty Officer.
     * @return A new {@code Duty} instance.
     */
    public static Duty fromName(String name) {
        return new Duty(name);
    }

    /**
     * Gets the next {@code Duty} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Duty} instance.
     */
    public Duty next(int n) {
        return fromIndex(nextIndex(n));
    }

}
