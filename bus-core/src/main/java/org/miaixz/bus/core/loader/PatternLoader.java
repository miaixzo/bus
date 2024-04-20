/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
package org.miaixz.bus.core.loader;

import org.miaixz.bus.core.io.resource.Resource;

import java.io.IOException;
import java.util.Enumeration;

/**
 * 模式匹配资源加载器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class PatternLoader extends DelegateLoader implements Loader {

    protected PatternLoader(Loader delegate) {
        super(delegate);
    }

    /**
     * 加载匹配模式表达式的所有资源,由于模式表达式中有可能表达了是否递归加载的含义,
     * 所以缺省情况下recursively参数会被忽略,取而代之的是{@link PatternLoader#recursively(String)}的返回值,
     * 如果字类实现的模式表达式并不能表达是否递归加载的含义,需要重写该方法以满足更多定制化的需求
     * 另外当filter参数不为null时,由模式表达式推导出的过滤器将会和filter参数混合成一个{@link AllFilter}混合过滤器
     *
     * @param pattern     模式表达式
     * @param recursively 递归加载
     * @param filter      过滤器
     * @return 所有匹配模式表达式的资源
     * @throws IOException I/O 异常
     */
    @Override
    public Enumeration<Resource> load(String pattern, boolean recursively, Filter filter) throws IOException {
        Filter matcher = filter(pattern);
        AllFilter allFilter = new AllFilter();
        if (null != matcher) {
            allFilter.add(matcher);
        }
        if (null != filter) {
            allFilter.add(filter);
        }
        return delegate.load(
                path(pattern),
                recursively(pattern),
                allFilter
        );
    }

    /**
     * 根据资源表达式推导出资源根路径
     *
     * @param pattern 资源表达式
     * @return 资源根路径
     */
    protected abstract String path(String pattern);

    /**
     * 根据资源表达式推导出是否从根路径开始递归加载
     *
     * @param pattern 资源表达式
     * @return 是否从根路径开始递归加载
     */
    protected abstract boolean recursively(String pattern);

    /**
     * 根据资源表达式推导出资源过滤器
     *
     * @param pattern 资源表达式
     * @return 出资源过滤器
     */
    protected abstract Filter filter(String pattern);

}
