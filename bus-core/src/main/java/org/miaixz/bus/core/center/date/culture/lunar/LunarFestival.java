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
package org.miaixz.bus.core.center.date.culture.lunar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.solar.SolarDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.lang.EnumValue;

/**
 * Represents traditional lunar festivals (based on the national standard "Compilation and Promulgation of the Lunar
 * Calendar" GB/T 33661-2017).
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LunarFestival extends Loops {

    /**
     * Names of lunar festivals.
     */
    public static final String[] NAMES = { "春节", "元宵节", "龙头节", "上巳节", "清明节", "端午节", "七夕节", "中元节", "中秋节", "重阳节", "冬至节",
            "腊八节", "除夕" };

    /**
     * Data string containing festival information.
     */
    public static String DATA = "@0000101@0100115@0200202@0300303@04107@0500505@0600707@0700715@0800815@0900909@10124@1101208@122";

    /**
     * The type of festival.
     */
    protected EnumValue.Festival type;

    /**
     * The index of the festival.
     */
    protected int index;

    /**
     * The lunar day of the festival.
     */
    protected LunarDay day;

    /**
     * The solar term associated with the festival, if any.
     */
    protected SolarTerms solarTerms;

    /**
     * The name of the festival.
     */
    protected String name;

    /**
     * Constructs a {@code LunarFestival} instance.
     *
     * @param type       The type of festival.
     * @param day        The lunar day of the festival.
     * @param solarTerms The solar term associated with the festival (can be null).
     * @param data       The raw data string for the festival.
     */
    public LunarFestival(EnumValue.Festival type, LunarDay day, SolarTerms solarTerms, String data) {
        this.type = type;
        this.day = day;
        this.solarTerms = solarTerms;
        index = Integer.parseInt(data.substring(1, 3), 10);
        name = NAMES[index];
    }

    /**
     * Creates a {@code LunarFestival} instance from the given year and festival index.
     *
     * @param year  The year.
     * @param index The index of the festival.
     * @return A new {@link LunarFestival} instance, or {@code null} if not found.
     * @throws IllegalArgumentException if the index is out of valid range.
     */
    public static LunarFestival fromIndex(int year, int index) {
        if (index < 0 || index >= NAMES.length) {
            return null;
        }
        Matcher matcher = Pattern.compile(String.format("@%02d\\d+", index)).matcher(DATA);
        if (!matcher.find()) {
            return null;
        }
        String data = matcher.group();
        EnumValue.Festival type = EnumValue.Festival.fromCode(data.charAt(3) - '0');
        switch (type) {
            case DAY:
                return new LunarFestival(type,
                        LunarDay.fromYmd(
                                year,
                                Integer.parseInt(data.substring(4, 6), 10),
                                Integer.parseInt(data.substring(6), 10)),
                        null, data);

            case TERM:
                SolarTerms solarTerm = SolarTerms.fromIndex(year, Integer.parseInt(data.substring(4), 10));
                return new LunarFestival(type, solarTerm.getSolarDay().getLunarDay(), solarTerm, data);

            case EVE:
                return new LunarFestival(type, LunarDay.fromYmd(year + 1, 1, 1).next(-1), null, data);

            default:
                return null;
        }
    }

    /**
     * Creates a {@code LunarFestival} instance from the given year, month, and day.
     *
     * @param year  The year.
     * @param month The month.
     * @param day   The day.
     * @return A new {@link LunarFestival} instance, or {@code null} if no festival matches.
     */
    public static LunarFestival fromYmd(int year, int month, int day) {
        Matcher matcher = Pattern.compile(String.format("@\\d{2}0%02d%02d", month, day)).matcher(DATA);
        if (matcher.find()) {
            return new LunarFestival(EnumValue.Festival.DAY, LunarDay.fromYmd(year, month, day), null, matcher.group());
        }
        LunarDay lunarDay = LunarDay.fromYmd(year, month, day);
        SolarDay solarDay = lunarDay.getSolarDay();
        matcher = Pattern.compile("@\\d{2}1\\d{2}").matcher(DATA);
        while (matcher.find()) {
            String data = matcher.group();
            SolarTerms term = SolarTerms.fromIndex(year, Integer.parseInt(data.substring(4), 10));
            SolarDay termDay = term.getSolarDay();
            if (termDay.getYear() == solarDay.getYear() && termDay.getMonth() == solarDay.getMonth()
                    && termDay.getDay() == solarDay.getDay()) {
                return new LunarFestival(EnumValue.Festival.TERM, lunarDay, term, data);
            }
        }
        if (month == 12 && day > 28) {
            matcher = Pattern.compile("@\\d{2}2").matcher(DATA);
            if (!matcher.find()) {
                return null;
            }
            LunarDay nextDay = lunarDay.next(1);
            if (nextDay.getMonth() == 1 && nextDay.getDay() == 1) {
                return new LunarFestival(EnumValue.Festival.EVE, lunarDay, null, matcher.group());
            }
        }
        return null;
    }

    /**
     * Gets the next lunar festival after a specified number of festivals.
     *
     * @param n The number of festivals to add.
     * @return The {@link LunarFestival} after {@code n} festivals.
     */
    public LunarFestival next(int n) {
        int size = NAMES.length;
        int i = index + n;
        return fromIndex((day.getYear() * size + i) / size, indexOf(i, size));
    }

    /**
     * Gets the type of festival.
     *
     * @return The {@link EnumValue.Festival} type.
     */
    public EnumValue.Festival getType() {
        return type;
    }

    /**
     * Gets the index of the festival.
     *
     * @return The index.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the lunar day of the festival.
     *
     * @return The {@link LunarDay} of the festival.
     */
    public LunarDay getDay() {
        return day;
    }

    /**
     * Gets the solar term associated with the festival. Returns {@code null} if it's not a solar term festival.
     *
     * @return The {@link SolarTerms} if it's a solar term festival, otherwise {@code null}.
     */
    public SolarTerms getSolarTerm() {
        return solarTerms;
    }

    /**
     * Gets the name of the festival.
     *
     * @return The name of the festival.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the string representation of this object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return String.format("%s %s", day, name);
    }

}
