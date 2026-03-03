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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A replacer that performs replacements based on a lookup map. It searches for specific keywords and replaces them with
 * their corresponding values.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class LookupReplacer extends StringReplacer {

    @Serial
    private static final long serialVersionUID = 2852239213366L;

    /**
     * The map containing lookup keys and their replacement values.
     */
    private final Map<String, String> lookupMap;
    /**
     * A set of characters representing the first character of each lookup key, used for quick pre-filtering.
     */
    private final Set<Character> keyPrefixSkeyet;
    /**
     * The minimum length of a lookup key.
     */
    private final int minLength;
    /**
     * The maximum length of a lookup key.
     */
    private final int maxLength;

    /**
     * Constructs a new {@code LookupReplacer} with the given lookup key-value pairs.
     *
     * @param lookup An array of String arrays, where each inner array represents a key-value pair (e.g., {@code new
     *               String[]{"key", "value"}}).
     */
    public LookupReplacer(final String[]... lookup) {
        this.lookupMap = new HashMap<>(lookup.length, 1);
        this.keyPrefixSkeyet = new HashSet<>(lookup.length, 1);

        int minLength = Integer.MAX_VALUE;
        int maxLength = 0;
        String key;
        int keySize;
        for (final String[] pair : lookup) {
            key = pair[0];
            lookupMap.put(key, pair[1]);
            this.keyPrefixSkeyet.add(key.charAt(0));
            keySize = key.length();
            if (keySize > maxLength) {
                maxLength = keySize;
            }
            if (keySize < minLength) {
                minLength = keySize;
            }
        }
        this.maxLength = maxLength;
        this.minLength = minLength;
    }

    /**
     * Replaces a portion of the text based on the lookup map. It checks if the character at the current position
     * matches any key's first character. If so, it attempts to find the longest matching key and replaces it with its
     * corresponding value.
     *
     * @param text The text to be processed.
     * @param pos  The current position in the text.
     * @param out  The {@code StringBuilder} to which the replaced text is appended.
     * @return The number of characters consumed by the replacement, or 0 if no replacement occurred.
     */
    @Override
    protected int replace(final CharSequence text, final int pos, final StringBuilder out) {
        if (keyPrefixSkeyet.contains(text.charAt(pos))) {
            int max = this.maxLength;
            if (pos + this.maxLength > text.length()) {
                max = text.length() - pos;
            }
            CharSequence subSeq;
            String result;
            for (int i = max; i >= this.minLength; i--) {
                subSeq = text.subSequence(pos, pos + i);
                result = lookupMap.get(subSeq.toString());
                if (null != result) {
                    out.append(result);
                    return i;
                }
            }
        }
        return 0;
    }

}
