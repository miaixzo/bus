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
package org.miaixz.bus.core.center.date;

import org.miaixz.bus.core.center.date.culture.Modify;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.DateKit;

import java.util.Calendar;

/**
 * 日期修改器
 * 用于实现自定义某个日期字段的调整，包括：
 *
 * <pre>
 * 1. 获取指定字段的起始时间
 * 2. 获取指定字段的四舍五入时间
 * 3. 获取指定字段的结束时间
 * </pre>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Modifier {

    /**
     * 忽略的计算的字段
     */
    private static final int[] IGNORE_FIELDS = new int[]{
            Calendar.HOUR_OF_DAY, // 与HOUR同名
            Calendar.AM_PM, // 此字段单独处理，不参与计算起始和结束
            Calendar.DAY_OF_WEEK_IN_MONTH, // 不参与计算
            Calendar.DAY_OF_YEAR, // DAY_OF_MONTH体现
            Calendar.WEEK_OF_MONTH, // 特殊处理
            Calendar.WEEK_OF_YEAR // WEEK_OF_MONTH体现
    };

    /**
     * 修改日期
     *
     * @param calendar  {@link Calendar}
     * @param dateField 日期字段，即保留到哪个日期字段
     * @param modify    修改类型，包括舍去、四舍五入、进一等
     * @return 修改后的{@link Calendar}
     */
    public static Calendar modify(final Calendar calendar, final int dateField, final Modify modify) {
        return modify(calendar, dateField, modify, false);
    }

    /**
     * 修改日期，取起始值或者结束值
     * 可选是否归零毫秒
     *
     * <p>
     * 在{@link Modify#TRUNCATE}模式下，毫秒始终要归零,
     * 但是在{@link Modify#CEILING}和{@link Modify#ROUND}模式下，
     * 有时候由于毫秒部分必须为0（如MySQL数据库中），因此在此加上选项。
     * </p>
     *
     * @param calendar            {@link Calendar}
     * @param dateField           日期字段，即保留到哪个日期字段
     * @param modify              修改类型，包括舍去、四舍五入、进一等
     * @param truncateMillisecond 是否归零毫秒
     * @return 修改后的{@link Calendar}
     */
    public static Calendar modify(final Calendar calendar, final int dateField, final Modify modify, final boolean truncateMillisecond) {
        // AM_PM上下午特殊处理
        if (Calendar.AM_PM == dateField) {
            final boolean isAM = DateKit.isAM(calendar);
            switch (modify) {
                case TRUNCATE:
                    calendar.set(Calendar.HOUR_OF_DAY, isAM ? 0 : 12);
                    break;
                case CEILING:
                    calendar.set(Calendar.HOUR_OF_DAY, isAM ? 11 : 23);
                    break;
                case ROUND:
                    final int min = isAM ? 0 : 12;
                    final int max = isAM ? 11 : 23;
                    final int href = (max - min) / 2 + 1;
                    final int value = calendar.get(Calendar.HOUR_OF_DAY);
                    calendar.set(Calendar.HOUR_OF_DAY, (value < href) ? min : max);
                    break;
            }
            // 处理下一级别字段
            return modify(calendar, dateField + 1, modify);
        }

        final int endField = truncateMillisecond ? Calendar.SECOND : Calendar.MILLISECOND;
        // 循环处理各级字段，精确到毫秒字段
        for (int i = dateField + 1; i <= endField; i++) {
            if (ArrayKit.contains(IGNORE_FIELDS, i)) {
                // 忽略无关字段（WEEK_OF_MONTH）始终不做修改
                continue;
            }

            // 在计算本周的起始和结束日时，月相关的字段忽略。
            if (Calendar.WEEK_OF_MONTH == dateField || Calendar.WEEK_OF_YEAR == dateField) {
                if (Calendar.DAY_OF_MONTH == i) {
                    continue;
                }
            } else {
                // 其它情况忽略周相关字段计算
                if (Calendar.DAY_OF_WEEK == i) {
                    continue;
                }
            }

            modifyField(calendar, i, modify);
        }

        if (truncateMillisecond) {
            calendar.set(Calendar.MILLISECOND, 0);
        }

        return calendar;
    }

    /**
     * 修改日期字段值
     *
     * @param calendar {@link Calendar}
     * @param field    字段，见{@link Calendar}
     * @param modify   {@link Modify}
     */
    private static void modifyField(final Calendar calendar, int field, final Modify modify) {
        if (Calendar.HOUR == field) {
            // 修正小时。HOUR为12小时制，上午的结束时间为12:00，此处改为HOUR_OF_DAY: 23:59
            field = Calendar.HOUR_OF_DAY;
        }

        switch (modify) {
            case TRUNCATE:
                calendar.set(field, Calendars.getBeginValue(calendar, field));
                break;
            case CEILING:
                calendar.set(field, Calendars.getEndValue(calendar, field));
                break;
            case ROUND:
                final int min = Calendars.getBeginValue(calendar, field);
                final int max = Calendars.getEndValue(calendar, field);
                final int href;
                if (Calendar.DAY_OF_WEEK == field) {
                    // 星期特殊处理，假设周一是第一天，中间的为周四
                    href = (min + 3) % 7;
                } else {
                    href = (max - min) / 2 + 1;
                }
                final int value = calendar.get(field);
                calendar.set(field, (value < href) ? min : max);
                break;
        }
    }

}
