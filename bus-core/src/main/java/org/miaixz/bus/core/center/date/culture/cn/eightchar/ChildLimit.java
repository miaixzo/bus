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

import org.miaixz.bus.core.center.date.culture.cn.Opposite;
import org.miaixz.bus.core.center.date.culture.cn.eightchar.provider.ChildLimitProvider;
import org.miaixz.bus.core.center.date.culture.cn.eightchar.provider.impl.DefaultChildLimitProvider;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycleYear;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.center.date.culture.solar.SolarTime;
import org.miaixz.bus.core.lang.Gender;

/**
 * Represents the "Child Limit" (童限), the period from birth until the start of the Grand Fortune (大运) in Chinese
 * astrology.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ChildLimit {

    /**
     * The provider for calculating Child Limit information.
     */
    public static ChildLimitProvider provider = new DefaultChildLimitProvider();

    /**
     * The Eight Characters (八字) of the birth.
     */
    protected EightChar eightChar;

    /**
     * The gender of the person.
     */
    protected Gender gender;

    /**
     * Indicates whether the fortune is calculated forward (顺推) or backward (逆推).
     */
    protected boolean forward;

    /**
     * The detailed information about the Child Limit.
     */
    protected ChildLimitInfo info;

    /**
     * Constructs a {@code ChildLimit} instance with the specified birth time and gender.
     *
     * @param birthTime The Gregorian birth time.
     * @param gender    The gender of the person.
     */
    public ChildLimit(SolarTime birthTime, Gender gender) {
        this.gender = gender;
        eightChar = birthTime.getLunarHour().getEightChar();
        // Determine forward or backward calculation: Yang male, Yin female -> forward; Yin male, Yang female ->
        // backward
        boolean yang = Opposite.YANG == eightChar.getYear().getHeavenStem().getOpposite();
        boolean man = Gender.MALE == gender;
        forward = (yang && man) || (!yang && !man);
        SolarTerms term = birthTime.getTerm();
        if (!term.isJie()) {
            term = term.next(-1);
        }
        if (forward) {
            term = term.next(2);
        }
        info = provider.getInfo(birthTime, term);
    }

    /**
     * Creates a {@code ChildLimit} instance from a Gregorian birth time and gender.
     *
     * @param birthTime The Gregorian birth time.
     * @param gender    The gender of the person.
     * @return A new {@code ChildLimit} instance.
     */
    public static ChildLimit fromSolarTime(SolarTime birthTime, Gender gender) {
        return new ChildLimit(birthTime, gender);
    }

    /**
     * Gets the Eight Characters (八字) associated with this Child Limit.
     *
     * @return The {@link EightChar} instance.
     */
    public EightChar getEightChar() {
        return eightChar;
    }

    /**
     * Gets the gender of the person.
     *
     * @return The {@link Gender} of the person.
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * Checks if the fortune calculation is forward (顺推).
     *
     * @return {@code true} if forward, {@code false} if backward.
     */
    public boolean isForward() {
        return forward;
    }

    /**
     * Gets the number of years in the Child Limit.
     *
     * @return The number of years.
     */
    public int getYearCount() {
        return info.getYearCount();
    }

    /**
     * Gets the number of months in the Child Limit.
     *
     * @return The number of months.
     */
    public int getMonthCount() {
        return info.getMonthCount();
    }

    /**
     * Gets the number of days in the Child Limit.
     *
     * @return The number of days.
     */
    public int getDayCount() {
        return info.getDayCount();
    }

    /**
     * Gets the number of hours in the Child Limit.
     *
     * @return The number of hours.
     */
    public int getHourCount() {
        return info.getHourCount();
    }

    /**
     * Gets the number of minutes in the Child Limit.
     *
     * @return The number of minutes.
     */
    public int getMinuteCount() {
        return info.getMinuteCount();
    }

    /**
     * Gets the start time (birth time) of the Child Limit.
     *
     * @return The {@link SolarTime} instance representing the start time.
     */
    public SolarTime getStartTime() {
        return info.getStartTime();
    }

    /**
     * Gets the end time (start of Grand Fortune) of the Child Limit.
     *
     * @return The {@link SolarTime} instance representing the end time.
     */
    public SolarTime getEndTime() {
        return info.getEndTime();
    }

    /**
     * Gets the starting Grand Fortune (大运) for this Child Limit.
     *
     * @return The {@link DecadeFortune} instance.
     */
    public DecadeFortune getStartDecadeFortune() {
        return DecadeFortune.fromChildLimit(this, 0);
    }

    /**
     * Gets the Grand Fortune (大运) associated with this Child Limit.
     *
     * @return The {@link DecadeFortune} instance.
     */
    public DecadeFortune getDecadeFortune() {
        return DecadeFortune.fromChildLimit(this, -1);
    }

    /**
     * Gets the starting Fortune (小运) for this Child Limit.
     *
     * @return The {@link Fortune} instance.
     */
    public Fortune getStartFortune() {
        return Fortune.fromChildLimit(this, 0);
    }

    /**
     * Gets the Sixty-Year Cycle Year (干支年) of the start time (birth).
     *
     * @return The {@link SixtyCycleYear} instance.
     */
    public SixtyCycleYear getStartSixtyCycleYear() {
        return SixtyCycleYear.fromYear(getStartTime().getYear());
    }

    /**
     * Gets the Sixty-Year Cycle Year (干支年) of the end time (start of Grand Fortune).
     *
     * @return The {@link SixtyCycleYear} instance.
     */
    public SixtyCycleYear getEndSixtyCycleYear() {
        return SixtyCycleYear.fromYear(getEndTime().getYear());
    }

    /**
     * Gets the starting age for the Child Limit (always 1).
     *
     * @return The starting age.
     */
    public int getStartAge() {
        return 1;
    }

    /**
     * Gets the ending age for the Child Limit.
     *
     * @return The ending age.
     */
    public int getEndAge() {
        int n = getEndSixtyCycleYear().getYear() - getStartSixtyCycleYear().getYear();
        return Math.max(n, 1);
    }

}
