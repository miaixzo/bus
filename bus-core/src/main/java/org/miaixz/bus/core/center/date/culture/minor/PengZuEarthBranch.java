/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
 * @since Java 17+
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
