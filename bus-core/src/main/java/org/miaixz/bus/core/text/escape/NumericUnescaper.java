/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.text.escape;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.translate.CharSequenceTranslator;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.EnumSet;

/**
 * Translate XML numeric entities of the form &amp;#[xX]?\d+;? to
 * the specific codepoint.
 * <p>
 * Note that the semi-colon is optional.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class NumericUnescaper extends CharSequenceTranslator {

    /**
     * EnumSet of OPTIONS, given from the constructor.
     */
    private final EnumSet<OPTION> options;

    /**
     * Create a UnicodeUnescaper.
     * <p>
     * The constructor takes a list of options, only one type of which is currently
     * available (whether to allow, error or ignore the semi-colon on the end of a
     * numeric entity to being missing).
     * <p>
     * For example, to support numeric entities without a ';':
     * new NumericUnescaper(NumericUnescaper.OPTION.semiColonOptional)
     * and to throw an IllegalArgumentException when they're missing:
     * new NumericUnescaper(NumericUnescaper.OPTION.errorIfNoSemiColon)
     * <p>
     * Note that the default behaviour is to ignore them.
     *
     * @param options to apply to this unescaper
     */
    public NumericUnescaper(final OPTION... options) {
        if (options.length > 0) {
            this.options = EnumSet.copyOf(Arrays.asList(options));
        } else {
            this.options = EnumSet.copyOf(Arrays.asList(OPTION.semiColonRequired));
        }
    }

    /**
     * Whether the passed in option is currently set.
     *
     * @param option to check state of
     * @return whether the option is set
     */
    public boolean isSet(final OPTION option) {
        return null != options && options.contains(option);
    }


    @Override
    public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
        final int seqEnd = input.length();
        // Uses -2 to ensure there is something after the &#
        if (input.charAt(index) == Symbol.C_AND && index < seqEnd - 2 && input.charAt(index + 1) == Symbol.C_SHAPE) {
            int start = index + 2;
            boolean isHex = false;

            final char firstChar = input.charAt(start);
            if (firstChar == 'x' || firstChar == 'X') {
                start++;
                isHex = true;

                // Check there's more than just an x after the &#
                if (start == seqEnd) {
                    return 0;
                }
            }

            int end = start;
            // Note that this supports character codes without a ; on the end
            while (end < seqEnd && (input.charAt(end) >= Symbol.C_ZERO && input.charAt(end) <= Symbol.C_NINE
                    || input.charAt(end) >= 'a' && input.charAt(end) <= 'f'
                    || input.charAt(end) >= 'A' && input.charAt(end) <= 'F')) {
                end++;
            }

            final boolean semiNext = end != seqEnd && input.charAt(end) == Symbol.C_SEMICOLON;

            if (!semiNext) {
                if (isSet(OPTION.semiColonRequired)) {
                    return 0;
                }
                if (isSet(OPTION.errorIfNoSemiColon)) {
                    throw new IllegalArgumentException("Semi-colon required at end of numeric entity");
                }
            }

            int entityValue;
            try {
                if (isHex) {
                    entityValue = Integer.parseInt(input.subSequence(start, end).toString(), Normal._16);
                } else {
                    entityValue = Integer.parseInt(input.subSequence(start, end).toString(), 10);
                }
            } catch (final NumberFormatException nfe) {
                return 0;
            }

            if (entityValue > 0xFFFF) {
                final char[] chrs = Character.toChars(entityValue);
                out.write(chrs[0]);
                out.write(chrs[1]);
            } else {
                out.write(entityValue);
            }

            return 2 + end - start + (isHex ? 1 : 0) + (semiNext ? 1 : 0);
        }
        return 0;
    }

    /**
     * NumericUnescaper option enum.
     */
    public enum OPTION {semiColonRequired, semiColonOptional, errorIfNoSemiColon}
}
