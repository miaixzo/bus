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
package org.miaixz.bus.core.center.date.culture.sixty;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.Sound;
import org.miaixz.bus.core.center.date.culture.Ten;
import org.miaixz.bus.core.center.date.culture.minor.PengZu;

/**
 * Represents a Sixty-Year Cycle (六十甲子), also known as GanZhi (干支) or Sexagenary Cycle. This class extends
 * {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SixtyCycle extends Samsara {

    /**
     * Array of names for the Sixty-Year Cycles.
     */
    public static final String[] NAMES = { "甲子", "乙丑", "丙寅", "丁卯", "戊辰", "己巳", "庚午", "辛未", "壬申", "癸酉", "甲戌", "乙亥", "丙子",
            "丁丑", "戊寅", "己卯", "庚辰", "辛巳", "壬午", "癸未", "甲申", "乙酉", "丙戌", "丁亥", "戊子", "己丑", "庚寅", "辛卯", "壬辰", "癸巳", "甲午",
            "乙未", "丙申", "丁酉", "戊戌", "己亥", "庚子", "辛丑", "壬寅", "癸卯", "甲辰", "乙巳", "丙午", "丁未", "戊申", "己酉", "庚戌", "辛亥", "壬子",
            "癸丑", "甲寅", "乙卯", "丙辰", "丁巳", "戊午", "己未", "庚申", "辛酉", "壬戌", "癸亥" };

    /**
     * Constructs a {@code SixtyCycle} instance with the specified index.
     *
     * @param index The index of the Sixty-Year Cycle in the {@link #NAMES} array.
     */
    public SixtyCycle(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code SixtyCycle} instance with the specified name.
     *
     * @param name The name of the Sixty-Year Cycle.
     */
    public SixtyCycle(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code SixtyCycle} instance from its index.
     *
     * @param index The index of the Sixty-Year Cycle.
     * @return A new {@code SixtyCycle} instance.
     */
    public static SixtyCycle fromIndex(int index) {
        return new SixtyCycle(index);
    }

    /**
     * Creates a {@code SixtyCycle} instance from its name.
     *
     * @param name The name of the Sixty-Year Cycle.
     * @return A new {@code SixtyCycle} instance.
     */
    public static SixtyCycle fromName(String name) {
        return new SixtyCycle(name);
    }

    /**
     * Gets the Heavenly Stem (天干) component of this Sixty-Year Cycle.
     *
     * @return The {@link HeavenStem} instance.
     */
    public HeavenStem getHeavenStem() {
        return HeavenStem.fromIndex(index % HeavenStem.NAMES.length);
    }

    /**
     * Gets the Earthly Branch (地支) component of this Sixty-Year Cycle.
     *
     * @return The {@link EarthBranch} instance.
     */
    public EarthBranch getEarthBranch() {
        return EarthBranch.fromIndex(index % EarthBranch.NAMES.length);
    }

    /**
     * Gets the Na Yin (纳音) element associated with this Sixty-Year Cycle.
     *
     * @return The {@link Sound} instance.
     */
    public Sound getSound() {
        return Sound.fromIndex(index / 2);
    }

    /**
     * Gets the Peng Zu Bai Ji (彭祖百忌) associated with this Sixty-Year Cycle.
     *
     * @return The {@link PengZu} instance.
     */
    public PengZu getPengZu() {
        return PengZu.fromSixtyCycle(this);
    }

    /**
     * Gets the Ten-day Cycle (旬) to which this Sixty-Year Cycle belongs.
     *
     * @return The {@link Ten} instance.
     */
    public Ten getTen() {
        return Ten.fromIndex((getHeavenStem().getIndex() - getEarthBranch().getIndex()) / 2);
    }

    /**
     * Gets the Empty Branches (旬空) for this Sixty-Year Cycle. Empty Branches are the two Earthly Branches that do not
     * combine with a Heavenly Stem in a given Ten-day Cycle.
     *
     * @return An array of two {@link EarthBranch} instances representing the Empty Branches.
     */
    public EarthBranch[] getExtraEarthBranches() {
        EarthBranch[] l = new EarthBranch[2];
        l[0] = EarthBranch.fromIndex(10 + getEarthBranch().getIndex() - getHeavenStem().getIndex());
        l[1] = l[0].next(1);
        return l;
    }

    /**
     * Gets the next {@code SixtyCycle} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code SixtyCycle} instance.
     */
    public SixtyCycle next(int n) {
        return fromIndex(nextIndex(n));
    }

}
