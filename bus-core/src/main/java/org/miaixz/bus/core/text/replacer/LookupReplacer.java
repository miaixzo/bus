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

    /**
     * The serial version UID.
     */
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
