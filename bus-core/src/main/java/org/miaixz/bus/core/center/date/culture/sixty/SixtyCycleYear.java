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

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.Direction;
import org.miaixz.bus.core.center.date.culture.Twenty;
import org.miaixz.bus.core.center.date.culture.star.nine.NineStar;

/**
 * Represents a Sixty-Year Cycle Year (干支年), a traditional Chinese calendar unit. This class extends {@link Loops} for
 * cyclical operations.
 *
 * @author Kimi Liu
 * @since Java 17+
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
            throw new IllegalArgumentException(String.format("illegal sixty cycle year: %d", year));
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
     * Gets the Sixty-Year Cycle (干支) for this year.
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
     * Gets the Twenty-year Cycle (运) to which this year belongs.
     *
     * @return The {@link Twenty} instance.
     */
    public Twenty getTwenty() {
        return Twenty.fromIndex((int) Math.floor((year - 1864) / 20D));
    }

    /**
     * Gets the Nine Star (九星) associated with this year.
     *
     * @return The {@link NineStar} instance.
     */
    public NineStar getNineStar() {
        return NineStar.fromIndex(63 + getTwenty().getSixty().getIndex() * 3 - getSixtyCycle().getIndex());
    }

    /**
     * Gets the direction of Jupiter (太岁方位) for this year.
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
     * Gets the first Sixty-Year Cycle Month (首月) of this year. (Reference:
     * 五虎遁：甲己之年丙作首，乙庚之岁戊为头，丙辛必定寻庚起，丁壬壬位顺行流，若问戊癸何方发，甲寅之上好追求。)
     *
     * @return The {@link SixtyCycleMonth} instance representing the first month.
     */
    public SixtyCycleMonth getFirstMonth() {
        HeavenStem h = HeavenStem.fromIndex((getSixtyCycle().getHeavenStem().getIndex() + 1) * 2);
        return new SixtyCycleMonth(this, SixtyCycle.fromName(h.getName() + "寅"));
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
