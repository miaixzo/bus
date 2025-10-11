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
import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.HiddenStems;
import org.miaixz.bus.core.center.date.culture.cn.JulianDay;
import org.miaixz.bus.core.center.date.culture.cn.Week;
import org.miaixz.bus.core.center.date.culture.cn.climate.Climate;
import org.miaixz.bus.core.center.date.culture.cn.climate.ClimateDay;
import org.miaixz.bus.core.center.date.culture.cn.dog.Dog;
import org.miaixz.bus.core.center.date.culture.cn.dog.DogDay;
import org.miaixz.bus.core.center.date.culture.cn.nine.Nine;
import org.miaixz.bus.core.center.date.culture.cn.nine.NineDay;
import org.miaixz.bus.core.center.date.culture.cn.plumrain.PlumRain;
import org.miaixz.bus.core.center.date.culture.cn.plumrain.PlumRainDay;
import org.miaixz.bus.core.center.date.culture.cn.sixty.HiddenStem;
import org.miaixz.bus.core.center.date.culture.cn.sixty.HiddenStemDay;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycleDay;
import org.miaixz.bus.core.center.date.culture.en.Constellation;
import org.miaixz.bus.core.center.date.culture.lunar.LunarDay;
import org.miaixz.bus.core.center.date.culture.lunar.LunarMonth;
import org.miaixz.bus.core.center.date.culture.rabjung.RabjungDay;

/**
 * Represents a day in the Gregorian calendar.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarDay extends Loops {

    /**
     * Names of solar days.
     */
    public static final String[] NAMES = { "1日", "2日", "3日", "4日", "5日", "6日", "7日", "8日", "9日", "10日", "11日", "12日",
            "13日", "14日", "15日", "16日", "17日", "18日", "19日", "20日", "21日", "22日", "23日", "24日", "25日", "26日", "27日",
            "28日", "29日", "30日", "31日" };

    /**
     * The solar month this day belongs to.
     */
    protected SolarMonth month;

    /**
     * The day of the solar month.
     */
    protected int day;

    /**
     * Constructs a {@code SolarDay} with the given year, month, and day.
     *
     * @param year  The year.
     * @param month The month.
     * @param day   The day.
     * @throws IllegalArgumentException if the day is out of valid range for the given month and year.
     */
    public SolarDay(int year, int month, int day) {
        if (day < 1) {
            throw new IllegalArgumentException(String.format("illegal solar day: %d-%d-%d", year, month, day));
        }
        SolarMonth m = SolarMonth.fromYm(year, month);
        if (1582 == year && 10 == month) {
            if ((day > 4 && day < 15) || day > 31) {
                throw new IllegalArgumentException(String.format("illegal solar day: %d-%d-%d", year, month, day));
            }
        } else if (day > m.getDayCount()) {
            throw new IllegalArgumentException(String.format("illegal solar day: %d-%d-%d", year, month, day));
        }
        this.month = m;
        this.day = day;
    }

    /**
     * Creates a {@code SolarDay} instance from the given year, month, and day.
     *
     * @param year  The year.
     * @param month The month.
     * @param day   The day.
     * @return A new {@link SolarDay} instance.
     */
    public static SolarDay fromYmd(int year, int month, int day) {
        return new SolarDay(year, month, day);
    }

    /**
     * Gets the solar month this day belongs to.
     *
     * @return The {@link SolarMonth}.
     */
    public SolarMonth getSolarMonth() {
        return month;
    }

    /**
     * Gets the year of this solar day.
     *
     * @return The year.
     */
    public int getYear() {
        return month.getYear();
    }

    /**
     * Gets the month of this solar day.
     *
     * @return The month.
     */
    public int getMonth() {
        return month.getMonth();
    }

    /**
     * Gets the day of this solar day.
     *
     * @return The day.
     */
    public int getDay() {
        return day;
    }

    /**
     * Gets the week of this solar day.
     *
     * @return The {@link Week} of this day.
     */
    public Week getWeek() {
        return getJulianDay().getWeek();
    }

    /**
     * Gets the constellation for this solar day.
     *
     * @return The {@link Constellation} for this day.
     */
    public Constellation getConstellation() {
        int y = getMonth() * 100 + day;
        return Constellation.get(
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
     * Gets the Chinese name of the solar day.
     *
     * @return The name of the solar day.
     */
    public String getName() {
        return NAMES[day - 1];
    }

    @Override
    public String toString() {
        return month + getName();
    }

    /**
     * Gets the solar day after a specified number of days.
     *
     * @param n The number of days to add.
     * @return The {@link SolarDay} after {@code n} days.
     */
    public SolarDay next(int n) {
        return getJulianDay().next(n).getSolarDay();
    }

    /**
     * Checks if this solar day is before the target solar day.
     *
     * @param target The target solar day.
     * @return {@code true} if this day is before the target, {@code false} otherwise.
     */
    public boolean isBefore(SolarDay target) {
        int aYear = getYear();
        int bYear = target.getYear();
        if (aYear != bYear) {
            return aYear < bYear;
        }
        int aMonth = getMonth();
        int bMonth = target.getMonth();
        return aMonth != bMonth ? aMonth < bMonth : day < target.getDay();
    }

    /**
     * Checks if this solar day is after the target solar day.
     *
     * @param target The target solar day.
     * @return {@code true} if this day is after the target, {@code false} otherwise.
     */
    public boolean isAfter(SolarDay target) {
        int aYear = getYear();
        int bYear = target.getYear();
        if (aYear != bYear) {
            return aYear > bYear;
        }
        int aMonth = getMonth();
        int bMonth = target.getMonth();
        return aMonth != bMonth ? aMonth > bMonth : day > target.getDay();
    }

    /**
     * Gets the solar term for this solar day.
     *
     * @return The {@link SolarTerms} for this day.
     */
    public SolarTerms getTerm() {
        return getTermDay().getSolarTerm();
    }

    /**
     * Gets the solar term day information for this solar day.
     *
     * @return The {@link SolarTermDay} for this day.
     */
    public SolarTermDay getTermDay() {
        int y = getYear();
        int i = getMonth() * 2;
        if (i == 24) {
            y += 1;
            i = 0;
        }
        SolarTerms term = SolarTerms.fromIndex(y, i);
        SolarDay day = term.getJulianDay().getSolarDay();
        while (isBefore(day)) {
            term = term.next(-1);
            day = term.getJulianDay().getSolarDay();
        }
        return new SolarTermDay(term, subtract(day));
    }

    /**
     * Gets the solar week for this solar day.
     *
     * @param start The starting day of the week, 1-7 (1 for Monday, 7 for Sunday).
     * @return The {@link SolarWeek} for this day.
     */
    public SolarWeek getSolarWeek(int start) {
        int y = getYear();
        int m = getMonth();
        return SolarWeek.fromYm(
                y,
                m,
                (int) Math.ceil((day + fromYmd(y, m, 1).getWeek().next(-start).getIndex()) / 7D) - 1,
                start);
    }

    /**
     * Gets the phenology (Hou) for this solar day.
     *
     * @return The {@link Climate} for this day.
     */
    public Climate getPhenology() {
        return getClimateDay().getClimate();
    }

    /**
     * Gets the climate day information (72 Hou) for this solar day.
     *
     * @return The {@link ClimateDay} for this day.
     */
    public ClimateDay getClimateDay() {
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
     * Gets the Dog Day (SanFuTian) information for this solar day.
     *
     * @return The {@link DogDay} for this day.
     */
    public DogDay getDogDay() {
        // Summer Solstice
        SolarTerms xiaZhi = SolarTerms.fromIndex(getYear(), 12);
        SolarDay start = xiaZhi.getJulianDay().getSolarDay();
        // The 3rd Geng day after Summer Solstice, which is the 1st day of Chufu.
        start = start.next(start.getLunarDay().getSixtyCycle().getHeavenStem().stepsTo(6) + 20);
        int days = subtract(start);
        // Before Chufu
        if (days < 0) {
            return null;
        }
        if (days < 10) {
            return new DogDay(Dog.fromIndex(0), days);
        }
        // The 4th Geng day, 1st day of Zhongfu.
        start = start.next(10);
        days = subtract(start);
        if (days < 10) {
            return new DogDay(Dog.fromIndex(1), days);
        }
        // The 5th Geng day, 11th day of Zhongfu or 1st day of Mofu.
        start = start.next(10);
        days = subtract(start);
        // Autumn Begins
        if (xiaZhi.next(3).getJulianDay().getSolarDay().isAfter(start)) {
            if (days < 10) {
                return new DogDay(Dog.fromIndex(1), days + 10);
            }
            start = start.next(10);
            days = subtract(start);
        }
        return days >= 10 ? null : new DogDay(Dog.fromIndex(2), days);
    }

    /**
     * Gets the Nine Day (ShuJiuTian) information for this solar day.
     *
     * @return The {@link NineDay} for this day.
     */
    public NineDay getNineDay() {
        int year = getYear();
        SolarDay start = SolarTerms.fromIndex(year + 1, 0).getJulianDay().getSolarDay();
        if (isBefore(start)) {
            start = SolarTerms.fromIndex(year, 0).getJulianDay().getSolarDay();
        }
        SolarDay end = start.next(81);
        if (isBefore(start) || !isBefore(end)) {
            return null;
        }
        int days = subtract(start);
        return new NineDay(Nine.fromIndex(days / 9), days % 9);
    }

    /**
     * Gets the Plum Rain Day (MeiYuTian) information for this solar day (MeiYu starts on the first Bing day after
     * Mangzhong, and ends on the first Wei day after Xiaoshu).
     *
     * @return The {@link PlumRainDay} for this day.
     */
    public PlumRainDay getPlumRainDay() {
        // Grain in Ear
        SolarTerms grainInEar = SolarTerms.fromIndex(getYear(), 11);
        SolarDay start = grainInEar.getJulianDay().getSolarDay();
        // The first Bing day after Mangzhong
        start = start.next(start.getLunarDay().getSixtyCycle().getHeavenStem().stepsTo(2));

        // Minor Heat
        SolarDay end = grainInEar.next(2).getJulianDay().getSolarDay();
        // The first Wei day after Xiaoshu
        end = end.next(end.getLunarDay().getSixtyCycle().getEarthBranch().stepsTo(7));

        if (isBefore(start) || isAfter(end)) {
            return null;
        }
        return equals(end) ? new PlumRainDay(PlumRain.fromIndex(1), 0)
                : new PlumRainDay(PlumRain.fromIndex(0), subtract(start));
    }

    /**
     * Gets the Hidden Stem Day (RenYuanSiLingFenYe) information for this solar day.
     *
     * @return The {@link HiddenStemDay} for this day.
     */
    public HiddenStemDay getHideHeavenStemDay() {
        int[] dayCounts = { 3, 5, 7, 9, 10, 30 };
        SolarTerms term = getTerm();
        if (term.isQi()) {
            term = term.next(-1);
        }
        int dayIndex = subtract(term.getJulianDay().getSolarDay());
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
     * Gets the index of this day within the year.
     *
     * @return The index within the year (0-365 or 0-366 for leap years).
     */
    public int getIndexInYear() {
        return subtract(fromYmd(getYear(), 1, 1));
    }

    /**
     * Subtracts a target {@code SolarDay} from this {@code SolarDay}, returning the difference in days.
     *
     * @param target The target {@code SolarDay}.
     * @return The number of days difference.
     */
    public int subtract(SolarDay target) {
        return (int) (getJulianDay().subtract(target.getJulianDay()));
    }

    /**
     * Gets the Julian day corresponding to this solar day.
     *
     * @return The {@link JulianDay} for this solar day.
     */
    public JulianDay getJulianDay() {
        return JulianDay.fromYmdHms(getYear(), getMonth(), day, 0, 0, 0);
    }

    /**
     * Gets the lunar day corresponding to this solar day.
     *
     * @return The {@link LunarDay} for this solar day.
     */
    public LunarDay getLunarDay() {
        LunarMonth m = LunarMonth.fromYm(getYear(), getMonth());
        int days = subtract(m.getFirstJulianDay().getSolarDay());
        while (days < 0) {
            m = m.next(-1);
            days += m.getDayCount();
        }
        return LunarDay.fromYmd(m.getYear(), m.getMonthWithLeap(), days + 1);
    }

    /**
     * Gets the Sixty Cycle Day (GanZhi day) corresponding to this solar day.
     *
     * @return The {@link SixtyCycleDay} for this solar day.
     */
    public SixtyCycleDay getSixtyCycleDay() {
        return SixtyCycleDay.fromSolarDay(this);
    }

    /**
     * Gets the statutory holiday for this day. Returns {@code null} if it's not a statutory holiday.
     *
     * @return The {@link Holiday} if it's a holiday, otherwise {@code null}.
     */
    public Holiday getHoliday() {
        return Holiday.fromYmd(getYear(), getMonth(), day);
    }

    /**
     * Gets the modern Gregorian festival for this day. Returns {@code null} if it's not a modern Gregorian festival.
     *
     * @return The {@link SolarFestival} if it's a festival, otherwise {@code null}.
     */
    public SolarFestival getFestival() {
        return SolarFestival.fromYmd(getYear(), getMonth(), day);
    }

    /**
     * Gets the Tibetan day corresponding to this solar day.
     *
     * @return The {@link RabjungDay} for this solar day.
     */
    public RabjungDay getRabByungDay() {
        return RabjungDay.fromSolarDay(this);
    }

}
