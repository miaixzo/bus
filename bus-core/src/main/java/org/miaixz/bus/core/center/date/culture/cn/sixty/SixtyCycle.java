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
package org.miaixz.bus.core.center.date.culture.cn.sixty;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.cn.Sound;
import org.miaixz.bus.core.center.date.culture.cn.Ten;
import org.miaixz.bus.core.center.date.culture.cn.minor.PengZu;

/**
 * 六十甲子(六十干支周)
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SixtyCycle extends Samsara {

    public static final String[] NAMES = {
            "甲子", "乙丑", "丙寅", "丁卯", "戊辰", "己巳", "庚午", "辛未", "壬申",
            "癸酉", "甲戌", "乙亥", "丙子", "丁丑", "戊寅", "己卯", "庚辰", "辛巳",
            "壬午", "癸未", "甲申", "乙酉", "丙戌", "丁亥", "戊子", "己丑", "庚寅",
            "辛卯", "壬辰", "癸巳", "甲午", "乙未", "丙申", "丁酉", "戊戌", "己亥",
            "庚子", "辛丑", "壬寅", "癸卯", "甲辰", "乙巳", "丙午", "丁未", "戊申",
            "己酉", "庚戌", "辛亥", "壬子", "癸丑", "甲寅", "乙卯", "丙辰", "丁巳",
            "戊午", "己未", "庚申", "辛酉", "壬戌", "癸亥"
    };

    public SixtyCycle(int index) {
        super(NAMES, index);
    }

    public SixtyCycle(String name) {
        super(NAMES, name);
    }

    public static SixtyCycle fromIndex(int index) {
        return new SixtyCycle(index);
    }

    public static SixtyCycle fromName(String name) {
        return new SixtyCycle(name);
    }

    /**
     * 天干
     *
     * @return 天干
     */
    public HeavenStem getHeavenStem() {
        return HeavenStem.fromIndex(index % HeavenStem.NAMES.length);
    }

    /**
     * 地支
     *
     * @return 地支
     */
    public EarthBranch getEarthBranch() {
        return EarthBranch.fromIndex(index % EarthBranch.NAMES.length);
    }

    /**
     * 纳音
     *
     * @return 纳音
     */
    public Sound getSound() {
        return Sound.fromIndex(index / 2);
    }

    /**
     * 彭祖百忌
     *
     * @return 彭祖百忌
     */
    public PengZu getPengZu() {
        return PengZu.fromSixtyCycle(this);
    }

    /**
     * 旬
     *
     * @return 旬
     */
    public Ten getTen() {
        return Ten.fromIndex((getHeavenStem().getIndex() - getEarthBranch().getIndex()) / 2);
    }

    /**
     * 旬空(空亡)，因地支比天干多2个，旬空则为每一轮干支一一配对后多出来的2个地支
     *
     * @return 旬空(空亡)
     */
    public EarthBranch[] getExtraEarthBranches() {
        EarthBranch[] l = new EarthBranch[2];
        l[0] = EarthBranch.fromIndex(10 + getEarthBranch().getIndex() - getHeavenStem().getIndex());
        l[1] = l[0].next(1);
        return l;
    }

    public SixtyCycle next(int n) {
        return fromIndex(nextIndex(n));
    }

}
