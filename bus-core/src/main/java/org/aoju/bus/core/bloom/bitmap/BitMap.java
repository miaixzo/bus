/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.bloom.bitmap;

import org.aoju.bus.core.lang.Normal;

/**
 * BitMap接口，用于将某个int或long值映射到一个数组中，从而判定某个值是否存在
 *
 * @author Kimi Liu
 * @version 6.3.5
 * @since Java 17+
 */
public interface BitMap {

    /**
     * 长度32
     */
    int MACHINE32 = Normal._32;
    /**
     * 长度64
     */
    int MACHINE64 = Normal._64;

    /**
     * 加入值
     *
     * @param i 值
     */
    void add(long i);

    /**
     * 检查是否包含值
     *
     * @param i 值
     * @return 是否包含
     */
    boolean contains(long i);

    /**
     * 移除值
     *
     * @param i 值
     */
    void remove(long i);

}