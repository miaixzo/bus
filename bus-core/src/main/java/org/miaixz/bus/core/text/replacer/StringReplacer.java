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
import java.io.Serializable;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.xyz.StringKit;

/**
 * Abstract string replacer class. Subclasses implement the {@link #replace(CharSequence, int, StringBuilder)} method to
 * define specific replacement logic.
 *
 * @author Kimi Liu
 * @since Java 17+
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
