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
package org.miaixz.bus.core.center.date.culture.rabjung;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.Zodiac;
import org.miaixz.bus.core.center.date.culture.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.solar.SolarYear;

/**
 * Represents a year in the Tibetan calendar (Gregorian year 1027 is the first year of the Tibetan calendar, the first
 * Rabjung Fire Rabbit year).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RabjungYear extends Loops {

    /**
     * The Rabjung (Victorious Cycle) sequence number, starting from 0.
     */
    protected int rabByungIndex;

    /**
     * The Sixty Cycle (GanZhi) of the year.
     */
    protected SixtyCycle sixtyCycle;

    /**
     * Constructs a {@code RabjungYear} with the given Rabjung index and Sixty Cycle.
     *
     * @param rabByungIndex The Rabjung sequence number, starting from 0.
     * @param sixtyCycle    The {@link SixtyCycle} of the year.
     * @throws IllegalArgumentException if the Rabjung index is out of valid range.
     */
    public RabjungYear(int rabByungIndex, SixtyCycle sixtyCycle) {
        if (rabByungIndex < 0 || rabByungIndex > 150) {
            throw new IllegalArgumentException(String.format("illegal rab-byung index: %d", rabByungIndex));
        }
        this.rabByungIndex = rabByungIndex;
        this.sixtyCycle = sixtyCycle;
    }

    /**
     * Creates a {@code RabjungYear} instance from the given Rabjung index and Sixty Cycle.
     *
     * @param rabByungIndex The Rabjung sequence number, starting from 0.
     * @param sixtyCycle    The {@link SixtyCycle} of the year.
     * @return A new {@link RabjungYear} instance.
     */
    public static RabjungYear fromSixtyCycle(int rabByungIndex, SixtyCycle sixtyCycle) {
        return new RabjungYear(rabByungIndex, sixtyCycle);
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
        for (int i = 0; i < 60; i++) {
            SixtyCycle sixtyCycle = SixtyCycle.fromIndex(i);
            if (sixtyCycle.getEarthBranch().getZodiac().equals(zodiac)
                    && sixtyCycle.getHeavenStem().getElement().getIndex() == element.getIndex()) {
                return new RabjungYear(rabByungIndex, sixtyCycle);
            }
        }
        throw new IllegalArgumentException(String.format("illegal rab-byung element %s, zodiac %s", element, zodiac));
    }

    /**
     * Creates a {@code RabjungYear} instance from the given Gregorian year.
     *
     * @param year The Gregorian year.
     * @return A new {@link RabjungYear} instance.
     */
    public static RabjungYear fromYear(int year) {
        return new RabjungYear((year - 1024) / 60, SixtyCycle.fromIndex(year - 4));
    }

    /**
     * Gets the Rabjung sequence number.
     *
     * @return The Rabjung sequence number, starting from 0.
     */
    public int getRabByungIndex() {
        return rabByungIndex;
    }

    /**
     * Gets the Sixty Cycle (GanZhi) of this Tibetan year.
     *
     * @return The {@link SixtyCycle} of this year.
     */
    public SixtyCycle getSixtyCycle() {
        return sixtyCycle;
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
     * @return The name of this Tibetan year.
     */
    public String getName() {
        String[] digits = { "零", "一", "二", "三", "四", "五", "六", "七", "八", "九" };
        String[] units = { "", "十", "百" };
        int n = rabByungIndex + 1;
        StringBuilder s = new StringBuilder();
        int pos = 0;
        while (n > 0) {
            int digit = n % 10;
            if (digit > 0) {
                s.insert(0, digits[digit] + units[pos]);
            } else if (s.length() > 0) {
                s.insert(0, digits[digit]);
            }
            n /= 10;
            pos++;
        }
        String letter = s.toString();
        if (letter.startsWith("一十")) {
            letter = letter.substring(1);
        }
        return String.format("第%s饶迥%s%s年", letter, getElement(), getZodiac());
    }

    /**
     * Gets the Tibetan year after a specified number of years.
     *
     * @param n The number of years to add.
     * @return The {@link RabjungYear} after {@code n} years.
     */
    public RabjungYear next(int n) {
        return fromYear(getYear() + n);
    }

    /**
     * Gets the Gregorian year corresponding to this Tibetan year.
     *
     * @return The Gregorian year.
     */
    public int getYear() {
        return 1024 + rabByungIndex * 60 + getSixtyCycle().getIndex();
    }

    /**
     * Gets the leap month of this Tibetan year.
     *
     * @return The leap month number (1 for leap January, 0 if no leap month).
     */
    public int getLeapMonth() {
        int y = 1;
        int m = 4;
        int t = 0;
        int currentYear = getYear();
        while (y < currentYear) {
            int i = m - 1 + (t % 2 == 0 ? 33 : 32);
            y = (y * 12 + i) / 12;
            m = i % 12 + 1;
            t++;
        }
        return y == currentYear ? m : 0;
    }

    /**
     * Gets the corresponding solar year.
     *
     * @return The {@link SolarYear} corresponding to this Tibetan year.
     */
    public SolarYear getSolarYear() {
        return SolarYear.fromYear(getYear());
    }

    /**
     * Gets the first month of this Tibetan year.
     *
     * @return The first {@link RabjungMonth} of this year.
     */
    public RabjungMonth getFirstMonth() {
        return new RabjungMonth(this, 1);
    }

    /**
     * Gets the number of months in this Tibetan year.
     *
     * @return The number of months. Typically 12, or 13 if there is a leap month.
     */
    public int getMonthCount() {
        return getLeapMonth() < 1 ? 12 : 13;
    }

    /**
     * Gets a list of all months in this Tibetan year.
     *
     * @return A list of {@link RabjungMonth} objects for this year.
     */
    public List<RabjungMonth> getMonths() {
        List<RabjungMonth> l = new ArrayList<>();
        int leapMonth = getLeapMonth();
        for (int i = 1; i < 13; i++) {
            l.add(new RabjungMonth(this, i));
            if (i == leapMonth) {
                l.add(new RabjungMonth(this, -i));
            }
        }
        return l;
    }

}
