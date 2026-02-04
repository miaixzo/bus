/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.shade.safety.complex;

import java.util.regex.Pattern;

import org.miaixz.bus.shade.safety.Complex;

/**
 * An abstract {@link Complex} implementation that filters entries based on regular expression matching.
 *
 * @param <E> The type of entry to be filtered.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class RegexComplex<E> implements Complex<E> {

    /**
     * The compiled regular expression pattern used for matching.
     */
    protected final Pattern pattern;

    /**
     * Constructs a new {@code RegexComplex} with the specified regular expression string. The string is compiled into a
     * {@link Pattern}.
     *
     * @param regex The regular expression string to use for filtering.
     */
    protected RegexComplex(String regex) {
        this(Pattern.compile(regex));
    }

    /**
     * Constructs a new {@code RegexComplex} with the specified compiled {@link Pattern}.
     *
     * @param pattern The compiled {@link Pattern} to use for filtering.
     */
    protected RegexComplex(Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Evaluates the given entry against the configured regular expression pattern. The entry is first converted to a
     * string representation using {@link #toText(Object)}.
     *
     * @param entry The entry to be evaluated.
     * @return {@code true} if the string representation of the entry matches the regular expression; {@code false}
     *         otherwise.
     */
    @Override
    public boolean on(E entry) {
        String text = toText(entry);
        return pattern.matcher(text).matches();
    }

    /**
     * Converts an entry into its string representation for pattern matching. Subclasses must implement this method to
     * define how their specific entry type is represented as a string.
     *
     * @param entry The entry to convert.
     * @return The string representation of the entry.
     */
    protected abstract String toText(E entry);

}
