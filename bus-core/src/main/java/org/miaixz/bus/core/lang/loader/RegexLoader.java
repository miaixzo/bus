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

import org.miaixz.bus.core.lang.Normal;

/**
 * A resource loader that uses regular expressions to match resource paths. This loader delegates the actual resource
 * loading to another {@link Loader} instance.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegexLoader extends PatternLoader implements Loader {

    /**
     * Constructs a new {@code RegexLoader} with a default {@link StdLoader} as its delegate.
     */
    public RegexLoader() {
        this(new StdLoader());
    }

    /**
     * Constructs a new {@code RegexLoader} with a specified {@link ClassLoader} for its delegate {@link StdLoader}.
     *
     * @param classLoader The class loader to use for the delegate {@link StdLoader}.
     */
    public RegexLoader(ClassLoader classLoader) {
        this(new StdLoader(classLoader));
    }

    /**
     * Constructs a new {@code RegexLoader} with a specified delegate {@link Loader}.
     *
     * @param delegate The delegate loader to use for actual resource loading.
     */
    public RegexLoader(Loader delegate) {
        super(delegate);
    }

    /**
     * Returns an empty string as the base path for regular expression patterns. The regular expression itself defines
     * the path matching logic.
     *
     * @param pattern The regular expression pattern.
     * @return An empty string.
     */
    @Override
    protected String path(String pattern) {
        return Normal.EMPTY;
    }

    /**
     * Determines whether to recursively load resources based on the regular expression pattern. For
     * {@code RegexLoader}, this method always returns {@code true} to allow recursive matching by default.
     *
     * @param pattern The regular expression pattern.
     * @return Always {@code true}.
     */
    @Override
    protected boolean recursively(String pattern) {
        return true;
    }

    /**
     * Creates a {@link RegexFilter} based on the provided regular expression pattern.
     *
     * @param pattern The regular expression pattern.
     * @return A new {@link RegexFilter} instance.
     */
    @Override
    protected Filter filter(String pattern) {
        return new RegexFilter(pattern);
    }

}
