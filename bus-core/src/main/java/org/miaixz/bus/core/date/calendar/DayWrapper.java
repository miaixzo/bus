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
import org.miaixz.bus.core.date.*;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.toolkit.ObjectKit;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 天/日/Day
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Data
public class DayWrapper implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * date
     */
    private Date date;

    /**
     * java8 localDateTime 丰富方法可以使用
     */
    private LocalDateTime localDateTime;

    /**
     * 日期 yyyy-MM-dd
     */
    private String dateStr;

    /**
     * 天，当月第几天
     */
    private int day;

    /**
     * 星期，数字，1-7
     */
    private int week;

    /**
     * 星期，中文简写，比如星期一为一
     */
    private String weekCnShort;

    /**
     * 星期，中文全称，比如星期一
     */
    private String weekCnLong;

    /**
     * 星期，英文简写，比如星期一为Mon
     */
    private String weekEnShort;

    /**
     * 星期，英文简写大写，比如星期一为MON
     */
    private String weekEnShortUpper;

    /**
     * 星期，英文全称，比如星期一为Monday
     */
    private String weekEnLong;

    /**
     * 公历节日
     */
    private String localHoliday;

    /**
     * 农历
     */
    private Lunar lunar;

    /**
     * 农历节日
     */
    private String chineseHoliday;

    /**
     * 农历日期
     */
    private String lunarDateStr;

    /**
     * 农历天，比如初一
     */
    private String lunarDay;

    /**
     * 二十四节气
     */
    private String solarTerm;

    /**
     * 日期类型，0休息日，1其他为工作日
     */
    private int dateType;

    /**
     * 扩展信息
     */
    private Object object;

    /**
     * 创建DayWrapper
     *
     * @param localDateTime LocalDateTime
     */
    public DayWrapper(LocalDateTime localDateTime) {
        this(localDateTime, false);
    }

    /**
     * 创建DayWrapper
     *
     * @param localDateTime    LocalDateTime
     * @param includeLunarDate 是否包含农历
     */
    public DayWrapper(LocalDateTime localDateTime, boolean includeLunarDate) {
        this(localDateTime, includeLunarDate, false);
    }

    /**
     * 创建DayWrapper
     *
     * @param localDateTime    LocalDateTime
     * @param includeLunarDate 是否包含农历
     * @param includeHoliday   是否包含节日
     */
    public DayWrapper(LocalDateTime localDateTime, boolean includeLunarDate, boolean includeHoliday) {
        this(localDateTime, null, includeLunarDate, includeHoliday);
    }

    public DayWrapper(LocalDateTime localDateTime, Object object, boolean includeLunarDate, boolean includeHoliday) {
        super();
        this.localDateTime = localDateTime;
        this.date = Converter.toDate(localDateTime);
        this.dateStr = Formatter.format(localDateTime);
        this.day = localDateTime.getDayOfMonth();
        // 星期
        this.week = localDateTime.getDayOfWeek().getValue();
        this.weekCnShort = Almanac.getDayOfWeekCnShort(localDateTime);
        this.weekCnLong = Almanac.getDayOfWeekCn(localDateTime);
        this.weekEnShort = Almanac.getDayOfWeekEnShort(localDateTime);
        this.weekEnShortUpper = Almanac.getDayOfWeekEnShortUpper(localDateTime);
        this.weekEnLong = Almanac.getDayOfWeekEnLong(localDateTime);
        this.object = object;

        // 农历
        if (includeLunarDate) {
            this.lunar = new Solar(localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth()).getLunar();
            this.lunarDateStr = lunar.toString();
            this.lunarDay = lunar.getDayInChinese();
            this.solarTerm = lunar.getSolarTerm();
        }

        // 节假日
        if (includeHoliday) {
            Holiday holiday = Holiday.getHoliday(localDateTime.getYear(),
                    localDateTime.getMonthValue(),
                    localDateTime.getDayOfMonth());
            this.localHoliday = ObjectKit.isNotNull(holiday) ? holiday.toString() : Normal.EMPTY;

            holiday = Holiday.getHoliday(localDateTime.getYear(),
                    localDateTime.getMonthValue(),
                    getDay());
            if (includeLunarDate) {
                this.chineseHoliday = ObjectKit.isNotNull(holiday) ? holiday.toString() : Normal.EMPTY;
            }
        }

        // 工作日
        this.dateType = Almanac.isWorkDay(localDateTime) ? 1 : 0;
    }

}
