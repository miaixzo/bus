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
package org.miaixz.bus.core.center.date.culture.cn.eightchar;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.Tradition;
import org.miaixz.bus.core.center.date.culture.cn.sixty.EarthBranch;
import org.miaixz.bus.core.center.date.culture.cn.sixty.HeavenStem;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.center.date.culture.solar.SolarTime;

/**
 * Represents the Eight Characters (八字), also known as Bazi or Four Pillars of Destiny, in Chinese astrology. It
 * consists of four pairs of Heavenly Stems and Earthly Branches, representing the year, month, day, and hour of birth.
 * This class extends {@link Tradition} to encapsulate these four pillars.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class EightChar extends Tradition {

    /**
     * The Sixty-Year Cycle for the year (年柱).
     */
    protected SixtyCycle year;

    /**
     * The Sixty-Year Cycle for the month (月柱).
     */
    protected SixtyCycle month;

    /**
     * The Sixty-Year Cycle for the day (日柱).
     */
    protected SixtyCycle day;

    /**
     * The Sixty-Year Cycle for the hour (时柱).
     */
    protected SixtyCycle hour;

    /**
     * Constructs an {@code EightChar} instance with the specified year, month, day, and hour pillars.
     *
     * @param year  The {@link SixtyCycle} for the year.
     * @param month The {@link SixtyCycle} for the month.
     * @param day   The {@link SixtyCycle} for the day.
     * @param hour  The {@link SixtyCycle} for the hour.
     */
    public EightChar(SixtyCycle year, SixtyCycle month, SixtyCycle day, SixtyCycle hour) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
    }

    /**
     * Constructs an {@code EightChar} instance with the specified year, month, day, and hour pillar names.
     *
     * @param year  The name of the Sixty-Year Cycle for the year.
     * @param month The name of the Sixty-Year Cycle for the month.
     * @param day   The name of the Sixty-Year Cycle for the day.
     * @param hour  The name of the Sixty-Year Cycle for the hour.
     */
    public EightChar(String year, String month, String day, String hour) {
        this(SixtyCycle.fromName(year), SixtyCycle.fromName(month), SixtyCycle.fromName(day),
                SixtyCycle.fromName(hour));
    }

    /**
     * Gets the Sixty-Year Cycle for the year (年柱).
     *
     * @return The {@link SixtyCycle} instance representing the year pillar.
     */
    public SixtyCycle getYear() {
        return year;
    }

    /**
     * Gets the Sixty-Year Cycle for the month (月柱).
     *
     * @return The {@link SixtyCycle} instance representing the month pillar.
     */
    public SixtyCycle getMonth() {
        return month;
    }

    /**
     * Gets the Sixty-Year Cycle for the day (日柱).
     *
     * @return The {@link SixtyCycle} instance representing the day pillar.
     */
    public SixtyCycle getDay() {
        return day;
    }

    /**
     * Gets the Sixty-Year Cycle for the hour (时柱).
     *
     * @return The {@link SixtyCycle} instance representing the hour pillar.
     */
    public SixtyCycle getHour() {
        return hour;
    }

    /**
     * Gets the Fetal Origin (胎元) for this Eight Char.
     *
     * @return The {@link SixtyCycle} instance representing the Fetal Origin.
     */
    public SixtyCycle getFetalOrigin() {
        return SixtyCycle.fromName(month.getHeavenStem().next(1).getName() + month.getEarthBranch().next(3).getName());
    }

    /**
     * Gets the Fetal Breath (胎息) for this Eight Char.
     *
     * @return The {@link SixtyCycle} instance representing the Fetal Breath.
     */
    public SixtyCycle getFetalBreath() {
        return SixtyCycle.fromName(
                day.getHeavenStem().next(5).getName()
                        + EarthBranch.fromIndex(13 - day.getEarthBranch().getIndex()).getName());
    }

    /**
     * Gets the Life Palace (命宫) for this Eight Char.
     *
     * @return The {@link SixtyCycle} instance representing the Life Palace.
     */
    public SixtyCycle getOwnSign() {
        int m = month.getEarthBranch().getIndex() - 1;
        if (m < 1) {
            m += 12;
        }
        int h = hour.getEarthBranch().getIndex() - 1;
        if (h < 1) {
            h += 12;
        }
        int offset = m + h;
        offset = (offset >= 14 ? 26 : 14) - offset;
        return SixtyCycle.fromName(
                HeavenStem.fromIndex((year.getHeavenStem().getIndex() + 1) * 2 + offset - 1).getName()
                        + EarthBranch.fromIndex(offset + 1).getName());
    }

    /**
     * Gets the Body Palace (身宫) for this Eight Char.
     *
     * @return The {@link SixtyCycle} instance representing the Body Palace.
     */
    public SixtyCycle getBodySign() {
        int offset = month.getEarthBranch().getIndex() - 1;
        if (offset < 1) {
            offset += 12;
        }
        offset += hour.getEarthBranch().getIndex() + 1;
        if (offset > 12) {
            offset -= 12;
        }
        return SixtyCycle.fromName(
                HeavenStem.fromIndex((year.getHeavenStem().getIndex() + 1) * 2 + offset - 1).getName()
                        + EarthBranch.fromIndex(offset + 1).getName());
    }

    /**
     * Gets a list of Gregorian times that correspond to this Eight Char.
     *
     * @param startYear The start year (inclusive), supporting years from 1 to 9999.
     * @param endYear   The end year (inclusive), supporting years from 1 to 9999.
     * @return A list of {@link SolarTime} objects matching this Eight Char.
     */
    public List<SolarTime> getSolarTimes(int startYear, int endYear) {
        List<SolarTime> l = new ArrayList<>();
        // Offset of the month's Earthly Branch from Yin month (寅月)
        int m = month.getEarthBranch().next(-2).getIndex();
        // The Heavenly Stem of the month must match
        if (!HeavenStem.fromIndex((year.getHeavenStem().getIndex() + 1) * 2 + m).equals(month.getHeavenStem())) {
            return l;
        }
        // The first year's Start of Spring (立春) is Xinyou (辛酉), index 57
        int y = year.next(-57).getIndex() + 1;
        // Solar term offset
        m *= 2;
        // Convert hour's Earthly Branch to hour
        int h = hour.getEarthBranch().getIndex() * 2;
        // Handle different schools of thought for Zi hour (子时)
        int[] hours = h == 0 ? new int[] { 0, 23 } : new int[] { h };
        int baseYear = startYear - 1;
        if (baseYear > y) {
            y += 60 * (int) Math.ceil((baseYear - y) / 60D);
        }
        while (y <= endYear) {
            // Start of Spring (立春) is the beginning of Yin month (寅月)
            SolarTerms term = SolarTerms.fromIndex(y, 3);
            // Adjust solar term, then year and month pillars will match
            if (m > 0) {
                term = term.next(m);
            }
            SolarTime solarTime = term.getJulianDay().getSolarTime();
            if (solarTime.getYear() >= startYear) {
                // Offset between day pillar and solar term pillar
                SolarDay solarDay = solarTime.getSolarDay();
                int d = day.next(-solarDay.getLunarDay().getSixtyCycle().getIndex()).getIndex();
                if (d > 0) {
                    // Advance days from solar term
                    solarDay = solarDay.next(d);
                }
                for (int hour : hours) {
                    int mi = 0;
                    int s = 0;
                    // In extreme cases where it's the solar term day and the hour matches, include minutes and seconds
                    if (d == 0 && hour == solarTime.getHour()) {
                        mi = solarTime.getMinute();
                        s = solarTime.getSecond();
                    }
                    SolarTime time = SolarTime
                            .fromYmdHms(solarDay.getYear(), solarDay.getMonth(), solarDay.getDay(), hour, mi, s);
                    if (d == 30) {
                        time = time.next(-3600);
                    }
                    // Verify the result
                    if (time.getLunarHour().getEightChar().equals(this)) {
                        l.add(time);
                    }
                }
            }
            y += 60;
        }
        return l;
    }

    /**
     * Gets the string representation of the Eight Characters.
     *
     * @return The Eight Characters as a formatted string.
     */
    public String getName() {
        return String.format("%s %s %s %s", year, month, day, hour);
    }

}
