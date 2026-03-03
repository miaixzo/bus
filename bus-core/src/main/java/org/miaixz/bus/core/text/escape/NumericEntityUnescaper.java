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
package org.miaixz.bus.core.text.escape;

import java.io.Serial;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.replacer.StringReplacer;
import org.miaixz.bus.core.xyz.CharKit;

/**
 * Unescaper for numeric entities, such as {@code &#123;} or {@code &#xABC;}. This class handles both decimal and
 * hexadecimal numeric character references.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NumericEntityUnescaper extends StringReplacer {

    @Serial
    private static final long serialVersionUID = 2852236101870L;

    /**
     * Replaces a numeric entity (e.g., {@code &#123;}, {@code &#xABC;}) with its corresponding character.
     *
     * @param text The text to be unescaped.
     * @param pos  The current position in the text.
     * @param out  The {@link StringBuilder} to which the unescaped character is appended.
     * @return The number of characters consumed from the input text if a numeric entity was found and unescaped,
     *         otherwise 0.
     */
    @Override
    protected int replace(final CharSequence text, final int pos, final StringBuilder out) {
        final int len = text.length();
        // Check to ensure it starts with '&#'
        if (text.charAt(pos) == Symbol.C_AND && pos < len - 2 && text.charAt(pos + 1) == Symbol.C_HASH) {
            int start = pos + 2;
            boolean isHex = false;
            final char firstChar = text.charAt(start);
            if (firstChar == 'x' || firstChar == 'X') {
                start++;
                isHex = true;
            }

            // Ensure there are digits after '&#'
            if (start == len) {
                return 0;
            }

            int end = start;
            while (end < len && CharKit.isHexChar(text.charAt(end))) {
                end++;
            }
            final boolean isSemiNext = (end != len) && (text.charAt(end) == Symbol.C_SEMICOLON);
            if (isSemiNext) {
                final int entityValue;
                try {
                    if (isHex) {
                        entityValue = Integer.parseInt(text.subSequence(start, end).toString(), 16);
                    } else {
                        entityValue = Integer.parseInt(text.subSequence(start, end).toString(), 10);
                    }
                } catch (final NumberFormatException nfe) {
                    return 0;
                }
                out.append((char) entityValue);
                return 2 + end - start + (isHex ? 1 : 0) + 1;
            }
        }
        return 0;
    }

}
