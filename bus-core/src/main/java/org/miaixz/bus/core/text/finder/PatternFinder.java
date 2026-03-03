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
package org.miaixz.bus.core.text.finder;

import java.io.Serial;
import java.util.regex.Matcher;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;

/**
 * Regular expression finder. Finds the start and end positions of a regular expression match within a specified string.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PatternFinder extends TextFinder {

    @Serial
    private static final long serialVersionUID = 2852237367830L;

    /**
     * The compiled regular expression pattern.
     */
    private final java.util.regex.Pattern pattern;
    /**
     * The matcher object used for performing match operations.
     */
    private Matcher matcher;

    /**
     * Constructor.
     *
     * @param regex           The regular expression to be searched.
     * @param caseInsensitive Whether to ignore case.
     */
    public PatternFinder(final String regex, final boolean caseInsensitive) {
        this(Pattern.get(regex, caseInsensitive ? java.util.regex.Pattern.CASE_INSENSITIVE : 0));
    }

    /**
     * Constructor.
     *
     * @param pattern The compiled regular expression {@link java.util.regex.Pattern} to be searched.
     */
    public PatternFinder(final java.util.regex.Pattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Sets the text to search and creates a new matcher for it.
     *
     * @param text The text to search in.
     * @return This {@code PatternFinder} instance for chaining.
     */
    @Override
    public TextFinder setText(final CharSequence text) {
        this.matcher = pattern.matcher(text);
        return super.setText(text);
    }

    /**
     * Sets the negative search direction. This operation is not supported for pattern matching.
     *
     * @param negative The negative flag (ignored).
     * @return This method always throws an exception.
     * @throws UnsupportedOperationException Always, as negative search is not supported for pattern matching.
     */
    @Override
    public TextFinder setNegative(final boolean negative) {
        throw new UnsupportedOperationException("Negative is invalid for Pattern!");
    }

    /**
     * Finds the start position of the next pattern match.
     *
     * @param from The position to start searching from.
     * @return The start position of the match, or -1 if no match is found.
     */
    @Override
    public int start(final int from) {
        if (matcher.find(from)) {
            final int end = matcher.end();
            // Only if the end of the matched string is within the limit, it is considered found.
            if (end <= getValidEndIndex()) {
                final int start = matcher.start();
                if (start == end) {
                    // If an empty string is matched, treat it as not matched to avoid an infinite loop.
                    return Normal.__1;
                }

                return start;
            }
        }
        return Normal.__1;
    }

    /**
     * Returns the end position of the pattern match.
     *
     * @param start The start position of the match.
     * @return The end position of the match, or -1 if invalid.
     */
    @Override
    public int end(final int start) {
        if (start < 0) {
            return -1;
        }
        final int end = matcher.end();
        final int limit;
        if (endIndex < 0) {
            limit = text.length();
        } else {
            limit = Math.min(endIndex, text.length());
        }
        return end <= limit ? end : Normal.__1;
    }

    /**
     * Resets the matcher to its initial state.
     *
     * @return This {@code PatternFinder} instance for chaining.
     */
    @Override
    public PatternFinder reset() {
        this.matcher.reset();
        return this;
    }

}
