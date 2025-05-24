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
package org.miaixz.bus.core.center.date.culture.rabjung;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.cn.Zodiac;
import org.miaixz.bus.core.center.date.culture.cn.sixty.SixtyCycle;
import org.miaixz.bus.core.center.date.culture.solar.SolarYear;

/**
 * 藏历年(公历1027年为藏历元年，第一饶迥火兔年）
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RabjungYear extends Loops {

    /**
     * 饶迥(胜生周)序号，从0开始
     */
    protected int rabByungIndex;

    /**
     * 干支
     */
    protected SixtyCycle sixtyCycle;

    public RabjungYear(int rabByungIndex, SixtyCycle sixtyCycle) {
        if (rabByungIndex < 0 || rabByungIndex > 150) {
            throw new IllegalArgumentException(String.format("illegal rab-byung index: %d", rabByungIndex));
        }
        this.rabByungIndex = rabByungIndex;
        this.sixtyCycle = sixtyCycle;
    }

    public static RabjungYear fromSixtyCycle(int rabByungIndex, SixtyCycle sixtyCycle) {
        return new RabjungYear(rabByungIndex, sixtyCycle);
    }

    public static RabjungYear fromElementZodiac(int rabByungIndex, RabjungElement element, Zodiac zodiac) {
        for (int i = 0; i < 60; i++) {
            SixtyCycle sixtyCycle = SixtyCycle.fromIndex(i);
            if (sixtyCycle.getEarthBranch().getZodiac().equals(zodiac)
                    && sixtyCycle.getHeavenStem().getElement().getIndex() == element.getIndex()) {
                return new RabjungYear(rabByungIndex, sixtyCycle);
            }
        }
        throw new IllegalArgumentException(String.format("illegal rab-byung element %s, zodiac %s", element, zodiac));
    }

    public static RabjungYear fromYear(int year) {
        return new RabjungYear((year - 1024) / 60, SixtyCycle.fromIndex(year - 4));
    }

    /**
     * 饶迥序号
     *
     * @return 数字，从0开始
     */
    public int getRabByungIndex() {
        return rabByungIndex;
    }

    /**
     * 干支
     *
     * @return 干支
     */
    public SixtyCycle getSixtyCycle() {
        return sixtyCycle;
    }

    /**
     * 生肖
     *
     * @return 生肖
     */
    public Zodiac getZodiac() {
        return getSixtyCycle().getEarthBranch().getZodiac();
    }

    /**
     * 五行
     *
     * @return 藏历五行
     */
    public RabjungElement getElement() {
        return RabjungElement.fromIndex(getSixtyCycle().getHeavenStem().getElement().getIndex());
    }

    /**
     * 名称
     *
     * @return 名称
     */
    public String getName() {
        String[] digits = { "零", "一", "二", "三", "四", "五", "六", "七", "八", "九" };
        String[] units = { "", "十", "百" };
        int n = rabByungIndex + 1;
        StringBuilder s = new StringBuilder();
        int pos = 0;
        while (n > 0) {
            int digit = n % 10;
            if (digit > 0) {
                s.insert(0, digits[digit] + units[pos]);
            } else if (s.length() > 0) {
                s.insert(0, digits[digit]);
            }
            n /= 10;
            pos++;
        }
        String letter = s.toString();
        if (letter.startsWith("一十")) {
            letter = letter.substring(1);
        }
        return String.format("第%s饶迥%s%s年", letter, getElement(), getZodiac());
    }

    public RabjungYear next(int n) {
        return fromYear(getYear() + n);
    }

    /**
     * 年
     *
     * @return 年
     */
    public int getYear() {
        return 1024 + rabByungIndex * 60 + getSixtyCycle().getIndex();
    }

    /**
     * 闰月
     *
     * @return 闰月数字，1代表闰1月，0代表无闰月
     */
    public int getLeapMonth() {
        int y = 1;
        int m = 4;
        int t = 0;
        int currentYear = getYear();
        while (y < currentYear) {
            int i = m - 1 + (t % 2 == 0 ? 33 : 32);
            y = (y * 12 + i) / 12;
            m = i % 12 + 1;
            t++;
        }
        return y == currentYear ? m : 0;
    }

    /**
     * 公历年
     *
     * @return 公历年
     */
    public SolarYear getSolarYear() {
        return SolarYear.fromYear(getYear());
    }

    /**
     * 首月
     *
     * @return 藏历月
     */
    public RabjungMonth getFirstMonth() {
        return new RabjungMonth(this, 1);
    }

    /**
     * 月份数量
     *
     * @return 数量
     */
    public int getMonthCount() {
        return getLeapMonth() == 0 ? 12 : 13;
    }

    /**
     * 藏历月列表
     *
     * @return 藏历月列表
     */
    public List<RabjungMonth> getMonths() {
        List<RabjungMonth> l = new ArrayList<>();
        int leapMonth = getLeapMonth();
        for (int i = 1; i <= 12; i++) {
            l.add(new RabjungMonth(this, i));
            if (i == leapMonth) {
                l.add(new RabjungMonth(this, -i));
            }
        }
        return l;
    }

}
