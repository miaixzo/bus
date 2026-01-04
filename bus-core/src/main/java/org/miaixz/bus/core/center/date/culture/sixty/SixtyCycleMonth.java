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
package org.miaixz.bus.core.center.date.culture.sixty;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.Direction;
import org.miaixz.bus.core.center.date.culture.star.nine.NineStar;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;

/**
 * Represents a Sixty-Year Cycle Month (干支月), a traditional Chinese calendar unit. This class extends {@link Loops} for
 * cyclical operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SixtyCycleMonth extends Loops {

    /**
     * The Sixty-Year Cycle Year associated with this month.
     */
    protected SixtyCycleYear year;

    /**
     * The Sixty-Year Cycle for the month (月柱).
     */
    protected SixtyCycle month;

    /**
     * Constructs a {@code SixtyCycleMonth} instance with the specified Sixty-Year Cycle Year and month.
     *
     * @param year  The {@link SixtyCycleYear} instance.
     * @param month The {@link SixtyCycle} instance representing the month.
     */
    public SixtyCycleMonth(SixtyCycleYear year, SixtyCycle month) {
        this.year = year;
        this.month = month;
    }

    /**
     * Creates a {@code SixtyCycleMonth} instance from a year and month index.
     *
     * @param year  The year.
     * @param index The index of the month (0-11, where 0 is Yin month).
     * @return A new {@code SixtyCycleMonth} instance.
     */
    public static SixtyCycleMonth fromIndex(int year, int index) {
        return SixtyCycleYear.fromYear(year).getFirstMonth().next(index);
    }

    /**
     * Gets the Sixty-Year Cycle Year associated with this month.
     *
     * @return The {@link SixtyCycleYear} instance.
     */
    public SixtyCycleYear getSixtyCycleYear() {
        return year;
    }

    /**
     * Gets the Sixty-Year Cycle for the year (年柱).
     *
     * @return The {@link SixtyCycle} instance representing the year.
     */
    public SixtyCycle getYear() {
        return year.getSixtyCycle();
    }

    /**
     * Gets the Sixty-Year Cycle for the month (月柱).
     *
     * @return The {@link SixtyCycle} instance representing the month.
     */
    public SixtyCycle getSixtyCycle() {
        return month;
    }

    /**
     * Gets the name of this Sixty-Year Cycle Month.
     *
     * @return The name of the month as a formatted string.
     */
    public String getName() {
        return String.format("%s月", month);
    }

    /**
     * Returns a string representation of this Sixty-Year Cycle Month, including the year and month.
     *
     * @return A string representation of the Sixty-Year Cycle Month.
     */
    @Override
    public String toString() {
        return String.format("%s%s", year, getName());
    }

    /**
     * Gets the next {@code SixtyCycleMonth} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code SixtyCycleMonth} instance.
     */
    public SixtyCycleMonth next(int n) {
        return new SixtyCycleMonth(SixtyCycleYear.fromYear((year.getYear() * 12 + getIndexInYear() + n) / 12),
                month.next(n));
    }

    /**
     * Gets the index of this month within the year (0-11), where Yin month (寅月) is 0.
     *
     * @return The index of the month in the year.
     */
    public int getIndexInYear() {
        return month.getEarthBranch().next(-2).getIndex();
    }

    /**
     * Gets the Nine Star (九星) associated with this month.
     *
     * @return The {@link NineStar} instance.
     */
    public NineStar getNineStar() {
        int index = month.getEarthBranch().getIndex();
        if (index < 2) {
            index += 3;
        }
        return NineStar.fromIndex(27 - getYear().getEarthBranch().getIndex() % 3 * 3 - index);
    }

    /**
     * Gets the direction of Jupiter (太岁方位) for this month.
     *
     * @return The {@link Direction} of Jupiter.
     */
    public Direction getJupiterDirection() {
        int n = new int[] { 7, -1, 1, 3 }[month.getEarthBranch().next(-2).getIndex() % 4];
        return n == -1 ? month.getHeavenStem().getDirection() : Direction.fromIndex(n);
    }

    /**
     * Gets the first day (节令当天) of this Sixty-Year Cycle Month.
     *
     * @return The {@link SixtyCycleDay} instance representing the first day.
     */
    public SixtyCycleDay getFirstDay() {
        return SixtyCycleDay.fromSolarDay(SolarTerms.fromIndex(year.getYear(), 3 + getIndexInYear() * 2).getSolarDay());
    }

    /**
     * Gets a list of all Sixty-Year Cycle Days within this month.
     *
     * @return A list of {@link SixtyCycleDay} objects.
     */
    public List<SixtyCycleDay> getDays() {
        List<SixtyCycleDay> l = new ArrayList<>();
        SixtyCycleDay d = getFirstDay();
        while (d.getSixtyCycleMonth().equals(this)) {
            l.add(d);
            d = d.next(1);
        }
        return l;
    }

}
