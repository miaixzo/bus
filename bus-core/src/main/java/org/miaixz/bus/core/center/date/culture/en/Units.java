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
package org.miaixz.bus.core.center.date.culture.en;

import java.time.temporal.ChronoUnit;

/**
 * 日期时间单位，每个单位都是以毫秒为基数
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Units {
    /**
     * 一毫秒
     */
    MS(1),
    /**
     * 一秒的毫秒数
     */
    SECOND(1000),
    /**
     * 一分钟的毫秒数
     */
    MINUTE(SECOND.getMillis() * 60),
    /**
     * 一小时的毫秒数
     */
    HOUR(MINUTE.getMillis() * 60),
    /**
     * 一天的毫秒数
     */
    DAY(HOUR.getMillis() * 24),
    /**
     * 一周的毫秒数
     */
    WEEK(DAY.getMillis() * 7);

    private final long millis;

    Units(final long millis) {
        this.millis = millis;
    }

    /**
     * 单位兼容转换，将{@link ChronoUnit}转换为对应的DateUnit
     *
     * @param unit {@link ChronoUnit}
     * @return Units，null表示不支持此单位
     */
    public static Units of(final ChronoUnit unit) {
        switch (unit) {
        case MICROS:
            return Units.MS;
        case SECONDS:
            return Units.SECOND;
        case MINUTES:
            return Units.MINUTE;
        case HOURS:
            return Units.HOUR;
        case DAYS:
            return Units.DAY;
        case WEEKS:
            return Units.WEEK;
        }
        return null;
    }

    /**
     * 单位兼容转换，将DateUnit转换为对应的{@link ChronoUnit}
     *
     * @param unit Units
     * @return {@link ChronoUnit}
     */
    public static ChronoUnit toChronoUnit(final Units unit) {
        switch (unit) {
        case MS:
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

    /**
     * @return 单位对应的毫秒数
     */
    public long getMillis() {
        return this.millis;
    }

    /**
     * 单位兼容转换，将DateUnit转换为对应的{@link ChronoUnit}
     *
     * @return {@link ChronoUnit}
     */
    public ChronoUnit toChronoUnit() {
        return Units.toChronoUnit(this);
    }

}
