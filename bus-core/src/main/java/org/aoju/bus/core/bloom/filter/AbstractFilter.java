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
package org.aoju.bus.core.bloom.filter;

import org.aoju.bus.core.bloom.BloomFilter;
import org.aoju.bus.core.bloom.bitmap.BitMap;
import org.aoju.bus.core.bloom.bitmap.IntMap;
import org.aoju.bus.core.bloom.bitmap.LongMap;

/**
 * 抽象Bloom过滤器
 *
 * @author Kimi Liu
 * @version 6.2.9
 * @since JDK 1.8+
 */
public abstract class AbstractFilter implements BloomFilter {

    private static final long serialVersionUID = 1L;
    protected long size = 0;
    private BitMap bm = null;

    /**
     * 构造
     *
     * @param maxValue   最大值
     * @param machineNum 机器位数
     */
    public AbstractFilter(long maxValue, int machineNum) {
        init(maxValue, machineNum);
    }

    /**
     * 构造32位
     *
     * @param maxValue 最大值
     */
    public AbstractFilter(long maxValue) {
        this(maxValue, BitMap.MACHINE32);
    }

    /**
     * 初始化
     *
     * @param maxValue   最大值
     * @param machineNum 机器位数
     */
    public void init(long maxValue, int machineNum) {
        this.size = maxValue;
        switch (machineNum) {
            case BitMap.MACHINE32:
                bm = new IntMap((int) (size / machineNum));
                break;
            case BitMap.MACHINE64:
                bm = new LongMap((int) (size / machineNum));
                break;
            default:
                throw new RuntimeException("Error Machine number!");
        }
    }

    @Override
    public boolean contains(String text) {
        return bm.contains(Math.abs(hash(text)));
    }

    @Override
    public boolean add(String text) {
        final long hash = Math.abs(hash(text));
        if (bm.contains(hash)) {
            return false;
        }

        bm.add(hash);
        return true;
    }

    /**
     * 自定义Hash方法
     *
     * @param text 字符串
     * @return the long
     */
    public abstract long hash(String text);

}