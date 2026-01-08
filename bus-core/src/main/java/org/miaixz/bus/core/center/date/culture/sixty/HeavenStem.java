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
package org.miaixz.bus.core.center.date.culture.sixty;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.Direction;
import org.miaixz.bus.core.center.date.culture.Element;
import org.miaixz.bus.core.center.date.culture.Opposite;
import org.miaixz.bus.core.center.date.culture.Terrain;
import org.miaixz.bus.core.center.date.culture.minor.PengZuHeavenStem;
import org.miaixz.bus.core.center.date.culture.star.ten.TenStar;

/**
 * Represents a Heavenly Stem (天干), one of the ten celestial stems in the Chinese sexagenary cycle. This class extends
 * {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HeavenStem extends Samsara {

    /**
     * Array of names for the Heavenly Stems.
     */
    public static final String[] NAMES = { "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸" };

    /**
     * Constructs a {@code HeavenStem} instance with the specified index.
     *
     * @param index The index of the Heavenly Stem in the {@link #NAMES} array.
     */
    public HeavenStem(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code HeavenStem} instance with the specified name.
     *
     * @param name The name of the Heavenly Stem.
     */
    public HeavenStem(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code HeavenStem} instance from its index.
     *
     * @param index The index of the Heavenly Stem.
     * @return A new {@code HeavenStem} instance.
     */
    public static HeavenStem fromIndex(int index) {
        return new HeavenStem(index);
    }

    /**
     * Creates a {@code HeavenStem} instance from its name.
     *
     * @param name The name of the Heavenly Stem.
     * @return A new {@code HeavenStem} instance.
     */
    public static HeavenStem fromName(String name) {
        return new HeavenStem(name);
    }

    /**
     * Gets the next {@code HeavenStem} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code HeavenStem} instance.
     */
    public HeavenStem next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Gets the corresponding {@link Element} (五行) for this Heavenly Stem.
     *
     * @return The {@link Element} associated with this Heavenly Stem.
     */
    public Element getElement() {
        return Element.fromIndex(index / 2);
    }

    /**
     * Gets the Yin or Yang (阴阳) attribute of this Heavenly Stem.
     *
     * @return {@link Opposite#YANG} if the index is even, {@link Opposite#YIN} if odd.
     */
    public Opposite getOpposite() {
        return index % 2 == 0 ? Opposite.YANG : Opposite.YIN;
    }

    /**
     * Gets the Ten God (十神) relationship between this Heavenly Stem and a target Heavenly Stem. The Ten Gods describe
     * the relationships between the Heavenly Stems in Bazi (Four Pillars of Destiny). (Reference:
     * 生我者，正印偏印。我生者，伤官食神。克我者，正官七杀。我克者，正财偏财。同我者，劫财比肩。)
     *
     * @param target The target Heavenly Stem.
     * @return The {@link TenStar} representing the Ten God relationship, or {@code null} if the target is null.
     */
    public TenStar getTenStar(HeavenStem target) {
        if (null == target) {
            return null;
        }
        int targetIndex = target.getIndex();
        int offset = targetIndex - index;
        if (index % 2 != 0 && targetIndex % 2 == 0) {
            offset += 2;
        }
        return TenStar.fromIndex(offset);
    }

    /**
     * Gets the corresponding {@link Direction} (方位) for this Heavenly Stem, derived from its {@link Element}.
     *
     * @return The {@link Direction} associated with this Heavenly Stem.
     */
    public Direction getDirection() {
        return getElement().getDirection();
    }

    /**
     * Gets the auspicious direction for joy (喜神方位) based on this Heavenly Stem. (Reference:
     * 《喜神方位歌》甲己在艮乙庚乾，丙辛坤位喜神安。丁壬只在离宫坐，戊癸原在在巽间。)
     *
     * @return The {@link Direction} for joy.
     */
    public Direction getJoyDirection() {
        return Direction.fromIndex(new int[] { 7, 5, 1, 8, 3 }[index % 5]);
    }

    /**
     * Gets the auspicious direction for the Yang Nobleman (阳贵神方位) based on this Heavenly Stem. (Reference:
     * 《阳贵神歌》甲戊坤艮位，乙己是坤坎，庚辛居离艮，丙丁兑与乾，震巽属何日，壬癸贵神安。)
     *
     * @return The {@link Direction} for the Yang Nobleman.
     */
    public Direction getYangDirection() {
        return Direction.fromIndex(new int[] { 1, 1, 6, 5, 7, 0, 8, 7, 2, 3 }[index]);
    }

    /**
     * Gets the auspicious direction for the Yin Nobleman (阴贵神方位) based on this Heavenly Stem. (Reference:
     * 《阴贵神歌》甲戊见牛羊，乙己鼠猴乡，丙丁猪鸡位，壬癸蛇兔藏，庚辛逢虎马，此是贵神方。)
     *
     * @return The {@link Direction} for the Yin Nobleman.
     */
    public Direction getYinDirection() {
        return Direction.fromIndex(new int[] { 7, 0, 5, 6, 1, 1, 7, 8, 3, 2 }[index]);
    }

    /**
     * Gets the auspicious direction for wealth (财神方位) based on this Heavenly Stem. (Reference:
     * 《财神方位歌》甲乙东北是财神，丙丁向在西南寻，戊己正北坐方位，庚辛正东去安身，壬癸原来正南坐，便是财神方位真。)
     *
     * @return The {@link Direction} for wealth.
     */
    public Direction getWealthDirection() {
        return Direction.fromIndex(new int[] { 7, 1, 0, 2, 8 }[index / 2]);
    }

    /**
     * Gets the auspicious direction for blessings (福神方位) based on this Heavenly Stem. (Reference:
     * 《福神方位歌》甲乙东南是福神，丙丁正东是堪宜，戊北己南庚辛坤，壬在乾方癸在西。)
     *
     * @return The {@link Direction} for blessings.
     */
    public Direction getMascotDirection() {
        return Direction.fromIndex(new int[] { 3, 3, 2, 2, 0, 8, 1, 1, 5, 6 }[index]);
    }

    /**
     * Gets the Peng Zu Bai Ji (彭祖百忌) associated with this Heavenly Stem.
     *
     * @return The {@link PengZuHeavenStem} instance.
     */
    public PengZuHeavenStem getPengZuHeavenStem() {
        return PengZuHeavenStem.fromIndex(index);
    }

    /**
     * Gets the Terrain (长生十二神) or Twelve Stages of Life based on this Heavenly Stem and a target Earthly Branch.
     *
     * @param earthBranch The target Earthly Branch.
     * @return The {@link Terrain} instance.
     */
    public Terrain getTerrain(EarthBranch earthBranch) {
        int earthBranchIndex = earthBranch.getIndex();
        return Terrain.fromIndex(
                new int[] { 1, 6, 10, 9, 10, 9, 7, 0, 4, 3 }[index]
                        + (Opposite.YANG == getOpposite() ? earthBranchIndex : -earthBranchIndex));
    }

    /**
     * Gets the Heavenly Stem that combines with this Heavenly Stem in a Five Combinations (五合). (Reference:
     * 甲己合，乙庚合，丙辛合，丁壬合，戊癸合)
     *
     * @return The combining {@link HeavenStem}.
     */
    public HeavenStem getCombine() {
        return next(5);
    }

    /**
     * Determines the element formed by the combination of this Heavenly Stem and a target Heavenly Stem. (Reference:
     * 甲己合化土，乙庚合化金，丙辛合化水，丁壬合化木，戊癸合化火)
     *
     * @param target The target Heavenly Stem to combine with.
     * @return The {@link Element} formed by the combination, or {@code null} if no combination occurs.
     */
    public Element combine(HeavenStem target) {
        return getCombine().equals(target) ? Element.fromIndex(index + 2) : null;
    }

}
