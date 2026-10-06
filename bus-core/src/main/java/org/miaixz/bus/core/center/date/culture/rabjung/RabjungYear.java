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
package org.miaixz.bus.core.center.date.culture.rabjung;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.Zodiac;
import org.miaixz.bus.core.center.date.culture.parts.AbstractTraditionalYear;
import org.miaixz.bus.core.center.date.culture.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.solar.SolarYear;

/**
 * Represents a year in the Tibetan calendar (Gregorian year 1027 is the first year of the Tibetan calendar, the first
 * Rabjung Fire Rabbit year).
 *
 * @author Kimi Liu
 */
public class RabjungYear extends AbstractTraditionalYear {

    /**
     * Constructs a Rabjung year with the given indices.
     *
     * @param rabByungIndex Rabjung (Victorious Cycle) sequence number, starting from 0
     * @param elementIndex  element (Five Phases) index, starting from 0
     * @param zodiacIndex   zodiac animal index, starting from 0
     * @throws IllegalArgumentException if any index is out of valid range
     */
    public RabjungYear(int rabByungIndex, int elementIndex, int zodiacIndex) {
        this(resolveYear(rabByungIndex, elementIndex, zodiacIndex));
    }

    /**
     * Constructs a Rabjung year from a Gregorian year.
     *
     * @param year Gregorian year
     */
    public RabjungYear(int year) {
        super(year);
        validate(year);
    }

    private static int resolveYear(int rabByungIndex, int elementIndex, int zodiacIndex) {
        if (rabByungIndex < 0 || rabByungIndex > 150) {
            throw new IllegalArgumentException("illegal rab-byung index: " + rabByungIndex);
        }
        if (elementIndex < 0 || elementIndex >= RabjungElement.NAMES.length) {
            throw new IllegalArgumentException("illegal element index: " + elementIndex);
        }
        if (zodiacIndex < 0 || zodiacIndex >= Zodiac.NAMES.length) {
            throw new IllegalArgumentException("illegal zodiac index: " + zodiacIndex);
        }
        return 1024 + rabByungIndex * 60
                + SixtyCycle.fromIndex(6 * (elementIndex * 2 + zodiacIndex % 2) - 5 * zodiacIndex).getIndex();
    }

    /**
     * Validates that the given Gregorian year falls within the supported range (1027–9999).
     *
     * @param year Gregorian year to validate
     * @throws IllegalArgumentException if the year is out of range
     */
    public static void validate(int year) {
        validateRange(year, 1027, 9999, "rab-byung year");
    }

    /**
     * Creates a {@code RabjungYear} instance from the given Rabjung index and Sixty Cycle.
     *
     * @param rabByungIndex The Rabjung sequence number, starting from 0.
     * @param sixtyCycle    The {@link SixtyCycle} of the year.
     * @return A new {@link RabjungYear} instance.
     */
    public static RabjungYear fromSixtyCycle(int rabByungIndex, SixtyCycle sixtyCycle) {
        return fromYear(1024 + rabByungIndex * 60 + sixtyCycle.getIndex());
    }

    /**
     * Creates a {@code RabjungYear} instance from the given Rabjung index, element, and zodiac.
     *
     * @param rabByungIndex The Rabjung sequence number, starting from 0.
     * @param element       The {@link RabjungElement} of the year.
     * @param zodiac        The {@link Zodiac} of the year.
     * @return A new {@link RabjungYear} instance.
     * @throws IllegalArgumentException if the element and zodiac combination does not correspond to a valid Sixty
     *                                  Cycle.
     */
    public static RabjungYear fromElementZodiac(int rabByungIndex, RabjungElement element, Zodiac zodiac) {
        return fromSixtyCycle(
                rabByungIndex,
                SixtyCycle.fromIndex(6 * (element.getIndex() * 2 + zodiac.getIndex() % 2) - 5 * zodiac.getIndex()));
    }

    /**
     * Creates a {@code RabjungYear} instance from the given Gregorian year.
     *
     * @param year The Gregorian year.
     * @return A new {@link RabjungYear} instance.
     */
    public static RabjungYear fromYear(int year) {
        return new RabjungYear(year);
    }

    /**
     * Gets the Rabjung sequence number.
     *
     * @return The Rabjung sequence number, starting from 0.
     */
    public int getRabByungIndex() {
        return (year - 1024) / 60;
    }

    /**
     * Gets the Zodiac (ShengXiao) of this Tibetan year.
     *
     * @return The {@link Zodiac} of this year.
     */
    public Zodiac getZodiac() {
        return getSixtyCycle().getEarthBranch().getZodiac();
    }

    /**
     * Gets the Rabjung element of this Tibetan year.
     *
     * @return The {@link RabjungElement} of this year.
     */
    public RabjungElement getElement() {
        return RabjungElement.fromIndex(getSixtyCycle().getHeavenStem().getElement().getIndex());
    }

    /**
     * Gets the name of this Tibetan year.
     *
     * @return The localized display name of this Tibetan year.
     */
    public String getName() {
        int n = getRabByungIndex() + 1;
        String d = "零一二三四五六七八九";
        String s = n > 99 ? d.charAt(n / 100) + "百" : "";
        n %= 100;
        return String.format(
                "第%s饶迥%s%s年",
                n == 0 ? s
                        : n < 10 ? s + (s.isEmpty() ? "" : "零") + d.charAt(n)
                                : s + (n < 20 ? (s.isEmpty() ? "" : "一") : d.charAt(n / 10)) + "十"
                                        + (n % 10 > 0 ? d.charAt(n % 10) + "" : ""),
                getElement(),
                getZodiac());
    }

    /**
     * Gets the Tibetan year after a specified number of years.
     *
     * @param n The number of years to add.
     * @return The {@link RabjungYear} after {@code n} years.
     */
    public RabjungYear next(int n) {
        return fromYear(year + n);
    }

    /**
     * Gets the leap month of this Tibetan year.
     *
     * @return The leap month number (1 for leap January, 0 if no leap month).
     */
    public int getLeapMonth() {
        int y = 1;
        int m = 4;
        int t = 1;
        while (y < year) {
            int i = m + 31 + t;
            y += 2;
            m = i - 23;
            if (i > 35) {
                y += 1;
                m -= 12;
            }
            t = 1 - t;
        }
        return y == year ? m : 0;
    }

    /**
     * Gets the corresponding solar year.
     *
     * @return The {@link SolarYear} corresponding to this Tibetan year.
     */
    public SolarYear getSolarYear() {
        return SolarYear.fromYear(year);
    }

    /**
     * Gets the first month of this Tibetan year.
     *
     * @return The first {@link RabjungMonth} of this year.
     */
    public RabjungMonth getFirstMonth() {
        return RabjungMonth.fromYm(year, 1);
    }

    /**
     * Gets a list of all months in this Tibetan year.
     *
     * @return A list of {@link RabjungMonth} objects for this year.
     */
    public List<RabjungMonth> getMonths() {
        List<RabjungMonth> l = new ArrayList<>(13);
        int leapMonth = getLeapMonth();
        for (int i = 1; i < 13; i++) {
            l.add(RabjungMonth.fromYm(year, i));
            if (i == leapMonth) {
                l.add(RabjungMonth.fromYm(year, -i));
            }
        }
        return l;
    }

}
