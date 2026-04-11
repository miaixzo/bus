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

/**
 * Builder for constructing {@link Festival} instances.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class FestivalBuilder {

    /**
     * Festival name.
     */
    protected String name;

    /**
     * Encoded festival data.
     */
    protected char[] data = { '@', '_', '_', '_', '_', '_', '0', '0', '0' };

    /**
     * Sets the festival name.
     *
     * @param name festival name
     * @return this builder
     */
    public FestivalBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the registry encoding character for the specified index.
     *
     * @param index character index
     * @return encoded character
     */
    protected char getChar(int index) {
        return FestivalRegistry.CHARS.charAt(index);
    }

    /**
     * Writes a signed numeric value into the encoded payload.
     *
     * @param index payload position
     * @param n     signed numeric value
     * @return this builder
     */
    protected FestivalBuilder setValue(int index, int n) {
        data[index] = getChar(31 + n);
        return this;
    }

    /**
     * Sets the rule content fields.
     *
     * @param type festival rule type
     * @param a    first content value
     * @param b    second content value
     * @param c    third content value
     * @return this builder
     */
    protected FestivalBuilder content(FestivalRule type, int a, int b, int c) {
        data[1] = getChar(type.getCode());
        return setValue(2, a).setValue(3, b).setValue(4, c);
    }

    /**
     * Sets the rule to a solar day.
     *
     * @param solarMonth solar month (1 to 12)
     * @param solarDay   solar day (1 to 31)
     * @param delayDays  delay days when the date does not exist, e.g. Feb 29 in non-leap years (-31 to 31)
     * @return this builder
     */
    public FestivalBuilder solarDay(int solarMonth, int solarDay, int delayDays) {
        return content(FestivalRule.SOLAR_DAY, solarMonth, solarDay, delayDays);
    }

    /**
     * Sets the rule to a lunar day.
     *
     * @param lunarMonth lunar month (-12 to -1 for leap months, 1 to 12)
     * @param lunarDay   lunar day (1 to 30)
     * @param delayDays  delay days when the date does not exist, e.g. the 30th may not exist in some months (-31 to 31)
     * @return this builder
     */
    public FestivalBuilder lunarDay(int lunarMonth, int lunarDay, int delayDays) {
        return content(FestivalRule.LUNAR_DAY, lunarMonth, lunarDay, delayDays);
    }

    /**
     * Sets the rule to a specific weekday occurrence in a solar month.
     *
     * @param solarMonth solar month (1 to 12)
     * @param weekIndex  week occurrence (1 for 1st, -1 for last, etc.)
     * @param week       day of week (0=Sunday, 1=Monday, ..., 6=Saturday)
     * @return this builder
     */
    public FestivalBuilder solarWeek(int solarMonth, int weekIndex, int week) {
        return content(FestivalRule.SOLAR_WEEK, solarMonth, weekIndex, week);
    }

    /**
     * Sets the rule to a solar term day.
     *
     * @param termIndex solar term index (0 to 23)
     * @param delayDays delay days (-31 to 31)
     * @return this builder
     */
    public FestivalBuilder termDay(int termIndex, int delayDays) {
        return content(FestivalRule.TERM_DAY, termIndex, 0, delayDays);
    }

    /**
     * Sets the rule to a solar term with heaven stem.
     *
     * @param termIndex       solar term index (0 to 23)
     * @param heavenStemIndex heaven stem index (0 to 9)
     * @param delayDays       delay days (-31 to 31)
     * @return this builder
     */
    public FestivalBuilder termHeavenStem(int termIndex, int heavenStemIndex, int delayDays) {
        return content(FestivalRule.TERM_HS, termIndex, heavenStemIndex, delayDays);
    }

    /**
     * Sets the rule to a solar term with earth branch.
     *
     * @param termIndex        solar term index (0 to 23)
     * @param earthBranchIndex earth branch index (0 to 11)
     * @param delayDays        delay days (-31 to 31)
     * @return this builder
     */
    public FestivalBuilder termEarthBranch(int termIndex, int earthBranchIndex, int delayDays) {
        return content(FestivalRule.TERM_EB, termIndex, earthBranchIndex, delayDays);
    }

    /**
     * Sets the start year of the festival.
     *
     * @param year start year
     * @return this builder
     */
    public FestivalBuilder startYear(int year) {
        int size = FestivalRegistry.CHARS.length();
        int n = year;
        for (int i = 0; i < 3; i++) {
            data[8 - i] = getChar(n % size);
            n /= size;
        }
        return this;
    }

    /**
     * Sets the day offset.
     *
     * @param days offset in days (-31 to 31)
     * @return this builder
     */
    public FestivalBuilder offset(int days) {
        return setValue(5, days);
    }

    /**
     * Builds the festival instance.
     *
     * @return festival
     */
    public Festival build() {
        return new Festival(name, new String(data));
    }

}
