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
package org.miaixz.bus.core.center.date.culture.solar;

import org.miaixz.bus.core.center.date.culture.Loops;
import org.miaixz.bus.core.lang.EnumMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 公历现代节日
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SolarFestival extends Loops {

    public static final String[] NAMES = {
            "元旦", "三八妇女节", "植树节", "五一劳动节", "五四青年节",
            "六一儿童节", "建党节", "八一建军节", "教师节", "国庆节"
    };

    public static String DATA = "@00001011950@01003081950@02003121979@03005011950@04005041950@05006011950@06007011941@07008011933@08009101985@09010011950";

    /**
     * 类型
     */
    protected EnumMap.Festival type;

    /**
     * 索引
     */
    protected int index;

    /**
     * 公历日
     */
    protected SolarDay day;

    /**
     * 名称
     */
    protected String name;

    /**
     * 起始年
     */
    protected int startYear;

    public SolarFestival(EnumMap.Festival type, SolarDay day, int startYear, String data) {
        this.type = type;
        this.day = day;
        this.startYear = startYear;
        index = Integer.parseInt(data.substring(1, 3), 10);
        name = NAMES[index];
    }

    public static SolarFestival fromIndex(int year, int index) {
        if (index < 0 || index >= NAMES.length) {
            throw new IllegalArgumentException(String.format("illegal index: %d", index));
        }
        Matcher matcher = Pattern.compile(String.format("@%02d\\d+", index)).matcher(DATA);
        if (matcher.find()) {
            String data = matcher.group();
            EnumMap.Festival type = EnumMap.Festival.fromCode(data.charAt(3) - '0');
            if (type == EnumMap.Festival.DAY) {
                int startYear = Integer.parseInt(data.substring(8), 10);
                if (year >= startYear) {
                    return new SolarFestival(type, SolarDay.fromYmd(year, Integer.parseInt(data.substring(4, 6), 10), Integer.parseInt(data.substring(6, 8), 10)), startYear, data);
                }
            }
        }
        return null;
    }

    public static SolarFestival fromYmd(int year, int month, int day) {
        Matcher matcher = Pattern.compile(String.format("@\\d{2}0%02d%02d\\d+", month, day)).matcher(DATA);
        if (matcher.find()) {
            String data = matcher.group();
            int startYear = Integer.parseInt(data.substring(8), 10);
            if (year >= startYear) {
                return new SolarFestival(EnumMap.Festival.DAY, SolarDay.fromYmd(year, month, day), startYear, data);
            }
        }
        return null;
    }

    public SolarFestival next(int n) {
        SolarMonth m = day.getMonth();
        int year = m.getYear().getYear();
        if (n == 0) {
            return fromYmd(year, m.getMonth(), day.getDay());
        }
        int size = NAMES.length;
        int t = this.index + n;
        int offset = indexOf(t, size);
        if (t < 0) {
            t -= size;
        }
        return fromIndex(year + t / size, offset);
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
     * 公历日
     *
     * @return 公历日
     */
    public SolarDay getDay() {
        return day;
    }

    /**
     * 起始年
     *
     * @return 年
     */
    public int getStartYear() {
        return startYear;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("%s %s", day, name);
    }

}
