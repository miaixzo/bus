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
package org.miaixz.bus.shade.safety.boot.jar;

import java.util.regex.Pattern;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.miaixz.bus.shade.safety.Complex;
import org.miaixz.bus.shade.safety.complex.RegexComplex;

/**
 * A {@link Complex} implementation that filters {@link JarArchiveEntry} entries based on regular expression matching.
 * This class extends {@link RegexComplex} and provides a way to apply regex patterns to the names of JAR archive
 * entries.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JarRegexComplex extends RegexComplex<JarArchiveEntry> implements Complex<JarArchiveEntry> {

    /**
     * Constructs a new {@code JarRegexComplex} with the specified regular expression string.
     *
     * @param regex The regular expression string to use for filtering.
     */
    public JarRegexComplex(String regex) {
        super(regex);
    }

    /**
     * Constructs a new {@code JarRegexComplex} with the specified compiled {@link Pattern}.
     *
     * @param pattern The compiled {@link Pattern} to use for filtering.
     */
    public JarRegexComplex(Pattern pattern) {
        super(pattern);
    }

    /**
     * Converts a {@link JarArchiveEntry} into a string representation for pattern matching. This implementation returns
     * the name of the JAR archive entry.
     *
     * @param entry The {@link JarArchiveEntry} to convert.
     * @return The name of the JAR archive entry.
     */
    @Override
    protected String toText(JarArchiveEntry entry) {
        return entry.getName();
    }

}
