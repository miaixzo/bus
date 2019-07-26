package org.aoju.bus.core.text.translate;

import java.io.IOException;
import java.io.Writer;

/**
 * Abstract translator for processing whole input in single pass.
 * Handles initial index checking and counting of returned code points.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
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
