/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.office.magic.filter;

import com.sun.star.lang.XComponent;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.Context;

/**
 * FilterChain的默认实现.
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8+
 */
public class DefaultFilter extends AbstractFilter {

    private final boolean endsWithRefreshFilter;

    /**
     * 创建一个没有任何过滤器的过滤器链，它总是在链的末尾应用一个{@link RefreshFilter}.
     * 稍后可以使用{@link #addFilter(Filter)}添加过滤器
     */
    public DefaultFilter() {
        this(true);
    }

    /**
     * 创建一个FilterChain，它将包含指定的过滤器，并始终在链的末尾应用一个{@link RefreshFilter}.
     *
     * @param filters 要添加到链中的过滤器.
     */
    public DefaultFilter(final Filter... filters) {
        this(true, filters);
    }

    /**
     * 创建一个没有任何过滤器的过滤器链。稍后可以使用{@link #addFilter(Filter)}添加过滤器.
     *
     * @param endsWithRefreshFilter 如果{@code true}， {@link RefreshFilter}将始终应用于链的末尾.
     */
    public DefaultFilter(final boolean endsWithRefreshFilter) {
        super();

        this.endsWithRefreshFilter = endsWithRefreshFilter;
    }

    /**
     * 创建一个包含指定过滤器的FilterChain.
     *
     * @param endsWithRefreshFilter 如果{@code true}， {@link RefreshFilter}将始终应用于链的末尾.
     * @param filters               要添加到链中的过滤器.
     */
    public DefaultFilter(final boolean endsWithRefreshFilter, final Filter... filters) {
        super(filters);

        this.endsWithRefreshFilter = endsWithRefreshFilter;
    }

    @Override
    public FilterChain copy() {
        return new DefaultFilter(endsWithRefreshFilter, filters.toArray(new Filter[0]));
    }

    @Override
    public void doFilter(final Context context, final XComponent document)
            throws InstrumentException {

        // 如果在链的末端，调用RefreshFilter
        if (pos == filters.size() && endsWithRefreshFilter) {
            doFilter(RefreshFilter.INSTANCE, context, document);
        } else {
            super.doFilter(context, document);
        }
    }

}
