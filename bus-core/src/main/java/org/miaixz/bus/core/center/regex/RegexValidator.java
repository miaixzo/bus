/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.regex;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * Regular expression validation utility class. This class provides methods to check if a given content matches a
 * regular expression or if it contains any substring that matches a regular expression.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RegexValidator {

    /**
     * Constructs a new regular expression validator.
     */
    public RegexValidator() {

    }

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
