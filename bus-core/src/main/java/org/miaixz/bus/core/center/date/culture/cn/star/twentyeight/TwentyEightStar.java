/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.date.culture.cn.star.twentyeight;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.cn.Animal;
import org.miaixz.bus.core.center.date.culture.cn.Land;
import org.miaixz.bus.core.center.date.culture.cn.Luck;
import org.miaixz.bus.core.center.date.culture.cn.Zone;
import org.miaixz.bus.core.center.date.culture.cn.star.seven.SevenStar;

/**
 * 二十八宿
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TwentyEightStar extends Samsara {

    public static final String[] NAMES = { "角", "亢", "氐", "房", "心", "尾", "箕", "斗", "牛", "女", "虚", "危", "室", "壁", "奎",
            "娄", "胃", "昴", "毕", "觜", "参", "井", "鬼", "柳", "星", "张", "翼", "轸" };

    public TwentyEightStar(int index) {
        super(NAMES, index);
    }

    public TwentyEightStar(String name) {
        super(NAMES, name);
    }

    public static TwentyEightStar fromIndex(int index) {
        return new TwentyEightStar(index);
    }

    public static TwentyEightStar fromName(String name) {
        return new TwentyEightStar(name);
    }

    public TwentyEightStar next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * 七曜
     *
     * @return 七曜
     */
    public SevenStar getSevenStar() {
        return SevenStar.fromIndex(index % 7 + 4);
    }

    /**
     * 九野
     *
     * @return 九野
     */
    public Land getLand() {
        return Land.fromIndex(new int[] { 4, 4, 4, 2, 2, 2, 7, 7, 7, 0, 0, 0, 0, 5, 5, 5, 6, 6, 6, 1, 1, 1, 8, 8, 8, 3,
                3, 3 }[index]);
    }

    /**
     * 宫
     *
     * @return 宫
     */
    public Zone getZone() {
        return Zone.fromIndex(index / 7);
    }

    /**
     * 动物
     *
     * @return 动物
     */
    public Animal getAnimal() {
        return Animal.fromIndex(index);
    }

    /**
     * 吉凶
     *
     * @return 吉凶
     */
    public Luck getLuck() {
        return Luck.fromIndex(new int[] { 0, 1, 1, 0, 1, 0, 0, 0, 1, 1, 1, 1, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 1, 1, 0,
                1, 0 }[index]);
    }

}
