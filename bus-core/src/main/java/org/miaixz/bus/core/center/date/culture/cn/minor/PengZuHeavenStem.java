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
package org.miaixz.bus.core.center.date.culture.cn.minor;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * 天干彭祖百忌
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PengZuHeavenStem extends Samsara {

    public static final String[] NAMES = {
            "甲不开仓财物耗散", "乙不栽植千株不长", "丙不修灶必见灾殃", "丁不剃头头必生疮",
            "戊不受田田主不祥", "己不破券二比并亡", "庚不经络织机虚张", "辛不合酱主人不尝",
            "壬不泱水更难提防", "癸不词讼理弱敌强"
    };

    public PengZuHeavenStem(String name) {
        super(NAMES, name);
    }

    public PengZuHeavenStem(int index) {
        super(NAMES, index);
    }

    /**
     * 从名称初始化
     *
     * @param name 名称
     * @return 天干彭祖百忌
     */
    public static PengZuHeavenStem fromName(String name) {
        return new PengZuHeavenStem(name);
    }

    /**
     * 从索引初始化
     *
     * @param index 索引
     * @return 天干彭祖百忌
     */
    public static PengZuHeavenStem fromIndex(int index) {
        return new PengZuHeavenStem(index);
    }

    public PengZuHeavenStem next(int n) {
        return new PengZuHeavenStem(nextIndex(n));
    }

}
