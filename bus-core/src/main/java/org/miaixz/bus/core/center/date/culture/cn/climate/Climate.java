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
package org.miaixz.bus.core.center.date.culture.cn.climate;

import org.miaixz.bus.core.center.date.culture.Galaxy;
import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.cn.JulianDay;

/**
 * 候
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Climate extends Samsara {

    public static final String[] NAMES = { "蚯蚓结", "麋角解", "水泉动", "雁北乡", "鹊始巢", "雉始雊", "鸡始乳", "征鸟厉疾", "水泽腹坚", "东风解冻",
            "蛰虫始振", "鱼陟负冰", "獭祭鱼", "候雁北", "草木萌动", "桃始华", "仓庚鸣", "鹰化为鸠", "玄鸟至", "雷乃发声", "始电", "桐始华", "田鼠化为鴽", "虹始见",
            "萍始生", "鸣鸠拂其羽", "戴胜降于桑", "蝼蝈鸣", "蚯蚓出", "王瓜生", "苦菜秀", "靡草死", "麦秋至", "螳螂生", "鵙始鸣", "反舌无声", "鹿角解", "蜩始鸣",
            "半夏生", "温风至", "蟋蟀居壁", "鹰始挚", "腐草为萤", "土润溽暑", "大雨行时", "凉风至", "白露降", "寒蝉鸣", "鹰乃祭鸟", "天地始肃", "禾乃登", "鸿雁来",
            "玄鸟归", "群鸟养羞", "雷始收声", "蛰虫坯户", "水始涸", "鸿雁来宾", "雀入大水为蛤", "菊有黄花", "豺乃祭兽", "草木黄落", "蛰虫咸俯", "水始冰", "地始冻",
            "雉入大水为蜃", "虹藏不见", "天气上升地气下降", "闭塞而成冬", "鹖鴠不鸣", "虎始交", "荔挺出" };

    /**
     * 年
     */
    protected int year;

    public Climate(int year, String name) {
        super(NAMES, name);
        initByYear(year, index);
    }

    public Climate(int year, int index) {
        super(NAMES, index);
        int size = getSize();
        initByYear((year * size + index) / size, getIndex());
    }

    protected void initByYear(int year, int offset) {
        this.year = year;
        this.index = offset;
    }

    /**
     * 从名称初始化
     *
     * @param year 年
     * @param name 名称
     * @return 候
     */
    public static Climate fromName(int year, String name) {
        return new Climate(year, name);
    }

    /**
     * 从索引初始化
     *
     * @param year  年
     * @param index 索引
     * @return 候
     */
    public static Climate fromIndex(int year, int index) {
        return new Climate(year, index);
    }

    /**
     * 三候
     *
     * @return 三候
     */
    public ThreeClimate getThree() {
        return ThreeClimate.fromIndex(index % 3);
    }

    public Climate next(int n) {
        int size = getSize();
        int i = index + n;
        return fromIndex((year * size + i) / size, indexOf(i));
    }

    /**
     * 儒略日
     *
     * @return 儒略日
     */
    public JulianDay getJulianDay() {
        double t = Galaxy.saLonT((year - 2000 + (index - 18) * 5.0 / 360 + 1) * 2 * Math.PI);
        return JulianDay.fromJulianDay(t * 36525 + JulianDay.J2000 + 8.0 / 24 - Galaxy.dtT(t * 36525));
    }

    /**
     * 年
     *
     * @return 年
     */
    public int getYear() {
        return year;
    }

}
