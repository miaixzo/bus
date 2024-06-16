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

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.lunar.LunarYear;

/**
 * 小运
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Fortune extends Loops {

    /**
     * 童限
     */
    protected ChildLimit childLimit;

    /**
     * 序号
     */
    protected int index;

    public Fortune(ChildLimit childLimit, int index) {
        this.childLimit = childLimit;
        this.index = index;
    }

    /**
     * 通过童限初始化
     *
     * @param childLimit 童限
     * @param index      序号
     * @return 大运
     */
    public static Fortune fromChildLimit(ChildLimit childLimit, int index) {
        return new Fortune(childLimit, index);
    }

    /**
     * 年龄
     *
     * @return 年龄
     */
    public int getAge() {
        return childLimit.getYearCount() + 1 + index;
    }

    /**
     * 农历年
     *
     * @return 农历年
     */
    public LunarYear getLunarYear() {
        return childLimit.getEndTime().getLunarHour().getDay().getMonth().getYear().next(index);
    }

    /**
     * 干支
     *
     * @return 干支
     */
    public SixtyCycle getSixtyCycle() {
        int n = getAge();
        return childLimit.getEightChar().getHour().next(childLimit.isForward() ? n : -n);
    }

    public String getName() {
        return getSixtyCycle().getName();
    }

    public Fortune next(int n) {
        return fromChildLimit(childLimit, index + n);
    }

}
