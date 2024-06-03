/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org 6tail and other contributors.              *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.center.date.culture.cn.sixty;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.cn.Direction;
import org.miaixz.bus.core.center.date.culture.cn.Element;
import org.miaixz.bus.core.center.date.culture.cn.Opposite;
import org.miaixz.bus.core.center.date.culture.cn.Terrain;
import org.miaixz.bus.core.center.date.culture.cn.minor.PengZuHeavenStem;
import org.miaixz.bus.core.center.date.culture.cn.star.ten.TenStar;

/**
 * 天干
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HeavenStem extends Samsara {

    public static final String[] NAMES = {
            "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"
    };

    public HeavenStem(int index) {
        super(NAMES, index);
    }

    public HeavenStem(String name) {
        super(NAMES, name);
    }

    public static HeavenStem fromIndex(int index) {
        return new HeavenStem(index);
    }

    public static HeavenStem fromName(String name) {
        return new HeavenStem(name);
    }

    public HeavenStem next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * 五行
     *
     * @return 五行
     */
    public Element getElement() {
        return Element.fromIndex(index / 2);
    }

    /**
     * 阴阳
     *
     * @return 阴阳
     */
    public Opposite getOpposite() {
        return index % 2 == 0 ? Opposite.YANG : Opposite.YIN;
    }

    /**
     * 十神（生我者，正印偏印。我生者，伤官食神。克我者，正官七杀。我克者，正财偏财。同我者，劫财比肩。）
     *
     * @param target 天干
     * @return 十神
     */
    public TenStar getTenStar(HeavenStem target) {
        if (null == target) {
            return null;
        }
        Element hostElement = getElement();
        Element guestElement = target.getElement();
        int index = 0;
        boolean sameYinYang = getOpposite().equals(target.getOpposite());
        if (hostElement.getReinforce().equals(guestElement)) {
            index = 1;
        } else if (hostElement.getRestrain().equals(guestElement)) {
            index = 2;
        } else if (guestElement.getRestrain().equals(hostElement)) {
            index = 3;
        } else if (guestElement.getReinforce().equals(hostElement)) {
            index = 4;
        }
        return TenStar.fromIndex(index * 2 + (sameYinYang ? 0 : 1));
    }

    /**
     * 方位
     *
     * @return 方位
     */
    public Direction getDirection() {
        return Direction.fromIndex(new int[]{2, 8, 4, 6, 0}[index / 2]);
    }

    /**
     * 喜神方位（《喜神方位歌》甲己在艮乙庚乾，丙辛坤位喜神安。丁壬只在离宫坐，戊癸原在在巽间。）
     *
     * @return 方位
     */
    public Direction getJoyDirection() {
        return Direction.fromIndex(new int[]{7, 5, 1, 8, 3}[index % 5]);
    }

    /**
     * 阳贵神方位（《阳贵神歌》甲戊坤艮位，乙己是坤坎，庚辛居离艮，丙丁兑与乾，震巽属何日，壬癸贵神安。）
     *
     * @return 方位
     */
    public Direction getYangDirection() {
        return Direction.fromIndex(new int[]{1, 1, 6, 5, 7, 0, 8, 7, 2, 3}[index]);
    }

    /**
     * 阴贵神方位（《阴贵神歌》甲戊见牛羊，乙己鼠猴乡，丙丁猪鸡位，壬癸蛇兔藏，庚辛逢虎马，此是贵神方。）
     *
     * @return 方位
     */
    public Direction getYinDirection() {
        return Direction.fromIndex(new int[]{7, 0, 5, 6, 1, 1, 7, 8, 3, 2}[index]);
    }

    /**
     * 财神方位（《财神方位歌》甲乙东北是财神，丙丁向在西南寻，戊己正北坐方位，庚辛正东去安身，壬癸原来正南坐，便是财神方位真。）
     *
     * @return 方位
     */
    public Direction getWealthDirection() {
        return Direction.fromIndex(new int[]{7, 1, 0, 2, 8}[index / 2]);
    }

    /**
     * 福神方位（《福神方位歌》甲乙东南是福神，丙丁正东是堪宜，戊北己南庚辛坤，壬在乾方癸在西。）
     *
     * @return 方位
     */
    public Direction getMascotDirection() {
        return Direction.fromIndex(new int[]{3, 3, 2, 2, 0, 8, 1, 1, 5, 6}[index]);
    }

    /**
     * 天干彭祖百忌
     *
     * @return 天干彭祖百忌
     */
    public PengZuHeavenStem getPengZuHeavenStem() {
        return PengZuHeavenStem.fromIndex(index);
    }

    /**
     * 地势(长生十二神)
     *
     * @param earthBranch 地支
     * @return 地势(长生十二神)
     */
    public Terrain getTerrain(EarthBranch earthBranch) {
        int earthBranchIndex = earthBranch.getIndex();
        return Terrain.fromIndex(new int[]{1, 6, 10, 9, 10, 9, 7, 0, 4, 3}[index] + (Opposite.YANG == getOpposite() ? earthBranchIndex : -earthBranchIndex));
    }

}
