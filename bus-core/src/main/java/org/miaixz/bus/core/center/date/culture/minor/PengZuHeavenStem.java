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
package org.miaixz.bus.core.center.date.culture.minor;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents the Peng Zu Bai Ji (彭祖百忌) associated with Heavenly Stems (天干), a traditional Chinese calendar concept of
 * taboos. The names describe actions to avoid on days corresponding to specific Heavenly Stems. (Reference:
 * 《天干六甲胎神歌》甲己之日占在门，乙庚碓磨休移动。丙辛厨灶莫相干，丁壬仓库忌修弄。戊癸房床若移整，犯之孕妇堕孩童。) This class extends {@link Samsara} to manage a cyclical
 * list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PengZuHeavenStem extends Samsara {

    /**
     * Array of taboo descriptions for each Heavenly Stem.
     */
    public static final String[] NAMES = { "甲不开仓财物耗散", "乙不栽植千株不长", "丙不修灶必见灾殃", "丁不剃头头必生疮", "戊不受田田主不祥", "己不破券二比并亡",
            "庚不经络织机虚张", "辛不合酱主人不尝", "壬不泱水更难提防", "癸不词讼理弱敌强" };

    /**
     * Constructs a {@code PengZuHeavenStem} instance with the specified name.
     *
     * @param name The name of the taboo.
     */
    public PengZuHeavenStem(String name) {
        super(NAMES, name);
    }

    /**
     * Constructs a {@code PengZuHeavenStem} instance with the specified index.
     *
     * @param index The index of the taboo in the {@link #NAMES} array.
     */
    public PengZuHeavenStem(int index) {
        super(NAMES, index);
    }

    /**
     * Creates a {@code PengZuHeavenStem} instance from its name.
     *
     * @param name The name of the taboo.
     * @return A new {@code PengZuHeavenStem} instance.
     */
    public static PengZuHeavenStem fromName(String name) {
        return new PengZuHeavenStem(name);
    }

    /**
     * Creates a {@code PengZuHeavenStem} instance from its index.
     *
     * @param index The index of the taboo.
     * @return A new {@code PengZuHeavenStem} instance.
     */
    public static PengZuHeavenStem fromIndex(int index) {
        return new PengZuHeavenStem(index);
    }

    /**
     * Gets the next {@code PengZuHeavenStem} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code PengZuHeavenStem} instance.
     */
    public PengZuHeavenStem next(int n) {
        return new PengZuHeavenStem(nextIndex(n));
    }

}
