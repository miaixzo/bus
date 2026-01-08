/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.extra.nlp.provider.ansj;

import java.io.Serial;

import org.ansj.domain.Term;
import org.miaixz.bus.extra.nlp.NLPWord;

/**
 * Wrapper for a word in Ansj segmentation.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnsjWord implements NLPWord {

    @Serial
    private static final long serialVersionUID = 2852285815563L;

    /**
     * The Term object from the Ansj library.
     */
    private final Term term;

    /**
     * Constructor.
     *
     * @param term The {@link Term} object.
     */
    public AnsjWord(final Term term) {
        this.term = term;
    }

    /**
     * Gets the text of the word.
     *
     * @return The text of the word.
     */
    @Override
    public String getText() {
        return term.getName();
    }

    /**
     * Gets the start offset of the word in the original text.
     *
     * @return The start offset.
     */
    @Override
    public int getStartOffset() {
        return this.term.getOffe();
    }

    /**
     * Gets the end offset of the word in the original text.
     *
     * @return The end offset.
     */
    @Override
    public int getEndOffset() {
        return getStartOffset() + getText().length();
    }

    /**
     * Returns the text of the word.
     *
     * @return The text of the word.
     */
    @Override
    public String toString() {
        return getText();
    }

}
