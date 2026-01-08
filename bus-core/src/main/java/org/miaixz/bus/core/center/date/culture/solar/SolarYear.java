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
package org.miaixz.bus.core.center.date.culture.solar;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.parts.YearParts;
import org.miaixz.bus.core.center.date.culture.rabjung.RabjungYear;

/**
 * Represents a year in the Gregorian calendar.
 * <p>
 * This class handles year calculations including leap year determination, which differs between the Julian calendar
 * (before 1600) and Gregorian calendar (after 1600).
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarYear extends YearParts {

    /**
     * Constructs a SolarYear instance.
     *
     * @param year the year (1-9999)
     * @throws IllegalArgumentException if the year is out of range
     */
    public SolarYear(int year) {
        validate(year);
        this.year = year;
    }

    /**
     * Creates a SolarYear from a year value.
     *
     * @param year the year (supports 1 to 9999)
     * @return a new SolarYear instance
     */
    public static SolarYear fromYear(int year) {
        return new SolarYear(year);
    }

    public static void validate(int year) {
        if (year < 1 || year > 9999) {
            throw new IllegalArgumentException(String.format("illegal solar year: %d", year));
        }
    }

    /**
     * Gets the number of days in this year. 1582 has 355 days (due to Gregorian calendar reform), common years have 365
     * days, and leap years have 366 days.
     *
     * @return the number of days in this year
     */
    public int getDayCount() {
        if (1582 == year) {
            return 355;
        }
        return isLeap() ? 366 : 365;
    }

    /**
     * Checks if this is a leap year. Before 1600 (Julian calendar): leap year if divisible by 4. After 1600 (Gregorian
     * calendar): leap year if divisible by 4 but not by 100, unless also divisible by 400.
     *
     * @return true if this is a leap year
     */
    public boolean isLeap() {
        if (year < 1600) {
            return year % 4 == 0;
        }
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }

    /**
     * Gets the Chinese name of this year.
     *
     * @return the year name in Chinese (e.g., "2024年")
     */
    public String getName() {
        return String.format("%d年", year);
    }

    /**
     * Gets the next or previous year.
     *
     * @param n the number of years to move (positive for forward, negative for backward)
     * @return the SolarYear n years from this one
     */
    public SolarYear next(int n) {
        return fromYear(year + n);
    }

    /**
     * Gets the list of months in this year. Each year has 12 months.
     *
     * @return a list of 12 SolarMonth objects
     */
    public List<SolarMonth> getMonths() {
        List<SolarMonth> l = new ArrayList<>(12);
        for (int i = 1; i < 13; i++) {
            l.add(SolarMonth.fromYm(year, i));
        }
        return l;
    }

    /**
     * Gets the list of quarters in this year. Each year has 4 quarters.
     *
     * @return a list of 4 SolarQuarter objects
     */
    public List<SolarQuarter> getQuarters() {
        List<SolarQuarter> l = new ArrayList<>(4);
        for (int i = 0; i < 4; i++) {
            l.add(SolarQuarter.fromIndex(year, i));
        }
        return l;
    }

    /**
     * Gets the list of half-years in this year. Each year has 2 half-years.
     *
     * @return a list of 2 SolarHalfYear objects
     */
    public List<SolarHalfYear> getHalfYears() {
        List<SolarHalfYear> l = new ArrayList<>(2);
        for (int i = 0; i < 2; i++) {
            l.add(SolarHalfYear.fromIndex(year, i));
        }
        return l;
    }

    /**
     * Gets the Tibetan calendar (Rabjung) year corresponding to this solar year.
     *
     * @return the corresponding RabjungYear
     */
    public RabjungYear getRabByungYear() {
        return RabjungYear.fromYear(year);
    }

}
