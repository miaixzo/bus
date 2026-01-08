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
package org.miaixz.bus.extra.nlp.provider.word;

import java.io.Serial;

import org.miaixz.bus.extra.nlp.NLPWord;

/**
 * Wrapper class for a single word from the Word word segmentation library. This class adapts the
 * {@link org.apdplat.word.segmentation.Word} object to the common {@link NLPWord} interface, providing a unified way to
 * access segmented word information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class WordWord implements NLPWord {

    /**
     * The serialization version identifier for this class.
     */
    @Serial
    private static final long serialVersionUID = 2852286932330L;

    /**
     * The underlying {@link org.apdplat.word.segmentation.Word} object from the Word library.
     */
    private final org.apdplat.word.segmentation.Word word;

    /**
     * Constructs a {@code WordWord} instance by wrapping a Word library's {@link org.apdplat.word.segmentation.Word}
     * object.
     *
     * @param word The {@link org.apdplat.word.segmentation.Word} object from Word word segmentation.
     */
    public WordWord(final org.apdplat.word.segmentation.Word word) {
        this.word = word;
    }

    /**
     * Retrieves the text of the word from the wrapped Word library's {@link org.apdplat.word.segmentation.Word}.
     *
     * @return The text of the word as a {@link String}.
     */
    @Override
    public String getText() {
        return word.getText();
    }

    /**
     * Retrieves the starting character offset of this word within the original text. Note: The Word library's
     * {@link org.apdplat.word.segmentation.Word} does not directly provide offset information, so this method returns
     * -1.
     *
     * @return The starting position (inclusive) of the word, or -1 if not available.
     */
    @Override
    public int getStartOffset() {
        return -1;
    }

    /**
     * Retrieves the ending character offset of this word within the original text. Note: The Word library's
     * {@link org.apdplat.word.segmentation.Word} does not directly provide offset information, so this method returns
     * -1.
     *
     * @return The ending position (exclusive) of the word, or -1 if not available.
     */
    @Override
    public int getEndOffset() {
        return -1;
    }

    /**
     * Returns the textual representation of this word, which is the same as {@link #getText()}.
     *
     * @return The text of the word.
     */
    @Override
    public String toString() {
        return getText();
    }

}
