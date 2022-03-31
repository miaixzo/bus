/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org Greg Messner and other contributors.         *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.gitlab.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * This class represents a duration in time.
 */
public class Duration {

    private int seconds;
    private String durationString;

    /**
     * Create a Duration instance from a human readable string. e.g: 3h30m
     *
     * @param durationString a duration in human readable format
     */
    public Duration(String durationString) {
        seconds = org.aoju.bus.gitlab.support.Duration.parse(durationString);
        this.durationString = (seconds == 0 ? "0m" : org.aoju.bus.gitlab.support.Duration.toString(seconds));
    }

    /**
     * Create a Duration instance from a number of seconds.
     *
     * @param seconds the number of seconds for this Duration instance to represent
     */
    public Duration(int seconds) {
        this.seconds = seconds;
        durationString = (seconds == 0 ? "0m" : org.aoju.bus.gitlab.support.Duration.toString(seconds));
    }

    @JsonCreator
    public static Duration forValue(String value) {
        return new Duration(value);
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
}
