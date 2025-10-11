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

import java.net.URL;
import java.util.regex.Pattern;

/**
 * A filter that uses regular expressions to match resource names.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegexFilter implements Filter {

    /**
     * The compiled regular expression pattern used for filtering.
     */
    private final Pattern pattern;

    /**
     * Constructs a {@code RegexFilter} with the given regular expression string.
     *
     * @param regex The regular expression string.
     */
    public RegexFilter(String regex) {
        this(Pattern.compile(regex));
    }

    /**
     * Constructs a {@code RegexFilter} with the given compiled {@link Pattern}.
     *
     * @param pattern The compiled regular expression pattern.
     * @throws IllegalArgumentException If the provided pattern is {@code null}.
     */
    public RegexFilter(Pattern pattern) {
        if (null == pattern) {
            throw new IllegalArgumentException("pattern must not be null");
        }
        this.pattern = pattern;
    }

    /**
     * Filters a resource based on whether its name matches the regular expression pattern.
     *
     * @param name The name of the resource (relative path).
     * @param url  The URL of the resource (not used in this filter).
     * @return {@code true} if the resource name matches the pattern, {@code false} otherwise.
     */
    @Override
    public boolean filtrate(String name, URL url) {
        return pattern.matcher(name).matches();
    }

}
