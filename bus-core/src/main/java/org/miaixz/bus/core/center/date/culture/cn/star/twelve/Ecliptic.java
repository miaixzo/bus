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
package org.miaixz.bus.core.center.date.culture.cn.star.twelve;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.cn.Luck;

/**
 * 黄道黑道
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Ecliptic extends Samsara {

    public static final String[] NAMES = {"黄道", "黑道"};

    public Ecliptic(int index) {
        super(NAMES, index);
    }

    public Ecliptic(String name) {
        super(NAMES, name);
    }

    public static Ecliptic fromIndex(int index) {
        return new Ecliptic(index);
    }

    public static Ecliptic fromName(String name) {
        return new Ecliptic(name);
    }

    public Ecliptic next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * 吉凶
     *
     * @return 吉凶
     */
    public Luck getLuck() {
        return Luck.fromIndex(index);
    }

}
