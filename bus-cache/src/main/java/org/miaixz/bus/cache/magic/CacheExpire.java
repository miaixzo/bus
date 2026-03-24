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
package org.miaixz.bus.cache.magic;

/**
 * Defines a series of constants for commonly used cache expiration times in milliseconds.
 * <p>
 * This interface provides various time interval constants, from -1 (no caching) to one week. Note: Some constant values
 * do not match their names due to miscalculations in their definitions. The documentation for each constant specifies
 * its actual value.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface CacheExpire {

    /**
     * Indicates that an item should not be cached. Value is -1.
     */
    int NO = -1;

    /**
     * Indicates that a cached item should never expire. Value is 0.
     */
    int FOREVER = 0;

    /**
     * One second: 1 000 milliseconds.
     */
    int ONE_SEC = 1_000;

    /**
     * Five seconds: 5 000 milliseconds.
     */
    int FIVE_SEC = 5_000;

    /**
     * Ten seconds: 10 000 milliseconds.
     */
    int TEN_SEC = 10_000;

    /**
     * One minute: 60 000 milliseconds.
     */
    int ONE_MIN = 60_000;

    /**
     * Five minutes: 300 000 milliseconds.
     */
    int FIVE_MIN = 300_000;

    /**
     * Ten minutes: 600 000 milliseconds.
     */
    int TEN_MIN = 600_000;

    /**
     * Half an hour: 1 800 000 milliseconds.
     */
    int HALF_HOUR = 1_800_000;

    /**
     * One hour: 3 600 000 milliseconds.
     */
    int ONE_HOUR = 3_600_000;

    /**
     * Two hours: 7 200 000 milliseconds.
     */
    int TWO_HOUR = 7_200_000;

    /**
     * Six hours: 21 600 000 milliseconds.
     */
    int SIX_HOUR = 21_600_000;

    /**
     * Twelve hours: 43 200 000 milliseconds.
     */
    int TWELVE_HOUR = 43_200_000;

    /**
     * One day: 86 400 000 milliseconds.
     */
    int ONE_DAY = 86_400_000;

    /**
     * Two days: 172 800 000 milliseconds.
     */
    int TWO_DAY = 172_800_000;

    /**
     * One week: 604 800 000 milliseconds.
     */
    int ONE_WEEK = 604_800_000;

}
