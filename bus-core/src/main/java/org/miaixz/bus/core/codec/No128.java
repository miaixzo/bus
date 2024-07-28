/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.codec;

import java.nio.ByteOrder;
import java.util.Objects;

/**
 * 128位数字表示，分为：
 * <ul>
 * <li>最高有效位（Most Significant Bit），64 bit（8 bytes）</li>
 * <li>最低有效位（Least Significant Bit），64 bit（8 bytes）</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class No128 extends Number implements Comparable<No128> {

    private static final long serialVersionUID = -1L;

    /**
     * 最高有效位（Most Significant Bit），64 bit（8 bytes）
     */
    private long mostSigBits;
    /**
     * 最低有效位（Least Significant Bit），64 bit（8 bytes）
     */
    private long leastSigBits;

    /**
     * 构造
     *
     * @param mostSigBits  高位
     * @param leastSigBits 低位
     */
    public No128(final long mostSigBits, final long leastSigBits) {
        this.mostSigBits = mostSigBits;
        this.leastSigBits = leastSigBits;
    }

    /**
     * 获取最高有效位（Most Significant Bit），64 bit（8 bytes）
     *
     * @return 最高有效位（Most Significant Bit），64 bit（8 bytes）
     */
    public long getMostSigBits() {
        return mostSigBits;
    }

    /**
     * 设置最高有效位（Most Significant Bit），64 bit（8 bytes）
     *
     * @param hiValue 最高有效位（Most Significant Bit），64 bit（8 bytes）
     */
    public void setMostSigBits(final long hiValue) {
        this.mostSigBits = hiValue;
    }

    /**
     * 获取最低有效位（Least Significant Bit），64 bit（8 bytes）
     *
     * @return 最低有效位（Least Significant Bit），64 bit（8 bytes）
     */
    public long getLeastSigBits() {
        return leastSigBits;
    }

    /**
     * 设置最低有效位（Least Significant Bit），64 bit（8 bytes）
     *
     * @param leastSigBits 最低有效位（Least Significant Bit），64 bit（8 bytes）
     */
    public void setLeastSigBits(final long leastSigBits) {
        this.leastSigBits = leastSigBits;
    }

    /**
     * 获取高低位数组，规则为：
     * <ul>
     * <li>{@link ByteOrder#LITTLE_ENDIAN}，则long[0]：低位，long[1]：高位</li>
     * <li>{@link ByteOrder#BIG_ENDIAN}，则long[0]：高位，long[1]：低位</li>
     * </ul>
     *
     * @param byteOrder 端续
     * @return 高低位数组，long[0]：低位，long[1]：高位
     */
    public long[] getLongArray(final ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            return new long[] { leastSigBits, mostSigBits };
        } else {
            return new long[] { mostSigBits, leastSigBits };
        }
    }

    @Override
    public int intValue() {
        return (int) longValue();
    }

    @Override
    public long longValue() {
        return this.leastSigBits;
    }

    @Override
    public float floatValue() {
        return longValue();
    }

    @Override
    public double doubleValue() {
        return longValue();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof No128) {
            final No128 no128 = (No128) o;
            return leastSigBits == no128.leastSigBits && mostSigBits == no128.mostSigBits;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(leastSigBits, mostSigBits);
    }

    @Override
    public int compareTo(final No128 o) {
        final int mostSigBits = Long.compare(this.mostSigBits, o.mostSigBits);
        return mostSigBits != 0 ? mostSigBits : Long.compare(this.leastSigBits, o.leastSigBits);
    }

}
