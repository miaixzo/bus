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
package org.miaixz.bus.core.center.date.culture;

import org.miaixz.bus.core.center.date.Galaxy;
import org.miaixz.bus.core.center.date.culture.lunar.LunarDay;
import org.miaixz.bus.core.center.date.culture.lunar.LunarMonth;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarTime;

/**
 * Represents the phases of the moon (月相) in Chinese traditional culture. This class extends {@link Samsara} to manage a
 * cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Phase extends Samsara {

    /**
     * Array of names for the moon phases.
     */
    public static final String[] NAMES = { "新月", "蛾眉月", "上弦月", "盈凸月", "满月", "亏凸月", "下弦月", "残月" };

    /**
     * The lunar year.
     */
    protected int lunarYear;

    /**
     * The lunar month.
     */
    protected int lunarMonth;

    /**
     * Constructs a {@code Phase} instance from a lunar year, month, and phase index.
     *
     * @param lunarYear  The lunar year.
     * @param lunarMonth The lunar month (negative for leap month).
     * @param index      The index of the moon phase (0-7).
     */
    public Phase(int lunarYear, int lunarMonth, int index) {
        super(NAMES, index);
        LunarMonth m = LunarMonth.fromYm(lunarYear, lunarMonth).next(index / getSize());
        this.lunarYear = m.getYear();
        this.lunarMonth = m.getMonthWithLeap();
    }

    /**
     * Constructs a {@code Phase} instance from a lunar year, month, and phase name.
     *
     * @param lunarYear  The lunar year.
     * @param lunarMonth The lunar month (negative for leap month).
     * @param name       The name of the moon phase (e.g., "新月").
     */
    public Phase(int lunarYear, int lunarMonth, String name) {
        super(NAMES, name);
        this.lunarYear = lunarYear;
        this.lunarMonth = lunarMonth;
    }

    /**
     * Creates a {@code Phase} instance from a lunar year, month, and phase index.
     *
     * @param lunarYear  The lunar year.
     * @param lunarMonth The lunar month (negative for leap month).
     * @param index      The index of the moon phase (0-7).
     * @return A new {@link Phase} instance.
     */
    public static Phase fromIndex(int lunarYear, int lunarMonth, int index) {
        return new Phase(lunarYear, lunarMonth, index);
    }

    /**
     * Creates a {@code Phase} instance from a lunar year, month, and phase name.
     *
     * @param lunarYear  The lunar year.
     * @param lunarMonth The lunar month (negative for leap month).
     * @param name       The name of the moon phase.
     * @return A new {@link Phase} instance.
     */
    public static Phase fromName(int lunarYear, int lunarMonth, String name) {
        return new Phase(lunarYear, lunarMonth, name);
    }

    /**
     * Gets the moon phase {@code n} positions away from this one.
     *
     * @param n The number of positions to move (positive for next, negative for previous).
     * @return The new {@link Phase} instance.
     */
    public Phase next(int n) {
        int size = getSize();
        int i = index + n;
        if (i < 0) {
            i -= size;
        }
        i /= size;
        LunarMonth m = LunarMonth.fromYm(lunarYear, lunarMonth);
        if (i != 0) {
            m = m.next(i);
        }
        return fromIndex(m.getYear(), m.getMonthWithLeap(), nextIndex(n));
    }

    /**
     * Calculates the approximate start time of the first phase (New Moon) in the given lunar month.
     *
     * @return The {@link SolarTime} representing the start time.
     */
    protected SolarTime getStartSolarTime() {
        int n = (int) Math.floor((lunarYear - 2000) * 365.2422 / 29.53058886);
        int i = 0;
        double jd = JulianDay.J2000 + Galaxy.ONE_THIRD;
        SolarDay d = LunarDay.fromYmd(lunarYear, lunarMonth, 1).getSolarDay();
        while (true) {
            double t = Galaxy.msaLonT((n + i) * Galaxy.PI_2) * 36525;
            if (!JulianDay.fromJulianDay(jd + t - Galaxy.dtT(t)).getSolarDay().isBefore(d)) {
                break;
            }
            i++;
        }
        int[] r = { 0, 90, 180, 270 };
        double t = Galaxy.msaLonT((n + i + r[index / 2] / 360D) * Galaxy.PI_2) * 36525;
        return JulianDay.fromJulianDay(jd + t - Galaxy.dtT(t)).getSolarTime();
    }

    /**
     * Gets the Gregorian (Solar) time of this phase.
     *
     * @return The {@link SolarTime}.
     */
    public SolarTime getSolarTime() {
        SolarTime t = getStartSolarTime();
        return index % 2 == 1 ? t.next(1) : t;
    }

    /**
     * Gets the Gregorian (Solar) day of this phase.
     *
     * @return The {@link SolarDay}.
     */
    public SolarDay getSolarDay() {
        SolarDay d = getStartSolarTime().getSolarDay();
        return index % 2 == 1 ? d.next(1) : d;
    }

    /**
     * Represents a specific day within a moon phase.
     */
    public static class PhaseDay extends Replenish {

        /**
         * Constructs a {@code PhaseDay} instance.
         *
         * @param phase    The moon phase.
         * @param dayIndex The index of the day within this phase.
         */
        public PhaseDay(Phase phase, int dayIndex) {
            super(phase, dayIndex);
        }

        /**
         * Gets the moon phase.
         *
         * @return The {@link Phase}.
         */
        public Phase getPhase() {
            return (Phase) tradition;
        }

    }

}
