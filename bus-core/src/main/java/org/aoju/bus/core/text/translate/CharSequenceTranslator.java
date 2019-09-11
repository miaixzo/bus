/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.text.translate;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;


/**
 * An API for translating text.
 * Its core use is to escape and unescape text. Because escaping and unescaping
 * is completely contextual, the API does not present two separate signatures.
 *
 * @author Kimi Liu
 * @version 3.2.6
 * @since JDK 1.8
 */
public abstract class CharSequenceTranslator {

    /**
     * <p>Returns an upper case hexadecimal <code>String</code> for the given
     * character.</p>
     *
     * @param codepoint The codepoint to convert.
     * @return An upper case hexadecimal <code>String</code>
     */
    public static String hex(final int codepoint) {
        return Integer.toHexString(codepoint).toUpperCase(Locale.ENGLISH);
    }

    /**
     * Translate a set of codepoints, represented by an int index into a CharSequence,
     * into another set of codepoints. The number of codepoints consumed must be returned,
     * and the only IOExceptions thrown must be from interacting with the Writer so that
     * the top level API may reliably ignore StringWriter IOExceptions.
     *
     * @param input CharSequence that is being translated
     * @param index int representing the current point of translation
     * @param out   Writer to translate the text to
     * @return int count of codepoints consumed
     * @throws IOException if and only if the Writer produces an IOException
     */
    public abstract int translate(CharSequence input, int index, Writer out) throws IOException;

    /**
     * Helper for non-Writer usage.
     *
     * @param input CharSequence to be translated
     * @return String output of translation
     */
    public final String translate(final CharSequence input) {
        if (input == null) {
            return null;
        }
        try {
            final StringWriter writer = new StringWriter(input.length() * 2);
            translate(input, writer);
            return writer.toString();
        } catch (final IOException ioe) {
            // this should never ever happen while writing to a StringWriter
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Translate an input onto a Writer. This is intentionally final as its algorithm is
     * tightly coupled with the abstract method of this class.
     *
     * @param input CharSequence that is being translated
     * @param out   Writer to translate the text to
     * @throws IOException if and only if the Writer produces an IOException
     */
    public final void translate(final CharSequence input, final Writer out) throws IOException {
        if (input == null) {
            return;
        }
        int pos = 0;
        final int len = input.length();
        while (pos < len) {
            final int consumed = translate(input, pos, out);
            if (consumed == 0) {
                // inlined implementation of Character.toChars(Character.codePointAt(input, pos))
                // avoids allocating temp char arrays and duplicate checks
                final char c1 = input.charAt(pos);
                out.write(c1);
                pos++;
                if (Character.isHighSurrogate(c1) && pos < len) {
                    final char c2 = input.charAt(pos);
                    if (Character.isLowSurrogate(c2)) {
                        out.write(c2);
                        pos++;
                    }
                }
                continue;
            }
            // contract with translators is that they have to understand codepoints
            // and they just took care of a surrogate pair
            for (int pt = 0; pt < consumed; pt++) {
                pos += Character.charCount(Character.codePointAt(input, pos));
            }
        }
    }

    /**
     * Helper method to create a merger of this translator with another set of
     * translators. Useful in customizing the standard functionality.
     *
     * @param translators CharSequenceTranslator array of translators to merge with this one
     * @return CharSequenceTranslator merging this translator with the others
     */
    public final CharSequenceTranslator with(final CharSequenceTranslator... translators) {
        final CharSequenceTranslator[] newArray = new CharSequenceTranslator[translators.length + 1];
        newArray[0] = this;
        System.arraycopy(translators, 0, newArray, 1, translators.length);
        return new AggregateTranslator(newArray);
    }

}
