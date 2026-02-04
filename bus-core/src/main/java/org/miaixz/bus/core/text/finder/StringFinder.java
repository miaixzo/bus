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
package org.miaixz.bus.core.text.finder;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.text.CharsBacker;

/**
 * String finder. Used to find the position of a specified string within a text.
 * <p>
 * This implementation uses the Sunday algorithm for efficient substring searching.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringFinder extends TextFinder {

    @Serial
    private static final long serialVersionUID = 2852237656798L;

    /**
     * The string to find.
     */
    private final CharSequence strToFind;

    /**
     * Whether to ignore case during the search.
     */
    private final boolean caseInsensitive;

    /**
     * Cache for forward offset mapping (used in forward searches).
     */
    private Map<Character, Integer> forwardOffsetMap;

    /**
     * Cache for reverse offset mapping (used in backward searches).
     */
    private Map<Character, Integer> reverseOffsetMap;

    /**
     * Constructor.
     *
     * @param strToFind       The string to be searched.
     * @param caseInsensitive Whether to ignore case.
     */
    public StringFinder(final CharSequence strToFind, final boolean caseInsensitive) {
        Assert.notEmpty(strToFind);
        this.strToFind = strToFind;
        this.caseInsensitive = caseInsensitive;
    }

    /**
     * Creates a finder. After construction, {@link #setText(CharSequence)} must be called to set the text to be
     * searched.
     *
     * @param strToFind       The string to be searched.
     * @param caseInsensitive Whether to ignore case.
     * @return A new {@code StringFinder} instance.
     */
    public static StringFinder of(final CharSequence strToFind, final boolean caseInsensitive) {
        return new StringFinder(strToFind, caseInsensitive);
    }

    /**
     * Builds the forward offset map for the Sunday algorithm.
     * <p>
     * The offset indicates how far to jump if the character following the current window match attempt does not match.
     * It maps characters in the pattern to their distance from the end of the pattern.
     * </p>
     *
     * @param pattern         The pattern string.
     * @param caseInsensitive Whether the search is case-insensitive.
     * @return A map of character offsets.
     */
    private static Map<Character, Integer> buildForwardOffsetMap(CharSequence pattern, boolean caseInsensitive) {
        int m = pattern.length();
        Map<Character, Integer> map = new HashMap<>(Math.min(m, 128));

        for (int i = 0; i < m; i++) {
            char c = pattern.charAt(i);
            int jump = m - i;

            if (caseInsensitive) {
                map.put(Character.toLowerCase(c), jump);
            } else {
                map.put(c, jump);
            }
        }
        return map;
    }

    /**
     * Builds the reverse offset map for the Sunday algorithm (adapted for backward searching).
     * <p>
     * The offset indicates how far to jump backwards if the character preceding the current window match attempt does
     * not match. It maps characters in the pattern to their distance from the start of the pattern.
     * </p>
     *
     * @param pattern         The pattern string.
     * @param caseInsensitive Whether the search is case-insensitive.
     * @return A map of character offsets.
     */
    private static Map<Character, Integer> buildReverseOffsetMap(CharSequence pattern, boolean caseInsensitive) {
        int m = pattern.length();
        Map<Character, Integer> map = new HashMap<>(Math.min(m, 128));

        for (int i = m - 1; i >= 0; i--) {
            char c = pattern.charAt(i);
            int jump = i + 1;

            if (caseInsensitive) {
                map.put(Character.toLowerCase(c), jump);
            } else {
                map.put(c, jump);
            }
        }
        return map;
    }

    /**
     * Finds the start position of the string to search for.
     * <p>
     * Uses the Sunday algorithm for efficient substring searching.
     * </p>
     *
     * @param from The position to start searching from.
     * @return The start position of the match, or -1 if no match is found.
     */
    @Override
    public int start(int from) {
        Assert.notNull(this.text, "Text to find must be not null!");
        final int subLen = strToFind.length();
        final int textLen = text.length();

        // Efficient substring query based on the Sunday algorithm
        if (negative) {
            // Backward search
            if (this.reverseOffsetMap == null) {
                this.reverseOffsetMap = buildReverseOffsetMap(strToFind, caseInsensitive);
            }
            int maxIndex = textLen - subLen;
            if (from > maxIndex) {
                from = maxIndex;
            }
            int i = from;
            while (i >= 0) {
                if (CharsBacker.isSubEquals(text, i, strToFind, 0, subLen, caseInsensitive)) {
                    return i;
                }
                if (i - 1 < 0) {
                    break;
                }
                // Calculate jump based on the character preceding the current window
                char preChar = text.charAt(i - 1);
                int jump = reverseOffsetMap
                        .getOrDefault(caseInsensitive ? Character.toLowerCase(preChar) : preChar, subLen + 1);
                i -= jump;
            }
        } else {
            // Forward search
            if (this.forwardOffsetMap == null) {
                this.forwardOffsetMap = buildForwardOffsetMap(strToFind, caseInsensitive);
            }
            if (from < 0) {
                from = 0;
            }
            int endLimit = textLen - subLen;
            int i = from;
            while (i <= endLimit) {
                if (CharsBacker.isSubEquals(text, i, strToFind, 0, subLen, caseInsensitive)) {
                    return i;
                }
                if (i + subLen >= textLen) {
                    break;
                }
                // Calculate jump based on the character following the current window
                char nextChar = text.charAt(i + subLen);
                int jump = forwardOffsetMap
                        .getOrDefault(caseInsensitive ? Character.toLowerCase(nextChar) : nextChar, subLen + 1);
                i += jump;
            }
        }

        return Normal.__1;
    }

    /**
     * Returns the end position of the match.
     *
     * @param start The start position of the match.
     * @return The end position, or -1 if the start position is invalid.
     */
    @Override
    public int end(final int start) {
        if (start < 0) {
            return -1;
        }
        return start + strToFind.length();
    }

}
