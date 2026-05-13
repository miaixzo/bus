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
package org.miaixz.bus.image.builtin.hp;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Relative time range for prior selection.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class RelativeTime {

    /**
     * The values value.
     */
    private final int[] values;

    /**
     * The units value.
     */
    private final Units units;

    /**
     * Creates a new instance.
     */
    public RelativeTime() {
        this(0, 0, Units.SECONDS);
    }

    /**
     * Creates a new instance.
     *
     * @param start the start.
     * @param end   the end.
     * @param units the units.
     */
    public RelativeTime(int start, int end, Units units) {
        this(new int[] { start, end }, units);
    }

    /**
     * Creates a new instance.
     *
     * @param values the values.
     * @param units  the units.
     */
    public RelativeTime(int[] values, Units units) {
        this.values = Objects.requireNonNullElse(values, new int[2]).clone();
        if (this.values.length != 2) {
            throw new IllegalArgumentException("values must have a length of 2");
        }
        this.units = Objects.requireNonNullElse(units, Units.SECONDS);
    }

    /**
     * Gets the start.
     *
     * @return the start.
     */
    public int getStart() {
        return values[0];
    }

    /**
     * Gets the end.
     *
     * @return the end.
     */
    public int getEnd() {
        return values[1];
    }

    /**
     * Gets the values.
     *
     * @return the values.
     */
    public int[] getValues() {
        return values.clone();
    }

    /**
     * Gets the start date.
     *
     * @return the start date.
     */
    public LocalDateTime getStartDate() {
        return toDate(values[0], Clock.systemDefaultZone());
    }

    /**
     * Gets the end date.
     *
     * @return the end date.
     */
    public LocalDateTime getEndDate() {
        return toDate(values[1], Clock.systemDefaultZone());
    }

    /**
     * Gets the start date.
     *
     * @param clock the clock.
     * @return the start date.
     */
    public LocalDateTime getStartDate(Clock clock) {
        return toDate(values[0], clock);
    }

    /**
     * Gets the end date.
     *
     * @param clock the clock.
     * @return the end date.
     */
    public LocalDateTime getEndDate(Clock clock) {
        return toDate(values[1], clock);
    }

    /**
     * Determines whether current time.
     *
     * @return true if the condition is met; otherwise false.
     */
    public boolean isCurrentTime() {
        return values[0] == 0 && values[1] == 0;
    }

    /**
     * Gets the units.
     *
     * @return the units.
     */
    public Units getUnits() {
        return units;
    }

    /**
     * Converts this value to date.
     *
     * @param value the value.
     * @param clock the clock.
     * @return the operation result.
     */
    private LocalDateTime toDate(int value, Clock clock) {
        return LocalDateTime.now(clock).minus(value, units.getChronoUnit());
    }

    /**
     * Defines the Units values.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum Units {

        /**
         * Constant for the seconds value.
         */
        SECONDS("SECONDS", ChronoUnit.SECONDS),
        /**
         * Constant for the minutes value.
         */
        MINUTES("MINUTES", ChronoUnit.MINUTES),
        /**
         * Constant for the hours value.
         */
        HOURS("HOURS", ChronoUnit.HOURS),
        /**
         * Constant for the days value.
         */
        DAYS("DAYS", ChronoUnit.DAYS),
        /**
         * Constant for the weeks value.
         */
        WEEKS("WEEKS", ChronoUnit.WEEKS),
        /**
         * Constant for the months value.
         */
        MONTHS("MONTHS", ChronoUnit.MONTHS),
        /**
         * Constant for the years value.
         */
        YEARS("YEARS", ChronoUnit.YEARS);

        /**
         * The code string value.
         */
        private final String codeString;

        /**
         * The chrono unit value.
         */
        private final ChronoUnit chronoUnit;

        /**
         * Creates a new instance.
         *
         * @param codeString the code string.
         * @param chronoUnit the chrono unit.
         */
        Units(String codeString, ChronoUnit chronoUnit) {
            this.codeString = codeString;
            this.chronoUnit = chronoUnit;
        }

        /**
         * Gets the code string.
         *
         * @return the code string.
         */
        public String getCodeString() {
            return codeString;
        }

        /**
         * Gets the chrono unit.
         *
         * @return the chrono unit.
         */
        public ChronoUnit getChronoUnit() {
            return chronoUnit;
        }

    }

}
