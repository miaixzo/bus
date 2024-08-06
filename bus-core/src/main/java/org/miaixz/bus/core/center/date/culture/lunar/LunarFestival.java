/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.date.culture.lunar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.center.date.culture.solar.SolarTerms;
import org.miaixz.bus.core.lang.EnumMap;

/**
 * 农历传统节日（依据国家标准《农历的编算和颁行》GB/T 33661-2017）
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LunarFestival extends Loops {

    public static final String[] NAMES = { "春节", "元宵节", "龙头节", "上巳节", "清明节", "端午节", "七夕节", "中元节", "中秋节", "重阳节", "冬至节",
            "腊八节", "除夕" };

    public static String DATA = "@0000101@0100115@0200202@0300303@04107@0500505@0600707@0700715@0800815@0900909@10124@1101208@122";

    /**
     * 类型
     */
    protected EnumMap.Festival type;

    /**
     * 索引
     */
    protected int index;

    /**
     * 农历日
     */
    protected LunarDay day;

    /**
     * 节气
     */
    protected SolarTerms solarTerms;

    /**
     * 名称
     */
    protected String name;

    public LunarFestival(EnumMap.Festival type, LunarDay day, SolarTerms solarTerms, String data) {
        this.type = type;
        this.day = day;
        this.solarTerms = solarTerms;
        index = Integer.parseInt(data.substring(1, 3), 10);
        name = NAMES[index];
    }

    public static LunarFestival fromIndex(int year, int index) {
        if (index < 0 || index >= NAMES.length) {
            throw new IllegalArgumentException(String.format("illegal index: %d", index));
        }
        Matcher matcher = Pattern.compile(String.format("@%02d\\d+", index)).matcher(DATA);
        if (!matcher.find()) {
            return null;
        }
        String data = matcher.group();
        EnumMap.Festival type = EnumMap.Festival.fromCode(data.charAt(3) - '0');
        switch (type) {
        case DAY:
            return new LunarFestival(type, LunarDay.fromYmd(year, Integer.parseInt(data.substring(4, 6), 10),
                    Integer.parseInt(data.substring(6), 10)), null, data);
        case TERM:
            SolarTerms solarTerm = SolarTerms.fromIndex(year, Integer.parseInt(data.substring(4), 10));
            return new LunarFestival(type, solarTerm.getJulianDay().getSolarDay().getLunarDay(), solarTerm, data);
        case EVE:
            return new LunarFestival(type, LunarDay.fromYmd(year + 1, 1, 1).next(-1), null, data);
        default:
            return null;
        }
    }

    public static LunarFestival fromYmd(int year, int month, int day) {
        Matcher matcher = Pattern.compile(String.format("@\\d{2}0%02d%02d", month, day)).matcher(DATA);
        if (matcher.find()) {
            return new LunarFestival(EnumMap.Festival.DAY, LunarDay.fromYmd(year, month, day), null, matcher.group());
        }
        matcher = Pattern.compile("@\\d{2}1\\d{2}").matcher(DATA);
        while (matcher.find()) {
            String data = matcher.group();
            SolarTerms solarTerms = SolarTerms.fromIndex(year, Integer.parseInt(data.substring(4), 10));
            LunarDay lunarDay = solarTerms.getJulianDay().getSolarDay().getLunarDay();
            if (lunarDay.getYear() == year && lunarDay.getMonth() == month && lunarDay.getDay() == day) {
                return new LunarFestival(EnumMap.Festival.TERM, lunarDay, solarTerms, data);
            }
        }
        matcher = Pattern.compile("@\\d{2}2").matcher(DATA);
        if (!matcher.find()) {
            return null;
        }
        LunarDay lunarDay = LunarDay.fromYmd(year, month, day);
        LunarDay nextDay = lunarDay.next(1);
        return nextDay.getMonth() == 1 && nextDay.getDay() == 1
                ? new LunarFestival(EnumMap.Festival.EVE, lunarDay, null, matcher.group())
                : null;
    }

    public LunarFestival next(int n) {
        if (n == 0) {
            return fromYmd(day.getYear(), day.getMonth(), day.getDay());
        }
        int size = NAMES.length;
        int t = index + n;
        int offset = indexOf(t, size);
        if (t < 0) {
            t -= size;
        }
        return fromIndex(day.getYear() + t / size, offset);
    }

    /**
     * 类型
     *
     * @return 节日类型
     */
    public EnumMap.Festival getType() {
        return type;
    }

    /**
     * 索引
     *
     * @return 索引
     */
    public int getIndex() {
        return index;
    }

    /**
     * 农历日
     *
     * @return 农历日
     */
    public LunarDay getDay() {
        return day;
    }

    /**
     * 节气，非节气返回null
     *
     * @return 节气
     */
    public SolarTerms getSolarTerm() {
        return solarTerms;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("%s %s", day, name);
    }

}
