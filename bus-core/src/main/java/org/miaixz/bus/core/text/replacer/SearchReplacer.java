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
 * @since Java 21+
 */
public class SearchReplacer extends StringReplacer {

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
