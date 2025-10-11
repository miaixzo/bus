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
package org.miaixz.bus.core.center.date.culture.cn.sixty;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.cn.*;
import org.miaixz.bus.core.center.date.culture.cn.minor.PengZuEarthBranch;

/**
 * Represents an Earthly Branch (地支), one of the twelve terrestrial branches in the Chinese sexagenary cycle. This class
 * extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EarthBranch extends Samsara {

    /**
     * Array of names for the Earthly Branches.
     */
    public static final String[] NAMES = { "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥" };

    /**
     * Constructs an {@code EarthBranch} instance with the specified index.
     *
     * @param index The index of the Earthly Branch in the {@link #NAMES} array.
     */
    public EarthBranch(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs an {@code EarthBranch} instance with the specified name.
     *
     * @param name The name of the Earthly Branch.
     */
    public EarthBranch(String name) {
        super(NAMES, name);
    }

    /**
     * Creates an {@code EarthBranch} instance from its index.
     *
     * @param index The index of the Earthly Branch.
     * @return A new {@code EarthBranch} instance.
     */
    public static EarthBranch fromIndex(int index) {
        return new EarthBranch(index);
    }

    /**
     * Creates an {@code EarthBranch} instance from its name.
     *
     * @param name The name of the Earthly Branch.
     * @return A new {@code EarthBranch} instance.
     */
    public static EarthBranch fromName(String name) {
        return new EarthBranch(name);
    }

    /**
     * Gets the next {@code EarthBranch} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code EarthBranch} instance.
     */
    public EarthBranch next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Gets the corresponding {@link Element} (五行) for this Earthly Branch.
     *
     * @return The {@link Element} associated with this Earthly Branch.
     */
    public Element getElement() {
        return Element.fromIndex(new int[] { 4, 2, 0, 0, 2, 1, 1, 2, 3, 3, 2, 4 }[index]);
    }

    /**
     * Gets the Yin or Yang (阴阳) attribute of this Earthly Branch.
     *
     * @return {@link Opposite#YANG} if the index is even, {@link Opposite#YIN} if odd.
     */
    public Opposite getOpposite() {
        return index % 2 == 0 ? Opposite.YANG : Opposite.YIN;
    }

    /**
     * Gets the main Hidden Heavenly Stem (藏干之本气) for this Earthly Branch.
     *
     * @return The main {@link HeavenStem} hidden within this Earthly Branch.
     */
    public HeavenStem getHideHeavenStemMain() {
        return HeavenStem.fromIndex(new int[] { 9, 5, 0, 1, 4, 2, 3, 5, 6, 7, 4, 8 }[index]);
    }

    /**
     * Gets the middle Hidden Heavenly Stem (藏干之中气) for this Earthly Branch.
     *
     * @return The middle {@link HeavenStem} hidden within this Earthly Branch, or {@code null} if none.
     */
    public HeavenStem getHideHeavenStemMiddle() {
        int n = new int[] { -1, 9, 2, -1, 1, 6, 5, 3, 8, -1, 7, 0 }[index];
        return n == -1 ? null : HeavenStem.fromIndex(n);
    }

    /**
     * Gets the residual Hidden Heavenly Stem (藏干之余气) for this Earthly Branch.
     *
     * @return The residual {@link HeavenStem} hidden within this Earthly Branch, or {@code null} if none.
     */
    public HeavenStem getHideHeavenStemResidual() {
        int n = new int[] { -1, 7, 4, -1, 9, 4, -1, 1, 4, -1, 3, -1 }[index];
        return n == -1 ? null : HeavenStem.fromIndex(n);
    }

    /**
     * Gets a list of all Hidden Heavenly Stems (藏干列表) for this Earthly Branch.
     *
     * @return A list of {@link HiddenStem} objects.
     */
    public List<HiddenStem> getHideHeavenStems() {
        List<HiddenStem> l = new ArrayList<>();
        l.add(new HiddenStem(getHideHeavenStemMain(), HiddenStems.PRINCIPAL));
        HeavenStem o = getHideHeavenStemMiddle();
        if (null != o) {
            l.add(new HiddenStem(o, HiddenStems.MIDDLE));
        }
        o = getHideHeavenStemResidual();
        if (null != o) {
            l.add(new HiddenStem(o, HiddenStems.RESIDUAL));
        }
        return l;
    }

    /**
     * Gets the corresponding Chinese Zodiac (生肖) animal for this Earthly Branch.
     *
     * @return The {@link Zodiac} instance.
     */
    public Zodiac getZodiac() {
        return Zodiac.fromIndex(index);
    }

    /**
     * Gets the corresponding {@link Direction} (方位) for this Earthly Branch.
     *
     * @return The {@link Direction} associated with this Earthly Branch.
     */
    public Direction getDirection() {
        return Direction.fromIndex(new int[] { 0, 4, 2, 2, 4, 8, 8, 4, 6, 6, 4, 0 }[index]);
    }

    /**
     * Gets the ominous direction (煞) for this Earthly Branch. (Reference:
     * 逢巳日、酉日、丑日必煞东；亥日、卯日、未日必煞西；申日、子日、辰日必煞南；寅日、午日、戌日必煞北。)
     *
     * @return The {@link Direction} that is considered ominous.
     */
    public Direction getOminous() {
        return Direction.fromIndex(new int[] { 8, 2, 0, 6 }[index % 4]);
    }

    /**
     * Gets the Peng Zu Bai Ji (彭祖百忌) associated with this Earthly Branch.
     *
     * @return The {@link PengZuEarthBranch} instance.
     */
    public PengZuEarthBranch getPengZuEarthBranch() {
        return PengZuEarthBranch.fromIndex(index);
    }

    /**
     * Gets the Earthly Branch that forms a Six Clash (六冲) with this Earthly Branch. (Reference:
     * 子午冲，丑未冲，寅申冲，辰戌冲，卯酉冲，巳亥冲)
     *
     * @return The clashing {@link EarthBranch}.
     */
    public EarthBranch getSixclash() {
        return next(6);
    }

    /**
     * Gets the Earthly Branch that forms a Six Combination (六合) with this Earthly Branch. (Reference:
     * 子丑合，寅亥合，卯戌合，辰酉合，巳申合，午未合)
     *
     * @return The combining {@link EarthBranch}.
     */
    public EarthBranch getCombine() {
        return fromIndex(1 - index);
    }

    /**
     * Gets the Earthly Branch that forms a Six Harm (六害) with this Earthly Branch. (Reference: 子未害、丑午害、寅巳害、卯辰害、申亥害、酉戌害)
     *
     * @return The harming {@link EarthBranch}.
     */
    public EarthBranch getHarm() {
        return fromIndex(19 - index);
    }

    /**
     * Determines the element formed by the combination of this Earthly Branch and a target Earthly Branch. (Reference:
     * 子丑合化土，寅亥合化木，卯戌合化火，辰酉合化金，巳申合化水，午未合化土)
     *
     * @param target The target Earthly Branch to combine with.
     * @return The {@link Element} formed by the combination, or {@code null} if no combination occurs.
     */
    public Element combine(EarthBranch target) {
        return getCombine().equals(target) ? Element.fromIndex(new int[] { 2, 2, 0, 1, 3, 4, 2, 2, 4, 3, 1, 0 }[index])
                : null;
    }

}
