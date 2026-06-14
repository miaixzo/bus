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
package org.miaixz.bus.core.center.date.culture.hijri;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.parts.YearParts;

/**
 * Represents a year in the tabular Hijri calendar.
 * <p>
 * The leap-year rule follows a 30-year cycle where years 2, 5, 7, 10, 13, 16, 18, 21, 24, 26 and 29 are leap years.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HijriYear extends YearParts {

    /**
     * Constructs a Hijri year.
     *
     * @param year Hijri year
     */
    public HijriYear(int year) {
        validate(year);
        this.year = year;
    }

    /**
     * Validates the supported Hijri year range.
     *
     * @param year Hijri year
     * @throws IllegalArgumentException if the year is out of range
     */
    public static void validate(int year) {
        validateRange(year, -640, 9666, "hijri year");
    }

    /**
     * Creates a Hijri year.
     *
     * @param year Hijri year
     * @return a new {@link HijriYear}
     */
    public static HijriYear fromYear(int year) {
        return new HijriYear(year);
    }

    /**
     * Gets the number of days in this year.
     *
     * @return 355 for leap years, otherwise 354
     */
    public int getDayCount() {
        return isLeap() ? 355 : 354;
    }

    /**
     * Checks whether this Hijri year is a leap year.
     *
     * @return {@code true} if this year is leap
     */
    public boolean isLeap() {
        int i = Math.floorMod(year - 1, 30);
        return i == 1 || i == 4 || i == 6 || i == 9 || i == 12 || i == 15 || i == 17 || i == 20 || i == 23 || i == 25
                || i == 28;
    }

    /**
     * Gets the display name.
     *
     * @return display name
     */
    public String getName() {
        return year + "年";
    }

    /**
     * Gets the year that is {@code n} years away.
     *
     * @param n number of years to move
     * @return target Hijri year
     */
    public HijriYear next(int n) {
        return fromYear(year + n);
    }

    /**
     * Gets all months in this year.
     *
     * @return 12 Hijri months
     */
    public List<HijriMonth> getMonths() {
        List<HijriMonth> l = new ArrayList<>(12);
        for (int i = 1; i < 13; i++) {
            l.add(HijriMonth.fromYm(year, i));
        }
        return l;
    }

    /**
     * Gets the first month of this year.
     *
     * @return first Hijri month
     */
    public HijriMonth getFirstMonth() {
        return HijriMonth.fromYm(year, 1);
    }

}
