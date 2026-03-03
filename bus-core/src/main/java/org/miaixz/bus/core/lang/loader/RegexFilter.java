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
