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

import org.miaixz.bus.core.center.date.Holiday;
import org.miaixz.bus.core.center.date.Zodiac;
import org.miaixz.bus.core.center.date.culture.HiddenStems;
import org.miaixz.bus.core.center.date.culture.JulianDay;
import org.miaixz.bus.core.center.date.culture.Phase;
import org.miaixz.bus.core.center.date.culture.Week;
import org.miaixz.bus.core.center.date.culture.climate.Climate;
import org.miaixz.bus.core.center.date.culture.climate.ClimateDay;
import org.miaixz.bus.core.center.date.culture.dog.Dog;
import org.miaixz.bus.core.center.date.culture.dog.DogDay;
import org.miaixz.bus.core.center.date.culture.lunar.LunarDay;
import org.miaixz.bus.core.center.date.culture.lunar.LunarMonth;
import org.miaixz.bus.core.center.date.culture.nine.Nine;
import org.miaixz.bus.core.center.date.culture.nine.NineDay;
import org.miaixz.bus.core.center.date.culture.parts.DayParts;
import org.miaixz.bus.core.center.date.culture.plumrain.PlumRain;
import org.miaixz.bus.core.center.date.culture.plumrain.PlumRainDay;
import org.miaixz.bus.core.center.date.culture.rabjung.RabjungDay;
import org.miaixz.bus.core.center.date.culture.sixty.HiddenStem;
import org.miaixz.bus.core.center.date.culture.sixty.HiddenStemDay;
import org.miaixz.bus.core.center.date.culture.sixty.SixtyCycleDay;

/**
 * Represents a day in the Gregorian calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarDay extends DayParts {

    /**
     * Array of Chinese day names (1日 to 31日).
     */
    public static final String[] NAMES = { "1日", "2日", "3日", "4日", "5日", "6日", "7日", "8日", "9日", "10日", "11日", "12日",
            "13日", "14日", "15日", "16日", "17日", "18日", "19日", "20日", "21日", "22日", "23日", "24日", "25日", "26日", "27日",
            "28日", "29日", "30日", "31日" };

    /**
     * Constructs a SolarDay instance.
     *
     * @param year  the year (1-9999)
     * @param month the month (1-12)
     * @param day   the day of month
     * @throws IllegalArgumentException if the date is invalid (e.g., February 30)
     */
    public SolarDay(int year, int month, int day) {
        validate(year, month, day);
        this.year = year;
        this.month = month;
        this.day = day;
    }

    /**
     * Creates a SolarDay from year, month, and day values.
     *
     * @param year  the year (1-9999)
     * @param month the month (1-12)
     * @param day   the day of month
     * @return a new SolarDay instance
     * @throws IllegalArgumentException if the date is invalid
     */
    public static SolarDay fromYmd(int year, int month, int day) {
        return new SolarDay(year, month, day);
    }

    /**
     * Validates the given year, month, and day.
     *
     * @param year  the year
     * @param month the month
     * @param day   the day
     * @throws IllegalArgumentException if the date is invalid
     */
    public static void validate(int year, int month, int day) {
        if (day < 1) {
            throw new IllegalArgumentException(String.format("illegal solar day: %d-%d-%d", year, month, day));
        }
        if (1582 == year && 10 == month) {
            if ((day > 4 && day < 15) || day > 31) {
                throw new IllegalArgumentException(String.format("illegal solar day: %d-%d-%d", year, month, day));
            }
        } else if (day > SolarMonth.fromYm(year, month).getDayCount()) {
            throw new IllegalArgumentException(String.format("illegal solar day: %d-%d-%d", year, month, day));
        }
    }

    /**
     * Gets the solar month containing this day.
     *
     * @return the SolarMonth
     */
    public SolarMonth getSolarMonth() {
        return SolarMonth.fromYm(year, month);
    }

    /**
     * Gets the day of week.
     *
     * @return the Week enum value
     */
    public Week getWeek() {
        return getJulianDay().getWeek();
    }

    /**
     * Gets the zodiac sign for this day.
     *
     * @return the Zodiac sign
     */
    public Zodiac getZodiac() {
        int y = month * 100 + day;
        return Zodiac.get(
                y > 1221 || y < 120 ? 9
                        : y < 219 ? 10
                                : y < 321 ? 11
                                        : y < 420 ? 0
                                                : y < 521 ? 1
                                                        : y < 622 ? 2
                                                                : y < 723 ? 3
                                                                        : y < 823 ? 4
                                                                                : y < 923 ? 5
                                                                                        : y < 1024 ? 6
                                                                                                : y < 1123 ? 7 : 8);
    }

    /**
     * Gets the Chinese name of this day (e.g., "1日", "2日").
     *
     * @return the Chinese day name
     */
    public String getName() {
        return NAMES[day - 1];
    }

    /**
     * Gets the next or previous solar day.
     *
     * @param n the number of days to move (positive for forward, negative for backward)
     * @return the SolarDay n days from this day
     */
    public SolarDay next(int n) {
        return getJulianDay().next(n).getSolarDay();
    }

    /**
     * Checks if this day is before the target day.
     *
     * @param target the solar day to compare with
     * @return true if this day is before the target
     */
    public boolean isBefore(SolarDay target) {
        if (year != target.getYear()) {
            return year < target.getYear();
        }
        return month != target.getMonth() ? month < target.getMonth() : day < target.getDay();
    }

    /**
     * Checks if this day is after the target day.
     *
     * @param target the solar day to compare with
     * @return true if this day is after the target
     */
    public boolean isAfter(SolarDay target) {
        if (year != target.getYear()) {
            return year > target.getYear();
        }
        return month != target.getMonth() ? month > target.getMonth() : day > target.getDay();
    }

    /**
     * Gets the solar term for this day.
     *
     * @return the SolarTerms
     */
    public SolarTerms getTerm() {
        return getTermDay().getSolarTerm();
    }

    /**
     * Gets the solar term day information (which solar term and which day within that term).
     *
     * @return the SolarTermDay containing the term and day index
     */
    public SolarTermDay getTermDay() {
        int y = year;
        int i = month * 2;
        if (i == 24) {
            y += 1;
            i = 0;
        }
        SolarTerms term = SolarTerms.fromIndex(y, i);
        SolarDay day = term.getSolarDay();
        while (isBefore(day)) {
            term = term.next(-1);
            day = term.getSolarDay();
        }
        return new SolarTermDay(term, subtract(day));
    }

    /**
     * Gets the solar week containing this day.
     *
     * @param start the start day of week (1=Monday, 2=Tuesday, ..., 0=Sunday)
     * @return the SolarWeek
     */
    public SolarWeek getSolarWeek(int start) {
        return SolarWeek.fromYm(
                year,
                month,
                (int) Math.ceil((day + fromYmd(year, month, 1).getWeek().next(-start).getIndex()) / 7D) - 1,
                start);
    }

    /**
     * Gets the climate pentad (five-day period) for this day.
     *
     * @return the Climate (pentad)
     */
    public Climate getPhenology() {
        return getPhenologyDay().getClimate();
    }

    /**
     * Gets the climate day information (which pentad and which day within that pentad). The 72 pentads system divides
     * the solar year into 72 periods of approximately 5 days each.
     *
     * @return the ClimateDay containing the pentad and day index
     */
    public ClimateDay getPhenologyDay() {
        SolarTermDay d = getTermDay();
        int dayIndex = d.getDayIndex();
        int index = dayIndex / 5;
        if (index > 2) {
            index = 2;
        }
        SolarTerms term = d.getSolarTerm();
        return new ClimateDay(Climate.fromIndex(term.getYear(), term.getIndex() * 3 + index), dayIndex - index * 5);
    }

    /**
     * Gets the Sanfu day (the three ten-day periods of the hottest season). Sanfu consists of: Toufu (first 10 days),
     * Zhongfu (10-20 or 10-40 days), Mofu (last 10 days).
     *
     * @return the DogDay, or null if not in the Sanfu period
     */
    public DogDay getDogDay() {
        // Summer Solstice
        SolarTerms xiaZhi = SolarTerms.fromIndex(year, 12);
        SolarDay start = xiaZhi.getSolarDay();
        // The 3rd Geng day, which is the 1st day of the first period (Toufu)
        start = start.next(start.getLunarDay().getSixtyCycle().getHeavenStem().stepsTo(6) + 20);
        int days = subtract(start);
        // Before the first period (Toufu)
        if (days < 0) {
            return null;
        }
        if (days < 10) {
            return new DogDay(Dog.fromIndex(0), days);
        }
        // The 4th Geng day, the 1st day of the middle period (Zhongfu)
        start = start.next(10);
        days = subtract(start);
        if (days < 10) {
            return new DogDay(Dog.fromIndex(1), days);
        }
        // The 5th Geng day, the 11th day of Zhongfu or the 1st day of the last period (Mofu)
        start = start.next(10);
        days = subtract(start);
        // Start of Autumn (Liqiu)
        if (xiaZhi.next(3).getSolarDay().isAfter(start)) {
            if (days < 10) {
                return new DogDay(Dog.fromIndex(1), days + 10);
            }
            start = start.next(10);
            days = subtract(start);
        }
        return days >= 10 ? null : new DogDay(Dog.fromIndex(2), days);
    }

    /**
     * Gets the Shujiu day (the nine nine-day periods of the coldest season). Shujiu consists of 9 periods of 9 days
     * each, starting from the Winter Solstice.
     *
     * @return the NineDay, or null if not in the Shujiu period
     */
    public NineDay getNineDay() {
        SolarDay start = SolarTerms.fromIndex(year + 1, 0).getSolarDay();
        if (isBefore(start)) {
            start = SolarTerms.fromIndex(year, 0).getSolarDay();
        }
        SolarDay end = start.next(81);
        if (isBefore(start) || !isBefore(end)) {
            return null;
        }
        int days = subtract(start);
        return new NineDay(Nine.fromIndex(days / 9), days % 9);
    }

    /**
     * Gets the Meiyu (plum rain) day information. Meiyu season begins on the first Bing day after Mangzhong and ends on
     * the first Wei day after Xiaoshu.
     *
     * @return the PlumRainDay, or null if not in the Meiyu period
     */
    public PlumRainDay getPlumRainDay() {
        // Grain in Ear (Mangzhong)
        SolarTerms grainInEar = SolarTerms.fromIndex(year, 11);
        SolarDay start = grainInEar.getSolarDay();
        // The 1st Bing day after Grain in Ear (Mangzhong)
        start = start.next(start.getLunarDay().getSixtyCycle().getHeavenStem().stepsTo(2));

        // Minor Heat (Xiaoshu)
        SolarDay end = grainInEar.next(2).getSolarDay();
        // The 1st Wei day after Minor Heat (Xiaoshu)
        end = end.next(end.getLunarDay().getSixtyCycle().getEarthBranch().stepsTo(7));

        if (isBefore(start) || isAfter(end)) {
            return null;
        }
        return equals(end) ? new PlumRainDay(PlumRain.fromIndex(1), 0)
                : new PlumRainDay(PlumRain.fromIndex(0), subtract(start));
    }

    /**
     * Gets the hidden heavenly stem day (Renyuan Siling). This is used in Chinese astrology to determine the hidden
     * stems in a day.
     *
     * @return the HiddenStemDay
     */
    public HiddenStemDay getHideHeavenStemDay() {
        int[] dayCounts = { 3, 5, 7, 9, 10, 30 };
        SolarTerms term = getTerm();
        if (term.isQi()) {
            term = term.next(-1);
        }
        int dayIndex = subtract(term.getSolarDay());
        int startIndex = (term.getIndex() - 1) * 3;
        String data = "93705542220504xx1513904541632524533533105544806564xx7573304542018584xx95"
                .substring(startIndex, startIndex + 6);
        int days = 0;
        int heavenStemIndex = 0;
        int typeIndex = 0;
        while (typeIndex < 3) {
            int i = typeIndex * 2;
            String d = data.substring(i, i + 1);
            int count = 0;
            if (!d.equals("x")) {
                heavenStemIndex = Integer.parseInt(d);
                count = dayCounts[Integer.parseInt(data.substring(i + 1, i + 2))];
                days += count;
            }
            if (dayIndex <= days) {
                dayIndex -= days - count;
                break;
            }
            typeIndex++;
        }
        return new HiddenStemDay(new HiddenStem(heavenStemIndex, HiddenStems.fromCode(typeIndex)), dayIndex);
    }

    /**
     * Gets the index of this day within the year (0-based, Jan 1 = 0).
     *
     * @return the day index within the year
     */
    public int getIndexInYear() {
        return subtract(fromYmd(year, 1, 1));
    }

    /**
     * Calculates the difference in days between this day and the target day.
     *
     * @param target the solar day to subtract from this day
     * @return the number of days difference (positive if target is earlier)
     */
    public int subtract(SolarDay target) {
        return (int) (getJulianDay().subtract(target.getJulianDay()));
    }

    /**
     * Gets the Julian Day for this solar day (at noon).
     *
     * @return the JulianDay
     */
    public JulianDay getJulianDay() {
        return JulianDay.fromYmdHms(year, month, day, 0, 0, 0);
    }

    /**
     * Converts this solar day to a lunar day.
     *
     * @return the corresponding LunarDay
     */
    public LunarDay getLunarDay() {
        LunarMonth m = LunarMonth.fromYm(year, month);
        int days = subtract(m.getFirstJulianDay().getSolarDay());
        while (days < 0) {
            m = m.next(-1);
            days += m.getDayCount();
        }
        return LunarDay.fromYmd(m.getYear(), m.getMonthWithLeap(), days + 1);
    }

    /**
     * Converts this solar day to a sexagenary cycle (Gan-Zhi) day.
     *
     * @return the corresponding SixtyCycleDay
     */
    public SixtyCycleDay getSixtyCycleDay() {
        return SixtyCycleDay.fromSolarDay(this);
    }

    /**
     * Converts this solar day to a Tibetan calendar (Rabjung) day.
     *
     * @return the corresponding RabjungDay
     */
    public RabjungDay getRabByungDay() {
        return RabjungDay.fromSolarDay(this);
    }

    /**
     * Gets the legal holiday for this day, if any.
     *
     * @return the Holiday, or null if this day is not a legal holiday
     */
    public Holiday getLegalHoliday() {
        return Holiday.fromYmd(year, month, day);
    }

    /**
     * Gets the solar festival (modern festival) for this day, if any.
     *
     * @return the SolarFestival, or null if this day is not a solar festival
     */
    public SolarFestival getFestival() {
        return SolarFestival.fromYmd(year, month, day);
    }

    /**
     * Gets the moon phase day information (which phase and which day within that phase).
     *
     * @return the Phase.PhaseDay containing the phase and day index
     */
    public Phase.PhaseDay getPhaseDay() {
        LunarMonth month = getLunarDay().getLunarMonth().next(1);
        Phase p = Phase.fromIndex(month.getYear(), month.getMonthWithLeap(), 0);
        SolarDay d = p.getSolarDay();
        while (d.isAfter(this)) {
            p = p.next(-1);
            d = p.getSolarDay();
        }
        return new Phase.PhaseDay(p, subtract(d));
    }

    /**
     * Gets the moon phase for this day.
     *
     * @return the Phase
     */
    public Phase getPhase() {
        return getPhaseDay().getPhase();
    }

    @Override
    public String toString() {
        return getSolarMonth() + getName();
    }

}
