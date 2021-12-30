/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.toolkit;

import org.aoju.bus.core.bloom.BitMapBloomFilter;
import org.aoju.bus.core.bloom.BitSetBloomFilter;

/**
 * 布隆过滤器工具
 *
 * @author Kimi Liu
 * @version 6.3.3
 * @since JDK 1.8+
 */
public class BloomKit {

    /**
     * 创建一个BitSet实现的布隆过滤器，过滤器的容量为c * n 个bit
     *
     * @param c 当前过滤器预先开辟的最大包含记录,通常要比预计存入的记录多一倍
     * @param n 当前过滤器预计所要包含的记录
     * @param k 哈希函数的个数，等同每条记录要占用的bit数
     * @return the object
     */
    public static BitSetBloomFilter createBitSet(int c, int n, int k) {
        return new BitSetBloomFilter(c, n, k);
    }

    /**
     * 创建BitMap实现的布隆过滤器
     *
     * @param m BitMap的大小
     * @return the object
     */
    public static BitMapBloomFilter createBitMap(int m) {
        return new BitMapBloomFilter(m);
    }

}
