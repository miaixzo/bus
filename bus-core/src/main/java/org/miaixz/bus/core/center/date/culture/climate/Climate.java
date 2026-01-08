/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.date.culture.climate;

import org.miaixz.bus.core.center.date.Galaxy;
import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.JulianDay;

/**
 * Represents a "Hou" (候) or Pentad, a five-day period in the Chinese traditional calendar, often associated with
 * natural phenomena. This class extends {@link Samsara} to manage a cyclical list of these entities.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Climate extends Samsara {

    /**
     * Array of names for the 72 Pentads (五日一候).
     */
    public static final String[] NAMES = { "蚯蚓结", "麋角解", "水泉动", "雁北乡", "鹊始巢", "雉始雊", "鸡始乳", "征鸟厉疾", "水泽腹坚", "东风解冻",
            "蛰虫始振", "鱼陟负冰", "獭祭鱼", "候雁北", "草木萌动", "桃始华", "仓庚鸣", "鹰化为鸠", "玄鸟至", "雷乃发声", "始电", "桐始华", "田鼠化为鴽", "虹始见",
            "萍始生", "鸣鸠拂其羽", "戴胜降于桑", "蝼蝈鸣", "蚯蚓出", "王瓜生", "苦菜秀", "靡草死", "麦秋至", "螳螂生", "鵙始鸣", "反舌无声", "鹿角解", "蜩始鸣",
            "半夏生", "温风至", "蟋蟀居壁", "鹰始挚", "腐草为萤", "土润溽暑", "大雨行时", "凉风至", "白露降", "寒蝉鸣", "鹰乃祭鸟", "天地始肃", "禾乃登", "鸿雁来",
            "玄鸟归", "群鸟养羞", "雷始收声", "蛰虫坯户", "水始涸", "鸿雁来宾", "雀入大水为蛤", "菊有黄花", "豺乃祭兽", "草木黄落", "蛰虫咸俯", "水始冰", "地始冻",
            "雉入大水为蜃", "虹藏不见", "天气上升地气下降", "闭塞而成冬", "鹖鴠不鸣", "虎始交", "荔挺出" };

    /**
     * The Gregorian year to which this Climate belongs.
     */
    protected int year;

    /**
     * Constructs a {@code Climate} instance with the specified year and name.
     *
     * @param year The Gregorian year.
     * @param name The name of the Pentad.
     */
    public Climate(int year, String name) {
        super(NAMES, name);
        this.year = year;
    }

    /**
     * Constructs a {@code Climate} instance with the specified year and index.
     *
     * @param year  The Gregorian year.
     * @param index The index of the Pentad in the {@link #NAMES} array.
     */
    public Climate(int year, int index) {
        super(NAMES, index);
        int size = getSize();
        this.year = (year * size + index) / size;
    }

    /**
     * Creates a {@code Climate} instance from its year and name.
     *
     * @param year The Gregorian year.
     * @param name The name of the Pentad.
     * @return A new {@code Climate} instance.
     */
    public static Climate fromName(int year, String name) {
        return new Climate(year, name);
    }

    /**
     * Creates a {@code Climate} instance from its year and index.
     *
     * @param year  The Gregorian year.
     * @param index The index of the Pentad.
     * @return A new {@code Climate} instance.
     */
    public static Climate fromIndex(int year, int index) {
        return new Climate(year, index);
    }

    /**
     * Gets the Three Climates (三候) to which this Pentad belongs.
     *
     * @return The {@link ThreeClimate} instance.
     */
    public ThreeClimate getThree() {
        return ThreeClimate.fromIndex(index % 3);
    }

    /**
     * Gets the next {@code Climate} in the cycle.
     *
     * @param n The number of steps to move forward or backward in the cycle.
     * @return The next {@code Climate} instance.
     */
    public Climate next(int n) {
        int size = getSize();
        int i = index + n;
        return fromIndex((year * size + i) / size, indexOf(i));
    }

    /**
     * Gets the Julian Day (儒略日) for this Climate.
     *
     * @return The {@link JulianDay} instance.
     */
    public JulianDay getJulianDay() {
        double t = Galaxy.saLonT((year - 2000 + (index - 18) * 5.0 / 360 + 1) * 2 * Math.PI);
        return JulianDay.fromJulianDay(t * 36525 + JulianDay.J2000 + 8.0 / 24 - Galaxy.dtT(t * 36525));
    }

    /**
     * Gets the Gregorian year of this Climate.
     *
     * @return The Gregorian year.
     */
    public int getYear() {
        return year;
    }

}
