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
package org.miaixz.bus.core.date;

import org.miaixz.bus.core.lang.Fields;
import org.miaixz.bus.core.toolkit.DateKit;

/**
 * 计时器
 * 计算某个过程花费的时间,精确到毫秒
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NonTimer {

    private final boolean isNano;
    private long time;

    public NonTimer() {
        this(false);
    }

    public NonTimer(boolean isNano) {
        this.isNano = isNano;
        start();
    }

    /**
     * @return 开始计时并返回当前时间
     */
    public long start() {
        time = DateKit.timestamp(isNano);
        return time;
    }

    /**
     * @return 重新计时并返回从开始到当前的持续时间
     */
    public long intervalRestart() {
        long now = DateKit.timestamp(isNano);
        long d = now - time;
        time = now;
        return d;
    }

    /**
     * 重新开始计算时间(重置开始时间)
     *
     * @return this
     */
    public NonTimer restart() {
        time = DateKit.timestamp(isNano);
        return this;
    }

    /**
     * 从开始到当前的间隔时间(毫秒数)
     * 如果使用纳秒计时,返回纳秒差,否则返回毫秒差
     *
     * @return 从开始到当前的间隔时间(毫秒数)
     */
    public long interval() {
        return DateKit.timestamp(isNano) - time;
    }

    /**
     * 从开始到当前的间隔时间(毫秒数)
     *
     * @return 从开始到当前的间隔时间(毫秒数)
     */
    public long intervalMs() {
        return isNano ? interval() / 1000000L : interval();
    }

    /**
     * 从开始到当前的间隔秒数,取绝对值
     *
     * @return 从开始到当前的间隔秒数, 取绝对值
     */
    public long intervalSecond() {
        return intervalMs() / Fields.Units.SECOND.getUnit();
    }

    /**
     * 从开始到当前的间隔分钟数,取绝对值
     *
     * @return 从开始到当前的间隔分钟数, 取绝对值
     */
    public long intervalMinute() {
        return intervalMs() / Fields.Units.MINUTE.getUnit();
    }

    /**
     * 从开始到当前的间隔小时数,取绝对值
     *
     * @return 从开始到当前的间隔小时数, 取绝对值
     */
    public long intervalHour() {
        return intervalMs() / Fields.Units.HOUR.getUnit();
    }

    /**
     * 从开始到当前的间隔天数,取绝对值
     *
     * @return 从开始到当前的间隔天数, 取绝对值
     */
    public long intervalDay() {
        return intervalMs() / Fields.Units.DAY.getUnit();
    }

    /**
     * 从开始到当前的间隔周数,取绝对值
     *
     * @return 从开始到当前的间隔周数, 取绝对值
     */
    public long intervalWeek() {
        return intervalMs() / Fields.Units.WEEK.getUnit();
    }

}
