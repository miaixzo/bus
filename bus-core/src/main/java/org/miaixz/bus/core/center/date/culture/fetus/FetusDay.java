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

import org.miaixz.bus.core.center.date.culture.Tradition;
import org.miaixz.bus.core.center.date.culture.Direction;
import org.miaixz.bus.core.center.date.culture.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.sixty.SixtyCycleDay;
import org.miaixz.bus.core.center.date.culture.lunar.LunarDay;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Represents the daily Fetus God (逐日胎神), a concept in Chinese traditional culture related to pregnancy and auspicious
 * directions. This class extends {@link Tradition} to provide information about the Fetus God for a specific day.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class FetusDay extends Tradition {

    /**
     * Fetus God associated with Heavenly Stems (天干六甲胎神).
     */
    protected FetusHeavenStem fetusHeavenStem;

    /**
     * Fetus God associated with Earthly Branches (地支六甲胎神).
     */
    protected FetusEarthBranch fetusEarthBranch;

    /**
     * Indicates whether the Fetus God is inside (0) or outside (1).
     */
    protected int side;

    /**
     * The direction associated with the Fetus God.
     */
    protected Direction direction;

    /**
     * Constructs a {@code FetusDay} instance based on a {@link SixtyCycle}.
     *
     * @param sixtyCycle The SixtyCycle (GanZhi) of the day.
     */
    protected FetusDay(SixtyCycle sixtyCycle) {
        fetusHeavenStem = new FetusHeavenStem(sixtyCycle.getHeavenStem().getIndex() % 5);
        fetusEarthBranch = new FetusEarthBranch(sixtyCycle.getEarthBranch().getIndex() % 6);
        int index = new int[] { 3, 3, 8, 8, 8, 8, 8, 1, 1, 1, 1, 1, 1, 6, 6, 6, 6, 6, 5, 5, 5, 5, 5, 5, 0, 0, 0, 0, 0,
                -9, -9, -9, -9, -9, -5, -5, -1, -1, -1, -3, -7, -7, -7, -7, -5, 7, 7, 7, 7, 7, 7, 2, 2, 2, 2, 2, 3, 3,
                3, 3 }[sixtyCycle.getIndex()];
        side = index < 0 ? 0 : 1;
        direction = Direction.fromIndex(index);
    }

    /**
     * Constructs a {@code FetusDay} instance based on a {@link LunarDay}.
     *
     * @param lunarDay The LunarDay.
     */
    public FetusDay(LunarDay lunarDay) {
        this(lunarDay.getSixtyCycle());
    }

    /**
     * Constructs a {@code FetusDay} instance based on a {@link SixtyCycleDay}.
     *
     * @param sixtyCycleDay The SixtyCycleDay.
     */
    public FetusDay(SixtyCycleDay sixtyCycleDay) {
        this(sixtyCycleDay.getSixtyCycle());
    }

    /**
     * Creates a {@code FetusDay} instance from a {@link LunarDay}.
     *
     * @param lunarDay The LunarDay.
     * @return A new {@code FetusDay} instance.
     */
    public static FetusDay fromLunarDay(LunarDay lunarDay) {
        return new FetusDay(lunarDay);
    }

    /**
     * Creates a {@code FetusDay} instance from a {@link SixtyCycleDay}.
     *
     * @param sixtyCycleDay The SixtyCycleDay.
     * @return A new {@code FetusDay} instance.
     */
    public static FetusDay fromSixtyCycleDay(SixtyCycleDay sixtyCycleDay) {
        return new FetusDay(sixtyCycleDay);
    }

    /**
     * Gets the name of the Fetus God, including its location and direction.
     *
     * @return The name of the Fetus God.
     */
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

        s += Symbol.SPACE;

        if (0 == side) {
            s += "房";
        }
        s += "内";

        String directionName = direction.getName();
        if (1 == side && "北南西东".contains(directionName)) {
            s += "正";
        }
        s += directionName;
        return s;
    }

    /**
     * Gets the side (inside or outside) of the Fetus God.
     *
     * @return 0 for inside, 1 for outside.
     */
    public int getSide() {
        return side;
    }

    /**
     * Gets the direction associated with the Fetus God.
     *
     * @return The {@link Direction} instance.
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Gets the Fetus God associated with Heavenly Stems.
     *
     * @return The {@link FetusHeavenStem} instance.
     */
    public FetusHeavenStem getFetusHeavenStem() {
        return fetusHeavenStem;
    }

    /**
     * Gets the Fetus God associated with Earthly Branches.
     *
     * @return The {@link FetusEarthBranch} instance.
     */
    public FetusEarthBranch getFetusEarthBranch() {
        return fetusEarthBranch;
    }

}
