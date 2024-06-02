/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org 6tail and other contributors.              *
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
package org.miaixz.bus.core.center.date.culture.solar;

import org.miaixz.bus.core.center.date.culture.Galaxy;
import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.cn.JulianDay;

/**
 * 节气
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarTerm extends Samsara {

    public static final String[] NAMES = {
            "冬至", "小寒", "大寒", "立春", "雨水", "惊蛰", "春分", "清明",
            "谷雨", "立夏", "小满", "芒种", "夏至", "小暑", "大暑", "立秋",
            "处暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪"
    };

    /**
     * 粗略的儒略日
     */
    protected double cursoryJulianDay;

    public SolarTerm(int year, int index) {
        super(NAMES, index);
        initByYear(year, index);
    }

    public SolarTerm(int year, String name) {
        super(NAMES, name);
        initByYear(year, index);
    }

    public SolarTerm(double cursoryJulianDay, int index) {
        super(NAMES, index);
        this.cursoryJulianDay = cursoryJulianDay;
    }

    public static void main(String[] args) {
        // 冬至在去年，2022-12-22 05:48:11
        SolarTerm dongZhi = SolarTerm.fromName(3001, "冬至");
        System.out.println(dongZhi.getJulianDay().getSolarTime());
    }

    public static SolarTerm fromIndex(int year, int index) {
        return new SolarTerm(year, index);
    }

    public static SolarTerm fromName(int year, String name) {
        return new SolarTerm(year, name);
    }

    protected void initByYear(int year, int offset) {
        double jd = Math.floor((year - 2000) * 365.2422 + 180);
        // 355是2000.12冬至，得到较靠近jd的冬至估计值
        double w = Math.floor((jd - 355 + 183) / 365.2422) * 365.2422 + 355;
        if (Galaxy.calcQi(w) > jd) {
            w -= 365.2422;
        }
        cursoryJulianDay = Galaxy.calcQi(w + 15.2184 * offset);
    }

    public SolarTerm next(int n) {
        return new SolarTerm(cursoryJulianDay + 15.2184 * n, nextIndex(n));
    }

    /**
     * 是否节
     *
     * @return true/false
     */
    public boolean isJie() {
        return index % 2 == 1;
    }

    /**
     * 是否气
     *
     * @return true/false
     */
    public boolean isQi() {
        return index % 2 == 0;
    }

    /**
     * 儒略日
     *
     * @return 儒略日
     */
    public JulianDay getJulianDay() {
        return JulianDay.fromJulianDay(Galaxy.qiAccurate2(cursoryJulianDay) + JulianDay.J2000);
    }

    /**
     * 粗略的儒略日
     *
     * @return 儒略日数
     */
    public double getCursoryJulianDay() {
        return cursoryJulianDay;
    }

}
