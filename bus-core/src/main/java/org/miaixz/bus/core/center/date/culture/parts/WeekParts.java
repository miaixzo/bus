/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.date.culture.parts;

import org.miaixz.bus.core.center.date.culture.Week;

/**
 * Abstract base class for date components containing week information.
 *
 * <p>
 * This class extends {@link MonthParts} and adds week-related fields, representing a specific week within a month. The
 * week definition can vary based on the starting weekday and the week index.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class WeekParts extends MonthParts {

    /**
     * The week index within the month (0-5).
     */
    protected int index;

    /**
     * The starting weekday (1-7 for Monday-Sunday, or 0 for Sunday).
     */
    protected int start;

    /**
     * Gets the week index.
     *
     * @return the week index (0-5)
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the starting weekday of the week.
     *
     * @return the starting weekday as a {@link Week} enum value
     */
    public Week getStart() {
        return Week.fromIndex(start);
    }

    /**
     * Validates the week components.
     *
     * @param index the week index to validate (0-5)
     * @param start the starting weekday to validate (0-6)
     * @throws IllegalArgumentException if index or start is out of valid range
     */
    public static void validate(int index, int start) {
        if (index < 0 || index > 5) {
            throw new IllegalArgumentException(String.format("illegal week index: %d", index));
        }
        if (start < 0 || start > 6) {
            throw new IllegalArgumentException(String.format("illegal week start: %d", start));
        }
    }

}
