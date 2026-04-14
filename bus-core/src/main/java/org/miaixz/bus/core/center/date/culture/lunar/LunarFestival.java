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

import org.miaixz.bus.core.center.date.culture.festival.AbstractFestival;
import org.miaixz.bus.core.center.date.culture.festival.Festival;
import org.miaixz.bus.core.center.date.culture.festival.FestivalRegistry;
import org.miaixz.bus.core.center.date.culture.solar.SolarTermDay;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;

/**
 * Represents traditional lunar festivals (based on the national standard "Compilation and Promulgation of the Lunar
 * Calendar" GB/T 33661-2017).
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class LunarFestival extends AbstractFestival {

    /**
     * Names of lunar festivals.
     */
    public static final String[] NAMES = { "春节", "元宵节", "龙头节", "上巳节", "清明节", "端午节", "七夕节", "中元节", "中秋节", "重阳节", "冬至节",
            "腊八节", "除夕" };

    /**
     * Encoded lunar festival data.
     *
     * @see FestivalRegistry#DATA
     */
    public static String DATA = "2VV__0002Vj__0002WW__0002XX__0003b___0002ZZ__0002bb__0002bj__0002cj__0002dd__0003s___0002gc__0002hV_U000";

    /**
     * Constructs a lunar festival instance.
     *
     * @param index festival index within {@link #NAMES}
     * @param event festival definition
     * @param day   matched lunar day
     */
    public LunarFestival(int index, Festival event, LunarDay day) {
        super(index, event, day);
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
        int start = index * 8;
        Festival e = new Festival(NAMES[index], "@" + DATA.substring(start, start + 8));
        switch (e.getType()) {
            case LUNAR_DAY:
                int[] m = e.getMonth(year);
                LunarDay d = LunarDay.fromYmd(m[0], m[1], e.getValue(3));
                int offset = e.getValue(5);
                return new LunarFestival(index, e, 0 == offset ? d : d.next(offset));

            case TERM_DAY:
                return new LunarFestival(index, e,
                        SolarTerms.fromIndex(year, e.getValue(2)).getSolarDay().getLunarDay());

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
        LunarDay d = LunarDay.fromYmd(year, month, day);
        for (int i = 0, j = LunarFestival.NAMES.length; i < j; i++) {
            int start = i * 8;
            Festival e = new Festival(LunarFestival.NAMES[i], '@' + LunarFestival.DATA.substring(start, start + 8));
            switch (e.getType()) {
                case LUNAR_DAY:
                    int offset = e.getValue(5);
                    if (0 == offset) {
                        if (d.getMonth() == e.getValue(2) && d.getDay() == e.getValue(3)) {
                            return new LunarFestival(i, e, d);
                        }
                    } else {
                        int[] m = e.getMonth(d.getYear());
                        LunarDay next = d.next(-offset);
                        if (next.getYear() == m[0] && next.getMonth() == m[1] && next.getDay() == e.getValue(3)) {
                            return new LunarFestival(i, e, d);
                        }
                    }
                    break;

                case TERM_DAY:
                    SolarTermDay term = d.getSolarDay().getTermDay();
                    if (term.getDayIndex() == 0 && term.getSolarTerm().getIndex() == e.getValue(2) % 24) {
                        return new LunarFestival(i, e, d);
                    }
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
     * Gets the lunar day associated with this festival.
     *
     * @return lunar day
     */
    public LunarDay getDay() {
        return (LunarDay) super.getDay();
    }

    /**
     * Gets the related solar term when this festival falls exactly on a solar-term day.
     *
     * @return solar term, or {@code null} if this day is not a solar-term day
     */
    public SolarTerms getSolarTerm() {
        SolarTermDay t = getDay().getSolarDay().getTermDay();
        return t.getDayIndex() == 0 ? t.getSolarTerm() : null;
    }

}
