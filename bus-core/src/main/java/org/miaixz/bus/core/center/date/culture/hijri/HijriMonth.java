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

import org.miaixz.bus.core.center.date.culture.parts.MonthParts;

/**
 * Represents a month in the tabular Hijri calendar.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class HijriMonth extends MonthParts {

    /**
     * Localized Hijri month names.
     */
    public static final String[] NAMES = { "穆哈兰姆月", "色法尔月", "赖比尔·敖外鲁月", "赖比尔·阿色尼月", "主马达·敖外鲁月", "主马达·阿色尼月", "赖哲卜月",
            "舍尔邦月", "赖买丹月", "闪瓦鲁月", "都尔喀尔德月", "都尔黑哲月" };

    /**
     * Validates a Hijri year and month.
     *
     * @param year  Hijri year
     * @param month Hijri month, 1-12
     * @throws IllegalArgumentException if the month or year is invalid
     */
    public static void validate(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("illegal hijri month: " + month);
        }
        HijriYear.validate(year);
    }

    /**
     * Constructs a Hijri month.
     *
     * @param year  Hijri year
     * @param month Hijri month, 1-12
     */
    public HijriMonth(int year, int month) {
        validate(year, month);
        this.year = year;
        this.month = month;
    }

    /**
     * Creates a Hijri month.
     *
     * @param year  Hijri year
     * @param month Hijri month, 1-12
     * @return a new {@link HijriMonth}
     */
    public static HijriMonth fromYm(int year, int month) {
        return new HijriMonth(year, month);
    }

    /**
     * Gets the Hijri year containing this month.
     *
     * @return Hijri year
     */
    public HijriYear getHijriYear() {
        return HijriYear.fromYear(year);
    }

    /**
     * Gets the number of days in this month. Odd months have 30 days, even months have 29 days, and month 12 has 30
     * days in leap years.
     *
     * @return day count
     */
    public int getDayCount() {
        int d = month % 2 == 0 ? 29 : 30;
        if (12 == month && getHijriYear().isLeap()) {
            d++;
        }
        return d;
    }

    /**
     * Gets this month's index within the year.
     *
     * @return index from 0 to 11
     */
    public int getIndexInYear() {
        return month - 1;
    }

    /**
     * Gets the localized month name.
     *
     * @return month name
     */
    public String getName() {
        return NAMES[getIndexInYear()];
    }

    /**
     * Returns the display text of this month.
     *
     * @return display text
     */
    @Override
    public String toString() {
        return getHijriYear() + getName();
    }

    /**
     * Gets the month that is {@code n} months away.
     *
     * @param n number of months to move
     * @return target Hijri month
     */
    public HijriMonth next(int n) {
        int i = month - 1 + n;
        return fromYm((year * 12 + i) / 12, indexOf(i, 12) + 1);
    }

    /**
     * Gets all days in this month.
     *
     * @return days in this month
     */
    public List<HijriDay> getDays() {
        int size = getDayCount();
        List<HijriDay> l = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) {
            l.add(HijriDay.fromYmd(year, month, i));
        }
        return l;
    }

    /**
     * Gets the first day of this month.
     *
     * @return first Hijri day
     */
    public HijriDay getFirstDay() {
        return HijriDay.fromYmd(year, month, 1);
    }

}
