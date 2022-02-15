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
package org.aoju.bus.core.bloom;

import org.aoju.bus.core.bloom.filter.*;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.toolkit.MathKit;

/**
 * BlommFilter 实现
 * 1.构建hash算法
 * 2.散列hash映射到数组的bit位置
 * 3.验证
 * 此实现方式可以指定Hash算法
 *
 * @author Kimi Liu
 * @version 6.3.5
 * @since JDK 1.8+
 */
public class BitMapBloomFilter implements BloomFilter {

    private static final long serialVersionUID = 1L;

    private BloomFilter[] filters;

    /**
     * 构造，使用默认的5个过滤器
     *
     * @param m M值决定BitMap的大小
     */
    public BitMapBloomFilter(int m) {
        long mNum = MathKit.div(String.valueOf(m), String.valueOf(5)).longValue();
        long size = mNum * Normal._1024 * Normal._1024 * 8;

        filters = new BloomFilter[]{
                new DefaultFilter(size),
                new ELFFilter(size),
                new JSFilter(size),
                new PJWFilter(size),
                new SDBMFilter(size)
        };
    }

    /**
     * 使用自定的多个过滤器建立BloomFilter
     *
     * @param m       M值决定BitMap的大小
     * @param filters Bloom过滤器列表
     */
    public BitMapBloomFilter(int m, BloomFilter... filters) {
        this(m);
        this.filters = filters;
    }

    /**
     * 增加字符串到Filter映射中
     *
     * @param text 字符串
     */
    @Override
    public boolean add(String text) {
        boolean flag = false;
        for (BloomFilter filter : filters) {
            flag |= filter.add(text);
        }
        return flag;
    }

    /**
     * 是否可能包含此字符串，此处存在误判
     *
     * @param text 字符串
     * @return 是否存在
     */
    @Override
    public boolean contains(String text) {
        for (BloomFilter filter : filters) {
            if (filter.contains(text) == false) {
                return false;
            }
        }
        return true;
    }

}