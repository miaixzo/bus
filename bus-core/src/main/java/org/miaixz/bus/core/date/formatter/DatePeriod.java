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
package org.miaixz.bus.core.date.formatter;

import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.toolkit.StringKit;

/**
 * 时长格式化器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DatePeriod {

    /**
     * 计算单位最大个数
     */
    private final int maxCount;
    /**
     * 时长毫秒数
     */
    private long betweenMs;
    /**
     * 计算单位
     */
    private Fields.Units unit;

    /**
     * 构造
     *
     * @param betweenMs 日期间隔
     * @param units     级别,按照天、小时、分、秒、毫秒分为5个等级,根据传入等级,格式化到相应级别
     */
    public DatePeriod(long betweenMs, Fields.Units units) {
        this(betweenMs, units, 0);
    }

    /**
     * 构造
     *
     * @param betweenMs 日期间隔
     * @param unit      级别,按照天、小时、分、秒、毫秒分为5个等级,根据传入等级,格式化到相应级别
     * @param maxCount  格式化级别的最大个数,假如级别个数为1,但是级别到秒,那只显示一个级别
     */
    public DatePeriod(long betweenMs, Fields.Units unit, int maxCount) {
        this.betweenMs = betweenMs;
        this.unit = unit;
        this.maxCount = maxCount;
    }

    /**
     * 格式化日期间隔输出
     *
     * @return 格式化后的字符串
     */
    public String format() {
        final StringBuilder sb = new StringBuilder();
        if (betweenMs > 0) {
            long day = betweenMs / Fields.Units.DAY.getUnit();
            long hour = betweenMs / Fields.Units.HOUR.getUnit() - day * 24;
            long minute = betweenMs / Fields.Units.MINUTE.getUnit() - day * 24 * 60 - hour * 60;
            long second = betweenMs / Fields.Units.SECOND.getUnit() - ((day * 24 + hour) * 60 + minute) * 60;
            long millisecond = betweenMs - (((day * 24 + hour) * 60 + minute) * 60 + second) * 1000;

            final int level = this.unit.ordinal();
            int unitCount = 0;

            if (isUnitCountValid(unitCount) && 0 != day && level >= Fields.Units.DAY.ordinal()) {
                sb.append(day).append(Fields.Units.DAY.getName());
                unitCount++;
            }
            if (isUnitCountValid(unitCount) && 0 != hour && level >= Fields.Units.HOUR.ordinal()) {
                sb.append(hour).append(Fields.Units.HOUR.getName());
                unitCount++;
            }
            if (isUnitCountValid(unitCount) && 0 != minute && level >= Fields.Units.MINUTE.ordinal()) {
                sb.append(minute).append(Fields.Units.MINUTE.getName());
                unitCount++;
            }
            if (isUnitCountValid(unitCount) && 0 != second && level >= Fields.Units.SECOND.ordinal()) {
                sb.append(second).append(Fields.Units.SECOND.getName());
                unitCount++;
            }
            if (isUnitCountValid(unitCount) && 0 != millisecond && level >= Fields.Units.MILLISECOND.ordinal()) {
                sb.append(millisecond).append(Fields.Units.MILLISECOND.getName());
                unitCount++;
            }
        }

        if (StringKit.isEmpty(sb)) {
            sb.append(0).append(this.unit.getName());
        }

        return sb.toString();
    }

    /**
     * 获得 时长毫秒数
     *
     * @return 时长毫秒数
     */
    public long getBetweenMs() {
        return betweenMs;
    }

    /**
     * 设置 时长毫秒数
     *
     * @param betweenMs 时长毫秒数
     */
    public void setBetweenMs(long betweenMs) {
        this.betweenMs = betweenMs;
    }

    /**
     * 获得 格式化单位
     *
     * @return 格式化级别
     */
    public Fields.Units getUnit() {
        return this.unit;
    }

    /**
     * 设置格式化单位
     *
     * @param unit 格式化单位
     */
    public void setUnit(Fields.Units unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return format();
    }

    /**
     * 等级数量是否有效
     * 有效的定义是：maxCount大于0(被设置),当前等级数量没有超过这个最大值
     *
     * @param unitCount 登记数量
     * @return 是否有效
     */
    private boolean isUnitCountValid(int unitCount) {
        return this.maxCount <= 0 || unitCount < this.maxCount;
    }

}
