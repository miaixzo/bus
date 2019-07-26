package org.aoju.bus.core.text.escape;

import org.aoju.bus.core.text.translate.CodePointTranslator;

import java.io.Writer;

/**
 * Helper subclass to CharSequenceTranslator to remove unpaired surrogates.
 *
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public class UnicodeUnpaired extends CodePointTranslator {

    @Override
    public boolean translate(final int codepoint, final Writer out) {
        return codepoint >= Character.MIN_SURROGATE && codepoint <= Character.MAX_SURROGATE;
    }
}

