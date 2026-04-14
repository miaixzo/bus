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
package org.miaixz.bus.core.center.date.culture.festival;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.lunar.LunarDay;
import org.miaixz.bus.core.center.date.culture.lunar.LunarMonth;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarMonth;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;

/**
 * Festival definition with encoded rule data.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Festival extends Loops {

    /**
     * Festival name.
     */
    protected String name;

    /**
     * Encoded festival data.
     */
    protected String data;

    /**
     * Validates the encoded festival data.
     *
     * @param data encoded data string (must be exactly 9 characters)
     * @throws IllegalArgumentException if data is null or not 9 characters long
     */
    public static void validate(String data) {
        if (null == data) {
            throw new IllegalArgumentException("illegal event data: null");
        }
        if (data.length() != 9) {
            throw new IllegalArgumentException("illegal event data: " + data);
        }
    }

    /**
     * Constructs a festival with the given name and encoded data.
     *
     * @param name festival name
     * @param data encoded festival data
     */
    public Festival(String name, String data) {
        validate(data);
        this.name = name;
        this.data = data;
    }

    /**
     * Creates a new festival builder.
     *
     * @return festival builder
     */
    public static FestivalBuilder builder() {
        return new FestivalBuilder();
    }

    /**
     * Finds a festival by its name from the registry.
     *
     * @param name festival name
     * @return the matching festival, or {@code null} if not found
     */
    public static Festival fromName(String name) {
        Matcher matcher = Pattern.compile(String.format(FestivalRegistry.REGEX, name)).matcher(FestivalRegistry.DATA);
        return matcher.find() ? new Festival(name, matcher.group(1)) : null;
    }

    /**
     * Gets the registry character index stored at the specified payload position.
     *
     * @param index payload position
     * @return registry character index
     */
    protected int getCharIndex(int index) {
        return FestivalRegistry.CHARS.indexOf(data.charAt(index));
    }

    /**
     * Decodes the signed numeric value stored at the specified payload position.
     *
     * @param index payload position
     * @return decoded numeric value
     */
    public int getValue(int index) {
        return getCharIndex(index) - 31;
    }

    /**
     * Resolves the logical festival month for the specified year.
     *
     * @param year target year
     * @return a two-element array containing resolved year and month
     */
    public int[] getMonth(int year) {
        int y = year;
        int m = getValue(2);
        if (m > 12) {
            m = 1;
            y += 1;
        }
        return new int[] { y, m };
    }

    /**
     * Gets the festival rule type.
     *
     * @return festival rule type
     */
    public FestivalRule getType() {
        return FestivalRule.fromCode(getCharIndex(1));
    }

    /**
     * Gets the festival name.
     *
     * @return festival name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the encoded festival data.
     *
     * @return encoded data string
     */
    public String getData() {
        return data;
    }

    /**
     * Gets the start year of the festival.
     *
     * @return start year
     */
    public int getStartYear() {
        int n = 0;
        int size = FestivalRegistry.CHARS.length();
        for (int i = 0; i < 3; i++) {
            n = n * size + getCharIndex(6 + i);
        }
        return n;
    }

    /**
     * Gets festivals matching the given solar day.
     *
     * @param d solar day
     * @return list of matching festivals
     */
    public static List<Festival> fromSolarDay(SolarDay d) {
        List<Festival> l = new ArrayList<>();
        for (Festival e : all()) {
            if (d.equals(e.getSolarDay(d.getYear()))) {
                l.add(e);
            }
        }
        return l;
    }

    /**
     * Gets all registered festivals.
     *
     * @return list of all festivals
     */
    public static List<Festival> all() {
        List<Festival> l = new ArrayList<>();
        Matcher matcher = Pattern.compile(String.format(FestivalRegistry.REGEX, ".[^@]+"))
                .matcher(FestivalRegistry.DATA);
        while (matcher.find()) {
            l.add(new Festival(matcher.group(2), matcher.group(1)));
        }
        return l;
    }

    /**
     * Gets the solar day for this festival in the given year.
     *
     * @param year year
     * @return solar day, or {@code null} if the festival does not occur in the given year
     */
    public SolarDay getSolarDay(int year) {
        FestivalRule type = getType();
        if (null == type) {
            return null;
        }
        if (year < getStartYear()) {
            return null;
        }
        SolarDay d = null;
        switch (type) {
            case SOLAR_DAY:
                d = getSolarDayBySolarDay(year);
                break;

            case SOLAR_WEEK:
                d = getSolarDayByWeek(year);
                break;

            case LUNAR_DAY:
                d = getSolarDayByLunarDay(year);
                break;

            case TERM_DAY:
                d = getSolarDayByTerm(year);
                break;

            case TERM_HS:
                d = getSolarDayByTermHeavenStem(year);
                break;

            case TERM_EB:
                d = getSolarDayByTermEarthBranch(year);
                break;
        }
        if (null == d) {
            return null;
        }
        int offset = getValue(5);
        return 0 == offset ? d : d.next(offset);
    }

    /**
     * Resolves the solar day for a solar-day-based festival rule.
     *
     * @param year year
     * @return solar day, or {@code null} if not applicable
     */
    protected SolarDay getSolarDayBySolarDay(int year) {
        int[] month = getMonth(year);
        int y = month[0];
        int m = month[1];
        int d = getValue(3);
        int delay = getValue(4);
        int lastDay = SolarMonth.fromYm(y, m).getDayCount();
        if (d > lastDay) {
            if (0 == delay) {
                return null;
            }
            return delay < 0 ? SolarDay.fromYmd(y, m, d + delay) : SolarDay.fromYmd(y, m, lastDay).next(delay);
        }
        return SolarDay.fromYmd(y, m, d);
    }

    /**
     * Resolves the solar day for a lunar-day-based festival rule.
     *
     * @param year year
     * @return solar day, or {@code null} if not applicable
     */
    protected SolarDay getSolarDayByLunarDay(int year) {
        int[] month = getMonth(year);
        int y = month[0];
        int m = month[1];
        int d = getValue(3);
        int delay = getValue(4);
        int lastDay = LunarMonth.fromYm(y, m).getDayCount();
        if (d > lastDay) {
            if (0 == delay) {
                return null;
            }
            return delay < 0 ? LunarDay.fromYmd(y, m, d + delay).getSolarDay()
                    : LunarDay.fromYmd(y, m, lastDay).getSolarDay().next(delay);
        }
        return LunarDay.fromYmd(y, m, d).getSolarDay();
    }

    /**
     * Resolves the solar day for a solar-week-based festival rule.
     *
     * @param year year
     * @return solar day, or {@code null} if not applicable
     */
    protected SolarDay getSolarDayByWeek(int year) {
        // which week occurrence
        int n = getValue(3);
        if (n == 0) {
            return null;
        }
        SolarMonth m = SolarMonth.fromYm(year, getValue(2));
        // day of week
        int w = getValue(4);
        if (n > 0) {
            // first day of the month
            SolarDay d = m.getFirstDay();
            // find the n-th occurrence of the given weekday
            return d.next(d.getWeek().stepsTo(w) + 7 * n - 7);
        }
        // last day of the month
        SolarDay d = SolarDay.fromYmd(year, m.getMonth(), m.getDayCount());
        // find the n-th occurrence from the end of the given weekday
        return d.next(d.getWeek().stepsBackTo(w) + 7 * n + 7);
    }

    /**
     * Resolves the solar day for a solar-term-based festival rule.
     *
     * @param year year
     * @return solar day
     */
    protected SolarDay getSolarDayByTerm(int year) {
        SolarDay d = SolarTerms.fromIndex(year, getValue(2)).getSolarDay();
        int offset = getValue(4);
        return 0 == offset ? d : d.next(offset);
    }

    /**
     * Resolves the solar day for a solar-term heaven-stem-based festival rule.
     *
     * @param year year
     * @return solar day
     */
    protected SolarDay getSolarDayByTermHeavenStem(int year) {
        SolarDay d = getSolarDayByTerm(year);
        return d.next(d.getLunarDay().getSixtyCycle().getHeavenStem().stepsTo(getValue(3)));
    }

    /**
     * Resolves the solar day for a solar-term earth-branch-based festival rule.
     *
     * @param year year
     * @return solar day
     */
    protected SolarDay getSolarDayByTermEarthBranch(int year) {
        SolarDay d = getSolarDayByTerm(year);
        return d.next(d.getLunarDay().getSixtyCycle().getEarthBranch().stepsTo(getValue(3)));
    }

}
