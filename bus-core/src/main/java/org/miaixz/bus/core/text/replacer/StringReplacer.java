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
import java.io.Serializable;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * Abstract string replacer class. Subclasses implement the {@link #replace(CharSequence, int, StringBuilder)} method to
 * define specific replacement logic.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class StringReplacer implements UnaryOperator<CharSequence>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852239827580L;

    /**
     * Abstract method to perform a partial string replacement. This method defines the logic for replacing a portion of
     * the input text.
     *
     * @param text The character sequence being processed.
     * @param pos  The current position in the character sequence.
     * @param out  The {@code StringBuilder} to which the replaced or original characters are appended.
     * @return The number of characters consumed from the input {@code text} by this replacement operation. Return 0 to
     *         indicate that no replacement occurred at the current position, and the original character at {@code pos}
     *         should be appended.
     */
    protected abstract int replace(CharSequence text, int pos, StringBuilder out);

    /**
     * Executes the replacement operation on the given character sequence. It iterates through the input text, applying
     * the replacement logic defined in {@link #replace(CharSequence, int, StringBuilder)} to relevant parts, and
     * keeping other parts unchanged.
     *
     * @param text The character sequence to be processed.
     * @return The character sequence after all replacements have been applied.
     */
    @Override
    public CharSequence apply(final CharSequence text) {
        if (StringKit.isEmpty(text)) {
            return text;
        }
        final int len = text.length();
        final StringBuilder builder = new StringBuilder(len);
        int pos = 0;// Current position
        int consumed;// Number of characters processed
        while (pos < len) {
            consumed = replace(text, pos, builder);
            if (0 == consumed) {
                // If 0 characters are consumed, it means no replacement occurred at this position.
                // Append the original character and move to the next position.
                builder.append(text.charAt(pos));
                pos++;
            }
            pos += consumed;
        }
        return builder;
    }

}
