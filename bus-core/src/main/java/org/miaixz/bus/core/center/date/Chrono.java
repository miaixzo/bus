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
package org.miaixz.bus.core.center.date;

import java.time.temporal.ChronoUnit;

/**
 * Date and time units, each unit is based on milliseconds.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Chrono {

    /**
     * One millisecond.
     */
    MILLISECOND(1, "毫秒"),
    /**
     * Number of milliseconds in one second.
     */
    SECOND(1000, "秒"),
    /**
     * Number of milliseconds in one minute.
     */
    MINUTE(SECOND.getMillis() * 60, "分"),
    /**
     * Number of milliseconds in one hour.
     */
    HOUR(MINUTE.getMillis() * 60, "小时"),
    /**
     * Number of milliseconds in one day.
     */
    DAY(HOUR.getMillis() * 24, "天"),
    /**
     * Number of milliseconds in one week.
     */
    WEEK(DAY.getMillis() * 7, "周");

    /**
     * The number of milliseconds for this unit.
     */
    private final long millis;

    /**
     * The name of the level.
     */
    private final String name;

    /**
     * Constructor for Chrono enum.
     *
     * @param millis The number of milliseconds for this unit.
     */
    Chrono(final long millis, final String name) {
        this.millis = millis;
        this.name = name;
    }

    /**
     * Gets the number of milliseconds corresponding to this unit.
     *
     * @return The number of milliseconds.
     */
    public long getMillis() {
        return this.millis;
    }

    /**
     * Gets the name of the level.
     *
     * @return The name of the level.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Converts {@link ChronoUnit} to the corresponding {@link Chrono}.
     *
     * @param chrono The {@link ChronoUnit} to convert.
     * @return The corresponding {@link Chrono}, or {@code null} if the unit is not supported.
     */
    public static Chrono of(final ChronoUnit chrono) {
        switch (chrono) {
            case MICROS:
                return Chrono.MILLISECOND;

            case SECONDS:
                return Chrono.SECOND;

            case MINUTES:
                return Chrono.MINUTE;

            case HOURS:
                return Chrono.HOUR;

            case DAYS:
                return Chrono.DAY;

            case WEEKS:
                return Chrono.WEEK;
        }
        return null;
    }

    /**
     * Converts this {@link Chrono} to the corresponding {@link ChronoUnit}.
     *
     * @return The corresponding {@link ChronoUnit}.
     */
    public ChronoUnit of() {
        return Chrono.of(this);
    }

    /**
     * Converts {@link Chrono} to the corresponding {@link ChronoUnit}.
     *
     * @param chrono The {@link Chrono} to convert.
     * @return The corresponding {@link ChronoUnit}.
     */
    public static ChronoUnit of(final Chrono chrono) {
        switch (chrono) {
            case MILLISECOND:
                return ChronoUnit.MICROS;

            case SECOND:
                return ChronoUnit.SECONDS;

            case MINUTE:
                return ChronoUnit.MINUTES;

            case HOUR:
                return ChronoUnit.HOURS;

            case DAY:
                return ChronoUnit.DAYS;

            case WEEK:
                return ChronoUnit.WEEKS;
        }
        return null;
    }

}
