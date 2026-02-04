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
 * Represents the concept of "Na Yin" (纳音), which associates a sound element with each of the 60 Jiazi cycles. This
 * class extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Sound extends Samsara {

    /**
     * Array of names for the Na Yin elements.
     */
    public static final String[] NAMES = { "海中金", "炉中火", "大林木", "路旁土", "剑锋金", "山头火", "涧下水", "城头土", "白蜡金", "杨柳木", "泉中水",
            "屋上土", "霹雳火", "松柏木", "长流水", "沙中金", "山下火", "平地木", "壁上土", "金箔金", "覆灯火", "天河水", "大驿土", "钗钏金", "桑柘木", "大溪水",
            "沙中土", "天上火", "石榴木", "大海水" };

    /**
     * Constructs a {@code Sound} instance with the specified index.
     *
     * @param index The index of the Na Yin element in the {@link #NAMES} array.
     */
    public Sound(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Sound} instance with the specified name.
     *
     * @param name The name of the Na Yin element.
     */
    public Sound(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Sound} instance from its index.
     *
     * @param index The index of the Na Yin element.
     * @return A new {@code Sound} instance.
     */
    public static Sound fromIndex(int index) {
        return new Sound(index);
    }

    /**
     * Creates a {@code Sound} instance from its name.
     *
     * @param name The name of the Na Yin element.
     * @return A new {@code Sound} instance.
     */
    public static Sound fromName(String name) {
        return new Sound(name);
    }

    /**
     * Gets the next {@code Sound} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Sound} instance.
     */
    public Sound next(int n) {
        return fromIndex(nextIndex(n));
    }

}
