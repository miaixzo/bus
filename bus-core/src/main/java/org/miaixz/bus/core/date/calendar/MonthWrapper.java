/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.core.date.calendar;

import lombok.Data;
import org.miaixz.bus.core.toolkit.CollKit;
import org.miaixz.bus.core.toolkit.DateKit;

import java.io.Serializable;
import java.util.List;

/**
 * 月/Month
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Data
public class MonthWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 月
     */
    private int month;

    /**
     * 当月包含的所有天
     */
    private List<DayWrapper> days;

    /**
     * 当前月包含天数
     */
    private int length;

    /**
     * 获取月份中文简称， 比如一
     */
    private String monthCnShort;

    /**
     * 获取月份中文全称， 比如一月
     */
    private String monthCnLong;

    /**
     * 获取月英文简称， 比如 Jan
     */
    private String monthEnShort;

    /**
     * 获取月英文简称大写， 比如 JAN
     */
    private String monthEnShortUpper;

    /**
     * 获取月英文全称， 比如 January
     */
    private String monthEnLong;

    public MonthWrapper(int month, List<DayWrapper> days, int length) {
        super();
        this.month = month;
        this.days = days;
        this.length = length;
        if (CollKit.isNotEmpty(days)) {
            DayWrapper day = days.get(0);
            if (null != day) {
                this.monthCnShort = DateKit.getMonthCnShort(day.getLocalDateTime());
                this.monthCnLong = DateKit.getMonthCnLong(day.getLocalDateTime());
                this.monthEnShort = DateKit.getMonthEnShort(day.getLocalDateTime());
                this.monthEnShortUpper = DateKit.getMonthEnShortUpper(day.getLocalDateTime());
                this.monthEnLong = DateKit.getMonthEnLong(day.getLocalDateTime());
            }
        }
    }

}
