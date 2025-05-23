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

import java.io.Serial;

/**
 * 组合BloomFilter 实现 1.构建hash算法 2.散列hash映射到数组的bit位置 3.验证 此实现方式可以指定Hash算法
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CombinedBloomFilter implements BloomFilter {

    @Serial
    private static final long serialVersionUID = 2852319692172L;

    private final BloomFilter[] filters;

    /**
     * 使用自定的多个过滤器建立BloomFilter
     *
     * @param filters Bloom过滤器列表
     */
    public CombinedBloomFilter(final BloomFilter... filters) {
        this.filters = filters;
    }

    /**
     * 增加字符串到Filter映射中
     *
     * @param text 字符串
     */
    @Override
    public boolean add(final String text) {
        boolean flag = false;
        for (final BloomFilter filter : filters) {
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
    public boolean contains(final String text) {
        for (final BloomFilter filter : filters) {
            if (filter.contains(text) == false) {
                return false;
            }
        }
        return true;
    }

}
