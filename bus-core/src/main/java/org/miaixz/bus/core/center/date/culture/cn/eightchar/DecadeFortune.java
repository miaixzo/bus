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
package org.miaixz.bus.core.center.date.culture.cn.eightchar;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.lunar.LunarYear;

/**
 * 大运（10年1大运）
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DecadeFortune extends Loops {

    /**
     * 童限
     */
    protected ChildLimit childLimit;

    /**
     * 序号
     */
    protected int index;

    public DecadeFortune(ChildLimit childLimit, int index) {
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
    public static DecadeFortune fromChildLimit(ChildLimit childLimit, int index) {
        return new DecadeFortune(childLimit, index);
    }

    /**
     * 开始年龄
     *
     * @return 开始年龄
     */
    public int getStartAge() {
        return childLimit.getEndTime().getYear() - childLimit.getStartTime().getYear() + 1 + index * 10;
    }

    /**
     * 结束年龄
     *
     * @return 结束年龄
     */
    public int getEndAge() {
        return getStartAge() + 9;
    }

    /**
     * 开始农历年
     *
     * @return 农历年
     */
    public LunarYear getStartLunarYear() {
        return childLimit.getEndLunarYear().next(index * 10);
    }

    /**
     * 结束农历年
     *
     * @return 农历年
     */
    public LunarYear getEndLunarYear() {
        return getStartLunarYear().next(9);
    }

    /**
     * 干支
     *
     * @return 干支
     */
    public SixtyCycle getSixtyCycle() {
        int n = index + 1;
        return childLimit.getEightChar().getMonth().next(childLimit.isForward() ? n : -n);
    }

    public String getName() {
        return getSixtyCycle().getName();
    }

    public DecadeFortune next(int n) {
        return fromChildLimit(childLimit, index + n);
    }

    /**
     * 开始小运
     *
     * @return 小运
     */
    public Fortune getStartFortune() {
        return Fortune.fromChildLimit(childLimit, index * 10);
    }

}
