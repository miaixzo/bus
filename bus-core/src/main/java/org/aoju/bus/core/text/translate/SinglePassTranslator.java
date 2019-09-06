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
import java.io.Writer;

/**
 * Abstract translator for processing whole input in single pass.
 * Handles initial index checking and counting of returned code points.
 *
 * @author Kimi Liu
 * @version 3.2.0
 * @since JDK 1.8
 */
abstract class SinglePassTranslator extends CharSequenceTranslator {

    @Override
    public int translate(final CharSequence input, final int index, final Writer out) throws IOException {
        if (index != 0) {
            throw new IllegalArgumentException(getClassName() + ".translate(final CharSequence input, final int "
                    + "index, final Writer out) can not handle a non-zero index.");
        }

        translateWhole(input, out);

        return Character.codePointCount(input, index, input.length());
    }

    /**
     * A utility method to be used in the {@link #translate(CharSequence, int, Writer)} method.
     *
     * @return the name of this or the extending class.
     */
    private String getClassName() {
        final Class<? extends SinglePassTranslator> clazz = this.getClass();
        return clazz.isAnonymousClass() ? clazz.getName() : clazz.getSimpleName();
    }

    /**
     * Translate whole set of code points passed in input.
     *
     * @param input CharSequence that is being translated
     * @param out   Writer to translate the text to
     * @throws IOException if and only if the Writer produces an IOException
     */
    abstract void translateWhole(CharSequence input, Writer out) throws IOException;
}
