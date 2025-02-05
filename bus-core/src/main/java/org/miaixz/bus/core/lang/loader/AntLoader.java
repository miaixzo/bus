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
package org.miaixz.bus.core.lang.loader;

import java.io.IOException;
import java.util.Enumeration;

import org.miaixz.bus.core.io.resource.Resource;
import org.miaixz.bus.core.lang.Symbol;

/**
 * ANT风格路径资源加载器
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AntLoader extends PatternLoader implements Loader {

    /**
     * 构造
     */
    public AntLoader() {
        this(new StdLoader());
    }

    /**
     * 构造
     *
     * @param classLoader 加载器
     */
    public AntLoader(ClassLoader classLoader) {
        this(new StdLoader(classLoader));
    }

    /**
     * 构造
     *
     * @param delegate 类加载代理
     */
    public AntLoader(Loader delegate) {
        super(delegate);
    }

    @Override
    public Enumeration<Resource> load(String pattern, boolean recursively, Filter filter) throws IOException {
        if (Math.max(pattern.indexOf(Symbol.C_STAR), pattern.indexOf(Symbol.C_QUESTION_MARK)) < 0) {
            return delegate.load(pattern, recursively, filter);
        } else {
            return super.load(pattern, recursively, filter);
        }
    }

    /**
     * 加载路径处理
     *
     * @param ant ANT风格路径表达式
     * @return the string
     */
    protected String path(String ant) {
        int index = Integer.MAX_VALUE - 1;
        if (ant.contains(Symbol.STAR) && ant.indexOf(Symbol.C_STAR) < index)
            index = ant.indexOf(Symbol.C_STAR);
        if (ant.contains(Symbol.QUESTION_MARK) && ant.indexOf(Symbol.C_QUESTION_MARK) < index)
            index = ant.indexOf(Symbol.C_QUESTION_MARK);
        return ant.substring(0, ant.lastIndexOf(Symbol.C_SLASH, index) + 1);
    }

    /**
     * 是否递归
     *
     * @param ant ANT风格路径表达式
     * @return the boolean
     */
    protected boolean recursively(String ant) {
        return true;
    }

    /**
     * 过滤器
     *
     * @param ant ANT风格路径表达式
     * @return the 过滤器
     */
    protected Filter filter(String ant) {
        return new AntFilter(ant);
    }

}
