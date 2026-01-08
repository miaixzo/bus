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
package org.miaixz.bus.core.center.date.culture.solar;

import org.miaixz.bus.core.center.date.Galaxy;
import org.miaixz.bus.core.center.date.culture.JulianDay;
import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * Represents the 24 Solar Terms (Jieqi) in the Chinese calendar.
 * <p>
 * The 24 Solar Terms are a traditional Chinese calendar system that divides the solar year into 24 periods, each
 * approximately 15 days long. They are based on the position of the sun in the zodiac and are used to guide
 * agricultural activities and predict weather patterns.
 * </p>
 * <p>
 * Each solar term is either a "Jie" (节) or "Qi" (气):
 * <ul>
 * <li>Jie (odd indices): Marks the beginning of a zodiac month</li>
 * <li>Qi (even indices): Marks the middle of a zodiac month</li>
 * </ul>
 * <p>
 * The 24 solar terms in order are: Winter Solstice, Minor Cold, Major Cold, Spring Commences, Rain Water, Awakening of
 * Insects, Spring Equinox, Pure Brightness, Grain Rain, Summer Commences, Grain Full, Grain in Ear, Summer Solstice,
 * Minor Heat, Major Heat, Autumn Commences, Limit of Heat, White Dew, Autumn Equinox, Cold Dew, Frost Descent, Winter
 * Commences, Minor Snow, Major Snow.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarTerms extends Samsara {

    /**
     * Chinese names of the 24 solar terms.
     */
    public static final String[] NAMES = { "冬至", "小寒", "大寒", "立春", "雨水", "惊蛰", "春分", "清明", "谷雨", "立夏", "小满", "芒种", "夏至",
            "小暑", "大暑", "立秋", "处暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪" };

    /**
     * The year of this solar term.
     */
    protected int year;

    /**
     * The cursory Julian Day (for calendar purposes, accurate only to noon).
     */
    protected double cursoryJulianDay;

    /**
     * Constructs a SolarTerm from year and index.
     *
     * @param year  the year
     * @param index the index of the solar term (0-23)
     */
    public SolarTerms(int year, int index) {
        super(NAMES, index);
        int size = getSize();
        initByYear((year * size + index) / size, getIndex());
    }

    /**
     * Constructs a SolarTerm from year and name.
     *
     * @param year the year
     * @param name the Chinese name of the solar term
     */
    public SolarTerms(int year, String name) {
        super(NAMES, name);
        initByYear(year, index);
    }

    /**
     * Creates a SolarTerm from year and index.
     *
     * @param year  the year
     * @param index the solar term index (0-23)
     * @return a new SolarTerm instance
     */
    public static SolarTerms fromIndex(int year, int index) {
        return new SolarTerms(year, index);
    }

    /**
     * Creates a SolarTerm from year and name.
     *
     * @param year the year
     * @param name the Chinese name of the solar term
     * @return a new SolarTerm instance
     */
    public static SolarTerms fromName(int year, String name) {
        return new SolarTerms(year, name);
    }

    /**
     * Gets the next or previous solar term.
     *
     * @param n the number of solar terms to move (positive for forward, negative for backward)
     * @return the SolarTerm n terms from this one
     */
    public SolarTerms next(int n) {
        int size = getSize();
        int i = index + n;
        return fromIndex((year * size + i) / size, indexOf(i));
    }

    /**
     * Initializes this solar term for a given year and offset. Uses astronomical calculations to determine the precise
     * time of the solar term.
     *
     * @param year   the year
     * @param offset the offset (0-23) representing which solar term
     */
    protected void initByYear(int year, int offset) {
        double jd = Math.floor((year - 2000) * 365.2422 + 180);
        // 355 is the Winter Solstice of 2000, used to get an estimated value close to jd
        double w = Math.floor((jd - 355 + 183) / 365.2422) * 365.2422 + 355;
        if (Galaxy.calcQi(w) > jd) {
            w -= 365.2422;
        }
        this.year = year;
        cursoryJulianDay = Galaxy.calcQi(w + 15.2184 * offset);
    }

    /**
     * Checks if this is a Jie (节) term. Jie terms have odd indices and mark the beginning of zodiac months.
     *
     * @return true if this is a Jie term
     */
    public boolean isJie() {
        return index % 2 == 1;
    }

    /**
     * Checks if this is a Qi (气) term. Qi terms have even indices and mark the middle of zodiac months.
     *
     * @return true if this is a Qi term
     */
    public boolean isQi() {
        return index % 2 == 0;
    }

    /**
     * Gets the Julian Day for this solar term (accurate to the second).
     *
     * @return the JulianDay
     */
    public JulianDay getJulianDay() {
        return JulianDay.fromJulianDay(Galaxy.qiAccurate2(cursoryJulianDay) + JulianDay.J2000);
    }

    /**
     * Gets the solar day for this solar term (for calendar purposes).
     *
     * @return the SolarDay
     */
    public SolarDay getSolarDay() {
        return JulianDay.fromJulianDay(cursoryJulianDay + JulianDay.J2000).getSolarDay();
    }

    /**
     * Gets the year of this solar term.
     *
     * @return the year
     */
    public int getYear() {
        return year;
    }

    /**
     * Gets the cursory Julian Day (for calendar purposes, accurate only to noon).
     *
     * @return the cursory Julian Day number
     */
    public double getCursoryJulianDay() {
        return cursoryJulianDay;
    }

}
