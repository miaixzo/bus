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
package org.miaixz.bus.extra.nlp.provider.mmseg;

import java.io.Serial;

import org.miaixz.bus.extra.nlp.NLPWord;

/**
 * Wrapper class for a single word from mmseg4j word segmentation. This class adapts the {@link com.chenlb.mmseg4j.Word}
 * object to the common {@link NLPWord} interface, providing a unified way to access segmented word information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MmsegWord implements NLPWord {

    /**
     * The serialization version identifier for this class.
     */
    @Serial
    private static final long serialVersionUID = 2852286361503L;

    /**
     * The underlying mmseg4j {@link com.chenlb.mmseg4j.Word} object.
     */
    private final com.chenlb.mmseg4j.Word word;

    /**
     * Constructs a {@code MmsegWord} instance by wrapping an mmseg4j {@link com.chenlb.mmseg4j.Word}.
     *
     * @param word The {@link com.chenlb.mmseg4j.Word} object from mmseg4j word segmentation.
     */
    public MmsegWord(final com.chenlb.mmseg4j.Word word) {
        this.word = word;
    }

    /**
     * Retrieves the text of the word from the wrapped mmseg4j {@link com.chenlb.mmseg4j.Word}.
     *
     * @return The text of the word as a {@link String}.
     */
    @Override
    public String getText() {
        return word.getString();
    }

    /**
     * Retrieves the starting character offset of this word within the original text. This delegates to the
     * {@code getStartOffset()} method of the mmseg4j {@link com.chenlb.mmseg4j.Word}.
     *
     * @return The starting position (inclusive) of the word.
     */
    @Override
    public int getStartOffset() {
        return this.word.getStartOffset();
    }

    /**
     * Retrieves the ending character offset of this word within the original text. This delegates to the
     * {@code getEndOffset()} method of the mmseg4j {@link com.chenlb.mmseg4j.Word}.
     *
     * @return The ending position (exclusive) of the word.
     */
    @Override
    public int getEndOffset() {
        return this.word.getEndOffset();
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
