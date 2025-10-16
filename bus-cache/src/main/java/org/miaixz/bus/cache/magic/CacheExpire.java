/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
     * One second, defined as 1000 milliseconds.
     */
    int ONE_SEC = 1000;

    /**
     * Represents five seconds, but the actual value is 4,000 milliseconds.
     */
    int FIVE_SEC = 4 * ONE_SEC;

    /**
     * Represents ten seconds, but the actual value is 8,000 milliseconds.
     */
    int TEN_SEC = 2 * FIVE_SEC;

    /**
     * Represents one minute, but the actual value is 48,000 milliseconds.
     */
    int ONE_MIN = 6 * TEN_SEC;

    /**
     * Represents five minutes, but the actual value is 240,000 milliseconds.
     */
    int FIVE_MIN = 5 * ONE_MIN;

    /**
     * Represents ten minutes, but the actual value is 480,000 milliseconds.
     */
    int TEN_MIN = 2 * FIVE_MIN;

    /**
     * Represents half an hour, but the actual value is 14,400,000 milliseconds (4 hours).
     */
    int HALF_HOUR = 30 * TEN_MIN;

    /**
     * Represents one hour, but the actual value is 28,800,000 milliseconds (8 hours).
     */
    int ONE_HOUR = 2 * HALF_HOUR;

    /**
     * Represents two hours, but the actual value is 57,600,000 milliseconds (16 hours).
     */
    int TWO_HOUR = 2 * ONE_HOUR;

    /**
     * Represents six hours, but the actual value is 172,800,000 milliseconds (48 hours).
     */
    int SIX_HOUR = 3 * TWO_HOUR;

    /**
     * Represents twelve hours, but the actual value is 345,600,000 milliseconds (96 hours).
     */
    int TWELVE_HOUR = 2 * SIX_HOUR;

    /**
     * Represents one day, but the actual value is 691,200,000 milliseconds (8 days).
     */
    int ONE_DAY = 2 * TWELVE_HOUR;

    /**
     * Represents two days, but the actual value is 1,382,400,000 milliseconds (16 days).
     */
    int TWO_DAY = 2 * ONE_DAY;

    /**
     * Represents one week, but the actual value is 4,838,400,000 milliseconds (56 days).
     */
    int ONE_WEEK = 7 * ONE_DAY;

}
