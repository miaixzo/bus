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
package org.miaixz.bus.core.text.replacer;

import java.io.Serial;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A replacer that searches for a given string and replaces all occurrences with a new string. Other characters remain
 * unchanged.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SearchReplacer extends StringReplacer {

    /**
     * The serial version UID.
     */
    @Serial
    private static final long serialVersionUID = 2852239556681L;

    /**
     * The starting index (inclusive) for the search operation.
     */
    private final int fromIndex;
    /**
     * The character sequence to search for.
     */
    private final CharSequence searchText;
    /**
     * The length of the search text.
     */
    private final int searchTextLength;
    /**
     * The character sequence to replace the search text with.
     */
    private final CharSequence replacement;
    /**
     * Indicates whether the search should ignore case.
     */
    private final boolean ignoreCase;

    /**
     * Constructs a new {@code SearchReplacer}.
     *
     * @param fromIndex   The starting position (inclusive) for the search.
     * @param searchText  The string to be searched for.
     * @param replacement The string to replace the found text with.
     * @param ignoreCase  {@code true} if the search should ignore case, {@code false} otherwise.
     */
    public SearchReplacer(final int fromIndex, final CharSequence searchText, final CharSequence replacement,
            final boolean ignoreCase) {
        this.fromIndex = Math.max(fromIndex, 0);
        this.searchText = Assert.notEmpty(searchText, "'searchStr' must be not empty!");
        this.searchTextLength = searchText.length();
        this.replacement = StringKit.toStringOrEmpty(replacement);
        this.ignoreCase = ignoreCase;
    }

    /**
     * Applies the replacement logic to the given text. This method performs a full replacement operation on the input
     * text.
     *
     * @param text The text to which the replacement logic will be applied.
     * @return The text with all occurrences of {@code searchText} replaced by {@code replacement}.
     */
    @Override
    public String apply(final CharSequence text) {
        if (StringKit.isEmpty(text)) {
            return StringKit.toStringOrNull(text);
        }

        final int strLength = text.length();
        if (strLength < this.searchTextLength) {
            return StringKit.toStringOrNull(text);
        }

        final int fromIndex = this.fromIndex;
        if (fromIndex > strLength) {
            // Out of bounds truncation
            return Normal.EMPTY;
        }

        final StringBuilder result = new StringBuilder(strLength - this.searchTextLength + this.replacement.length());
        if (0 != fromIndex) {
            // Initial part
            result.append(text.subSequence(0, fromIndex));
        }

        // Replacement part
        int pos = fromIndex;
        int consumed;// Number of characters processed
        while ((consumed = replace(text, pos, result)) > 0) {
            pos += consumed;
        }

        if (pos < strLength) {
            // Trailing part
            result.append(text.subSequence(pos, strLength));
        }
        return result.toString();
    }

    /**
     * Replaces a portion of the text if {@code searchText} is found at or after the given position.
     *
     * @param text The text to be processed.
     * @param pos  The current position in the text.
     * @param out  The {@code StringBuilder} to which the replaced text is appended.
     * @return The number of characters consumed by the replacement, or {@code Normal.__1} if no replacement occurred.
     */
    @Override
    protected int replace(final CharSequence text, final int pos, final StringBuilder out) {
        final int index = StringKit.indexOf(text, this.searchText, pos, this.ignoreCase);
        if (index > Normal.__1) {
            // Part that does not need replacement
            out.append(text.subSequence(pos, index));
            // Part to be replaced
            out.append(replacement);

            // Length processed = length not replaced (position of search string - start position) + length of
            // replacement
            return index - pos + searchTextLength;
        }

        // Not found
        return Normal.__1;
    }

}
