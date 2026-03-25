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
 * Defines the supported festival rule types.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum FestivalRule {

    /**
     * Solar calendar day (fixed month and day).
     */
    SOLAR_DAY(0, "公历日期"),

    /**
     * N-th weekday occurrence in a solar month.
     */
    SOLAR_WEEK(1, "几月第几个星期几"),

    /**
     * Lunar calendar day (fixed lunar month and day).
     */
    LUNAR_DAY(2, "农历日期"),

    /**
     * Solar term day (one of the 24 solar terms).
     */
    TERM_DAY(3, "节气日期"),

    /**
     * Solar term with heaven stem (Tian Gan).
     */
    TERM_HS(4, "节气天干"),

    /**
     * Solar term with earth branch (Di Zhi).
     */
    TERM_EB(5, "节气地支");

    /**
     * Numeric code of the rule.
     */
    private final int code;

    /**
     * Display name of the rule.
     */
    private final String name;

    FestivalRule(int code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Gets the rule type for the given numeric code.
     *
     * @param code numeric rule code
     * @return matching rule type, or {@code null} if not found
     */
    public static FestivalRule fromCode(Integer code) {
        if (null == code) {
            return null;
        }
        for (FestivalRule item : values()) {
            if (item.getCode() == code) {
                return item;
            }
        }
        return null;
    }

    /**
     * Gets the rule type for the given display name.
     *
     * @param name display name
     * @return matching rule type, or {@code null} if not found
     */
    public static FestivalRule fromName(String name) {
        if (null == name) {
            return null;
        }
        for (FestivalRule item : values()) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    /**
     * Gets the numeric code of the rule.
     *
     * @return numeric code
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets the display name of the rule.
     *
     * @return display name
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

}
