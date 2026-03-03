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
package org.miaixz.bus.gitlab.models;

import java.io.Serial;

import java.io.Serial;

import java.io.Serial;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * This class represents a duration in time.
 */
public class Duration implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852252100196L;

    private static final String[] TIME_UNITS = { "mo", "w", "d", "h", "m", "s" };
    private static final int[] TIME_UNIT_MULTIPLIERS = { 60 * 60 * 8 * 5 * 4, // 4 weeks = 1 month
            60 * 60 * 8 * 5, // 5 days = 1 week
            60 * 60 * 8, // 8 hours = 1 day
            60 * 60, // 60 minutes = 1 hours
            60, // 60 seconds = 1 minute
            1 };
    private static Pattern durationPattern = Pattern.compile("(\\s*(\\d+)(mo|[wdhms]))");

    private int seconds;
    private String durationString;

    /**
     * Create a Duration instance from a human readable string. e.g: 3h30m
     *
     * @param durationString a duration in human readable format
     */
    public Duration(String durationString) {
        seconds = parse(durationString);
        this.durationString = (seconds == 0 ? "0m" : toString(seconds, true));
    }

    /**
     * Create a Duration instance from a number of seconds.
     *
     * @param seconds the number of seconds for this Duration instance to represent
     */
    public Duration(int seconds) {
        this.seconds = seconds;
        durationString = (seconds == 0 ? "0m" : toString(seconds, true));
    }

    /**
     * Get the number of seconds this duration represents.
     *
     * @return the number of seconds this duration represents
     */
    public int getSeconds() {
        return (seconds);
    }

    /**
     * Set the number of seconds this duration represents.
     *
     * @param seconds the number of seconds this duration represents
     */
    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    @JsonValue
    @Override
    public String toString() {
        return (durationString);
    }

    @JsonCreator
    public static Duration forValue(String value) {
        return new Duration(value);
    }

    /**
     * Create a human readable duration string from seconds.
     *
     * @param durationSeconds the total number of seconds in the duration
     * @param includeMonths   when true will include months "mo", in the string otherwise uses "4w" for each month
     * @return a human readable string representing the duration
     */
    public static final String toString(int durationSeconds, boolean includeMonths) {

        int seconds = durationSeconds;
        int months = (includeMonths ? seconds / TIME_UNIT_MULTIPLIERS[0] : 0);
        seconds -= months * TIME_UNIT_MULTIPLIERS[0];
        int weeks = seconds / TIME_UNIT_MULTIPLIERS[1];
        seconds -= weeks * TIME_UNIT_MULTIPLIERS[1];
        int days = seconds / TIME_UNIT_MULTIPLIERS[2];
        seconds -= days * TIME_UNIT_MULTIPLIERS[2];
        int hours = seconds / 3600;
        seconds -= hours * 3600;
        int minutes = seconds / 60;
        seconds -= minutes * 60;

        StringBuilder buf = new StringBuilder();
        if (months > 0) {

            buf.append(months).append("mo");
            if (weeks > 0) {
                buf.append(weeks).append('w');
            }

            if (seconds > 0) {
                buf.append(days).append('d').append(hours).append('h').append(minutes).append('m').append(seconds)
                        .append('s');
            } else if (minutes > 0) {
                buf.append(days).append('d').append(hours).append('h').append(minutes).append('m');
            } else if (hours > 0) {
                buf.append(days).append('d').append(hours).append('h');
            } else if (days > 0) {
                buf.append(days).append('d');
            }

        } else if (weeks > 0) {

            buf.append(weeks).append('w');
            if (seconds > 0) {
                buf.append(days).append('d').append(hours).append('h').append(minutes).append('m').append(seconds)
                        .append('s');
            } else if (minutes > 0) {
                buf.append(days).append('d').append(hours).append('h').append(minutes).append('m');
            } else if (hours > 0) {
                buf.append(days).append('d').append(hours).append('h');
            } else if (days > 0) {
                buf.append(days).append('d');
            }

        } else if (days > 0) {

            buf.append(days).append('d');
            if (seconds > 0) {
                buf.append(hours).append('h').append(minutes).append('m').append(seconds).append('s');
            } else if (minutes > 0) {
                buf.append(hours).append('h').append(minutes).append('m');
            } else if (hours > 0) {
                buf.append(hours).append('h');
            }

        } else if (hours > 0) {

            buf.append(hours).append('h');
            if (seconds > 0) {
                buf.append(minutes).append('m').append(seconds).append('s');
            } else if (minutes > 0) {
                buf.append(minutes).append('m');
            }

        } else if (minutes > 0) {

            buf.append(minutes).append('m');
            if (seconds > 0) {
                buf.append(seconds).append('s');
            }

        } else {
            buf.append(seconds).append('s');
        }

        return (buf.toString());
    }

    /**
     * Parses a human readable duration string and calculates the number of seconds it represents.
     *
     * @param durationString the human readable duration
     * @return the total number of seconds in the duration
     */
    public static final int parse(String durationString) {

        durationString = durationString.toLowerCase();
        Matcher matcher = durationPattern.matcher(durationString);

        int currentUnitIndex = -1;
        int seconds = 0;
        Boolean validDuration = null;

        while (matcher.find() && validDuration != Boolean.FALSE) {

            validDuration = true;

            int numGroups = matcher.groupCount();
            if (numGroups == 3) {

                String unit = matcher.group(3);
                int nextUnitIndex = getUnitIndex(unit);
                if (nextUnitIndex > currentUnitIndex) {

                    currentUnitIndex = nextUnitIndex;
                    try {
                        seconds += Long.parseLong(matcher.group(2)) * TIME_UNIT_MULTIPLIERS[nextUnitIndex];
                    } catch (NumberFormatException nfe) {
                        validDuration = false;
                    }
                } else {
                    validDuration = false;
                }

            } else {
                validDuration = false;
            }
        }

        if (validDuration != Boolean.TRUE) {
            throw new IllegalArgumentException(String.format("'%s' is not a valid duration", durationString));
        }

        return (seconds);
    }

    private static int getUnitIndex(String unit) {

        for (int i = 0; i < TIME_UNITS.length; i++) {
            if (unit.equals(TIME_UNITS[i]))
                return (i);
        }

        return (-1);
    }

}
