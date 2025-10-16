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
package org.miaixz.bus.extra.nlp.provider.analysis;

import java.io.Serial;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Attribute;
import org.miaixz.bus.extra.nlp.NLPWord;

/**
 * Wrapper class for a single word (Attribute) from Lucene-analysis word segmentation. This class adapts the Lucene
 * {@link Attribute} (specifically {@link CharTermAttribute} and {@link OffsetAttribute}) to the common {@link NLPWord}
 * interface, providing a unified way to access segmented word information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnalysisWord implements NLPWord {

    /**
     * The serialization version identifier for this class.
     */
    @Serial
    private static final long serialVersionUID = 2852285660095L;

    /**
     * The underlying Lucene {@link Attribute} object, typically a {@link CharTermAttribute}.
     */
    private final Attribute word;

    /**
     * Constructs an {@code AnalysisWord} instance by wrapping a Lucene {@link CharTermAttribute}.
     *
     * @param word The {@link CharTermAttribute} object from Lucene analysis.
     */
    public AnalysisWord(final CharTermAttribute word) {
        this.word = word;
    }

    /**
     * Retrieves the text of the word from the wrapped Lucene {@link Attribute}.
     *
     * @return The text of the word as a {@link String}.
     */
    @Override
    public String getText() {
        return word.toString();
    }

    /**
     * Retrieves the starting character offset of this word within the original text. This method checks if the
     * underlying attribute is an instance of {@link OffsetAttribute} and returns the start offset if available.
     *
     * @return The starting position (inclusive) of the word, or -1 if not available.
     */
    @Override
    public int getStartOffset() {
        if (this.word instanceof OffsetAttribute) {
            return ((OffsetAttribute) this.word).startOffset();
        }
        return -1;
    }

    /**
     * Retrieves the ending character offset of this word within the original text. This method checks if the underlying
     * attribute is an instance of {@link OffsetAttribute} and returns the end offset if available.
     *
     * @return The ending position (exclusive) of the word, or -1 if not available.
     */
    @Override
    public int getEndOffset() {
        if (this.word instanceof OffsetAttribute) {
            return ((OffsetAttribute) this.word).endOffset();
        }
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
