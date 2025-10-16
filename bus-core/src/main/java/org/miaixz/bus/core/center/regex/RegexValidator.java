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
package org.miaixz.bus.core.center.regex;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * Regular expression validation utility class. This class provides methods to check if a given content matches a
 * regular expression or if it contains any substring that matches a regular expression.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RegexValidator {

    /**
     * Checks if the given content matches the regular expression.
     *
     * @param regex   The regular expression string.
     * @param content The content to be checked.
     * @return {@code true} if the content matches the regex, {@code false} otherwise. Returns {@code true} if the regex
     *         is {@code null} or empty (no check performed). Returns {@code false} if the content is {@code null}.
     */
    public static boolean isMatch(final String regex, final CharSequence content) {
        if (content == null) {
            // A null string is considered not matching.
            return false;
        }

        if (StringKit.isEmpty(regex)) {
            // If regex is null or empty, it's considered a full match (no restriction).
            return true;
        }

        final java.util.regex.Pattern pattern = Pattern.get(regex, java.util.regex.Pattern.DOTALL);
        return isMatch(pattern, content);
    }

    /**
     * Checks if the given content matches the compiled regular expression pattern.
     *
     * @param pattern The compiled regular expression pattern.
     * @param content The content to be checked.
     * @return {@code true} if the content matches the pattern, {@code false} otherwise. Returns {@code false} if the
     *         content or pattern is {@code null}.
     */
    public static boolean isMatch(final java.util.regex.Pattern pattern, final CharSequence content) {
        if (content == null || pattern == null) {
            // A null string or null pattern is considered not matching.
            return false;
        }
        return pattern.matcher(content).matches();
    }

    /**
     * Checks if the given content contains any substring that matches the regular expression.
     *
     * @param regex   The regular expression string.
     * @param content The content to be searched.
     * @return {@code true} if the content contains a match, {@code false} otherwise. Returns {@code false} if regex or
     *         content is {@code null}.
     */
    public static boolean contains(final String regex, final CharSequence content) {
        if (null == regex || null == content) {
            return false;
        }

        final java.util.regex.Pattern pattern = Pattern.get(regex, java.util.regex.Pattern.DOTALL);
        return contains(pattern, content);
    }

    /**
     * Checks if the given content contains any substring that matches the compiled regular expression pattern.
     *
     * @param pattern The compiled regular expression pattern.
     * @param content The content to be searched.
     * @return {@code true} if the content contains a match, {@code false} otherwise. Returns {@code false} if pattern
     *         or content is {@code null}.
     */
    public static boolean contains(final java.util.regex.Pattern pattern, final CharSequence content) {
        if (null == pattern || null == content) {
            return false;
        }
        return pattern.matcher(content).find();
    }

}
