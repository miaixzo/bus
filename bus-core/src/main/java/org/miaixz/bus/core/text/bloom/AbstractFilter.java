/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.text.bloom;

import java.util.BitSet;

import org.miaixz.bus.core.lang.Assert;

/**
 * 抽象Bloom过滤器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractFilter implements BloomFilter {

    private static final long serialVersionUID = -1L;

    /**
     * 增长向量
     */
    private final BitSet bitSet;
    /**
     * 容量
     */
    protected int size;

    /**
     * 构造
     *
     * @param size 容量
     */
    public AbstractFilter(final int size) {
        Assert.isTrue(size > 0, "Size must be greater than 0.");
        this.size = size;
        this.bitSet = new BitSet(size);
    }

    @Override
    public boolean contains(final String text) {
        return bitSet.get(Math.abs(hash(text)));
    }

    @Override
    public boolean add(final String text) {
        final int hash = Math.abs(hash(text));
        if (bitSet.get(hash)) {
            return false;
        }

        bitSet.set(hash);
        return true;
    }

    /**
     * 自定义Hash方法
     *
     * @param text 字符串
     * @return the int
     */
    public abstract int hash(String text);

}
