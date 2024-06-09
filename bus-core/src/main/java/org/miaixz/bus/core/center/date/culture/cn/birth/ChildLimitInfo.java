/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org 6tail and other contributors.              ~
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
package org.miaixz.bus.core.center.date.culture.cn.birth;

import org.miaixz.bus.core.center.date.culture.solar.SolarTime;

/**
 * 童限信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ChildLimitInfo {

    /**
     * 开始(即出生)的公历时刻
     */
    protected SolarTime startTime;

    /**
     * 结束(即开始起运)的公历时刻
     */
    protected SolarTime endTime;

    /**
     * 年数
     */
    protected int yearCount;

    /**
     * 月数
     */
    protected int monthCount;

    /**
     * 日数
     */
    protected int dayCount;

    /**
     * 小时数
     */
    protected int hourCount;

    /**
     * 分钟数
     */
    protected int minuteCount;

    public ChildLimitInfo(SolarTime startTime, SolarTime endTime, int yearCount, int monthCount, int dayCount, int hourCount, int minuteCount) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.yearCount = yearCount;
        this.monthCount = monthCount;
        this.dayCount = dayCount;
        this.hourCount = hourCount;
        this.minuteCount = minuteCount;
    }

    public SolarTime getStartTime() {
        return startTime;
    }

    public SolarTime getEndTime() {
        return endTime;
    }

    public int getYearCount() {
        return yearCount;
    }

    public int getMonthCount() {
        return monthCount;
    }

    public int getDayCount() {
        return dayCount;
    }

    public int getHourCount() {
        return hourCount;
    }

    public int getMinuteCount() {
        return minuteCount;
    }

}
