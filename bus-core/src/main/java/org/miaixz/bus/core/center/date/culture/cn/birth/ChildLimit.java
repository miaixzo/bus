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

import org.miaixz.bus.core.center.date.culture.cn.Opposite;
import org.miaixz.bus.core.center.date.culture.cn.birth.provider.ChildLimitProvider;
import org.miaixz.bus.core.center.date.culture.cn.birth.provider.impl.DefaultChildLimitProvider;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.center.date.culture.solar.SolarTime;
import org.miaixz.bus.core.lang.Gender;

/**
 * 童限（从出生到起运的时间段）
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ChildLimit {

    /**
     * 童限计算接口
     */
    public static ChildLimitProvider provider = new DefaultChildLimitProvider();

    /**
     * 八字
     */
    protected EightChar eightChar;

    /**
     * 性别
     */
    protected Gender gender;

    /**
     * 顺逆
     */
    protected boolean forward;

    /**
     * 童限信息
     */
    protected ChildLimitInfo info;

    public ChildLimit(SolarTime birthTime, Gender gender) {
        this.gender = gender;
        eightChar = birthTime.getLunarHour().getEightChar();
        // 阳男阴女顺推，阴男阳女逆推
        boolean yang = Opposite.YANG == eightChar.getYear().getHeavenStem().getOpposite();
        boolean man = Gender.MALE == gender;
        forward = (yang && man) || (!yang && !man);
        SolarTerms term = birthTime.getTerm();
        if (!term.isJie()) {
            term = term.next(-1);
        }
        if (forward) {
            term = term.next(2);
        }
        info = provider.getInfo(birthTime, term);
    }

    /**
     * 通过出生公历时刻初始化
     *
     * @param birthTime 出生公历时刻
     * @param gender    性别
     * @return 童限
     */
    public static ChildLimit fromSolarTime(SolarTime birthTime, Gender gender) {
        return new ChildLimit(birthTime, gender);
    }

    /**
     * 八字
     *
     * @return 八字
     */
    public EightChar getEightChar() {
        return eightChar;
    }

    /**
     * 性别
     *
     * @return 性别
     */
    public Gender getGender() {
        return gender;
    }

    /**
     * 是否顺推
     *
     * @return true/false
     */
    public boolean isForward() {
        return forward;
    }

    /**
     * 年数
     *
     * @return 年数
     */
    public int getYearCount() {
        return info.getYearCount();
    }

    /**
     * 月数
     *
     * @return 月数
     */
    public int getMonthCount() {
        return info.getMonthCount();
    }

    /**
     * 日数
     *
     * @return 日数
     */
    public int getDayCount() {
        return info.getDayCount();
    }

    /**
     * 小时数
     *
     * @return 小时数
     */
    public int getHourCount() {
        return info.getHourCount();
    }

    /**
     * 分钟数
     *
     * @return 分钟数
     */
    public int getMinuteCount() {
        return info.getMinuteCount();
    }

    /**
     * 开始(即出生)的公历时刻
     *
     * @return 公历时刻
     */
    public SolarTime getStartTime() {
        return info.getStartTime();
    }

    /**
     * 结束(即开始起运)的公历时刻
     *
     * @return 公历时刻
     */
    public SolarTime getEndTime() {
        return info.getEndTime();
    }

    /**
     * 大运
     *
     * @return 大运
     */
    public DecadeFortune getStartDecadeFortune() {
        return DecadeFortune.fromChildLimit(this, 0);
    }

    /**
     * 小运
     *
     * @return 小运
     */
    public Fortune getStartFortune() {
        return Fortune.fromChildLimit(this, 0);
    }

}
