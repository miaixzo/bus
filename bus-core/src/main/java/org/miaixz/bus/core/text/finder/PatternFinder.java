/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
