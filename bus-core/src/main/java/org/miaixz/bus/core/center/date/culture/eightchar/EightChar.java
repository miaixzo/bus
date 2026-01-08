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
package org.miaixz.bus.core.center.date.culture.eightchar;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.Tradition;
import org.miaixz.bus.core.center.date.culture.Duty;
import org.miaixz.bus.core.center.date.culture.sixty.*;
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
     * The Three Pillars (Year, Month, Day).
     */
    protected ThreePillars threePillars;

    /**
     * The Hour Pillar.
     */
    protected SixtyCycle hour;

    /**
     * Initializes the EightChar.
     *
     * @param year  The Year Pillar.
     * @param month The Month Pillar.
     * @param day   The Day Pillar.
     * @param hour  The Hour Pillar.
     */
    public EightChar(SixtyCycle year, SixtyCycle month, SixtyCycle day, SixtyCycle hour) {
        this.threePillars = new ThreePillars(year, month, day);
        this.hour = hour;
    }

    /**
     * Initializes the EightChar.
     *
     * @param year  The Year Pillar (as a string).
     * @param month The Month Pillar (as a string).
     * @param day   The Day Pillar (as a string).
     * @param hour  The Hour Pillar (as a string).
     */
    public EightChar(String year, String month, String day, String hour) {
        this(SixtyCycle.fromName(year), SixtyCycle.fromName(month), SixtyCycle.fromName(day),
                SixtyCycle.fromName(hour));
    }

    /**
     * Gets the Year Pillar.
     *
     * @return The Year Pillar.
     */
    public SixtyCycle getYear() {
        return threePillars.getYear();
    }

    /**
     * Gets the Month Pillar.
     *
     * @return The Month Pillar.
     */
    public SixtyCycle getMonth() {
        return threePillars.getMonth();
    }

    /**
     * Gets the Day Pillar.
     *
     * @return The Day Pillar.
     */
    public SixtyCycle getDay() {
        return threePillars.getDay();
    }

    /**
     * Gets the Hour Pillar.
     *
     * @return The Hour Pillar.
     */
    public SixtyCycle getHour() {
        return hour;
    }

    /**
     * Gets the Fetal Origin (Tai Yuan).
     *
     * @return The Fetal Origin.
     */
    public SixtyCycle getFetalOrigin() {
        SixtyCycle m = getMonth();
        return SixtyCycle.fromName(m.getHeavenStem().next(1).getName() + m.getEarthBranch().next(3).getName());
    }

    /**
     * Gets the Fetal Breath (Tai Xi).
     *
     * @return The Fetal Breath.
     */
    public SixtyCycle getFetalBreath() {
        SixtyCycle d = getDay();
        return SixtyCycle.fromName(
                d.getHeavenStem().next(5).getName()
                        + EarthBranch.fromIndex(13 - d.getEarthBranch().getIndex()).getName());
    }

    /**
     * Gets the Own Sign (Ming Gong), or Life Palace.
     *
     * @return The Own Sign.
     */
    public SixtyCycle getOwnSign() {
        int m = getMonth().getEarthBranch().getIndex() - 1;
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
                HeavenStem.fromIndex((getYear().getHeavenStem().getIndex() + 1) * 2 + offset - 1).getName()
                        + EarthBranch.fromIndex(offset + 1).getName());
    }

    /**
     * Gets the Body Sign (Shen Gong), or Body Palace.
     *
     * @return The Body Sign.
     */
    public SixtyCycle getBodySign() {
        int offset = getMonth().getEarthBranch().getIndex() - 1;
        if (offset < 1) {
            offset += 12;
        }
        offset += hour.getEarthBranch().getIndex() + 1;
        if (offset > 12) {
            offset -= 12;
        }
        return SixtyCycle.fromName(
                HeavenStem.fromIndex((getYear().getHeavenStem().getIndex() + 1) * 2 + offset - 1).getName()
                        + EarthBranch.fromIndex(offset + 1).getName());
    }

    /**
     * Gets the 12 Duty Gods (Jian Chu).
     *
     * @return The Duty.
     * @see SixtyCycleDay#getDuty()
     */
    @Deprecated
    public Duty getDuty() {
        return Duty.fromIndex(getDay().getEarthBranch().getIndex() - getYear().getEarthBranch().getIndex());
    }

    /**
     * Gets a list of Gregorian (Solar) times that match this EightChar.
     *
     * @param startYear The inclusive start year (supports 1-9999).
     * @param endYear   The inclusive end year (supports 1-9999).
     * @return A list of {@link SolarTime} objects.
     */
    public List<SolarTime> getSolarTimes(int startYear, int endYear) {
        List<SolarTime> l = new ArrayList<>();
        SixtyCycle year = getYear();
        SixtyCycle month = getMonth();
        SixtyCycle day = getDay();
        // Offset of the month's Earthly Branch from Yin month
        int m = month.getEarthBranch().next(-2).getIndex();
        // The month's Heavenly Stem must match
        if (!HeavenStem.fromIndex((year.getHeavenStem().getIndex() + 1) * 2 + m).equals(month.getHeavenStem())) {
            return l;
        }
        // The Start of Spring in year 1 is Xin-You, index 57
        int y = year.next(-57).getIndex() + 1;
        // Solar term offset
        m *= 2;
        // Convert hour Earthly Branch to hour of day
        int h = hour.getEarthBranch().getIndex() * 2;
        // Compatible with multiple schools of thought for Zi hour (midnight)
        int[] hours = h == 0 ? new int[] { 0, 23 } : new int[] { h };
        int baseYear = startYear - 1;
        if (baseYear > y) {
            y += 60 * (int) Math.ceil((baseYear - y) / 60D);
        }
        while (y <= endYear) {
            // Start of Spring is the beginning of Yin month
            SolarTerms term = SolarTerms.fromIndex(y, 3);
            // Shift by solar terms, the year and month pillars will match
            if (m > 0) {
                term = term.next(m);
            }
            SolarTime solarTime = term.getJulianDay().getSolarTime();
            if (solarTime.getYear() >= startYear) {
                // Offset between the day's SixtyCycle and the solar term's SixtyCycle
                SolarDay solarDay = solarTime.getSolarDay();
                int d = day.next(-solarDay.getLunarDay().getSixtyCycle().getIndex()).getIndex();
                if (d > 0) {
                    // Shift days from the solar term
                    solarDay = solarDay.next(d);
                }
                for (int hour : hours) {
                    int mi = 0;
                    int s = 0;
                    // In the extreme case where it is exactly the solar term day and the hour
                    // matches the solar term's hour, include minutes and seconds
                    if (d == 0 && hour == solarTime.getHour()) {
                        mi = solarTime.getMinute();
                        s = solarTime.getSecond();
                    }
                    SolarTime time = SolarTime
                            .fromYmdHms(solarDay.getYear(), solarDay.getMonth(), solarDay.getDay(), hour, mi, s);
                    if (d == 30) {
                        time = time.next(-3600);
                    }
                    // Verify
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
        return String.format("%s %s", threePillars, hour);
    }

}
