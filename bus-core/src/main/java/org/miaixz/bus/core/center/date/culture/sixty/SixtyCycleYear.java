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

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.Direction;
import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.Twenty;
import org.miaixz.bus.core.center.date.culture.star.nine.NineStar;

/**
 * Represents a Sixty-Year Cycle year, a traditional Chinese calendar unit. This class extends {@link Loops} for
 * cyclical operations.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SixtyCycleYear extends Loops {

    /**
     * The Gregorian year.
     */
    protected int year;

    /**
     * Constructs a {@code SixtyCycleYear} instance with the specified Gregorian year.
     *
     * @param year The Gregorian year, ranging from -1 to 9999.
     * @throws IllegalArgumentException if the year is out of the supported range.
     */
    public SixtyCycleYear(int year) {
        if (year < -1 || year > 9999) {
            throw new IllegalArgumentException("illegal sixty cycle year: " + year);
        }
        this.year = year;
    }

    /**
     * Creates a {@code SixtyCycleYear} instance from a Gregorian year.
     *
     * @param year The Gregorian year.
     * @return A new {@code SixtyCycleYear} instance.
     */
    public static SixtyCycleYear fromYear(int year) {
        return new SixtyCycleYear(year);
    }

    /**
     * Gets the Gregorian year.
     *
     * @return The Gregorian year.
     */
    public int getYear() {
        return year;
    }

    /**
     * Gets the Sixty-Year Cycle value for this year.
     *
     * @return The {@link SixtyCycle} instance.
     */
    public SixtyCycle getSixtyCycle() {
        return SixtyCycle.fromIndex(year - 4);
    }

    /**
     * Gets the name of this Sixty-Year Cycle Year.
     *
     * @return The name of the year as a formatted string.
     */
    public String getName() {
        return String.format("%s年", getSixtyCycle());
    }

    /**
     * Gets the twenty-year cycle to which this year belongs.
     *
     * @return The {@link Twenty} instance.
     */
    public Twenty getTwenty() {
        return Twenty.fromIndex((int) Math.floor((year - 1864) / 20D));
    }

    /**
     * Gets the nine-star marker associated with this year.
     *
     * @return The {@link NineStar} instance.
     */
    public NineStar getNineStar() {
        return NineStar.fromIndex(63 + getTwenty().getSixty().getIndex() * 3 - getSixtyCycle().getIndex());
    }

    /**
     * Gets the Jupiter direction for this year.
     *
     * @return The {@link Direction} of Jupiter.
     */
    public Direction getJupiterDirection() {
        return Direction.fromIndex(
                new int[] { 0, 7, 7, 2, 3, 3, 8, 1, 1, 6, 0, 0 }[getSixtyCycle().getEarthBranch().getIndex()]);
    }

    /**
     * Gets the next {@code SixtyCycleYear} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code SixtyCycleYear} instance.
     */
    public SixtyCycleYear next(int n) {
        return fromYear(year + n);
    }

    /**
     * Gets the first Sixty-Year Cycle month of this year.
     * The calculation follows the traditional "Five Tigers" rule used to derive the opening month stem.
     *
     * @return The {@link SixtyCycleMonth} instance representing the first month.
     */
    public SixtyCycleMonth getFirstMonth() {
        return new SixtyCycleMonth(this, SixtyCycle.fromIndex(year * 12 - 46));
    }

    /**
     * Gets a list of all Sixty-Year Cycle Months within this year.
     *
     * @return A list of {@link SixtyCycleMonth} objects.
     */
    public List<SixtyCycleMonth> getMonths() {
        List<SixtyCycleMonth> l = new ArrayList<>();
        SixtyCycleMonth m = getFirstMonth();
        l.add(m);
        for (int i = 1; i < 12; i++) {
            l.add(m.next(i));
        }
        return l;
    }

}
