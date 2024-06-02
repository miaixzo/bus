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
package org.miaixz.bus.core.center.date.culture.cn;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * 纳音
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Sound extends Samsara {

    public static final String[] NAMES = {
            "海中金", "炉中火", "大林木", "路旁土", "剑锋金", "山头火",
            "涧下水", "城头土", "白蜡金", "杨柳木", "泉中水", "屋上土",
            "霹雳火", "松柏木", "长流水", "沙中金", "山下火", "平地木",
            "壁上土", "金箔金", "覆灯火", "天河水", "大驿土", "钗钏金",
            "桑柘木", "大溪水", "沙中土", "天上火", "石榴木", "大海水"
    };

    public Sound(int index) {
        super(NAMES, index);
    }

    public Sound(String name) {
        super(NAMES, name);
    }

    public static Sound fromIndex(int index) {
        return new Sound(index);
    }

    public static Sound fromName(String name) {
        return new Sound(name);
    }

    public Sound next(int n) {
        return fromIndex(nextIndex(n));
    }

}
