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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 复合过滤器,实际上内部维护一个过滤器的{@link LinkedHashSet}集合,提供添加/删除以及链式拼接的方法来混合多个子过滤器,该过滤器的具体逻辑由子类拓展
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class MixFilter implements Filter {

    protected final Set<Filter> filters;

    protected MixFilter(Filter... filters) {
        this(Arrays.asList(filters));
    }

    protected MixFilter(Collection<? extends Filter> filters) {
        this.filters = null != filters ? new LinkedHashSet<>(filters) : new LinkedHashSet<>();
    }

    /**
     * 添加过滤器
     *
     * @param filter 过滤器
     * @return 添加成功：true    否则：false 即代表重复添加
     */
    public boolean add(Filter filter) {
        return filters.add(filter);
    }

    /**
     * 删除过滤器
     *
     * @param filter 过滤器
     * @return 删除成功：true    否则：false 即代表已不存在
     */
    public boolean remove(Filter filter) {
        return filters.remove(filter);
    }

    /**
     * 支持采用链式调用的方式混合多个过滤器,其内部调用{@link MixFilter#add(Filter)}且返回this.
     * 该方法设计成abstract其用意是强制子类将方法的返回值类型替换成自身类型
     *
     * @param filter 过滤器
     * @return this
     */
    public abstract MixFilter mix(Filter filter);

}
