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
package org.aoju.bus.office.magic.filter;

import com.sun.star.lang.XComponent;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.office.Context;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 过滤器链的基类.
 *
 * @author Kimi Liu
 * @version 6.3.0
 * @since JDK 1.8+
 */
public abstract class AbstractFilter implements FilterChain {

    protected List<Filter> filters;
    protected int pos;
    private boolean readOnly;

    /**
     * 创建一个FilterChain.
     */
    public AbstractFilter() {
        this(false);
    }

    /**
     * 创建一个包含指定过滤器的FilterChain.
     *
     * @param filters 要添加到链中的过滤器.
     */
    public AbstractFilter(final Filter... filters) {
        this(false, filters);
    }

    /**
     * 创建一个包含指定过滤器的FilterChain.
     *
     * @param readOnly {@code true}如果链必须是只读的(这意味着没有其他过滤器可以添加到链中)，{@code false}否则.
     * @param filters  最初添加到链中的过滤器.
     */
    public AbstractFilter(final boolean readOnly, final Filter... filters) {

        this.readOnly = readOnly;
        this.pos = 0;
        this.filters =
                Arrays.stream(Optional.ofNullable(filters).orElse(new Filter[0]))
                        .collect(Collectors.toList());

        if (readOnly) {
            this.filters = Collections.unmodifiableList(this.filters);
        }
    }

    @Override
    public void addFilter(final Filter filter) {

        if (readOnly) {
            throw new UnsupportedOperationException();
        }
        filters.add(filter);
    }

    @Override
    public void doFilter(final Context context, final XComponent document)
            throws InstrumentException {

        // 如果有下一个过滤器，则调用它
        if (pos < filters.size()) {
            final Filter filter = filters.get(pos++);
            doFilter(filter, context, document);
        }
    }

    /**
     * 导致调用指定的筛选器.
     *
     * @param filter   要执行的筛选器.
     * @param context  用于沿链传递的上下文.
     * @param document 被转换为沿链传递的文档.
     * @throws InstrumentException 如果处理过滤器时发生错误.
     */
    protected void doFilter(
            final Filter filter, final Context context, final XComponent document)
            throws InstrumentException {

        try {
            filter.doFilter(context, document, this);
        } catch (InstrumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new InstrumentException("Could not apply filter " + filter.getClass().getName() + Symbol.DOT, ex);
        }
    }

    /**
     * 将过滤器链中的位置重置为0，使该链可重用.
     */
    public void reset() {
        pos = 0;
    }

}
