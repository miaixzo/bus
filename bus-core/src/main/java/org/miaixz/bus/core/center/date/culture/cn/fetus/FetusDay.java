/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.date.culture.cn.fetus;

import org.miaixz.bus.core.center.date.culture.Tradition;
import org.miaixz.bus.core.center.date.culture.cn.Direction;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.lunar.LunarDay;

/**
 * 逐日胎神
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class FetusDay extends Tradition {

    /**
     * 天干六甲胎神
     */
    protected FetusHeavenStem fetusHeavenStem;

    /**
     * 地支六甲胎神
     */
    protected FetusEarthBranch fetusEarthBranch;

    /**
     * 内外
     */
    protected int side;

    /**
     * 方位
     */
    protected Direction direction;

    public FetusDay(LunarDay lunarDay) {
        SixtyCycle sixtyCycle = lunarDay.getSixtyCycle();
        fetusHeavenStem = new FetusHeavenStem(sixtyCycle.getHeavenStem().getIndex() % 5);
        fetusEarthBranch = new FetusEarthBranch(sixtyCycle.getEarthBranch().getIndex() % 6);
        int index = new int[] { 3, 3, 8, 8, 8, 8, 8, 1, 1, 1, 1, 1, 1, 6, 6, 6, 6, 6, 5, 5, 5, 5, 5, 5, 0, 0, 0, 0, 0,
                -9, -9, -9, -9, -9, -5, -5, -1, -1, -1, -3, -7, -7, -7, -7, -5, 7, 7, 7, 7, 7, 7, 2, 2, 2, 2, 2, 3, 3,
                3, 3 }[sixtyCycle.getIndex()];
        side = index < 0 ? 0 : 1;
        direction = Direction.fromIndex(index);
    }

    /**
     * 从农历日初始化
     *
     * @param lunarDay 农历日
     * @return 逐日胎神
     */
    public static FetusDay fromLunarDay(LunarDay lunarDay) {
        return new FetusDay(lunarDay);
    }

    public String getName() {
        String s = fetusHeavenStem.getName() + fetusEarthBranch.getName();
        if ("门门".equals(s)) {
            s = "占大门";
        } else if ("碓磨碓".equals(s)) {
            s = "占碓磨";
        } else if ("房床床".equals(s)) {
            s = "占房床";
        } else if (s.startsWith("门")) {
            s = "占" + s;
        }

        s += " ";

        String directionName = direction.getName();
        if (0 == side) {
            s += "房";
        }
        s += "内";

        if (1 == side && "北南西东".contains(directionName)) {
            s += "正";
        }
        s += directionName;
        return s;
    }

    /**
     * 内外
     *
     * @return 内外
     */
    public int getSide() {
        return side;
    }

    /**
     * 方位
     *
     * @return 方位
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * 天干六甲胎神
     *
     * @return 天干六甲胎神
     */
    public FetusHeavenStem getFetusHeavenStem() {
        return fetusHeavenStem;
    }

    /**
     * 地支六甲胎神
     *
     * @return 地支六甲胎神
     */
    public FetusEarthBranch getFetusEarthBranch() {
        return fetusEarthBranch;
    }

}
