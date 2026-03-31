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
package org.miaixz.bus.core.center.date.culture;

import java.util.Calendar;
import java.util.Date;

import org.miaixz.bus.core.center.date.culture.sixty.EarthBranch;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.DateKit;

/**
 * Represents the Chinese Zodiac (生肖) animals. This class extends {@link Samsara} to manage a cyclical list of these
 * entities.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Zodiac extends Samsara {

    /**
     * Array of names for the Chinese Zodiac animals.
     */
    public static final String[] NAMES = { "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪" };

    /**
     * Constructs a {@code Zodiac} instance with the specified index.
     *
     * @param index The index of the Zodiac animal in the {@link #NAMES} array.
     */
    public Zodiac(int index) {
        super(NAMES, index);
    }

    /**
     * Constructs a {@code Zodiac} instance with the specified name.
     *
     * @param name The name of the Zodiac animal.
     */
    public Zodiac(String name) {
        super(NAMES, name);
    }

    /**
     * Creates a {@code Zodiac} instance from its index.
     *
     * @param index The index of the Zodiac animal.
     * @return A new {@code Zodiac} instance.
     */
    public static Zodiac fromIndex(int index) {
        return new Zodiac(index);
    }

    /**
     * Creates a {@code Zodiac} instance from its name.
     *
     * @param name The name of the Zodiac animal.
     * @return A new {@code Zodiac} instance.
     */
    public static Zodiac fromName(String name) {
        return new Zodiac(name);
    }

    /**
     * Calculates the Chinese Zodiac animal based on the birth date. This method is designed for birth dates after 1900
     * (Gregorian calendar).
     *
     * @param date The birth date (Gregorian year is used for calculation).
     * @return The name of the Zodiac animal.
     */
    public static String getName(final Date date) {
        return getName(DateKit.calendar(date));
    }

    /**
     * Calculates the Chinese Zodiac animal based on the birth date. This method is designed for birth dates after 1900
     * (Gregorian calendar).
     *
     * @param calendar The birth date (Gregorian year is used for calculation).
     * @return The name of the Zodiac animal.
     */
    public static String getName(final Calendar calendar) {
        if (null == calendar) {
            return null;
        }
        return getName(calendar.get(Calendar.YEAR));
    }

    /**
     * Calculates the Chinese Zodiac animal based on the Gregorian year.
     *
     * @param year The Gregorian year.
     * @return The name of the Zodiac animal.
     */
    public static String getName(final int year) {
        return NAMES[year % Normal._12];
    }

    /**
     * Gets the next {@code Zodiac} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Zodiac} instance.
     */
    public Zodiac next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * Gets the corresponding {@link EarthBranch} for this zodiac animal.
     *
     * @return The {@link EarthBranch} associated with this Zodiac animal.
     */
    public EarthBranch getEarthBranch() {
        return EarthBranch.fromIndex(index);
    }

}
