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
package org.miaixz.bus.core.center.date.culture.cn.dog;

import org.miaixz.bus.core.center.date.culture.Samsara;

/**
 * 三伏
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Dog extends Samsara {

    public static final String[] NAMES = {"初伏", "中伏", "末伏"};

    public Dog(String name) {
        super(NAMES, name);
    }

    public Dog(int index) {
        super(NAMES, index);
    }

    /**
     * 从名称初始化
     *
     * @param name 名称
     * @return 伏
     */
    public static Dog fromName(String name) {
        return new Dog(name);
    }

    /**
     * 从索引初始化
     *
     * @param index 索引
     * @return 伏
     */
    public static Dog fromIndex(int index) {
        return new Dog(index);
    }

    public Dog next(int n) {
        return fromIndex(nextIndex(n));
    }

}
