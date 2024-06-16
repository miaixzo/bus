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
package org.miaixz.bus.core.lang.loader;

import java.net.URL;
import java.util.Collection;

/**
 * ALL逻辑复合过滤器,即所有过滤器都满足的时候才满足,
 * 只要有一个过滤器不满足就立刻返回不满足,
 * 如果没有过滤器的时候则认为所有过滤器都满足
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AllFilter extends MixFilter implements Filter {

    /**
     * 构造
     *
     * @param filters 过滤器
     */
    public AllFilter(Filter... filters) {
        super(filters);
    }

    /**
     * 构造
     *
     * @param filters 过滤器
     */
    public AllFilter(Collection<? extends Filter> filters) {
        super(filters);
    }

    /**
     * @param name 资源名称,即相对路径
     * @param url  资源URL地址
     * @return the boolean
     */
    public boolean filtrate(String name, URL url) {
        Filter[] filters = this.filters.toArray(new Filter[0]);
        for (Filter filter : filters) {
            if (!filter.filtrate(name, url)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param filter 过滤器
     * @return the object
     */
    public AllFilter mix(Filter filter) {
        add(filter);
        return this;
    }

}
