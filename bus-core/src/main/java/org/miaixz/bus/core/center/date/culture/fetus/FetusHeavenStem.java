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
package org.miaixz.bus.core.center.date.culture.fetus;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents the Fetus God associated with Heavenly Stems (天干六甲胎神), a concept in Chinese traditional culture related to
 * pregnancy. The names indicate the location where the Fetus God resides based on the Heavenly Stem of the day.
 * (Reference: 《天干六甲胎神歌》甲己之日占在门，乙庚碓磨休移动。丙辛厨灶莫相干，丁壬仓库忌修弄。戊癸房床若移整，犯之孕妇堕孩童。) This class extends {@link Samsara} to manage a
 * cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class FetusHeavenStem extends Samsara {

    /**
     * Array of names for the Fetus God locations based on Heavenly Stems.
     */
    public static final String[] NAMES = { "门", "碓磨", "厨灶", "仓库", "房床" };

    /**
     * Constructs a {@code FetusHeavenStem} instance with the specified index.
     *
     * @param index The index of the Fetus God location in the {@link #NAMES} array.
     */
    public FetusHeavenStem(int index) {
        super(NAMES, index);
    }

    /**
     * Gets the next {@code FetusHeavenStem} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code FetusHeavenStem} instance.
     */
    public FetusHeavenStem next(int n) {
        return new FetusHeavenStem(nextIndex(n));
    }

}
