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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.lang.EnumValue;

/**
 * Represents modern Gregorian festivals.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarFestival extends Loops {

    /**
     * Names of solar festivals.
     */
    public static final String[] NAMES = { "元旦", "三八妇女节", "植树节", "五一劳动节", "五四青年节", "六一儿童节", "建党节", "八一建军节", "教师节",
            "国庆节" };

    /**
     * Data string containing festival information, including type, month, day, and start year.
     */
    public static String DATA = "@00001011950@01003081950@02003121979@03005011950@04005041950@05006011950@06007011941@07008011933@08009101985@09010011950";

    /**
     * The type of festival.
     */
    protected EnumValue.Festival type;

    /**
     * The index of the festival.
     */
    protected int index;

    /**
     * The solar day of the festival.
     */
    protected SolarDay day;

    /**
     * The name of the festival.
     */
    protected String name;

    /**
     * The start year from which this festival is observed.
     */
    protected int startYear;

    /**
     * Constructs a {@code SolarFestival} instance.
     *
     * @param type      The type of festival.
     * @param day       The solar day of the festival.
     * @param startYear The start year from which this festival is observed.
     * @param data      The raw data string for the festival.
     */
    public SolarFestival(EnumValue.Festival type, SolarDay day, int startYear, String data) {
        this.type = type;
        this.day = day;
        this.startYear = startYear;
        index = Integer.parseInt(data.substring(1, 3), 10);
        name = NAMES[index];
    }

    /**
     * Creates a {@code SolarFestival} instance from the given year and festival index.
     *
     * @param year  The year.
     * @param index The index of the festival.
     * @return A new {@link SolarFestival} instance, or {@code null} if not found or not applicable for the year.
     * @throws IllegalArgumentException if the index is out of valid range.
     */
    public static SolarFestival fromIndex(int year, int index) {
        if (index < 0 || index >= NAMES.length) {
            throw new IllegalArgumentException(String.format("illegal index: %d", index));
        }
        Matcher matcher = Pattern.compile(String.format("@%02d\\d+", index)).matcher(DATA);
        if (!matcher.find()) {
            return null;
        }
        String data = matcher.group();
        EnumValue.Festival type = EnumValue.Festival.fromCode(data.charAt(3) - '0');
        if (type != EnumValue.Festival.DAY) {
            return null;
        }
        int startYear = Integer.parseInt(data.substring(8), 10);
        return year < startYear ? null
                : new SolarFestival(type,
                        SolarDay.fromYmd(
                                year,
                                Integer.parseInt(data.substring(4, 6), 10),
                                Integer.parseInt(data.substring(6, 8), 10)),
                        startYear, data);
    }

    /**
     * Creates a {@code SolarFestival} instance from the given year, month, and day.
     *
     * @param year  The year.
     * @param month The month.
     * @param day   The day.
     * @return A new {@link SolarFestival} instance, or {@code null} if no festival matches or not applicable for the
     *         year.
     */
    public static SolarFestival fromYmd(int year, int month, int day) {
        Matcher matcher = Pattern.compile(String.format("@\\d{2}0%02d%02d\\d+", month, day)).matcher(DATA);
        if (!matcher.find()) {
            return null;
        }
        String data = matcher.group();
        int startYear = Integer.parseInt(data.substring(8), 10);
        return year < startYear ? null
                : new SolarFestival(EnumValue.Festival.DAY, SolarDay.fromYmd(year, month, day), startYear, data);
    }

    /**
     * Gets the next solar festival after a specified number of festivals.
     *
     * @param n The number of festivals to add.
     * @return The {@link SolarFestival} after {@code n} festivals.
     */
    public SolarFestival next(int n) {
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
     * Gets the solar day of the festival.
     *
     * @return The {@link SolarDay} of the festival.
     */
    public SolarDay getDay() {
        return day;
    }

    /**
     * Gets the start year from which this festival is observed.
     *
     * @return The start year.
     */
    public int getStartYear() {
        return startYear;
    }

    /**
     * Gets the name of the festival.
     *
     * @return The name of the festival.
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("%s %s", day, name);
    }

}
