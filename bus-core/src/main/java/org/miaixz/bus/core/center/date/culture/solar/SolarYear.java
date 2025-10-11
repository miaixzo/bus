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
package org.miaixz.bus.core.center.date.culture.solar;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.rabjung.RabjungYear;

/**
 * Represents a year in the Gregorian calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarYear extends Loops {

    /**
     * The year number.
     */
    protected int year;

    /**
     * Constructs a {@code SolarYear} with the given year.
     *
     * @param year The year, supporting 1 to 9999.
     * @throws IllegalArgumentException if the year is out of the supported range.
     */
    public SolarYear(int year) {
        if (year < 1 || year > 9999) {
            throw new IllegalArgumentException(String.format("illegal solar year: %d", year));
        }
        this.year = year;
    }

    /**
     * Creates a {@code SolarYear} instance from the given year.
     *
     * @param year The year, supporting 1 to 9999.
     * @return A new {@link SolarYear} instance.
     */
    public static SolarYear fromYear(int year) {
        return new SolarYear(year);
    }

    /**
     * Gets the year number.
     *
     * @return The year number.
     */
    public int getYear() {
        return year;
    }

    /**
     * Gets the total number of days in this solar year (355 days for 1582, 365 for common years, 366 for leap years).
     *
     * @return The total number of days.
     */
    public int getDayCount() {
        if (1582 == year) {
            return 355;
        }
        return isLeap() ? 366 : 365;
    }

    /**
     * Checks if this solar year is a leap year. (Before 1582, the Julian calendar was used, where a year divisible by 4
     * was a leap year. After that, the Gregorian calendar is used, where a leap year occurs every four years, except
     * for years divisible by 100 but not by 400).
     *
     * @return {@code true} if it's a leap year, {@code false} otherwise.
     */
    public boolean isLeap() {
        if (year < 1600) {
            return year % 4 == 0;
        }
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    /**
     * Gets the name of this solar year.
     *
     * @return The name of this solar year.
     */
    public String getName() {
        return String.format("%d年", year);
    }

    /**
     * Gets the solar year after a specified number of years.
     *
     * @param n The number of years to add.
     * @return The {@link SolarYear} after {@code n} years.
     */
    public SolarYear next(int n) {
        return fromYear(year + n);
    }

    /**
     * Gets a list of all months in this solar year. A year has 12 months.
     *
     * @return A list of {@link SolarMonth} objects for this year.
     */
    public List<SolarMonth> getMonths() {
        List<SolarMonth> l = new ArrayList<>(12);
        for (int i = 1; i < 13; i++) {
            l.add(SolarMonth.fromYm(year, i));
        }
        return l;
    }

    /**
     * Gets a list of all quarters in this solar year. A year has 4 quarters.
     *
     * @return A list of {@link SolarQuarter} objects for this year.
     */
    public List<SolarQuarter> getQuarter() {
        List<SolarQuarter> l = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            l.add(SolarQuarter.fromIndex(year, i));
        }
        return l;
    }

    /**
     * Gets a list of all half-years in this solar year. A year has 2 half-years.
     *
     * @return A list of {@link SolarHalfYear} objects for this year.
     */
    public List<SolarHalfYear> getHalfYears() {
        List<SolarHalfYear> l = new ArrayList<>(2);
        for (int i = 0; i < 2; i++) {
            l.add(SolarHalfYear.fromIndex(year, i));
        }
        return l;
    }

    /**
     * Gets the Tibetan year corresponding to this solar year.
     *
     * @return The {@link RabjungYear} for this solar year.
     */
    public RabjungYear getRabByungYear() {
        return RabjungYear.fromYear(year);
    }

}
