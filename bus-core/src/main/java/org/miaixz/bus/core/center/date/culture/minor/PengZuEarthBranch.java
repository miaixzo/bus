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
package org.miaixz.bus.core.center.date.culture.minor;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents the Peng Zu Bai Ji (彭祖百忌) associated with Earthly Branches (地支), a traditional Chinese calendar concept of
 * taboos. The names describe actions to avoid on days corresponding to specific Earthly Branches. (Reference:
 * 《地支六甲胎神歌》子午二日碓须忌，丑未厕道莫修移。寅申火炉休要动，卯酉大门修当避。辰戌鸡栖巳亥床，犯着六甲身堕胎。) This class extends {@link Samsara} to manage a cyclical
 * list of these entities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class PengZuEarthBranch extends Samsara {

    /**
     * Array of taboo descriptions for each Earthly Branch.
     */
    public static final String[] NAMES = { "子不问卜自惹祸殃", "丑不冠带主不还乡", "寅不祭祀神鬼不尝", "卯不穿井水泉不香", "辰不哭泣必主重丧", "巳不远行财物伏藏",
            "午不苫盖屋主更张", "未不服药毒气入肠", "申不安床鬼祟入房", "酉不会客醉坐颠狂", "戌不吃犬作怪上床", "亥不嫁娶不利新郎" };

    /**
     * Constructs a {@code PengZuEarthBranch} instance with the specified name.
     *
     * @param name The name of the taboo.
     */
    public PengZuEarthBranch(String name) {
        super(NAMES, name);
    }

    /**
     * Constructs a {@code PengZuEarthBranch} instance with the specified index.
     *
     * @param index The index of the taboo in the {@link #NAMES} array.
     */
    public PengZuEarthBranch(int index) {
        super(NAMES, index);
    }

    /**
     * Creates a {@code PengZuEarthBranch} instance from its name.
     *
     * @param name The name of the taboo.
     * @return A new {@code PengZuEarthBranch} instance.
     */
    public static PengZuEarthBranch fromName(String name) {
        return new PengZuEarthBranch(name);
    }

    /**
     * Creates a {@code PengZuEarthBranch} instance from its index.
     *
     * @param index The index of the taboo.
     * @return A new {@code PengZuEarthBranch} instance.
     */
    public static PengZuEarthBranch fromIndex(int index) {
        return new PengZuEarthBranch(index);
    }

    /**
     * Gets the next {@code PengZuEarthBranch} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code PengZuEarthBranch} instance.
     */
    public PengZuEarthBranch next(int n) {
        return new PengZuEarthBranch(nextIndex(n));
    }

}
