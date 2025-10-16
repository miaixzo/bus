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

/**
 * An abstract resource loader that supports pattern matching for resource paths. This loader delegates the actual
 * resource loading to another {@link Loader} instance after interpreting the pattern to determine the base path,
 * recursion, and filtering logic.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class PatternLoader extends DelegateLoader implements Loader {

    /**
     * Constructs a new {@code PatternLoader} with a specified delegate {@link Loader}.
     *
     * @param delegate The delegate loader to use for actual resource loading.
     */
    protected PatternLoader(Loader delegate) {
        super(delegate);
    }

    /**
     * Loads all resources that match the given pattern expression. The {@code recursively} parameter might be ignored
     * if the pattern itself implies recursion. Its behavior is determined by the return value of
     * {@link PatternLoader#recursively(String)}. If the subclass's pattern expression cannot express recursion, this
     * method should be overridden to meet more customized requirements. Additionally, if the {@code filter} parameter
     * is not {@code null}, the filter derived from the pattern will be combined with the {@code filter} parameter into
     * an {@link AllFilter} composite filter.
     *
     * @param pattern     The pattern expression for resources.
     * @param recursively Whether to load resources recursively (may be overridden by pattern interpretation).
     * @param filter      An additional filter to apply to resources, or {@code null} if no additional filter is needed.
     * @return An enumeration of all resources matching the pattern expression.
     * @throws IOException If an I/O error occurs during resource loading.
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
        return delegate.load(path(pattern), recursively(pattern), allFilter);
    }

    /**
     * Derives the root path for resources based on the pattern expression.
     *
     * @param pattern The pattern expression.
     * @return The root path for resources.
     */
    protected abstract String path(String pattern);

    /**
     * Determines whether to recursively load resources starting from the root path, based on the pattern expression.
     *
     * @param pattern The pattern expression.
     * @return {@code true} if resources should be loaded recursively, {@code false} otherwise.
     */
    protected abstract boolean recursively(String pattern);

    /**
     * Derives a resource filter based on the pattern expression.
     *
     * @param pattern The pattern expression.
     * @return A {@link Filter} instance derived from the pattern.
     */
    protected abstract Filter filter(String pattern);

}
