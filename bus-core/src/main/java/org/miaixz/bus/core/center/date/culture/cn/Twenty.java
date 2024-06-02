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
 * 运（20年=1运，3运=1元）
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Twenty extends Samsara {

    public static final String[] NAMES = {
            "一运", "二运", "三运", "四运", "五运", "六运", "七运", "八运", "九运"
    };

    public Twenty(int index) {
        super(NAMES, index);
    }

    public Twenty(String name) {
        super(NAMES, name);
    }

    public static Twenty fromIndex(int index) {
        return new Twenty(index);
    }

    public static Twenty fromName(String name) {
        return new Twenty(name);
    }

    public Twenty next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * 元
     *
     * @return 元
     */
    public Sixty getSixty() {
        return Sixty.fromIndex(index / 3);
    }

}
